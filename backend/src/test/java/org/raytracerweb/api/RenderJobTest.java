package org.raytracerweb.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.raytracerweb.preview.graphics.GraphicsSettings;
import org.raytracerweb.raytracer.IRayTracer;

class RenderJobTest {

    private IRayTracer mockTracer(int w, int h) {
        IRayTracer tracer = mock(IRayTracer.class);
        GraphicsSettings settings = new GraphicsSettings(w, h, 4, 1, false, false);
        when(tracer.graphicsSettings()).thenReturn(settings);
        return tracer;
    }

    @Test
    void initialProgress_isZero() {
        RenderJob job = new RenderJob(mockTracer(100, 100));
        assertThat(job.getProgressPercent()).isZero();
    }

    @Test
    void progressReachesHundred_whenAllPixelsComplete() {
        int w = 10, h = 10;
        RenderJob job = new RenderJob(mockTracer(w, h));
        for (int i = 0; i < w * h; i++) job.incrementPixel();
        assertThat(job.getProgressPercent()).isEqualTo(100);
    }

    @Test
    void progressIsProportional() {
        RenderJob job = new RenderJob(mockTracer(100, 100)); // 10000 pixels
        for (int i = 0; i < 5000; i++) job.incrementPixel();
        assertThat(job.getProgressPercent()).isEqualTo(50);
    }

    @Test
    void progressNeverExceedsHundred() {
        RenderJob job = new RenderJob(mockTracer(10, 10));
        for (int i = 0; i < 200; i++) job.incrementPixel(); // more than 100 pixels
        assertThat(job.getProgressPercent()).isEqualTo(100);
    }

    @Test
    void initialStatus_isPending() {
        RenderJob job = new RenderJob(mockTracer(10, 10));
        assertThat(job.getStatus()).isEqualTo(RenderJob.Status.PENDING);
    }

    @Test
    void statusTransitions_workCorrectly() {
        RenderJob job = new RenderJob(mockTracer(10, 10));
        job.setStatus(RenderJob.Status.RUNNING);
        assertThat(job.getStatus()).isEqualTo(RenderJob.Status.RUNNING);
        job.setStatus(RenderJob.Status.COMPLETE);
        assertThat(job.getStatus()).isEqualTo(RenderJob.Status.COMPLETE);
    }

    @Test
    void idIsUniquePerJob() {
        RenderJob a = new RenderJob(mockTracer(10, 10));
        RenderJob b = new RenderJob(mockTracer(10, 10));
        assertThat(a.getId()).isNotEqualTo(b.getId());
    }
}
