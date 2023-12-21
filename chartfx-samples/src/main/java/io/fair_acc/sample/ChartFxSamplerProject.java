package io.fair_acc.sample;

import java.io.InputStream;
import java.util.Objects;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.scenicview.ScenicView;

import io.fair_acc.chartfx.Chart;

import fr.brouillard.oss.cssfx.CSSFX;
import fxsampler.FXSamplerProject;
import fxsampler.model.WelcomePage;

public class ChartFxSamplerProject implements FXSamplerProject {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectName() {
        return "ChartFx";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSampleBasePackage() {
        return "io.fair_acc.sample.chart";
    }

    ///** {@inheritDoc} */
    //@Override
    // public String getModuleName() {
    //    return "io.fair-acc";
    //}

    /**
     * {@inheritDoc}
     */
    @Override
    public WelcomePage getWelcomePage() {
        VBox vBox = new VBox();
        ImageView imgView = new ImageView();
        final InputStream bannerResource = ChartFxSamplerProject.class.getResourceAsStream("banner.png");
        if (bannerResource != null) {
            imgView.setImage(new Image(bannerResource));
        }
        // imgView.setStyle("-fx-image: url('banner.png');");
        StackPane pane = new StackPane();
        pane.setPrefHeight(207);
        // pane.setStyle("-fx-background-image: url('org/controlsfx/samples/bar.png');"
        //         + "-fx-background-repeat: repeat-x;");
        pane.getChildren().add(imgView);
        Label label = new Label();
        label.setWrapText(true);
        label.setText("Welcome to ChartFx samples!\nThis library provides a wide array of facilities for high performance scientific plotting.\n\n Explore the available chart controls by clicking on the options to the left.");
        label.setStyle("-fx-font-size: 1.5em; -fx-padding: 20 0 0 5;");
        vBox.setStyle("-fx-padding: 5px; -fx-spacing: 5px");

        // set window icon
        vBox.sceneProperty().addListener(((obs, o, n) -> {
            if (n != null) {
                Window window = n.getWindow();
                final InputStream iconResource = ChartFxSamplerProject.class.getResourceAsStream("icon.png");
                if (window instanceof Stage && iconResource != null) {
                    Stage stage = (Stage) window;
                    stage.getIcons().add(new Image(iconResource));
                }
            }
        }));

        var scenicView = new Button("Show ScenicView");
        scenicView.setOnAction(a -> ScenicView.show(scenicView.getScene()));

        var addDefaultCss = new Button("Add chart.css");
        addDefaultCss.setOnAction(a -> {
            // agent stylesheets don't get reloaded, so we need to manually add the css file
            // to make CSSFX work with "mvn -pl chartfx-chart sass-cli:watch"
            addDefaultCss.getScene().getStylesheets().add(Objects.requireNonNull(Chart.class.getResource("chart.css")).toExternalForm());
            addDefaultCss.setDisable(true);
        });
        addDefaultCss.managedProperty().bind(addDefaultCss.visibleProperty());
        addDefaultCss.setVisible(false);

        var cssFx = new Button("Start CSSFX");
        cssFx.setOnAction(a -> {
            cssFx.getScene().getStylesheets().add(Objects.requireNonNull(Chart.class.getResource("chart.css")).toExternalForm());
            CSSFX.start(cssFx.getScene());
            cssFx.setDisable(true);
            addDefaultCss.setVisible(true);
        });

        vBox.getChildren().addAll(pane, label, scenicView, cssFx, addDefaultCss);
        return new WelcomePage("Welcome to ChartFx!", vBox);
    }
}
