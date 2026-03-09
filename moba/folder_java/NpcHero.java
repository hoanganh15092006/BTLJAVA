package folder_java;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class NpcHero extends Entity {
    Entity target;
    int attackCooldown = 0;
    int skill1CD = 0, skill2CD = 0, skill3CD = 0;
    public int respawnTimer = 0;
    int spawnX, spawnY;
    static BufferedImage image;

    static {
        try {
            image = SpriteUtils.makeTransparent(ImageIO.read(new File("res/npc.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NpcHero(int x, int y, int team) {
        super(x, y, 144, 144, 6000, 2.5, team);
        spawnX = x;
        spawnY = y;
    }

    @Override
    public void update(GamePanel gp) {
        if (!active) {
            respawnTimer++;
            if (respawnTimer > 300) { // 5 seconds respawn
                hp = maxHp;
                x = spawnX;
                y = spawnY;
                active = true;
                respawnTimer = 0;
            }
            return;
        }

        if (attackCooldown > 0)
            attackCooldown--;
        if (skill1CD > 0)
            skill1CD--;
        if (skill2CD > 0)
            skill2CD--;
        if (skill3CD > 0)
            skill3CD--;
        target = null;
        double minDst = 500;

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

            if (length > 200) {
                isMoving = true;
                moveTimer++;
                x += (dx / length) * speed;
                y += (dy / length) * speed;
                faceAngle = Math.atan2(dy, dx) + Math.PI / 2;
            } else {
                isMoving = false;
                faceAngle = Math.atan2(dy, dx) + Math.PI / 2;
                double baseA = Math.atan2(dy, dx);

                if (skill3CD <= 0 && length > 50) {
                    // Ultimate (Giant Blast)
                    gp.bullets.add(new Bullet(x + width / 2, y + height / 2, dx / length, dy / length, team,
                            team == 1 ? 5 : 6, faceAngle, true));
                    skill3CD = 800;
                    attackCooldown = 30;
                } else if (skill2CD <= 0 && length < 100) {
                    // Circle Burst
                    for (int i = 0; i < 8; i++) {
                        double a = i * (Math.PI / 4);
                        gp.bullets.add(new Bullet(x + width / 2, y + height / 2, Math.cos(a), Math.sin(a), team,
                                team == 1 ? 3 : 4, a + Math.PI / 2, false));
                    }
                    skill2CD = 400;
                    attackCooldown = 30;
                } else if (skill1CD <= 0) {
                    // Spread Shot
                    for (int i = -1; i <= 1; i++) {
                        double a = baseA + i * 0.3;
                        gp.bullets.add(new Bullet(x + width / 2, y + height / 2, Math.cos(a), Math.sin(a), team,
                                team == 1 ? 3 : 4, a + Math.PI / 2, false));
                    }
                    skill1CD = 200;
                    attackCooldown = 30;
                } else if (attackCooldown <= 0) {
                    gp.bullets.add(new Bullet(x + width / 2, y + height / 2, dx / length, dy / length, team,
                            team == 1 ? 3 : 4, faceAngle, false));
                    attackCooldown = 40;
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
        if (!active)
            return;
        if (image != null) {
            drawSprite(g2, image, gp);
        } else {
            g2.setColor(team == 1 ? new Color(0, 100, 255) : new Color(255, 100, 0));
            g2.fillRoundRect((int) x, (int) y, width, height, 10, 10);
        }

        g2.setColor(Color.RED);
        g2.fillRect((int) x, (int) y - 14, width, 8);
        g2.setColor(Color.GREEN);
        g2.fillRect((int) x, (int) y - 14, (int) (width * ((double) hp / maxHp)), 8);

        // Draw skill cooldown bars
        int barW = width / 3;
        if (skill1CD > 0) {
            g2.setColor(Color.YELLOW);
            g2.fillRect((int) x, (int) y + height + 5, (int) (barW * (1.0 - (double) skill1CD / 200)), 4);
        }
        if (skill2CD > 0) {
            g2.setColor(Color.ORANGE);
            g2.fillRect((int) x + barW, (int) y + height + 5, (int) (barW * (1.0 - (double) skill2CD / 400)), 4);
        }
        if (skill3CD > 0) {
            g2.setColor(Color.MAGENTA);
            g2.fillRect((int) x + barW * 2, (int) y + height + 5, (int) (barW * (1.0 - (double) skill3CD / 800)), 4);
        }
    }
}
