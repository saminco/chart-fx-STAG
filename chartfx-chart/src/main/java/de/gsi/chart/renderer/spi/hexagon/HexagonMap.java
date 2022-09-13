package de.gsi.chart.renderer.spi.hexagon;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class HexagonMap {
    public final int hexagonSize;
    public int graphicsXpadding = 0;
    public int graphicsYpadding = 0;
    private MapGenerator mapGenerator;
    public boolean renderCoordinates = false;
    private final GridDrawer gridDrawer = new GridDrawer(this);
    private final Map<GridPosition, Hexagon> hexagons = new HashMap<>();
    // TODO consider JavaFX property model
    public List<HexagonCallback> onHexClickedCallback = new LinkedList<>();
    public List<HexagonCallback> onHexEnteredCallback = new LinkedList<>();
    public List<HexagonCallback> onHexExitCallback = new LinkedList<>();

    /**
     * Creates an empty HexagonMap
     *
     * @param hexagonSize the distance between the center and one corner
     */
    public HexagonMap(final int hexagonSize) {
        this.hexagonSize = hexagonSize;
    }

    /**
     * Generates a HexagonMap from an Image
     *
     * @param hexagonSize the distance between the center and one corner
     * @param image an Image which will be used to generate a HexagonMap
     * @param mapWidthInHexes the number of hexagons on the x-axis
     */
    public HexagonMap(final int hexagonSize, final Image image, final int mapWidthInHexes) {
        this.hexagonSize = hexagonSize;
        mapGenerator = new MapGenerator(this, image, mapWidthInHexes);
        mapGenerator.generate((q, r, imagePixelColor, map) -> {
            final Hexagon h = new Hexagon(q, r);
            h.setBackgroundColor(imagePixelColor);
            map.addHexagon(h);
        });
    }

    /**
     * Generates a HexagonMap from an Image
     *
     * @param hexagonSize the distance between the center and one corner
     * @param image an Image which will be used to generate a HexagonMap
     * @param mapWidthInHexes the number of hexagons on the x-axis
     * @param hexagonCreator a class implementing IHexagonCreator. This is how you decide HOW the HexagonMap should be
     *        generated from the Image. In it's most basic form:
     *        <p>
     *        public void createHexagon(int q, int r, Color imagePixelColor, HexagonMap map) { Hexagon h = new
     *        Hexagon(q, r); h.setBackgroundColor(imagePixelColor); map.addHexagon(h); }
     */
    public HexagonMap(final int hexagonSize, final Image image, final int mapWidthInHexes,
            final IHexagonCreator hexagonCreator) {
        this.hexagonSize = hexagonSize;
        mapGenerator = new MapGenerator(this, image, mapWidthInHexes);
        mapGenerator.generate(hexagonCreator);
    }

    /**
     * Add a Hexagon to the HexagonMap
     * 
     * @param hexagon new hexagon
     *
     * @return the same hexagon
     */
    public Hexagon addHexagon(final Hexagon hexagon) {
        hexagon.setMap(this);
        hexagons.put(hexagon.position, hexagon);
        return hexagon;
    }

    /**
     * @return all Hexagons that has been added to the map
     */
    public Collection<Hexagon> getAllHexagons() {
        return hexagons.values();
    }

    public int getGraphicsHexagonHeight() {
        return hexagonSize * 2;
    }

    private double getGraphicsHexagonWidth() {
        return Math.sqrt(3) / 2 * hexagonSize * 2;
    }

    public double getGraphicsHorizontalDistanceBetweenHexagons() {
        return getGraphicsHexagonWidth();
    }

    public double getGraphicsverticalDistanceBetweenHexagons() {
        return 3.0 / 4.0 * hexagonSize * 2.0;
    }

    public Hexagon getHexagon(final GridPosition position) {
        return getHexagon(position.q, position.r);
    }

    /**
     * Retrieves the Hexagon at the specified position (axial coordinates)
     *
     * @param q the Q coordinate
     * @param r the R coordinate
     * @return the Hexagon if there is no Hexagon at the specified position
     */
    public Hexagon getHexagon(final int q, final int r) {
        final GridPosition position = new GridPosition(q, r);
        final Hexagon result = hexagons.get(position);
        // if (result == null) {
        // throw new NoHexagonFoundException("There is no Hexagon on q:" + q + " r:" + r);
        // }
        return result;
    }

    public Hexagon getHexagonByCube(final int x, final int y, final int z) {
        return getHexagon(x, z);
    }

    /**
     * @param x carthesian x coordinate
     * @param y carthesian y coordinate
     * @return the hexagon that is rendered on a specific position on the screen if there is no Hexagon at the specified
     *         position
     */
    public Hexagon getHexagonContainingPixel(final int x, final int y) {
        return getHexagon(
                GridDrawer.pixelToPosition(x, y, getGraphicsHexagonHeight(), graphicsXpadding, graphicsYpadding));
    }

    /**
     * If the map was created from an Image, this will return the horizontal pixel relation between the image and the
     * generated map
     * 
     * @return scaling factor between pixel and grid
     */
    public Optional<Double> getImageMapHorizontalRelation() {
        return mapGenerator == null ? Optional.empty() : mapGenerator.getHorizontalRelation();
    }

    /**
     * If the map was created from an Image, this will return the vertical pixel relation between the image and the
     * generated map
     * 
     * @return scaling factor between pixel and grid
     */
    public Optional<Double> getImageMapVerticalRelation() {
        return mapGenerator == null ? Optional.empty() : mapGenerator.getVerticalRelation();
    }

    public double getPaddingX() {
        return graphicsXpadding;
    }

    public double getPaddingY() {
        return graphicsYpadding;
    }

    public void registerCanvasMouseLiner(final Canvas canvas) {
        canvas.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<>() {
            private Hexagon oldHexagon = null;

            @Override
            public void handle(final MouseEvent me) {
                final GraphicsContext gc = canvas.getGraphicsContext2D();
                final int x = (int) me.getX();
                final int y = (int) me.getY();

                final Hexagon hexagon = getHexagonContainingPixel(x, y);
                if (hexagon != null) {
                    if (oldHexagon != null) {
                        gc.save();
                        gc.setFill(oldHexagon.getFill());
                        gc.setStroke(oldHexagon.getStroke());
                        oldHexagon.drawHexagon(gc);
                        gc.restore();
                    }
                    oldHexagon = hexagon;
                } else {
                    if (oldHexagon != null) {
                        // System.err.println("left hexagon " + oldHexagon);
                        gc.save();
                        gc.setFill(oldHexagon.getFill());
                        gc.setStroke(oldHexagon.getStroke());
                        oldHexagon.drawHexagon(gc);
                        gc.restore();
                    }

                    oldHexagon = null;
                    return;
                }

                // System.err.println("found hexagon = " + hexagon);
                gc.save();
                gc.setFill(Color.YELLOW.darker());
                gc.setStroke(hexagon.getStroke());
                hexagon.drawHexagon(gc);
                gc.restore();

                // GridPosition pos = ((Hexagon) me.getSource()).position;
                // for (HexagonCallback callBack : map.onHexExitCallback) {
                // if (callBack == null) {
                // continue;
                // }
                // try {
                // callBack.handle(map.getHexagon(pos));
                // } catch (NoHexagonFoundException e) {
                // }
                // }
            }
        });
    }

    /**
     * Removes a Hexagon from the HexagonMap
     * 
     * @param hexagon to be removed
     */
    public void removeHexagon(final Hexagon hexagon) {
        hexagon.setMap(null);
        hexagons.remove(hexagon.position);
    }

    /**
     * Renders the HexagonMap
     *
     * @param canvas the JaxaFX Group where all the hexagons should be rendered
     */
    public void render(final Canvas canvas) {
        gridDrawer.draw(canvas);
    }

    /**
     * Renders the HexagonMap
     *
     * @param group the JaxaFX Group where all the hexagons should be rendered
     */
    public void render(final Group group) {
        gridDrawer.draw(group);
    }

    /**
     * Renders the contours of the HexagonMap
     *
     * @param canvas the JaxaFX Group where all the hexagons should be rendered
     */
    public void renderContour(final Canvas canvas) {
        gridDrawer.drawContour(canvas);
    }

    /**
     * A callback when the user clicks on a Hexagon
     * 
     * @param callback call-back function handler
     */
    public void setOnHexagonClickedCallback(final HexagonCallback callback) {
        onHexClickedCallback.add(callback);
    }

    /**
     * A callback when the user moves into a Hexagon
     * 
     * @param callback call-back function handler
     */
    public void setOnHexagonEnteredCallback(final HexagonCallback callback) {
        onHexEnteredCallback.add(callback);
    }

    /**
     * A callback when the user moves out of a Hexagon
     * 
     * @param callback call-back function handler
     */
    public void setOnHexagonExitCallback(final HexagonCallback callback) {
        onHexExitCallback.add(callback);
    }

    /**
     * Tells the renderer that you want some space before the HexagonMap is rendered
     * 
     * @param left padding margin left
     * @param top padding margin right
     */
    public void setPadding(final int left, final int top) {
        graphicsXpadding = left;
        graphicsYpadding = top;
        for (final Hexagon h : getAllHexagons()) {
            h.getPoints().removeAll(h.getPoints());
            h.init();
        }
    }

    /**
     * If you want the coordinates rendered on the screen
     * 
     * @param b true: render coordinates
     */
    public void setRenderCoordinates(final boolean b) {
        renderCoordinates = b;
    }

    /**
     * Sets the font used to draw the hexagon positions
     * 
     * @param font for rendering the hex positions
     */
    public void setRenderFont(final Font font) {
        gridDrawer.setFont(font);
    }

    static class DefaultPathInfoSupplier implements IPathInfoSupplier {
        @Override
        public int getMovementCost(final Hexagon from, final Hexagon to) {
            return 1;
        }

        @Override
        public boolean isBlockingPath(final Hexagon hexagon) {
            return hexagon == null || hexagon.isBlockingPath();
        }
    }

    public enum Direction {
        NORTHWEST,
        NORTHEAST,
        EAST,
        SOUTHEAST,
        SOUTHWEST,
        WEST
    }
}
