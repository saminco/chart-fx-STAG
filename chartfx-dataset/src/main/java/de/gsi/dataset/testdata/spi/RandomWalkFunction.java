package de.gsi.dataset.testdata.spi;

/**
 * abstract error data set for graphical testing purposes this implementation generates a random walk (Brownian noise)
 * function.
 *
 * @author rstein
 */
public class RandomWalkFunction extends AbstractTestFunction<RandomWalkFunction> {
    private static final long serialVersionUID = 5274313670852663800L;

    /**
     * 
     * @param name data set name
     * @param count number of samples
     */
    public RandomWalkFunction(final String name, final int count) {
        super(name, count);
    }

    @Override
    public double[] generateY(final int count) {
        return RandomDataGenerator.generateDoubleArray(0, 0.01, count);
    }
}