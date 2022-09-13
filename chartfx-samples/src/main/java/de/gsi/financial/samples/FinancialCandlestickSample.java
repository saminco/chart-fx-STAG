package de.gsi.financial.samples;

import javafx.application.Application;

import de.gsi.chart.XYChart;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.renderer.spi.financial.CandleStickRenderer;
import de.gsi.dataset.spi.DefaultDataSet;
import de.gsi.dataset.spi.financial.OhlcvDataSet;

/**
 * Candlestick Renderer Sample
 *
 * @author afischer
 */
public class FinancialCandlestickSample extends AbstractBasicFinancialApplication {
    protected void prepareRenderers(XYChart chart, OhlcvDataSet ohlcvDataSet, DefaultDataSet indiSet) {
        // create and apply renderers
        CandleStickRenderer candleStickRenderer = new CandleStickRenderer();
        candleStickRenderer.getDatasets().addAll(ohlcvDataSet);

        ErrorDataSetRenderer avgRenderer = new ErrorDataSetRenderer();
        avgRenderer.setDrawMarker(false);
        avgRenderer.setErrorType(ErrorStyle.NONE);
        avgRenderer.getDatasets().addAll(indiSet);

        chart.getRenderers().clear();
        chart.getRenderers().add(candleStickRenderer);
        chart.getRenderers().add(avgRenderer);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }
}
