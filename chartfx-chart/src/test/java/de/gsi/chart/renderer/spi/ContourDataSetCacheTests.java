package de.gsi.chart.renderer.spi;

import static org.junit.jupiter.api.Assertions.*;

import static de.gsi.dataset.DataSet.DIM_X;
import static de.gsi.dataset.DataSet.DIM_Y;
import static de.gsi.dataset.DataSet.DIM_Z;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import de.gsi.chart.axes.AxisTransform;
import de.gsi.dataset.GridDataSet;
import de.gsi.dataset.spi.DataRange;
import de.gsi.dataset.spi.DataSetBuilder;
import de.gsi.math.ArrayUtils;
import de.gsi.math.Math;

/**
 * @author rstein
 */
public class ContourDataSetCacheTests {
    private static final double[] TEST_DATA_X = { 1, 2, 3 };
    private static final double[] TEST_DATA_Y = { 1, 2, 3, 4 };
    private static final double[] TEST_DATA_Z = { //
        1, 2, 3, //
        4, 5, 6, //
        7, 8, 9, //
        10, 11, 12
    };
    // test cases for inversion
    private static final double[] TEST_DATA_Z_X_INVERTED = { //
        3, 2, 1, //
        6, 5, 4, //
        9, 8, 7, //
        12, 11, 10
    };
    private static final double[] TEST_DATA_Z_Y_INVERTED = { //
        10, 11, 12, //
        7, 8, 9, //
        4, 5, 6, //
        1, 2, 3
    };
    private static final double[] TEST_DATA_Z_XY_INVERTED = { //
        12, 11, 10, //
        9, 8, 7, //
        6, 5, 4, //
        3, 2, 1
    };
    private static final double[] TEST_DATA_Z_QUANT1 = { //
        0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0
    };
    private static final double[] TEST_DATA_Z_QUANT2 = { //
        0.9, 0.8, 0.7, 0.6, 0.5, 0.5, 0.4, 0.3, 0.2, 0.1, 0.0, 0.0
    };

    @Test
    public void testDataSet() {
        GridDataSet dataSet = new DataSetBuilder().setValues(DIM_X, TEST_DATA_X).setValues(DIM_Y, TEST_DATA_Y).setValues(DIM_Z, TEST_DATA_Z).build(GridDataSet.class);

        assertArrayEquals(new int[] { TEST_DATA_X.length, TEST_DATA_Y.length }, dataSet.getShape());
        assertEquals(TEST_DATA_Z.length, dataSet.getDataCount());

        for (int i = 0; i < dataSet.getShape(DIM_X); i++) {
            assertEquals(TEST_DATA_X[i], dataSet.getGrid(DIM_X, i));
        }
        for (int i = 0; i < dataSet.getShape(DIM_Y); i++) {
            assertEquals(TEST_DATA_Y[i], dataSet.getGrid(DIM_Y, i));
        }
        for (int i = 0; i < dataSet.getDataCount(); i++) {
            assertEquals(TEST_DATA_Z[i], dataSet.get(DIM_Z, i));
        }
    }

    @Test
    public void testHelperFunctions() {
        assertEquals(0.1, ContourDataSetCache.quantize(0.12, 10));
        assertEquals(0.1, ContourDataSetCache.quantize(0.19, 10));
        assertEquals(0.2, ContourDataSetCache.quantize(0.20, 10));
        assertEquals(0.2, ContourDataSetCache.quantize(0.21, 10));

        assertEquals(2, ContourDataSetCache.roundDownEven(2.1));
        assertEquals(2, ContourDataSetCache.roundDownEven(2.5));
        assertEquals(2, ContourDataSetCache.roundDownEven(3.0));
        assertEquals(2, ContourDataSetCache.roundDownEven(3.9));
        assertEquals(4, ContourDataSetCache.roundDownEven(4));

        DataRange range = ContourDataSetCache.computeLocalRange(TEST_DATA_Z, TEST_DATA_X.length, TEST_DATA_Y.length,
                true);
        assertEquals(Math.minimum(TEST_DATA_Z), range.getMin());
        assertEquals(Math.maximum(TEST_DATA_Z), range.getMax());
        assertTrue(range.isDefined());

        range = ContourDataSetCache.computeLocalRange(TEST_DATA_Z, TEST_DATA_X.length, TEST_DATA_Y.length, false);
        assertFalse(range.isDefined());

        final AxisTransform identityTransform = new AxisTransform() {
            @Override
            public double backward(double val) {
                return val;
            }

            @Override
            public double forward(double val) {
                return val;
            }

            @Override
            public double getMaximumRange() {
                // not necessary for this test
                return 0;
            }

            @Override
            public double getMinimumRange() {
                // not necessary for this test
                return 0;
            }

            @Override
            public double getRoundedMaximumRange(double val) {
                // not necessary for this test
                return 0;
            }

            @Override
            public double getRoundedMinimumRange(double val) {
                // not necessary for this test
                return 0;
            }

            @Override
            public void setMaximumRange(double val) {
                // not necessary for this test
            }

            @Override
            public void setMinimumRange(double val) {
                // not necessary for this test
            }
        };
        final double[] inputData = Arrays.copyOf(TEST_DATA_Z, TEST_DATA_Z.length);
        ContourDataSetCache.quantizeData(inputData, TEST_DATA_X.length, TEST_DATA_Y.length, false, 0, 12,
                identityTransform, 10);
        assertArrayEquals(TEST_DATA_Z_QUANT1, inputData, "quantizeData(..)");

        final double[] inputDataInv = Arrays.copyOf(TEST_DATA_Z, TEST_DATA_Z.length);
        ContourDataSetCache.quantizeData(inputDataInv, TEST_DATA_X.length, TEST_DATA_Y.length, true, 0, 12,
                identityTransform, 10);
        assertArrayEquals(TEST_DATA_Z_QUANT2, inputDataInv, "quantizeData(..) - inverted");
    }

    @Test
    public void testDataTransform() {
        GridDataSet dataSet = new DataSetBuilder().setValues(DIM_X, TEST_DATA_X).setValues(DIM_Y, TEST_DATA_Y).setValues(DIM_Z, TEST_DATA_Z).build(GridDataSet.class);

        assertEquals(TEST_DATA_X.length, dataSet.getShape(DIM_X), "data vector x length");
        assertEquals(TEST_DATA_Y.length, dataSet.getShape(DIM_Y), "data vector y length");
        assertEquals(TEST_DATA_Z.length, dataSet.getDataCount(), "data vector z length");

        final double[] dataBuffer = new double[dataSet.getDataCount()];
        ArrayUtils.fillArray(dataBuffer, -1);
        assertEquals(TEST_DATA_Z.length, dataBuffer.length, "data buffer length");

        assertDoesNotThrow(() -> {
            ContourDataSetCache.computeCoordinates(dataSet, dataBuffer, dataBuffer.length, //
                    false, 0, 2, //
                    false, 0, 3, 0);
        });
        assertArrayEquals(TEST_DATA_Z, dataBuffer, "data buffer content - normal");
        ArrayUtils.fillArray(dataBuffer, -1);

        assertDoesNotThrow(() -> {
            ContourDataSetCache.computeCoordinates(dataSet, dataBuffer, dataBuffer.length, //
                    true, 0, 2, //
                    false, 0, 3, 0);
        });
        assertArrayEquals(TEST_DATA_Z_X_INVERTED, dataBuffer, "data buffer content - X inverted");
        ArrayUtils.fillArray(dataBuffer, -1);

        assertDoesNotThrow(() -> {
            ContourDataSetCache.computeCoordinates(dataSet, dataBuffer, dataBuffer.length, //
                    false, 0, 2, //
                    true, 0, 3, 0);
        });
        assertArrayEquals(TEST_DATA_Z_Y_INVERTED, dataBuffer, "data buffer content - X inverted");
        ArrayUtils.fillArray(dataBuffer, -1);

        assertDoesNotThrow(() -> {
            ContourDataSetCache.computeCoordinates(dataSet, dataBuffer, dataBuffer.length, //
                    true, 0, 2, //
                    true, 0, 3, 0);
        });
        assertArrayEquals(TEST_DATA_Z_XY_INVERTED, dataBuffer, "data buffer content - X inverted");

        ArrayUtils.fillArray(dataBuffer, -1);
        ContourDataSetCache.copySubFrame(dataSet, dataBuffer, false, false, 0, 2, false, 0, 3);
        assertArrayEquals(TEST_DATA_Z, dataBuffer, "data buffer content - normal copySubFrame");
        ArrayUtils.fillArray(dataBuffer, -1);

        ContourDataSetCache.copySubFrame(dataSet, dataBuffer, true, false, 0, 2, false, 0, 3);
        assertArrayEquals(TEST_DATA_Z, dataBuffer, "data buffer content - parallel copySubFrame");

        // requires FX to be tested, now in ContourDataSetRendererTests
        // final ContourDataSetCache cache = FXUtils.runAndWait(() -> new ContourDataSetCache(new XYChart(), new ContourDataSetRenderer(), dataSet));
        // assertDoesNotThrow(() -> cache.convertDataArrayToImage(TEST_DATA_Z, TEST_DATA_X.length, TEST_DATA_Y.length, ColorGradient.DEFAULT), "data to colour image conversion");
    }
}
