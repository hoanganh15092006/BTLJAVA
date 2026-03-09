package folder_java;

import java.awt.*;

public class Tower extends Entity {
    int attackCooldown = 0;
    public Tower requiredTower;
    int range = 1000;

    public Tower(int x, int y, int team) {
        super(x, y, 70, 90, 8000, 0, team);
    }

    @Override
    public void update(GamePanel gp) {
        if (!active || attackCooldown-- > 0)
            return;
        Entity target = null;
        double minDist = range;

        // Priority: Minions first
        for (Minion e : gp.minions) {
            if (e.active && e.team != team) {
                double d = distanceTo(e);
                if (d < minDist) {
                    minDist = d;
                    target = e;
                }
            }
        }

        // Then NPC Heroes or Player if no minions
        if (target == null) {
            for (NpcHero e : gp.npcHeroes) {
                if (e.active && e.team != team) {
                    double d = distanceTo(e);
                    if (d < minDist) {
                        minDist = d;
                        target = e;
                    }
                }
            }
            if (team == 2 && gp.player.active) {
                double d = distanceTo(gp.player);
                if (d < minDist) {
                    minDist = d;
                    target = gp.player;
                }
            }
        }

        // Hero Aggro: If enemy (player) hit friendly NPC hero, tower targets player
        if (team == 1 && gp.playerAggroTimer > 0) {
            if (gp.player.active && distanceTo(gp.player) < range)
                target = gp.player;
        }

        if (target != null) {
            double dx = target.x + target.width / 2 - (x + width / 2),
                    dy = target.y + target.height / 2 - (y + height / 2);
            double len = Math.sqrt(dx * dx + dy * dy);
            gp.bullets.add(new Bullet(x + width / 2, y + height / 2, dx / len, dy / len, team, team == 1 ? 3 : 4,
                    Math.atan2(dy, dx) + Math.PI / 2, false));
            attackCooldown = 80;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int bx = (int) x, by = (int) y;
        Color col = team == 1 ? new Color(0, 160, 200) : new Color(200, 40, 40);
        Color dark = team == 1 ? new Color(0, 80, 120) : new Color(130, 10, 10);
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 50));
        g2.fillOval(bx - 10, by + height - 10, width + 20, 22);
        g2.setColor(dark);
        g2.fillRoundRect(bx, by + height - 18, width, 18, 6, 6);
        g2.setColor(col);
        g2.fillRoundRect(bx + 12, by + 20, width - 24, height - 20, 6, 6);
        g2.setColor(dark);
        for (int i = 0; i < 3; i++)
            g2.fillRect(bx + 8 + i * ((width - 16) / 3), by + 8, (width - 16) / 3 - 5, 20);
        g2.setColor(new Color(255, 230, 100, 200));
        g2.fillOval(bx + width / 2 - 8, by + height / 2 - 10, 16, 20);
        g2.setColor(Color.RED);
        g2.fillRect(bx, by - 10, width, 7);
        g2.setColor(new Color(80, 220, 80));
        g2.fillRect(bx, by - 10, (int) (width * ((double) hp / maxHp)), 7);
    }

    @Override
    public void takeDamage(int amount) {
        if (requiredTower != null && requiredTower.active)
            return;
        super.takeDamage(amount);
    }
}
