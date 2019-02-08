package ija.scheme.canvas;

/**
 * Abstraktní třída sloužící k reprezentaci datových typů.
 *
 * @author Radek Hůlka
 */
public abstract class AbstractType {
    private boolean valuesSet = false;

    /**
     * @param index index hodnoty v poli
     * @return Vrací konkrétní hodnotu z pole hodnot.
     */
    abstract public double getValue(int index);

    /**
     * Přepíše předešlou hodnotu v poli podle indexu.
     *
     * @param index index hodnoty v poli
     * @param newValue hodnota, na kterou se má předešlá přepsat
     */
    abstract public void setValue(int index, double newValue);

    /**
     * @return Vrací délku pole hodnot.
     */
    abstract public int getValuesLength();

    /**
     * @return Vrací název datového typu.
     */
    abstract public String getType();

    /**
     * @param index index hodnoty v poli
     * @return Vrací název hodnoty z pole hodnot.
     */
    abstract public String getValueName(int index);

    /**
     * @return Vrací informaci o tom, zdali má typ nastavené hodnoty.
     */
    public boolean getValuesSet() {
        return this.valuesSet;
    }

    /**
     * Nastavuje informaci o vyplnění hodnot typu.
     *
     * @param value hodnota, která se má nastavit
     */
    public void setValuesSet(boolean value) {
        this.valuesSet = value;
    }

}
