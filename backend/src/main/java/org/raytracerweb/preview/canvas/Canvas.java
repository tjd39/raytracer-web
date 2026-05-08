package org.raytracerweb.preview.canvas;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.raytracerweb.preview.colour.Colour;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class Canvas {
    private final int width;
    private final int height;
    private ColourStack[] pixelRaster;
    private final BufferedImage image;

    public Canvas(final int width, final int height) {
        this.width = width;
        this.height = height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixelRaster = new ColourStack[width * height];
        clear();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Colour getPixelValue(int x, int y) {
        return pixelRaster[y * width + x].getColour();
    }

    public void setPixelValue(final int x, final int y, final Colour colour) {
        pixelRaster[y * width + x] = new ColourStack(colour);
    }

    public void addPixelValue(final int x, final int y, final Colour colour) {
        pixelRaster[y * width + x].addColour(colour);
    }

    public void clear() {
        for (int i = 0; i < pixelRaster.length; i++) {
            pixelRaster[i] = new ColourStack();
        }
    }

    public BufferedImage getBufferedImage() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, pixelRaster[y * width + x].getAvgColour().getRGBValue());
            }
        }
        return image;
    }

    public static Canvas fromImage(URL url) throws IOException {
        BufferedImage bi = ImageIO.read(url);
        if (bi == null) throw new IOException("Could not read image from: " + url);
        Canvas canvas = new Canvas(bi.getWidth(), bi.getHeight());
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                canvas.setPixelValue(x, y, Colour.fromRGBValue(bi.getRGB(x, y)));
            }
        }
        return canvas;
    }

    public static Canvas fromImage(String filePath) throws FileNotFoundException {
        File imageFile = new File(filePath);
        try {
            BufferedImage bi = ImageIO.read(imageFile);
            int pixelWidth = bi.getWidth();
            int pixelHeight = bi.getHeight();
            Canvas canvas = new Canvas(pixelWidth, pixelHeight);
            for (int x = 0; x < pixelWidth; x++) {
                for (int y = 0; y < pixelHeight; y++) {
                    canvas.setPixelValue(x, y, Colour.fromRGBValue(bi.getRGB(x, y)));
                }
            }
            return canvas;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
