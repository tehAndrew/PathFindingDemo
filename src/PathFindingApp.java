import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PathFindingApp extends Application {
    final int WINDOW_WIDTH = 850;
    final int WINDOW_HEIGHT = 800;
    final int SIM_AREA_WIDTH = WINDOW_WIDTH;
    final int SIM_AREA_HEIGHT = 750;
    final int CONTROL_PANE_WIDTH = WINDOW_WIDTH;
    final int CONTROL_PANE_HEIGHT = WINDOW_HEIGHT - SIM_AREA_HEIGHT;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        stage.setTitle(getClass().getName());
        stage.setResizable(false);
        stage.setScene(scene);

        // Setup the simulation area.
        SimArea simArea = new SimArea(0, 0, SIM_AREA_WIDTH, SIM_AREA_HEIGHT, 50);

        // Setup the control panel.
        ControlPane controlPane = new ControlPane(0, SIM_AREA_HEIGHT, CONTROL_PANE_WIDTH, CONTROL_PANE_HEIGHT);
        controlPane.addButton("Iterate", (event) -> simArea.iterate());
        controlPane.addButton("Run", (event) -> simArea.run());
        controlPane.addButton("Reset", (event) -> simArea.reset());

        controlPane.addChoiceBox(simArea.getDrawToolNames(), (obsValue, oldVal, newVal) -> simArea.setDrawTool(newVal));

        // Add the control panel and the simulation area to the root.
        root.getChildren().addAll(simArea, controlPane);

        // Display app
        stage.show();
    }
}
