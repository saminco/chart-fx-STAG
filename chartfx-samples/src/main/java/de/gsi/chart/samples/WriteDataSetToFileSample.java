package de.gsi.chart.samples;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.utils.PeriodicScreenCapture;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import de.gsi.dataset.utils.DataSetUtils;
import de.gsi.serializer.spi.BinarySerialiser;
import de.gsi.serializer.spi.FastByteBuffer;
import de.gsi.serializer.spi.iobuffer.DataSetSerialiser;

/**
 * @author rstein
 */
public class WriteDataSetToFileSample extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(WriteDataSetToFileSample.class);
    private static final int N_SAMPLES = 100;
    private static final String CSV_FILE_NAME_1 = "{dataSetName}.csv.zip";
    private static final String CSV_FILE_NAME_2 = "test2.dat.gz";
    private static final String CSV_FILE_NAME_SYSTEMTIME = "test_systemtime_{systemTime;date}_MagnetNr{magNr;int}.csv.gz";
    private static final String CSV_FILE_NAME_1_TIMESTAMED = "test1_{yMin;double}-{yMax;float;%.2e}_{acqTimeStamp;date}.csv.zip";
    private static final String CSV_FILE_NAME_2_TIMESTAMED = "test2_{yMin}-{yMax;float;%.2f}_{acqTimeStamp;int}.dat.gz";
    private static final String PNG_FILE_NAME = "test.png";
    private static final int DEFAULT_DELAY = 2;
    private static final int DEFAULT_PERIOD = 5;
    private static long now = System.currentTimeMillis();
    private static DoubleDataSet dataSet1;
    private static DoubleDataSet dataSet2;
    private static final FastByteBuffer fastByteBuffer = new FastByteBuffer();
    private static final DataSetSerialiser dataSetSerialiser = DataSetSerialiser.withIoSerialiser(new BinarySerialiser(fastByteBuffer));

    @Override
    public void start(final Stage primaryStage) {
        final String userHome = System.getProperty("user.home");

        final XYChart chart1 = new XYChart(new DefaultNumericAxis(), new DefaultNumericAxis());
        final XYChart chart2 = new XYChart();

        now = System.currentTimeMillis();
        dataSet1 = getDemoDataSet(now, true);
        dataSet2 = getDemoDataSet(now, false);
        dataSet2.getMetaInfo().put("magNr", Integer.toString(5));
        chart1.getDatasets().setAll(dataSet1, dataSet2); // two data sets

        final Scene scene = new Scene(chart1, 800, 600);
        primaryStage.setTitle(this.getClass().getSimpleName() + " - original");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(evt -> Platform.exit());

        final Stage secondaryStage = new Stage();

        secondaryStage.setTitle(this.getClass().getSimpleName() + " - recovered");
        secondaryStage.setScene(new Scene(chart2, 800, 600));
        secondaryStage.setOnCloseRequest(evt -> Platform.exit());
        primaryStage.show();
        secondaryStage.show();

        LOGGER.atInfo().log("userHome = " + userHome);
        final Path path = Paths.get(userHome + "/ChartSamples");

        final boolean addDateTimeToFileName = true;

        // write DataSet to File and recover
        DataSetUtils.writeDataSetToFile(dataSet1, path, CSV_FILE_NAME_1, false);
        DataSetUtils.writeDataSetToFile(dataSet2, path, CSV_FILE_NAME_2, true);
        DataSetUtils.writeDataSetToFile(dataSet2, path, CSV_FILE_NAME_SYSTEMTIME, false);

        // start periodic screen capture
        final PeriodicScreenCapture screenCapture = new PeriodicScreenCapture(path, PNG_FILE_NAME, scene, DEFAULT_DELAY,
                DEFAULT_PERIOD, addDateTimeToFileName);

        screenCapture.addListener(obs -> {
            final long userTimeStampMillis = System.currentTimeMillis();

            // add some important meta data to dataSet1 (e.g. acquisition time
            // stamp)
            dataSet1.getMetaInfo().put("acqTimeStamp", Long.toString(userTimeStampMillis));
            dataSet2.getMetaInfo().put("acqTimeStamp", Long.toString(userTimeStampMillis));

            final String actualFileName1 = DataSetUtils.writeDataSetToFile(dataSet1, path, CSV_FILE_NAME_1_TIMESTAMED,
                    false);
            final String actualFileName2 = DataSetUtils.writeDataSetToFile(dataSet2, path, CSV_FILE_NAME_2_TIMESTAMED,
                    true);

            // to suppress serialising the meta-data, default: true
            // DataSetSerialiser.setMetaDataSerialised(false); // uncomment
            // to suppress serialising data labels and styles, default: true
            // DataSetSerialiser.setDataLablesSerialised(false); // uncomment
            boolean asFloat = true;
            fastByteBuffer.reset(); // '0' writing at start of buffer
            dataSetSerialiser.write(dataSet2, asFloat);
            LOGGER.atInfo().log("written bytes to byte buffer = " + fastByteBuffer.position());
            fastByteBuffer.reset(); // return read position to '0'

            LOGGER.atInfo().log("write data time-stamped to directory = " + path);
            LOGGER.atInfo().log("actualFileName1 = " + actualFileName1);
            LOGGER.atInfo().log("actualFileName2 = " + actualFileName2);

            // recover written data sets
            final DataSet recoveredDataSet1 = DataSetUtils.readDataSetFromFile(actualFileName1);
            final DataSet recoveredDataSet2 = DataSetUtils.readDataSetFromFile(actualFileName2);
            final DataSet recoveredDataSet3 = dataSetSerialiser.read();

            chart2.getDatasets().clear();
            if (recoveredDataSet1 != null) {
                chart2.getDatasets().add(recoveredDataSet1);
            }

            if (recoveredDataSet2 != null) {
                chart2.getDatasets().add(recoveredDataSet2);
            }

            if (recoveredDataSet3 != null) {
                chart2.getDatasets().add(recoveredDataSet3);
            }

            // generate new data sets
            now = System.currentTimeMillis();
            dataSet1 = getDemoDataSet(now, true);
            dataSet2 = getDemoDataSet(now, false);
            chart1.getDatasets().setAll(dataSet1, dataSet2); // two data sets
        });

        screenCapture.start();

        // screenCapture.stop();
    }

    private static DoubleDataSet getDemoDataSet(final long timestamp, final boolean isSine) {
        final DoubleDataSet dataSet = new DoubleDataSet((isSine ? "sine" : "cosine") + "data set #1 @t=" + timestamp);

        final double[] xValues = new double[N_SAMPLES];
        final double[] yValues = new double[N_SAMPLES];
        for (int n = 0; n < N_SAMPLES; n++) {
            final double phase = Math.toRadians((10.0 * n) + (timestamp / 1000.0));
            xValues[n] = n;
            yValues[n] = isSine ? Math.sin(phase) : Math.cos(phase);
        }
        dataSet.set(xValues, yValues);

        return dataSet;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }
}
