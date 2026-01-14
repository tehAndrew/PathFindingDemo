package org.pathfindingdemo.Application;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.pathfindingdemo.AStar.AStar;
import org.pathfindingdemo.AStar.CellType;
import org.pathfindingdemo.AStar.Heuristic;
import org.pathfindingdemo.Helpers.Pair;

public class SimArea extends Canvas {
    private boolean buildingEnabled;
    private boolean running;
    private String view;
    private int gridSide;
    private int mapWidth;
    private int mapHeight;
    private CellType[][] map;
    private Pair startPos;
    private Pair endPos;
    private HashMap<String, SimAreaTool> toolTable;
    private HashMap<String, Heuristic> heuristicTable;
    private SimAreaTool selectedTool;
    private Heuristic selectedHeuristic;
    private AStar aStar;
    private AnimationTimer runTimer;

    private void initCells() {
        mapWidth = (int) Math.floor(getWidth() / gridSide);
        mapHeight = (int) Math.floor(getHeight() / gridSide);
        map = new CellType[mapWidth][mapHeight];

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                map[x][y] = CellType.NORMAL;
            }
        }

        startPos = new Pair(0, 0);
        endPos = new Pair(mapWidth - 1, mapHeight - 1);
    }

    private void draw() {
        GraphicsContext g2d = getGraphicsContext2D();

        // Clear sim area
        g2d.clearRect(0, 0, getWidth(), getHeight());

        // Draw border around simulation area.
        g2d.setStroke(Color.BLACK);
        g2d.strokeRect(0, 0, getWidth(), getHeight());

        // Draw cells
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                switch (map[x][y]) {
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

        if (aStar != null) {
            switch (view) {
                case "Normal":
                case "Normal show F":
                    aStar.drawNormal(g2d, gridSide);
                    break;
                case "Paths":
                    aStar.drawPaths(g2d, gridSide);
                    break;
            }
        }

        // Draw start and goal
        g2d.setFill(Color.GREEN);
        g2d.fillRect(startPos.getX() * gridSide, startPos.getY() * gridSide, gridSide, gridSide);

        g2d.setFill(Color.RED);
        g2d.fillRect(endPos.getX() * gridSide, endPos.getY() * gridSide, gridSide, gridSide);

        g2d.setStroke(Color.DARKGRAY);
        // Draw grid.
        if (view == "Paths")
            return;

        for (int x = gridSide; x < getWidth(); x += gridSide) {
            g2d.strokeLine(x, 0, x, getHeight());
        }
        for (int y = gridSide; y < getHeight(); y += gridSide) {
            g2d.strokeLine(0, y, getWidth(), y);
        }
        if (view != "Normal show F" || aStar == null)
            return;
        aStar.drawF(g2d, gridSide);
    }

    private void useTool(MouseEvent event) {
        if (!buildingEnabled)
            return;

        int mouseX = (int) Math.floor(event.getSceneX() / gridSide);
        int mouseY = (int) Math.floor(event.getSceneY() / gridSide);

        mouseX = Math.min(Math.max(mouseX, 0), mapWidth - 1);
        mouseY = Math.min(Math.max(mouseY, 0), mapHeight - 1);

        selectedTool.useTool(mouseX, mouseY);
        draw();
    }

    public SimArea(int x, int y, int width, int height, int gridSide) {
        super(width, height);
        relocate(x, y);
        this.gridSide = gridSide;

        buildingEnabled = true;
        running = false;
        view = "Normal";
        initCells();

        // Create tools
        toolTable = new HashMap<>();
        toolTable.put("Place Start", (int mouseX, int mouseY) -> {
            if (endPos.getX() == mouseX && endPos.getY() == mouseY)
                return;

            startPos = new Pair(mouseX, mouseY);
            map[mouseX][mouseY] = CellType.NORMAL;
        });

        toolTable.put("Place End", (int mouseX, int mouseY) -> {
            if (startPos.getX() == mouseX && startPos.getY() == mouseY)
                return;

            endPos = new Pair(mouseX, mouseY);
            map[mouseX][mouseY] = CellType.NORMAL;
        });

        toolTable.put("Draw Normal Cell", (int mouseX, int mouseY) -> {
            if (startPos.getX() == mouseX && startPos.getY() == mouseY)
                return;

            if (endPos.getX() == mouseX && endPos.getY() == mouseY)
                return;

            map[mouseX][mouseY] = CellType.NORMAL;
        });

        toolTable.put("Draw Slow Cell", (int mouseX, int mouseY) -> {
            if (startPos.getX() == mouseX && startPos.getY() == mouseY)
                return;

            if (endPos.getX() == mouseX && endPos.getY() == mouseY)
                return;

            map[mouseX][mouseY] = CellType.OBSTACLE;
        });

        toolTable.put("Draw Solid Cell", (int mouseX, int mouseY) -> {
                if (startPos.getX() == mouseX && startPos.getY() == mouseY)
                    return;

                if (endPos.getX() == mouseX && endPos.getY() == mouseY)
                    return;

                map[mouseX][mouseY] = CellType.IMPASSABLE;
        });

        selectedTool = toolTable.get("Draw Normal Cell");

        // Create Heuristics
        heuristicTable = new HashMap<>();
        heuristicTable.put("Dijkstra", (Pair fromPos, Pair endPos) -> {
            return 0.;
        });

        heuristicTable.put("Diagonal distance", (Pair fromPos, Pair endPos) -> {
            int dx = Math.abs(fromPos.getX() - endPos.getX());
            int dy = Math.abs(fromPos.getY() - endPos.getY());
            return AStar.NORMAL_WEIGHT * (dx + dy) +
                    (AStar.DIAGONAL_COST_FACTOR * AStar.NORMAL_WEIGHT - 2 * AStar.NORMAL_WEIGHT)
                    * Math.min(dx, dy);
        });

        heuristicTable.put("Euclidean", (Pair fromPos, Pair endPos) -> {
            int dx = Math.abs(fromPos.getX() - endPos.getX());
            int dy = Math.abs(fromPos.getY() - endPos.getY());
            return AStar.NORMAL_WEIGHT * Math.sqrt(dx * dx + dy * dy);
        });

        heuristicTable.put("Manhattan", (Pair fromPos, Pair endPos) -> {
            int dx = Math.abs(fromPos.getX() - endPos.getX());
            int dy = Math.abs(fromPos.getY() - endPos.getY());
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

    public void setView(String view) {
        this.view = view;
        draw();
    }

    public void newGrid() {
        reset();
        initCells();
        draw();
    }

    public void iterate() {
        if (aStar == null) {
            aStar = new AStar(startPos, endPos, new Pair(mapWidth, mapHeight), selectedHeuristic, map);
        }
        aStar.iterate();
        draw();
    }

    public void run() {
        if (running) {
            runTimer.stop();
            running = false;
            return;
        }
        runTimer.start();
        running = true;
    }

    public void reset() {
        runTimer.stop();
        running = false;
        aStar = null;
        draw();
    }

    public void enableBuilding(boolean enable) {
        buildingEnabled = enable;
    }

    public void setGridSide(int gridSide) {
        this.gridSide = gridSide;
    }

    public void setDrawTool(String tool) {
        selectedTool = toolTable.get(tool);
    }

    public void setHeuristic(String heuristic) {
        selectedHeuristic = heuristicTable.get(heuristic);
    }

    public final MapData getMap() {
        return new MapData(startPos, endPos, mapWidth, mapHeight, map);
    }

    public void setMap(final MapData mapData) {
        startPos = mapData.getStartPos();
        endPos = mapData.getEndPos();
        mapWidth = mapData.getMapWidth();
        mapHeight = mapData.getMapHeight();
        map = mapData.getMap();
        draw();
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