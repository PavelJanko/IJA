package ija.scheme.canvas.wire;

import ija.scheme.canvas.Port;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import java.io.IOException;

/**
 * Třída reprezentující drát, který spojuje výstupní a vstupní port se stejným datovým type.
 *
 *  @author Radek Hůlka
 *  @author Pavel Janko
 */
public class Wire extends Line
{
    private Port inPort = null;
    private Port outPort = null;

    private static final int WIRE_THICKNESS = 3;

    /**
     * Konstruktor sloužící k vytvoření drátu.
     */
    public Wire() {
        this.loadFXML();
        this.setStrokeWidth(WIRE_THICKNESS);
    }

    /**
     * Copy konstruktor - vytvoří drát podle vzorového drátu.
     *
     * @param wire vzorový drát
     */
    public Wire(Wire wire) {
        this.loadFXML();
        this.inPort = wire.inPort;
        this.outPort = wire.outPort;

        this.setStartX(wire.getStartX());
        this.setStartY(wire.getStartY());
        this.setEndX(wire.getEndX());
        this.setEndY(wire.getEndY());

        this.setStrokeWidth(WIRE_THICKNESS);

        this.setOnMouseEntered((MouseEvent event) -> {
            Tooltip t = new Tooltip(this.inPort.getValues(this.inPort));
            Tooltip.install(this, t);
            event.consume();
        });
    }

    /**
     * Inicializuje drát.
     */
    public void clearPorts() {
        this.inPort = this.outPort = null;
        this.setStartX(0);
        this.setStartY(0);
        this.setEndX(0);
        this.setEndY(0);
    }

    /**
     * Načte kostru drátu z fxml souboru.
     */
    private void loadFXML() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("wire.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Vrací vstupní port spojený s drátem.
     */
    public Port getInPort() {
        return inPort;
    }

    /**
     * @return Vrací výstupní port spojený s drátem
     */
    public Port getOutPort() {
        return outPort;
    }

    /**
     * @param port Port spojený s drátem.
     * @return Vrací druhý port spojený s drátem.
     */
    public Port getOtherPort(Port port) {
        if (port.getPortType().equals("in"))
            return this.outPort;
        return this.inPort;
    }

    /**
     * Nastaví drátu port, který je s ním spojený.
     *
     * @param port Port, který je s drátem spojený.
     */
    public void setPort(Port port) {
        if (port.getPortType().equals("in"))
            this.inPort = port;
        else
            this.outPort = port;
    }
}
