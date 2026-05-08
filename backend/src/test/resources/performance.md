# Render Performance Benchmarks

Machine: Apple M1 Pro (10-core)  
Run with: `mvn test -Pbenchmark` from `backend/`

Each scenario runs 1 warmup render followed by 3 measured renders.
The avg/min/max figures are wall-clock milliseconds.

## Scenarios

| ID     | Scene         | Resolution | levels | aa | Description                      |
|--------|---------------|------------|--------|----|----------------------------------|
| LOW    | sunset_balls  | 200×200    | 2      | 1  | Geometry-only, minimal recursion |
| MEDIUM | simple_ball   | 500×500    | 4      | 2  | Typical single-object render     |
| HIGH   | cowboy_pingy  | 500×500    | 4      | 2  | Mesh-heavy BVH stress test       |

## Results

| Date       | Commit  | LOW avg | LOW min | LOW max | MED avg | MED min | MED max | HIGH avg | HIGH min | HIGH max |
|------------|---------|---------|---------|---------|---------|---------|---------|----------|----------|----------|
| 2026-05-08 | d905030 |   36 ms |   28 ms |   43 ms | 1267 ms | 1144 ms | 1396 ms | 32018 ms | 31833 ms | 32164 ms |
| 2026-05-08 | 7ec4151 |   18 ms |   10 ms |   29 ms |  141 ms |  126 ms |  158 ms |  8853 ms |  8770 ms |  8999 ms |
