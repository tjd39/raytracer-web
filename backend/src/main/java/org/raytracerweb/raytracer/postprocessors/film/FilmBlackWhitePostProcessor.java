package org.raytracerweb.raytracer.postprocessors.film;

import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

import org.raytracerweb.preview.canvas.Canvas;
import org.raytracerweb.preview.colour.Colour;
import org.raytracerweb.raytracer.engine.core.CommandAbortedException;
import org.raytracerweb.raytracer.engine.core.CommandHandle;
import org.raytracerweb.raytracer.postprocessors.PostProcessor;

/**
 * A post-processor that simulates black and white film.
 */
public class FilmBlackWhitePostProcessor implements PostProcessor {
    private final Canvas inputCanvas;
    private final int scaleFactor;
    private final FilmExposure filmExposure;
    private BufferedImage outputImage;

    public FilmBlackWhitePostProcessor(final Canvas inputCanvas, final int scaleFactor, final FilmExposure filmExposure) {
        this.inputCanvas = inputCanvas;
        this.scaleFactor = scaleFactor;
        this.filmExposure = filmExposure;
    }

    @Override
    public void process(final CommandHandle handle) throws CommandAbortedException {
        final int width = (inputCanvas.getWidth() - 1) * scaleFactor + 1;
        final int height = (inputCanvas.getHeight() - 1) * scaleFactor + 1;
        final boolean[][] grain2DArray = new boolean[width][height];

        for (int x = 0; x < inputCanvas.getWidth() - 1; x++) {
            for (int y = 0; y < inputCanvas.getHeight() - 1; y++) {
                final Colour colour00 = inputCanvas.getPixelValue(x, y);
                for (int i = 0; i < scaleFactor; i++) {
                    for (int j = 0; j < scaleFactor; j++) {
                        if (handle.isCancelled()) {
                            throw new CommandAbortedException();
                        }

                        Colour interpolatedColour;
                        float iLerp = (float) i / scaleFactor;
                        float jLerp = (float) j / scaleFactor;

                        if (i == 0 && j == 0) {
                            interpolatedColour = colour00;
                        } else if (i == 0) {
                            interpolatedColour = Colour.lerp(colour00, inputCanvas.getPixelValue(x, y + 1), jLerp);
                        } else if (j == 0) {
                            interpolatedColour = Colour.lerp(colour00, inputCanvas.getPixelValue(x + 1, y), iLerp);
                        } else {
                            interpolatedColour = Colour.lerp(
                                    Colour.lerp(
                                            colour00,
                                            inputCanvas.getPixelValue(x + 1, y), iLerp),
                                    Colour.lerp(
                                            inputCanvas.getPixelValue(x, y + 1),
                                            inputCanvas.getPixelValue(x + 1, y + 1), iLerp),
                                    jLerp
                            );
                        }
                        grain2DArray[x * scaleFactor + i][y * scaleFactor + j] = ThreadLocalRandom.current().nextFloat() < interpolatedColour.getLuminance();
                    }
                }
            }
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (handle.isCancelled()) {
                    throw new CommandAbortedException();
                }

                image.setRGB(x, y, grainColour(grain2DArray[x][y]));
            }
        }
        outputImage = image;
    }

    public BufferedImage getOutput() {
        return outputImage;
    }

    protected int grainColour(boolean grain) {
        return switch (filmExposure) {
            case NEGATIVE -> grain ? 0x000000 : 0xFFFFFF;
            case POSITIVE -> grain ? 0xFFFFFF : 0x000000;
        };
    }
}

