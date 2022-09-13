package de.gsi.chart.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import de.gsi.chart.ui.geometry.Side;

/**
 * SidesPane inspired by the HiddenSidesPane of the controlsFX package
 * <p>
 * A pane used to display a full-size content node and four initially hidden nodes on the four sides. The hidden nodes
 * can be made visible by moving the mouse cursor to the edges (see {@link #setTriggerDistance(double)}) of the pane.
 * The hidden node will appear (at its preferred width or height) with a short slide-in animation. The node will
 * disappear again as soon as the mouse cursor exits it. A hidden node / side can also be pinned by calling
 * {@link #setPinned(Side,Boolean)}. It will remain visible as long as it stays pinned.
 * <h2>Screenshot</h2> The following screenshots shows the right side node hovering over a table after it was made
 * visible: <img src="hiddenSidesPane.png" alt="Screenshot of HiddenSidesPane">
 * <h2>Code Sample</h2>
 *
 * <pre>
 * SidesPane pane = new SidesPane();
 * pane.setContent(new TableView());
 * pane.setTop(new ToolBarView());
 * </pre>
 */
public class SidesPane extends Control {
    private final DoubleProperty prefHeightTop = new SimpleDoubleProperty(this, "prefHeightTop", -1);
    private final DoubleProperty prefHeightBottom = new SimpleDoubleProperty(this, "prefHeightBottom", -1);
    private final DoubleProperty prefWidthLeft = new SimpleDoubleProperty(this, "prefWidthLeft", -1);
    private final DoubleProperty prefWidthRight = new SimpleDoubleProperty(this, "prefWidthRight", -1);
    private final DoubleProperty triggerDistance = new SimpleDoubleProperty(this, "triggerDistance", 16);
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content"); //$NON-NLS-1$
    private final ObjectProperty<Node> top = new SimpleObjectProperty<>(this, "top") {
        @Override
        public void set(Node node) {
            if (node instanceof Region) {
                ((Region) node).setPadding(Insets.EMPTY);
                ((Region) node).setPrefHeight(-1);
                ((Region) node).setMinHeight(0);
                ((Region) node).setPrefWidth(-1);
                ((Region) node).setMinWidth(0);
            }
            super.set(node);
        }
    };

    /**
     * The property used to store a reference to the node shown at the right side of the pane.
     */
    private final ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right") {
        @Override
        public void set(Node node) {
            if (node instanceof Region) {
                ((Region) node).setPadding(Insets.EMPTY);
                ((Region) node).setPrefHeight(-1);
                ((Region) node).setMinHeight(0);
                ((Region) node).setPrefWidth(-1);
                ((Region) node).setMinWidth(0);
            }
            super.set(node);
        }
    };

    /**
     * The property used to store a reference to the node shown at the bottom side of the pane.
     */
    private final ObjectProperty<Node> bottom = new SimpleObjectProperty<>(this, "bottom") {
        @Override
        public void set(Node node) {
            if (node instanceof Region) {
                ((Region) node).setPadding(Insets.EMPTY);
                ((Region) node).setPrefHeight(-1);
                ((Region) node).setMinHeight(0);
                ((Region) node).setPrefWidth(-1);
                ((Region) node).setMinWidth(0);
            }
            super.set(node);
        }
    };

    /**
     * The property used to store a reference to the node shown at the left side of the pane.
     */
    private final ObjectProperty<Node> left = new SimpleObjectProperty<>(this, "left") {
        @Override
        public void set(Node node) {
            if (node instanceof Region) {
                ((Region) node).setPadding(Insets.EMPTY);
                ((Region) node).setPrefHeight(-1);
                ((Region) node).setMinHeight(0);
                ((Region) node).setPrefWidth(-1);
                ((Region) node).setMinWidth(0);
            }
            super.set(node);
        }
    };

    // Pinned side support.
    private final ObservableMap<Side, Boolean> pinnedSide = FXCollections.observableHashMap();
    private final ObjectProperty<Duration> animationDelay = new SimpleObjectProperty<>(this, "animationDelay",
            Duration.millis(500));
    private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty<>(this, "animationDuration", //$NON-NLS-1$
            Duration.millis(200));

    /**
     * Constructs a new pane with no content and no side nodes.
     */
    public SidesPane() {
        this(null, null, null, null, null);
    }

    /**
     * Constructs a new pane with the given content node and the four side nodes. Each one of the side nodes may be
     * null.
     *
     * @param content the primary node that will fill the entire width and height of the pane
     * @param top the hidden node on the top side
     * @param right the hidden node on the right side
     * @param bottom the hidden node on the bottom side
     * @param left the hidden node on the left side
     */
    public SidesPane(final Node content, final Node top, final Node right, final Node bottom, final Node left) {
        super();
        setContent(content);
        setTop(top);
        setRight(right);
        setBottom(bottom);
        setLeft(left);
        // add listener here to set preferred width/height on new data sets
        // being set
        // later the setPrefWidth/Height is being used to increase/decrease the
        // actual size of the side panes
        topProperty().addListener((obs, o, n) -> {
            if (n != null) {
                final double val = n.prefHeight(-1);
                prefHeightTop.set(val > 0 ? val : -1);
            }
        });
        bottomProperty().addListener((obs, o, n) -> {
            if (n != null) {
                final double val = n.prefHeight(-1);
                prefHeightBottom.set(val > 0 ? val : -1);
            }
        });
        leftProperty().addListener((obs, o, n) -> {
            if (n != null) {
                final double val = n.prefWidth(-1);
                prefWidthLeft.set(val > 0 ? val : -1);
            }
        });
        rightProperty().addListener((obs, o, n) -> {
            if (n != null) {
                final double val = n.prefWidth(-1);
                prefWidthRight.set(val > 0 ? val : -1);
            }
        });
    }

    // Content node support.

    /**
     * Returns the animation delay property. The value of this property determines the delay before the hidden side
     * slide in / slide out animation starts to play.
     *
     * @return animation delay property
     */
    public final ObjectProperty<Duration> animationDelayProperty() {
        return animationDelay;
    }

    /**
     * Returns the animation duration property. The value of this property determines the fade in time for a hidden side
     * to become visible.
     *
     * @return animation delay property
     */
    public final ObjectProperty<Duration> animationDurationProperty() {
        return animationDuration;
    }

    /**
     * Returns the value of the bottom node property.
     *
     * @return the bottom node property value
     */
    public final ObjectProperty<Node> bottomProperty() {
        return bottom;
    }

    /**
     * The property that is used to store a reference to the content node. The content node will fill the entire width
     * and height of the pane.
     *
     * @return the content node property
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    // Top node support.

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SidesPaneSkin(this);
    }

    /**
     * Returns the animation delay
     *
     * @return animation delay
     */
    public final Duration getAnimationDelay() {
        return animationDelay.get();
    }

    /**
     * Returns the animation delay
     *
     * @return animation delay
     */
    public final Duration getAnimationDuration() {
        return animationDuration.get();
    }

    /**
     * Returns the value of the bottom node property.
     *
     * @return the bottom node property value
     */
    public final Node getBottom() {
        return bottomProperty().get();
    }

    // Right node support.

    /**
     * Returns the value of the content node property.
     *
     * @return the content node property value
     */
    public final Node getContent() {
        return contentProperty().get();
    }

    /**
     * Returns the value of the left node property.
     *
     * @return the left node property value
     */
    public final Node getLeft() {
        return leftProperty().get();
    }

    /**
     * Returns the value of the right node property.
     *
     * @return the right node property value
     */
    public final Node getRight() {
        return rightProperty().get();
    }

    /**
     * Returns the value of the top node property.
     *
     * @return the top node property value
     */
    public final Node getTop() {
        return topProperty().get();
    }

    // Bottom node support.

    /**
     * Returns the value of the trigger distance property.
     *
     * @return the trigger distance property value
     */
    public final double getTriggerDistance() {
        return triggerDistance.get();
    }

    /**
     * Returns the value of the pinned side property.
     *
     * @param side the side to query
     * @return the pinned side property value
     */
    public final Boolean isPinned(final Side side) {
        final Boolean ret = pinnedSideProperty().get(side);
        if (ret == null) {
            return false;
        }
        return ret;
    }

    /**
     * Returns the value of the left node property.
     *
     * @return the left node property value
     */
    public final ObjectProperty<Node> leftProperty() {
        return left;
    }

    /**
     * Returns the pinned side property. The value of this property determines if one of the four hidden sides stays
     * visible all the time.
     *
     * @return the pinned side property
     */
    public final ObservableMap<Side, Boolean> pinnedSideProperty() {
        return pinnedSide;
    }

    // Left node support.

    public DoubleProperty prefHeightBottomProperty() {
        return prefHeightBottom;
    }

    public DoubleProperty prefHeightTopProperty() {
        return prefHeightTop;
    }

    public DoubleProperty prefWidthLeftProperty() {
        return prefWidthLeft;
    }

    public DoubleProperty prefWidthRightProperty() {
        return prefWidthRight;
    }

    /**
     * Returns the value of the right node property.
     *
     * @return the right node property value
     */
    public final ObjectProperty<Node> rightProperty() {
        return right;
    }

    /**
     * Set the animation delay
     *
     * @param duration slide in animation delay
     */
    public final void setAnimationDelay(final Duration duration) {
        animationDelay.set(duration);
    }

    /**
     * Set the animation delay
     *
     * @param duration animation duration
     */
    public final void setAnimationDuration(final Duration duration) {
        animationDuration.set(duration);
    }

    /**
     * Sets the value of the bottom node property.
     *
     * @param bottom the bottom node value
     */
    public final void setBottom(final Node bottom) {
        bottomProperty().set(bottom);
    }

    // slide in animation delay

    /**
     * Sets the value of the content node property.
     *
     * @param content the new content node
     */
    public final void setContent(final Node content) {
        contentProperty().set(content);
    }

    /**
     * Sets the value of the left node property.
     *
     * @param left the left node value
     */
    public final void setLeft(final Node left) {
        leftProperty().set(left);
    }

    /**
     * Sets the value of the pinned side property.
     *
     * @param side the new pinned side value
     * @param stateState pinned state
     */
    public final void setPinned(final Side side, final Boolean stateState) {
        if (pinnedSideProperty().get(side) == null) {
            pinnedSideProperty().put(side, stateState);
            return;
        }
        pinnedSideProperty().replace(side, stateState);
    }

    /**
     * Sets the value of the right node property.
     *
     * @param right the right node value
     */
    public final void setRight(final Node right) {
        rightProperty().set(right);
    }

    // slide in / slide out duration

    /**
     * Sets the value of the top node property.
     *
     * @param top the top node value
     */
    public final void setTop(final Node top) {
        topProperty().set(top);
    }

    /**
     * Set the value of the trigger distance property. <br>
     * Setting the property to zero or a negative value will disable this functionality, so a hidden side can only be
     * made visible with {@link #setPinned(Side,Boolean)}.
     *
     * @param distance the new value for the trigger distance property
     */
    public final void setTriggerDistance(final double distance) {
        triggerDistance.set(distance);
    }

    /**
     * The property used to store a reference to the node shown at the top side of the pane.
     *
     * @return the hidden node at the top side of the pane
     */
    public final ObjectProperty<Node> topProperty() {
        return top;
    }

    /**
     * The property that stores the distance to the pane's edges that will trigger the appearance of the hidden side
     * nodes.<br>
     * Setting the property to zero or a negative value will disable this functionality, so a hidden side can only be
     * made visible with {@link #setPinned(Side, Boolean)}.
     *
     * @return the trigger distance property
     */
    public final DoubleProperty triggerDistanceProperty() {
        return triggerDistance;
    }
}
