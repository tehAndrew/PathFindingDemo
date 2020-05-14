import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class SimArea extends Canvas {
    private final byte EDGE_TO_NODE_NONE = 0;
    private final byte EDGE_TO_NODE_NORMAL = 1;
    private final byte EDGE_TO_NODE_SLOW = 2;

    private int gridSide;
    private int cellsWidth;
    private int cellsHeight;
    private byte[][] cells;
    private Position startPos;
    private Position goalPos;
    private HashMap<String, SimAreaTool> toolTable;
    private SimAreaTool selectedTool;

    private void initCells() {
        cells = new byte[cellsWidth][cellsHeight];

        for (int x = 0; x < cellsWidth; x++) {
            for (int y = 0; y < cellsHeight; y++) {
                cells[x][y] = EDGE_TO_NODE_NORMAL;
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
                    case EDGE_TO_NODE_NONE:
                        g2d.setFill(Color.BLACK);
                        g2d.fillRect(x * gridSide, y * gridSide, gridSide, gridSide);
                        break;
                    case EDGE_TO_NODE_SLOW:
                        g2d.setFill(Color.LIGHTBLUE);
                        g2d.fillRect(x * gridSide, y * gridSide, gridSide, gridSide);
                        break;
                    default:
                        break;
                }
            }
        }

        // Draw start and goal
        g2d.setFill(Color.BLACK);
        g2d.setFont(new Font(30));
        g2d.setTextAlign(TextAlignment.CENTER);
        g2d.setTextBaseline(VPos.CENTER);
        g2d.fillText("A", startPos.x * gridSide + gridSide * 0.5, startPos.y * gridSide + gridSide * 0.5);
        g2d.fillText("B", goalPos.x * gridSide + gridSide * 0.5, goalPos.y * gridSide + gridSide * 0.5);

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

        startPos = new Position(0, 0);
        goalPos = new Position(cellsWidth - 1, cellsHeight - 1);

        // Create tools
        toolTable = new HashMap<>();
        toolTable.put("Place Start", new SimAreaTool() {
            @Override
            public void useTool(int mouseX, int mouseY) {
                if (goalPos.x == mouseX && goalPos.y == mouseY)
                    return;

                startPos = new Position(mouseX, mouseY);
                cells[mouseX][mouseY] = EDGE_TO_NODE_NORMAL;
            }
        });

        toolTable.put("Place End", new SimAreaTool() {
            @Override
            public void useTool(int mouseX, int mouseY) {
                if (startPos.x == mouseX && startPos.y == mouseY)
                    return;

                goalPos = new Position(mouseX, mouseY);
                cells[mouseX][mouseY] = EDGE_TO_NODE_NORMAL;
            }
        });

        toolTable.put("Draw Normal Cell", new SimAreaTool() {
            @Override
            public void useTool(int mouseX, int mouseY) {
                if (startPos.x == mouseX && startPos.y == mouseY)
                    return;

                if (goalPos.x == mouseX && goalPos.y == mouseY)
                    return;

                cells[mouseX][mouseY] = EDGE_TO_NODE_NORMAL;
            }
        });

        toolTable.put("Draw Slow Cell", new SimAreaTool() {
            @Override
            public void useTool(int mouseX, int mouseY) {
                if (startPos.x == mouseX && startPos.y == mouseY)
                    return;

                if (goalPos.x == mouseX && goalPos.y == mouseY)
                    return;

                cells[mouseX][mouseY] = EDGE_TO_NODE_SLOW;
            }
        });

        toolTable.put("Draw Solid Cell", new SimAreaTool() {
            @Override
            public void useTool(int mouseX, int mouseY) {
                if (startPos.x == mouseX && startPos.y == mouseY)
                    return;

                if (goalPos.x == mouseX && goalPos.y == mouseY)
                    return;

                cells[mouseX][mouseY] = EDGE_TO_NODE_NONE;
            }
        });

        selectedTool = toolTable.get("Draw Normal Cell");

        setOnMouseDragged((event -> useTool(event)));

        draw();
    }

    public void iterate() {
        System.out.println("iterate");
    }

    public void run() {
        System.out.println("run");
    }

    public void reset() {
        System.out.println("reset");
    }

    public void setDrawTool(final String tool) {
        selectedTool = toolTable.get(tool);
    }

    public String[] getDrawToolNames() {
        Set<String> toolNameSet = toolTable.keySet();
        String[] toolNameArr = toolNameSet.toArray(new String[toolNameSet.size()]);
        Arrays.sort(toolNameArr);
        return toolNameArr;
    }
}
