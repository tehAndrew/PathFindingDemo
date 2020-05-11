import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class SimArea extends Canvas {
    public static enum State {
        SIM,
        SIM_END,
        BUILD
    };

    public SimArea(int x, int y, int width, int height) {
        super(width, height);
        relocate(x, y);

        getGraphicsContext2D().setFill(Color.GREEN);
        getGraphicsContext2D().fillRect(0, 0, width, height);

        setOnMouseClicked((event -> {
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();
            System.out.println("sim area clicked (" + mouseX + ", " + mouseY + ").");
        }));
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
}
