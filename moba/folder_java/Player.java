package folder_java;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player extends Entity {
    KeyHandler keyH;
    int attackCooldown = 0;
    int skill1CD = 0, skill2CD = 0, skill3CD = 0;
    public int level = 1, exp = 0, nextLevelExp = 100;
    public int laserChargeTimer = 0;
    public int respawnTimer = 0;
    static BufferedImage image;

    static {
        try {
            image = SpriteUtils.makeTransparent(ImageIO.read(new File("res/player.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Player(int x, int y, KeyHandler keyH) {
        super(x, y, 192, 192, 2000, 4.0, 1);
        this.keyH = keyH;
    }

    @Override
    public void update(GamePanel gp) {
        if (!active) {
            respawnTimer++;
            if (respawnTimer > 180) { // 3 seconds respawn
                hp = maxHp;
                x = 100;
                y = gp.screenHeight - 150;
                active = true;
                respawnTimer = 0;
            }
            return;
        }

        double oldX = x, oldY = y;
        isMoving = false;

        if (keyH.upPressed)
            y -= speed;
        if (keyH.downPressed)
            y += speed;
        if (keyH.leftPressed)
            x -= speed;
        if (keyH.rightPressed)
            x += speed;

        if (x != oldX || y != oldY) {
            isMoving = true;
            moveTimer++;
            faceAngle = Math.atan2(y - oldY, x - oldX) + Math.PI / 2;
        }

        if (x < 0)
            x = 0;
        if (x > gp.worldWidth - width)
            x = gp.worldWidth - width;
        if (y < 0)
            y = 0;
        if (y > gp.worldHeight - height)
            y = gp.worldHeight - height;

        if (attackCooldown > 0)
            attackCooldown--;
        if (skill1CD > 0)
            skill1CD--;
        if (skill2CD > 0)
            skill2CD--;
        if (skill3CD > 0)
            skill3CD--;

        if (gp.mouseH.mousePressed && attackCooldown <= 0) {
            double mx = (gp.mouseH.mouseX / gp.zoom) + gp.cameraX;
            double my = (gp.mouseH.mouseY / gp.zoom) + gp.cameraY;
            double dx = mx - (x + width / 2);
            double dy = my - (y + height / 2);
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length > 0) {
                dx /= length;
                dy /= length;
                faceAngle = Math.atan2(dy, dx) + Math.PI / 2;
                gp.bullets.add(new Bullet(x + width / 2, y + height / 2, dx, dy, 1, 0, faceAngle, false));
                attackCooldown = 15;
            }
        }

        // Skill 1 (U): Dash towards mouse cursor
        if (keyH.uPressed && skill1CD <= 0) {
            double mx = (gp.mouseH.mouseX / gp.zoom) + gp.cameraX;
            double my = (gp.mouseH.mouseY / gp.zoom) + gp.cameraY;
            double dx = mx - (x + width / 2), dy = my - (y + height / 2);
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len > 0) {
                x += (dx / len) * 300;
                y += (dy / len) * 300; // Large dash
                for (int i = 0; i < 15; i++)
                    gp.particles.add(new Particle(x + width / 2, y + height / 2, Color.WHITE));
                skill1CD = 240;
            }
        }

        // Skill 2 (I): 3-Way Shot
        if (keyH.iPressed && skill2CD <= 0) {
            double baseA = faceAngle - Math.PI / 2;
            for (int i = -1; i <= 1; i++) {
                double a = baseA + i * 0.25;
                gp.bullets.add(new Bullet(x + width / 2, y + height / 2, Math.cos(a), Math.sin(a), 1, 0,
                        a + Math.PI / 2, false));
            }
            skill2CD = 180;
        }

        // Skill 3 (O): Ultimate - Ultra Laser (1.5s charge)
        if (keyH.oPressed && skill3CD <= 0) {
            laserChargeTimer = 90; // 1.5 seconds at 60fps
            skill3CD = 900;
        }

        if (laserChargeTimer > 0) {
            laserChargeTimer--;
            gp.particles.add(new Particle(x + width / 2 + Math.random() * 40 - 20,
                    y + height / 2 + Math.random() * 40 - 20, Color.CYAN));
            if (laserChargeTimer == 0) {
                double a = faceAngle - Math.PI / 2;
                gp.bullets.add(
                        new Bullet(x + width / 2, y + height / 2, Math.cos(a), Math.sin(a), 1, 5, faceAngle, true));
            }
        }

        // Leveling logic check
        if (exp >= nextLevelExp && level < 15) {
            level++;
            exp -= nextLevelExp;
            nextLevelExp += 50;
            maxHp += 200;
            hp = maxHp; // Full heal on level up
            for (int i = 0; i < 20; i++)
                gp.particles.add(new Particle(x + width / 2, y + height / 2, Color.YELLOW));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active)
            return;
        if (image != null) {
            drawSprite(g2, image, gp);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillRect((int) x, (int) y, width, height);
        }

        g2.setColor(Color.RED);
        g2.fillRect((int) x, (int) y - 12, width, 8);
        g2.setColor(Color.GREEN);
        g2.fillRect((int) x, (int) y - 12, (int) (width * ((double) hp / maxHp)), 8);

        // Draw level next to health bar
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("Lv." + level, (int) x + width + 5, (int) y - 2);

        // Draw skill cooldown bars (U, I, O)
        int barW = width / 3;
        if (skill1CD > 0) {
            g2.setColor(Color.YELLOW);
            g2.fillRect((int) x, (int) y + height + 5, (int) (barW * (1.0 - (double) skill1CD / 180)), 4);
        }
        if (skill2CD > 0) {
            g2.setColor(Color.ORANGE);
            g2.fillRect((int) x + barW, (int) y + height + 5, (int) (barW * (1.0 - (double) skill2CD / 300)), 4);
        }
        if (skill3CD > 0) {
            g2.setColor(Color.MAGENTA);
            g2.fillRect((int) x + barW * 2, (int) y + height + 5, (int) (barW * (1.0 - (double) skill3CD / 600)), 4);
        }
    }
}
