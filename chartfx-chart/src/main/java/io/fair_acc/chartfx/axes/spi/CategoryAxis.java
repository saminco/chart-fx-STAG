package io.fair_acc.chartfx.axes.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

import io.fair_acc.chartfx.axes.AxisLabelOverlapPolicy;
import io.fair_acc.chartfx.utils.PropUtil;
import io.fair_acc.dataset.DataSet;
import io.fair_acc.dataset.spi.fastutil.DoubleArrayList;

/**
 * A axis implementation that will works on string categories where each value as a unique category(tick mark) along the
 * axis.
 */
public final class CategoryAxis extends DefaultNumericAxis {
    private boolean forceAxisCategories = false;

    private boolean changeIsLocal = false;

    private final ObjectProperty<ObservableList<String>> categories = new ObjectPropertyBase<>() {
        @Override
        public Object getBean() {
            return CategoryAxis.this;
        }

        @Override
        public String getName() {
            return "categories";
        }
    };

    /**
     * Create a auto-ranging category axis with an empty list of categories.
     */
    public CategoryAxis() {
        this((String) null);
        setTickUnit(1.0);
        changeIsLocal = true;
        setCategories(FXCollections.observableArrayList());
        changeIsLocal = false;
    }

    /**
     * Create a category axis with the given categories. This will not auto-range but be fixed with the given
     * categories.
     *
     * @param categories List of the categories for this axis
     */
    public CategoryAxis(final ObservableList<String> categories) {
        this(null, categories);
    }

    /**
     * Create a {@link #autoRangingProperty() non-auto-ranging} Axis with the given upper bound, lower bound and tick
     * unit.
     *
     * @param axisLabel the axis {@link #nameProperty() label}
     */
    public CategoryAxis(final String axisLabel) {
        super(axisLabel);
        this.setOverlapPolicy(AxisLabelOverlapPolicy.SHIFT_ALT);
        PropUtil.runOnChange(() -> {
            final double range = Math.abs(getMax() - getMin());
            if (!Double.isFinite(range))
                return;
            final double scale = 0.5 / ((int) range);
            autoRangePaddingProperty().set(scale);
        }, minProperty(), maxProperty());
    }

    /**
     * Create a {@link #autoRangingProperty() non-auto-ranging} Axis with the given upper bound, lower bound and tick
     * unit.
     *
     * @param axisLabel the axis {@link #nameProperty() label}
     * @param categories List of the categories for this axis
     */
    public CategoryAxis(final String axisLabel, final ObservableList<String> categories) {
        super(axisLabel, 0, categories.size(), 1.0);
        changeIsLocal = true;
        setCategories(categories);
        changeIsLocal = false;
    }

    /**
     * Returns a {@link ObservableList} of categories plotted on this axis.
     *
     * @return ObservableList of categories for this axis.
     */
    public ObservableList<String> getCategories() {
        return categories.get();
    }

    /**
     * @param categories list of strings
     */
    public void setCategories(final List<String> categories) {
        if (categories == null) {
            forceAxisCategories = false;
            setCategories(FXCollections.observableArrayList());
            return;
        }
        forceAxisCategories = true;
        setCategories(FXCollections.observableArrayList(categories));
    }

    /**
     * The ordered list of categories plotted on this axis. This is set automatically based on the charts data if
     * autoRanging is true. If the application sets the categories then auto ranging is turned off. If there is an
     * attempt to add duplicate entry into this list, an {@link IllegalArgumentException} is thrown. setting the
     * category via axis forces the axis' category, setting the axis categories to null forces the dataset's category
     *
     * @param categoryList the category list
     */
    public void setCategories(final ObservableList<String> categoryList) {
        if (categoryList == null) {
            forceAxisCategories = false;
            setCategories(FXCollections.observableArrayList());
            return;
        }

        this.setMaxMajorTickLabelCount(categoryList.size() + 1); // number of categories + unknown category
        setTickLabelFormatter(new StringConverter<>() {
            @Override
            public Number fromString(String string) {
                for (int i = 0; i < getCategories().size(); i++) {
                    if (getCategories().get(i).equalsIgnoreCase(string)) {
                        return i;
                    }
                }
                throw new IllegalArgumentException("Category not found.");
            }

            @Override
            public String toString(Number object) {
                final int index = Math.round(object.floatValue());
                if (index < 0 || index >= getCategories().size()) {
                    return "unknown category";
                }
                return getCategories().get(index);
            }
        });
        categories.set(categoryList);
    }

    /**
     * Update the categories based on the data labels attached to the DataSet values
     *
     * @param dataSet data set from which the data labels are used as category
     * @return true is categories were modified, false otherwise
     */
    public boolean updateCategories(final DataSet dataSet) {
        if (dataSet == null || !dataSet.hasDataLabels() || forceAxisCategories) {
            return false;
        }

        final List<String> newCategoryList = new ArrayList<>();
        boolean zeroDataLabels = true;
        for (int i = 0; i < dataSet.getDataCount(); i++) {
            final String dataLabel = dataSet.getDataLabel(i);
            String sanitizedLabel;
            if (dataLabel == null) {
                sanitizedLabel = "unknown category";
            } else {
                sanitizedLabel = dataLabel;
                zeroDataLabels = false;
            }
            newCategoryList.add(sanitizedLabel);
        }

        if (!zeroDataLabels) {
            setCategories(newCategoryList);
            forceAxisCategories = false;
        }

        return false;
    }

    @Override
    protected AxisRange autoRange(final double minValue, final double maxValue, final double length,
            final double labelSize) {
        double min = minValue > 0 && isForceZeroInRange() ? 0 : minValue;
        if (isLogAxis && minValue <= 0) {
            min = DefaultNumericAxis.DEFAULT_LOG_MIN_VALUE;
            isUpdating = true;
            setMin(DefaultNumericAxis.DEFAULT_LOG_MIN_VALUE);
            isUpdating = false;
        }
        final double max = maxValue < 0 && isForceZeroInRange() ? 0 : maxValue;
        final double padding = DefaultNumericAxis.getEffectiveRange(min, max) * getAutoRangePadding();
        final double paddingScale = 1.0 + getAutoRangePadding();
        // compared to DefaultNumericAxis clamping wasn't really necessary for
        // CategoryAxis
        // N.B. it was unnecessarily forcing the first bound to 0 (rather than
        // -0.5)
        final double paddedMin = isLogAxis ? minValue / paddingScale : min - padding;
        final double paddedMax = isLogAxis ? maxValue * paddingScale : max + padding;

        return computeRange(paddedMin, paddedMax, length, labelSize);
    }

    @Override
    protected void calculateMinorTickValues(DoubleArrayList tickValues) {
        // categories have no minor ticks
    }

    @Override
    protected double computeTickUnit(final double rawTickUnit) {
        return 1.0;
    }
}
