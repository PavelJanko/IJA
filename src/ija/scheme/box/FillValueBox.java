package ija.scheme.box;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import ija.scheme.canvas.AbstractType;
import static ija.scheme.box.AlertBox.*;

/**
 * Třída reprezentující okénko pro vyplnění hodnot vstupního portu.
 *
 * @author Radek Hůlka
 */
public class FillValueBox {

    private final static double ROW_SPACING = 5;
    private final static double IDENTATION = 10;
    private final static double WINDOW_WIDTH = 400;
    private final static double BUTTON_HEIGHT = 30;
    private final static double BUTTON_WIDTH = 80;
    private final static double NUMBER_FIELD_HEIGHT = 26;

    /**
     * Zobrazí okénko pro vyplnění hodnot vstupního portu.
     *
     * @param dataType Typ dat portu.
     */
    public static void display(AbstractType dataType) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Fill in-port value(s)");
        window.setMinWidth(WINDOW_WIDTH);
        window.setMaxWidth(WINDOW_WIDTH);

        Pane layout = new Pane();

        double posX = IDENTATION;
        double posY = IDENTATION;

        for (int i = 0; i < dataType.getValuesLength(); i++) {
            Text valueName = new Text(dataType.getValueName(i) + ": ");
            posY += NUMBER_FIELD_HEIGHT / 2 + valueName.getBoundsInParent().getHeight() / 2;
            posY += ROW_SPACING;
            valueName.setLayoutX(posX);
            valueName.setLayoutY(posY);
            posX += valueName.getBoundsInParent().getWidth() + IDENTATION / 3;
            posY -= NUMBER_FIELD_HEIGHT / 2 + valueName.getBoundsInParent().getHeight() / 2;
            layout.getChildren().addAll(valueName);
            TextField numberTextField = new TextField();
            numberTextField.setLayoutX(posX);
            numberTextField.setLayoutY(posY);
            numberTextField.setMaxWidth(WINDOW_WIDTH - valueName.getBoundsInParent().getWidth() - 2 * IDENTATION);
            numberTextField.setMinWidth(WINDOW_WIDTH - valueName.getBoundsInParent().getWidth() - 2 * IDENTATION);
            numberTextField.setMinHeight(NUMBER_FIELD_HEIGHT);
            numberTextField.setMaxHeight(NUMBER_FIELD_HEIGHT);
            layout.getChildren().addAll(numberTextField);
            posY += NUMBER_FIELD_HEIGHT + ROW_SPACING;
            posX = IDENTATION;
            ((TextField) layout.getChildren().get(layout.getChildren().size() - 1)).setText(String.valueOf(dataType.getValue(i)));
        }

        posY += 2 * IDENTATION;

        // OK BUTTON
        Button okButton = new Button("Ok");
        okButton.setMinWidth(BUTTON_WIDTH);
        okButton.setMaxWidth(BUTTON_WIDTH);
        okButton.setMinHeight(BUTTON_HEIGHT);
        okButton.setMaxHeight(BUTTON_HEIGHT);
        okButton.setTextAlignment(TextAlignment.CENTER);
        okButton.setAlignment(Pos.CENTER);
        okButton.setLayoutX(WINDOW_WIDTH / 4 - BUTTON_WIDTH / 2);
        okButton.setLayoutY(posY);
        okButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                okButton.fire();
                e.consume();
            }
        });
        okButton.setOnAction(e -> {
            if (validateTextFields(layout)) {
                int valueIndex = 0;
                for (int i = 0; i < layout.getChildren().size(); i++) {
                    if (layout.getChildren().get(i).getClass().getName().equals("javafx.scene.control.TextField")) {
                        dataType.setValue(valueIndex, Double.valueOf(((TextField) layout.getChildren().get(i)).getText()));
                        valueIndex++;
                    }
                }
                dataType.setValuesSet(true);
                window.close();
            } else {
                AlertBox.display(WRONG_VALUES_BOX_WIDTH, WRONG_VALUES_BOX_TITLE, WRONG_VALUES_BOX_DESCRIPTION);
            }
        });
        layout.getChildren().addAll(okButton);

        // CLOSE BUTTON
        Button closeButton = new Button("Close");
        closeButton.setMinWidth(BUTTON_WIDTH);
        closeButton.setMaxWidth(BUTTON_WIDTH);
        closeButton.setMinHeight(BUTTON_HEIGHT);
        closeButton.setMaxHeight(BUTTON_HEIGHT);
        closeButton.setTextAlignment(TextAlignment.CENTER);
        closeButton.setAlignment(Pos.CENTER);
        closeButton.setLayoutX(WINDOW_WIDTH * 3 / 4 - BUTTON_WIDTH / 2);
        closeButton.setLayoutY(posY);
        closeButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                closeButton.fire();
                e.consume();
            }
        });
        closeButton.setOnAction(e -> window.close());
        layout.getChildren().addAll(closeButton);

        // VYTVORENI ODSAZENI
        Text textHack = new Text(".");
        textHack.setFill(Color.TRANSPARENT);
        textHack.setLayoutX(0);
        posY += BUTTON_HEIGHT + IDENTATION;
        textHack.setLayoutY(posY);
        layout.getChildren().addAll(textHack);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }

    /**
     * @param layout Oblast, jejíž textová pole se mají zkontrolovat.
     * @return Vrací true, pokud jsou hodnoty textových polí vpořádku, jinak false.
     */
    private static boolean validateTextFields(Pane layout) {
        for (int i = 0; i < layout.getChildren().size(); i++) {
            if (layout.getChildren().get(i).getClass().getName().equals("javafx.scene.control.TextField")) {
                if (!((((TextField) layout.getChildren().get(i)).getText()).matches("([\\+-].)?\\d*\\.?\\d*"))) {
                    return false;
                }
            }
        }
        return true;
    }
}
