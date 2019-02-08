package ija.scheme;

import ija.scheme.canvas.Canvas;
import ija.scheme.menubar.MenuBar;
import ija.scheme.toolbar.ToolBar;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

/**
 * Třída sloužící k rozložení elementů aplikace
 *
 * @author Pavel Janko
 */
public class Scheme extends BorderPane
{
    public static final int SCHEME_WIDTH = 1280;
    public static final int SCHEME_HEIGHT = 720;

    /**
     * Konstruktor třídy slouží k instanciaci třech hlavních grafických komponent a jejich
     * následnému rozložení.
     */
    public Scheme()
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("scheme.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        Canvas canvas = new Canvas();

        MenuBar menuBar = new MenuBar(canvas);

        ToolBar toolBar = new ToolBar(menuBar.menuBar);

        this.setTop(menuBar.menuBar);
        this.setLeft(toolBar.toolBar);
        this.setCenter(canvas);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
