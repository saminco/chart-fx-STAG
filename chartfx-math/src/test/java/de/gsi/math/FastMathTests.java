package de.gsi.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for FastMath identities
 *
 * @author rstein
 */
@SuppressWarnings("PMD.UnnecessaryFullyQualifiedName") // the fully qualified name is needed, because we have our own Math class
public class FastMathTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(FastMathTests.class);

    @Test
    public void cosineIdentityChecks() {
        double precision = MathBaseFast.getPrecision();
        int range = MathBaseFast.getPrecision();
        for (int i = -range; i < range; i++) {
            final float x = (float) (2.0 * java.lang.Math.PI * i / precision);
            final float val1 = (float) java.lang.Math.cos(x);
            final float val2 = MathBaseFast.cos(x);
            final float diffApproximation = java.lang.Math.abs(val1 - val2);
            assertTrue(diffApproximation <= 2e-3, i + ": cos(" + x + ") diff (float) = " + diffApproximation);
        }

        for (int i = -range; i < range; i++) {
            final double x = 2.0 * java.lang.Math.PI * i / precision;
            final double val1 = java.lang.Math.cos(x);
            final double val2 = MathBaseFast.cos(x);
            final double diffApproximation = java.lang.Math.abs(val1 - val2);
            assertTrue(diffApproximation <= 2e-3, i + ": cos(" + x + ") diff (double) = " + diffApproximation);
        }

        for (int i = -360; i < 360; i++) {
            final float x = (float) java.lang.Math.toRadians(i);
            final float val1 = (float) java.lang.Math.cos(x);
            final float val2 = MathBaseFast.cosDeg((float) i);
            final float diffApproximation = java.lang.Math.abs(val1 - val2);
            assertTrue(diffApproximation <= 2e-3, i + ": cos(" + x + ") diff (float, deg) = " + diffApproximation);
        }

        for (int i = -360; i < 360; i++) {
            final double x = java.lang.Math.toRadians(i);
            final double val1 = java.lang.Math.cos(x);
            final double val2 = MathBaseFast.cosDeg((double) i);
            final double diffApproximation = java.lang.Math.abs(val1 - val2);
            assertTrue(diffApproximation <= 2e-3, i + ": cos(" + x + ") diff (double, deg) = " + diffApproximation);
        }
    }

    @Test
    public void simpleTest() {
        final int oldPrecision = MathBaseFast.getPrecision();
        MathBaseFast.setPrecision(100);
        assertEquals(100, MathBaseFast.getPrecision(), "set precision");

        MathBaseFast.setPrecision(oldPrecision);

        assertThrows(IllegalArgumentException.class, () -> MathBaseFast.setPrecision(-1));
    }

    @Test
    public void sineIdentityChecks() {
        double precision = MathBaseFast.getPrecision();
        int range = MathBaseFast.getPrecision();
        for (int i = -range; i < range; i++) {
            final float x = (float) (2.0 * java.lang.Math.PI * i / precision);
            final float val1 = (float) java.lang.Math.sin(x);
            final float val2 = MathBaseFast.sin(x);
            final float diffApproximation = java.lang.Math.abs(val1 - val2);
            assertTrue(diffApproximation <= 1e-3, i + ": cos(" + x + ") diff (float) = " + diffApproximation);
        }

        for (int i = -range; i < range; i++) {
            final double x = 2.0 * java.lang.Math.PI * i / precision;
            final double val1 = java.lang.Math.sin(x);
            final double val2 = MathBaseFast.sin(x);
            final double diffApproximation = java.lang.Math.abs(val1 - val2);
            assertTrue(diffApproximation <= 1e-3, i + ": cos(" + x + ") diff (double) = " + diffApproximation);
        }

        for (int i = -360; i < 360; i++) {
            final float x = (float) java.lang.Math.toRadians(i);
            final float val1 = (float) java.lang.Math.sin(x);
            final float val2 = MathBaseFast.sinDeg((float) i);
            final float diffApproximation = java.lang.Math.abs(val1 - val2);
            assertTrue(diffApproximation <= 2e-3, i + ": sin(" + x + ") diff (float, deg) = " + diffApproximation);
        }

        for (int i = -360; i < 360; i++) {
            final double x = java.lang.Math.toRadians(i);
            final double val1 = java.lang.Math.sin(x);
            final double val2 = MathBaseFast.sinDeg((double) i);
            final double diffApproximation = java.lang.Math.abs(val1 - val2);
            assertTrue(diffApproximation <= 2e-3, i + ": sin(" + x + ") diff (double, deg) = " + diffApproximation);
        }
    }

    @Test
    public void performanceTest() {
        int reps = 1 << 20;
        int sets = 5;

        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            // code that requires less then 3 seconds to execute
            LOGGER.atInfo().log("run\tsinTab\tcosTab\tsinLib\tsinJaFa\tcosJaFa\tsinApa\tcosApa");
            for (int i = 0; i < sets; i++) {
                LOGGER.atInfo().addArgument(i).addArgument(testSinTab(reps)).addArgument(testCosTab(reps)).addArgument(testSinLib(reps)).addArgument(testSinJaFama(reps)).addArgument(testCosJaFama(reps)).addArgument(testSinApache(reps)).addArgument(testCosApache(reps)).log("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{} [ns]");
            }
        }, "performance test length exceeded");
    }

    public static void main(String[] args) {
        new FastMathTests().performanceTest();
    }

    private static float[] sample(int n) {
        Random rand = new Random();
        float[] values = new float[n];
        for (int i = 0; i < n; i++) {
            values[i] = 400 * (rand.nextFloat() * 2 - 1);
        }
        return values;
    }

    private static float testCosApache(int n) {
        float[] sample = sample(n);
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            sample[i] = (float) org.apache.commons.math3.util.FastMath.cos(sample[i]);
        }
        time += System.nanoTime();
        return time / n;
    }

    private static float testCosJaFama(int n) {
        float[] sample = sample(n);
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            sample[i] = (float) net.jafama.FastMath.cosQuick(sample[i]);
        }
        time += System.nanoTime();
        return time / n;
    }

    private static float testCosTab(int n) {
        float[] sample = sample(n);
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            sample[i] = MathBaseFast.cos(sample[i]);
        }
        time += System.nanoTime();
        return time / n;
    }

    private static float testSinApache(int n) {
        float[] sample = sample(n);
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            sample[i] = (float) org.apache.commons.math3.util.FastMath.sin(sample[i]);
        }
        time += System.nanoTime();
        return time / n;
    }

    private static float testSinJaFama(int n) {
        float[] sample = sample(n);
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            sample[i] = (float) net.jafama.FastMath.sinQuick(sample[i]);
        }
        time += System.nanoTime();
        return time / n;
    }

    private static float testSinLib(int n) {
        float[] sample = sample(n);
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            sample[i] = (float) java.lang.Math.sin(sample[i]);
        }
        time += System.nanoTime();
        return time / n;
    }

    private static float testSinTab(int n) {
        float[] sample = sample(n);
        long time = -System.nanoTime();
        for (int i = 0; i < n; i++) {
            sample[i] = MathBaseFast.sin(sample[i]);
        }
        time += System.nanoTime();
        return time / n;
    }
}
