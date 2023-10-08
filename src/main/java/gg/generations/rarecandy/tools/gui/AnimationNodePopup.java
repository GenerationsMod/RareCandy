package gg.generations.rarecandy.tools.gui;

import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextWriter;
import org.msgpack.core.MessagePack;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;

import static gg.generations.rarecandy.renderer.LoggerUtil.printError;

public class AnimationNodePopup extends JPopupMenu {

    private final GuiHandler gui;

    public AnimationNodePopup(JTree tree, GuiHandler guiHandler, MouseEvent e) {
        this.gui = guiHandler;

        var imports = new JMenuItem("Import");

        add(imports);
        show(e.getComponent(), e.getX(), e.getY());

        var path = tree.getClosestPathForLocation(e.getPoint().x, e.getPoint().y);
        imports.addActionListener(ae -> importNode(tree, path));
    }

    public void importNode(JTree tree, TreePath pathNode) {
        var model = (DefaultTreeModel) tree.getModel();
        var target = (DefaultMutableTreeNode) pathNode.getLastPathComponent();

        var path = DialogueUtils.chooseFile("Animations;smdx,smd,bmd");
        assert path != null;
        var name = path.getFileName().toString();

        byte[] fileBytes = null;

        try (var stream = Files.newInputStream(path)) {
            if (name.endsWith("smd")) {
                fileBytes = stream.readAllBytes();
            } else if (name.endsWith("smdx")) {
                fileBytes = new SMDTextWriter().write(new SMDBinaryReader().read(MessagePack.newDefaultUnpacker(stream))).getBytes();
            }
        } catch (IOException e) {
            printError(e);
            return;
        }


        if (fileBytes == null) throw new RuntimeException("Removed non-existing file");
        gui.asset.files.put(path.getFileName().toString(), fileBytes);

        target.add(new DefaultMutableTreeNode(name));
        model.reload(target);
        gui.markDirty();

        gui.getCanvas().openFile(gui.asset);
    }
}
