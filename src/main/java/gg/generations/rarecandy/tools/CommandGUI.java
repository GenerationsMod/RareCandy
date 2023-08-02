package gg.generations.rarecandy.tools;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.function.Consumer;

public class CommandGUI extends Frame {
    private List<Command> commands;
    private final String[] args;

    public CommandGUI(List<Command> commands, String[] args) {
        this.commands = commands;
        this.args = args;
        setTitle("Command GUI");
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);

        for (Command command : commands) {
            String name = command.name();
            String description = command.description();

            Button button = new Button(name);
            Label label = new Label(description);

            gbc.gridy++;
            add(button, gbc);

            gbc.gridx++;
            add(label, gbc);

            button.addActionListener(new CommandButtonListener(command.consumer()));

            gbc.gridx = 0; // Reset the gridx to start from the left for the next row
        }
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });

        pack();
        setVisible(true);
    }

    private class CommandButtonListener implements ActionListener {
        private Consumer<String[]> consumer;

        public CommandButtonListener(Consumer<String[]> consumer) {
            this.consumer = consumer;
        }

        public void actionPerformed(ActionEvent event) {
            String[] args = new String[] {}; // You can replace this with actual command arguments
            consumer.accept(args);
            dispose();
        }
    }
}