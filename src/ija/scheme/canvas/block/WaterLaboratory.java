package ija.scheme.canvas.block;

import ija.scheme.canvas.AbstractBlock;
import javafx.geometry.Point2D;

/**
 * Třída reprezentující blok vodní laboratoře, která má 1 vstupní port typu "Input CHemicals" a 1 výstupní port typu "Output Chemicals". Provede zpracování vstupních chemikálií a výsledek pošle na výstupní port..
 *
 * @author Radek Hůlka
 * @author Pavel Janko
 */
public class WaterLaboratory extends AbstractBlock
{
    private final static int IN_PORT_COUNT = 1;
    private final static String[] IN_PORTS_TYPES = {"Input Chemicals"};
    private final static String[] IN_PORTS_LABELS = {"3 Chemicals"};

    private final static int OUT_PORT_COUNT = 1;
    private final static String[] OUT_PORTS_TYPES = {"Output Chemicals"};
    private final static String[] OUT_PORTS_LABELS = {"5 Chemicals"};

    private final static int BLOCK_HEIGHT = 45;
    private final static int BLOCK_WIDTH = 400;
    private final static String BLOCK_NAME = "Water laboratory";

    public WaterLaboratory(double x, double y, double canvasWidth, double canvasHeight) {
        super.loadFXML(this, "waterLaboratory.fxml");
        super.location = new Point2D(getLocX(x, canvasWidth, BLOCK_WIDTH), getLocY(y, canvasHeight, BLOCK_HEIGHT));
        super.constructBlock(this, BLOCK_WIDTH, BLOCK_HEIGHT, IN_PORT_COUNT, IN_PORTS_TYPES, IN_PORTS_LABELS, OUT_PORT_COUNT, OUT_PORTS_TYPES, OUT_PORTS_LABELS, BLOCK_NAME);
        super.relocate(super.location.getX() + super.getWidth(), super.location.getY() + super.getHeight());
        super.setHandlers(this);
    }

    public void doCalculation() {
        double relativeWeightH = 1.00784;
        double relativeWeightO = 15.9994;
        double relativeWeightC = 12.0107;
        double hydrogenium = super.getInPort(0).getValue(0);
        double oxid = super.getInPort(0).getValue(1);
        double methan = super.getInPort(0).getValue(2);
        double water = 0;
        double carbonDioxide = 0;
        if (hydrogenium / (4 * relativeWeightH) < oxid / (2 * relativeWeightO)) {
            water = hydrogenium;
            oxid = oxid - hydrogenium * ((2 * relativeWeightO) / (4 * relativeWeightH));
            hydrogenium = 0;
        } else {
            water = oxid;
            hydrogenium = hydrogenium - oxid * ((4 * relativeWeightH) / (2 * relativeWeightO));
        }

        if (methan / (4 * relativeWeightH + relativeWeightC) < oxid / (2 * relativeWeightO)) {
            water += methan * ((4 * relativeWeightH) / relativeWeightC);
            carbonDioxide = ((methan - methan * ((4 * relativeWeightH) / relativeWeightC)) / relativeWeightC) * (relativeWeightC + 2 * relativeWeightO);
            oxid = oxid - ((methan - methan * ((4 * relativeWeightH) / relativeWeightC)) / relativeWeightC) * (4 * relativeWeightO);
            methan = 0;
        } else {
            water += oxid / 2;
            carbonDioxide = ((oxid / 2) / (2 * relativeWeightO)) * (2 * relativeWeightO + relativeWeightC);
            methan = methan - ((oxid / 2) / (2 * relativeWeightO)) * (4 * relativeWeightH + relativeWeightC);
            oxid = 0;
        }
        super.getOutPort(0).setValue(0, water);
        super.getOutPort(0).setValue(1, carbonDioxide);
        super.getOutPort(0).setValue(2, hydrogenium);
        super.getOutPort(0).setValue(3, oxid);
        super.getOutPort(0).setValue(4, methan);
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
