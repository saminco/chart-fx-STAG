package de.gsi.chart.renderer.spi.financial;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static de.gsi.chart.renderer.spi.financial.css.FinancialColorSchemeConstants.SAND;

import java.util.*;

import javafx.scene.Scene;
import javafx.stage.Stage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.AxisLabelOverlapPolicy;
import de.gsi.chart.axes.spi.CategoryAxis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.spi.financial.PositionFinancialRendererPaintAfterEP.PositionRendered;
import de.gsi.chart.renderer.spi.financial.css.FinancialColorSchemeConfig;
import de.gsi.chart.renderer.spi.financial.utils.CalendarUtils;
import de.gsi.chart.renderer.spi.financial.utils.FinancialTestUtils;
import de.gsi.chart.renderer.spi.financial.utils.Interval;
import de.gsi.chart.renderer.spi.financial.utils.PositionFinancialDataSetDummy;
import de.gsi.chart.ui.utils.JavaFXInterceptorUtils;
import de.gsi.chart.ui.utils.TestFx;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.AbstractDataSet;
import de.gsi.dataset.spi.financial.OhlcvDataSet;
import de.gsi.dataset.utils.ProcessingProfiler;

@ExtendWith(ApplicationExtension.class)
@ExtendWith(JavaFXInterceptorUtils.SelectiveJavaFxInterceptor.class)
class PositionFinancialRendererPaintAfterEPTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PositionFinancialRendererPaintAfterEPTest.class);
    private XYChart chart;
    private OhlcvDataSet ohlcvDataSet;

    @Start
    public void start(Stage stage) throws Exception {
        ProcessingProfiler.setDebugState(false); // enable for detailed renderer tracing
        ohlcvDataSet = new OhlcvDataSet("ohlc1");
        ohlcvDataSet.setData(FinancialTestUtils.createTestOhlcv2());
        CandleStickRenderer candleStickRenderer = new CandleStickRenderer(true);
        candleStickRenderer.setComputeLocalRange(true);

        final DefaultNumericAxis xAxis = new DefaultNumericAxis("time", "iso");
        xAxis.setTimeAxis(true);
        xAxis.setAutoRangeRounding(false);
        xAxis.setAutoRanging(false);
        Interval<Calendar> xrange = CalendarUtils.createByDateInterval("2020/08/24-2020/11/12");
        xAxis.set(xrange.from.getTime().getTime() / 1000.0, xrange.to.getTime().getTime() / 1000.0);

        final DefaultNumericAxis yAxis = new DefaultNumericAxis("price", "points");
        yAxis.setAutoRanging(false);

        // prepare chart structure
        chart = new XYChart(xAxis, yAxis);
        chart.getGridRenderer().setDrawOnTop(false);

        // define test positions (no conversion from domain objects)
        List<PositionRendered> positionList = new ArrayList<>();
        positionList.add(fillPositionRendered(0, 1599228000, 2, 1.0, 1, 3407.25, true, new Double[] {}));
        positionList.add(fillPositionRendered(0, 1599228000, 2, 1.0, 1, 3407.25, true, new Double[] { 1.5989688E9, 3516.75, -1.0 }));
        positionList.add(fillPositionRendered(1, 1599746400, 1, 1.0, -1, 3330.0, true, new Double[] {}));
        positionList.add(fillPositionRendered(1, 1600437600, 2, 1.0, -1, 3316.25, true, new Double[] { 1.5997464E9, 3330.0, 1.0 }));
        positionList.add(fillPositionRendered(2, 1601301600, 1, 2.0, 1, 3291.0, true, new Double[] {}));
        positionList.add(fillPositionRendered(2, 1601906400, 2, 1.0, 1, 3393.0, true, new Double[] { 1.6013016E9, 3291.0, 1.0 }));
        positionList.add(fillPositionRendered(3, 1603116000, 2, 1.0, 1, 3422.75, true, new Double[] { 1.6013016E9, 3291.0, 1.0 }));
        PositionFinancialDataSetDummy positionFinancialDataSet = new PositionFinancialDataSetDummy(positionList);

        for (PositionRendered positionRendered : positionList) {
            LOGGER.info(positionRendered.toString());
        }
        Assertions.assertEquals(0, positionList.get(0).compareTo(positionList.get(1)));
        Assertions.assertEquals(-1, positionList.get(1).compareTo(positionList.get(2)));
        Assertions.assertEquals(1, positionList.get(2).compareTo(positionList.get(1)));

        // create test instance!
        PositionFinancialRendererPaintAfterEP positionPaintAfterEPTested = new PositionFinancialRendererPaintAfterEP(positionFinancialDataSet, chart);

        candleStickRenderer.getDatasets().add(ohlcvDataSet);
        chart.getRenderers().clear();
        chart.getRenderers().add(candleStickRenderer);
        // Extension point usage
        candleStickRenderer.addPaintAfterEp(positionPaintAfterEPTested);

        new FinancialColorSchemeConfig().applyTo(SAND, chart);

        stage.setScene(new Scene(chart, 800, 600));
        stage.show();
    }

    @TestFx
    public void categoryAxisTest() {
        final CategoryAxis xAxis = new CategoryAxis("time [iso]");
        xAxis.setTickLabelRotation(90);
        xAxis.setOverlapPolicy(AxisLabelOverlapPolicy.SKIP_ALT);
        ohlcvDataSet.setCategoryBased(true);
        chart.getAxes().add(0, xAxis);
        chart.layoutChildren();
    }

    @Test
    public void illegalDataSetTest() {
        DataSet illegalDataSet = new AbstractDataSet<PositionFinancialDataSetDummy>("testIllegalModel", 2) {
            @Override
            public double get(int dimIndex, int index) {
                return 0;
            }

            @Override
            public int getDataCount() {
                return 1;
            }

            @Override
            public DataSet set(DataSet other, boolean copy) {
                return null;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> new PositionFinancialRendererPaintAfterEP(illegalDataSet, chart));
    }

    private PositionRendered fillPositionRendered(int positionId, int index, int entryExit, double quantity, int posType, double price, boolean closed, Double[] joinedEntries) {
        PositionRendered positionRendered = new PositionRendered();
        positionRendered.positionId = positionId;
        positionRendered.index = index;
        positionRendered.entryExit = entryExit;
        positionRendered.quantity = quantity;
        positionRendered.posType = posType;
        positionRendered.price = price;
        positionRendered.closed = closed;
        List<List<Double>> joinedEntriesList = new ArrayList<>();
        List<Double> row = new ArrayList<>();
        Collections.addAll(row, joinedEntries);
        joinedEntriesList.add(row);
        positionRendered.joinedEntries = joinedEntriesList;

        return positionRendered;
    }
}
