//package gg.generations.rarecandy.tools.gui
//
//import gg.generations.rarecandy.pokeutils.resource.ResourceLocator
//import gg.generations.rarecandy.tools.gui.PokeUtilsGui.FloatInputComponent
//import gg.generations.rarecandy.tools.gui.RareCandyCanvas.BaseMultiRenderObject
//import imgui.ImGui
//import java.io.IOException
//import java.nio.file.Path
//import java.util.List
//import java.util.function.Consumer
//
//class PixelAssetTree(val gui: PokeUtilsGui) {
//    val scale: FloatInputComponent
//
//    private var tree = CompositeNode("N/A")
//
//    init {
//        scale = FloatInputComponent(
//            "Scale",
//            { gui.canvas.originalScaleModifer },
//            { scale: Double -> gui.canvas.scaleModifier = scale.toFloat() })
//    }
//
//    fun render() {
//        ImGui.begin("File Tree")
//
//        tree.render()
//        ImGui.separator()
//
//        scale.render()
//
//        ImGui.end()
//    }
//
//    @Throws(IOException::class)
//    fun initializeAsset(asset: ResourceLocator?, assetPath: Path, model: BaseMultiRenderObject) {
//        tree = CompositeNode(assetPath.getFileName().toString())
//        val animationsNode =
//            RadioListNode("animations", Consumer { animation: String? -> gui.canvas.setAnimation(animation!!) })
//        val imagesNode = CompositeNode("images")
//
//        val config = gui.canvas.config
//
//        val variants = config.variants.keys
//
//        val objs: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
//            config.defaultVariant().keySet()
//
//        model.animationNameToId.keys.stream().sorted().forEach { text: String? -> animationsNode.add(text) }
//        model.imageNameToId.keys.stream().sorted().map<ImageNode?> { s: String? -> ImageNode(model, s) }
//            .forEach { child: ImageNode? -> imagesNode.add(child) }
//
//        if (animationsNode.size() > 0) {
//            gui.canvas.setAnimation(animationsNode.selectedOption!!)
//
//            tree.add(animationsNode)
//        }
//        if (imagesNode.size() > 0) tree.add(imagesNode)
//
//        if (!variants.isEmpty()) {
//            val variantsNode =
//                RadioListNode("variants", Consumer { variant: String? -> gui.canvas.selected!!.variant = variant })
//            for (name in variants) variantsNode.add(name)
//            gui.canvas.selected!!.variant = variantsNode.selectedOption
//            tree.add(variantsNode)
//        }
//
//        val objectsNode = RadioListNode("objects", Consumer { item: String? -> gui.canvas.selected!!.mesh = item })
//        for (name in objs) objectsNode.add(name)
//        gui.canvas.selected!!.mesh = objectsNode.selectedOption
//        tree.add(objectsNode)
//    }
//
//    fun interface Node {
//        fun render()
//    }
//
//    class CompositeNode(private val label: String?) : Node {
//        private val children: MutableList<Node> = ArrayList<Node>()
//
//        fun add(child: Node?) {
//            children.add(child!!)
//        }
//
//        override fun render() {
//            if (ImGui.treeNode(label)) {
//                for (child in children) {
//                    child.render()
//                }
//                ImGui.treePop()
//            }
//        }
//
//        fun size(): Int {
//            return children.size
//        }
//    }
//
//    @JvmRecord
//    data class TextNode(val name: String?) : Node {
//        override fun render() {
//            ImGui.text(name)
//        }
//    }
//
//    class RadioListNode(private val label: String?, private val consumer: Consumer<String?>) : Node {
//        private val items: MutableList<String?> = ArrayList<String?>()
//        var selectedIndex: Int = -1
//            private set
//
//        fun add(text: String?) {
//            items.add(text)
//
//            if (this.selectedIndex == -1) {
//                this.selectedIndex = 0
//            }
//        }
//
//        val selectedOption: String?
//            get() = if (this.selectedIndex >= 0 && this.selectedIndex < items.size) items.get(this.selectedIndex) else null
//
//        fun select(item: Int) {
//            this.selectedIndex = item
//            consumer.accept(this.selectedOption)
//        }
//
//        override fun render() {
//            if (ImGui.treeNode(label)) {
//                for (i in items.indices) {
//                    if (ImGui.radioButton(items.get(i), this.selectedIndex == i)) {
//                        select(i)
//                    }
//                }
//                ImGui.treePop()
//            }
//        }
//
//        fun contains(name: String?): Boolean {
//            return items.contains(name)
//        }
//
//        fun size(): Int {
//            return items.size
//        }
//    }
//
//    class CheckboxListNode(private val label: String?, private val consumer: Consumer<Item?>) : Node {
//        private val items: MutableList<Item> = ArrayList<Item>()
//
//        protected class Item(val text: String?, var checked: Boolean) : Node {
//            override fun render() {
//                if (ImGui.checkbox(text, checked)) {
//                    checked = !checked
//                    this@CheckboxListNode.consumer.accept(this)
//                }
//            }
//        }
//
//        fun add(text: String?, initial: Boolean) {
//            items.add(CheckboxListNode.Item(text, initial))
//        }
//
//        override fun render() {
//            if (ImGui.treeNode(label)) {
//                for (i in items.indices) {
//                    val item = items.get(i)
//                    item.render()
//                }
//                ImGui.treePop()
//            }
//        }
//    } //    private static class FilesystemTransferHandler extends TransferHandler {
//    //
//    //        @SuppressWarnings("unchecked")
//    //        public boolean importData(JComponent comp, Transferable t) {
//    //            if (!(comp instanceof PixelAssetTree tree) || !t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
//    //                return false;
//    //            }
//    //
//    //            try {
//    //                var model = (DefaultTreeModel) tree.getModel();
//    //                var root = (DefaultMutableTreeNode) model.getRoot();
//    //                var data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
//    //
//    //                for (var f : data) root.add(new DefaultMutableTreeNode(f.getName()));
//    //                return true;
//    //            } catch (Exception e) {
//    //                throw new RuntimeException(e);
//    //            }
//    //        }
//    //
//    //        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
//    //            if (comp instanceof PixelAssetTree) {
//    //                for (var transferFlavor : transferFlavors) {
//    //                    if (!transferFlavor.equals(DataFlavor.javaFileListFlavor)) {
//    //                        return false;
//    //                    }
//    //                }
//    //
//    //                return true;
//    //            }
//    //
//    //            return false;
//    //        }
//    //    }
//}
