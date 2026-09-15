# Satellite Mission Tracker

Single Spring Boot 3 + MongoDB application. The REST API and the static
frontend are served together from one Maven project, one port.

## Project layout

```
satellite-mission-tracker/
├── pom.xml
├── lombok.config
└── src/
    ├── main/
    │   ├── java/com/satellite/tracker/
    │   │   ├── SatelliteTrackerApplication.java
    │   │   ├── model/            Mission, Orbit, MissionStatus  (MongoDB document + enums)
    │   │   ├── dto/              MissionDTO                     (API contract)
    │   │   ├── repository/       MissionRepository (Spring Data), MissionRepositoryMetrics (repo-layer metrics)
    │   │   ├── service/          MissionService (interface)
    │   │   │   └── impl/         MissionServiceImpl (CRUD, validation, business rules, service-layer metrics)
    │   │   ├── controller/       MissionController (REST endpoints, controller-layer metrics)
    │   │   ├── exception/        MissionNotFoundException, GlobalExceptionHandler, ErrorResponse
    │   │   └── config/           MetricsConfig (enables @Timed), DataSeeder (demo data)
    │   └── resources/
    │       ├── application.properties
    │       └── static/            Frontend (served automatically by Spring Boot's web server)
    │           ├── index.html
    │           ├── css/style.css
    │           └── js/app.js
    └── test/java/com/satellite/tracker/
        └── MissionServiceImplTest.java
```

Layered/SOLID structure: Controller → Service (interface + impl) →
Repository → Model/DTO, with cross-cutting concerns (exceptions, metrics)
in their own packages.

## Prerequisites

- Java 25
- Maven 3.9+ (or an IDE that bundles it)
- MongoDB running locally on `mongodb://localhost:27017` (or update
  `src/main/resources/application.properties`)

## Run it

```bash
mvn spring-boot:run
```

Then open **http://localhost:8080** — Spring Boot serves `static/index.html`
directly, and the frontend calls the API on the same origin (`/api/missions`),
so nothing else needs to run. No separate frontend server, no CORS config.

On first run, `DataSeeder` inserts three demo missions (Hubble Telescope,
James Webb, Artemis III) if the `missions` collection is empty.

### REST endpoints

| Method | Path                             | Description                        |
|--------|----------------------------------|--------------------------------------|
| POST   | `/api/missions`                  | Create a mission                    |
| GET    | `/api/missions`                  | List all missions                   |
| GET    | `/api/missions/{id}`             | Get one mission by id               |
| PUT    | `/api/missions/{id}`             | Update a mission                    |
| DELETE | `/api/missions/{id}`             | Delete a mission                    |
| GET    | `/api/missions/search?keyword=`  | Search by mission name (contains)   |
| GET    | `/api/missions/status/{status}`  | Filter by status (ACTIVE/PLANNED/DECOMMISSIONED) |
| GET    | `/api/missions/orbit/{orbit}`    | Filter by orbit (LEO/GEO/MEO/DEEP_SPACE) |

Sample create payload:

```json
{
  "missionName": "Voyager 2",
  "agency": "NASA",
  "launchDate": "1977-08-20",
  "orbit": "DEEP_SPACE",
  "status": "ACTIVE"
}
```

### Metrics

`spring-boot-starter-actuator` is enabled. With the app running, inspect:

- `http://localhost:8080/actuator/metrics` — list of recorded metric names,
  including `repository.mission.*` (repository layer), `service.mission.*`
  (service layer), and `controller.mission.*` (controller layer).
- `http://localhost:8080/actuator/metrics/{metric.name}` — details for one metric.
- `http://localhost:8080/actuator/prometheus` — Prometheus scrape format.

## Business rules enforced by the service layer

- Mission names must be unique (case-insensitive).
- A `DECOMMISSIONED` mission can never be moved back to `ACTIVE` or `PLANNED`.
- All required fields are validated both via Bean Validation (`@Valid` on the
  controller) and defensively again in the service layer.

## Lombok

`@Data` / `@Builder` / `@NoArgsConstructor` / `@AllArgsConstructor` generate
the model/DTO boilerplate. Two things in `pom.xml` matter here, both required
rather than optional:

- **Lombok version is pinned to 1.18.46.** Older Lombok releases (anything
  before ~1.18.40) throw `java.lang.ExceptionInInitializerError:
  com.sun.tools.javac.code.TypeTag :: UNKNOWN` on newer JDKs (23+), because
  they poke at javac's internals and those internals changed. 1.18.46 is
  current and supports modern JDKs including 25.
- **`maven-compiler-plugin` explicitly lists Lombok as an annotation
  processor path.** Starting with JDK 23, javac stopped auto-discovering
  annotation processors on the classpath by default. Without this, Lombok
  would silently do nothing on a modern JDK — no error, just missing
  getters/setters. This is not extra configuration you need to maintain;
  it's already done in `pom.xml`.

### Running in IntelliJ IDEA

1. Open the project folder — IntelliJ detects `pom.xml` and imports it as a
   Maven project automatically.
2. **Enable annotation processing:** Settings/Preferences → Build, Execution,
   Deployment → Compiler → Annotation Processors → check "Enable annotation
   processing" (on by default in recent IntelliJ versions when a Maven
   processor path is present, but worth confirming).
3. **Lombok plugin:** Settings/Preferences → Plugins → confirm "Lombok" is
   installed and enabled (bundled with IntelliJ; occasionally disabled).
4. **Project SDK:** File → Project Structure → Project → set the SDK to a
   JDK 25 installation. The pom targets Java 25 (`<java.version>25</java.version>`,
   Spring Boot 3.5.x, Lombok 1.18.40+) — all of which are JDK 25-ready — so
   a mismatched older SDK selected in IntelliJ is the most common cause of
   red squiggles here even when the command-line build is fine.
5. Run `SatelliteTrackerApplication.main()`, or use the Maven panel →
   Plugins → spring-boot → `spring-boot:run`.

If IntelliJ ever shows red squiggles on Lombok-generated methods (`getX()`,
`builder()`, etc.) despite the build succeeding from the command line, that's
almost always step 2 or 3 above, not the `pom.xml`.
