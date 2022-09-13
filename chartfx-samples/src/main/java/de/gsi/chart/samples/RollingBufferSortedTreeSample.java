package de.gsi.chart.samples;

import java.time.ZoneOffset;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.axes.spi.format.DefaultTimeFormatter;
import de.gsi.chart.plugins.EditAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.chart.utils.SimplePerformanceMeter;
import de.gsi.dataset.spi.LimitedIndexedTreeDataSet;
import de.gsi.dataset.utils.ProcessingProfiler;

/**
 * @author rstein
 */
public class RollingBufferSortedTreeSample extends Application {
    private static final String MONOSPACED = "Monospaced";
    private static final Logger LOGGER = LoggerFactory.getLogger(RollingBufferSortedTreeSample.class);
    public final LimitedIndexedTreeDataSet rollingBufferDipoleCurrent = new LimitedIndexedTreeDataSet(
            "dipole current [A]", RollingBufferSample.BUFFER_CAPACITY);
    public final LimitedIndexedTreeDataSet rollingBufferBeamIntensity = new LimitedIndexedTreeDataSet(
            "beam intensity [ppp]", RollingBufferSample.BUFFER_CAPACITY);
    private final ErrorDataSetRenderer beamIntensityRenderer = new ErrorDataSetRenderer();
    private final ErrorDataSetRenderer dipoleCurrentRenderer = new ErrorDataSetRenderer();
    private Timer timer;

    private void generateData() {
        final long startTime = ProcessingProfiler.getTimeStamp();
        final double now = System.currentTimeMillis() / 1000.0 + 1; // N.B. '+1'
                // to check
                // for
                // resolution

        if (rollingBufferDipoleCurrent.getDataCount() == 0) {
            rollingBufferBeamIntensity.autoNotification().set(false);
            rollingBufferDipoleCurrent.autoNotification().set(false);
            for (int n = RollingBufferSample.N_SAMPLES; n > 0; n--) {
                final double t = now - n * RollingBufferSample.UPDATE_PERIOD / 1000.0;
                final double y = 25 * RollingBufferSample.rampFunctionDipoleCurrent(t);
                final double y2 = 100 * RollingBufferSample.rampFunctionBeamIntensity(t);
                final double ey = 1;
                rollingBufferDipoleCurrent.add(t, y, ey, ey);
                rollingBufferBeamIntensity.add(t, y2, ey, ey);
            }
            rollingBufferBeamIntensity.autoNotification().set(true);
            rollingBufferDipoleCurrent.autoNotification().set(true);
        } else {
            rollingBufferDipoleCurrent.autoNotification().set(false);
            final double t = now;
            final double y = 25 * RollingBufferSample.rampFunctionDipoleCurrent(t);
            final double y2 = 100 * RollingBufferSample.rampFunctionBeamIntensity(t);
            final double ey = 1;
            rollingBufferDipoleCurrent.add(t, y, ey, ey);
            rollingBufferBeamIntensity.add(t, y2, ey, ey);
            rollingBufferDipoleCurrent.autoNotification().set(true);
        }
        ProcessingProfiler.getTimeDiff(startTime, "adding data into DataSet");
    }

    private HBox getHeaderBar(Scene scene) {
        final Button newDataSet = new Button("new DataSet");
        newDataSet.setOnAction(evt -> Platform.runLater(getTask()));

        final Button startTimer = new Button("timer");
        startTimer.setOnAction(evt -> {
            if (timer == null) {
                timer = new Timer("sample-update-timer", true);
                rollingBufferBeamIntensity.reset();
                rollingBufferDipoleCurrent.reset();
                timer.scheduleAtFixedRate(getTask(), 0, RollingBufferSample.UPDATE_PERIOD);
            } else {
                timer.cancel();
                timer = null;
            }
        });

        // H-Spacer
        Region spacer = new Region();
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // JavaFX and Chart Performance metrics
        SimplePerformanceMeter meter = new SimplePerformanceMeter(scene, RollingBufferSample.DEBUG_UPDATE_RATE);

        Label fxFPS = new Label();
        fxFPS.setFont(Font.font(MONOSPACED, 12));
        Label chartFPS = new Label();
        chartFPS.setFont(Font.font(MONOSPACED, 12));
        Label cpuLoadProcess = new Label();
        cpuLoadProcess.setFont(Font.font(MONOSPACED, 12));
        Label cpuLoadSystem = new Label();
        cpuLoadSystem.setFont(Font.font(MONOSPACED, 12));
        meter.fxFrameRateProperty().addListener((ch, o, n) -> {
            final String fxRate = String.format("%4.1f", meter.getFxFrameRate());
            final String actualRate = String.format("%4.1f", meter.getActualFrameRate());
            final String cpuProcess = String.format("%5.1f", meter.getProcessCpuLoad());
            final String cpuSystem = String.format("%5.1f", meter.getSystemCpuLoad());
            fxFPS.setText(String.format("%-6s: %4s %s", "JavaFX", fxRate, "FPS, "));
            chartFPS.setText(String.format("%-6s: %4s %s", "Actual", actualRate, "FPS, "));
            cpuLoadProcess.setText(String.format("%-11s: %4s %s", "Process-CPU", cpuProcess, "%"));
            cpuLoadSystem.setText(String.format("%-11s: %4s %s", "System -CPU", cpuSystem, "%"));
        });

        return new HBox(newDataSet, startTimer, spacer, new VBox(fxFPS, chartFPS),
                new VBox(cpuLoadProcess, cpuLoadSystem));
    }

    private TimerTask getTask() {
        return new TimerTask() {
            private int updateCount;

            @Override
            public void run() {
                Platform.runLater(() -> {
                    generateData();

                    if (updateCount % 20 == 0) {
                        LOGGER.atInfo().log("update iteration #" + updateCount);
                    }
                    updateCount++;
                });
            }
        };
    }

    public BorderPane initComponents(Scene scene) {
        final BorderPane root = new BorderPane();
        generateData();
        initErrorDataSetRenderer(beamIntensityRenderer);
        initErrorDataSetRenderer(dipoleCurrentRenderer);

        final DefaultNumericAxis xAxis1 = new DefaultNumericAxis("time");
        xAxis1.setAutoRangeRounding(false);
        xAxis1.setTickLabelRotation(45);
        xAxis1.invertAxis(false);
        xAxis1.setTimeAxis(true);
        final DefaultNumericAxis yAxis1 = new DefaultNumericAxis("beam intensity", "ppp");
        final DefaultNumericAxis yAxis2 = new DefaultNumericAxis("dipole current", "A");
        yAxis2.setSide(Side.RIGHT);
        yAxis2.setAnimated(false);
        // N.B. it's important to set secondary axis on the 2nd renderer before
        // adding the renderer to the chart
        dipoleCurrentRenderer.getAxes().add(yAxis2);

        final XYChart chart = new XYChart(xAxis1, yAxis1);
        chart.legendVisibleProperty().set(true);
        chart.setAnimated(false);
        chart.getYAxis().setName(rollingBufferBeamIntensity.getName());
        chart.getRenderers().set(0, beamIntensityRenderer);
        chart.getRenderers().add(dipoleCurrentRenderer);
        chart.getPlugins().add(new EditAxis());

        beamIntensityRenderer.getDatasets().add(rollingBufferBeamIntensity);
        dipoleCurrentRenderer.getDatasets().add(rollingBufferDipoleCurrent);

        // set localised time offset
        if (xAxis1.isTimeAxis() && xAxis1.getAxisLabelFormatter() instanceof DefaultTimeFormatter) {
            final DefaultTimeFormatter axisFormatter = (DefaultTimeFormatter) xAxis1.getAxisLabelFormatter();

            axisFormatter.setTimeZoneOffset(ZoneOffset.UTC);
            axisFormatter.setTimeZoneOffset(ZoneOffset.ofHoursMinutes(5, 0));
        }

        yAxis1.setForceZeroInRange(true);
        yAxis2.setForceZeroInRange(true);
        yAxis1.setAutoRangeRounding(true);
        yAxis2.setAutoRangeRounding(true);

        root.setTop(getHeaderBar(scene));

        long startTime = ProcessingProfiler.getTimeStamp();
        ProcessingProfiler.getTimeDiff(startTime, "adding data to chart");

        startTime = ProcessingProfiler.getTimeStamp();
        root.setCenter(chart);

        ProcessingProfiler.getTimeDiff(startTime, "adding chart into StackPane");

        return root;
    }

    protected void initErrorDataSetRenderer(final ErrorDataSetRenderer eRenderer) {
        eRenderer.setErrorType(ErrorStyle.ERRORSURFACE);
        eRenderer.setDashSize(RollingBufferSample.MIN_PIXEL_DISTANCE); // plot
                // pixel-to-pixel
                // distance
        eRenderer.setDrawMarker(false);
        final DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) eRenderer.getRendererDataReducer();
        reductionAlgorithm.setMinPointPixelDistance(RollingBufferSample.MIN_PIXEL_DISTANCE);
    }

    @Override
    public void start(final Stage primaryStage) {
        ProcessingProfiler.setVerboseOutputState(true);
        ProcessingProfiler.setLoggerOutputState(true);
        ProcessingProfiler.setDebugState(false);

        final BorderPane root = new BorderPane();
        final Scene scene = new Scene(root, 1800, 400);
        root.setCenter(initComponents(scene));

        final long startTime = ProcessingProfiler.getTimeStamp();
        primaryStage.setTitle(this.getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(evt -> Platform.exit());
        primaryStage.show();
        ProcessingProfiler.getTimeDiff(startTime, "for showing");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }
}