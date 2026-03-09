package folder_java;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Bullet extends Entity {
    double dx, dy, startAngle;
    int lifetime = 100;
    boolean pierce;
    int ownerType; // 0=Player, 1=Minion1, 2=Minion2, 3=Npc1, 4=Npc2, 5=GiantBlue, 6=GiantOrange

    public Bullet(double x, double y, double dx, double dy, int team, int ownerType, double angle, boolean pierce) {
        super(x, y, ownerType >= 5 ? 100 : 40, ownerType >= 5 ? 100 : 40, 1,
                ownerType == 5 ? 50.0
                        : (ownerType == 0 ? 14.0
                                : (ownerType == 3 || ownerType == 4 ? 13.0 : (ownerType == 6 ? 7.0 : 9.0))),
                team);
        this.dx = dx;
        this.dy = dy;
        this.ownerType = ownerType;
        this.startAngle = angle;
        this.pierce = pierce;
        if (ownerType >= 5)
            lifetime = 220;
    }

    @Override
    public void update(GamePanel gp) {
        x += dx * speed;
        y += dy * speed;
        lifetime--;
        if (lifetime <= 0)
            active = false;

        if (team == 1) {
            for (Minion e : gp.minions) {
                if (e.team == 2 && e.active && intersects(e)) {
                    e.takeDamage(ownerType == 5 ? e.maxHp / 2 : 50);
                    if (ownerType == 5) { // Impact particles
                        for (int i = 0; i < 5; i++)
                            gp.particles.add(new Particle(e.x + e.width / 2, e.y + e.height / 2, Color.RED));
                    }
                    if (!pierce) {
                        active = false;
                        break;
                    }
                }
            }
            if (active) {
                for (NpcHero e : gp.npcHeroes) {
                    if (e.team == 2 && e.active && intersects(e)) {
                        gp.playerAggroTimer = 300; // Trigger tower aggro
                        e.takeDamage(ownerType == 5 ? e.maxHp / 2 : 50);
                        if (ownerType == 5) {
                            for (int i = 0; i < 5; i++)
                                gp.particles.add(new Particle(e.x + e.width / 2, e.y + e.height / 2, Color.RED));
                        }
                        if (!pierce) {
                            active = false;
                            break;
                        }
                    }
                }
            }
            if (active) {
                for (Tower t : gp.towers) {
                    if (t.team == 2 && t.active && intersects(t)) {
                        t.takeDamage(ownerType >= 5 ? 80 : 50);
                        if (ownerType == 5) {
                            for (int i = 0; i < 5; i++)
                                gp.particles.add(new Particle(t.x + t.width / 2, t.y + t.height / 2, Color.RED));
                        }
                        if (!pierce) {
                            active = false;
                            break;
                        }
                    }
                }
            }
            if (active && gp.enemyBase.active && intersects(gp.enemyBase)) {
                gp.enemyBase.takeDamage(ownerType >= 5 ? 80 : 50);
                if (!pierce)
                    active = false;
            }
            if (active) {
                for (JungleMonster jm : gp.jungles) {
                    if (jm.active && intersects(jm)) {
                        jm.takeDamageFrom(ownerType >= 5 ? 80 : 50, gp.player);
                        if (!pierce) {
                            active = false;
                            break;
                        }
                    }
                }
            }
        } else {
            if (gp.player.active && intersects(gp.player)) {
                gp.player.takeDamage(ownerType >= 5 ? 60 : 20);
                if (!pierce)
                    active = false;
            }
            if (active) {
                for (Minion e : gp.minions) {
                    if (e.team == 1 && e.active && intersects(e)) {
                        e.takeDamage(ownerType >= 5 ? 60 : 20);
                        if (!pierce) {
                            active = false;
                            break;
                        }
                    }
                }
            }
            if (active) {
                for (Tower t : gp.towers) {
                    if (t.team == 1 && t.active && intersects(t)) {
                        t.takeDamage(ownerType >= 5 ? 60 : 20);
                        if (!pierce) {
                            active = false;
                            break;
                        }
                    }
                }
            }
            if (active && gp.playerBase.active && intersects(gp.playerBase)) {
                gp.playerBase.takeDamage(ownerType >= 5 ? 60 : 20);
                if (!pierce)
                    active = false;
            }
            if (active) {
                for (JungleMonster jm : gp.jungles) {
                    if (jm.active && intersects(jm)) {
                        jm.takeDamageFrom(ownerType >= 5 ? 60 : 20,
                                gp.npcHeroes.isEmpty() ? null : gp.npcHeroes.get(0));
                        if (!pierce) {
                            active = false;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform old = g2.getTransform();
        g2.translate(x + width / 2, y + height / 2);
        g2.rotate(startAngle);
        int r = width / 2;

        switch (ownerType) {
            case 0: // Player basic - Blue electric orb
                g2.setColor(new Color(0, 180, 255, 60));
                g2.fillOval(-r - 8, -r - 8, (r + 8) * 2, (r + 8) * 2);
                g2.setColor(new Color(0, 210, 255));
                g2.fillOval(-r, -r, r * 2, r * 2);
                g2.setColor(new Color(180, 240, 255));
                g2.fillOval(-r / 2, -r / 2, r, r);
                g2.setColor(Color.WHITE);
                g2.fillOval(-r / 4, -r / 4, r / 2, r / 2);
                break;
            case 1: // Minion Blue - Cyan dart
                g2.setColor(new Color(0, 255, 220, 80));
                g2.fillOval(-r - 4, -r - 4, (r + 4) * 2, (r + 4) * 2);
                g2.setColor(Color.CYAN);
                g2.fillOval(-r, -r, r * 2, r * 2);
                g2.setColor(Color.WHITE);
                g2.fillOval(-r / 3, -r / 3, r * 2 / 3, r * 2 / 3);
                break;
            case 2: // Minion Red - Red dart
                g2.setColor(new Color(255, 50, 50, 80));
                g2.fillOval(-r - 4, -r - 4, (r + 4) * 2, (r + 4) * 2);
                g2.setColor(new Color(255, 60, 60));
                g2.fillOval(-r, -r, r * 2, r * 2);
                g2.setColor(new Color(255, 200, 200));
                g2.fillOval(-r / 3, -r / 3, r * 2 / 3, r * 2 / 3);
                break;
            case 3: // Npc Blue - crescent blade
                g2.setColor(new Color(50, 130, 255, 90));
                g2.fillOval(-r - 6, -r - 6, (r + 6) * 2, (r + 6) * 2);
                g2.setColor(new Color(80, 160, 255));
                g2.fillArc(-r, -r * 2, r * 2, r * 4, 60, 60);
                g2.setColor(new Color(180, 210, 255));
                g2.fillArc(-r / 2, -r * 2 + r / 2, r, r * 3, 65, 50);
                break;
            case 4: // Npc Orange - fire blade
                g2.setColor(new Color(255, 100, 0, 90));
                g2.fillOval(-r - 6, -r - 6, (r + 6) * 2, (r + 6) * 2);
                g2.setColor(new Color(255, 120, 0));
                g2.fillArc(-r, -r * 2, r * 2, r * 4, 60, 60);
                g2.setColor(new Color(255, 220, 80));
                g2.fillArc(-r / 2, -r * 2 + r / 2, r, r * 3, 65, 50);
                break;
            case 5: // Giant Red - Ultra Laser
                g2.rotate(-Math.PI / 2); // Align elongated beam with travel direction
                g2.setColor(new Color(255, 0, 0, 150));
                g2.fillRect(-r * 40, -r, r * 80, r * 2); // Elongated red beam
                g2.setColor(new Color(255, 255, 255, 240));
                g2.fillRect(-r * 40, -r / 3, r * 80, r * 2 / 3); // Bright white core
                for (int i = 0; i < 25; i++) {
                    int px = (int) (Math.random() * r * 80 - r * 40);
                    int py = (int) (Math.random() * r * 2 - r);
                    g2.setColor(new Color(255, 150, 150));
                    g2.fillOval(px, py, 4, 4);
                }
                break;
            case 6: // Giant Orange - Boss Ultimate
                for (int i = 0; i < 3; i++) {
                    g2.setColor(new Color(255, 100, 0, 40));
                    int s = (int) (r * 2 + Math.cos(System.currentTimeMillis() * 0.01 + i) * 12);
                    g2.drawOval(-s / 2, -s / 2, s, s);
                }
                g2.setColor(new Color(255, 80, 0));
                g2.fillOval(-r, -r, r * 2, r * 2);
                g2.setColor(new Color(255, 200, 50));
                g2.fillOval(-r + 8, -r + 8, (r - 8) * 2, (r - 8) * 2);
                g2.setColor(Color.WHITE);
                for (int i = 0; i < 6; i++) {
                    double starA = i * Math.PI / 3 - System.currentTimeMillis() * 0.007;
                    g2.fillRect((int) (Math.cos(starA) * r * 0.7) - 3, (int) (Math.sin(starA) * r * 0.7) - 3, 6, 6);
                }
                break;
        }
        g2.setTransform(old);
    }
}
