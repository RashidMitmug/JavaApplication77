package javaapplication7;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.swing.*;

public class Tutorial implements ActionListener, KeyListener, MouseMotionListener, MouseListener {
    private static final int MAP_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int MAP_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    private static final int WALL_THICKNESS = 50;
    private Controls controls;
    private JFrame frame;
    private TutorialPanel tutorialPanel;
    private Point mousePosition = new Point(0, 0);
    private boolean mousePressed = false;
    private int currentStage = 1;
    private Point targetArea;
    private String currentInstruction = "Use WASD to move to the red circle";
    private BufferedImage[] playerFrames;
    private boolean facingRight = true;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private static final long FRAME_DELAY = 100; // milliseconds between frames
    private BufferedImage floorTexture;
    private BufferedImage[] enemyFrames;
    private Clip backgroundMusic;
    private float backgroundVolume = 0.2f; // 20% volume
    private float shootVolume = 0.3f; // 30% volume
    private ArrayList<Clip> activeClips = new ArrayList<>();  // Add this to track all active clips

    private class Enemy {
        int x, y;
        int health = 3;
        double dx, dy;
        long lastShotTime = 0;
        static final long SHOT_COOLDOWN = 2000;
        boolean canMove = false;
        Color currentColor = Color.RED;
        int animationFrame = 0;
        private long lastBulletTime = 0;
        private static final long BULLET_INTERVAL = 200;
        private int bulletsShot = 0;
        private long lastFrameTime = 0;
        private static final long FRAME_DELAY = 100;
        private boolean facingRight = true;

        public Enemy() {
            Random rand = new Random();
            this.x = rand.nextInt(MAP_WIDTH - 200) + 100;
            this.y = rand.nextInt(MAP_HEIGHT - 200) + 100;
            dx = Math.random() * 4 - 2;
            dy = Math.random() * 4 - 2;
            facingRight = dx > 0;
        }

        public void move() {
            if (!canMove) {
                animationFrame = 0;
                return;
            }

            x += dx;
            y += dy;
            
            // Update facing direction
            facingRight = dx > 0;
            
            if (x <= WALL_THICKNESS || x >= MAP_WIDTH - WALL_THICKNESS - 70) {
                dx *= -1;
                facingRight = dx > 0;
            }
            if (y <= WALL_THICKNESS || y >= MAP_HEIGHT - WALL_THICKNESS - 100) {
                dy *= -1;
            }
            
            x = Math.max(WALL_THICKNESS, Math.min(x, MAP_WIDTH - WALL_THICKNESS - 70));
            y = Math.max(WALL_THICKNESS, Math.min(y, MAP_HEIGHT - WALL_THICKNESS - 100));
            
            // Update animation
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameTime > FRAME_DELAY) {
                animationFrame = (animationFrame + 1) % 4;
                lastFrameTime = currentTime;
            }
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, 70, 100);
        }

        public int getAnimationFrame() {
            return animationFrame;
        }

        public boolean isFacingRight() {
            return facingRight;
        }

        public void shoot(ArrayList<Bullet> enemyBullets, int playerX, int playerY) {
            if (!canMove) return;
            
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShotTime >= SHOT_COOLDOWN) {
                if (bulletsShot == 0) {
                    lastShotTime = currentTime;
                    lastBulletTime = currentTime;
                    enemyBullets.add(new Bullet(x + 35, y + 50, playerX, playerY));
                    bulletsShot++;
                } else if (currentTime - lastBulletTime >= BULLET_INTERVAL) {
                    lastBulletTime = currentTime;
                    switch(bulletsShot) {
                        case 1: enemyBullets.add(new Bullet(x + 35, y + 50, playerX - 100, playerY)); break;
                        case 2: enemyBullets.add(new Bullet(x + 35, y + 50, playerX + 100, playerY)); break;
                        case 3: enemyBullets.add(new Bullet(x + 35, y + 50, playerX, playerY - 100)); break;
                    }
                    bulletsShot++;
                    if (bulletsShot >= 4) bulletsShot = 0;
                }
            }
        }
    }

    private class Bullet {
        double x, y;
        double dx, dy;
        int speed = 15;

        public Bullet(double startX, double startY, double targetX, double targetY) {
            this.x = startX;
            this.y = startY;
            double angle = Math.atan2(targetY - startY, targetX - startX);
            dx = Math.cos(angle) * speed;
            dy = Math.sin(angle) * speed;
        }

        public void move() {
            x += dx;
            y += dy;
        }

        public Rectangle getBounds() {
            return new Rectangle((int)x, (int)y, 10, 10);
        }
    }

    private class Controls {
        private boolean[] keys = new boolean[256];
        private int playerX = MAP_WIDTH/2 - 35;
        private int playerY = MAP_HEIGHT/2 - 50;
        private static final int PLAYER_SPEED = 5;

        public void setKey(int keyCode, boolean pressed) {
            if (keyCode >= 0 && keyCode < keys.length) {
                keys[keyCode] = pressed;
            }
        }

        public boolean isMoving() {
            return keys[KeyEvent.VK_W] || keys[KeyEvent.VK_S] || 
                   keys[KeyEvent.VK_A] || keys[KeyEvent.VK_D] ||
                   keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_DOWN] ||
                   keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_RIGHT];
        }

        public boolean updateMovement() {
            boolean moved = false;

            if (keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]) {
                if (playerX > WALL_THICKNESS) {
                    playerX -= PLAYER_SPEED;
                    facingRight = false;
                    moved = true;
                }
            }
            if (keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]) {
                if (playerX < MAP_WIDTH - WALL_THICKNESS - 70) {
                    playerX += PLAYER_SPEED;
                    facingRight = true;
                    moved = true;
                }
            }
            if (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]) {
                if (playerY > WALL_THICKNESS) {
                    playerY -= PLAYER_SPEED;
                    moved = true;
                }
            }
            if (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]) {
                if (playerY < MAP_HEIGHT - WALL_THICKNESS - 100) {
                    playerY += PLAYER_SPEED;
                    moved = true;
                }
            }

            return moved;
        }

        public int getPlayerX() { return playerX; }
        public int getPlayerY() { return playerY; }
        
        public void setPlayerX(int x) { 
            playerX = Math.max(WALL_THICKNESS, Math.min(x, MAP_WIDTH - WALL_THICKNESS - 70));
        }
        
        public void setPlayerY(int y) { 
            playerY = Math.max(WALL_THICKNESS, Math.min(y, MAP_HEIGHT - WALL_THICKNESS - 100));
        }
    }

    private class TutorialPanel extends JPanel {
        private Timer timer;
        private ArrayList<Bullet> bullets;
        private ArrayList<Enemy> enemies;
        private ArrayList<Bullet> enemyBullets;
        private long lastShotTime = 0;
        private static final long SHOT_COOLDOWN = 200;
        private int playerHealth = 5;
        private static final int MINIMAP_SIZE = 200;
        private int cameraX = 0;
        private int cameraY = 0;
        private Point portalLocation;
        private boolean portalSpawned = false;
        private PlaySound shootSound;

        public TutorialPanel() {
            setBackground(Color.BLACK);
            setOpaque(true);
            bullets = new ArrayList<>();
            enemies = new ArrayList<>();
            enemyBullets = new ArrayList<>();
            
            Random rand = new Random();
            targetArea = new Point(
                rand.nextInt(MAP_WIDTH - 600) + 300,
                rand.nextInt(MAP_HEIGHT - 600) + 300
            );

            shootSound = new PlaySound();
            
            timer = new Timer(16, e -> {
                controls.updateMovement();
                updateCamera();
                checkStageProgress();
                moveEnemies();
                updateBullets();
                updateEnemyBullets();
                
                if (mousePressed && System.currentTimeMillis() - lastShotTime >= SHOT_COOLDOWN) {
                    shoot();
                    lastShotTime = System.currentTimeMillis();
                }
                
                checkCollisions();
                repaint();
            });
            timer.start();
        }

        private void updateCamera() {
            cameraX = Math.max(0, Math.min(controls.getPlayerX() - getWidth()/2 + 35, MAP_WIDTH - getWidth()));
            cameraY = Math.max(0, Math.min(controls.getPlayerY() - getHeight()/2 + 50, MAP_HEIGHT - getHeight()));
        }

        private void moveEnemies() {
            for (Enemy enemy : enemies) {
                enemy.move();
                if (currentStage == 3) {
                    enemy.shoot(enemyBullets, controls.getPlayerX(), controls.getPlayerY());
                }
            }
        }

        private void shoot() {
            Point worldMouse = new Point(mousePosition.x + cameraX, mousePosition.y + cameraY);
            bullets.add(new Bullet(controls.getPlayerX() + 35, controls.getPlayerY() + 50, 
                                 worldMouse.x, worldMouse.y));
            shootSound.playEffect("Audio/shoot.wav");
        }

        private void updateBullets() {
            bullets.removeIf(bullet -> 
                bullet.x < 0 || bullet.x > MAP_WIDTH || 
                bullet.y < 0 || bullet.y > MAP_HEIGHT);
            
            for (Bullet bullet : bullets) {
                bullet.move();
            }
        }

        private void updateEnemyBullets() {
            enemyBullets.removeIf(bullet -> 
                bullet.x < 0 || bullet.x > MAP_WIDTH || 
                bullet.y < 0 || bullet.y > MAP_HEIGHT);
            
            for (Bullet bullet : enemyBullets) {
                bullet.move();
            }
        }

        private void checkStageProgress() {
            if (currentStage == 1) {
                Rectangle targetBounds = new Rectangle(targetArea.x - 50, targetArea.y - 50, 100, 100);
                if (targetBounds.contains(controls.getPlayerX() + 35, controls.getPlayerY() + 50)) {
                    currentStage = 2;
                    currentInstruction = "Shoot the enemies (Left Click)";
                    spawnEnemies(3, false);
                }
            } else if (currentStage == 2 && enemies.isEmpty()) {
                currentStage = 3;
                currentInstruction = "Survive and defeat moving enemies";
                spawnEnemies(3, true);
            } else if (currentStage == 3 && enemies.isEmpty() && !portalSpawned) {
                spawnPortal();
                currentInstruction = "Enter the portal to continue";
            }
        }

        private void spawnEnemies(int count, boolean canMove) {
            for (int i = 0; i < count; i++) {
                Enemy enemy = new Enemy();
                enemy.canMove = canMove;
                enemies.add(enemy);
            }
        }

        private void spawnPortal() {
            portalSpawned = true;
            portalLocation = new Point(MAP_WIDTH/2, MAP_HEIGHT/2);
        }

        private void checkCollisions() {
            Rectangle playerBounds = new Rectangle(controls.getPlayerX(), controls.getPlayerY(), 70, 100);
            
            Iterator<Bullet> bulletIt = bullets.iterator();
            while (bulletIt.hasNext()) {
                Bullet bullet = bulletIt.next();
                Iterator<Enemy> enemyIt = enemies.iterator();
                while (enemyIt.hasNext()) {
                    Enemy enemy = enemyIt.next();
                    if (bullet.getBounds().intersects(enemy.getBounds())) {
                        enemy.health--;
                        bulletIt.remove();
                        if (enemy.health <= 0) {
                            enemyIt.remove();
                        }
                        break;
                    }
                }
            }

            Iterator<Bullet> enemyBulletIt = enemyBullets.iterator();
            while (enemyBulletIt.hasNext()) {
                Bullet bullet = enemyBulletIt.next();
                if (bullet.getBounds().intersects(playerBounds)) {
                    playerHealth--;
                    enemyBulletIt.remove();
                    if (playerHealth <= 0) {
                        handleGameOver();
                    }
                }
            }

            if (portalSpawned) {
                Rectangle portalBounds = new Rectangle(portalLocation.x - 50, portalLocation.y - 50, 100, 100);
                if (playerBounds.intersects(portalBounds)) {
                    handleGameComplete();
                }
            }
        }

        private void handleGameOver() {
            if (currentStage == 3) {
                int choice = JOptionPane.showConfirmDialog(frame, 
                    "Game Over! Would you like to retry Stage 3?", 
                    "Game Over", 
                    JOptionPane.YES_NO_OPTION);
                
                if (choice == JOptionPane.YES_OPTION) {
                    restartStage3();
                } else {
                    timer.stop();
                    frame.dispose();
                }
            } else {
                timer.stop();
                JOptionPane.showMessageDialog(frame, "Game Over!");
                frame.dispose();
            }
        }

        private void handleGameComplete() {
            // Stop all audio and close tutorial
            stopAllAudio();
            timer.stop();
            frame.dispose();

            // Open GameProper
            SwingUtilities.invokeLater(() -> {
                GameProper gameProper = new GameProper();
                gameProper.setframe();
            });
        }

        private void restartStage3() {
            playerHealth = 5;
            controls.setPlayerX(MAP_WIDTH/2 - 35);
            controls.setPlayerY(MAP_HEIGHT/2 - 50);
            enemies.clear();
            enemyBullets.clear();
            bullets.clear();
            
            // Spawn enemies at specific positions
            Enemy enemy1 = new Enemy();
            enemy1.x = 300;
            enemy1.y = 300;
            
            Enemy enemy2 = new Enemy();
            enemy2.x = 700;
            enemy2.y = 300;
            
            Enemy enemy3 = new Enemy();
            enemy3.x = 500;
            enemy3.y = 700;
            
            for (Enemy enemy : new Enemy[]{enemy1, enemy2, enemy3}) {
                enemy.canMove = true;
                enemies.add(enemy);
            }
        }

        private void drawMinimap(Graphics2D g2d) {
            int minimapSize = 150;
            int margin = 20;
            int x = getWidth() - minimapSize - margin;
            
            // Save current clip
            Shape oldClip = g2d.getClip();
            
            // Create circular minimap
            Ellipse2D circle = new Ellipse2D.Double(x, margin, minimapSize, minimapSize);
            g2d.setClip(circle);
            
            // Draw background
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillOval(x, margin, minimapSize, minimapSize);
            
            // Calculate scale for minimap
            float scale = (float)minimapSize / MAP_WIDTH;
            
            // Draw walls
            g2d.setColor(Color.GREEN);
            g2d.fillRect(x + (int)(WALL_THICKNESS * scale), 
                         margin + (int)(WALL_THICKNESS * scale),
                         (int)((MAP_WIDTH - 2*WALL_THICKNESS) * scale),
                         (int)((MAP_HEIGHT - 2*WALL_THICKNESS) * scale));
            
            // Draw enemies
            g2d.setColor(Color.RED);
            for (Enemy enemy : enemies) {
                int miniX = x + (int)(enemy.x * scale);
                int miniY = margin + (int)(enemy.y * scale);
                g2d.fillOval(miniX - 3, miniY - 3, 6, 6);
            }
            
            // Draw player
            g2d.setColor(Color.BLUE);
            int playerMiniX = x + (int)(controls.getPlayerX() * scale);
            int playerMiniY = margin + (int)(controls.getPlayerY() * scale);
            g2d.fillOval(playerMiniX - 4, playerMiniY - 4, 8, 8);
            
            // Draw border
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x, margin, minimapSize, minimapSize);
            
            // Restore clip
            g2d.setClip(oldClip);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.translate(-cameraX, -cameraY);
            
            // Draw floor using texture
            TexturePaint floorPaint = new TexturePaint(floorTexture, 
                new Rectangle(0, 0, floorTexture.getWidth(), floorTexture.getHeight()));
            g2d.setPaint(floorPaint);
            g2d.fillRect(WALL_THICKNESS, WALL_THICKNESS, 
                         MAP_WIDTH - 2*WALL_THICKNESS, 
                         MAP_HEIGHT - 2*WALL_THICKNESS);
            
            // Draw walls in black
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, MAP_WIDTH, WALL_THICKNESS); // Top wall
            g2d.fillRect(0, MAP_HEIGHT - WALL_THICKNESS, MAP_WIDTH, WALL_THICKNESS); // Bottom wall
            g2d.fillRect(0, 0, WALL_THICKNESS, MAP_HEIGHT); // Left wall
            g2d.fillRect(MAP_WIDTH - WALL_THICKNESS, 0, WALL_THICKNESS, MAP_HEIGHT); // Right wall
            
            // Draw player
            int frameOffset = facingRight ? 0 : 4; // Use 0-3 for right, 4-7 for left
            if (controls.isMoving()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFrameTime > FRAME_DELAY) {
                    currentFrame = (currentFrame + 1) % 3; // Only cycle through Move frames
                    lastFrameTime = currentTime;
                }
                g2d.drawImage(playerFrames[frameOffset + currentFrame + 1], // +1 to skip Idle frame
                              controls.getPlayerX(), 
                              controls.getPlayerY(), 
                              70, 100, null);
            } else {
                // Draw Idle frame
                g2d.drawImage(playerFrames[frameOffset], // Idle frame is at 0 or 4
                              controls.getPlayerX(), 
                              controls.getPlayerY(), 
                              70, 100, null);
            }
            
            // Draw gun
            g2d.setColor(Color.GRAY);
            Point worldMouse = new Point(mousePosition.x + cameraX, mousePosition.y + cameraY);
            double angle = Math.atan2(worldMouse.y - (controls.getPlayerY() + 50),
                                    worldMouse.x - (controls.getPlayerX() + 35));
            AffineTransform old = g2d.getTransform();
            g2d.translate(controls.getPlayerX() + 35, controls.getPlayerY() + 50);
            g2d.rotate(angle);
            g2d.fillRect(0, -5, 40, 10);
            g2d.setTransform(old);
            
            // Draw enemies
            for (Enemy enemy : enemies) {
                BufferedImage frame;
                if (enemy.isFacingRight()) {
                    // Use regular frames
                    frame = enemyFrames[enemy.getAnimationFrame()];
                } else {
                    // Use inverted frames
                    frame = enemyFrames[enemy.getAnimationFrame() + 4];
                }
                g2d.drawImage(frame, enemy.x, enemy.y, 70, 100, null);
            }
            
            // Draw bullets
            g2d.setColor(Color.YELLOW);
            for (Bullet bullet : bullets) {
                g2d.fillOval((int)bullet.x, (int)bullet.y, 10, 10);
            }
            
            g2d.setColor(Color.RED);
            for (Bullet bullet : enemyBullets) {
                g2d.fillOval((int)bullet.x, (int)bullet.y, 10, 10);
            }
            
            // Draw target in stage 1
            if (currentStage == 1) {
                g2d.setColor(Color.RED);
                g2d.fillOval(targetArea.x - 50, targetArea.y - 50, 100, 100);
            }
            
            // Draw portal
            if (portalSpawned) {
                g2d.setColor(Color.MAGENTA);
                g2d.fillOval(portalLocation.x - 50, portalLocation.y - 50, 100, 100);
                g2d.setColor(Color.CYAN);
                g2d.drawOval(portalLocation.x - 40 + (int)(Math.random() * 10),
                            portalLocation.y - 40 + (int)(Math.random() * 10),
                            80, 80);
            }
            
            // Draw minimap (add this before UI elements)
            drawMinimap(g2d);
            
            g2d.translate(cameraX, cameraY);
            
            // Draw health
            g2d.setColor(Color.RED);
            for (int i = 0; i < playerHealth; i++) {
                g2d.fillOval(10 + i * 40, 10, 30, 30);
            }
            
            // Draw crosshair
            g2d.setColor(Color.GREEN);
            g2d.drawLine(mousePosition.x - 10, mousePosition.y, mousePosition.x + 10, mousePosition.y);
            g2d.drawLine(mousePosition.x, mousePosition.y - 10, mousePosition.x, mousePosition.y + 10);
            
            // Draw instructions
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString(currentInstruction, 10, getHeight() - 30);
        }
    }

    class PlaySound {
        public void playEffect(String soundFile) {
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(soundFile));
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                
                // Set volume to 30%
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float volume = -10.5f;  // Approximately 30% volume
                gainControl.setValue(volume);
                
                activeClips.add(clip);  // Add clip to tracking list
                clip.start();
                
                // Remove clip from list when it finishes
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        activeClips.remove(clip);
                        clip.close();
                    }
                });
            } catch (Exception e) {
                System.out.println("Error playing sound effect: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Tutorial() {
        controls = new Controls();
        loadPlayerFrames();
        loadTextures();
        loadEnemyFrames();
        playBackgroundMusic();
        frame = new JFrame("Tutorial");
        tutorialPanel = new TutorialPanel();
        
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(tutorialPanel);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.addKeyListener(this);
        frame.addMouseMotionListener(this);
        frame.addMouseListener(this);
        frame.setFocusable(true);
        frame.setUndecorated(true);
        
        // Add window listener to stop music when closing
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopAllAudio();
                frame.dispose();
            }
        });
    }

    public void setframe() {
        frame.setVisible(true);
        initBackgroundMusic();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousePosition = e.getPoint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        controls.setKey(e.getKeyCode(), true);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            stopAllAudio();
            frame.dispose();
            System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        controls.setKey(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {}

    private void loadPlayerFrames() {
        playerFrames = new BufferedImage[8]; // 4 right-facing + 4 left-facing frames
        try {
            // Load right-facing frames (0-3)
            playerFrames[0] = ImageIO.read(new File("Images/Idle1.png"));
            playerFrames[1] = ImageIO.read(new File("Images/Move1.png"));
            playerFrames[2] = ImageIO.read(new File("Images/Move2.png"));
            playerFrames[3] = ImageIO.read(new File("Images/Move3.png"));
            
            // Load left-facing frames (4-7)
            playerFrames[4] = ImageIO.read(new File("Images/Idle1_Inv.png"));
            playerFrames[5] = ImageIO.read(new File("Images/Move1_Inv.png"));
            playerFrames[6] = ImageIO.read(new File("Images/Move2_Inv.png"));
            playerFrames[7] = ImageIO.read(new File("Images/Move3_Inv.png"));
            
        } catch (IOException e) {
            System.out.println("Error loading player images: " + e.getMessage());
            e.printStackTrace(); // This will help show where the error is occurring
            
            // Create colored rectangles as fallback
            for (int i = 0; i < 8; i++) {
                playerFrames[i] = new BufferedImage(70, 100, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = playerFrames[i].createGraphics();
                g.setColor(new Color(0, 0, 255 - i * 30));
                g.fillRect(0, 0, 70, 100);
                g.dispose();
            }
        }
    }

    private void loadTextures() {
        try {
            floorTexture = ImageIO.read(new File("Images/stonefloor.png"));
        } catch (IOException e) {
            System.out.println("Error loading floor texture: " + e.getMessage());
            // Create a fallback texture if image fails to load
            floorTexture = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = floorTexture.createGraphics();
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, 100, 100);
            g.dispose();
        }
    }

    private void loadEnemyFrames() {
        enemyFrames = new BufferedImage[8];
        try {
            // Regular frames
            enemyFrames[0] = ImageIO.read(new File("Images/EIdle1.png"));
            enemyFrames[1] = ImageIO.read(new File("Images/EMove1.png"));
            enemyFrames[2] = ImageIO.read(new File("Images/EMove2.png"));
            enemyFrames[3] = ImageIO.read(new File("Images/EMove3.png"));
            
            // Inverted frames
            enemyFrames[4] = ImageIO.read(new File("Images/EIdle1_Inv.png"));
            enemyFrames[5] = ImageIO.read(new File("Images/EMove1_Inv.png"));
            enemyFrames[6] = ImageIO.read(new File("Images/EMove2_Inv.png"));
            enemyFrames[7] = ImageIO.read(new File("Images/EMove3_Inv.png"));
        } catch (IOException e) {
            System.out.println("Error loading enemy images: " + e.getMessage());
            e.printStackTrace();
            // Create fallback rectangles
            for (int i = 0; i < 8; i++) {
                enemyFrames[i] = new BufferedImage(70, 100, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = enemyFrames[i].createGraphics();
                g.setColor(new Color(255 - i * 30, 0, 0));
                g.fillRect(0, 0, 70, 100);
                g.dispose();
            }
        }
    }

    private void playBackgroundMusic() {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File("Audio/gamebackground.wav"));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            
            // Loop continuously
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            
            // Optional: Adjust volume if needed
            FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-10.0f); // Reduce volume by 10 decibels
            
        } catch (Exception e) {
            System.out.println("Error playing background music: " + e.getMessage());
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }

    private void initBackgroundMusic() {
        try {
            File musicFile = new File("Audio/gamebackground.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            
            // Set volume to 20%
            FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(backgroundVolume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
            
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.out.println("Error playing background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }

    private void stopAllAudio() {
        // Stop background music
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
        
        // Stop all active sound effects
        for (Clip clip : new ArrayList<>(activeClips)) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.close();
        }
        activeClips.clear();
    }

    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            Tutorial tutorial = new Tutorial();
            tutorial.setframe();
        });
    }
}