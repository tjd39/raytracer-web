# Raytracer Web

A browser-accessible raytracer built with a Spring Boot backend and Vue 3 frontend, served via Docker Compose.

## Architecture

```
raytracer-web/
├── backend/        # Spring Boot 3.2.5 (Java 17) — raytracing engine + REST API
├── frontend/       # Vue 3 + Vite — render UI and scene controls
└── docker-compose.yml
```

**Backend layers:**

| Package | Responsibility |
|---|---|
| `api/` | REST controllers and render job management |
| `raytracer/` | `LEMRayTracer`, `AbstractRayTracer`, `RenderEngine`, command pipeline |
| `scene/` | Scene construction (`SceneBuilder`, `ScenePresets`), camera, lights, geometry, materials |
| `preview/` | `Canvas` pixel buffer, `Colour`, `GraphicsSettings` |
| `ray/` | `Ray`, `HitInfo` |
| `geometry/`, `math/` | Rotation matrices, `Vec4`, helpers |

**Frontend:** A single `App.vue` component handles scene selection, render settings (resolution, levels, anti-aliasing, accumulation), camera movement, and polling render status via REST.

## Running Locally

**Requirements:** Docker and Docker Compose.

```bash
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

## REST API

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/render/scenes` | List available scene names |
| `POST` | `/api/render` | Start a render job (returns job ID) |
| `GET` | `/api/render/{id}/status` | Poll job progress (0–100, state) |
| `GET` | `/api/render/{id}/image` | Fetch rendered PNG |
| `DELETE` | `/api/render/{id}` | Cancel a job |
| `POST` | `/api/camera/move` | Move camera and trigger a re-render |

### Render request body

```json
{
  "scene": "simple_ball",
  "width": 500,
  "height": 500,
  "renderLevels": 4,
  "antiAlias": 2,
  "sampleCount": 8
}
```

`sampleCount` defaults to 1 (single pass). Values >1 enable multi-pass accumulation.

### Status response body

```json
{
  "status": "RUNNING",
  "progress": 43,
  "completedSamples": 3,
  "sampleCount": 8
}
```

### Available scenes

| Name | Description |
|---|---|
| `simple_ball` | Single reflective sphere on a textured floor |
| `pool_balls` | Cluster of coloured spheres |
| `sunset_balls` | Warm-toned spheres with coloured sky |
| `cowboy_pingy` | Mesh-heavy scene: Pingy model with cowboy hat, pig, and trees |

## Raytracer

The engine is a **LEM (Light Emissivity Model)** stochastic path tracer. Each pixel fires one primary ray; at each bounce a single outgoing ray is chosen stochastically (diffuse vs. specular based on roughness). Soft shadows emerge naturally from the path tracing — no explicit shadow rays.

Key features:
- **Stochastic single-branch path tracing** — O(levels) rays per pixel instead of O(2^levels)
- **Multi-pass accumulation** — optionally run 2–64 passes; each pass adds to the canvas and the average is tone-mapped before output
- **Reinhard HDR tone mapping** — accumulated pixel values are averaged in linear light space, then per-channel Reinhard (`v / (1+v)`) compresses HDR values before converting back to sRGB; single-pass renders bypass tone mapping entirely
- **BVH acceleration** — mesh objects are subdivided into a bounding volume hierarchy for fast intersection
- **Pre-computed primary rays** — camera rays are built once at tracer construction
- **Thread pool rendering** — pixels distributed across all CPU cores via a static `ExecutorService`
- **Materials** — simple (diffuse/specular/roughness/transparency), iridescent, kaleidoscopic, image texture
- **Post-processing** — film exposure, black-and-white

## Testing

### Backend

Normal test run (unit + integration, excludes benchmarks):

```bash
cd backend
mvn test
```

Test classes:

| Class | What it covers |
|---|---|
| `RenderControllerTest` | REST endpoint behaviour, job lifecycle |
| `RenderJobTest` | Job state transitions |
| `CameraDtoTest` | Camera DTO validation |
| `ScenePresetsTest` | All named scenes build without error |
| `CameraTest` | Camera movement and position |
| `RenderEngineProgressTest` | Progress reporting during a live render |

### Performance benchmarks

Benchmarks are gated behind a Maven profile to keep normal test runs fast:

```bash
cd backend
mvn test -Pbenchmark
```

Three scenarios — LOW (200×200), MEDIUM (500×500), HIGH (500×500 mesh-heavy) — each run with 1 warmup + 3 measured iterations. Results are printed to stdout and should be recorded in `src/test/resources/performance.md` after notable commits.

### Frontend

```bash
cd frontend
npm test
# or during development:
npm run test:watch
```

Tests use Vitest + Vue Test Utils + jsdom. The frontend Dockerfile also runs tests as part of the image build, so a failing test will fail `docker compose up --build`.

## Development (without Docker)

**Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Frontend** (proxies `/api` to `localhost:8080`):
```bash
cd frontend
npm install
npm run dev
```
