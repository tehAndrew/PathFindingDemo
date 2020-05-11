import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

public class ControlPane extends FlowPane {
    public ControlPane(int x, int y, int width, int height) {
        super();
        setMaxSize(width, height);
        setMinSize(width, height);
        relocate(x, y);
    }

    public void addButton(String text, EventHandler<ActionEvent> onAction) {
        Button button = new Button(text);
        button.setOnAction(onAction);
        getChildren().add(button);
    }
}
