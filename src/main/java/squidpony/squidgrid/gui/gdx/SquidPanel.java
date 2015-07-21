package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import squidpony.squidgrid.Direction;
import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Displays text and images in a grid pattern. Supports basic animations.
 * 
 * Grid width and height settings are in terms of number of cells. Cell width and height
 * are in terms of number of pixels.
 *
 * When text is placed, the background color is set separately from the foreground character. When moved, only the
 * foreground character is moved.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SquidPanel extends Group {

    private static float DEFAULT_ANIMATION_DURATION = 0.2F;
    private static FreeTypeFontGenerator DEFAULT_FONT = DefaultResources.getDefaultFont();
    private int animationCount = 0;
    private Color defaultForeground = Color.WHITE;
    private final int gridWidth, gridHeight, cellWidth, cellHeight;
    private String[][] contents;
    private int[][] colors;
    private final TextCellFactory textFactory;

    /**
     * Creates a bare-bones panel with all default values for text rendering.
     * 
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     */
    public SquidPanel(int gridWidth, int gridHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().defaultSquareFont());
    }

    /**
     * Creates a panel with the given grid and cell size. Uses a default square font.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param cellWidth the number of horizontal pixels in each cell
     * @param cellHeight the number of vertical pixels in each cell
     */
    public SquidPanel(int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().defaultSquareFont().width(cellWidth).height(cellHeight));
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     * 
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     */
    public SquidPanel(int gridWidth, int gridHeight, TextCellFactory factory) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        textFactory = factory;

        if (factory == null) {
            factory = new TextCellFactory();
        }

        if (!factory.initialized()) {
            factory.initByFont();
        }

        cellWidth = factory.width();
        cellHeight = factory.height();

        contents = new String[gridWidth][gridHeight];
        colors = new int[gridWidth][gridHeight];

        int w = gridWidth * cellWidth;
        int h = gridHeight * cellHeight;
        setSize(w, h);
    }

    /**
     * Places the given characters into the grid starting at 0,0.
     *
     * @param chars
     */
    public void put(char[][] chars) {
        SquidPanel.this.put(0, 0, chars);
    }

    public void put(char[][] chars, Color[][] foregrounds) {
        SquidPanel.this.put(0, 0, chars, foregrounds);
    }

    public void put(char[][] chars, int[][] indices, ArrayList<Color> palette) {
        SquidPanel.this.put(0, 0, chars, indices, palette);
    }

    public void put(int xOffset, int yOffset, char[][] chars) {
        SquidPanel.this.put(xOffset, yOffset, chars, defaultForeground);
    }

    public void put(int xOffset, int yOffset, char[][] chars, Color[][] foregrounds) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    SquidPanel.this.put(x, y, chars[x - xOffset][y - yOffset], foregrounds[x - xOffset][y - yOffset]);
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, int[][] indices, ArrayList<Color> palette) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    SquidPanel.this.put(x, y, chars[x - xOffset][y - yOffset], palette.get(indices[x - xOffset][y - yOffset]));
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, Color[][] foregrounds) {
        for (int x = xOffset; x < xOffset + foregrounds.length; x++) {
            for (int y = yOffset; y < yOffset + foregrounds[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    SquidPanel.this.put(x, y, '\0', foregrounds[x - xOffset][y - yOffset]);
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, int[][] indices, ArrayList<Color> palette) {
        for (int x = xOffset; x < xOffset + indices.length; x++) {
            for (int y = yOffset; y < yOffset + indices[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    SquidPanel.this.put(x, y, '\0', palette.get(indices[x - xOffset][y - yOffset]));
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, Color foreground) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    SquidPanel.this.put(x, y, chars[x - xOffset][y - yOffset], foreground);
                }
            }
        }
    }

    /**
     * Puts the given string horizontally with the first character at the given offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * Will use the default color for this component to draw the characters.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     */
    public void put(int xOffset, int yOffset, String string) {
        SquidPanel.this.put(xOffset, yOffset, string, defaultForeground);
    }

    /**
     * Puts the given string horizontally with the first character at the given offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     * @param foreground the color to draw the characters
     */
    public void put(int xOffset, int yOffset, String string, Color foreground) {
        char[][] temp = new char[string.length()][1];
        for (int i = 0; i < string.length(); i++) {
            temp[i][0] = string.charAt(i);
        }
        SquidPanel.this.put(xOffset, yOffset, temp, foreground);
    }

    /**
     * Puts the given string horizontally with the first character at the given offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * Will use the default color for this component to draw the characters.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     * @param vertical true if the text should be written vertically, from top to bottom
     */
    public void placeVerticalString(int xOffset, int yOffset, String string, boolean vertical) {
        SquidPanel.this.put(xOffset, yOffset, string, defaultForeground, vertical);
    }

    /**
     * Puts the given string horizontally with the first character at the given offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     * @param foreground the color to draw the characters
     * @param vertical true if the text should be written vertically, from top to bottom
     */
    public void put(int xOffset, int yOffset, String string, Color foreground, boolean vertical) {
        if (vertical) {
            SquidPanel.this.put(xOffset, yOffset, new char[][]{string.toCharArray()}, foreground);
        } else {
            SquidPanel.this.put(xOffset, yOffset, string, foreground);
        }
    }

    /**
     * Erases the entire panel, leaving only a transparent space.
     */
    public void erase() {
        for (int i = 0; i < contents.length; i++) {
            for (int j = 0; j < contents[i].length; j++) {
                contents[i][j] = "";
                colors[i][j] = (255 << 24);
            }

        }
    }

    /**
     * Removes the contents of this cell, leaving a transparent space.
     *
     * @param x
     * @param y
     */
    public void clear(int x, int y) {
        this.put(x, y, Color.CLEAR);
    }

    public void put(int x, int y, Color color) {
        put(x, y, '\0', color);
    }

    public void put(int x, int y, char c) {
        put(x, y, c, defaultForeground);
    }

    /**
     * Takes a unicode codepoint for input.
     *
     * @param x
     * @param y
     * @param code
     */
    public void put(int x, int y, int code) {
        put(x, y, code, defaultForeground);
    }

    public void put(int x, int y, int c, Color color) {
        put(x, y, String.valueOf(Character.toChars(c)), color);
    }

    public void put(int x, int y, int index, ArrayList<Color> palette) {
        put(x, y, palette.get(index));
    }

    public void put(int x, int y, char c, int index, ArrayList<Color> palette) {
        put(x, y, c, palette.get(index));
    }

    /**
     * Takes a unicode codepoint for input.
     *
     * @param x
     * @param y
     * @param c
     * @param color
     */
    public void put(int x, int y, char c, Color color) {
        if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
            return;//skip if out of bounds
        }
        contents[x][y] = String.valueOf(c);
        colors[x][y] = Color.rgba8888(color);
    }

    public int cellWidth() {
        return cellWidth;
    }

    public int cellHeight() {
        return cellHeight;
    }

    public int gridHeight() {
        return gridHeight;
    }

    public int gridWidth() {
        return gridWidth;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        Color tmp = new Color();
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                Color.rgba8888ToColor(tmp, colors[x][y]);
                textFactory.draw(batch, contents[x][y], tmp, x * cellWidth, (gridHeight - y) * cellHeight);
            }
        }
    }

    public void setDefaultForeground(Color defaultForeground) {
        this.defaultForeground = defaultForeground;
    }

    public Actor cellToActor(int x, int y)
    {
        if(contents[x][y] == null || contents[x][y] == "")
            return null;

        Actor a = textFactory.makeActor(contents[x][y], new Color(colors[x][y]), x, y);
        a.setName(contents[x][y]);
        super.addActor(a);
        contents[x][y] = "";
        return a;
    }
    public void recallActor(Actor a)
    {
        int ax = Math.round(a.getX(Align.bottomRight) / cellWidth),
             ay = Math.round(a.getY(Align.bottomRight) / cellHeight);
        if(ax >= 0 && ax < gridWidth && ay > 0 && ay < gridHeight)
        {
            contents[ax][ay] = a.getName();
            colors[ax][ay] = Color.rgba8888(a.getColor());
        }
        a.clear();
        super.removeActor(a);
        animationCount--;
    }

    /**
     * Start a bumping animation in the given direction that will last duration seconds.
     * @param x
     * @param y
     * @param direction
     * @param duration
     */
    public void bump(int x, int y, Direction direction, float duration)
    {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        animationCount++;
        Actions.addAction(Actions.sequence(
                        Actions.moveToAligned(x + direction.deltaX / 3F, y + direction.deltaY / 3F,
                                Align.center, duration * 0.35F),
                        Actions.moveToAligned(x, y, Align.center, duration * 0.65F),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                recallActor(a);
                            }
                        })),
                a);
    }

    /**
     * Starts a bumping animation in the direction provided.
     *
     * @param location
     * @param direction
     */
    public void bump(Point location, Direction direction) {
        bump(location.x, location.y, direction, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Start a movement animation for the object at the grid location x, y and moves it to newX, newY over a number of
     * seconds given by duration (often 0.2f or somewhere around there).
     * @param x
     * @param y
     * @param newX
     * @param newY
     * @param duration
     */
    public void slide(int x, int y, int newX, int newY, float duration)
    {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        animationCount++;
        Actions.addAction(Actions.sequence(
                        Actions.moveToAligned(newX, newY, Align.center, duration),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                recallActor(a);
                            }
                        })),
                a);
    }
    /**
     * Starts a movement animation for the object at the given grid location at the default speed.
     *
     * @param start
     * @param end
     */
    public void slide(Point start, Point end) {
        slide(start.x, start.y, end.x, end.y, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts a movement animation for the object at the given grid location at the default speed for one grid square in
     * the direction provided.
     *
     * @param start
     * @param direction
     */
    public void slide(Point start, Direction direction) {
        slide(start.x, start.y, start.x + direction.deltaX, start.y + direction.deltaY, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts a sliding movement animation for the object at the given location at the provided speed. The duration is
     * how many seconds should pass for the entire animation.
     *
     * @param start
     * @param end
     * @param duration
     */
    public void slide(Point start, Point end, float duration) {
        slide(start.x, start.y, end.x, end.y, duration);
    }

    /**
     * Starts an wiggling animation for the object at the given location for the given duration in seconds.
     *
     * @param x
     * @param y
     * @param duration
     */
    public void wiggle(int x, int y, float duration) {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        animationCount++;
        Actions.addAction(Actions.sequence(
                        Actions.moveToAligned(x + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                y + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                Align.center, duration * 0.1F),
                        Actions.moveToAligned(x + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                y + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                Align.center, duration * 0.1F),
                        Actions.moveToAligned(x + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                y + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                Align.center, duration * 0.1F),
                        Actions.moveToAligned(x + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                y + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                Align.center, duration * 0.1F),
                        Actions.moveToAligned(x + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                y + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                Align.center, duration * 0.1F),
                        Actions.moveToAligned(x + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                y + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                Align.center, duration * 0.1F),
                        Actions.moveToAligned(x + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                y + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                Align.center, duration * 0.1F),
                        Actions.moveToAligned(x + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                y + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                Align.center, duration * 0.1F),
                        Actions.moveToAligned(x + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                y + DefaultResources.guiRandom.nextFloat() - 0.5F,
                                Align.center, duration * 0.1F),
                        Actions.moveToAligned(x, y, Align.center, duration * 0.1F),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                recallActor(a);
                            }
                        })),
                a);
    }

    /**
     * Returns true if there are animations running when this method is called.
     *
     * @return
     */
    public boolean hasActiveAnimations() {
        return animationCount != 0;
    }

}
