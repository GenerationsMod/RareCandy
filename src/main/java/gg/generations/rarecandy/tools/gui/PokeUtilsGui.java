package gg.generations.rarecandy.tools.gui;

import com.github.weisj.darklaf.LafManager;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import org.lwjgl.opengl.awt.AWTGLCanvas;

import javax.swing.*;
import java.awt.*;

public class PokeUtilsGui extends JPanel {


    public GuiHandler handler;
    public JTree fileViewer;
    public JPanel canvasPanel;
    private AWTGLCanvas renderingWindow;
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JMenuBar toolbar;
    private JMenu file;
    private JMenuItem openArchive;
    private JMenuItem createArchive;
    private JMenuItem save;
    private JMenuItem saveAs;
    private JScrollPane scrollPane1;
    public PokeUtilsGui() {
        LafManager.install(LafManager.themeForPreferredStyle(LafManager.getPreferredThemeStyle()));
        initComponents();
        canvasPanel.add(renderingWindow);
        fileViewer.setFocusable(true);
        canvasPanel.setFocusable(true);
        openArchive.addActionListener(e -> {
            var chosenFile = DialogueUtils.chooseFile("PK;pk");
            if (chosenFile != null) handler.openAsset(chosenFile);
        });
        createArchive.addActionListener(e -> {
            var chosenFile = DialogueUtils.chooseFile("GLB;glb");
            if (chosenFile != null) handler.convertGlb(chosenFile);
        });

        save.addActionListener(e -> handler.save());

        saveAs.addActionListener(e -> {
            var chosenFile = DialogueUtils.saveFile("PK;pk");
            if (chosenFile != null) {
                handler.markDirty();
                handler.save(chosenFile);
            }
        });

        PipelineRegistry.setFunction(s-> switch(s) {
            case "transparent"-> GuiPipelines.TRANSPARENT;
            case "unlit" -> GuiPipelines.UNLIT;
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
        frame.setSize(new Dimension(960, 540));
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
        this.renderingWindow = new RareCandyCanvas();
    }
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        createUIComponents();

        toolbar = new JMenuBar();
        file = new JMenu();
        openArchive = new JMenuItem();
        createArchive = new JMenuItem();
        save = new JMenuItem();
        saveAs = new JMenuItem();
        var splitPane1 = new JSplitPane();
        scrollPane1 = new JScrollPane();
        canvasPanel = new JPanel();

        //======== this ========
        setMinimumSize(null);
        setMaximumSize(null);
        setLayout(new BorderLayout());

        //======== toolbar ========
        {
            toolbar.setMaximumSize(new Dimension(29, 20));
            toolbar.setMinimumSize(new Dimension(29, 20));
            toolbar.setPreferredSize(new Dimension(29, 20));
            toolbar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            //======== file ========
            {
                file.setText("File");

                //---- openArchive ----
                openArchive.setText("Open Archive (.pk)    ");
                file.add(openArchive);

                //---- convertGlb ----
                createArchive.setText("Create Archive (.glb)    ");
                file.add(createArchive);

                //---- save ----
                save.setText("Save");
                file.add(save);

                //---- saveAs ----
                saveAs.setText("Save As");
                file.add(saveAs);
            }
            toolbar.add(file);
        }
        add(toolbar, BorderLayout.NORTH);

        //======== splitPane1 ========
        {
            splitPane1.setMaximumSize(null);
            splitPane1.setMinimumSize(null);
            splitPane1.setDividerSize(4);
            splitPane1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            //======== scrollPane1 ========
            {
                scrollPane1.setMinimumSize(new Dimension(250, 540));
                scrollPane1.setPreferredSize(new Dimension(262, 540));

                //---- fileViewer ----
                fileViewer.setPreferredSize(new Dimension(250, 2000));
                fileViewer.setMaximumSize(null);
                fileViewer.setMinimumSize(new Dimension(200, 540));
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