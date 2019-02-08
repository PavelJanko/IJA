package ija.scheme.toolbar;

import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import ija.scheme.box.AlertBox;
import ija.scheme.canvas.AbstractBlock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static ija.scheme.box.AlertBox.*;

/**
 * Třída sloužící pro práci s tlačítky pro bloky - jejich přetažením na kanvas se vytvoří nová instance bloku.
 *
 * @author Radek Hůlka
 */
public class ToolBar extends javafx.scene.control.ToolBar {
    public javafx.scene.control.ToolBar toolBar;

    private final static double SPACING = 10;

    /**
     * Konstruktor, který nástrojový panel naplní tlačítky s bloky za použití reflekce.
     *
     * @param menubar kontextová nabídka ve vrchní části aplikace
     */
    public ToolBar(MenuBar menubar) {
        this.toolBar = new javafx.scene.control.ToolBar();

        String blocks[] = new String[AbstractBlock.blocks.length];
        double toolWidth = 0;
        for (int i = 0; i < AbstractBlock.blocks.length; i++) {
            try {
                Class<?> classHelper = Class.forName("ija.scheme.canvas.block." + AbstractBlock.blocks[i]);
                Constructor<?> constructor = classHelper.getConstructor(double.class, double.class,
                        double.class, double.class);
                Object instance = constructor.newInstance( 0, 0, 1280, 720);
                String toolName = "";
                toolName += ((AbstractBlock) instance).getBlockName();
                toolName += "\n";
                toolName += " " + ((AbstractBlock) instance).getInPortsLabels()[0];

                for (int j = 1; j < ((AbstractBlock) instance).getInPortsLabels().length; j++) {
                    toolName += "; " + ((AbstractBlock) instance).getInPortsLabels()[j];
                }

                toolName += " to";
                toolName += " " + ((AbstractBlock) instance).getOutPortsLabels()[0];

                for (int j = 1; j < ((AbstractBlock) instance).getOutPortsLabels().length; j++) {
                    toolName += "; " + ((AbstractBlock) instance).getOutPortsLabels()[j];
                }

                Text text = new Text(toolName);
                if (text.getBoundsInParent().getWidth() > toolWidth) {
                    toolWidth = text.getBoundsInParent().getWidth();
                }

                blocks[i] = toolName;
            } catch (ClassNotFoundException | NoSuchMethodException
                    | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        toolWidth += 2 * SPACING;

        for (int i = 0; i < blocks.length; i++) {
            Button toolBarButton = new Button(blocks[i]);
            toolBarButton.setMinWidth(toolWidth);
            toolBarButton.setMaxWidth(toolWidth);
            toolBarButton.setTextAlignment(TextAlignment.CENTER);

            int finalI = i;

            toolBarButton.setOnDragDetected((MouseEvent event) -> {
                if (!menubar.getMenus().get(1).isDisable()) {
                    Dragboard dragboard = toolBarButton.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString(AbstractBlock.blocks[finalI]);
                    dragboard.setContent(clipboardContent);
                    event.consume();
                } else {
                    AlertBox.display(CANT_CREATE_NEW_BLOCK_BOX_WIDTH, CANT_CREATE_NEW_BLOCK_BOX_TITLE, CANT_CREATE_NEW_BLOCK_BOX_DESCRIPTION);
                }
            });

            this.toolBar.getItems().add(toolBarButton);
        }

        this.toolBar.setOrientation(Orientation.VERTICAL);
    }
}
