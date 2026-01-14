package org.pathfindingdemo.Application;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PathFindingApp extends Application {
    enum SimState {
        BUILDING,
        SIMULATING
    }

    final int WINDOW_WIDTH = 1600;
    final int WINDOW_HEIGHT = 900;
    final int SIM_AREA_WIDTH = WINDOW_WIDTH;
    final int SIM_AREA_HEIGHT = 832;
    final int CONTROL_PANE_WIDTH = WINDOW_WIDTH;
    final int CONTROL_PANE_HEIGHT = WINDOW_HEIGHT - SIM_AREA_HEIGHT;

    final int SMALL_GRID_SIDE = 16;
    final int MEDIUM_GRID_SIDE = 32;
    final int LARGE_GRID_SIDE = 64;

    Group root;
    Scene scene;
    MapLoader mapLoader;
    SimArea simArea;
    ControlPane controlPane;
    int gridSideSetting;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        gridSideSetting = LARGE_GRID_SIDE;

        root = new Group();
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        mapLoader = new MapLoader();

        stage.setTitle(getClass().getName() + " - Ugly source code edition");
        stage.setResizable(false);
        stage.setScene(scene);

        // Setup the simulation area.
        simArea = new SimArea(0, 0, SIM_AREA_WIDTH, SIM_AREA_HEIGHT, gridSideSetting);

        // Setup the control panel.
        controlPane = new ControlPane(0, SIM_AREA_HEIGHT, CONTROL_PANE_WIDTH, CONTROL_PANE_HEIGHT);
        controlPane.addButton("New grid", (event) -> {
            setState(SimState.BUILDING);
            simArea.setGridSide(gridSideSetting);
            simArea.newGrid();
        });

        controlPane.addButton("Save", (event) -> {
            setState(SimState.BUILDING);
            mapLoader.saveMap(stage, simArea.getMap());
        });

        controlPane.addButton("Load", (event) -> {
            setState(SimState.BUILDING);
            simArea.reset();

            MapData mapData = mapLoader.loadMap(stage);
            if (mapData == null)
                return;

            // Check if grid size is valid
            int cellWidth = SIM_AREA_WIDTH / mapData.getMapWidth();
            int cellHeight = SIM_AREA_HEIGHT / mapData.getMapHeight();

            // Is the rectangle a square?
            if (cellWidth != cellHeight)
                return;

            // Does it match the predefined sizes?
            switch (cellWidth) {
                case SMALL_GRID_SIDE:
                case MEDIUM_GRID_SIDE:
                case LARGE_GRID_SIDE:
                    break;
                default:
                    return;
            }

            simArea.setGridSide(cellWidth);
            simArea.setMap(mapData);
        });

        String[] gridSizes = {"Small size", "Medium size", "Large size"};
        controlPane.addChoiceBox("Grid size", gridSizes, (obsValue, oldVal, newVal) -> {
            gridSideSetting = LARGE_GRID_SIDE;
            switch (newVal) {
                case "Medium size":
                    gridSideSetting = MEDIUM_GRID_SIDE;
                    break;
                case "Large size":
                    gridSideSetting = SMALL_GRID_SIDE;
                    break;
            }
        });

        String[] views = {"Normal", "Normal show F", "Paths"};
        controlPane.addChoiceBox("Grid size", views, (obsValue, oldVal, newVal) -> {
            simArea.setView(newVal);
        });

        controlPane.addButton("Iterate", (event) -> {
            setState(SimState.SIMULATING);
            simArea.iterate();
        });
        controlPane.addButton("Run/Stop", (event) -> {
            setState(SimState.SIMULATING);
            simArea.run();
        });
        controlPane.addButton("Reset", (event) -> {
            setState(SimState.BUILDING);
            simArea.reset();
        });

        controlPane.addChoiceBox("Tools", simArea.getDrawToolNames(), (obsValue, oldVal, newVal) -> simArea.setDrawTool(newVal));
        controlPane.addChoiceBox("Heuristics", simArea.getHeuristicNames(), (obsValue, oldVal, newVal) -> simArea.setHeuristic(newVal));

        // Add the control panel and the simulation area to the root.
        root.getChildren().addAll(simArea, controlPane);

        // Display app
        stage.show();
    }

    void setState(SimState state) {
        switch (state) {
            case BUILDING:
                controlPane.enableElement("Tools");
                controlPane.enableElement("Heuristics");
                simArea.enableBuilding(true);
                break;

            case SIMULATING:
                controlPane.disableElement("Tools");
                controlPane.disableElement("Heuristics");
                simArea.enableBuilding(false);
        }
    }
}
