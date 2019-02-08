package ija.scheme.canvas;

import ija.scheme.Scheme;
import javafx.scene.paint.Color;
import javafx.scene.control.Tooltip;
import javafx.scene.input.*;
import javafx.scene.shape.Circle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ija.scheme.canvas.wire.Wire;
import ija.scheme.box.FillValueBox;
import javafx.scene.shape.Polygon;

import static ija.scheme.menubar.MenuBar.ACTUAL_BLOCK_COLOR;

/**
 * Třída reprezentující jednotlivé porty, které jsou nedílnou součástí bloků
 *
 * @author Radek Hůlka
 * @author Pavel Janko
 */
public class Port extends Circle
{
    final static int PORT_RADIUS = 7;
    final static Color WIRELESS_UNSET_IN_PORT_COLOR = Color.RED;
    public final static Color WIRELESS_SET_IN_PORT_COLOR = Color.GREEN;
    final static Color WIRELESS_OUT_PORT_COLOR = Color.BLUE;
    final static Color WIRED_IN_OUT_PORT_COLOR = Color.BLACK;
    final static Color ON_MOUSE_IN_OUT_PORT_COLOR = Color.YELLOW;

    private int index;
    private String portType;
    private AbstractType dataType;
    private AbstractBlock parent;
    private Wire wire;

    /**
     * Konstruktor sloužící k vytvoření portu, nastavení konkrétních datových typů, zpracování událostí
     * a přidání k bloku.
     *
     * @param x x-ová souřadnice portu
     * @param y y-ová souřadnice portu
     * @param index index portu
     * @param portType typ portu
     * @param dataType datový typ portu
     * @param parent rodičovský blok portu
     */
    Port(double x, double y, int index, String portType, String dataType, AbstractBlock parent) {
        this.index = index;
        this.portType = portType;
        this.parent = parent;

        this.setCenterX(x);
        this.setCenterY(y);
        this.setRadius(PORT_RADIUS);

        if (this.portType.equals("in")) {
            this.setFill(WIRELESS_UNSET_IN_PORT_COLOR);
        } else {
            this.setFill(WIRELESS_OUT_PORT_COLOR);
        }

        // vytvoreni typu portu
        try {
            Class<?> classHelper = Class.forName(
                    "ija.scheme.canvas.type." + dataType.replaceAll("\\s+", ""));
            Constructor<?> constructor = classHelper.getConstructor();
            Object instance = constructor.newInstance();
            this.dataType = (AbstractType) instance;
        } catch (ClassNotFoundException | NoSuchMethodException
                | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        this.setOnDragDetected((MouseEvent event) -> {
            boolean stepCalculations = false;

            int k = 0;
            Canvas canvas = (Canvas) (this.getParent().getParent());
            while (k < canvas.getChildren().size() && !stepCalculations) {
                if (canvas.getChildren().get(k).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                    for (int j = 0; j < ((AbstractBlock) canvas.getChildren().get(k)).getChildren().size(); j++) {
                        if (((AbstractBlock) canvas.getChildren().get(k)).getChildren().get(j).getClass().getName().equals("javafx.scene.shape.Polygon")) {
                            if (((Color) ((Polygon) ((AbstractBlock) canvas.getChildren().get(k)).getChildren().get(j)).getStroke()) == ACTUAL_BLOCK_COLOR) {
                                stepCalculations = true;
                            }
                        }
                    }
                }
                k++;
            }

            if (!stepCalculations) {
                Dragboard dragboard = this.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString("PORT_DRAG");
                dragboard.setContent(clipboardContent);
            }
            event.consume();
        });

        this.setOnDragDropped((DragEvent event) -> {
            Dragboard dragboard = event.getDragboard();

            if (dragboard.getString().equals("PORT_DRAG")) {
                Port port = (Port) event.getGestureSource();

                if ((port.getParent() != this.getParent() && !port.getPortType().equals(this.getPortType())) && port.getDataType().equals(this.getDataType()) && this.getWire() == null) {
                    Wire newWire = new Wire(port.getWire());
                    newWire.setPort(this);
                    ((Canvas) port.getWire().getParent()).getChildren().add(newWire);
                    port.getWire().clearPorts();
                    port.setWire(newWire);

                    AbstractBlock portParent = (AbstractBlock) this.getParent();

                    if (this.getPortType().equals("in")) {
                        newWire.setEndX(portParent.getLocation().getX() + this.getCenterX());
                        newWire.setEndY(portParent.getLocation().getY() + this.getCenterY());
                    } else {
                        newWire.setEndX(portParent.getLocation().getX() + portParent.getWidth() +
                                this.getCenterX());
                        newWire.setEndY(portParent.getLocation().getY() + portParent.getWidth() +
                                this.getCenterY());
                    }

                    // nastaveni, aby start dratu byl vzdy na in portu
                    if (this.portType.equals("in")) {
                        double endX = port.getWire().getEndX();
                        double endY = port.getWire().getEndY();
                        port.getWire().setEndX(port.getWire().getStartX());
                        port.getWire().setEndY(port.getWire().getStartY());
                        port.getWire().setStartX(endX);
                        port.getWire().setStartY(endY);
                    }

                    this.setWire(newWire);

                    port.setFill(WIRED_IN_OUT_PORT_COLOR);
                    this.setFill(WIRED_IN_OUT_PORT_COLOR);
                    checkPortsColor((Canvas) port.getWire().getParent());
                } else {
                    port.getWire().clearPorts();
                    port.wire = null;
                }
            }

            event.setDropCompleted(true);
            event.consume();
        });

        this.setOnDragOver((DragEvent event) -> {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });

        this.setOnDragEntered((DragEvent event) -> {
            this.setFill(ON_MOUSE_IN_OUT_PORT_COLOR);
            event.consume();
        });

        this.setOnDragExited((DragEvent event) -> {
            if (this.getWire() != null) {
                this.setFill(WIRED_IN_OUT_PORT_COLOR);
            } else if (this.dataType.getValuesSet()) {
                this.setFill(WIRELESS_SET_IN_PORT_COLOR);
            } else if (!this.dataType.getValuesSet()) {
                if (this.portType.equals("in")) {
                    this.setFill(WIRELESS_UNSET_IN_PORT_COLOR);
                } else {
                    this.setFill(WIRELESS_OUT_PORT_COLOR);
                }
            }
            event.consume();
        });

        // rightclick pro zadani hodnot na portu
        this.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY && this.portType.equals("in") && this.getWire() == null) {
                FillValueBox.display(this.dataType);
            }
            event.consume();
        });

        this.setOnMouseEntered((MouseEvent event) -> {
            this.setFill(ON_MOUSE_IN_OUT_PORT_COLOR);
            Tooltip t = new Tooltip(getValues(this));
            Tooltip.install(this, t);
            event.consume();
        });

        this.setOnMouseExited((MouseEvent event) -> {
            if (this.getWire() != null) {
                this.setFill(WIRED_IN_OUT_PORT_COLOR);
            } else if (this.dataType.getValuesSet()) {
                this.setFill(WIRELESS_SET_IN_PORT_COLOR);
            } else if (!this.dataType.getValuesSet()) {
                if (this.portType.equals("in")) {
                    this.setFill(WIRELESS_UNSET_IN_PORT_COLOR);
                } else {
                    this.setFill(WIRELESS_OUT_PORT_COLOR);
                }
            }
            event.consume();
        });
    }

    /**
     * @param port port, jehož hodnoty se mají zjišťovat
     * @return Vrací řetězcový tvar hodnot spojených s typem portu
     */
    public String getValues(Port port) {
        String s = port.getDataAbstractType().getValueName(0);
        s += ": ";
        s += String.valueOf(port.getValue(0));
        for (int i = 1; i < port.getValuesLength(); i++) {
            s += "\n";
            s += port.getDataAbstractType().getValueName(i);
            s += ": ";
            s += String.valueOf(port.getValue(i));
        }
        return s;
    }

    /**
     * Zkontroluje barvy portů a korektně je nastaví.
     *
     * @param canvas kanvas, jehož děti mají být prohledány
     */
    public void checkPortsColor(Canvas canvas) {
        for (int i = 0; i < canvas.getChildren().size(); i++) {
            if (canvas.getChildren().get(i).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                for (int j = 0; j < ((AbstractBlock) canvas.getChildren().get(i)).getInPortsCount(); j++) {
                    if (((AbstractBlock) canvas.getChildren().get(i)).getInPort(j).getWire() != null) {
                        ((AbstractBlock) canvas.getChildren().get(i)).getInPort(j).setFill(WIRED_IN_OUT_PORT_COLOR);
                    } else if (((AbstractBlock) canvas.getChildren().get(i)).getInPort(j).dataType.getValuesSet()) {
                        ((AbstractBlock) canvas.getChildren().get(i)).getInPort(j).setFill(WIRELESS_SET_IN_PORT_COLOR);
                    } else if (!((AbstractBlock) canvas.getChildren().get(i)).getInPort(j).dataType.getValuesSet()) {
                        if (((AbstractBlock) canvas.getChildren().get(i)).getInPort(j).portType.equals("in")) {
                            ((AbstractBlock) canvas.getChildren().get(i)).getInPort(j).setFill(WIRELESS_UNSET_IN_PORT_COLOR);
                        } else {
                            ((AbstractBlock) canvas.getChildren().get(i)).getInPort(j).setFill(WIRELESS_OUT_PORT_COLOR);
                        }
                    }
                }
                for (int j = 0; j < ((AbstractBlock) canvas.getChildren().get(i)).getOutPortsCount(); j++) {
                    if (((AbstractBlock) canvas.getChildren().get(i)).getOutPort(j).getWire() != null) {
                        ((AbstractBlock) canvas.getChildren().get(i)).getOutPort(j).setFill(WIRED_IN_OUT_PORT_COLOR);
                    } else if (((AbstractBlock) canvas.getChildren().get(i)).getOutPort(j).dataType.getValuesSet()) {
                        ((AbstractBlock) canvas.getChildren().get(i)).getOutPort(j).setFill(WIRELESS_SET_IN_PORT_COLOR);
                    } else if (!((AbstractBlock) canvas.getChildren().get(i)).getOutPort(j).dataType.getValuesSet()) {
                        if (((AbstractBlock) canvas.getChildren().get(i)).getOutPort(j).portType.equals("in")) {
                            ((AbstractBlock) canvas.getChildren().get(i)).getOutPort(j).setFill(WIRELESS_UNSET_IN_PORT_COLOR);
                        } else {
                            ((AbstractBlock) canvas.getChildren().get(i)).getOutPort(j).setFill(WIRELESS_OUT_PORT_COLOR);
                        }
                    }
                }
            }
        }
    }

    /**
     * @return Vrací index portu.
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * @return Vrací typ portu.
     */
    public String getPortType() {
        return this.portType;
    }

    /**
     * @param index index hodnoty v poli hodnot
     * @return Vrací hodnotu portu na zadaném indexu.
     */
    public double getValue(int index) {
        return this.dataType.getValue(index);
    }

    /**
     * Nastaví novou hodnotu v poli hodnot podle indexu.
     *
     * @param index index přepisované hodnoty v poli hodnot
     * @param newValue nová hodnota, na kterou se má stará přepsat
     */
    public void setValue(int index, double newValue) {
        this.dataType.setValue(index, newValue);
    }

    /**
     * @return Vrací název datového typu portu.
     */
    public String getDataType() {
        return this.dataType.getType();
    }

    /**
     * @return Vrací instanci datového typu portu.
     */
    public AbstractType getDataAbstractType() {
        return this.dataType;
    }

    /**
     * @return Vrací počet hodnot na portu.
     */
    public int getValuesLength() {
        return this.dataType.getValuesLength();
    }

    /**
     * @return Vrací instanci drátu připojeného k portu.
     */
    public Wire getWire() {
        return this.wire;
    }

    /**
     * Nastavuje drát připojený k portu.
     *
     * @param wire drát, který se má k portu připojit
     */
    public void setWire(Wire wire) {
        this.wire = wire;
    }

    /**
     * @return Vrací hašový kód, sloužící pro jednoznačnou identifikaci portu.
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (int) parent.location.getX();
        result = 31 * result + (int) parent.location.getY();
        result = 31 * result + index;
        result = 31 * result + (int) this.getCenterX();
        result = 31 * result + (int) this.getCenterY();
        return result;
    }
}
