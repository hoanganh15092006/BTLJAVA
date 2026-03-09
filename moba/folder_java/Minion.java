package folder_java;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Minion extends Entity {
    Entity target;
    int attackCooldown = 0;
    static BufferedImage imageBlue, imageRed;

    static {
        try {
            imageBlue = SpriteUtils.makeTransparent(ImageIO.read(new File("res/minion_blue.png")));
            imageRed = SpriteUtils.makeTransparent(ImageIO.read(new File("res/minion_red.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Minion(int x, int y, int team) {
        super(x, y, 144, 144, 200, 1.5, team);
    }

    @Override
    public void update(GamePanel gp) {
        if (attackCooldown > 0)
            attackCooldown--;
        target = null;
        double minDst = 300;

        if (team == 1) {
            for (Minion e : gp.minions) {
                if (e.team == 2 && e.active) {
                    double d = distanceTo(e);
                    if (d < minDst) {
                        minDst = d;
                        target = e;
                    }
                }
            }
            for (NpcHero e : gp.npcHeroes) {
                if (e.team == 2 && e.active) {
                    double d = distanceTo(e);
                    if (d < minDst) {
                        minDst = d;
                        target = e;
                    }
                }
            }
            if (target == null) {
                for (Tower t : gp.towers) {
                    if (t.team == 2 && t.active) {
                        double d = distanceTo(t);
                        if (d < minDst) {
                            minDst = d;
                            target = t;
                        }
                    }
                }
            }
            if (target == null && gp.enemyBase.active)
                target = gp.enemyBase;
        } else {
            if (gp.player.active) {
                double d = distanceTo(gp.player);
                if (d < minDst) {
                    minDst = d;
                    target = gp.player;
                }
            }
            for (Minion e : gp.minions) {
                if (e.team == 1 && e.active) {
                    double d = distanceTo(e);
                    if (d < minDst) {
                        minDst = d;
                        target = e;
                    }
                }
            }
            for (NpcHero e : gp.npcHeroes) {
                if (e.team == 1 && e.active) {
                    double d = distanceTo(e);
                    if (d < minDst) {
                        minDst = d;
                        target = e;
                    }
                }
            }
            if (target == null) {
                for (Tower t : gp.towers) {
                    if (t.team == 1 && t.active) {
                        double d = distanceTo(t);
                        if (d < minDst) {
                            minDst = d;
                            target = t;
                        }
                    }
                }
            }
            if (target == null && gp.playerBase.active)
                target = gp.playerBase;
        }

        if (target != null) {
            double dx = target.x + target.width / 2 - (x + width / 2);
            double dy = target.y + target.height / 2 - (y + height / 2);
            double length = Math.sqrt(dx * dx + dy * dy);

            if (length > 100) {
                isMoving = true;
                moveTimer++;
                x += (dx / length) * speed;
                y += (dy / length) * speed;
                faceAngle = Math.atan2(dy, dx) + Math.PI / 2;
            } else {
                isMoving = false;
                if (attackCooldown <= 0) {
                    faceAngle = Math.atan2(dy, dx) + Math.PI / 2;
                    gp.bullets.add(new Bullet(x + width / 2, y + height / 2, dx / length, dy / length, team,
                            team == 1 ? 1 : 2, faceAngle, false));
                    attackCooldown = 60;
                }
            }
        } else {
            isMoving = false;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        BufferedImage img = team == 1 ? imageBlue : imageRed;
        if (img != null) {
            drawSprite(g2, img, gp);
        } else {
            g2.setColor(team == 1 ? Color.CYAN : Color.RED);
            g2.fillOval((int) x, (int) y, width, height);
        }

        g2.setColor(Color.BLACK);
        g2.fillRect((int) x, (int) y - 10, width, 6);
        g2.setColor(Color.RED);
        g2.fillRect((int) x, (int) y - 10, (int) (width * ((double) hp / maxHp)), 6);
    }
}
