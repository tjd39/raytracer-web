package org.raytracerweb.raytracer.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.raytracerweb.preview.canvas.Canvas;
import org.raytracerweb.preview.graphics.GraphicsSettings;
import org.raytracerweb.raytracer.IRayTracer;
import org.raytracerweb.raytracer.LEMRayTracer;
import org.raytracerweb.scene.ScenePresets;
import org.raytracerweb.scene.SceneResult;

class RenderEngineProgressTest {

    @Test
    void pixelCallback_firedForEveryPixel() throws InterruptedException {
        int w = 10, h = 10;
        SceneResult sr = ScenePresets.simpleBall();
        GraphicsSettings settings = new GraphicsSettings(w, h, 1, 1, false, false);
        IRayTracer tracer = new LEMRayTracer(sr.scene(), sr.camera(), new Canvas(w, h), settings);

        AtomicInteger pixelCount = new AtomicInteger(0);
        CountDownLatch done = new CountDownLatch(1);
        RenderEngine engine = new RenderEngine();

        engine.executeSingleTrace(
                tracer,
                done::countDown,
                done::countDown,
                e -> done.countDown(),
                pixelCount::incrementAndGet);

        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(pixelCount.get()).isEqualTo(w * h);
    }

    @Test
    void onComplete_calledAfterAllPixels() throws InterruptedException {
        int w = 5, h = 5;
        SceneResult sr = ScenePresets.simpleBall();
        GraphicsSettings settings = new GraphicsSettings(w, h, 1, 1, false, false);
        IRayTracer tracer = new LEMRayTracer(sr.scene(), sr.camera(), new Canvas(w, h), settings);

        AtomicInteger pixelCount = new AtomicInteger(0);
        CountDownLatch done = new CountDownLatch(1);
        RenderEngine engine = new RenderEngine();

        engine.executeSingleTrace(
                tracer,
                () -> {
                    // At completion time, all pixels should already be counted
                    assertThat(pixelCount.get()).isEqualTo(w * h);
                    done.countDown();
                },
                done::countDown,
                e -> done.countDown(),
                pixelCount::incrementAndGet);

        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
    }
}
