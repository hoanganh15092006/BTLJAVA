package folder_java;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

public abstract class Entity {
    public double x, y;
    public int width, height;
    public int hp, maxHp;
    public double speed;
    public boolean active = true;
    public int team; // 1 = player side, 2 = enemy side

    public double faceAngle = 0;
    public int moveTimer = 0;
    public boolean isMoving = false;

    public Entity(double x, double y, int width, int height, int hp, double speed, int team) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hp = hp;
        this.maxHp = hp;
        this.speed = speed;
        this.team = team;
    }

    public void drawSprite(Graphics2D g2, BufferedImage img, GamePanel gp) {
        if (img == null)
            return;
        AffineTransform old = g2.getTransform();

        double drawX = x + width / 2;
        double drawY = y + height / 2;

        if (isMoving) {
            drawY += Math.sin(moveTimer * 0.4) * 4; // bobbing effect
            if (moveTimer % 5 == 0 && gp != null) {
                gp.particles.add(new Particle(drawX, drawY + height / 3, team == 1 ? Color.CYAN : Color.RED));
            }
        }

        g2.translate(drawX, drawY);
        g2.rotate(faceAngle);
        g2.drawImage(img, -width / 2, -height / 2, width, height, null);

        g2.setTransform(old);
    }

    public abstract void update(GamePanel gp);

    public abstract void draw(Graphics2D g2);

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0)
            active = false;
    }

    public boolean intersects(Entity other) {
        return (x < other.x + other.width && x + width > other.x &&
                y < other.y + other.height && y + height > other.y);
    }

    public double distanceTo(Entity other) {
        double dx = (this.x + this.width / 2) - (other.x + other.width / 2);
        double dy = (this.y + this.height / 2) - (other.y + other.height / 2);
        return Math.sqrt(dx * dx + dy * dy);
    }
}
