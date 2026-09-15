const API_BASE = "/api/missions";

const els = {
    form: document.getElementById("missionForm"),
    formHeader: document.getElementById("formHeader"),
    missionId: document.getElementById("missionId"),
    missionName: document.getElementById("missionName"),
    agency: document.getElementById("agency"),
    launchDate: document.getElementById("launchDate"),
    orbit: document.getElementById("orbit"),
    status: document.getElementById("status"),
    formAlert: document.getElementById("formAlert"),
    saveBtn: document.getElementById("saveBtn"),
    cancelBtn: document.getElementById("cancelBtn"),
    tableBody: document.getElementById("missionsTableBody"),
    emptyState: document.getElementById("emptyState"),
    searchInput: document.getElementById("searchInput"),
    searchBtn: document.getElementById("searchBtn"),
    statTotal: document.getElementById("statTotal"),
    statActive: document.getElementById("statActive"),
    statPlanned: document.getElementById("statPlanned"),
};

const ORBIT_LABELS = { LEO: "LEO", GEO: "GEO", MEO: "MEO", DEEP_SPACE: "Deep Space" };
const STATUS_LABELS = { ACTIVE: "Active", PLANNED: "Planned", DECOMMISSIONED: "Decommissioned" };
const STATUS_BADGE_CLASS = {
    ACTIVE: "badge-status-active",
    PLANNED: "badge-status-planned",
    DECOMMISSIONED: "badge-status-decommissioned",
};

document.addEventListener("DOMContentLoaded", () => {
    loadMissions();
    els.form.addEventListener("submit", onSubmit);
    els.cancelBtn.addEventListener("click", resetForm);
    els.searchBtn.addEventListener("click", onSearch);
    els.searchInput.addEventListener("keyup", (e) => {
        if (e.key === "Enter") onSearch();
        if (els.searchInput.value.trim() === "") loadMissions();
    });
});

async function loadMissions() {
    try {
        const res = await fetch(API_BASE);
        if (!res.ok) throw new Error("Failed to load missions");
        const missions = await res.json();
        renderMissions(missions);
        renderStats(missions);
    } catch (err) {
        console.error(err);
        showTableError("Could not reach the API. Is the backend running on localhost:8080?");
    }
}

async function onSearch() {
    const keyword = els.searchInput.value.trim();
    if (!keyword) {
        loadMissions();
        return;
    }
    try {
        const res = await fetch(`${API_BASE}/search?keyword=${encodeURIComponent(keyword)}`);
        if (!res.ok) throw new Error("Search failed");
        const missions = await res.json();
        renderMissions(missions);
    } catch (err) {
        console.error(err);
        showTableError("Search failed. Please try again.");
    }
}

function renderStats(missions) {
    els.statTotal.textContent = missions.length;
    els.statActive.textContent = missions.filter((m) => m.status === "ACTIVE").length;
    els.statPlanned.textContent = missions.filter((m) => m.status === "PLANNED").length;
}

function renderMissions(missions) {
    els.tableBody.innerHTML = "";

    if (!missions || missions.length === 0) {
        els.emptyState.classList.remove("d-none");
        return;
    }
    els.emptyState.classList.add("d-none");

    missions.forEach((m) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td class="fw-semibold">${escapeHtml(m.missionName)}</td>
            <td>${escapeHtml(m.agency)}</td>
            <td>${m.launchDate}</td>
            <td><span class="badge-orbit">${ORBIT_LABELS[m.orbit] ?? m.orbit}</span></td>
            <td><span class="${STATUS_BADGE_CLASS[m.status] ?? ""}">${STATUS_LABELS[m.status] ?? m.status}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary me-1 edit-btn" data-id="${m.id}">
                    <i class="fa-solid fa-pen me-1"></i>Edit
                </button>
                <button class="btn btn-sm btn-outline-danger delete-btn" data-id="${m.id}">
                    <i class="fa-solid fa-trash me-1"></i>Delete
                </button>
            </td>
        `;
        els.tableBody.appendChild(tr);
    });

    document.querySelectorAll(".edit-btn").forEach((btn) =>
        btn.addEventListener("click", () => startEdit(btn.dataset.id))
    );
    document.querySelectorAll(".delete-btn").forEach((btn) =>
        btn.addEventListener("click", () => deleteMission(btn.dataset.id))
    );
}

async function startEdit(id) {
    try {
        const res = await fetch(`${API_BASE}/${id}`);
        if (!res.ok) throw new Error("Mission not found");
        const mission = await res.json();

        els.missionId.value = mission.id;
        els.missionName.value = mission.missionName;
        els.agency.value = mission.agency;
        els.launchDate.value = mission.launchDate;
        els.orbit.value = mission.orbit;
        els.status.value = mission.status;

        els.formHeader.textContent = "Edit Mission";
        els.saveBtn.textContent = "Update Mission";
        els.cancelBtn.classList.remove("d-none");
        window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (err) {
        console.error(err);
        alert("Could not load mission for editing.");
    }
}

async function deleteMission(id) {
    if (!confirm("Delete this mission? This cannot be undone.")) return;
    try {
        const res = await fetch(`${API_BASE}/${id}`, { method: "DELETE" });
        if (!res.ok && res.status !== 204) throw new Error("Delete failed");
        loadMissions();
    } catch (err) {
        console.error(err);
        alert("Could not delete mission.");
    }
}

async function onSubmit(e) {
    e.preventDefault();
    hideFormAlert();

    if (!els.form.checkValidity()) {
        els.form.classList.add("was-validated");
        return;
    }

    const payload = {
        missionName: els.missionName.value.trim(),
        agency: els.agency.value.trim(),
        launchDate: els.launchDate.value,
        orbit: els.orbit.value,
        status: els.status.value,
    };

    const id = els.missionId.value;
    const isEdit = Boolean(id);
    const url = isEdit ? `${API_BASE}/${id}` : API_BASE;
    const method = isEdit ? "PUT" : "POST";

    try {
        const res = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });

        if (!res.ok) {
            const errorBody = await res.json().catch(() => ({}));
            const message = errorBody.message || "Could not save mission.";
            const fieldErrors = errorBody.fieldErrors
                ? Object.values(errorBody.fieldErrors).join(" ")
                : "";
            showFormAlert(`${message} ${fieldErrors}`.trim());
            return;
        }

        resetForm();
        loadMissions();
    } catch (err) {
        console.error(err);
        showFormAlert("Network error - could not reach the API.");
    }
}

function resetForm() {
    els.form.reset();
    els.form.classList.remove("was-validated");
    els.missionId.value = "";
    els.formHeader.textContent = "Add New Mission";
    els.saveBtn.textContent = "Save Mission";
    els.cancelBtn.classList.add("d-none");
    hideFormAlert();
}

function showFormAlert(message) {
    els.formAlert.textContent = message;
    els.formAlert.classList.remove("d-none");
}

function hideFormAlert() {
    els.formAlert.classList.add("d-none");
    els.formAlert.textContent = "";
}

function showTableError(message) {
    els.tableBody.innerHTML = "";
    els.emptyState.textContent = message;
    els.emptyState.classList.remove("d-none");
}

function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str ?? "";
    return div.innerHTML;
}
