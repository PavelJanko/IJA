package ija.scheme.canvas.type;

import ija.scheme.canvas.AbstractType;

/**
 * Třída reprezentující typ portu "Output Chemicals"
 *
 * @author Radek Hůlka
 * @author Pavel Janko
 */
public class OutputChemicals extends AbstractType {

    private final static String TYPE_NAME = "Output Chemicals";
    private final static int NUMBER_OF_VALUES = 5;
    public final static String[] VALUES_NAMES = {"Water weight", "Carbon dioxide weight", "Hydrogenium weight", "Oxid weight", "Methan weight"};

    private double[] values;

    public OutputChemicals() {
        this.values = new double[NUMBER_OF_VALUES];
        this.values[0] = 0.0;
        this.values[1] = 0.0;
        this.values[2] = 0.0;
        this.values[3] = 0.0;
        this.values[4] = 0.0;
    }

    public double getValue(int index) {
        return this.values[index];
    }

    public void setValue(int index, double newValue) {
        this.values[index] = newValue;
    }

    public int getValuesLength() {
        return NUMBER_OF_VALUES;
    }

    public String getType() { return  TYPE_NAME; }

    public String getValueName(int index) {
        return VALUES_NAMES[index];
    }
}
