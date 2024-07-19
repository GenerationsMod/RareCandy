package gg.generations.rarecandy.tools.gui;

import com.github.weisj.darklaf.LafManager;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.tools.TextureLoader;

import javax.swing.*;
import java.awt.*;

public class PokeUtilsGui extends JPanel {


    public GuiHandler handler;
    public JTree fileViewer;
    public JPanel canvasPanel;
    private RareCandyCanvas renderingWindow;

    public PokeUtilsGui() {
        ITextureLoader.setInstance(new TextureLoader());
        LafManager.install(LafManager.themeForPreferredStyle(LafManager.getPreferredThemeStyle()));
        initComponents();
        canvasPanel.add(renderingWindow);
        fileViewer.setFocusable(true);
        canvasPanel.setFocusable(true);

        PipelineRegistry.setFunction(s-> switch(s) {
            case "masked" -> GuiPipelines.MASKED;
            case "layered" -> GuiPipelines.LAYERED;
            case "paradox" -> GuiPipelines.PARADOX;
            case "plane" -> GuiPipelines.PLANE;
            default -> GuiPipelines.SOLID;
        });

        var renderLoop = new Runnable() {
            @Override
            public void run() {
                if (renderingWindow.isValid()) renderingWindow.render();
                SwingUtilities.invokeLater(this);
            }
        };

        SwingUtilities.invokeLater(renderLoop);
    }

    public static void main(String[] args) {
        var frame = new JFrame();
        var gui = new PokeUtilsGui();
        frame.setSize(new Dimension(250+512 + (512 - 482), 512));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(gui);
        new GuiHandler(frame, gui);
    }

    public void setHandler(GuiHandler handler) {
        this.handler = handler;
        addKeyListener(handler);
        fileViewer.addKeyListener(handler);
        canvasPanel.addKeyListener(handler);
    }

    private void createUIComponents() {
//        System.load("C:/Program Files/RenderDoc/renderdoc.dll");
        this.fileViewer = new PixelAssetTree(this);
        this.renderingWindow = new RareCandyCanvas(this);
    }
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    private void initComponents() {
        createUIComponents();

        var toolbar = new AdvancedMenuBar(29, 20, ComponentOrientation.LEFT_TO_RIGHT).addMenu("File").addMenuItem("Open Archive (.pk)", e -> {
            var chosenFile = DialogueUtils.chooseFile("PK;pk");
            if (chosenFile != null) handler.openAsset(chosenFile);
        }).addMenuItem("Create Archive (.glb)", e -> {
            var chosenFile = DialogueUtils.chooseFile("GLB;glb");
            if (chosenFile != null) handler.convertGlb(chosenFile);
        }).addMenuItem("Open Multiple Archives in sequence (.pk)", e -> {
            var chosenFiles = DialogueUtils.chooseMultipleFiles("PK;pk");
            if (chosenFiles != null) handler.openAsset(chosenFiles);
        }).addMenuItem("Save", e -> handler.save()).addMenuItem("Save As", e -> {
            var chosenFile = DialogueUtils.saveFile("PK;pk");
            if (chosenFile != null) {
                handler.markDirty();
                handler.save(chosenFile);
            }
        }).finish();
        var splitPane1 = new JSplitPane();
        // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
        JScrollPane scrollPane1 = new JScrollPane();
        canvasPanel = new JPanel();

        //======== this ========
        setMinimumSize(null);
        setMaximumSize(null);
        setLayout(new BorderLayout());

        add(toolbar, BorderLayout.NORTH);

        //======== splitPane1 ========
        {
            splitPane1.setMaximumSize(null);
            splitPane1.setMinimumSize(null);
            splitPane1.setDividerSize(4);
            splitPane1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            //======== scrollPane1 ========
            {
                scrollPane1.setMinimumSize(new Dimension(250, 512));
                scrollPane1.setPreferredSize(new Dimension(262, 512));

                //---- fileViewer ----
                fileViewer.setPreferredSize(new Dimension(250, 2000));
                fileViewer.setMaximumSize(null);
                fileViewer.setMinimumSize(new Dimension(200, 512));
                scrollPane1.setViewportView(fileViewer);
            }
            splitPane1.setLeftComponent(scrollPane1);

            //======== canvasPanel ========
            {
                canvasPanel.setMaximumSize(new Dimension(1920, 1080));
                canvasPanel.setPreferredSize(null);
                canvasPanel.setLayout(new BorderLayout());
            }
            splitPane1.setRightComponent(canvasPanel);
        }
        add(splitPane1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }
}