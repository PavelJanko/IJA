package ija.scheme.canvas.type;

import ija.scheme.canvas.AbstractType;

/**
 * Třída reprezentující typ portu "One Decimal Number"
 *
 * @author Radek Hůlka
 * @author Pavel Janko
 */
public class OneDecimalNumber extends AbstractType {

    private final static String TYPE_NAME = "One Decimal Number";
    private final static int NUMBER_OF_VALUES = 1;
    private final static String[] VALUES_NAMES = {"Decimal number"};

    private double[] values;

    public OneDecimalNumber() {
        this.values = new double[NUMBER_OF_VALUES];
        this.values[0] = 0.0;
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
