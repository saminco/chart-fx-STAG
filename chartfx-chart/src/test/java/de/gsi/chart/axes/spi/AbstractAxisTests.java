package de.gsi.chart.axes.spi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import de.gsi.chart.axes.AxisLabelFormatter;
import de.gsi.chart.axes.AxisTransform;
import de.gsi.chart.axes.LogAxisType;
import de.gsi.chart.axes.spi.transforms.DefaultAxisTransform;
import de.gsi.chart.legend.spi.DefaultLegend;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.chart.ui.utils.JavaFXInterceptorUtils;
import de.gsi.chart.utils.FXUtils;

@ExtendWith(ApplicationExtension.class)
@ExtendWith(JavaFXInterceptorUtils.SelectiveJavaFxInterceptor.class)
class AbstractAxisTests {
    private static final int DEFAULT_AXIS_LENGTH = 1000;
    private static final int WIDTH = 300;
    private static final int HEIGHT = 200;

    @Test
    void testAutoRange() {
        AbstractAxis axis = new EmptyAbstractAxis(-5.0, 5.0);
        assertFalse(axis.isAutoRangeRounding());
        final AxisRange defaultrange = axis.autoRange(1000);
        assertNotNull(defaultrange);
        assertEquals(1.0, defaultrange.getAxisLength()); // since not being attache to a pane
        assertEquals(-5.0, defaultrange.getMin());
        assertEquals(-5.0, defaultrange.getLowerBound());
        assertEquals(+5.0, defaultrange.getMax());
        assertEquals(+5.0, defaultrange.getUpperBound());

        axis.setAutoRanging(true);
        assertTrue(axis.isAutoRanging());
        assertNotNull(axis.getAutoRange());
        axis.getAutoRange().setMin(-5.0);
        axis.getAutoRange().setMax(+5.0);
        final AxisRange autoRange = axis.autoRange(1000);
        assertNotNull(autoRange);

        assertEquals(1000, autoRange.getAxisLength()); // since we set it explicitly as autoRange(1000) parameter
        assertEquals(-5.0, autoRange.getMin());
        assertEquals(-5.0, autoRange.getLowerBound());
        assertEquals(+5.0, autoRange.getMax());
        assertEquals(+5.0, autoRange.getUpperBound());

        assertDoesNotThrow(() -> axis.computeTickMarks(autoRange, true));

        assertDoesNotThrow(() -> axis.computeTickMarks(autoRange, false));

        List<Number> numberList = Collections.unmodifiableList(axis.calculateMajorTickValues(DEFAULT_AXIS_LENGTH, autoRange));
        axis.invalidateRange(new ArrayList<>(numberList));
    }

    @Test
    void testConstructors() {
        assertDoesNotThrow((ThrowingSupplier<EmptyAbstractAxis>) EmptyAbstractAxis::new);
        assertDoesNotThrow(() -> new EmptyAbstractAxis(-10.0, +10.0));
    }

    @ParameterizedTest
    @EnumSource(Side.class)
    void testDrawRoutines(final Side side) {
        final AbstractAxis axis = new EmptyAbstractAxis(-5.0, 5.0);
        final Canvas canvas = axis.getCanvas();
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        assertDoesNotThrow(() -> axis.setSide(side));

        assertDoesNotThrow(axis::forceRedraw);
        assertDoesNotThrow(() -> axis.drawAxis(gc, 100, 100));
        assertDoesNotThrow(() -> axis.drawAxis(null, 100, 100));
        assertDoesNotThrow(() -> AbstractAxis.drawTickMarkLabel(gc, 10, 10, 1.0, new TickMark(Side.BOTTOM, 1.0, 1.0, 0.0, "label")));
        assertDoesNotThrow(() -> AbstractAxis.drawTickMarkLabel(gc, 10, 10, 0.9, new TickMark(Side.BOTTOM, 1.0, 1.0, 90.0, "label")));
        axis.setTickMarkVisible(false);
        axis.setMinorTickVisible(false);
        assertDoesNotThrow(() -> axis.drawAxis(gc, 100, 100));
    }

    @Test
    void testGetterSetters() { // NOPMD NOSONAR -- number of assertions is part of the unit-test
        EmptyAbstractAxis axis = new EmptyAbstractAxis();

        assertTrue(axis.set(-5.0, +5.0));
        assertFalse(axis.set(-5.0, +5.0));
        assertEquals(-5.0, axis.getMin());
        assertEquals(+5.0, axis.getMax());

        final AxisLabelFormatter formatter = axis.getAxisLabelFormatter();
        axis.setAxisLabelFormatter(formatter);

        assertTrue(axis.setMin(-1.0));
        assertEquals(-1.0, axis.getMin());
        assertEquals(+5.0, axis.getMax());
        assertFalse(axis.setMin(-1.0));
        assertTrue(axis.setMax(+1.0));
        assertEquals(-1.0, axis.getMin());
        assertEquals(+1.0, axis.getMax());
        assertFalse(axis.setMax(+1.0));

        // log axis treatment
        axis.logAxis = true;
        assertTrue(axis.setMin(-1.0));
        assertEquals(1e-6, axis.getMin());
        assertEquals(+1.0, axis.getMax());
        assertFalse(axis.setMin(-1.0));
        assertTrue(axis.setMin(1e6));
        assertTrue(axis.setMin(2e-6));
        assertEquals(2e-6, axis.getMin());
        assertEquals(+1.0, axis.getMax());
        axis.logAxis = false;
        assertTrue(axis.set(-5.0, -5.0));

        axis.logAxis = true;
        assertFalse(axis.setMin(-1.0));
        assertEquals(-5.0, axis.getMin());
        assertEquals(-5.0, axis.getMax());
        assertFalse(axis.setMin(-1.0));
        assertFalse(axis.setMax(-1.0));
        assertTrue(axis.setMin(1e-6));
        assertEquals(1e-6, axis.getMin());
        assertEquals(-5.0, axis.getMax());
        assertTrue(axis.setMax(-1.0));
        assertEquals(1e-6, axis.getMin());
        assertEquals(1.0, axis.getMax());
    }

    @Test
    void testHelper() {
        assertEquals(0.5, AbstractAxis.snap(0.4));
        assertEquals(1.5, AbstractAxis.snap(0.7));
        assertEquals(1.5, AbstractAxis.snap(1.0));

        AbstractAxis axis = new EmptyAbstractAxis(-5.0, 5.0);
        assertDoesNotThrow(() -> axis.clearAxisCanvas(axis.getCanvas().getGraphicsContext2D(), 100, 100));

        axis.setSide(Side.BOTTOM);
        assertEquals(+1.0, axis.calculateNewScale(10, -5.0, +5.0));
        assertEquals(+17, axis.computePrefHeight(100), 2);
        assertEquals(+150.0, axis.computePrefWidth(-1));
        axis.setSide(Side.LEFT);
        assertEquals(-1.0, axis.calculateNewScale(10, -5.0, +5.0));
        assertEquals(+150, axis.computePrefHeight(-1));
        assertEquals(+22, axis.computePrefWidth(100), 2);

        assertDoesNotThrow(axis::clear);
        assertDoesNotThrow(axis::forceRedraw);
        final AtomicInteger counter = new AtomicInteger();
        assertDoesNotThrow(axis::fireInvalidated);
        assertDoesNotThrow(() -> FXUtils.runAndWait(axis::fireInvalidated));
        assertEquals(0, counter.get());
        axis.addListener(evt -> counter.incrementAndGet());
        assertDoesNotThrow(axis::fireInvalidated);
        assertDoesNotThrow(() -> FXUtils.runAndWait(axis::fireInvalidated));
        assertEquals(2, counter.get());
    }

    @Test
    void testTickMarks() {
        AbstractAxis axis = new EmptyAbstractAxis(-5.0, 5.0);
        final AxisRange autoRange = axis.autoRange(DEFAULT_AXIS_LENGTH);

        final List<TickMark> majorTickMarks = axis.computeTickMarks(autoRange, true);
        assertNotNull(majorTickMarks);
        assertEquals(10, majorTickMarks.size());
        assertDoesNotThrow(() -> majorTickMarks.forEach(tm -> {
            if (!tm.isVisible()) {
                throw new IllegalStateException("majorTickMarks " + tm + " is invisible");
            }
        }));

        final List<TickMark> minorTickMarks = axis.computeTickMarks(autoRange, false);
        assertNotNull(minorTickMarks);
        assertEquals(100, minorTickMarks.size());
        assertDoesNotThrow(() -> minorTickMarks.forEach(tm -> {
            if (!tm.isVisible()) {
                throw new IllegalStateException("minorTickMarks " + tm + " is invisible");
            }
        }));

        axis.invertAxis(true);
        majorTickMarks.clear();
        majorTickMarks.addAll(axis.computeTickMarks(autoRange, true));
        assertEquals(10, majorTickMarks.size());
        assertDoesNotThrow(() -> majorTickMarks.forEach(tm -> {
            if (!tm.isVisible()) {
                throw new IllegalStateException("tm " + tm + " is invisible");
            }
        }));
        axis.getTickMarks().setAll(majorTickMarks);
    }

    @Start
    public void start(Stage stage) {
        assertDoesNotThrow(DefaultLegend::new);

        final Pane pane = new Pane();

        stage.setScene(new Scene(pane, WIDTH, HEIGHT));
        stage.show();
    }

    private static class EmptyAbstractAxis extends AbstractAxis {
        private final DefaultAxisTransform transform = new DefaultAxisTransform(this);
        private boolean logAxis = false;

        public EmptyAbstractAxis() {
            super();
        }

        public EmptyAbstractAxis(final double min, final double max) {
            super(min, max);
        }

        @Override
        public double computePreferredTickUnit(final double axisLength) {
            //return axisLength / Math.abs(getMax() - getMin()) / 10.0
            return 0.1; // simplification for testing
        }

        @Override
        protected AxisRange autoRange(final double minValue, final double maxValue, final double length, final double labelSize) {
            return computeRange(minValue, maxValue, DEFAULT_AXIS_LENGTH, labelSize);
        }

        @Override
        public AxisTransform getAxisTransform() {
            return transform;
        }

        @Override
        public LogAxisType getLogAxisType() {
            return LogAxisType.LOG10_SCALE;
        }

        @Override
        public double getValueForDisplay(final double displayPosition) {
            return 0;
        }

        @Override
        public boolean isLogAxis() {
            return logAxis;
        }

        @Override
        protected List<Double> calculateMajorTickValues(final double length, final AxisRange axisRange) {
            final List<Double> majorTicks = new ArrayList<>();
            final double range = Math.abs(axisRange.getMax() - axisRange.getMin());
            final double min = Math.min(getMin(), getMax());
            for (int i = 0; i < 10; i++) {
                majorTicks.add(min + i * range / 10.0);
            }
            return majorTicks;
        }

        @Override
        protected List<Double> calculateMinorTickValues() {
            final List<Double> minorTicks = new ArrayList<>();
            final double range = Math.abs(getMax() - getMin());
            final double min = Math.min(getMin(), getMax());
            for (int i = 0; i < 100; i++) {
                minorTicks.add(min + i * range / 100.0);
            }
            return minorTicks;
        }

        @Override
        protected AxisRange computeRange(final double minValue, final double maxValue, final double axisLength, final double labelSize) {
            final double range = Math.abs(maxValue - minValue);
            final double scale = range > 0 ? axisLength / range : -1;
            final double tickUnit = range / 10.0;
            return new AxisRange(minValue, maxValue, axisLength, scale, tickUnit);
        }
    }
}
