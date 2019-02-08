package ija.scheme.canvas;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ija.scheme.canvas.wire.Wire;

import static ija.scheme.canvas.Port.PORT_RADIUS;

/**
 * Třída reprezentující plátno, na kterém se vytvářejí bloky.
 *
 * @author Radek Hůlka
 * @author Pavel Janko
 */
public class Canvas extends Pane
{
    public Wire canvasWireHelper;

    /**
     * Konstruktor třídy sloužící k načtení kostry kanvasu a
     * nastavení zpracovávání událostí.
     */
    public Canvas() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("canvas.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        canvasWireHelper = new Wire();
        this.getChildren().add(canvasWireHelper);

        this.setOnDragDropped((DragEvent event) -> {
            Dragboard dragboard = event.getDragboard();

            if (dragboard.getString().equals("BLOCK_DRAG")) {
                AbstractBlock block = (AbstractBlock) event.getGestureSource();
                block.relocate(getBlockNewX(block, event), getBlockNewY(block, event));
            } else if (dragboard.getString().equals("PORT_DRAG")) {
                Port port = (Port) event.getGestureSource();
                port.setWire(null);
                port.checkPortsColor(this);
                canvasWireHelper.clearPorts();
            } else {
                try {
                    Class<?> classHelper = Class.forName(
                            "ija.scheme.canvas.block." + dragboard.getString().replaceAll("\\s+", ""));
                    Constructor<?> constructor = classHelper.getConstructor(double.class, double.class,
                            double.class, double.class);
                    Object instance = constructor.newInstance(
                            event.getX(), event.getY(),
                            super.getWidth(), super.getHeight()
                    );
                    this.getChildren().add((AbstractBlock) instance);
                } catch (ClassNotFoundException | NoSuchMethodException
                        | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }

            event.setDropCompleted(true);
            event.consume();
        });

        this.setOnDragOver((DragEvent event) -> {
            Dragboard dragboard = event.getDragboard();

            if (dragboard.getString().equals("BLOCK_DRAG")) {
                AbstractBlock block = (AbstractBlock) event.getGestureSource();

                double blockNewX = getBlockNewX(block, event);
                double blockNewY = getBlockNewY(block, event);

                // zjisteni o kolik se posunul blok aby se podle toho potom posunuly i konce dratu...
                double blockShiftX = block.getLayoutX() - blockNewX;
                double blockShiftY = block.getLayoutY() - blockNewY;

                block.relocate(blockNewX, blockNewY);
                block.location = new Point2D(blockNewX, blockNewY);

                // uprava souradnic dratu v in portech
                for (int i = 0; i < block.getInPortsCount(); i++) {
                    if (block.getInPort(i).getWire() != null) {
                        block.getInPort(i).getWire().setStartX(block.getInPort(i).getWire().getStartX() - blockShiftX);
                        block.getInPort(i).getWire().setStartY(block.getInPort(i).getWire().getStartY() - blockShiftY);
                    }
                }

                // uprava souradnic dratu v out portech
                for (int i = 0; i < block.getOutPortsCount(); i++) {
                    if (block.getOutPort(i).getWire() != null) {
                        block.getOutPort(i).getWire().setEndX(block.getOutPort(i).getWire().getEndX() - blockShiftX);
                        block.getOutPort(i).getWire().setEndY(block.getOutPort(i).getWire().getEndY() - blockShiftY);
                    }
                }

            } else if (dragboard.getString().equals("PORT_DRAG")) {
                Port port = (Port) event.getGestureSource();

                if (port.getWire() != null && port.getWire().getOtherPort(port) != null) {
                    port.getWire().getOtherPort(port).setWire(null);
                    port.getWire().clearPorts();
                }

                port.setWire(canvasWireHelper);
                canvasWireHelper.setPort(port);

                if (port.getPortType().equals("in"))
                    canvasWireHelper.setStartX(port.getParent().getBoundsInParent().getMinX() + PORT_RADIUS / 2);
                else
                    canvasWireHelper.setStartX(port.getParent().getBoundsInParent().getMinX() +
                            ((AbstractBlock) port.getParent()).getWidth() - PORT_RADIUS / 2);
                canvasWireHelper.setStartY(port.getParent().getBoundsInParent().getMinY() +
                        ((AbstractBlock) port.getParent()).getPortDistance(port.getPortType()) * (port.getIndex() + 1));

                canvasWireHelper.setEndX(event.getX());
                canvasWireHelper.setEndY(event.getY());
            }

            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });
    }

    /**
     * @param block blok, jehož x-ová souřadnice se má zjistit
     * @param event instance události, podle které se souřadnice zjištují
     * @return Vrací novou x-ovou souřadnici bloku po přetahování bloku.
     */
    private double getBlockNewX(AbstractBlock block, DragEvent event) {
        if (event.getX() - block.getWidth() / 2 < 0) {
            return block.getWidth() / 2;
        } if (super.getWidth() < event.getX() + block.getWidth() / 2) {
            return super.getWidth() - block.getWidth() / 2;
        } return event.getX();
    }

    /**
     * @param block blok, jehož y-ová souřadnice se má zjistit
     * @param event instance události, podle které se souřadnice zjištují
     * @return Vrací novou y-ovou souřadnici bloku po přetahování bloku.
     */
    private double getBlockNewY(AbstractBlock block, DragEvent event) {
        if (event.getY() - block.getHeight() / 2 < 0) {
            return block.getHeight() / 2;
        } if (super.getHeight() < event.getY() + block.getHeight() / 2) {
            return super.getHeight() - block.getHeight() / 2;
        } return event.getY();
    }
}
