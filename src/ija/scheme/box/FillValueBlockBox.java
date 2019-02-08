package ija.scheme.box;

import ija.scheme.canvas.Canvas;
import ija.scheme.canvas.Port;
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

import ija.scheme.canvas.AbstractBlock;
import static ija.scheme.box.AlertBox.*;

/**
 * Třída reprezentující okénko pro vyplnění hodnot vstupních portů pro celý blok.
 *
 * @author Radek Hůlka
 */
public class FillValueBlockBox {

    private final static double ROW_SPACING = 5;
    private final static double IDENTATION = 10;
    private final static double WINDOW_WIDTH = 400;
    private final static double BUTTON_HEIGHT = 30;
    private final static double BUTTON_WIDTH = 80;
    private final static double VALUE_NAME_INDENTATION = 40;
    private final static double NUMBER_FIELD_HEIGHT = 26;

    /**
     * Zobrazí okénko pro vyplnění hodnot vstupních portu bloku.
     *
     * @param block Blok, jehož hodnoty vstupních portů mají být vyplněny.
     */
    public static void display(AbstractBlock block) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Fill in-port value(s)");
        window.setMinWidth(WINDOW_WIDTH);
        window.setMaxWidth(WINDOW_WIDTH);

        Pane layout = new Pane();

        double posY = IDENTATION;
        double posX;

        for (int i = 0; i < block.getInPortsCount(); i++) {
            if (block.getInPort(i).getWire() == null) {
                Text portLabel = new Text(String.valueOf(i+1) + ". IN-port of type \"" + block.getInPort(i).getDataAbstractType().getType() + "\":");
                posY += portLabel.getFont().getSize();
                posY += ROW_SPACING;
                posX = IDENTATION;
                portLabel.setLayoutX(posX);
                portLabel.setLayoutY(posY);
                layout.getChildren().addAll(portLabel);

                for (int j = 0; j < block.getInPort(i).getValuesLength(); j++) {
                    posX += VALUE_NAME_INDENTATION;
                    Text valueName = new Text(block.getInPort(i).getDataAbstractType().getValueName(j) + ": ");
                    posY += NUMBER_FIELD_HEIGHT / 2 + valueName.getBoundsInParent().getHeight() / 2;
                    posY += ROW_SPACING;
                    valueName.setLayoutX(posX);
                    valueName.setLayoutY(posY);
                    posX += valueName.getBoundsInParent().getWidth() + 2;
                    posY -= NUMBER_FIELD_HEIGHT / 2 + valueName.getBoundsInParent().getHeight() / 2;
                    layout.getChildren().addAll(valueName);
                    TextField numberTextField = new TextField();
                    numberTextField.setLayoutX(posX);
                    numberTextField.setLayoutY(posY);
                    numberTextField.setMaxWidth(WINDOW_WIDTH - valueName.getBoundsInParent().getWidth() - VALUE_NAME_INDENTATION - 2 * IDENTATION);
                    numberTextField.setMinWidth(WINDOW_WIDTH - valueName.getBoundsInParent().getWidth() - VALUE_NAME_INDENTATION - 2 * IDENTATION);
                    numberTextField.setMinHeight(NUMBER_FIELD_HEIGHT);
                    numberTextField.setMaxHeight(NUMBER_FIELD_HEIGHT);
                    layout.getChildren().addAll(numberTextField);
                    posY += NUMBER_FIELD_HEIGHT + ROW_SPACING;
                    posX = IDENTATION;
                    ((TextField) layout.getChildren().get(layout.getChildren().size() - 1)).setText(String.valueOf(block.getInPort(i).getValue(j)));
                }
            }
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
                int savedValues = 0;
                for (int i = 0; i < layout.getChildren().size(); i++) {
                    if (layout.getChildren().get(i).getClass().getName() == "javafx.scene.control.TextField") {
                        int k = getPortIndexForNextValue(savedValues, block);
                        int l = getPortValueIndexForNextValue(savedValues, block);
                        block.getInPort(k).setValue(l, Double.valueOf(((TextField) layout.getChildren().get(i)).getText()));
                        block.getInPort(k).getDataAbstractType().setValuesSet(true);
                        savedValues++;
                    }
                }
                window.close();
            } else {
                AlertBox.display(WRONG_VALUES_BOX_WIDTH, WRONG_VALUES_BOX_TITLE, WRONG_VALUES_BOX_DESCRIPTION);
            }
        });
        layout.getChildren().addAll(okButton);

        // DELETE BUTTON
        Button deleteButton = new Button("Delete");
        deleteButton.setMinWidth(BUTTON_WIDTH);
        deleteButton.setMaxWidth(BUTTON_WIDTH);
        deleteButton.setMinHeight(BUTTON_HEIGHT);
        deleteButton.setMaxHeight(BUTTON_HEIGHT);
        deleteButton.setTextAlignment(TextAlignment.CENTER);
        deleteButton.setAlignment(Pos.CENTER);
        deleteButton.setLayoutX(WINDOW_WIDTH * 2 / 4 - BUTTON_WIDTH / 2);
        deleteButton.setLayoutY(posY);
        deleteButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                deleteButton.fire();
                e.consume();
            }
        });
        deleteButton.setOnAction(e -> {
            for (int i = 0; i < block.getInPortsCount(); i++) {
                if (block.getInPort(i).getWire() != null) {
                    block.getInPort(i).getWire().getOtherPort(block.getInPort(i)).setWire(null);
                    ((Canvas) block.getInPort(i).getWire().getParent()).getChildren().remove(block.getInPort(i).getWire());
                }
            }
            for (int i = 0; i < block.getOutPortsCount(); i++) {
                if (block.getOutPort(i).getWire() != null) {
                    block.getOutPort(i).getWire().getOtherPort(block.getOutPort(i)).setWire(null);
                    ((Canvas) block.getOutPort(i).getWire().getParent()).getChildren().remove(block.getOutPort(i).getWire());
                }
            }
            ((Port) block.getOutPort(0)).checkPortsColor((Canvas) block.getParent());
            ((Canvas) block.getParent()).getChildren().remove(block);
            window.close();
        });
        layout.getChildren().addAll(deleteButton);

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
     * @param savedValues Celkový počet uložených hodnot v bloku.
     * @param block Blok, nakterém se má port nacházet.
     * @return Vrací index vstupního portu, do kterého má být uložena následující hodnota.
     */
    private static int getPortIndexForNextValue(int savedValues, AbstractBlock block) {
        int portIndex = 0;
        if (savedValues == 0) {
            portIndex = getNextWirelessInPort(portIndex, block);
        } else {
            while (savedValues > 0) {
                if (block.getInPort(portIndex).getWire() == null) {
                    savedValues -= block.getInPort(portIndex).getValuesLength();
                    if (savedValues >= 0) {
                        if (savedValues == 0) {
                            portIndex++;
                            portIndex = getNextWirelessInPort(portIndex, block);
                        } else {
                            portIndex++;
                        }
                    }
                } else {
                    portIndex++;
                }
            }
        }
        return portIndex;
    }

    /**
     * @param portIndex Index předchozího vstupního portu.
     * @param block Blok, na kterém se má port nacházet.
     * @return Vrací následující vstupní port bez drátu.
     */
    private static int getNextWirelessInPort(int portIndex, AbstractBlock block) {
        while (block.getInPort(portIndex).getWire() != null) {
            portIndex++;
        }
        return portIndex;
    }

    /**
     * @param savedValues Počet uložených hodnot v bloku.
     * @param block Blok na, ve kterém se hodnoty nachází.
     * @return Vrací index pro následující hodnotu.
     */
    private static int getPortValueIndexForNextValue(int savedValues, AbstractBlock block) {
        int portIndex = 0;
        int valueIndex = 0;
        while (savedValues > 0) {
            savedValues -= block.getInPort(portIndex).getValuesLength();
            if (savedValues >= 0) {
                portIndex++;
            } else {
                valueIndex = block.getInPort(portIndex).getValuesLength() + savedValues;
            }
        }
        return valueIndex;
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
