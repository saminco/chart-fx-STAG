package io.fair_acc.chartfx.axes.spi;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.chartfx.renderer.spi.utils.ColorGradient;
import io.fair_acc.chartfx.ui.geometry.Side;

/**
 * An Axis with a color gradient e.g. for use with HeatMap plots. By default this axis is excluded from the Zoomer
 * Plugin. TODO: - Fix LEFT, CENTER_HOR/VERT - Reduce Boilerplate Code - Allow free Positioning? e.g legend style,
 * outside of chart, ...
 *
 * @author Alexander Krimm
 */
public class ColorGradientAxis extends DefaultNumericAxis {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColorGradientAxis.class);
    protected final Rectangle gradientRect = new Rectangle();

    private final ObjectProperty<ColorGradient> colorGradient = new SimpleObjectProperty<>(this, "colorGradient",
            ColorGradient.DEFAULT);

    private final DoubleProperty gradientWidth = new SimpleDoubleProperty(this, "gradientWidth", 20);
    {
        gradientWidth.addListener((obs, old, value) -> requestAxisLayout());
    }

    /**
     * @param lowerBound the mininum axis value
     * @param upperBound the maximum axis value
     * @param tickUnit the default user-defined tick-unit
     */
    public ColorGradientAxis(double lowerBound, double upperBound, double tickUnit) {
        super(lowerBound, upperBound, tickUnit);
        this.colorGradient.addListener((p, o, n) -> this.forceRedraw());
        this.getProperties().put(Zoomer.ZOOMER_OMIT_AXIS, Boolean.TRUE);
    }

    public ColorGradientAxis(double lowerBound, double upperBound, double tickUnit, ColorGradient colorGradient) {
        this(lowerBound, upperBound, tickUnit);
        this.colorGradient.set(colorGradient);
    }

    /**
     * @param axisLabel axis title
     */
    public ColorGradientAxis(String axisLabel) {
        super(axisLabel);
        this.colorGradient.addListener((p, o, n) -> this.forceRedraw());
        this.getProperties().put(Zoomer.ZOOMER_OMIT_AXIS, Boolean.TRUE);
    }

    public ColorGradientAxis(String axisLabel, ColorGradient colorGradient) {
        this(axisLabel);
        this.colorGradient.set(colorGradient);
    }

    /**
     * @param axisLabel the axis title
     * @param lowerBound the minimum axis range
     * @param upperBound the maximum axis range
     * @param tickUnit the user-defined tick-unit
     */
    public ColorGradientAxis(String axisLabel, double lowerBound, double upperBound, double tickUnit) {
        super(axisLabel, lowerBound, upperBound, tickUnit);
        this.colorGradient.addListener((p, o, n) -> this.forceRedraw());
        this.getProperties().put(Zoomer.ZOOMER_OMIT_AXIS, Boolean.TRUE);
    }

    public ColorGradientAxis(String axisLabel, double lowerBound, double upperBound, double tickUnit,
            ColorGradient colorGradient) {
        this(axisLabel, lowerBound, upperBound, tickUnit);
        this.colorGradient.set(colorGradient);
    }

    /**
     * @param axisLabel the axis title
     * @param unit the unit label, e.g. 'm' or 's' please use SI-style units
     */
    public ColorGradientAxis(String axisLabel, String unit) {
        super(axisLabel, unit);
        this.colorGradient.addListener((p, o, n) -> this.forceRedraw());
        this.getProperties().put(Zoomer.ZOOMER_OMIT_AXIS, Boolean.TRUE);
    }

    public ColorGradientAxis(String axisLabel, String unit, ColorGradient colorGradient) {
        this(axisLabel, unit);
        this.colorGradient.set(colorGradient);
    }

    /**
     * Color gradient (linear) used to encode data point values.
     *
     * @return gradient property
     */
    public ObjectProperty<ColorGradient> colorGradientProperty() {
        return colorGradient;
    }

    @Override
    protected double computePrefHeight(final double width) {
        // add width of the bar
        final Side side = getSide();
        if ((side == null) || (side == Side.CENTER_HOR) || side.isVertical()) {
            return super.computePrefHeight(width);
        }
        return super.computePrefHeight(width) + getGradientWidth();
    }

    @Override
    protected double computePrefWidth(final double height) {
        // add width of the bar
        final Side side = getSide();
        if ((side == null) || (side == Side.CENTER_VER) || side.isHorizontal()) {
            return super.computePrefWidth(height);
        }
        return super.computePrefWidth(height) + getGradientWidth();
    }

    @Override // TODO: code has not been tested after refactoring
    public void drawAxis(final GraphicsContext gc, final double axisWidth, final double axisHeight) {
        if ((gc == null) || (getSide() == null)) {
            return;
        }

        // draw colorBar
        final double axisLength = getSide().isHorizontal() ? axisWidth : axisHeight;
        drawGradient(gc, axisLength, axisWidth, axisHeight);

        // Draw gradient
        final double gradientSize = getGradientWidth();
        double offsetX = 0, offsetY = 0;
        switch (getSide()) {
        case LEFT:
        case CENTER_VER:
            offsetX = -gradientSize;
            break;
        case RIGHT:
        case CENTER_HOR:
            offsetX = gradientSize;
            break;
        case TOP:
            offsetY = -gradientSize;
            break;
        case BOTTOM:
            offsetY = gradientSize;
            break;
        default:
            break;
        }

        try {
            gc.translate(offsetX, offsetY);
            super.drawAxis(gc, axisWidth - Math.abs(offsetX), axisHeight - Math.abs(offsetY));
        } finally {
            gc.translate(-offsetX, -offsetY);
        }
    }

    private void drawGradient(final GraphicsContext gc, final double axisLength, final double axisWidth, final double axisHeight) {
        gc.save();
        getMajorTickStyle().copyStyleTo(gc);
        if (getSide().isHorizontal()) {
            gc.setFill(new LinearGradient(0, 0, axisLength, 0, false, NO_CYCLE, getColorGradient().getStops()));
        } else {
            gc.setFill(new LinearGradient(0, axisLength, 0, 0, false, NO_CYCLE, getColorGradient().getStops()));
        }

        // for relative positioning of axes drawn on top of the main canvas
        final double gradientWidth = getGradientWidth();
        final double axisCentre = getAxisCenterPosition();

        switch (getSide()) {
        case LEFT:
            // axis line on right side of canvas
            gc.fillRect(snap(axisWidth - gradientWidth), snap(0), snap(axisWidth), snap(axisLength));
            gc.strokeRect(snap(axisWidth - gradientWidth), snap(0), snap(axisWidth), snap(axisLength));
            break;
        case RIGHT:
            // axis line on left side of canvas
            gc.fillRect(snap(0), snap(0), snap(gradientWidth), snap(axisLength));
            gc.strokeRect(snap(0), snap(0), snap(gradientWidth), snap(axisLength));
            break;
        case TOP:
            // line on bottom side of canvas (N.B. (0,0) is top left corner)
            gc.fillRect(snap(0), snap(axisHeight - gradientWidth), snap(axisLength), snap(axisHeight));
            gc.strokeRect(snap(0), snap(axisHeight - gradientWidth), snap(axisLength), snap(axisHeight));
            break;
        case BOTTOM:
            // line on top side of canvas (N.B. (0,0) is top left corner)
            gc.rect(snap(0), snap(0), snap(axisLength), snap(gradientWidth));
            break;
        case CENTER_HOR:
            // axis line at the centre of the canvas
            gc.fillRect(snap(0), axisCentre * axisHeight - 0.5 * gradientWidth, snap(axisLength),
                    snap(axisCentre * axisHeight + 0.5 * gradientWidth));
            gc.strokeRect(snap(0), axisCentre * axisHeight - 0.5 * gradientWidth, snap(axisLength),
                    snap(axisCentre * axisHeight + 0.5 * gradientWidth));
            break;
        case CENTER_VER:
            // axis line at the centre of the canvas
            gc.fillRect(snap(axisCentre * axisWidth - 0.5 * gradientWidth), snap(0),
                    snap(axisCentre * axisWidth + 0.5 * gradientWidth), snap(axisLength));
            gc.strokeRect(snap(axisCentre * axisWidth - 0.5 * gradientWidth), snap(0),
                    snap(axisCentre * axisWidth + 0.5 * gradientWidth), snap(axisLength));
            break;
        default:
            break;
        }
        gc.restore();
    }

    /**
     * @param value z-Value, values outside of the visible limit are clamped to the extrema
     * @return the color representing the input value on the z-Axis
     */
    public Color getColor(final double value) {
        final double offset = (value - getRange().getLowerBound())
                            / (getRange().getUpperBound() - getRange().getLowerBound());

        double lowerOffset = 0.0;
        double upperOffset = 1.0;
        Color lowerColor = Color.TRANSPARENT;
        Color upperColor = Color.TRANSPARENT;

        for (final Stop stop : getColorGradient().getStops()) {
            final double currentOffset = stop.getOffset();
            if (currentOffset == offset) {
                return stop.getColor();
            } else if (currentOffset < offset) {
                lowerOffset = currentOffset;
                lowerColor = stop.getColor();
            } else {
                upperOffset = currentOffset;
                upperColor = stop.getColor();
                break;
            }
        }

        final double interpolationOffset = (offset - lowerOffset) / (upperOffset - lowerOffset);
        return lowerColor.interpolate(upperColor, interpolationOffset);
    }

    /**
     * Returns the value of the {@link #colorGradientProperty()}.
     *
     * @return the color gradient used for encoding data values
     */
    public ColorGradient getColorGradient() {
        return colorGradientProperty().get();
    }

    public double getGradientWidth() {
        return gradientWidth.get();
    }

    /**
     * Return the color for a value as an integer with the color values in its bytes. For use e.g. with an IntBuffer
     * backed PixelBuffer.
     *
     * @param value z-Value
     * @return integer with one byte each set to alpha, red, green, blue
     */
    public int getIntColor(final double value) {
        final Color color = getColor(value);
        return ((byte) (color.getOpacity() * 255) << 24) + ((byte) (color.getRed() * 255) << 16)
      + ((byte) (color.getGreen() * 255) << 8) + ((byte) (color.getBlue() * 255));
    }

    public DoubleProperty gradientWidthProperty() {
        return gradientWidth;
    }

    /**
     * Sets the value of the {@link #colorGradientProperty()}.
     *
     * @param value the gradient to be used
     */
    public void setColorGradient(final ColorGradient value) {
        colorGradientProperty().set(value);
    }

    public void setGradientWidth(final double newGradientWidth) {
        gradientWidth.set(newGradientWidth);
    }
}
