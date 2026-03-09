package folder_java;

import javax.swing.*;
import java.awt.*;

public class MobaGame {
    private JFrame frame;
    private GamePanel panel;

    public static void main(String[] args) {
        new MobaGame().init();
    }

    public void init() {
        frame = new JFrame("Liên Quân Mini - 1 Player vs NPCs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        panel = new GamePanel(screenSize, this);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);

        panel.startGameThread();
    }

    public void toggleFullscreen() {
        frame.dispose();
        if (frame.isUndecorated()) {
            frame.setUndecorated(false);
            frame.setSize(1280, 720);
            frame.setLocationRelativeTo(null);
        } else {
            frame.setUndecorated(true);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
}
