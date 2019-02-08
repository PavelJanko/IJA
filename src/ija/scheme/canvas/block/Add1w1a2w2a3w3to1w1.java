package ija.scheme.canvas.block;

import ija.scheme.canvas.AbstractBlock;
import javafx.geometry.Point2D;

/**
 * Třída reprezentující blok sčítačky, která má 3 vstupní porty s typy "One Decimal Number", "Two Decimal Numbers", "Three Decimal Numbers" a 1 výstupní port s typem "One Decimal Number". Provede sečtení Všech hodnot na vstupních portech a výsledek pošle na výstupní port.
 *
 * @author Radek Hůlka
 * @author Pavel Janko
 */
public class Add1w1a2w2a3w3to1w1 extends AbstractBlock
{
    private final static int IN_PORT_COUNT = 3;
    private final static String[] IN_PORTS_TYPES = {"One Decimal Number", "Two Decimal Numbers", "Three Decimal Numbers"};
    private final static String[] IN_PORTS_LABELS = {"1.1", "2.2", "3.3"};

    private final static int OUT_PORT_COUNT = 1;
    private final static String[] OUT_PORTS_TYPES = {"One Decimal Number"};
    private final static String[] OUT_PORTS_LABELS = {"1.1"};

    private final static int BLOCK_HEIGHT = 70;
    private final static int BLOCK_WIDTH = 120;
    private final static String BLOCK_NAME = "ADD";

    public Add1w1a2w2a3w3to1w1(double x, double y, double canvasWidth, double canvasHeight) {
        super.loadFXML(this, "add1w1a2w2a3w3to1w1.fxml");
        super.location = new Point2D(getLocX(x, canvasWidth, BLOCK_WIDTH), getLocY(y, canvasHeight, BLOCK_HEIGHT));
        super.constructBlock(this, BLOCK_WIDTH, BLOCK_HEIGHT, IN_PORT_COUNT, IN_PORTS_TYPES, IN_PORTS_LABELS, OUT_PORT_COUNT, OUT_PORTS_TYPES, OUT_PORTS_LABELS, BLOCK_NAME);
        super.relocate(super.location.getX() + super.getWidth(), super.location.getY() + super.getHeight());
        super.setHandlers(this);
    }

    public void doCalculation() {
        double result = super.getInPort(0).getValue(0) + super.getInPort(1).getValue(0) + super.getInPort(1).getValue(1) + super.getInPort(2).getValue(0) + super.getInPort(2).getValue(1) + super.getInPort(2).getValue(2);
        super.getOutPort(0).setValue(0, result);
    }

    public int getInPortsCount() {
        return IN_PORT_COUNT;
    }

    public int getOutPortsCount() {
        return OUT_PORT_COUNT;
    }

    public String[] getInPortsLabels() { return IN_PORTS_LABELS; }

    public String[] getOutPortsLabels() { return  OUT_PORTS_LABELS; }

    public String getBlockName() { return BLOCK_NAME; }
}
