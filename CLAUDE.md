# Claude Code — Project Context

This file captures decisions, constraints, and history that aren't obvious from the code.

## What this project is

A dockerised web frontend for a Java raytracer. The backend was ported from a standalone CLI raytracer (`~/gitprojects/personal/raytracer2023`) — that repo is the origin of the scene/math/geometry code. The web layer (REST API, Vue UI, Docker) is new.

## GitHub

- Remote: `git@github.com:tjd39/raytracer-web.git` (personal account `tjd39`, not the Ping work account)
- SSH key: `~/.ssh/claude-tjd39` (ed25519). The per-repo override is set via `git config core.sshCommand`.
- The machine also has a work SSH key (`thomas-dennis_pingcorp`). Always use `-o IdentityAgent=none -o IdentitiesOnly=yes -i ~/.ssh/claude-tjd39` when pushing to avoid the agent offering the wrong key.

## Raytracer model

**LEM = Light Emissivity Model.** Soft shadows are emergent — there is no explicit shadow ray logic. Each object has an emissive colour contribution that propagates through bounces. Do not add hard-shadow logic; it would break the model.

**Stochastic path tracing.** At each bounce, one outgoing ray is chosen at random (diffuse or specular based on roughness probability). This replaced a binary ray tree that was O(2^levels). Multiple render passes can be accumulated and averaged for noise reduction — `RenderEngine.executeAccumulatedTrace()` drives this. Do not restore the binary tree.

**Levels** is recursion depth (max bounces), not sample count. Anti-alias (`aa`) sets the per-pixel sub-pixel grid: `aa=2` means 4 samples per pixel.

## Architecture decisions worth knowing

**No thread-per-frame.** `RenderCommand` uses a static `ExecutorService` (`newFixedThreadPool(availableProcessors())`). Do not reintroduce per-frame thread creation — the pool is intentional to avoid thread churn.

**Canvas is a flat array.** `Canvas.pixelRaster` is `ColourStack[]` indexed `y*width+x`. It has no `synchronized` — different worker threads write to different pixel indices. Do not add synchronisation; do not switch back to `ConcurrentHashMap`.

**Primary rays pre-computed.** `AbstractRayTracer` builds `Ray[] primaryRayCache` at construction. `validateCache()` is a no-op — the cache never needs invalidation because a new tracer is constructed for each render job. Do not restore the old hash-based cache.

**BVH traversal must be closest-hit.** The original implementation returned the first hit from the near child without checking whether the far child had a closer triangle. The fixed version in `BVH.checkIntersection` prunes the far child only if the near hit distance beats the far child's AABB entry distance. Do not simplify this back to first-hit.

**`Vec4.randomHemisphere` uses rejection sampling.** This gives a uniform sphere distribution. The old cosine-weighted approach was incorrect.

**Multi-pass accumulation.** `RenderEngine.executeAccumulatedTrace(tracer, N, ...)` sequences N `RenderCommand` passes on a daemon coordinator thread (`AccumulatorCoordinator`). Each pass uses a `CountDownLatch` to block the coordinator until all worker threads finish, then fires `onSampleComplete`. A single shared `CommandHandle` covers the whole job for cancellation. `AbstractRayTracer.pixelTrace` calls `canvas.addPixelValue` (not `setPixelValue`) when `GraphicsSettings.accumulateImage()` is true.

**`ColourStack.getAvgColour()` applies Reinhard tone mapping only when N > 1.** Accumulation sums raw path-tracer colours which can exceed 1.0 (HDR). The average is computed in linear space, then per-channel Reinhard (`v / (1+v)`) maps `[0,∞)` to `[0,1)` before converting to sRGB. Single-pass renders skip tone mapping so their output is unchanged. Do not apply tone mapping to single-sample stacks.

**`Canvas.clear()` creates empty `ColourStack`s, not black-seeded ones.** A black seed entry was poisoning accumulated averages with a phantom zeroth sample. `ColourStack.getColour()` now guards against an empty list.

## Removed code — do not restore

- `SimpleRayTracer.java` — dead (recursive raycast was 100% commented out)
- `RAY_TRACER.java` — enum with no remaining callers
- `flowControl/` package — `Callback`, `Callback1Arg`, `Callback2Args`, `Supplier`, etc. — all replaced by `Runnable` and `java.util.function.Consumer<Exception>`
- `IRayTracer.getInstance()` static factory — coupling interface to implementations; removed from the interface
- `primaryHitCache` and `AtomicInteger lastHash` in `AbstractRayTracer` — a write-once-read-once map per frame, eliminated in favour of the pre-computed primary ray array

## Testing

- **Normal test run:** `mvn test` from `backend/` — excludes `**/perf/**`
- **Benchmarks:** `mvn test -Pbenchmark` from `backend/` — excludes everything except `RenderBenchmarkTest`
- After perf changes, re-run benchmarks and record results in `backend/src/test/resources/performance.md`
- `RenderControllerTest.getStatus_completedJob_reaches100PercentAndComplete` is a pre-existing flaky test under parallel load; passes in isolation — not caused by our changes

## Performance baseline (2026-05-08)

After the initial 12-optimisation pass:

| Scenario | avg |
|---|---|
| LOW `sunset_balls` 200×200 l=2 aa=1 | 18 ms |
| MEDIUM `simple_ball` 500×500 l=4 aa=2 | 141 ms |
| HIGH `cowboy_pingy` 500×500 l=4 aa=2 | 8853 ms |

Machine: Apple M1 Pro (10-core). See `backend/src/test/resources/performance.md` for full min/max history.

## Scene assets

OBJ and TXT mesh files live under `backend/src/main/resources/org/raytracerweb/scene/objects/`. Loaded lazily via classpath by `MeshLoader`. The `Texture` enum is also lazy — it resolves the classpath URL at first use in `getUrl()`, not at enum static init time.

## Dependency notes

- `commons-math3` is on the classpath but largely unused — the codebase uses its own `Vec4`/rotation matrix implementations
- `jakarta.json` is used by `Canvas` for JSON serialisation of pixel data
