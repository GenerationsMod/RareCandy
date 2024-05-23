package gg.generations.rarecandy.tools.gui;

import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextWriter;
import gg.generations.rarecandy.renderer.LoggerUtil;
import org.msgpack.core.MessagePack;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;

public class TreeNodePopup extends JPopupMenu {
    private final GuiHandler gui;

    public TreeNodePopup(JTree tree, GuiHandler gui, MouseEvent e) {
        this.gui = gui;
        var delete = new JMenuItem("Delete");
        var rename = new JMenuItem("Rename    ");
        var export = new JMenuItem("Export");

        add(delete);
        add(rename);
        add(export);
        show(e.getComponent(), e.getX(), e.getY());

        var path = tree.getClosestPathForLocation(e.getPoint().x, e.getPoint().y);
        delete.addActionListener(ae -> deleteNode(tree, path));
        rename.addActionListener(ae -> renameNode(tree, path));
        export.addActionListener(ae -> exportNode(path));
    }

    public void deleteNode(JTree tree, TreePath pathNode) {
        var model = (DefaultTreeModel) tree.getModel();

        if (!model.getRoot().equals(pathNode.getLastPathComponent())) {
            if (gui.asset.files.remove(pathNode.getPath()[pathNode.getPath().length - 1].toString()) == null)
                throw new RuntimeException("Removed non-existing file");
            model.removeNodeFromParent((MutableTreeNode) pathNode.getLastPathComponent());
            model.reload((TreeNode) pathNode.getParentPath().getLastPathComponent());
            gui.markDirty();
        }
    }

    public void renameNode(JTree tree, TreePath pathNode) {
        var model = (DefaultTreeModel) tree.getModel();
        var target = (DefaultMutableTreeNode) pathNode.getLastPathComponent();
        var newName = JOptionPane.showInputDialog(tree.getRootPane(), "What do you want to rename this file to?", target);

        if (newName != null) {
            if (!isValidFileName(newName)) {
                JOptionPane.showMessageDialog(tree.getRootPane(), "\"" + newName + "\" is not a valid file name");
                return;
            }

            var fileBytes = gui.asset.files.remove(target.toString());
            if (fileBytes == null) throw new RuntimeException("Removed non-existing file");
            gui.asset.files.put(newName, fileBytes);

            var parent = (MutableTreeNode) pathNode.getParentPath().getLastPathComponent();
            parent.insert(new DefaultMutableTreeNode(newName), model.getIndexOfChild(parent, target));
            model.removeNodeFromParent(target);
            model.reload(parent);
            gui.markDirty();
            LoggerUtil.print("Rename  " + pathNode);
        }
    }

    public void importAnimation(TreePath pathNode) {
        try {
            var fileName = pathNode.getLastPathComponent().toString();
            var exportFileType = getExportFileType(fileName);
            var exportBytes = getExportBytes(gui.asset.files.get(fileName));
            var outputPath = DialogueUtils.saveFile(exportFileType);
            if (outputPath != null) Files.write(outputPath, exportBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void exportNode(TreePath pathNode) {
        try {
            var fileName = pathNode.getLastPathComponent().toString();
            var exportFileType = getExportFileType(fileName);
            var exportBytes = getExportBytes(gui.asset.files.get(fileName));
            var outputPath = DialogueUtils.saveFile(exportFileType);
            if (outputPath != null) Files.write(outputPath, exportBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getExportBytes(byte[] bytes) {
        var isCompressedAnim = Arrays.equals(Arrays.copyOfRange(bytes, 0, 4), "SMDX".getBytes());

        if (isCompressedAnim) {
            try (var unpacker = MessagePack.newDefaultUnpacker(bytes)) {
                var smdInfo = new SMDBinaryReader().read(unpacker);
                return new SMDTextWriter().write(smdInfo).getBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else return bytes;
    }

    private String getExportFileType(String fileName) {
        var split = fileName.split("\\.");
        var rawType = split.length == 2 ? split[1] : "";
        if (rawType.equals("pkanm")) return "smd";
        if (fileName.contains("pkanm")) return "smd"; // Error caused files without extensions to be created

        return rawType;
    }

    public boolean isValidFileName(String fileName) {
        try {
            Paths.get(fileName);
        } catch (InvalidPathException e) {
            return false;
        }

        return true;
    }
}