package org.raytracerweb.preview.canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PixelQueue {
    private final List<Pixel> pixels;
//    private Iterator<Pixel> iterator;

    AtomicInteger position = new AtomicInteger(0);

    public PixelQueue(final List<Pixel> pixels) {
        this.pixels = pixels;
        reset();
    }

//    public Pixel next() {
//        return pixels.get(position.incrementAndGet());
////        return iterator.next();
//    }

    public int size() {
        return pixels.size();
    }

    public int position() {
        return position.get();
    }

    public int nextPosition() {
        return position.incrementAndGet();
    }

    public Pixel get(int index) {
        return pixels.get(index);
    }

    public void reset() {
//        iterator = pixels.iterator();
        position = new AtomicInteger(0);
    }

    public static PixelQueue defaultQueue(int width, int height) {
        List<Pixel> pixelList = new ArrayList<>();
        for(int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelList.add(new Pixel(x, y));
            }
        }
        return new PixelQueue(pixelList);
    }
}
