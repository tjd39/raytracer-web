package org.raytracerweb.perf;

import org.junit.jupiter.api.Test;
import org.raytracerweb.preview.canvas.Canvas;
import org.raytracerweb.preview.graphics.GraphicsSettings;
import org.raytracerweb.raytracer.LEMRayTracer;
import org.raytracerweb.raytracer.engine.RenderEngine;
import org.raytracerweb.scene.ScenePresets;
import org.raytracerweb.scene.SceneResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Render benchmarks — run with: mvn test -Pbenchmark
 *
 * Results are printed to stdout and should be recorded in
 * src/test/resources/performance.md after each notable commit.
 *
 * Scenarios:
 *   LOW    sunset_balls  200×200  levels=2  aa=1   (~50ms,  geometry-only)
 *   MEDIUM simple_ball   500×500  levels=4  aa=2   (~1.7s,  normal render workload)
 *   HIGH   cowboy_pingy  500×500  levels=4  aa=2   (~32s,   mesh-heavy BVH stress)
 */
class RenderBenchmarkTest {

    private static final int WARMUP_RUNS = 1;
    private static final int MEASURED_RUNS = 3;

    @Test
    void benchmark_low_sunsetBalls_200x200_l2_aa1() throws InterruptedException {
        run("LOW   ", "sunset_balls", 200, 200, 2, 1);
    }

    @Test
    void benchmark_medium_simpleBall_500x500_l4_aa2() throws InterruptedException {
        run("MEDIUM", "simple_ball", 500, 500, 4, 2);
    }

    @Test
    void benchmark_high_cowboyPingy_500x500_l4_aa2() throws InterruptedException {
        run("HIGH  ", "cowboy_pingy", 500, 500, 4, 2);
    }

    private void run(String tier, String sceneName, int w, int h, int levels, int aa)
            throws InterruptedException {
        SceneResult sr = ScenePresets.forName(sceneName);
        RenderEngine engine = new RenderEngine();

        for (int i = 0; i < WARMUP_RUNS; i++) {
            renderOnce(engine, sr, w, h, levels, aa);
        }

        long total = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (int i = 0; i < MEASURED_RUNS; i++) {
            long ms = renderOnce(engine, sr, w, h, levels, aa);
            total += ms;
            min = Math.min(min, ms);
            max = Math.max(max, ms);
        }
        long avg = total / MEASURED_RUNS;

        System.out.printf("%n[BENCHMARK] %s | %-14s | %4dx%-4d | lvl=%-2d aa=%d | " +
                          "avg=%5dms  min=%5dms  max=%5dms%n",
                tier, sceneName, w, h, levels, aa, avg, min, max);
    }

    private long renderOnce(RenderEngine engine, SceneResult sr, int w, int h, int levels, int aa)
            throws InterruptedException {
        GraphicsSettings settings = new GraphicsSettings(w, h, levels, aa, false, false);
        Canvas canvas = new Canvas(w, h);
        LEMRayTracer tracer = new LEMRayTracer(sr.scene(), sr.camera(), canvas, settings);
        CountDownLatch done = new CountDownLatch(1);
        long start = System.currentTimeMillis();
        engine.executeSingleTrace(tracer, done::countDown, done::countDown, e -> done.countDown());
        done.await(5, TimeUnit.MINUTES);
        return System.currentTimeMillis() - start;
    }
}
