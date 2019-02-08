package ija;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import ija.scheme.Scheme;
import static ija.scheme.Scheme.SCHEME_WIDTH;
import static ija.scheme.Scheme.SCHEME_HEIGHT;

/**
 * Třída reprezentující hlavní okno aplikace.
 *
 * @author Pavel Janko
 */
public class Main extends Application
{
    /**
     * Inicializuje okno a následně jej zobrazí.
     *
     * @param primaryStage primární scéna aplikace
     */
    @Override
    public void start(Stage primaryStage) {
        Scheme scheme = new Scheme();
        primaryStage.setTitle("IJA - Block schema");
        primaryStage.setScene(new Scene(scheme, SCHEME_WIDTH, SCHEME_HEIGHT));
        primaryStage.show();
    }

    /**
     * Spouští aplikaci.
     *
     * @param args pole argumentů
     */
    public static void main(String[] args) {
        launch(args);
    }
}
