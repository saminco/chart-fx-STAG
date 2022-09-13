package de.gsi.chart.renderer.spi.financial.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static de.gsi.chart.renderer.spi.financial.css.FinancialColorSchemeConstants.BLACKBERRY;
import static de.gsi.chart.renderer.spi.financial.css.FinancialColorSchemeConstants.getDefaultColorSchemes;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.renderer.spi.financial.*;
import de.gsi.chart.renderer.spi.financial.service.RendererPaintAfterEP;
import de.gsi.chart.renderer.spi.financial.service.RendererPaintAfterEPAware;
import de.gsi.chart.renderer.spi.financial.utils.FinancialTestUtils;
import de.gsi.chart.renderer.spi.financial.utils.FootprintRenderedAPIDummyAdapter;
import de.gsi.chart.renderer.spi.financial.utils.PositionFinancialDataSetDummy;
import de.gsi.chart.ui.utils.JavaFXInterceptorUtils.SelectiveJavaFxInterceptor;
import de.gsi.chart.ui.utils.TestFx;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.financial.OhlcvDataSet;

@ExtendWith(ApplicationExtension.class)
@ExtendWith(SelectiveJavaFxInterceptor.class)
class FinancialColorSchemeConfigTest {
    private FinancialColorSchemeConfig financialColorSchemeConfig;
    private OhlcvDataSet ohlcvDataSet;
    private Renderer renderer;
    private XYChart chart;

    @BeforeEach
    void setUp() {
        financialColorSchemeConfig = new FinancialColorSchemeConfig();
        ohlcvDataSet = new OhlcvDataSet("ohlc1");
        ohlcvDataSet.setData(FinancialTestUtils.createTestOhlcv());
        renderer = new CandleStickRenderer();
    }

    @Start
    public void start(Stage stage) {
        setUp();
        chart = new XYChart();
        // possibility to configure extension points
        ((CandleStickRenderer) renderer).addPaintAfterEp(new PositionFinancialRendererPaintAfterEP(new PositionFinancialDataSetDummy(new ArrayList<>()), chart));
        renderer.getDatasets().add(ohlcvDataSet);
        chart.getRenderers().add(renderer); // one possibility
        chart.getDatasets().add(ohlcvDataSet); // second possibility
        stage.setScene(new Scene(chart));
        stage.show();
    }

    @Test
    void applySchemeToDataset() {
        financialColorSchemeConfig.applySchemeToDataset(BLACKBERRY, "custom1=red", ohlcvDataSet, renderer);
        assertEquals("custom1=red", ohlcvDataSet.getStyle());
        for (String colorScheme : getDefaultColorSchemes()) {
            financialColorSchemeConfig.applySchemeToDataset(colorScheme, null, ohlcvDataSet, renderer);
        }
        assertThrows(IllegalArgumentException.class, () -> financialColorSchemeConfig.applySchemeToDataset("NOT_EXIST", null, ohlcvDataSet, renderer));

        renderer = new HighLowRenderer();
        for (String colorScheme : getDefaultColorSchemes()) {
            financialColorSchemeConfig.applySchemeToDataset(colorScheme, null, ohlcvDataSet, renderer);
        }
        assertThrows(IllegalArgumentException.class, () -> financialColorSchemeConfig.applySchemeToDataset("NOT_EXIST", null, ohlcvDataSet, renderer));

        renderer = new FootprintRenderer(new FootprintRenderedAPIDummyAdapter(null));
        for (String colorScheme : getDefaultColorSchemes()) {
            financialColorSchemeConfig.applySchemeToDataset(colorScheme, null, ohlcvDataSet, renderer);
        }
        assertThrows(IllegalArgumentException.class, () -> financialColorSchemeConfig.applySchemeToDataset("NOT_EXIST", null, ohlcvDataSet, renderer));
    }

    @Test
    void testApplySchemeToDataset() {
        for (String colorScheme : getDefaultColorSchemes()) {
            financialColorSchemeConfig.applySchemeToDataset(colorScheme, ohlcvDataSet, renderer);
        }
        assertThrows(IllegalArgumentException.class, () -> financialColorSchemeConfig.applySchemeToDataset("NOT_EXIST", ohlcvDataSet, renderer));

        renderer = new HighLowRenderer();
        for (String colorScheme : getDefaultColorSchemes()) {
            financialColorSchemeConfig.applySchemeToDataset(colorScheme, ohlcvDataSet, renderer);
        }
        assertThrows(IllegalArgumentException.class, () -> financialColorSchemeConfig.applySchemeToDataset("NOT_EXIST", ohlcvDataSet, renderer));

        renderer = new FootprintRenderer(new FootprintRenderedAPIDummyAdapter(null));
        for (String colorScheme : getDefaultColorSchemes()) {
            financialColorSchemeConfig.applySchemeToDataset(colorScheme, ohlcvDataSet, renderer);
        }
        assertThrows(IllegalArgumentException.class, () -> financialColorSchemeConfig.applySchemeToDataset("NOT_EXIST", ohlcvDataSet, renderer));

        renderer = new EmptyFinancialRenderer();
        ((EmptyFinancialRenderer) renderer).addPaintAfterEp(new PositionFinancialRendererPaintAfterEP(new PositionFinancialDataSetDummy(new ArrayList<>()), chart));
        assertThrows(IllegalArgumentException.class, () -> financialColorSchemeConfig.applySchemeToDataset("NOT_EXIST", ohlcvDataSet, renderer));
    }

    @TestFx
    void applyTo() throws Exception {
        // just test pass of the all configuration, no test result in the chart - just configuration which is changed
        financialColorSchemeConfig.applyTo(BLACKBERRY, "custom1=white", chart);
        for (String colorScheme : getDefaultColorSchemes()) {
            financialColorSchemeConfig.applyTo(colorScheme, null, chart);
        }
    }

    @TestFx
    void testApplyTo() throws Exception {
        // just test pass of the all configuration, no test result in the chart - just configuration which is changed
        financialColorSchemeConfig.applyTo(BLACKBERRY, chart);
        for (String colorScheme : getDefaultColorSchemes()) {
            financialColorSchemeConfig.applyTo(colorScheme, chart);
        }
    }

    private static class EmptyFinancialRenderer extends AbstractFinancialRenderer<EmptyFinancialRenderer> implements RendererPaintAfterEPAware {
        protected List<RendererPaintAfterEP> paintAfterEPS = new ArrayList<>();

        @Override
        public Canvas drawLegendSymbol(DataSet dataSet, int dsIndex, int width, int height) {
            // not used for test
            return null;
        }

        @Override
        public List<DataSet> render(GraphicsContext gc, Chart chart, int dataSetOffset, ObservableList<DataSet> datasets) {
            // not used for test
            return null;
        }

        @Override
        protected EmptyFinancialRenderer getThis() {
            return this;
        }

        @Override
        public void addPaintAfterEp(RendererPaintAfterEP paintAfterEP) {
            paintAfterEPS.add(paintAfterEP);
        }

        @Override
        public List<RendererPaintAfterEP> getPaintAfterEps() {
            return paintAfterEPS;
        }
    }
}
