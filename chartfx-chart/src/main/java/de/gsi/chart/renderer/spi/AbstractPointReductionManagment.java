package de.gsi.chart.renderer.spi;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public abstract class AbstractPointReductionManagment<R extends AbstractPointReductionManagment<R>>
        extends AbstractDataSetManagement<R> {
    private final ReadOnlyBooleanWrapper actualPointReduction = new ReadOnlyBooleanWrapper(this, "actualPointReduction",
            true);
    private final BooleanProperty assumeSortedData = new SimpleBooleanProperty(this, "assumeSortedData", true);
    private final IntegerProperty minRequiredReductionSize = new SimpleIntegerProperty(this, "minRequiredReductionSize",
            5);
    private final BooleanProperty parallelImplementation = new SimpleBooleanProperty(this, "parallelImplementation",
            true);
    private final BooleanProperty pointReduction = new SimpleBooleanProperty(this, "pointReduction", true);

    public AbstractPointReductionManagment() {
        super();
        actualPointReduction.bind(Bindings.and(pointReduction, assumeSortedData));
    }

    /**
     * Indicates whether point reduction is active.
     *
     * @return true if data points are supposed to be reduced
     */
    public ReadOnlyBooleanProperty actualPointReductionProperty() {
        return actualPointReduction.getReadOnlyProperty();
    }

    /**
     * Disable this if you have data which is not monotonic in x-direction. This setting can increase rendering time
     * drastically because lots of off screen points might have to be rendered.
     *
     * @return true if data points are supposed to be sorted
     */
    public BooleanProperty assumeSortedDataProperty() {
        return assumeSortedData;
    }

    /**
     * @return the minimum number of samples before performing data reduction
     */
    public int getMinRequiredReductionSize() {
        return minRequiredReductionSize.get();
    }

    /**
     * Indicates whether point reduction is active.
     *
     * @return true if point reduction is on (default) else false.
     */
    public boolean isActualReducePoints() {
        return actualPointReduction.get();
    }

    /**
     * Disable this if you have data which is not monotonic in x-direction. This setting can increase rendering time
     * drastically because lots of off screen points might have to be rendered.
     *
     * @return true if points should be assumed to be sorted (default)
     */
    public boolean isAssumeSortedData() {
        return assumeSortedData.get();
    }

    /**
     * whether renderer should aim at parallelising sub-functionalities
     *
     * @return true if renderer is parallelising sub-functionalities
     */
    public boolean isParallelImplementation() {
        return parallelImplementation.get();
    }

    /**
     * Sets whether superfluous points, otherwise drawn on the same pixel area, are merged and represented by the
     * multiple point average. Note that the point Reduction is also disabled implicitly by assumeSortedData = false,
     * check the read only actualDataPointReduction Property.
     *
     * @return true if point reduction is on (default) else false.
     */
    public boolean isReducePoints() {
        return pointReduction.get();
    }

    public IntegerProperty minRequiredReductionSizeProperty() {
        return minRequiredReductionSize;
    }

    /**
     * Sets whether renderer should aim at parallelising sub-functionalities
     *
     * @return true if data points are supposed to be reduced
     */
    public BooleanProperty parallelImplementationProperty() {
        return parallelImplementation;
    }

    /**
     * Sets whether superfluous points, otherwise drawn on the same pixel area, are merged and represented by the
     * multiple point average.
     *
     * @return true if data points are supposed to be reduced
     */
    public BooleanProperty pointReductionProperty() {
        return pointReduction;
    }

    /**
     * Disable this if you have data which is not monotonic in x-direction. This setting can increase rendering time
     * drastically because lots of off screen points might have to be rendered.
     *
     * @param state true if data points are supposed to be sorted
     * @return itself (fluent design)
     */
    public R setAssumeSortedData(final boolean state) {
        assumeSortedData.set(state);
        return getThis();
    }

    /**
     * @param size the minimum number of samples before performing data reduction
     * @return itself (fluent design)
     */
    public R setMinRequiredReductionSize(final int size) {
        minRequiredReductionSize.setValue(size);
        return getThis();
    }

    /**
     * Sets whether renderer should aim at parallelising sub-functionalities
     *
     * @param state true if renderer is parallelising sub-functionalities
     * @return itself (fluent design)
     */
    public R setParallelImplementation(final boolean state) {
        parallelImplementation.set(state);
        return getThis();
    }

    /**
     * Sets whether superfluous points, otherwise drawn on the same pixel area, are merged and represented by the
     * multiple point average.
     *
     * @param state true if data points are supposed to be reduced
     * @return itself (fluent design)
     */
    public R setPointReduction(final boolean state) {
        pointReduction.set(state);
        return getThis();
    }
}
