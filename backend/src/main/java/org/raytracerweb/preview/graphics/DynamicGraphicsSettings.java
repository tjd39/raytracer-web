package org.raytracerweb.preview.graphics;

import java.util.List;
import java.util.Objects;

public class DynamicGraphicsSettings extends GraphicsSettings {
    private final List<GraphicsSettings> settingsList;
    private int currentLevel;

    public DynamicGraphicsSettings(List<GraphicsSettings> settingsList, int defaultLevel) {
        super(0, 0, 0, 0, false, false); // Dummy values, will be overridden
        this.settingsList = settingsList;
        this.currentLevel = defaultLevel;
    }

    public void setLevel(int level) {
        if (level < 0 || level >= settingsList.size()) {
            throw new IllegalArgumentException("Invalid render level: " + level);
        }
        this.currentLevel = level;
    }

    @Override
    public int imageWidth() {
        return imageWidth(currentLevel);
    }

    public int imageWidth(int level) {
        return settingsList.get(level).imageWidth();
    }

    @Override
    public int imageHeight() {
        return imageHeight(currentLevel);
    }

    public int imageHeight(int level) {
        return settingsList.get(level).imageHeight();
    }

    @Override
    public int renderLevels() {
        return renderLevels(currentLevel);
    }

    public int renderLevels(int level) {
        return settingsList.get(level).renderLevels();
    }

    @Override
    public int antiAlias() {
        return antiAlias(currentLevel);
    }

    public int antiAlias(int level) {
        return settingsList.get(level).antiAlias();
    }

    @Override
    public boolean accumulateImage() {
        return accumulateImage(currentLevel);
    }

    public boolean accumulateImage(int level) {
        return settingsList.get(level).accumulateImage();
    }

    @Override
    public boolean debugAABBs() {
        return debugAABBs(currentLevel);
    }

    public boolean debugAABBs(int level) {
        return settingsList.get(level).debugAABBs();
    }

    @Override
    public void setImageWidth(int imageWidth) {
        setImageWidth(this.currentLevel, imageWidth);
    }

    public void setImageWidth(int level, int imageWidth) {
        settingsList.get(level).setImageWidth(imageWidth);
    }

    @Override
    public void setImageHeight(int imageHeight) {
        setImageHeight(this.currentLevel, imageHeight);
    }

    public void setImageHeight(int level, int imageHeight) {
        settingsList.get(level).setImageHeight(imageHeight);
    }

    @Override
    public void setRenderLevels(int renderLevels) {
        setRenderLevels(this.currentLevel, renderLevels);
    }

    public void setRenderLevels(int level, int renderLevels) {
        settingsList.get(level).setRenderLevels(renderLevels);
    }

    @Override
    public void setAntiAlias(int antiAlias) {
        setAntiAlias(this.currentLevel, antiAlias);
    }

    public void setAntiAlias(int level, int antiAlias) {
        settingsList.get(level).setAntiAlias(antiAlias);
    }

    @Override
    public void setAccumulateImage(boolean accumulateImage) {
        setAccumulateImage(this.currentLevel, accumulateImage);
    }

    public void setAccumulateImage(int level, boolean accumulateImage) {
        settingsList.get(level).setAccumulateImage(accumulateImage);
    }

    @Override
    public void setDebugAABBs(boolean debugAABBs) {
        setDebugAABBs(this.currentLevel, debugAABBs);
    }

    public void setDebugAABBs(int level, boolean debugAABBs) {
        settingsList.get(level).setDebugAABBs(debugAABBs);
    }

    public GraphicsSettings getSettingsFor(final int level) {
        if (level < 0 || level >= settingsList.size()) {
            throw new IllegalArgumentException("Invalid render level: " + level);
        }
        return settingsList.get(level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentLevel, getSettingsFor(currentLevel));
    }
}
