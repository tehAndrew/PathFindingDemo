import javafx.animation.AnimationTimer;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Cell;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class SimArea extends Canvas {
    private int gridSide;
    private int cellsWidth;
    private int cellsHeight;
    private CellType[][] cells;
    private Pair startPos;
    private Pair goalPos;
    private HashMap<String, SimAreaTool> toolTable;
    private HashMap<String, Heuristic> heuristicTable;
    private SimAreaTool selectedTool;
    private Heuristic selectedHeuristic;
    private AStar aStar;
    private AnimationTimer runTimer;

    private void initCells() {
        cells = new CellType[cellsWidth][cellsHeight];

        for (int x = 0; x < cellsWidth; x++) {
            for (int y = 0; y < cellsHeight; y++) {
                cells[x][y] = CellType.NORMAL;
            }
        }
    }

    private void draw() {
        GraphicsContext g2d = getGraphicsContext2D();

        // Clear sim area
        g2d.clearRect(0, 0, getWidth(), getHeight());

        // Draw border around simulation area.
        g2d.setStroke(Color.BLACK);
        g2d.strokeRect(0, 0, getWidth(), getHeight());

        // Draw cells
        for (int x = 0; x < cellsWidth; x++) {
            for (int y = 0; y < cellsHeight; y++) {
                switch (cells[x][y]) {
                    case IMPASSABLE:
                        g2d.setFill(Color.BLACK);
                        g2d.fillRect(x * gridSide, y * gridSide, gridSide, gridSide);
                        break;
                    case OBSTACLE:
                        g2d.setFill(Color.LIGHTBLUE);
                        g2d.fillRect(x * gridSide, y * gridSide, gridSide, gridSide);
                        break;
                    default:
                        break;
                }
            }
        }

        if (aStar != null)
            aStar.draw(g2d, gridSide);

        // Draw start and goal
        g2d.setFill(Color.BLACK);
        g2d.setFont(new Font(30));
        g2d.setTextAlign(TextAlignment.CENTER);
        g2d.setTextBaseline(VPos.CENTER);
        g2d.fillText("A", startPos.x * gridSide + gridSide * 0.5, startPos.y * gridSide + gridSide * 0.5);
        g2d.fillText("B", goalPos.x * gridSide + gridSide * 0.5, goalPos.y * gridSide + gridSide * 0.5);

        g2d.setStroke(Color.DARKGRAY);
        // Draw grid.
        for (int x = gridSide; x < getWidth(); x += gridSide) {
            g2d.strokeLine(x, 0, x, getHeight());
        }
        for (int y = gridSide; y < getHeight(); y += gridSide) {
            g2d.strokeLine(0, y, getWidth(), y);
        }
    }

    private void useTool(MouseEvent event) {
        int mouseX = (int) Math.floor(event.getSceneX() / gridSide);
        int mouseY = (int) Math.floor(event.getSceneY() / gridSide);

        mouseX = Math.min(Math.max(mouseX, 0), cellsWidth - 1);
        mouseY = Math.min(Math.max(mouseY, 0), cellsHeight - 1);

        selectedTool.useTool(mouseX, mouseY);
        draw();
    }

    public SimArea(int x, int y, int width, int height, int gridSide) {
        super(width, height);
        relocate(x, y);
        this.gridSide = gridSide;
        cellsWidth = (int) Math.floor(getWidth() / gridSide);
        cellsHeight = (int) Math.floor(getHeight() / gridSide);
        initCells();

        startPos = new Pair(0, 0);
        goalPos = new Pair(cellsWidth - 1, cellsHeight - 1);

        // Create tools
        toolTable = new HashMap<>();
        toolTable.put("Place Start", (int mouseX, int mouseY) -> {
            if (goalPos.x == mouseX && goalPos.y == mouseY)
                return;

            startPos = new Pair(mouseX, mouseY);
            cells[mouseX][mouseY] = CellType.NORMAL;
        });

        toolTable.put("Place End", (int mouseX, int mouseY) -> {
            if (startPos.x == mouseX && startPos.y == mouseY)
                return;

            goalPos = new Pair(mouseX, mouseY);
            cells[mouseX][mouseY] = CellType.NORMAL;
        });

        toolTable.put("Draw Normal Cell", (int mouseX, int mouseY) -> {
            if (startPos.x == mouseX && startPos.y == mouseY)
                return;

            if (goalPos.x == mouseX && goalPos.y == mouseY)
                return;

            cells[mouseX][mouseY] = CellType.NORMAL;
        });

        toolTable.put("Draw Slow Cell", (int mouseX, int mouseY) -> {
            if (startPos.x == mouseX && startPos.y == mouseY)
                return;

            if (goalPos.x == mouseX && goalPos.y == mouseY)
                return;

            cells[mouseX][mouseY] = CellType.OBSTACLE;
        });

        toolTable.put("Draw Solid Cell", (int mouseX, int mouseY) -> {
                if (startPos.x == mouseX && startPos.y == mouseY)
                    return;

                if (goalPos.x == mouseX && goalPos.y == mouseY)
                    return;

                cells[mouseX][mouseY] = CellType.IMPASSABLE;
        });

        selectedTool = toolTable.get("Draw Normal Cell");

        // Create Heuristics
        heuristicTable = new HashMap<>();
        heuristicTable.put("Dijkstra", (Pair fromPos, Pair endPos) -> {
            return 0.;
        });

        heuristicTable.put("Diagonal distance", (Pair fromPos, Pair endPos) -> {
            int dx = Math.abs(fromPos.x - endPos.x);
            int dy = Math.abs(fromPos.y - endPos.y);
            return AStar.NORMAL_WEIGHT * (dx + dy) +
                    (AStar.DIAGONAL_COST_FACTOR * AStar.NORMAL_WEIGHT - 2 * AStar.NORMAL_WEIGHT)
                    * Math.min(dx, dy);
        });

        heuristicTable.put("Euclidean", (Pair fromPos, Pair endPos) -> {
            int dx = Math.abs(fromPos.x - endPos.x);
            int dy = Math.abs(fromPos.y - endPos.y);
            return AStar.NORMAL_WEIGHT * Math.sqrt(dx * dx + dy * dy);
        });

        heuristicTable.put("Manhattan", (Pair fromPos, Pair endPos) -> {
            int dx = Math.abs(fromPos.x - endPos.x);
            int dy = Math.abs(fromPos.y - endPos.y);
            return AStar.NORMAL_WEIGHT * (dx + dy);
        });

        selectedHeuristic = heuristicTable.get("Diagonal distance");

        setOnMouseClicked((event -> useTool(event)));
        setOnMouseDragged((event -> useTool(event)));

        runTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                iterate();
            }
        };

        draw();
    }

    public void iterate() {
        if (aStar == null) {
            aStar = new AStar(startPos, goalPos, new Pair(cellsWidth, cellsHeight), selectedHeuristic, cells);
        }
        aStar.iterate();
        draw();
    }

    public void run() {
        //runTimer.start();
        if (aStar == null) {
            aStar = new AStar(startPos, goalPos, new Pair(cellsWidth, cellsHeight), selectedHeuristic, cells);
        }
        aStar.run();
        draw();
    }

    public void reset() {
        runTimer.stop();
        aStar = null;
        draw();
    }

    public void setDrawTool(final String tool) {
        selectedTool = toolTable.get(tool);
    }

    public void setHeuristic(final String heuristic) {
        selectedHeuristic = heuristicTable.get(heuristic);
    }

    public String[] getDrawToolNames() {
        Set<String> toolNameSet = toolTable.keySet();
        String[] toolNameArr = toolNameSet.toArray(new String[toolNameSet.size()]);
        Arrays.sort(toolNameArr);
        return toolNameArr;
    }

    public String[] getHeuristicNames() {
        Set<String> heuristicNameSet = heuristicTable.keySet();
        String[] heuristicNameArr = heuristicNameSet.toArray(new String[heuristicNameSet.size()]);
        Arrays.sort(heuristicNameArr);
        return heuristicNameArr;
    }
}