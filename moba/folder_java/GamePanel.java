package folder_java;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {
    int screenWidth, screenHeight;
    int worldWidth = 4000, worldHeight = 3000;
    int cameraX = 0, cameraY = 0;
    Thread gameThread;
    KeyHandler keyH = new KeyHandler();
    MouseHandler mouseH = new MouseHandler();

    Player player;
    Base playerBase, enemyBase;

    ArrayList<Minion> minions = new ArrayList<>();
    ArrayList<NpcHero> npcHeroes = new ArrayList<>();
    ArrayList<JungleMonster> jungles = new ArrayList<>();
    ArrayList<Tower> towers = new ArrayList<>();
    java.util.List<Particle> particles = java.util.Collections.synchronizedList(new ArrayList<>());
    java.util.List<Bullet> bullets = java.util.Collections.synchronizedList(new ArrayList<>());

    int playerAggroTimer = 0;
    int spawnTimer = 0;
    int gameState = 0; // 0=Menu, 1=Playing, 2=Victory, 3=Defeat
    double zoom = 1.0;
    MobaGame mainApp;

    public GamePanel(Dimension size, MobaGame mainApp) {
        this.mainApp = mainApp;
        this.screenWidth = size.width;
        this.screenHeight = size.height;
        this.setPreferredSize(size);
        this.setBackground(new Color(34, 139, 34));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.addMouseListener(mouseH);
        this.addMouseMotionListener(mouseH);
        this.setFocusable(true);
        setupGame();
        gameState = 0; // Ensure we start at menu
    }

    public void setupGame() {
        playerBase = new Base(200, worldHeight - 300, true);
        enemyBase = new Base(worldWidth - 350, 200, false);
        player = new Player(350, worldHeight - 450, keyH);
        minions.clear();
        bullets.clear();
        npcHeroes.clear();
        jungles.clear();
        towers.clear();
        particles.clear();
        gameState = 1; // Playing
        npcHeroes.add(new NpcHero(worldWidth - 600, 400, 2));

        // Jungle camps - moved to sides and away from corner towers
        jungles.add(new JungleMonster(worldWidth / 2 - 500, worldHeight / 2 - 800));
        jungles.add(new JungleMonster(worldWidth / 2 + 500, worldHeight / 2 + 800));
        jungles.add(new JungleMonster(worldWidth / 2 - 1200, worldHeight / 2));
        jungles.add(new JungleMonster(worldWidth / 2 + 1200, worldHeight / 2));

        // Towers relocated: 3 along the diagonal lane for each side
        // Team 1 (Blue)
        Tower t1Outer = new Tower(1200, worldHeight - 1200, 1);
        Tower t1Mid = new Tower(800, worldHeight - 800, 1);
        Tower t1Inner = new Tower(400, worldHeight - 400, 1);
        t1Inner.requiredTower = t1Mid;
        t1Mid.requiredTower = t1Outer;
        playerBase.requiredTower = t1Inner;

        towers.add(t1Outer);
        towers.add(t1Mid);
        towers.add(t1Inner);

        // Team 2 (Red)
        Tower t2Outer = new Tower(worldWidth - 1200, 1200, 2);
        Tower t2Mid = new Tower(worldWidth - 800, 800, 2);
        Tower t2Inner = new Tower(worldWidth - 400, 400, 2);
        t2Inner.requiredTower = t2Mid;
        t2Mid.requiredTower = t2Outer;
        enemyBase.requiredTower = t2Inner;

        towers.add(t2Outer);
        towers.add(t2Mid);
        towers.add(t2Inner);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / 60;
        double delta = 0;
        long lastTime = System.nanoTime(), currentTime;
        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        if (keyH.fPressed) {
            mainApp.toggleFullscreen();
            keyH.fPressed = false; // consume event
        }

        if (gameState == 0) {
            if (keyH.rPressed || mouseH.mousePressed) {
                setupGame();
            }
            return;
        }

        if (gameState != 1) {
            if (keyH.rPressed)
                setupGame();
            return;
        }

        if (keyH.plusPressed)
            zoom = Math.min(zoom + 0.01, 2.0);
        if (keyH.minusPressed)
            zoom = Math.max(zoom - 0.01, 0.5);

        if (!playerBase.active)
            gameState = 3; // Defeat
        if (!enemyBase.active)
            gameState = 2; // Victory

        player.update(this);

        // Camera follows player
        cameraX = (int) (player.x + player.width / 2 - (screenWidth / zoom) / 2);
        cameraY = (int) (player.y + player.height / 2 - (screenHeight / zoom) / 2);
        int maxCamX = Math.max(0, worldWidth - (int) (screenWidth / zoom));
        int maxCamY = Math.max(0, worldHeight - (int) (screenHeight / zoom));
        cameraX = Math.max(0, Math.min(cameraX, maxCamX));
        cameraY = Math.max(0, Math.min(cameraY, maxCamY));

        spawnTimer++;
        if (spawnTimer >= 180) {
            minions.add(new Minion((int) enemyBase.x + 30, (int) enemyBase.y + 140, 2));
            minions.add(new Minion((int) playerBase.x + 130, (int) playerBase.y + 30, 1));
            spawnTimer = 0;
        }

        for (int i = 0; i < minions.size(); i++)
            if (minions.get(i).active)
                minions.get(i).update(this);
        for (int i = 0; i < bullets.size(); i++)
            if (bullets.get(i).active)
                bullets.get(i).update(this);
        for (int i = 0; i < npcHeroes.size(); i++)
            npcHeroes.get(i).update(this);
        for (int i = 0; i < jungles.size(); i++)
            jungles.get(i).update(this);
        for (int i = 0; i < towers.size(); i++)
            if (towers.get(i).active)
                towers.get(i).update(this);
        for (int i = 0; i < particles.size(); i++)
            particles.get(i).life--;

        if (playerAggroTimer > 0)
            playerAggroTimer--;

        minions.removeIf(m -> {
            if (!m.active) {
                if (m.team == 2)
                    player.exp += 30;
                return true;
            }
            return false;
        });

        synchronized (bullets) {
            bullets.removeIf(b -> !b.active);
        }

        // jungles should not be removed as they respawn now
        for (JungleMonster j : jungles) {
            if (!j.active && j.respawnTimer == 1800) { // Just died
                player.hp = Math.min(player.hp + 300, player.maxHp);
                player.exp += 150;
                // Add particles for death/exp
                for (int i = 0; i < 10; i++)
                    synchronized (particles) {
                        particles.add(new Particle(j.x + j.width / 2, j.y + j.height / 2, Color.YELLOW));
                    }
            }
        }

        towers.removeIf(t -> !t.active);
        synchronized (particles) {
            particles.removeIf(p -> p.life <= 0);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameState == 0) {
            drawStartScreen(g2);
            return;
        }

        AffineTransform oldTransform = g2.getTransform();
        g2.scale(zoom, zoom);
        g2.translate(-cameraX, -cameraY);
        drawMap(g2);

        if (playerBase.active)
            playerBase.draw(g2, this);
        if (enemyBase.active)
            enemyBase.draw(g2, this);
        player.draw(g2, this);
        for (int i = 0; i < minions.size(); i++) {
            Minion e = minions.get(i);
            if (e != null && e.active)
                e.draw(g2, this);
        }
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            if (b != null && b.active)
                b.draw(g2);
        }
        for (int i = 0; i < npcHeroes.size(); i++) {
            if (npcHeroes.get(i) != null)
                npcHeroes.get(i).draw(g2, this);
        }
        for (int i = 0; i < jungles.size(); i++) {
            JungleMonster j = jungles.get(i);
            if (j != null && j.active)
                j.draw(g2, this);
        }
        for (int i = 0; i < towers.size(); i++) {
            Tower t = towers.get(i);
            if (t != null && t.active)
                t.draw(g2);
        }
        for (int i = 0; i < particles.size(); i++) {
            if (particles.get(i) != null)
                particles.get(i).draw(g2);
        }

        g2.setTransform(oldTransform); // restore for HUD

        // HUD bar
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(8, 8, 530, 30, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.drawString("Lv. " + player.level + " (" + player.exp + "/" + player.nextLevelExp
                + ") | WASD move | Click shoot | U Dash | I 3-Way | O Ultra Laser", 14, 28);

        // Experience Bar
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(screenWidth / 2 - 150, screenHeight - 25, 300, 10);
        g2.setColor(new Color(200, 200, 0));
        g2.fillRect(screenWidth / 2 - 150, screenHeight - 25, (int) (300 * ((double) player.exp / player.nextLevelExp)),
                10);

        // Minimap
        int mmW = 200, mmH = (int) (200.0 * worldHeight / worldWidth), mmX = screenWidth - mmW - 12,
                mmY = screenHeight - mmH - 12;
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(mmX - 2, mmY - 2, mmW + 4, mmH + 4, 8, 8);
        g2.setColor(new Color(30, 90, 30, 200));
        g2.fillRect(mmX, mmY, mmW, mmH);

        // Lane on minimap
        g2.setColor(new Color(180, 140, 70, 180));
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(mmX, mmY + mmH, mmX + mmW, mmY);
        g2.setStroke(new BasicStroke(1));

        // Minimap entity icons
        for (int i = 0; i < towers.size(); i++) {
            Tower t = towers.get(i);
            if (t != null && t.active) {
                int mx = mmX + (int) (t.x / worldWidth * mmW), my = mmY + (int) (t.y / worldHeight * mmH);
                g2.setColor(t.team == 1 ? new Color(0, 200, 255) : new Color(255, 60, 60));
                g2.fillRect(mx - 4, my - 4, 9, 9);
            }
        }

        g2.setColor(new Color(0, 220, 220));
        g2.fillRect(mmX + (int) (playerBase.x / worldWidth * mmW) - 4,
                mmY + (int) (playerBase.y / worldHeight * mmH) - 4, 10, 10);
        g2.setColor(new Color(220, 0, 220));
        g2.fillRect(mmX + (int) (enemyBase.x / worldWidth * mmW) - 4, mmY + (int) (enemyBase.y / worldHeight * mmH) - 4,
                10, 10);

        for (int i = 0; i < minions.size(); i++) {
            Minion e = minions.get(i);
            if (e != null && e.active) {
                g2.setColor(e.team == 1 ? Color.CYAN : Color.RED);
                g2.fillOval(mmX + (int) (e.x / worldWidth * mmW) - 2, mmY + (int) (e.y / worldHeight * mmH) - 2, 5, 5);
            }
        }

        for (int i = 0; i < jungles.size(); i++) {
            JungleMonster j = jungles.get(i);
            if (j != null && j.active) {
                g2.setColor(new Color(200, 180, 0));
                g2.fillOval(mmX + (int) (j.x / worldWidth * mmW) - 3, mmY + (int) (j.y / worldHeight * mmH) - 3, 7, 7);
            }
        }

        for (int i = 0; i < npcHeroes.size(); i++) {
            NpcHero e = npcHeroes.get(i);
            if (e != null && e.active) {
                g2.setColor(new Color(255, 50, 50));
                int[] xs = { mmX + (int) (e.x / worldWidth * mmW), mmX + (int) (e.x / worldWidth * mmW) - 5,
                        mmX + (int) (e.x / worldWidth * mmW) + 5 };
                int[] ys = { mmY + (int) (e.y / worldHeight * mmH) - 6, mmY + (int) (e.y / worldHeight * mmH) + 4,
                        mmY + (int) (e.y / worldHeight * mmH) + 4 };
                g2.fillPolygon(xs, ys, 3);
            }
        }

        g2.setColor(Color.WHITE);
        g2.fillOval(mmX + (int) (player.x / worldWidth * mmW) - 5, mmY + (int) (player.y / worldHeight * mmH) - 5, 11,
                11);
        g2.setColor(new Color(0, 220, 255));
        g2.fillOval(mmX + (int) (player.x / worldWidth * mmW) - 4, mmY + (int) (player.y / worldHeight * mmH) - 4, 9,
                9);

        // Minimap Legend
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        int lx = mmX, ly = mmY - 45;
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(lx - 5, ly - 10, 150, 50, 5, 5);
        g2.setColor(Color.WHITE);
        g2.fillRect(lx, ly, 8, 8);
        g2.drawString("Base", lx + 12, ly + 8);
        g2.setColor(new Color(0, 200, 255));
        g2.fillRect(lx + 50, ly, 8, 8);
        g2.drawString("Tower", lx + 62, ly + 8);
        g2.setColor(new Color(200, 180, 0));
        g2.fillOval(lx + 105, ly, 8, 8);
        g2.drawString("Jungle", lx + 117, ly + 8);
        g2.setColor(Color.CYAN);
        g2.fillOval(lx, ly + 15, 6, 6);
        g2.drawString("Minion", lx + 12, ly + 23);
        g2.setColor(Color.WHITE);
        g2.drawOval(lx + 50, ly + 15, 8, 8);
        g2.drawString("You", lx + 62, ly + 23);

        // Viewport rect
        g2.setColor(new Color(255, 255, 255, 100));
        g2.drawRect(mmX + (int) ((double) cameraX / worldWidth * mmW),
                mmY + (int) ((double) cameraY / worldHeight * mmH),
                (int) ((double) screenWidth / worldWidth * mmW), (int) ((double) screenHeight / worldHeight * mmH));

        if (!player.active) {
            g2.setColor(new Color(180, 0, 0, 190));
            g2.fillRoundRect(screenWidth / 2 - 150, 38, 300, 42, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 26));
            g2.drawString("Respawning in " + (3 - player.respawnTimer / 60) + "s", screenWidth / 2 - 125, 67);
        }

        if (gameState > 1) {
            g2.setColor(new Color(0, 0, 0, 170));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.setFont(new Font("Arial", Font.BOLD, 72));
            if (gameState == 2) {
                g2.setColor(new Color(255, 215, 0));
                g2.drawString("VICTORY!", screenWidth / 2 - 180, screenHeight / 2);
            } else {
                g2.setColor(new Color(220, 40, 40));
                g2.drawString("DEFEAT!", screenWidth / 2 - 160, screenHeight / 2);
            }
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 24));
            g2.drawString("Press R to restart", screenWidth / 2 - 100, screenHeight / 2 + 60);
        }
    }

    private void drawStartScreen(Graphics2D g2) {
        g2.setColor(new Color(20, 50, 20));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Gradient effect
        GradientPaint gp = new GradientPaint(0, 0, new Color(40, 100, 40), screenWidth, screenHeight,
                new Color(10, 30, 10));
        g2.setPaint(gp);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("Arial", Font.BOLD, 80));
        String title = "LIÊN QUÂN MINI";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (screenWidth - fm.stringWidth(title)) / 2, screenHeight / 2 - 100);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        String sub = "Hệ thống MOBA Java 2D";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (screenWidth - fm.stringWidth(sub)) / 2, screenHeight / 2 - 40);

        // Play button area
        int btnW = 300, btnH = 80;
        int btnX = (screenWidth - btnW) / 2, btnY = screenHeight / 2 + 50;
        g2.setColor(new Color(0, 150, 0));
        g2.fillRoundRect(btnX, btnY, btnW, btnH, 20, 20);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(btnX, btnY, btnW, btnH, 20, 20);

        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String play = "PLAY GAME";
        fm = g2.getFontMetrics();
        g2.drawString(play, btnX + (btnW - fm.stringWidth(play)) / 2, btnY + 52);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        String hint = "Bấm R hoặc Click để bắt đầu";
        fm = g2.getFontMetrics();
        g2.drawString(hint, (screenWidth - fm.stringWidth(hint)) / 2, btnY + 120);

        g2.setColor(new Color(200, 200, 200, 150));
        g2.drawString("Settings: F (Fullscreen) | +/- (Zoom)", (screenWidth - 300) / 2, screenHeight - 50);
    }

    private void drawMap(Graphics2D g2) {
        g2.setColor(new Color(34, 110, 34));
        g2.fillRect(0, 0, worldWidth, worldHeight);

        g2.setColor(new Color(22, 75, 22));
        g2.fillRect(worldWidth / 2, 0, worldWidth / 2, worldHeight / 2);
        g2.fillRect(0, worldHeight / 2, worldWidth / 2, worldHeight / 2);

        java.util.Random rand = new java.util.Random(42);
        g2.setColor(new Color(45, 135, 45, 90));
        for (int i = 0; i < 700; i++) {
            int tx = rand.nextInt(worldWidth), ty = rand.nextInt(worldHeight);
            int ts = rand.nextInt(28) + 8;
            g2.fillOval(tx, ty, ts, ts / 2);
        }

        int laneW = 240;
        int[] lxs = { 0, laneW, worldWidth, worldWidth - laneW };
        int[] lys = { worldHeight, worldHeight, 0, 0 };
        g2.setColor(new Color(160, 130, 70, 180));
        g2.fillPolygon(lxs, lys, 4);
        g2.setColor(new Color(120, 100, 50, 200));
        g2.setStroke(new BasicStroke(5));
        g2.drawLine(0, worldHeight, worldWidth, 0);
        g2.drawLine(laneW, worldHeight, worldWidth, laneW);
        g2.setStroke(new BasicStroke(1));

        int ry = worldHeight / 2 - 70;
        g2.setColor(new Color(35, 95, 180, 180));
        g2.fillRect(0, ry, worldWidth, 140);
        g2.setColor(new Color(70, 140, 220, 100));
        for (int i = 0; i < worldWidth; i += 90) {
            g2.drawArc(i, ry + 15, 70, 28, 0, 180);
            g2.drawArc(i + 40, ry + 80, 65, 22, 0, 180);
        }

        rand = new java.util.Random(99);
        for (int i = 0; i < 100; i++) {
            int tx, ty;
            if (i < 50) {
                tx = worldWidth / 2 + 50 + rand.nextInt(worldWidth / 2 - 250);
                ty = 50 + rand.nextInt(worldHeight / 2 - 200);
            } else {
                tx = 50 + rand.nextInt(worldWidth / 2 - 250);
                ty = worldHeight / 2 + 80 + rand.nextInt(worldHeight / 2 - 200);
            }
            g2.setColor(new Color(70, 45, 15));
            g2.fillRect(tx + 2, ty + 32, 18, 34);
            g2.setColor(new Color(18, 80, 18, 210));
            g2.fillOval(tx - 18, ty, 60, 58);
            g2.setColor(new Color(28, 115, 28, 160));
            g2.fillOval(tx - 8, ty - 12, 42, 42);
        }

        rand = new java.util.Random(77);
        for (int i = 0; i < 40; i++) {
            int tx = rand.nextInt(worldWidth), ty = rand.nextInt(worldHeight);
            g2.setColor(new Color(110, 110, 110, 180));
            g2.fillOval(tx, ty, 32, 22);
            g2.setColor(new Color(155, 155, 155, 120));
            g2.fillOval(tx + 5, ty + 4, 16, 10);
        }

        g2.setColor(new Color(0, 0, 0, 200));
        g2.setStroke(new BasicStroke(14));
        g2.drawRect(0, 0, worldWidth, worldHeight);
        g2.setStroke(new BasicStroke(1));
    }
}
