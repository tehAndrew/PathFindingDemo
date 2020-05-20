import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.HashMap;

public class ControlPane extends FlowPane {
    HashMap<String, Node> elements;

    public ControlPane(int x, int y, int width, int height) {
        super();
        elements = new HashMap<>();
        setMaxSize(width, height);
        setMinSize(width, height);
        setHgap(10); // Hardcoded af
        setPadding(new Insets(10, 10, 10, 10)); // Hardcoded deluxe
        relocate(x, y);
    }

    public void addButton(String name, EventHandler<ActionEvent> onAction) {
        Button button = new Button(name);
        button.setOnAction(onAction);

        getChildren().add(button);
        elements.put(name, button);
    }

    public void addChoiceBox(String name, String[] choices, ChangeListener<String> listener) {
        ChoiceBox choiceBox = new ChoiceBox();
        for (int i = 0; i < choices.length; i++) {
            choiceBox.getItems().add(choices[i]);
        }
        choiceBox.getSelectionModel().select(0);
        choiceBox.getSelectionModel().selectedItemProperty().addListener(listener);

        getChildren().add(choiceBox);
        elements.put(name, choiceBox);
    }

    public void disableElement(String name) {
        Node element = elements.get(name);
        if (element == null)
            return;

        element.setDisable(true);
    }
}
