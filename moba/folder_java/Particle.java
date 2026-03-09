package folder_java;

import java.awt.*;

public class Particle {
    double x, y;
    Color color;
    int life = 20;

    public Particle(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, life * 12)));
        g2.fillOval((int) x, (int) y, 6, 6);
    }
}
