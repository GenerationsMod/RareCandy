package gg.generations.modelconfigviewer;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Map;
import java.util.function.BiFunction;

public class NestedMapNode<V> extends DefaultMutableTreeNode {
    public NestedMapNode(String name, Map<String, Map<String, V>> map, BiFunction<String, V, ModelConfigTree.ComponentProvider> providerFunction) {
        setUserObject(name);

        for (Map.Entry<String, Map<String, V>> entry : map.entrySet()) {
            var key = entry.getKey();
            var keyNode = new DefaultMutableTreeNode(key);
            add(keyNode);

            var value = entry.getValue();

            for (Map.Entry<String, V> e : value.entrySet()) {
                var node = new DefaultMutableTreeNode(e.getKey());
                var vNode = new DefaultMutableTreeNode(providerFunction.apply(e.getKey(), e.getValue()));
                node.add(vNode);
                keyNode.add(node);
            }

        }
    }
}
