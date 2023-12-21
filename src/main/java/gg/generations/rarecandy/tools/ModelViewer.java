package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.tools.gui.GuiPipelines;
import gg.generations.rarecandy.tools.gui.RareCandyCanvas;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.tukaani.xz.XZOutputStream;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static gg.generations.rarecandy.renderer.LoggerUtil.printError;
import static gg.generations.rarecandy.tools.gui.GuiHandler.OPTIONS;

public class ModelViewer extends JFrame {
    private final boolean moveToTemp;
    private Path path;
    private PixelAsset asset;

    public final DefaultMutableTreeNode animationNode = new DefaultMutableTreeNode("animations");
    public final JTree animationsTree = new JTree(new DefaultTreeModel(animationNode));
    public final DefaultMutableTreeNode variantNode = new DefaultMutableTreeNode("variants");
    public final JTree variantsTree = new JTree(new DefaultTreeModel(variantNode));
    public final RareCandyCanvas canvas = new RareCandyCanvas();

    public static final Path temp = Path.of("temp");
    private final Button loadButton;

    public ModelViewer(Consumer<Consumer<Pair<Path, PixelAsset>>> consumer, boolean moveToTemp) {
        this.moveToTemp = moveToTemp;
        setSize(new Dimension(700, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JSplitPane basic = new JSplitPane();
        basic.setFocusable(true);
        basic.setDividerLocation(500);
        basic.setOrientation(JSplitPane.VERTICAL_SPLIT);

        JSplitPane canvasPanel = new JSplitPane();
        canvasPanel.setMaximumSize(new Dimension(1080, 1080));
        canvasPanel.setPreferredSize(new Dimension(700, 500));
        canvasPanel.setDividerLocation(200);
        canvasPanel.setRightComponent(canvas);

        animationsTree.addTreeSelectionListener(e -> {
            if(e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode node && !node.isRoot()) {
                canvas.setAnimation(node.toString());
            }
        });

        variantsTree.addTreeSelectionListener(e -> {
            if(e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode node && !node.isRoot()) {
                canvas.setVariant(node.toString());
            }
        });


        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel1.add(animationsTree,gbc);
        gbc.gridy = 1;
        panel1.add(variantsTree, gbc);

        var scroll = new JScrollPane();
        scroll.setViewportView(panel1);
        scroll.setPreferredSize(new Dimension(200, 500));

        canvasPanel.setLeftComponent(scroll);

        basic.setTopComponent(canvasPanel);

        var panel = new JSplitPane();
        panel.setDividerLocation(350);
        loadButton = new Button("Load");

        loadButton.addActionListener(e -> {
            try {
                if (path == null) {
                    consumer.accept(this::processPath);
                } else {
                    Path path1 = Path.of("completed");
                    if (Files.notExists(path1)) Files.createDirectories(path1);

                    try (var xzWriter = new XZOutputStream(Files.newOutputStream(path1.resolve(asset.name)), OPTIONS)) {
                        try (var tarWriter = new TarArchiveOutputStream(xzWriter)) {
                            for (var file : asset.files.entrySet()) {
                                var entry = new TarArchiveEntry(file.getKey());
                                entry.setSize(file.getValue().length);
                                tarWriter.putArchiveEntry(entry);
                                IOUtils.copy(new BufferedInputStream(new ByteArrayInputStream(file.getValue())), tarWriter);
                                tarWriter.closeArchiveEntry();
                            }
                        }
                    }

                    if(moveToTemp) {
                        Files.walk(temp).filter(Files::isRegularFile).forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException ex) {
                            }
                        });
                    }

                    path = null;
                    asset = null;
                    setTitle("ModelViewer");
                    loadButton.setLabel("Load");
                    canvas.openFile(null);
                }
            } catch (IOException ex) {
                printError(ex);
            }
        });
        panel.setLeftComponent(loadButton);
        var reloadButton = new Button("Reload");
        reloadButton.addActionListener(e -> {
            if (path != null) {
                try {
                    canvas.openFile(asset = new PixelAsset(path), this::updateTrees);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        panel.setRightComponent(reloadButton);

        basic.setBottomComponent(panel);

        setContentPane(basic);
        setTitle("ModelViewer");
        setVisible(true);
        pack();
        transferFocus();

        RareCandyCanvas.setup(canvas);

            consumer.accept(this::processPath);

    }

    private void processPath(Pair<Path, PixelAsset> pair) {
        try {
            if (Files.notExists(temp)) {
                Files.createDirectories(temp);
            }

            path = pair.a();


            if (path != null) {


                    (asset = pair.b()).files.forEach((s, bytes) -> {
                        try {
                            Files.write(temp.resolve(s), bytes);
                        } catch (IOException ex) {
                            printError(ex);
                        }
                    });

                    canvas.openFile(asset, this::updateTrees);


                ModelViewer.this.setTitle("ModelViewer - " + asset.name);
                loadButton.setLabel("Complete");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    public static <T> void createFrame(String title, String leftName, Supplier<T> leftRunnable, String rightName, Supplier<T> rightRunnable, Consumer<T> consumer) {
        JFrame frame = new JFrame(title);

        JButton leftButton = new JButton(leftName);
        leftButton.addActionListener(e -> {
            consumer.accept(leftRunnable.get());
            frame.dispose();
        });

        JButton rightButton = new JButton(rightName);
        rightButton.addActionListener(e -> {
            consumer.accept(rightRunnable.get());
            frame.dispose();
        });

        JPanel panel = new JPanel();
        panel.add(leftButton);
        panel.add(rightButton);

        frame.getContentPane().add(panel);
        frame.setSize(200, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void updateTrees() {
        var animations = canvas.loadedModelInstance.getAnimationsIfAvailable().keySet();
        var variants = canvas.loadedModel.availableVariants();

        animationNode.removeAllChildren();
        animations.stream().map(DefaultMutableTreeNode::new).forEach(animationNode::add);

        variantNode.removeAllChildren();
        variants.stream().map(DefaultMutableTreeNode::new).forEach(variantNode::add);
    }
}
