package de.gsi.chart.axes;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

import de.gsi.chart.axes.spi.AxisRange;
import de.gsi.chart.axes.spi.MetricPrefix;
import de.gsi.chart.axes.spi.TickMark;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.AxisDescription;
import de.gsi.dataset.event.UpdateEvent;

public interface Axis extends AxisDescription {
    /**
     * This is true when the axis determines its range from the data automatically
     *
     * @return property
     */
    BooleanProperty autoGrowRangingProperty();

    /**
     * This is true when the axis determines its range from the data automatically
     *
     * @return property
     */
    BooleanProperty autoRangingProperty();

    /**
     * @return {@code true} -&gt; scale to the nearest SI unit prefix
     */
    BooleanProperty autoUnitScalingProperty();

    /**
     * Function allows custom drawing of axes outside the Axis environment (ie. on another canvas)
     *
     * @param gc the graphic context on which the axis is to be drawn
     * @param axisWidth the axis width in pixel (N.B. padding is being added)
     * @param axisHeight the axis height in pixel (N.B. padding is being added)
     */
    void drawAxis(GraphicsContext gc, double axisWidth, double axisHeight);

    /**
     * forces redrawing of axis (via layoutChildren()). This is used to force an update while the main chart area is
     * being updated (a requestLayout()) would be executed only during the next pulse. This is used explicitly in the
     * Chart class. Outside use of this context should be limited to a minimum... handle with care
     */
    void forceRedraw();

    /**
     * if available (last) auto-range that has been computed
     *
     * @return computed auto-range
     */
    AxisRange getAutoRange();

    AxisTransform getAxisTransform();

    /**
     * Get the display position along this axis for a given value. If the value is not in the current range, the
     * returned value will be an extrapolation of the display position. If the value is not valid for this Axis and the
     * axis cannot display such value in any range, Double.NaN is returned
     *
     * @param value The data value to work out display position for
     * @return display position or Double.NaN if value not valid
     */
    double getDisplayPosition(double value);

    double getHeight();

    /**
     * @return axis length in pixel
     */
    @Override
    double getLength();

    /**
     * @return given linear and/or logarithmic (+ sub-type, e.g. log10, dB20, ...) axis types
     */
    LogAxisType getLogAxisType();

    int getMinorTickCount();

    ObservableList<TickMark> getMinorTickMarks();

    /**
     * on auto-ranging this returns getAutoRange(), otherwise the user-specified range getUserRange() (ie. limits based
     * on [lower,upper]Bound)
     *
     * @return actual range that is being used.
     */
    AxisRange getRange();

    /**
     * @return the layout side
     */
    Side getSide();

    /**
     * @return the fill for all tick labels
     */
    Paint getTickLabelFill();

    /**
     * @return the font for all tick labels
     */
    Font getTickLabelFont();

    StringConverter<Number> getTickLabelFormatter();

    /**
     * @return the gap between tick labels and the tick mark lines
     */
    double getTickLabelGap();

    /**
     * @return the minimum gap between tick labels
     */
    double getTickLabelSpacing();

    /**
     * Get the string label name for a tick mark with the given value
     *
     * @param value The value to format into a tick label string
     * @return A formatted string for the given value
     */
    String getTickMarkLabel(double value);

    ObservableList<TickMark> getTickMarks();

    double getTickUnit();

    /**
     * @return axis primary unit scaling
     */
    double getUnitScaling();

    /**
     * user-specified range (ie. limits based on [lower,upper]Bound)
     *
     * @return user-specified range
     */
    AxisRange getUserRange();

    /**
     * Get the data value for the given display position on this axis. If the axis is a CategoryAxis this will be the
     * nearest value.
     *
     * @param displayPosition A pixel position on this axis
     * @return the nearest data value to the given pixel position or null if not on axis;
     */
    double getValueForDisplay(double displayPosition);

    double getWidth();

    /**
     * Get the display position of the zero line along this axis.
     *
     * @return display position or Double.NaN if zero is not in current range;
     */
    double getZeroPosition();

    /**
     * Called when data has changed and the range may not be valid any more. This is only called by the chart if
     * isAutoRanging() returns true. If we are auto ranging it will cause layout to be requested and auto ranging to
     * happen on next layout pass.
     *
     * @param data The current set of all data that needs to be plotted on this axis
     */
    void invalidateRange(List<Number> data);

    /**
     * This is {@code true} when the axis labels and data point order should be inverted
     *
     * @param value {@code true} if axis shall be inverted (i.e. drawn from 'max-&gt;min', rather than the normal
     *        'min-&gt;max')
     */
    void invertAxis(boolean value);

    /**
     * This is {@code true} when the axis labels and data point order should be inverted
     *
     * @return property
     */
    BooleanProperty invertAxisProperty();

    /**
     * invoke object within update listener list
     *
     * @param updateEvent the event the listeners are notified with
     * @param executeParallel {@code true} execute event listener via parallel executor service
     */
    @Override
    default void invokeListener(final UpdateEvent updateEvent, final boolean executeParallel) {
        // implemented for forwarding purposes
        AxisDescription.super.invokeListener(updateEvent, executeParallel);
    }

    /**
     * This is true when the axis determines its range from the data automatically and grows it if necessary
     *
     * @return true if axis shall be updated to the optimal data range
     */
    boolean isAutoGrowRanging();

    /**
     * This is true when the axis determines its range from the data automatically
     *
     * @return true if axis shall be updated to the optimal data range
     */
    boolean isAutoRanging();

    /**
     * @return whether unit is automatically adjusted to multiples of 1e3 (kilo, mega, ...) or 1e-3 (milli, micro, ...)
     */
    boolean isAutoUnitScaling();

    /**
     * This is {@code true} when the axis labels and data point order should be inverted
     *
     * @return {@code true} if axis shall be inverted (i.e. drawn from 'max-&gt;min', rather than the normal
     *         'min-&gt;max')
     */
    boolean isInvertedAxis();

    /**
     * This is true when the axis implements a log scale
     *
     * @return true if axis is log scale
     */
    boolean isLogAxis();

    /**
     * This is true when the axis corresponds to a time axis
     *
     * @return true if axis is a time scale
     */
    boolean isTimeAxis();

    /**
     * Checks if the given value is plottable on this axis
     *
     * @param value The value to check if its on axis
     * @return true if the given value is plottable on this axis
     */
    boolean isValueOnAxis(double value);

    DoubleProperty maxProperty();

    DoubleProperty minProperty();

    /**
     * @return the primary axis name/label property
     */
    StringProperty nameProperty();

    /**
     * Request that the axis is laid out in the next layout pass. This replaces requestLayout() as it has been
     * overridden to do nothing so that changes to children's bounds etc do not cause a layout. This was done as a
     * optimisation as the Axis knows the exact minimal set of changes that really need layout to be updated. So we only
     * want to request layout then, not on any child change.
     */
    void requestAxisLayout();

    /**
     * @param value true if axis range changes will be animated and false otherwise
     */
    void setAnimated(boolean value);

    /**
     * This is true when the axis determines its range from the data automatically and grows it if necessary
     *
     * @param value true if axis shall be updated to the optimal data range and grows it if necessary
     */
    void setAutoGrowRanging(boolean value);

    /**
     * This is true when the axis determines its range from the data automatically
     *
     * @param value true if axis shall be updated to the optimal data range
     */
    void setAutoRanging(boolean value);

    /**
     * @param value scaling value {@code true} -&gt; scale to the nearest SI unit prefix
     */
    void setAutoUnitScaling(final boolean value);

    /**
     * @param value the new axis primary label
     */
    void setName(final String value);

    void setSide(Side newSide);

    void setTickUnit(double tickUnit);

    /**
     * This is {@code true} when the axis labels and data point should be plotted according to some time-axis definition
     *
     * @param value {@code true} if axis shall be drawn with time-axis labels
     */
    void setTimeAxis(final boolean value);

    /**
     * @param value the new axis primary unit name
     */
    void setUnit(final String value);

    /**
     * @param value the new axis primary unit label
     */
    void setUnitScaling(final double value);

    /**
     * @param value the new axis primary unit label
     */
    void setUnitScaling(final MetricPrefix value);

    ObjectProperty<Side> sideProperty();

    DoubleProperty tickUnitProperty();

    /**
     * This is {@code true} when the axis labels and data point should be plotted according to some time-axis definition
     *
     * @return the timeAxis property
     */
    BooleanProperty timeAxisProperty();

    /**
     * @return the primary unit name property
     */
    StringProperty unitProperty();

    /**
     * @return the primary unit label property
     */
    DoubleProperty unitScalingProperty();

    /**
     * @return the canvas of the axis
     */
    Canvas getCanvas();
}
