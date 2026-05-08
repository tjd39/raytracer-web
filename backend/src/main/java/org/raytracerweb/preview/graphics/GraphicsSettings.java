package org.raytracerweb.preview.graphics;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

import org.raytracerweb.preview.canvas.Pixel;

public class GraphicsSettings {
    private int imageWidth;
    private int imageHeight;
    private int renderLevels;
    private int antiAlias;
    private boolean accumulateImage;
    private boolean debugAABBs;

    public GraphicsSettings(int imageWidth, int imageHeight, int renderLevels, int antiAlias, boolean accumulateImage,
            boolean debugAABBs) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.renderLevels = renderLevels;
        this.antiAlias = antiAlias;
        this.accumulateImage = accumulateImage;
        this.debugAABBs = debugAABBs;
    }

    public int imageWidth() {
        return imageWidth;
    }

    public int imageHeight() {
        return imageHeight;
    }

    public int renderLevels() {
        return renderLevels;
    }

    public int antiAlias() {
        return antiAlias;
    }

    public boolean accumulateImage() {
        return accumulateImage;
    }

    public boolean debugAABBs() {
        return debugAABBs;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public void setRenderLevels(int renderLevels) {
        this.renderLevels = renderLevels;
    }

    public void setAntiAlias(int antiAlias) {
        this.antiAlias = antiAlias;
    }

    public void setAccumulateImage(boolean accumulateImage) {
        this.accumulateImage = accumulateImage;
    }

    public void setDebugAABBs(boolean debugAABBs) {
        this.debugAABBs = debugAABBs;
    }

    public LinkedBlockingQueue<Pixel> sharedPixelQueue() {
        final LinkedBlockingQueue<Pixel> sharedPixelQueue = new LinkedBlockingQueue<>(imageHeight() * imageWidth());

        try {
            for (int y = 0; y < imageHeight(); y++) {
                for (int x = 0; x < imageWidth(); x++) {
                    sharedPixelQueue.put(new Pixel(x, y));
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Failed to create shared pixel queue: " + e.getMessage());
            System.exit(0);
        }

        return sharedPixelQueue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageWidth, imageHeight, antiAlias);
    }
}
