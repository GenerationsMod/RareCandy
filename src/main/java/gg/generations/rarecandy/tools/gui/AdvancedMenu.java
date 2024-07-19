package gg.generations.rarecandy.tools.gui;

import javax.swing.*;
import java.awt.event.ActionListener;

public class AdvancedMenu extends JMenu {
    private final AdvancedMenuBar bar;

    public AdvancedMenu(String name, AdvancedMenuBar bar) {
        this.bar = bar;
        this.setText(name);
    }

    public AdvancedMenu addMenuItem(String name, ActionListener listener) {
        var menu = new JMenuItem();
        menu.setText(name);
        menu.addActionListener(listener);
        add(menu);
        return this;
    }

    public AdvancedMenuBar finish() {
        return bar;
    }
}
