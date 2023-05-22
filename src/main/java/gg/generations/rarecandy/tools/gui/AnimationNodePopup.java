package gg.generations.rarecandy.tools.gui;

import dev.thecodewarrior.binarysmd.BinarySMD;
import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader;
import dev.thecodewarrior.binarysmd.formats.SMDBinaryWriter;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextWriter;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import gg.generations.rarecandy.animation.Animation;
import gg.generations.rarecandy.animation.Skeleton;
import org.msgpack.core.MessagePack;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        var name = path.getFileName().toString();

        if (path != null) {
            byte[] fileBytes = null;

            try(var stream = Files.newInputStream(path)) {
                if(name.endsWith("smd")) {
                    fileBytes = stream.readAllBytes();
                } else if(name.endsWith("smdx")) {
                    fileBytes = new SMDTextWriter().write(new SMDBinaryReader().read(MessagePack.newDefaultUnpacker(stream))).getBytes();
                }
            } catch (IOException e) {
                e.printStackTrace();
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
}
