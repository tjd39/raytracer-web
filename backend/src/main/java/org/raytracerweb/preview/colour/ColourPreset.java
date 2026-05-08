package org.raytracerweb.preview.colour;


import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

// https://rgbcolorpicker.com/0-1
public enum ColourPreset {
    RED(1f, 0.498f, 0.42f),
    ORANGE(1f, 0.784f, 0.42f),
    YELLOW(1f, 0.933f, 0.42f),
    GREEN(0.463f, 0.8f, 0.471f),
    BLUE(0.42f, 0.812f, 1f),
    PINK(0.98f, 0.639f, 0.827f),
    TEAL(0.29f, 0.831f, 0.812f),
    GOLD(0.72156863f, 0.54117647f, 0.26666667f),
    DARK_GREEN(0.05098039f, 0.27058824f, 0.12941176f),
    BROWN(0.27058824f, 0.10980392f, 0f),
    BLACK(0f, 0f, 0f),
    WHITE(1f, 1f, 1f),
    GREY(0.69f, 0.69f, 0.69f),
    SUNSET(0.98039215f, 0.37647058f, 0.33333333f),
    MAGENTA(1f, 0f, 1f),
    SKY(0.43921569f, 0.65490196f, 1f),
    BEIGE(0.851f, 0.733f, 0.608f);

    static final AtomicInteger count = new AtomicInteger(0);
    final Colour colour;
    private static final Random RANDOM = new Random();

    ColourPreset(float r, float g, float b) {
        this.colour = new ColourFinal(r, g, b);
    }

    public Colour get() {
        return this.colour;
    }

    public Colour getMutable() {
        return this.colour.mutableCopy();
    }

    public static ColourPreset random() {
        return Arrays.asList(ColourPreset.values()).get(RANDOM.nextInt(0, ColourPreset.values().length - 1));
    }

    public static ColourPreset randomNotDark() {
        return randomWithExclusions(BLACK,WHITE,GREY,DARK_GREEN,BROWN);
    }

    public static ColourPreset randomWithExclusions(ColourPreset... exclusions) {
        List<ColourPreset> exclusionsList = Arrays.asList(exclusions);
        List<ColourPreset> availableColours = Arrays.stream(ColourPreset.values()).filter(c -> !exclusionsList.contains(c)).toList();
        return availableColours.get(RANDOM.nextInt(0, availableColours.size() - 1));
    }

    public static ColourPreset debugNext() {
        return ColourPreset.values()[count.getAndIncrement() % 6];
    }

}
