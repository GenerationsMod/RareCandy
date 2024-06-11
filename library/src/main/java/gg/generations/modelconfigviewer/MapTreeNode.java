//package gg.generations.modelconfigviewer;
//
//import javax.swing.tree.DefaultMutableTreeNode;
//import java.util.Map;
//import java.util.function.Function;
//
//public class MapTreeNode<V> extends DefaultMutableTreeNode {
//    public MapTreeNode(String name, Map<String, V> map, Function<V, ModelConfigTree.ComponentProvider> providerFunction) {
//        setUserObject(name);
//        for (Map.Entry<String, V> entry : map.entrySet()) {
//            var key = entry.getKey();
//            var value = providerFunction.apply(entry.getValue());
//            DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(key);
//            DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(value);
//            keyNode.add(valueNode);
//            add(keyNode);
//        }
//    }
//}
//
