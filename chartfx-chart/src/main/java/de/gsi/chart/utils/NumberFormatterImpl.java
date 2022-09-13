package de.gsi.chart.utils;

import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberFormatterImpl extends StringConverter<Number> implements NumberFormatter {
    public final static char DEFAULT_DECIMAL_SEPARATOR = ' ';
    // com.ibm.icu.text.DecimalFormat

    protected DecimalFormat formatter;

    public NumberFormatterImpl() {
        super();
        String pattern = "###.###";
        formatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.applyPattern(pattern);
        formatter.setGroupingSize(0);
    }

    public NumberFormatterImpl(final int precision, final boolean exponentialForm) {
        this();
        setPrecision(precision);
        setExponentialForm(exponentialForm);
    }

    @Override
    public Number fromString(final String string) {
        return Double.parseDouble(string);
    }

    public DecimalFormat getFormatter() {
        return formatter;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.gsi.chart.utils.NumberFormatter#getPrecision()
     */
    @Override
    public int getPrecision() {
        return formatter.getMinimumIntegerDigits();
    }

    @Override
    public boolean isExponentialForm() {

        if (formatter.toPattern().equalsIgnoreCase("###.##E0")) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public NumberFormatter setExponentialForm(final boolean state) {

        String decimalFormat = "###.###";
        String scientificFormat = "###.##E0";

        if (state) {
            formatter.applyPattern(scientificFormat);
        }
        else {
            formatter.applyPattern(decimalFormat);
        }
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.gsi.chart.utils.NumberFormatter#setPrecision(int)
     */
    @Override
    public NumberFormatter setPrecision(final int precision) {
//        formatter.setSignificantDigitsUsed(true);
//         formatter.setMaximumSignificantDigits(precision);
        formatter.setMinimumIntegerDigits(precision);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(3);
        return this;
    }

    @Override
    public String toString(final double val) {
        return formatter.format(val);
    }

    @Override
    public String toString(final Number object) {
        return toString(object.doubleValue());
    }

}
