import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.FlowPane;

public class ControlPane extends FlowPane {
    public ControlPane(int x, int y, int width, int height) {
        super();
        setMaxSize(width, height);
        setMinSize(width, height);
        setHgap(10); // Hardcoded af
        setPadding(new Insets(10, 10, 10, 10)); // Hardcoded deluxe
        relocate(x, y);
    }

    public void addButton(String text, EventHandler<ActionEvent> onAction) {
        Button button = new Button(text);
        button.setOnAction(onAction);
        getChildren().add(button);
    }

    public void addChoiceBox(String[] choices, ChangeListener<String> listener) {
        ChoiceBox choiceBox = new ChoiceBox();
        for (int i = 0; i < choices.length; i++) {
            choiceBox.getItems().add(choices[i]);
        }
        choiceBox.getSelectionModel().select(0);
        choiceBox.getSelectionModel().selectedItemProperty().addListener(listener);

        getChildren().add(choiceBox);
    }
}
