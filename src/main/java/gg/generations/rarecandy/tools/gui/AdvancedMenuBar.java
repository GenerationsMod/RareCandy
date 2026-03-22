package gg.generations.rarecandy.tools.gui;

import javax.swing.*;
import java.awt.*;

import imgui.ImGui;
import java.util.ArrayList;
import java.util.List;

public class AdvancedMenuBar {
    private final List<Menu> menus = new ArrayList<>();

    public Menu addMenu(String name) {
        var menu = new Menu(name);
        menus.add(menu);
        return menu;
    }

    public void render() {
        if (ImGui.beginMainMenuBar()) {
            for (Menu menu : menus) {
                menu.render();
            }
            ImGui.endMainMenuBar();
        }
    }

    // Nested menu class
    public static class Menu {
        private final String label;
        private final List<MenuItem> items = new ArrayList<>();
        private final List<Menu> menus = new ArrayList<>();

        public Menu(String label) {
            this.label = label;
        }

        public MenuItem addItem(String label, Runnable action) {
            var item = new MenuItem(label, action);
            items.add(item);
            return item;
        }

        public Menu addMenu(String name) {
            var menu = new Menu(name);
            menus.add(menu);
            return menu;
        }

        private void render() {
            if (ImGui.beginMenu(label, true)) {
                for (MenuItem item : items) {
                    item.render();
                }

                for (Menu item : menus) {
                    item.render();
                }

                ImGui.endMenu();
            }
        }
    }

    // Nested menu item class
    public static class MenuItem {
        private final String label;
        private final Runnable action;
        private boolean selected;

        public MenuItem(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }

        private void render() {
            if (ImGui.menuItem(label, "", selected, true)) {
                selected = !selected;
                if (action != null) {
                    action.run();
                }
            }
        }

        public boolean isSelected() {
            return selected;
        }
    }
}
