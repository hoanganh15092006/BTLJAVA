package folder_java;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class JungleMonster extends Entity {
    static BufferedImage image;
    int attackCooldown = 0;
    Entity target;
    int spawnX, spawnY;
    public int respawnTimer = 0;

    static {
        try {
            image = SpriteUtils.makeTransparent(ImageIO.read(new File("res/jungle.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JungleMonster(int x, int y) {
        super(x, y, 192, 192, 800, 2.0, 0); // Neutral team
        spawnX = x;
        spawnY = y;
    }

    @Override
    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            active = false;
            respawnTimer = 1800; // 30 seconds at 60fps
        }
    }

    public void takeDamageFrom(int amount, Entity attacker) {
        takeDamage(amount);
        if (target == null)
            target = attacker;
    }

    @Override
    public void update(GamePanel gp) {
        if (!active) {
            if (respawnTimer > 0) {
                respawnTimer--;
                if (respawnTimer == 0) {
                    active = true;
                    hp = maxHp;
                    x = spawnX;
                    y = spawnY;
                    target = null;
                }
            }
            return;
        }
        if (attackCooldown > 0)
            attackCooldown--;

        if (target != null && (!target.active || distanceTo(target) > 400)) {
            target = null; // drop aggro
        }

        if (target != null) {
            double dx = target.x + target.width / 2 - (x + width / 2);
            double dy = target.y + target.height / 2 - (y + height / 2);
            double length = Math.sqrt(dx * dx + dy * dy);

            if (length > 60) {
                isMoving = true;
                moveTimer++;
                x += (dx / length) * speed;
                y += (dy / length) * speed;
            } else {
                isMoving = false;
                if (attackCooldown <= 0) {
                    target.takeDamage(40);
                    attackCooldown = 50;
                }
            }
        } else {
            // Return to spawn
            double dx = spawnX - x;
            double dy = spawnY - y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length > 5) {
                isMoving = true;
                moveTimer++;
                x += (dx / length) * speed;
                y += (dy / length) * speed;
            } else {
                isMoving = false;
                // Heal up if idle
                if (hp < maxHp)
                    hp += 2;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        if (image != null) {
            drawSprite(g2, image, gp);
        } else {
            g2.setColor(new Color(139, 69, 19)); // Brown
            g2.fillOval((int) x, (int) y, width, height);
        }
        g2.setColor(Color.RED);
        g2.fillRect((int) x, (int) y - 10, width, 6);
        g2.setColor(Color.GREEN);
        g2.fillRect((int) x, (int) y - 10, (int) (width * ((double) hp / maxHp)), 6);
    }
}
