package gg.generations.rarecandy.tools.gui;

import com.thebombzen.jxlatte.JXLDecoder;
import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextWriter;
import modelconfigviewer.ImageViewer;
import org.msgpack.core.MessagePack;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;

public class ImageNodePopup extends JPopupMenu {

    private final GuiHandler gui;

    public ImageNodePopup(JTree tree, GuiHandler guiHandler, MouseEvent e) {
        this.gui = guiHandler;

        var path = tree.getClosestPathForLocation(e.getPoint().x, e.getPoint().y);

        if(!path.getParentPath().toString().equals("images")) {
            var imports = new JMenuItem("Import");
            add(imports);
            imports.addActionListener(ae -> importNode(tree, path));
        } else {
            var export = new JMenuItem("Export");
            export.addActionListener(e1 -> {});
            var show = new JMenuItem("Show");
            show.addActionListener(s -> {
                var name = path.getLastPathComponent().toString();
                var imagePair = guiHandler.asset.getImageFiles().stream().filter(a -> a.equals(name)).findFirst().get();

                try {
                    BufferedImage image = new JXLDecoder(new ByteArrayInputStream(imagePair.getValue())).decode().asBufferedImage();
                    new ImageViewer(imagePair.getKey(), image);
                    setVisible(true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            add(export);
            add(show);
        }

        show(e.getComponent(), e.getX(), e.getY());
    }

    public void importNode(JTree tree, TreePath pathNode) {
        var model = (DefaultTreeModel) tree.getModel();
        var target = (DefaultMutableTreeNode) pathNode.getLastPathComponent();

        var path = DialogueUtils.chooseFile("Images;jxl");
        var name = path.getFileName().toString();

        if (path != null) {
            byte[] fileBytes = null;

            try(var stream = Files.newInputStream(path)) {
                fileBytes = stream.readAllBytes();

                if (fileBytes == null) throw new RuntimeException("Removed non-existing file");
                gui.asset.files.put(path.getFileName().toString(), fileBytes);

                target.add(new DefaultMutableTreeNode(name));
                model.reload(target);
                gui.markDirty();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
