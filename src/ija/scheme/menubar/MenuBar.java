package ija.scheme.menubar;

import ija.scheme.canvas.wire.Wire;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import ija.scheme.box.*;
import ija.scheme.canvas.*;
import ija.scheme.canvas.Canvas;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static ija.scheme.box.AlertBox.*;
import static ija.scheme.canvas.AbstractBlock.BLOCK_COLOR;

/**
 * Třída sloužící pro práci s kontextovou nabídkou v horní části aplikace.
 *
 * @author Radek Hůlka
 * @author Pavel Janko
 */
public class MenuBar extends javafx.scene.control.MenuBar
{
    public static javafx.scene.control.MenuBar menuBar;
    private int runMenuIndex;
    private int fileMenuIndex;
    private int saveMenuItemIndex;

    public final static Color ACTUAL_BLOCK_COLOR = Color.YELLOW;
    private final static Color NOT_CALCULATED_BLOCK_COLOR = Color.RED;
    private final static Color CALCULATED_BLOCK_COLOR = Color.GREEN;
    private final static Color LAST_CALCULATED_BLOCK_COLOR = Color.BLUE;

    /**
     * Konstruktor volající funkce pro vytvoření nové instance nabídky a následně nastavení funkcionality
     * při použití jednotlivých tlačítek.
     *
     * @param canvas kanvas, se kterým aplikace pracuje
     */
    public MenuBar(Canvas canvas) {
        createMenuBar();
        setOnActions(canvas);
    }

    /**
     * Vytvoření nové instance nabídky a naplnění tlačítky.
     */
    private void createMenuBar() {
        this.menuBar = new javafx.scene.control.MenuBar();

        MenuItem itemNew = new MenuItem("New");
        itemNew.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));

        MenuItem itemSave = new MenuItem("Save");
        itemSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));

        MenuItem itemImport = new MenuItem("Import");
        itemImport.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN));

        MenuItem itemCalculate = new MenuItem("Calculate");
        itemCalculate.setAccelerator(new KeyCodeCombination(KeyCode.F1, KeyCombination.SHORTCUT_DOWN));

        MenuItem itemStepCalculation = new MenuItem("Step Calculation");
        itemStepCalculation.setAccelerator(new KeyCodeCombination(KeyCode.F2, KeyCombination.SHORTCUT_DOWN));

        Menu menuFile = new Menu("_File");
        menuFile.setMnemonicParsing(true);
        saveMenuItemIndex = menuFile.getItems().size() + 1;
        menuFile.getItems().addAll(itemNew, itemSave, itemImport);
        fileMenuIndex = this.menuBar.getMenus().size();
        this.menuBar.getMenus().addAll(menuFile);

        Menu menuRun = new Menu("_Run");
        menuRun.setMnemonicParsing(true);
        menuRun.getItems().addAll(itemCalculate, itemStepCalculation);
        runMenuIndex = this.menuBar.getMenus().size();
        this.menuBar.getMenus().addAll(menuRun);

        Label menuHelpLabel = new Label("Help");
        menuHelpLabel.setOnMouseClicked(event -> {
            AlertBox.display(HELP_BOX_WIDTH, HELP_BOX_TITLE, HELP_BOX_DESCRIPTION); event.consume();
        });
        Menu menuHelp = new Menu();
        menuHelp.setGraphic(menuHelpLabel);
        menuHelp.setMnemonicParsing(true);
        this.menuBar.getMenus().addAll(menuHelp);

    }

    /**
     * Nastavení funkcionality jednotlivých tlačítek nabídky.
     *
     * @param canvas kanvas, se kterým aplikace pracuje
     */
    private void setOnActions(Canvas canvas) {
        // New actions
        this.menuBar.getMenus().get(0).getItems().get(0).setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Are you sure?");
            alert.setHeaderText("Do you really want to delete this block scheme and clear the canvas?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                canvas.getChildren().clear();
                canvas.getChildren().add(canvas.canvasWireHelper);
            }
        });

        // Save actions
        this.menuBar.getMenus().get(0).getItems().get(1).setOnAction(event -> {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder;

            try {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }

            Document document = documentBuilder.newDocument();
            Element rootElement = document.createElement("canvas");
            document.appendChild(rootElement);

            for (Node canvasChild : canvas.getChildren()) {
                if (canvasChild instanceof AbstractBlock) {
                    Element blockElement = document.createElement("block");
                    Attr blockType = document.createAttribute("type");
                    blockType.setValue(canvasChild.getClass().getName());
                    blockElement.setAttributeNode(blockType);

                    blockElement.setAttribute("x-pos", String.valueOf(((AbstractBlock) canvasChild).getLocation().getX()));
                    blockElement.setAttribute("y-pos", String.valueOf(((AbstractBlock) canvasChild).getLocation().getY()));

                    for (Node blockChild : ((AbstractBlock) canvasChild).getChildren()) {
                        if (blockChild instanceof Port) {
                            Element portElement;

                            if (((Port) blockChild).getPortType().equals("in"))
                                portElement = document.createElement("in-port");
                            else
                                portElement = document.createElement("out-port");

                            Attr portId = document.createAttribute("id");
                            portId.setValue(String.valueOf(blockChild.hashCode()));
                            portElement.setAttributeNode(portId);

                            for (int i = 0; i < ((Port) blockChild).getValuesLength(); i++) {
                                Element portValue = document.createElement("value");
                                portValue.setTextContent(String.valueOf(((Port) blockChild).getValue(i)));

                                if (((Port) blockChild).getDataAbstractType().getValuesSet())
                                    portValue.setAttribute("set", "true");

                                portElement.appendChild(portValue);
                            }

                            blockElement.appendChild(portElement);
                        }
                    }
                    rootElement.appendChild(blockElement);
                } else if (canvasChild instanceof Wire && ((Wire) canvasChild).getInPort() != null
                        && ((Wire) canvasChild).getOutPort() != null) {
                    Element wireElement = document.createElement("wire");
                    wireElement.setAttribute("in-port", String.valueOf(((Wire) canvasChild).getInPort().hashCode()));
                    wireElement.setAttribute("out-port", String.valueOf(((Wire) canvasChild).getOutPort().hashCode()));
                    rootElement.appendChild(wireElement);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = null;

            try {
                transformer = transformerFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException(e);
            }

            DOMSource source = new DOMSource(document);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Block Editor Schema", "*.bes"),
                    new FileChooser.ExtensionFilter("All Files", "*"));
            File fXmlFile = fileChooser.showSaveDialog(canvas.getScene().getWindow());

            if (fXmlFile != null) {
                StreamResult result = new StreamResult(fXmlFile);

                try {
                    transformer.transform(source, result);
                } catch (TransformerException e) {
                    throw new RuntimeException(e);
                }
            }

            event.consume();
        });

        // Import actions
        this.menuBar.getMenus().get(0).getItems().get(2).setOnAction(event -> {
            canvas.getChildren().clear();
            canvas.getChildren().add(canvas.canvasWireHelper);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Block Editor Schema", "*.bes"),
                    new FileChooser.ExtensionFilter("All Files", "*"));

            File fXmlFile = fileChooser.showOpenDialog(canvas.getScene().getWindow());

            if (fXmlFile != null) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = null;
                Document document = null;

                try {
                    documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    document = documentBuilder.parse(fXmlFile);
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    throw new RuntimeException(e);
                }

                document.normalize();
                NodeList nodeList = document.getElementsByTagName("block");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    org.w3c.dom.Node node = nodeList.item(i);

                    if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        Element element = (Element) node;

                        try {
                            Class<?> classHelper = Class.forName(element.getAttribute("type"));
                            Constructor<?> constructor = classHelper.getConstructor(double.class, double.class,
                                    double.class, double.class);
                            Object instance = constructor.newInstance(
                                    Double.parseDouble(element.getAttribute("x-pos")),
                                    Double.parseDouble(element.getAttribute("y-pos")),
                                    canvas.getWidth(), canvas.getHeight()
                            );

                            AbstractBlock importedBlock = (AbstractBlock) instance;
                            NodeList portList = ((Element) node).getElementsByTagName("in-port");

                            for (int j = 0; j < portList.getLength(); j++) {
                                org.w3c.dom.Node port = portList.item(j);
                                NodeList valueList = ((Element) port).getElementsByTagName("value");

                                for (int k = 0; k < valueList.getLength(); k++) {
                                    importedBlock.getInPort(j).setValue(k, Double.parseDouble(valueList.item(k).getTextContent()));

                                    if (((Element) valueList.item(k)).hasAttribute("set"))
                                        importedBlock.getInPort(j).getDataAbstractType().setValuesSet(true);
                                }
                            }

                            portList = ((Element) node).getElementsByTagName("out-port");

                            for (int j = 0; j < portList.getLength(); j++) {
                                org.w3c.dom.Node port = portList.item(j);
                                NodeList valueList = ((Element) port).getElementsByTagName("value");

                                for (int k = 0; k < valueList.getLength(); k++) {
                                    importedBlock.getOutPort(j).setValue(k, Double.parseDouble(valueList.item(k).getTextContent()));

                                    if (((Element) valueList.item(k)).hasAttribute("set"))
                                        importedBlock.getOutPort(j).getDataAbstractType().setValuesSet(true);
                                }
                            }

                            canvas.getChildren().add(importedBlock);
                        } catch (ClassNotFoundException | NoSuchMethodException
                                | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                nodeList = document.getElementsByTagName("wire");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    org.w3c.dom.Node node = nodeList.item(i);

                    if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        Wire wire = new Wire();

                        for (Node canvasChild : canvas.getChildren()) {
                            if (canvasChild instanceof AbstractBlock) {
                                for (Node blockChild : ((AbstractBlock) canvasChild).getChildren()) {
                                    if (blockChild instanceof Port) {
                                        if (blockChild.hashCode() == Integer.valueOf(element.getAttribute("in-port"))) {
                                            wire.setStartX(((AbstractBlock) canvasChild).getLocation().getX() +
                                                    ((Port) blockChild).getCenterX());
                                            wire.setStartY(((AbstractBlock) canvasChild).getLocation().getY() +
                                                    ((Port) blockChild).getCenterY());

                                            ((Port) blockChild).setWire(wire);
                                            wire.setPort((Port) blockChild);
                                            ((Port) blockChild).checkPortsColor(canvas);
                                        } else if (blockChild.hashCode() == Integer.valueOf(element.getAttribute("out-port"))) {
                                            wire.setEndX(((AbstractBlock) canvasChild).getLocation().getX() +
                                                    ((AbstractBlock) canvasChild).getWidth() +
                                                    ((Port) blockChild).getCenterX());
                                            wire.setEndY(((AbstractBlock) canvasChild).getLocation().getY() +
                                                    ((AbstractBlock) canvasChild).getWidth() +
                                                    ((Port) blockChild).getCenterY());

                                            ((Port) blockChild).setWire(wire);
                                            wire.setPort((Port) blockChild);
                                            ((Port) blockChild).checkPortsColor(canvas);
                                        }
                                    }
                                }
                            }
                        }

                        canvas.getChildren().add(wire);
                    }
                }
            }
            event.consume();
        });

        // Calculate actions
        this.menuBar.getMenus().get(1).getItems().get(0).setOnAction(event -> {
            if (findCycles(canvas)) {
                AlertBox.display(CYCLES_DETECTED_BOX_WIDTH, CYCLES_DETECTED_BOX_TITLE, CYCLES_DETECTED_BOX_DESCRIPTION);
            } else {
                if (checkWirelessInPorts(canvas)) {
                    setBlocksNotCalculated(canvas);
                    caltulate(canvas);
                    AlertBox.display(CALCULATIONS_DONE_BOX_WIDTH, CALCULATIONS_DONE_BOX_TITLE, CALCULATIONS_DONE_BOX_DESCRIPTION);
                } else {
                    AlertBox.display(NOT_FILLED_INPORT_VALUES_BOX_WIDTH, NOT_FILLED_INPORT_VALUES_BOX_TITLE, NOT_FILLED_INPORT_VALUES_BOX_DESCRIPTION);
                }
            }
            event.consume();
        });

        // Step calculation actions
        this.menuBar.getMenus().get(1).getItems().get(1).setOnAction(event -> {
            if (findCycles(canvas)) {
                AlertBox.display(CYCLES_DETECTED_BOX_WIDTH, CYCLES_DETECTED_BOX_TITLE, CYCLES_DETECTED_BOX_DESCRIPTION);
            } else {
                if (checkWirelessInPorts(canvas)) {
                    setBlocksForStepCalculations(canvas, NOT_CALCULATED_BLOCK_COLOR);
                    setBlocksNotCalculated(canvas);
                    setNextBlockForStepCalculations(canvas);
                    if (!checkEndOfStepCalculations(canvas)) {

                        // Next step - menu + actions
                        Label menuNextStepLabel = new Label("Next step");
                        menuNextStepLabel.setOnMouseClicked(event1 -> {
                            AbstractBlock nextBlock = getNextBlockForStepCalculations(canvas);
                            nextBlock.doCalculation();
                            sentResultsByWire(nextBlock);
                            nextBlock.setCalculated(true);
                            for (int j = 0; j < nextBlock.getChildren().size(); j++) {
                                if (nextBlock.getChildren().get(j).getClass().getName().equals("javafx.scene.shape.Polygon")) {
                                    setPreviousLastCalculatedBlockColor(canvas);
                                    ((Polygon) nextBlock.getChildren().get(j)).setStroke(LAST_CALCULATED_BLOCK_COLOR);
                                }
                            }
                            setNextBlockForStepCalculations(canvas);
                            if (checkEndOfStepCalculations(canvas)) {
                                setBlocksForStepCalculations(canvas, BLOCK_COLOR);
                                removeStepCalculationsAndEnableRunMenu();
                                AlertBox.display(CALCULATIONS_DONE_BOX_WIDTH, CALCULATIONS_DONE_BOX_TITLE, CALCULATIONS_DONE_BOX_DESCRIPTION);
                            }
                            event1.consume();
                        });
                        Menu menuNextStep = new Menu();
                        menuNextStep.setGraphic(menuNextStepLabel);
                        menuNextStep.setMnemonicParsing(true);
                        MenuBar.menuBar.getMenus().addAll(menuNextStep);

                        // Finish calculations - menu + actions
                        Label menuFinishLabel = new Label("Finish calculations");
                        menuFinishLabel.setOnMouseClicked(event2 -> {
                            caltulate(canvas);
                            setBlocksForStepCalculations(canvas, BLOCK_COLOR);
                            removeStepCalculationsAndEnableRunMenu();
                            AlertBox.display(CALCULATIONS_DONE_BOX_WIDTH, CALCULATIONS_DONE_BOX_TITLE, CALCULATIONS_DONE_BOX_DESCRIPTION);
                            event2.consume();
                        });
                        Menu menuFinish = new Menu();
                        menuFinish.setGraphic(menuFinishLabel);
                        menuFinish.setMnemonicParsing(true);
                        MenuBar.menuBar.getMenus().addAll(menuFinish);

                        // End calculations - menu + actions
                        Label menuEndLabel = new Label("End calculations");
                        menuEndLabel.setOnMouseClicked(event3 -> {
                            setBlocksForStepCalculations(canvas, BLOCK_COLOR);
                            removeStepCalculationsAndEnableRunMenu();
                            event3.consume();
                        });
                        Menu menuEnd = new Menu();
                        menuEnd.setGraphic(menuEndLabel);
                        menuEnd.setMnemonicParsing(true);
                        MenuBar.menuBar.getMenus().addAll(menuEnd);

                        // disable run...
                        MenuBar.menuBar.getMenus().get(runMenuIndex).setDisable(true);
                        MenuBar.menuBar.getMenus().get(fileMenuIndex).getItems().get(saveMenuItemIndex).setDisable(true);

                    } else {
                        AlertBox.display(CALCULATIONS_DONE_BOX_WIDTH, CALCULATIONS_DONE_BOX_TITLE, CALCULATIONS_DONE_BOX_DESCRIPTION);
                    }
                } else {
                    AlertBox.display(NOT_FILLED_INPORT_VALUES_BOX_WIDTH, NOT_FILLED_INPORT_VALUES_BOX_TITLE, NOT_FILLED_INPORT_VALUES_BOX_DESCRIPTION);
                }
            }
            event.consume();
        });
    }

    /**
     * Odstraní nabídku pro krokování a povolí spuštění.
     */
    private void removeStepCalculationsAndEnableRunMenu() {
        MenuBar.menuBar.getMenus().remove(MenuBar.menuBar.getMenus().size() - 1);
        MenuBar.menuBar.getMenus().remove(MenuBar.menuBar.getMenus().size() - 1);
        MenuBar.menuBar.getMenus().remove(MenuBar.menuBar.getMenus().size() - 1);
        MenuBar.menuBar.getMenus().get(runMenuIndex).setDisable(false);
        MenuBar.menuBar.getMenus().get(fileMenuIndex).getItems().get(saveMenuItemIndex).setDisable(false);
    }

    /**
     * @param canvas kanvas, se kterým aplikace pracuje
     * @return Vrací informaci o tom, zdali mají všechny in-porty, ve kterých není drát, vyplněné hodnoty.
     */
    private boolean checkWirelessInPorts(Canvas canvas) {
        for (int i = 0; i < canvas.getChildren().size(); i++) {
            if (canvas.getChildren().get(i).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                for (int j = 0; j < ((AbstractBlock) canvas.getChildren().get(i)).getInPortsCount(); j++) {
                    if (((AbstractBlock) canvas.getChildren().get(i)).getInPort(j).getWire() == null) {
                        if (!(((AbstractBlock) canvas.getChildren().get(i)).getInPort(j).getDataAbstractType().getValuesSet())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Provede výpočet blokového schématu.
     *
     * @param canvas kanvas, se kterým aplikace pracuje
     */
    private void caltulate(Canvas canvas) {
        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (int i = 0; i < canvas.getChildren().size(); i++) {
                if (canvas.getChildren().get(i).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                    if (!(((AbstractBlock) canvas.getChildren().get(i)).getCalculated())) {
                        if (readyForCalculation((AbstractBlock) canvas.getChildren().get(i))) {
                            ((AbstractBlock) canvas.getChildren().get(i)).doCalculation();
                            sentResultsByWire((AbstractBlock) canvas.getChildren().get(i));
                            ((AbstractBlock) canvas.getChildren().get(i)).setCalculated(true);
                        } else {
                            allDone = false;
                        }
                    }
                }
            }
        }
    }

    /**
     * @param block blok, jehož připravenost se má zjišťovat
     * @return Vrací informaci o tom, zdali je daný blok připraven k výpočtu.
     */
    private boolean readyForCalculation(AbstractBlock block) {
        for (int i = 0; i < block.getInPortsCount(); i++) {
            if (block.getInPort(i).getWire() != null) {
                if (!(((AbstractBlock) block.getInPort(i).getWire().getOtherPort(block.getInPort(i)).getParent()).getCalculated())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Nastaví pro všechny bloky informaci o tom, že nejsou spočítány.
     *
     * @param canvas kanvas, se kterým aplikace pracuje
     */
    private void setBlocksNotCalculated(Canvas canvas) {
        for (int i = 0; i < canvas.getChildren().size(); i++) {
            if (canvas.getChildren().get(i).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                ((AbstractBlock) canvas.getChildren().get(i)).setCalculated(false);
            }
        }
    }

    /**
     * Při krokování nastaví všechny bloky jako nevýpočítané.
     *
     * @param canvas kanvas, se kterým aplikace pracuje
     * @param color barva, na kterou se mají bloky nastavit
     */
    private void setBlocksForStepCalculations(Canvas canvas, Color color) {
        for (int i = 0; i < canvas.getChildren().size(); i++) {
            if (canvas.getChildren().get(i).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                for (int j = 0; j < ((AbstractBlock) canvas.getChildren().get(i)).getChildren().size(); j++) {
                    if (((AbstractBlock) canvas.getChildren().get(i)).getChildren().get(j).getClass().getName().equals("javafx.scene.shape.Polygon")) {
                        ((Polygon) ((AbstractBlock) canvas.getChildren().get(i)).getChildren().get(j)).setStroke(color);
                    }
                }
            }
        }
    }

    /**
     * @param canvas kanvas, se kterým aplikace pracuje
     * @return Vrací blok, který se má propočítat.
     */
    private AbstractBlock getNextBlockForStepCalculations(Canvas canvas) {
        boolean blockFound = false;
        AbstractBlock block = null;
        int k = 0;
        while (k < canvas.getChildren().size() && !blockFound) {
            if (canvas.getChildren().get(k).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                for (int j = 0; j < ((AbstractBlock) canvas.getChildren().get(k)).getChildren().size(); j++) {
                    if (((AbstractBlock) canvas.getChildren().get(k)).getChildren().get(j).getClass().getName().equals("javafx.scene.shape.Polygon")) {
                        if (((Color) ((Polygon) ((AbstractBlock) canvas.getChildren().get(k)).getChildren().get(j)).getStroke()) == ACTUAL_BLOCK_COLOR) {
                            block = (AbstractBlock) canvas.getChildren().get(k);
                            blockFound = true;
                        }
                    }
                }
            }
            k++;
        }
        return block;
    }

    /**
     * Nastaví poslední spočítaný blok na modrou barvu
     *
     * @param canvas kanvas, se kterým aplikace pracuje
     */
    private void setPreviousLastCalculatedBlockColor(Canvas canvas) {
        for (int i = 0; i < canvas.getChildren().size(); i++) {
            if (canvas.getChildren().get(i).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                for (int j = 0; j < ((AbstractBlock) canvas.getChildren().get(i)).getChildren().size(); j++) {
                    if (((AbstractBlock) canvas.getChildren().get(i)).getChildren().get(j).getClass().getName().equals("javafx.scene.shape.Polygon")) {
                        if (((Color) ((Polygon) ((AbstractBlock) canvas.getChildren().get(i)).getChildren().get(j)).getStroke()) == LAST_CALCULATED_BLOCK_COLOR) {
                            ((Polygon) ((AbstractBlock) canvas.getChildren().get(i)).getChildren().get(j)).setStroke(CALCULATED_BLOCK_COLOR);
                        }
                    }
                }
            }
        }
    }

    /**
     * Určí blok, který se má spočítat jako následující.
     *
     * @param canvas kanvas, se kterým aplikace pracuje
     */
    private void setNextBlockForStepCalculations(Canvas canvas) {
        boolean blockMarked = false;
        for (int i = 0; i < canvas.getChildren().size(); i++) {
            if (canvas.getChildren().get(i).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                for (int j = 0; j < ((AbstractBlock) canvas.getChildren().get(i)).getChildren().size(); j++) {
                    if (((AbstractBlock) canvas.getChildren().get(i)).getChildren().get(j).getClass().getName().equals("javafx.scene.shape.Polygon")) {
                        if (((Color) ((Polygon) ((AbstractBlock) canvas.getChildren().get(i)).getChildren().get(j)).getStroke()) == NOT_CALCULATED_BLOCK_COLOR) {
                            if (readyForCalculation((AbstractBlock) canvas.getChildren().get(i))) {
                                ((Polygon) ((AbstractBlock) canvas.getChildren().get(i)).getChildren().get(j)).setStroke(ACTUAL_BLOCK_COLOR);
                                blockMarked = true;
                            }
                        }
                    }
                }
                if (blockMarked) {
                    i = canvas.getChildren().size();
                }
            }
        }
    }

    /**
     * @param canvas kanvas, se kterým aplikace pracuje
     * @return Vrací informaci o tom, zdali jsou všechny bloky spočítané.
     */
    private boolean checkEndOfStepCalculations(Canvas canvas) {
        for (int i = 0; i < canvas.getChildren().size(); i++) {
            if (canvas.getChildren().get(i).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                for (int j = 0; j < ((AbstractBlock) canvas.getChildren().get(i)).getChildren().size(); j++) {
                    if (((AbstractBlock) canvas.getChildren().get(i)).getChildren().get(j).getClass().getName().equals("javafx.scene.shape.Polygon")) {
                        if (((Color) ((Polygon) ((AbstractBlock) canvas.getChildren().get(i)).getChildren().get(j)).getStroke()) == ACTUAL_BLOCK_COLOR) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Pošle hodnoty z výstupního portu na vstupní port jiného bloku, se kterým je spojen drátem.
     *
     * @param block blok, ze kterého se mají hodnoty poslat
     */
    private void sentResultsByWire(AbstractBlock block) {
        for (int i = 0; i < block.getOutPortsCount(); i++) {
            if (block.getOutPort(i).getWire() != null) {
                for (int j = 0; j < block.getOutPort(i).getValuesLength(); j++) {
                    block.getOutPort(i).getWire().getOtherPort(block.getOutPort(i)).setValue(j, block.getOutPort(i).getValue(j));
                }
            }
        }
    }

    /**
     * @param canvas kanvas, se kterým aplikace pracuje
     * @return Vrací informaci o tom, zdali se v kanvasu vyskytují cykly v propojení bloků.
     */
    private boolean findCycles(Canvas canvas) {
        for (int i = 0; i < canvas.getChildren().size(); i++) {
            if (canvas.getChildren().get(i).getClass().getSuperclass().getName().equals("ija.scheme.canvas.AbstractBlock")) {
                if (isInCycle((AbstractBlock) canvas.getChildren().get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param block blok, o kterém se má informace zjišťovat
     * @return Vráti informaci o tom, jestli je blok součástí cyklu.
     */
    private boolean isInCycle(AbstractBlock block) {
        for (int i = 0; i < block.getOutPortsCount(); i++) {
            if (block.getOutPort(i).getWire() != null) {
                if (isBlockInBranch(block, (AbstractBlock) block.getOutPort(i).getWire().getOtherPort(block.getOutPort(i)).getParent())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param block blok, o kterém se má informace zjišťovat
     * @param branch větev, ve které se blok může nacházet
     * @return Vrátí informaci o tom, zdali se blok nachází ve větvi
     */
    private boolean isBlockInBranch(AbstractBlock block, AbstractBlock branch) {
        if (block.equals(branch)) {
            return true;
        } else {
            if (isEndBlock(branch)) {
                return false;
            } else {
                for (int i = 0; i < branch.getOutPortsCount(); i++) {
                    if (branch.getOutPort(i).getWire() != null) {
                        if (isBlockInBranch(block, (AbstractBlock) branch.getOutPort(i).getWire().getOtherPort(branch.getOutPort(i)).getParent())) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }

    /**
     * @param block blok, o kterém se má informace zjišťovat
     * @return Vrátí informaci o tom, zdali je blok koncovým blokem
     */
    private boolean isEndBlock(AbstractBlock block) {
        for (int i = 0; i < block.getOutPortsCount(); i++) {
            if (block.getOutPort(i).getWire() != null) {
                return false;
            }
        }
        return true;
    }
}
