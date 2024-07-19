package gg.generations.rarecandy.tools.gui;

import javax.swing.*;
import java.awt.*;

public class AdvancedMenuBar extends JMenuBar {
    public AdvancedMenuBar(int width, int height, ComponentOrientation orientation) {
        var dimension = new Dimension(width, height);
        setMaximumSize(new Dimension(29, 20));
        setMinimumSize(new Dimension(29, 20));
        setPreferredSize(new Dimension(29, 20));
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
    }

    public AdvancedMenu addMenu(String name) {
        var menu = new AdvancedMenu(name, this);
        add(menu);
        return menu;
    }
}
