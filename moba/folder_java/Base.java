package folder_java;
import java.awt.*;

public class Base extends Entity {
    boolean isPlayerBase;
    public Tower requiredTower;
    public Base(int x, int y, boolean isPlayerBase) {
        super(x, y, 120, 120, 50000, 0, isPlayerBase ? 1 : 2);
        this.isPlayerBase = isPlayerBase;
    }
    @Override
    public void update(GamePanel gp) {}
    @Override
    public void draw(Graphics2D g2) {
    }
    public void draw(Graphics2D g2, GamePanel gp) {
        int bx = (int)x, by = (int)y;
        Color base = isPlayerBase ? new Color(0, 160, 180) : new Color(180, 30, 180);
        Color dark = isPlayerBase ? new Color(0, 100, 120) : new Color(130, 10, 130);
        Color light = isPlayerBase ? new Color(100, 220, 240) : new Color(240, 100, 240);
        
        // Glow under tower
        g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 60));
        g2.fillOval(bx - 20, by + height - 20, width + 40, 40);
        
        // Main tower body
        g2.setColor(base);
        g2.fillRoundRect(bx + 10, by + 20, width - 20, height - 20, 8, 8);
        
        // Side turrets
        g2.setColor(dark);
        g2.fillRect(bx, by + 30, 24, height - 30);
        g2.fillRect(bx + width - 24, by + 30, 24, height - 30);
        
        // Battlements (top crenellations)
        g2.setColor(dark);
        for (int i = 0; i < 5; i++) {
            g2.fillRect(bx + 10 + i * ((width-20)/5), by + 10, (width-20)/5 - 6, 18);
        }
        g2.fillRect(bx, by + 15, 12, 22);
        g2.fillRect(bx + width - 12, by + 15, 12, 22);
        
        // Windows
        g2.setColor(new Color(255, 230, 100, 200));
        g2.fillOval(bx + width/2 - 12, by + height/2 - 10, 24, 28);
        g2.fillOval(bx + 4, by + height/2, 14, 18);
        g2.fillOval(bx + width - 18, by + height/2, 14, 18);
        
        // Highlight edge
        g2.setColor(light);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(bx + 10, by + 20, width - 20, height - 20, 8, 8);
        g2.setStroke(new BasicStroke(1));
        
        // Flag
        g2.setColor(new Color(80, 40, 10));
        g2.fillRect(bx + width/2, by - 30, 4, 40);
        g2.setColor(isPlayerBase ? Color.CYAN : new Color(255, 80, 0));
        int[] fx = {bx + width/2 + 4, bx + width/2 + 28, bx + width/2 + 4};
        int[] fy = {by - 28, by - 18, by - 8};
        g2.fillPolygon(fx, fy, 3);
        
        // HP bar
        g2.setColor(new Color(0,0,0,140));
        g2.fillRoundRect(bx, by - 20, width, 12, 4, 4);
        g2.setColor(Color.RED);
        g2.fillRoundRect(bx, by - 20, width, 12, 4, 4);
        g2.setColor(new Color(80, 220, 80));
        g2.fillRoundRect(bx, by - 20, (int)(width * ((double)hp / maxHp)), 12, 4, 4);

        // Label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString(isPlayerBase ? "Our Base" : "Enemy Base", bx, by - 26);
    }
    @Override
    public void takeDamage(int amount) {
        if (requiredTower != null && requiredTower.active) return;
        super.takeDamage(amount);
    }
}
