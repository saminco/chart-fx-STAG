package io.fair_acc.dataset.testdata;

import io.fair_acc.dataset.DataSet2D;

/**
 * Standard interface for test data set
 *
 * @author rstein
 * @param <D> generics for fluent design
 */
public interface TestDataSet<D extends TestDataSet<D>> extends DataSet2D {
    /**
     * generate test data set
     *
     * @param count number of bins
     * @return the generated array
     */
    double[] generateX(final int count);

    /**
     * generate test data set
     *
     * @param count number of bins
     * @return the generated array
     */
    double[] generateY(final int count);

    /**
     * generate a new set of numbers
     *
     * @return itself (fluent design)
     */
    D update();
}
