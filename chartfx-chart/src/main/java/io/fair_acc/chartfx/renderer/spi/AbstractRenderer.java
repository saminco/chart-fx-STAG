package io.fair_acc.chartfx.renderer.spi;

import java.util.*;
import java.util.function.IntSupplier;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;

import io.fair_acc.chartfx.Chart;
import io.fair_acc.chartfx.axes.Axis;
import io.fair_acc.chartfx.renderer.Renderer;
import io.fair_acc.chartfx.ui.css.CssPropertyFactory;
import io.fair_acc.chartfx.ui.css.DataSetNode;
import io.fair_acc.chartfx.ui.css.StyleUtil;
import io.fair_acc.chartfx.utils.PropUtil;
import io.fair_acc.dataset.DataSet;
import io.fair_acc.dataset.DataSetError;
import io.fair_acc.dataset.events.ChartBits;
import io.fair_acc.dataset.spi.DoubleDataSet;
import io.fair_acc.dataset.spi.DoubleErrorDataSet;
import io.fair_acc.dataset.utils.NoDuplicatesList;
import io.fair_acc.dataset.utils.ProcessingProfiler;

/**
 * @author rstein
 * @param <R> renderer generics
 */
public abstract class AbstractRenderer<R extends Renderer> extends Parent implements Renderer {
    protected final StyleableBooleanProperty showInLegend = css().createBooleanProperty(this, "showInLegend", true);
    protected final StyleableBooleanProperty useGlobalColorIndex = css().createBooleanProperty(this, "useGlobalColorIndex", true);
    protected final IntegerProperty globalIndexOffset = new SimpleIntegerProperty(this, "globalIndexOffset", 0);
    protected final IntegerProperty localIndexOffset = css().createIntegerProperty(this, "localIndexOffset", 0);
    protected final IntegerProperty colorCount = css().createIntegerProperty(this, "colorCount", 8, true, null);
    private final ObservableList<DataSet> datasets = FXCollections.observableArrayList();
    private final ObservableList<DataSetNode> dataSetNodes = FXCollections.observableArrayList();
    private final ObservableList<DataSetNode> readOnlyDataSetNodes = FXCollections.unmodifiableObservableList(dataSetNodes);
    private final ObservableList<Axis> axesList = FXCollections.observableList(new NoDuplicatesList<>());
    private final ObjectProperty<Chart> chart = new SimpleObjectProperty<>();

    protected DataSetNode createNode(DataSet dataSet) {
        // Reuse existing nodes when possible
        for (DataSetNode dataSetNode : dataSetNodes) {
            if (dataSetNode.getDataSet() == dataSet) {
                return dataSetNode;
            }
        }
        return new DataSetNode(this, dataSet);
    }

    public AbstractRenderer() {
        StyleUtil.addStyles(this, "renderer");
        PropUtil.runOnChange(() -> fireInvalidated(ChartBits.ChartLegend), showInLegend);
        dataSetNodes.addListener((ListChangeListener<DataSetNode>) c -> {
            getChildren().setAll(dataSetNodes);
        });
        datasets.addListener((ListChangeListener<DataSet>) c -> updateNodes());
        dataSetNodes.addListener((ListChangeListener<DataSetNode>) c -> updateIndices());
        PropUtil.runOnChange(this::updateIndices, useGlobalColorIndex, globalIndexOffset, localIndexOffset, colorCount);
    }

    protected void updateNodes() {
        // Note: we can't use Stream::distinct() because it uses Objects::equal,
        // but in this case we need to check for equal object identity.
        List<DataSetNode> nodes = new ArrayList<>(datasets.size());
        distinctDataSets.clear();
        for (var dataset : datasets) {
            if (distinctDataSets.add(dataset)) {
                nodes.add(createNode(dataset));
            }
        }
        distinctDataSets.clear();
        dataSetNodes.setAll(nodes);
    }

    private final Set<DataSet> distinctDataSets = Collections.newSetFromMap(new IdentityHashMap<>());

    protected void updateIndices() {
        int localIndex = getLocalIndexOffset();
        int globalIndex = getGlobalIndexOffset() + localIndex;
        int colorIndex = useGlobalColorIndex.get() ? globalIndex : localIndex;
        int maxColorCount = getColorCount();
        for (DataSetNode datasetNode : getDatasetNodes()) {
            datasetNode.setLocalIndex(localIndex++);
            datasetNode.setGlobalIndex(globalIndex++);
            datasetNode.setColorIndex(colorIndex++ % maxColorCount);
        }
    }

    @Override
    public ObservableList<Axis> getAxes() {
        return axesList;
    }

    @Override
    public ObservableList<DataSet> getDatasets() {
        return datasets;
    }

    public ObservableList<DataSetNode> getDatasetNodes() {
        return readOnlyDataSetNodes;
    }

    protected ObservableList<DataSetNode> getInternalDataSetNodes() {
        return dataSetNodes;
    }

    public Axis getFirstAxis(final Orientation orientation) {
        for (final Axis axis : getAxes()) {
            if (axis.getSide() == null) {
                continue;
            }
            switch (orientation) {
            case VERTICAL:
                if (axis.getSide().isVertical()) {
                    return axis;
                }
                break;
            case HORIZONTAL:
            default:
                if (axis.getSide().isHorizontal()) {
                    return axis;
                }
                break;
            }
        }
        return null;
    }

    /**
     * Returns the first axis for a specific orientation and falls back to the first axis
     * of the chart if no such axis exists. The chart will automatically return a default
     * axis in case no axis is present.
     * Because this code adds axes automatically, it should not be called during chart setup
     * but only inside of rendering routines. Otherwise there is risk of duplicate axes if
     * things are called in the wrong order.
     *
     * @param orientation specifies if a horizontal or vertical axis is requested
     * @param fallback The chart from which to get the axis if no axis is present
     * @return The requested axis
     */
    protected Axis getFirstAxis(final Orientation orientation, final Chart fallback) {
        final Axis axis = getFirstAxis(orientation);
        if (axis == null) {
            return fallback.getFirstAxis(orientation);
        }
        return axis;
    }

    /**
     * @return the instance of this AbstractDataSetManagement.
     */
    protected abstract R getThis();

    public Chart getChart() {
        return chart.get();
    }

    public ObjectProperty<Chart> chartProperty() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart.set(chart);
    }

    /**
     * Sets whether DataSets attached to this renderer shall be shown in the legend
     *
     * @param state true (default) if data sets are supposed to be drawn
     * @return the renderer class
     */
    @Override
    public R setShowInLegend(final boolean state) {
        showInLegend.set(state);
        return getThis();
    }

    /**
     * Sets whether DataSets attached to this renderer shall be shown in the legend
     *
     * @return true (default) if data sets are supposed to be drawn
     */
    @Override
    public boolean showInLegend() {
        return showInLegend.get();
    }

    /**
     * Sets whether DataSets attached to this renderer shall be shown in the legend
     *
     * @return true (default) if data sets are supposed to be drawn
     */
    @Override
    public final BooleanProperty showInLegendProperty() {
        return showInLegend;
    }

    @Override
    public int getGlobalIndexOffset() {
        return globalIndexOffset.get();
    }

    public ReadOnlyIntegerProperty globalIndexOffsetProperty() {
        return globalIndexOffset;
    }

    public void setGlobalIndexOffset(int globalIndexOffset) {
        this.globalIndexOffset.set(globalIndexOffset);
    }

    public int getLocalIndexOffset() {
        return localIndexOffset.get();
    }

    public IntegerProperty localIndexOffsetProperty() {
        return localIndexOffset;
    }

    public void setLocalIndexOffset(int localIndexOffset) {
        this.localIndexOffset.set(localIndexOffset);
    }

    public boolean isUseGlobalColorIndex() {
        return useGlobalColorIndex.get();
    }

    public BooleanProperty useGlobalColorIndexProperty() {
        return useGlobalColorIndex;
    }

    public void setUseGlobalColorIndex(boolean useGlobalColorIndex) {
        this.useGlobalColorIndex.set(useGlobalColorIndex);
    }

    public int getColorCount() {
        return colorCount.get();
    }

    public IntegerProperty colorCountProperty() {
        return colorCount;
    }

    public void setColorCount(int colorCount) {
        this.colorCount.set(colorCount);
    }

    /**
     * @param prop property that causes the canvas to invalidate
     * @return property
     * @param <T> any type of property
     */
    protected <T extends Property<?>> T registerCanvasProp(T prop) {
        PropUtil.runOnChange(this::invalidateCanvas, prop);
        return prop;
    }

    protected void invalidateCanvas() {
        fireInvalidated(ChartBits.ChartCanvas);
    }

    protected void fireInvalidated(IntSupplier bit) {
        var chart = getChart();
        if (chart != null) {
            chart.fireInvalidated(bit);
        }
    }

    @Override
    public Node getNode() {
        return this;
    }

    protected CssPropertyFactory<AbstractRenderer<?>> css() {
        return CSS; // subclass specific CSS due to inheritance issues otherwise
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return css().getCssMetaData();
    }

    private static final CssPropertyFactory<AbstractRenderer<?>> CSS = new CssPropertyFactory<>(Parent.getClassCssMetaData());
}
