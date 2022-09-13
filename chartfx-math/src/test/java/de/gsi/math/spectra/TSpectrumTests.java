package de.gsi.math.spectra;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DefaultDataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import de.gsi.dataset.spi.utils.DoublePoint;
import de.gsi.dataset.testdata.spi.GaussFunction;
import de.gsi.math.ArrayMath;
import de.gsi.math.ArrayUtils;
import de.gsi.math.DataSetMath;
import de.gsi.math.spectra.TSpectrum.Direction;
import de.gsi.math.spectra.TSpectrum.FilterOrder;
import de.gsi.math.spectra.TSpectrum.SmoothWindow;

public class TSpectrumTests {
    @Test
    public void basicBackgroundTests() {
        DoubleDataSet testDataSet = generateSineWaveSpectrumData(12);
        final double[] rawDataDecibel = testDataSet.getYValues();
        final double[] dataLin = ArrayMath.inverseDecibel(rawDataDecibel);

        double[] destVector = new double[dataLin.length];
        for (Boolean compton : new Boolean[] { true, false }) {
            for (Direction direction : Direction.values()) {
                for (FilterOrder filterOrder : FilterOrder.values()) {
                    for (SmoothWindow smoothWindow : SmoothWindow.values()) {
                        for (int nIterations : new int[] { 1, 3 })
                            assertDoesNotThrow(() -> TSpectrum.background(dataLin, destVector, dataLin.length, nIterations, direction, filterOrder, smoothWindow, compton));
                    }
                }
            }
        }

        final int nIterations = 1;
        final Direction direction = Direction.DECREASING;
        final FilterOrder filterOrder = FilterOrder.ORDER_2;
        final SmoothWindow smoothWindow = SmoothWindow.SMOOTHING_WIDTH3;
        final boolean compton = true;

        assertDoesNotThrow(() -> TSpectrum.background(dataLin, destVector, dataLin.length, nIterations, direction, filterOrder, smoothWindow, compton));

        assertThrows(IllegalArgumentException.class, () -> TSpectrum.background(dataLin, destVector, 0, nIterations, direction, filterOrder, smoothWindow, compton));
        assertThrows(IllegalArgumentException.class, () -> TSpectrum.background(null, destVector, dataLin.length, nIterations, direction, filterOrder, smoothWindow, compton));
        assertThrows(IllegalArgumentException.class, () -> TSpectrum.background(new double[3], destVector, dataLin.length, nIterations, direction, filterOrder, smoothWindow, compton));
        assertThrows(IllegalArgumentException.class, () -> TSpectrum.background(dataLin, destVector, dataLin.length, 0, direction, filterOrder, smoothWindow, compton));
        assertThrows(IllegalArgumentException.class, () -> TSpectrum.background(dataLin, destVector, dataLin.length, 100, direction, filterOrder, smoothWindow, compton));
        assertThrows(IllegalArgumentException.class, () -> TSpectrum.background(dataLin, destVector, dataLin.length, nIterations, direction, null, smoothWindow, compton));
        assertThrows(IllegalArgumentException.class, () -> TSpectrum.background(dataLin, destVector, dataLin.length, nIterations, direction, filterOrder, null, compton));
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "false" })
    public void basicComptonTests(final boolean multipleComptonPeaks) {
        DoubleDataSet testDataSet = generateDiracData(12, multipleComptonPeaks);
        final double[] rawDataDecibel = testDataSet.getYValues();
        final double[] dataLin = ArrayMath.inverseDecibel(rawDataDecibel);
        double[] destVector = new double[dataLin.length];

        final int nIterations = 1;
        final Direction direction = Direction.DECREASING;
        final FilterOrder filterOrder = FilterOrder.ORDER_2;
        final SmoothWindow smoothWindow = SmoothWindow.SMOOTHING_WIDTH3;
        final boolean compton = true;

        assertDoesNotThrow(() -> TSpectrum.background(dataLin, destVector, dataLin.length, nIterations, direction, filterOrder, smoothWindow, compton));
    }

    @Test
    public void basicMarkovBackgroundTests() {
        DoubleDataSet testDataSet = generateSineWaveSpectrumData(12);
        final double[] rawDataDecibel = testDataSet.getYValues();
        final double[] dataLin = ArrayMath.inverseDecibel(rawDataDecibel);
        final double[] dataLin2 = new double[dataLin.length];

        double[] destVector = new double[dataLin.length];

        assertDoesNotThrow(() -> TSpectrum.smoothMarkov(dataLin, destVector, dataLin.length, 2));

        assertThrows(IllegalArgumentException.class, () -> TSpectrum.smoothMarkov(null, destVector, dataLin.length, 2));
        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.smoothMarkov(new double[dataLin.length - 1], destVector, dataLin.length, 2));

        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.smoothMarkov(dataLin, destVector, dataLin.length, 0));

        ArrayUtils.fillArray(dataLin2, -Double.MAX_VALUE);
        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.smoothMarkov(dataLin2, destVector, dataLin.length, 2));

        ArrayUtils.fillArray(dataLin2, +Double.MAX_VALUE);
        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.smoothMarkov(dataLin2, destVector, dataLin.length, 2));

        ArrayUtils.fillArray(dataLin2, 0.0);
        assertDoesNotThrow(() -> TSpectrum.smoothMarkov(dataLin2, destVector, dataLin.length, 2));
        assertDoesNotThrow(() -> TSpectrum.smoothMarkov(dataLin2, null, dataLin.length, 2));
        assertDoesNotThrow(() -> TSpectrum.smoothMarkov(dataLin2, new double[2], dataLin.length, 2));
    }

    @Test
    public void basicSearchTests() { // NOPMD - method length unavoidable
        DoubleDataSet testDataSet = generateSineWaveSpectrumData(512);
        final double[] freq = testDataSet.getXValues();
        final double[] rawDataDecibel = testDataSet.getYValues();
        final double[] dataLin = ArrayMath.inverseDecibel(rawDataDecibel);

        double[] destVector = new double[dataLin.length];
        final int nMaxPeaks = 100;
        final double sigma = 1.0;
        final double threshold = 1.0; // [%]
        List<DoublePoint> peaks = TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                dataLin.length, nMaxPeaks, sigma, threshold, //
                /* backgroundRemove */ false, //
                /* deconIterations */ 1, //
                /* markov */ false, //
                /* averWindow */ 5);

        assertEquals(2, peaks.size(), "peak finder with threshold = " + threshold);

        assertDoesNotThrow(() -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                                      dataLin.length, nMaxPeaks, sigma, threshold, //
                                      /* backgroundRemove */ false, //
                                      /* deconIterations */ 1, //
                                      /* markov */ false, //
                                      /* averWindow */ 5));

        assertDoesNotThrow(() -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                                      dataLin.length, nMaxPeaks, sigma, threshold, //
                                      /* backgroundRemove */ true, //
                                      /* deconIterations */ 1, //
                                      /* markov */ false, //
                                      /* averWindow */ 5));

        assertDoesNotThrow(() -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                                      dataLin.length, nMaxPeaks, sigma, threshold, //
                                      /* backgroundRemove */ false, //
                                      /* deconIterations */ 1, //
                                      /* markov */ true, //
                                      /* averWindow */ 5));

        assertDoesNotThrow(() -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                                      dataLin.length, nMaxPeaks, sigma, threshold, //
                                      /* backgroundRemove */ true, //
                                      /* deconIterations */ 1, //
                                      /* markov */ true, //
                                      /* averWindow */ 5));

        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.search(null, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                           dataLin.length, nMaxPeaks, sigma, threshold, //
                           /* backgroundRemove */ false, //
                           /* deconIterations */ 1, //
                           /* markov */ false, //
                           /* averWindow */ 5));

        assertThrows(IllegalArgumentException.class, () -> TSpectrum.search(freq, null, destVector, //
                                                                dataLin.length, nMaxPeaks, sigma, threshold, //
                                                                /* backgroundRemove */ false, //
                                                                /* deconIterations */ 1, //
                                                                /* markov */ false, //
                                                                /* averWindow */ 5));

        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.search(new double[freq.length - 1], ArrayMath.inverseDecibel(rawDataDecibel),
                           destVector, //
                           dataLin.length, nMaxPeaks, sigma, threshold, //
                           /* backgroundRemove */ false, //
                           /* deconIterations */ 1, //
                           /* markov */ false, //
                           /* averWindow */ 5));

        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.search(freq, new double[freq.length - 1], destVector, //
                           dataLin.length, nMaxPeaks, sigma, threshold, //
                           /* backgroundRemove */ false, //
                           /* deconIterations */ 1, //
                           /* markov */ false, //
                           /* averWindow */ 5));

        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                           dataLin.length, nMaxPeaks, 0.5, threshold, //
                           /* backgroundRemove */ false, //
                           /* deconIterations */ 1, //
                           /* markov */ false, //
                           /* averWindow */ 5));

        assertDoesNotThrow(() -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), null, //
                                      dataLin.length, nMaxPeaks, sigma, threshold, //
                                      /* backgroundRemove */ false, //
                                      /* deconIterations */ 1, //
                                      /* markov */ false, //
                                      /* averWindow */ 5));

        assertDoesNotThrow(
                () -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), new double[freq.length - 1], //
                           dataLin.length, nMaxPeaks, sigma, threshold, //
                           /* backgroundRemove */ false, //
                           /* deconIterations */ 1, //
                           /* markov */ false, //
                           /* averWindow */ 5));

        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                           dataLin.length, nMaxPeaks, sigma, 0, //
                           /* backgroundRemove */ false, //
                           /* deconIterations */ 1, //
                           /* markov */ false, //
                           /* averWindow */ 5));

        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                           dataLin.length, nMaxPeaks, sigma, 100, //
                           /* backgroundRemove */ false, //
                           /* deconIterations */ 1, //
                           /* markov */ false, //
                           /* averWindow */ 5));

        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                           dataLin.length, nMaxPeaks, 1025, threshold, //
                           /* backgroundRemove */ false, //
                           /* deconIterations */ 1, //
                           /* markov */ false, //
                           /* averWindow */ 5));

        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                           dataLin.length, nMaxPeaks, sigma, threshold, //
                           /* backgroundRemove */ false, //
                           /* deconIterations */ 1, //
                           /* markov */ true, //
                           /* averWindow */ 0));

        assertDoesNotThrow(() -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                                      dataLin.length, nMaxPeaks, sigma, threshold, //
                                      /* backgroundRemove */ false, //
                                      /* deconIterations */ 1, //
                                      /* markov */ true, //
                                      /* averWindow */ 10));

        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                           dataLin.length, nMaxPeaks, 24, threshold, //
                           /* backgroundRemove */ true, //
                           /* deconIterations */ 1, //
                           /* markov */ false, //
                           /* averWindow */ 5));

        assertDoesNotThrow(() -> TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                                      dataLin.length, nMaxPeaks, 24, threshold, //
                                      /* backgroundRemove */ false, //
                                      /* deconIterations */ 1, //
                                      /* markov */ false, //
                                      /* averWindow */ 5));
    }

    @ParameterizedTest
    @CsvSource({ "10, 1", "1, 2", "0.1, 3", "0.01, 4" })
    public void peakFinderTests(double threshold, final int nPeaks) {
        DoubleDataSet testDataSet = generateSineWaveSpectrumData(512);
        final double[] freq = testDataSet.getXValues();
        final double[] rawDataDecibel = testDataSet.getYValues();
        final double[] dataLin = ArrayMath.inverseDecibel(rawDataDecibel);

        double[] destVector = new double[dataLin.length];
        final int nMaxPeaks = 100;
        final double sigma = 1.0;
        List<DoublePoint> peaks = TSpectrum.search(freq, ArrayMath.inverseDecibel(rawDataDecibel), destVector, //
                dataLin.length, nMaxPeaks, sigma, threshold, //
                /* backgroundRemove */ false, //
                /* deconIterations */ 1, //
                /* markov */ false, //
                /* averWindow */ 5);

        assertEquals(nPeaks, peaks.size(), "peak finder with threshold = " + threshold);
    }

    @Test
    public void testDeconvolution() {
        int nIter = 2;
        int nRep = 2;
        double boost = 1.0;
        // check invalid inputs
        //  length = 0
        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.deconvolution(new double[10], new double[10], null, 0, nIter, nRep, boost));
        //  nRep = 0
        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.deconvolution(new double[10], new double[10], null, 10, nIter, 0, boost));
        //  distribution all zeroes
        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.deconvolution(new double[10], new double[10], null, 10, nIter, nRep, boost));

        // get input data
        DoubleDataSet dataSet = new DefaultDataSet(new GaussFunction("test", 50));
        DoubleDataSet dist = new DefaultDataSet(new GaussFunction("test", 50));
        assertDoesNotThrow(() -> TSpectrum.deconvolution(dataSet.getValues(DataSet.DIM_Y), dist.getValues(DataSet.DIM_Y), null, 50, nIter, nRep, boost));
        double[] output = new double[50]; // exact size
        assertSame(output, TSpectrum.deconvolution(dataSet.getValues(DataSet.DIM_Y), dist.getValues(DataSet.DIM_Y),
                                   output, 50, nIter, nRep, boost));
        output = new double[54]; // bigger return array
        assertSame(output, TSpectrum.deconvolution(dataSet.getValues(DataSet.DIM_Y), dist.getValues(DataSet.DIM_Y),
                                   output, 50, nIter, nRep, boost));
        output = new double[40]; // return array too small
        assertEquals(50, TSpectrum.deconvolution(dataSet.getValues(DataSet.DIM_Y), dist.getValues(DataSet.DIM_Y),
                                          output, 50, nIter, nRep, boost)
                                 .length);
    }

    @Test
    public void testDeconvolutionRL() {
        int nIter = 2;
        int nRep = 2;
        double boost = 1.0;
        // check invalid inputs
        //  length = 0
        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.deconvolutionRL(new double[10], new double[10], null, 0, nIter, nRep, boost));
        //  nRep = 0
        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.deconvolutionRL(new double[10], new double[10], null, 10, nIter, 0, boost));
        //  distribution all zeroes
        assertThrows(IllegalArgumentException.class,
                () -> TSpectrum.deconvolutionRL(new double[10], new double[10], null, 10, nIter, nRep, boost));

        // get input data
        DoubleDataSet dataSet = new DefaultDataSet(new GaussFunction("test", 50));
        DoubleDataSet dist = new DefaultDataSet(new GaussFunction("test", 50));
        assertDoesNotThrow(() -> TSpectrum.deconvolutionRL(dataSet.getValues(DataSet.DIM_Y), dist.getValues(DataSet.DIM_Y), null, 50, nIter, nRep, boost));
        double[] output = new double[50]; // exact size
        assertSame(output, TSpectrum.deconvolutionRL(dataSet.getValues(DataSet.DIM_Y), dist.getValues(DataSet.DIM_Y),
                                   output, 50, nIter, nRep, boost));
        output = new double[54]; // bigger return array
        assertSame(output, TSpectrum.deconvolutionRL(dataSet.getValues(DataSet.DIM_Y), dist.getValues(DataSet.DIM_Y),
                                   output, 50, nIter, nRep, boost));
        output = new double[40]; // return array too small
        assertEquals(50, TSpectrum.deconvolutionRL(dataSet.getValues(DataSet.DIM_Y), dist.getValues(DataSet.DIM_Y),
                                          output, 50, nIter, nRep, boost)
                                 .length);
    }

    @Test
    public void testUnfolding() {
        int numberIterations = 2;
        int numberRepetitions = 2;
        double boost = 1.2;
        int lenx = 50;
        int leny = 10;
        double[] source = new GaussFunction("gauss", lenx).getValues(DataSet.DIM_X);
        double[][] respMatrix = new double[leny][lenx];
        for (int i = 0; i < leny; i++) {
            System.arraycopy(new GaussFunction("gauss", leny + 2 * i).getValues(DataSet.DIM_X), 0, respMatrix[i], 0, leny + i);
        }
        // check valid inputs
        assertDoesNotThrow(() -> TSpectrum.unfolding(source, respMatrix, null, lenx, leny, numberIterations, numberRepetitions, boost));
        double[] output = new double[50]; // exact size
        assertSame(output, TSpectrum.unfolding(source, respMatrix, output, lenx, leny, numberIterations,
                                   numberRepetitions, boost));
        output = new double[54]; // bigger return array
        assertSame(output, TSpectrum.unfolding(source, respMatrix, output, lenx, leny, numberIterations,
                                   numberRepetitions, boost));
        output = new double[40]; // return array too small
        assertEquals(50, TSpectrum.unfolding(source, respMatrix, output, lenx, leny, numberIterations,
                                          numberRepetitions, boost)
                                 .length);

        // check invalid inputs
        assertThrows(IllegalArgumentException.class, // lenx < leny
                () -> TSpectrum.unfolding(source, respMatrix, null, 5, leny, numberIterations, numberRepetitions, boost));
        assertThrows(IllegalArgumentException.class, // empty column in resp matrix
                () -> TSpectrum.unfolding(source, new double[leny][lenx], null, lenx, leny, numberIterations, numberRepetitions, boost));
    }

    protected static DoubleDataSet generateSineWaveSpectrumData(final int nData) {
        DoubleDataSet function = new DoubleDataSet("composite sine", nData);
        for (int i = 0; i < nData; i++) {
            final double t = i;
            double y = 0;
            final double centreFrequency = 0.25;
            final double diffFrequency = 0.05;
            for (int j = 0; j < 8; j++) {
                final double a = 2.0 * Math.pow(10, -j);
                final double diff = j == 0 ? 0 : (j % 2 - 0.5) * j * diffFrequency;
                y += a * Math.sin(2.0 * Math.PI * (centreFrequency + diff) * t);
            }

            function.add(t, y);
        }

        return new DoubleDataSet(DataSetMath.magnitudeSpectrumDecibel(function));
    }

    protected static DoubleDataSet generateDiracData(final int nData, boolean multiple) {
        DoubleDataSet function = new DoubleDataSet("composite sine", nData);
        for (int i = 0; i < nData; i++) {
            final double t = i;
            double y;
            if (multiple) {
                y = i % (nData / 4) == 0 ? 1000.0 : 0.0;
            } else {
                y = i == nData / 2 ? 1000.0 : 0.0;
            }
            function.add(t, y);
        }

        return function;
    }
}
