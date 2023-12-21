package io.fair_acc.sample.chart.legacy;

import javafx.application.Application;
import javafx.scene.layout.BorderPane;

import com.sun.javafx.tk.Toolkit;

import io.fair_acc.sample.chart.RollingBufferSample;

/**
 * chart-fx stress test for updates at 100 Hz to 1 kHz
 *
 * @author rstein
 */
public class ChartHighUpdateRateSample extends RollingBufferSample {
    static {
        N_SAMPLES = 30000;
        UPDATE_PERIOD = 1;
        BUFFER_CAPACITY = 7500;
    }
    private static int counter = 0;

    @Override
    public BorderPane initComponents() {
        BorderPane pane = super.initComponents();
        Toolkit.getToolkit().addSceneTkPulseListener(() -> {
            counter = (counter + 1) % 100;
            if (counter == 0) {
                System.err.println("pulse auto dipole, "
                                   + " auto beam ");
            }
        });

        Toolkit.getToolkit().checkFxUserThread();
        return pane;
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }
}
