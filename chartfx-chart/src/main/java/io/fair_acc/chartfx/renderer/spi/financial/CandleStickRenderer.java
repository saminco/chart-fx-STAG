package io.fair_acc.chartfx.renderer.spi.financial;

import static io.fair_acc.dataset.DataSet.DIM_X;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

import io.fair_acc.chartfx.renderer.Renderer;
import io.fair_acc.chartfx.renderer.spi.financial.service.OhlcvRendererEpData;
import io.fair_acc.chartfx.renderer.spi.financial.service.RendererPaintAfterEP;
import io.fair_acc.chartfx.renderer.spi.financial.service.RendererPaintAfterEPAware;
import io.fair_acc.chartfx.ui.css.DataSetNode;
import io.fair_acc.chartfx.ui.css.StyleUtil;
import io.fair_acc.dataset.DataSet;
import io.fair_acc.dataset.spi.financial.OhlcvDataSet;
import io.fair_acc.dataset.spi.financial.api.attrs.AttributeModelAware;
import io.fair_acc.dataset.spi.financial.api.ohlcv.IOhlcvItemAware;

/**
 * Candlestick renderer
 *<p>
 * A candlestick chart (also called Japanese candlestick chart) is a style of financial chart used to describe price movements of a security,
 * derivative, or currency.
 *<p>
 * If the asset closed higher than it opened, the body is hollow or unfilled, with the opening price at the bottom of the body and the closing price at the top.
 * If the asset closed lower than it opened, the body is solid or filled, with the opening price at the top and the closing price at the bottom.
 * Thus, the color of the candle represents the price movement relative to the prior period's close and the "fill" (solid or hollow)
 * of the candle represents the price direction of the period in isolation (solid for a higher open and lower close; hollow for a lower open and a higher close).
 * <p>
 * A black (or red) candle represents a price action with a lower closing price than the prior candle's close.
 * A white (or green) candle represents a higher closing price than the prior candle's close.
 * <p>
 * In practice, any color can be assigned to rising or falling price candles. A candlestick need not have either a body or a wick.
 * Generally, the longer the body of the candle, the more intense the trading.
 *
 * @see <a href="https://www.investopedia.com/terms/c/candlestick.asp">Candlestick Investopedia</a>
 *
 * @author afischer
 */
@SuppressWarnings({ "PMD.ExcessiveMethodLength", "PMD.NPathComplexity", "PMD.ExcessiveParameterList" })
// designated purpose of this class
public class CandleStickRenderer extends AbstractFinancialRenderer<CandleStickRenderer> implements Renderer, RendererPaintAfterEPAware {
    private final boolean paintVolume;
    private final FindAreaDistances findAreaDistances;

    protected final List<RendererPaintAfterEP> paintAfterEPS = new ArrayList<>();

    public CandleStickRenderer(boolean paintVolume) {
        StyleUtil.addStyles(this, "candlestick");
        this.paintVolume = paintVolume;
        this.findAreaDistances = paintVolume ? new XMinVolumeMaxAreaDistances() : new XMinAreaDistances();
    }

    public CandleStickRenderer() {
        this(false);
    }

    public boolean isPaintVolume() {
        return paintVolume;
    }

    /**
     * @param dataSet the data set for which the representative icon should be generated
     * @param canvas the canvas in which the representative icon should be drawn
     * @return true if the renderer generates symbols that should be displayed
     */
    @Override
    public boolean drawLegendSymbol(final DataSetNode dataSet, final Canvas canvas) {
        final int width = (int) canvas.getWidth();
        final int height = (int) canvas.getHeight();
        final var gc = canvas.getGraphicsContext2D();

        gc.save();
        final FinancialDataSetNode style = (FinancialDataSetNode) dataSet;
        var candleLongColor = style.getCandleLongColor();
        var candleShortColor = style.getCandleShortColor();

        gc.setFill(candleLongColor);
        gc.setStroke(candleLongColor);
        gc.fillRect(1, 3, width / 2.0 - 2.0, height - 8.0);
        double x = width / 4.0;
        gc.strokeLine(x, 1, x, height - 2.0);

        gc.setFill(candleShortColor);
        gc.setStroke(candleShortColor);
        gc.fillRect(width / 2.0 + 2.0, 4, width - 2.0, height - 12.0);
        x = 3.0 * width / 4.0 + 1.5;
        gc.strokeLine(x, 1, x, height - 3.0);
        gc.restore();

        return true;
    }

    @Override
    protected CandleStickRenderer getThis() {
        return this;
    }

    @Override
    protected void render(GraphicsContext gc, DataSet ds, DataSetNode styleNode) {
        if (ds.getDimension() < 7) {
            return;
        }

        AttributeModelAware attrs = null;
        if (ds instanceof AttributeModelAware) {
            attrs = (AttributeModelAware) ds;
        }
        IOhlcvItemAware itemAware = null;
        if (ds instanceof IOhlcvItemAware) {
            itemAware = (IOhlcvItemAware) ds;
        }
        boolean isEpAvailable = !paintAfterEPS.isEmpty() || paintBarMarker != null;

        gc.save();

        // default styling level
        FinancialDataSetNode style = (FinancialDataSetNode) styleNode;
        gc.setLineWidth(style.getLineWidth());
        gc.setLineDashes(style.getLineDashes());

        // financial styling level
        var candleLongColor = style.getCandleLongColor();
        var candleShortColor = style.getCandleShortColor();
        var candleLongWickColor = style.getCandleLongWickColor();
        var candleShortWickColor = style.getCandleShortWickColor();
        var candleShadowColor = style.getCandleShadowColor();
        var candleVolumeLongColor = style.getCandleVolumeLongColor();
        var candleVolumeShortColor = style.getCandleVolumeShortColor();
        double barWidthPercent = style.getBarWidthPercent();
        double shadowLineWidth = style.getShadowLineWidth();
        double shadowTransPercent = style.getShadowTransPercent();

        if (ds.getDataCount() > 0) {
            int iMin = ds.getIndex(DIM_X, xMin);
            if (iMin < 0)
                iMin = 0;
            int iMax = Math.min(ds.getIndex(DIM_X, xMax) + 1, ds.getDataCount());

            double[] distances = null;
            var minRequiredWidth = 0.0;
            if (styleNode.getLocalIndex() == 0) {
                distances = findAreaDistances(findAreaDistances, ds, xAxis, yAxis, xMin, xMax);
                minRequiredWidth = distances[0];
            }
            double localBarWidth = minRequiredWidth * barWidthPercent;
            double barWidthHalf = localBarWidth / 2.0;

            for (int i = iMin; i < iMax; i++) {
                double x0 = xAxis.getDisplayPosition(ds.get(DIM_X, i));
                double yOpen = yAxis.getDisplayPosition(ds.get(OhlcvDataSet.DIM_Y_OPEN, i));
                double yHigh = yAxis.getDisplayPosition(ds.get(OhlcvDataSet.DIM_Y_HIGH, i));
                double yLow = yAxis.getDisplayPosition(ds.get(OhlcvDataSet.DIM_Y_LOW, i));
                double yClose = yAxis.getDisplayPosition(ds.get(OhlcvDataSet.DIM_Y_CLOSE, i));

                double yDiff = yOpen - yClose;
                double yMin = yDiff > 0 ? yClose : yOpen;

                // prepare extension point data (if EPs available)
                OhlcvRendererEpData data = null;
                if (isEpAvailable) {
                    data = new OhlcvRendererEpData();
                    data.gc = gc;
                    data.ds = ds;
                    data.style = style;
                    data.attrs = attrs;
                    data.ohlcvItemAware = itemAware;
                    data.ohlcvItem = itemAware != null ? itemAware.getItem(i) : null;
                    data.index = i;
                    data.minIndex = iMin;
                    data.maxIndex = iMax;
                    data.barWidth = localBarWidth;
                    data.barWidthHalf = barWidthHalf;
                    data.xCenter = x0;
                    data.yOpen = yOpen;
                    data.yHigh = yHigh;
                    data.yLow = yLow;
                    data.yClose = yClose;
                    data.yDiff = yDiff;
                    data.yMin = yMin;
                }

                // paint volume
                if (paintVolume) {
                    assert distances != null;
                    paintVolume(gc, ds, i, candleVolumeLongColor, candleVolumeShortColor, yAxis, distances, localBarWidth, barWidthHalf, x0);
                }

                // paint shadow
                if (candleShadowColor != null) {
                    double lineWidth = gc.getLineWidth();
                    paintCandleShadow(gc,
                            candleShadowColor, shadowLineWidth, shadowTransPercent,
                            localBarWidth, barWidthHalf, x0, yOpen, yClose, yLow, yHigh, yDiff, yMin);
                    gc.setLineWidth(lineWidth);
                }

                // choose color of the bar
                Paint barPaint = data == null ? null : getPaintBarColor(data);

                if (yDiff > 0) {
                    gc.setFill(Objects.requireNonNullElse(barPaint, candleLongColor));
                    gc.setStroke(Objects.requireNonNullElse(barPaint, candleLongWickColor));
                } else {
                    yDiff = Math.abs(yDiff);
                    gc.setFill(Objects.requireNonNullElse(barPaint, candleShortColor));
                    gc.setStroke(Objects.requireNonNullElse(barPaint, candleShortWickColor));
                }

                // paint candle
                gc.strokeLine(x0, yLow, x0, yDiff > 0 ? yOpen : yClose);
                gc.strokeLine(x0, yHigh, x0, yDiff > 0 ? yClose : yOpen);
                gc.fillRect(x0 - barWidthHalf, yMin, localBarWidth, yDiff); // open-close
                gc.strokeRect(x0 - barWidthHalf, yMin, localBarWidth, yDiff); // open-close

                // extension point - paint after painting of candle
                if (!paintAfterEPS.isEmpty()) {
                    paintAfter(data);
                }
            }
        }
        gc.restore();

        // possibility to re-arrange y-axis by min/max of dataset (after paint)
        if (computeLocalRange()) {
            applyLocalYRange(ds, yAxis, xMin, xMax);
        }
    }

    /**
     * Handle extension point PaintAfter
     *
     * @param data filled domain object which is provided to external extension points.
     */
    protected void paintAfter(OhlcvRendererEpData data) {
        for (RendererPaintAfterEP paintAfterEP : paintAfterEPS) {
            paintAfterEP.paintAfter(data);
        }
    }

    /**
     * Simple support for candle shadows painting. Without effects - performance problems.
     * The shadow has to be activated by parameter configuration candleShadowColor in css.
     *
     * @param gc                 GraphicsContext
     * @param shadowColor        color for shadow
     * @param shadowLineWidth    line width for painting shadow
     * @param shadowTransPercent object transposition for painting shadow in percentage
     * @param localBarWidth      width of bar
     * @param barWidthHalf       half width of bar
     * @param x0                 the center of the bar for X coordination
     * @param yOpen              coordination of Open price
     * @param yClose             coordination of Close price
     * @param yLow               coordination of Low price
     * @param yHigh              coordination of High price
     * @param yDiff              Difference of candle for painting candle body
     * @param yMin               minimal coordination for painting of candle body
     */
    protected void paintCandleShadow(GraphicsContext gc, Paint shadowColor, double shadowLineWidth, double shadowTransPercent, double localBarWidth, double barWidthHalf,
            double x0, double yOpen, double yClose, double yLow,
            double yHigh, double yDiff, double yMin) {
        double trans = shadowTransPercent * barWidthHalf;
        gc.setLineWidth(shadowLineWidth);
        gc.setFill(shadowColor);
        gc.setStroke(shadowColor);
        gc.strokeLine(x0 + trans, yLow + trans,
                x0 + trans, yDiff > 0 ? yOpen + trans : yClose + trans);
        gc.strokeLine(x0 + trans, yHigh + trans,
                x0 + trans, yDiff > 0 ? yClose + trans : yOpen + trans);
        gc.fillRect(x0 - barWidthHalf + trans, yMin + trans, localBarWidth, Math.abs(yDiff));
    }

    //-------------- injections --------------------------------------------

    @Override
    public void addPaintAfterEp(RendererPaintAfterEP paintAfterEP) {
        paintAfterEPS.add(paintAfterEP);
    }

    @Override
    public List<RendererPaintAfterEP> getPaintAfterEps() {
        return paintAfterEPS;
    }
}
