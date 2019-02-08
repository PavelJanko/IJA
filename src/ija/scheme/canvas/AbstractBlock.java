package ija.scheme.canvas;

import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;

import ija.scheme.box.FillValueBlockBox;

import javax.xml.bind.annotation.XmlTransient;

import static ija.scheme.canvas.Port.PORT_RADIUS;
import static javafx.scene.text.TextAlignment.CENTER;

/**
 * Abstraktní třída reprezentující jednotlivé bloky kanvasu
 *
 * @author Pavel Janko
 * @author Radek Hůlka
 */
public abstract class AbstractBlock extends Pane
{
    protected Point2D location;

    @XmlTransient
    private double inPortDistance;

    @XmlTransient
    private double outPortDistance;

    @XmlTransient
    private boolean calculated;

    public final static String[] blocks = {"Div1w1a2w1to1w1", "Mul1w1a2w1to1w1", "Sub1w1a2w1to1w1", "Add1w1a2w1to1w1", "Add1w3to1w1", "Add1w1a2w2a3w3to1w1", "Add1w1a2w1a3w1to1w1a2w1a3w1", "WaterLaboratory"};

    protected static int index = 0;
    private final static double PORT_LABEL_SPACING = 3;
    private final static int BLOCK_NAME_SIZE = 20;
    private final static double POLYGON_STROKE_WIDTH = 2;
    private final static double LOWER_TOP_BOTTOM_SPACING = 5;
    public final static Color BLOCK_COLOR = Color.BLACK;

    /**
     * Vytvoři grafickou podobu bloku.
     *
     * @param blockBox blok, který se má sestavit
     * @param width šířka bloku
     * @param height výška bloku
     * @param inPortCount počet vstupních portů
     * @param inPortsTypes počet typů vstupních portů
     * @param inPortsLabels popisky vstupních portů
     * @param outPortCount počet výstupních portů
     * @param outPortsTypes počet typů výstupních portů
     * @param outPortsLabels popisky výstupních portů
     * @param label popisek bloku
     */
    protected void constructBlock(AbstractBlock blockBox, int width, int height,
                                  int inPortCount, String[] inPortsTypes, String[] inPortsLabels, int outPortCount,
                                  String[] outPortsTypes, String[] outPortsLabels, String label) {
        index++;
        this.calculated = false;

        // vytvoreni in portu + jejich labelu
        this.inPortDistance = ((height + 2 * LOWER_TOP_BOTTOM_SPACING) / (inPortCount + 1));
        for (int i = 0; i < inPortCount; i++) {
            blockBox.getChildren().add(new Port(
                    -PORT_RADIUS - width / 2,
                    inPortDistance * (i + 1) - height / 2 - 5,
                    i,
                    "in",
                    inPortsTypes[i],
                    this
            ));
            Text portLabel = new Text(inPortsLabels[i]);
            portLabel.setLayoutX(PORT_LABEL_SPACING - width / 2);
            portLabel.setLayoutY(inPortDistance * (i + 1) - height / 2 + portLabel.getBoundsInParent().getHeight() / 2 - PORT_RADIUS / 2 - LOWER_TOP_BOTTOM_SPACING);
            blockBox.getChildren().addAll(portLabel);
        }

        // vytvoreni out portu + jejich labelu
        this.outPortDistance = ((height + 2 * LOWER_TOP_BOTTOM_SPACING) / (outPortCount + 1));
        for (int i = 0; i < outPortCount; i++) {
            blockBox.getChildren().add(new Port(
                    PORT_RADIUS + width / 2,
                    outPortDistance * (i + 1) - height / 2 - 5,
                    i,
                    "out",
                    outPortsTypes[i],
                    this
            ));
            Text portLabel = new Text(outPortsLabels[i]);
            portLabel.setLayoutX(width / 2 - PORT_LABEL_SPACING - portLabel.getBoundsInParent().getWidth());
            portLabel.setLayoutY(outPortDistance * (i + 1) - height / 2 + portLabel.getBoundsInParent().getHeight() / 2 - PORT_RADIUS / 2 - LOWER_TOP_BOTTOM_SPACING);
            blockBox.getChildren().addAll(portLabel);
        }

        // vytovreni obdelniku bloku
        Polygon polygon = new Polygon(
                -width / 2, -height / 2,
                width / 2, -height / 2,
                width / 2, height / 2,
                -width / 2, height / 2
        );
        polygon.setFill(Color.TRANSPARENT);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH);
        polygon.setStroke(BLOCK_COLOR);
        blockBox.getChildren().add(polygon);

        // vytvoreni textu bloku
        Text text = new Text(label);
        text.setFont(Font.font("Verdana", FontWeight.BOLD, BLOCK_NAME_SIZE));
        text.setX(-text.getBoundsInParent().getWidth() / 2);
        text.setY(-text.getBoundsInParent().getHeight() / 2 + text.getFont().getSize());
        text.setTextAlignment(CENTER);
        blockBox.getChildren().add(text);

        blockBox.relocate(
                blockBox.location.getX() + blockBox.getWidth(),
                blockBox.location.getY() + blockBox.getHeight()
        );

        // rightclick pro zadani hodnot na portu
        this.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                FillValueBlockBox.display(this);
                for (int i = 0; i < blockBox.getInPortsCount(); i++) {
                    if (blockBox.getInPort(i).getDataAbstractType().getValuesSet() && blockBox.getInPort(i).getWire() == null) {
                        blockBox.getInPort(i).setFill(ija.scheme.canvas.Port.WIRELESS_SET_IN_PORT_COLOR);
                    } else if (!blockBox.getInPort(i).getDataAbstractType().getValuesSet() && blockBox.getInPort(i).getWire() == null) {
                        blockBox.getInPort(i).setFill(ija.scheme.canvas.Port.WIRELESS_UNSET_IN_PORT_COLOR);
                    } else {
                        blockBox.getInPort(i).setFill(ija.scheme.canvas.Port.WIRED_IN_OUT_PORT_COLOR);
                    }
                }
            }
            event.consume();
        });
    }

    /**
     * Načte kostru bloku z FXML souboru
     *
     * @param block instance bloku, který dědí z této třídy
     * @param fileName soubor, ze kterého se má FXML načíst
     */
    protected void loadFXML(AbstractBlock block, String fileName) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fileName));
        fxmlLoader.setRoot(block);
        fxmlLoader.setController(block);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Nastaví drag pro blok.
     *
     * @param blockBox Blok pro, který se mají nastavit manipulátory
     */
    protected void setHandlers(AbstractBlock blockBox) {
        blockBox.setOnDragDetected((MouseEvent event) -> {
            Dragboard dragboard = blockBox.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString("BLOCK_DRAG");
            dragboard.setContent(clipboardContent);
            event.consume();
        });
    }

    /**
     * @param x X-ová souřadnice polohy kurzoru
     * @param canvasWidth šířka kanvasu
     * @param blockWidth šířka bloku
     * @return Vrací x-ovou souřadnici bloku.
     */
    public double getLocX(double x, double canvasWidth, int blockWidth) {
        if (x - blockWidth / 2 - Port.PORT_RADIUS * 2 < 0) {
            return blockWidth / 2 + Port.PORT_RADIUS * 2;
        }
        if (canvasWidth < x + blockWidth / 2 + Port.PORT_RADIUS * 2) {
            return canvasWidth - blockWidth / 2 - Port.PORT_RADIUS * 2;
        }
        return x;
    }

    /**
     * @param y Y-nová souřadnice polohy kurzoru
     * @param canvasHeight výška kanvasu
     * @param blockHeight výška bloku
     * @return Vrací y-ovou souřadnici bloku.
     */
    public double getLocY(double y, double canvasHeight, int blockHeight) {
        if (y - blockHeight / 2 < 0) {
            return blockHeight / 2;
        }
        if (canvasHeight < y + blockHeight / 2) {
            return canvasHeight - blockHeight / 2;
        }
        return y;
    }

    /**
     * Zjistí vstupní port podle indexu, přičemž předpokládá, že
     * vstupní porty jsou uloženy před výstupními.
     *
     * @param index index vstupního portu
     * @return Vrátí vstupní port bloku se zadaným indexem.
     */
    public Port getInPort(int index) {
        boolean portFound = false;
        Port port = null;
        int childrenIndex = 0;
        int portIndex = 0;

        while (!portFound) {
            if (this.getChildren().get(childrenIndex).getClass().getName().equals("ija.scheme.canvas.Port")) {
                if (portIndex == index) {
                    port = (Port) this.getChildren().get(childrenIndex);
                    portFound = true;
                }
                portIndex++;
            }
            childrenIndex++;
        }
        return port;
    }

    /**
     * Zjistí výstupní port podle indexu, přičemž předpokládá, že
     * vstupní porty jsou uloženy před výstupními.
     *
     * @param index index výstupního portu
     * @return Vrátí výstupní port bloku se zadaným indexem.
     */
    public Port getOutPort(int index) {
        boolean portFound = false;
        Port port = null;
        int childrenIndex = 0;
        int portIndex = 0;

        while (!portFound) {
            if (this.getChildren().get(childrenIndex).getClass().getName().equals("ija.scheme.canvas.Port")) {
                if (portIndex - this.getInPortsCount() == index) {
                    port = (Port) this.getChildren().get(childrenIndex);
                    portFound = true;
                }
                portIndex++;
            }
            childrenIndex++;
        }
        return port;
    }

    /**
     * @param type typ portu
     * @return Vrací vzdálenost portů od sebe.
     */
    public double getPortDistance(String type) {
        if (type.equals("in"))
            return this.inPortDistance;
        return this.outPortDistance;
    }

    /**
     * @return Vrací počet vstupních portů.
     */
    abstract public int getInPortsCount();

    /**
     * @return Vrací počet výstupních portů.
     */
    abstract public int getOutPortsCount();

    /**
     * Provede výpočet bloku.
     */
    abstract public void doCalculation();

    /**
     * @return Vrátí popisky vstupních portů.
     */
    abstract public String[] getInPortsLabels();

    /**
     * @return Vrátí popisky výstupních portů.
     */
    abstract public String[] getOutPortsLabels();

    /**
     * @return Vrátí název bloku.
     */
    abstract public String getBlockName();

    /**
     * Nastaví, zdali je blok vypočítaný nebo ne.
     *
     * @param value hodnota nastavení
     */
    public void setCalculated(boolean value) {
        this.calculated = value;
    }

    /**
     * @return Vrací informaci o tom, zdali je blok vypočítaný.
     */
    public boolean getCalculated() {
        return this.calculated;
    }

    /**
     * @return Vrací lokaci bloku.
     */
    public Point2D getLocation() {
        return location;
    }
}
