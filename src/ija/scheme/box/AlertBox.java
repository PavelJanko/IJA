package ija.scheme.box;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Třída reprezentující informační okénka.
 *
 * @author Radek Hůlka
 */
public class AlertBox {

    public final static double CALCULATIONS_DONE_BOX_WIDTH = 300;
    public final static String CALCULATIONS_DONE_BOX_TITLE = "Calculations done!";
    public final static String CALCULATIONS_DONE_BOX_DESCRIPTION = "Calculations successfully completed!";

    public final static double CYCLES_DETECTED_BOX_WIDTH = 300;
    public final static String CYCLES_DETECTED_BOX_TITLE = "Cycle detected!";
    public final static String CYCLES_DETECTED_BOX_DESCRIPTION = "There is a cycle in your block scheme...\nI can't calculate it.\nYou have to fix it!";

    public final static double WRONG_VALUES_BOX_WIDTH = 300;
    public final static String WRONG_VALUES_BOX_TITLE = "Wrong values inserted!";
    public final static String WRONG_VALUES_BOX_DESCRIPTION = "All values have to be decimal numbers!";

    public final static double NOT_FILLED_INPORT_VALUES_BOX_WIDTH = 300;
    public final static String NOT_FILLED_INPORT_VALUES_BOX_TITLE = "Not filled IN-ports!";
    public final static String NOT_FILLED_INPORT_VALUES_BOX_DESCRIPTION = "You have to set values to all red ports!\n\nUse righclick on block with red IN-port(s)\nor rightclick on red IN-port";

    public final static double HELP_BOX_WIDTH = 500;
    public final static String HELP_BOX_TITLE = "Help";
    public final static String HELP_BOX_DESCRIPTION = "The application is used for creating/editing block schemas.\n\nYou can add new block by dragging from toolbar.\n\nYou can fill in-port values by rightclick on single port\nor you can fill all blocks in-ports by righclick on whole block.\n\nYou can delete block by rightclick on block.\n\nYou can calculate your scheme through run menu.\n\nYou can save or import your schema through file menu.\n\nPort colors indicates port status and functionality:\nred is for wireless in-port with not filled values\nblue is for wireless out-port\nblack is for wired in-port/out-port\ngreen is for wireless in-port witch filled values\nyellow is for focused in-port/out-port";

    public final static double CANT_CREATE_NEW_BLOCK_BOX_WIDTH = 300;
    public final static String CANT_CREATE_NEW_BLOCK_BOX_TITLE = "Can't create new block!";
    public final static String CANT_CREATE_NEW_BLOCK_BOX_DESCRIPTION = "You have to finish step calculations\nif you want to create new blocks...";
    
    private final static double ROW_SPACING = 5;
    private final static double IDENTATION = 10;
    private final static double BUTTON_HEIGHT = 30;
    private final static double BUTTON_WIDTH = 80;

    /**
     * Zobrazí informační okénko.
     *
     * @param windowWidth Šířka okénka.
     * @param title Popis okénka.
     * @param description Obsah okénka.
     */
    public static void display(double windowWidth, String title, String description) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(windowWidth);
        window.setMaxWidth(windowWidth);

        Pane layout = new Pane();

        double posY = IDENTATION;
        double posX;

        Text text = new Text();
        text.setText(description);
        text.setTextAlignment(TextAlignment.CENTER);
        posY += text.getFont().getSize();
        posY += ROW_SPACING;
        posX = windowWidth / 2 - text.getBoundsInParent().getWidth() / 2;
        text.setLayoutX(posX);
        text.setLayoutY(posY);
        layout.getChildren().addAll(text);

        posY += 2 * IDENTATION + text.getBoundsInParent().getHeight() - text.getFont().getSize();

        // OK BUTTON
        Button okButton = new Button("Ok");
        okButton.setMinWidth(BUTTON_WIDTH);
        okButton.setMaxWidth(BUTTON_WIDTH);
        okButton.setMinHeight(BUTTON_HEIGHT);
        okButton.setMaxHeight(BUTTON_HEIGHT);
        okButton.setTextAlignment(TextAlignment.CENTER);
        okButton.setAlignment(Pos.CENTER);
        okButton.setLayoutX(windowWidth / 2 - BUTTON_WIDTH / 2);
        okButton.setLayoutY(posY);
        okButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                okButton.fire();
                e.consume();
            }
        });
        okButton.setOnAction(e -> window.close());
        layout.getChildren().addAll(okButton);

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
}
