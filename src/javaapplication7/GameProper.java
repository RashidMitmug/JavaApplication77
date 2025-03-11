package javaapplication7;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GameProper extends JPanel {
    private JFrame frame;
    private int playerX = 400;
    private int playerY = 300;
    private int cameraX = 0;
    private int cameraY = 0;
    private BufferedImage playerImage;
    private BufferedImage dirtTexture;
    private BufferedImage houseImage;
    private PlaySound gameSound;
    
    private static final int BLOCK_SIZE = 300;
    private static final String[] MAP_DATA = {
        "101111101",
        "020000000",
        "101111101",
        "100000001",
        "101111101",
        "000000000",
        "101111101"
    };
    
    private boolean[] keys;
    private double playerAngle = 0;
    private double playerDX = 0;
    private double playerDY = 0;
    private static final double MOVE_SPEED = 4;
    private static final double FRICTION = 0.8;
    private static final double MAX_SPEED = 5;
    
    private static final int MINIMAP_SIZE = 200;
    private static final int MINIMAP_SCALE = 15;
    private static final Color MINIMAP_BACKGROUND = new Color(0, 0, 0, 128);
    private static final Color MINIMAP_WALL = new Color(64, 64, 64, 192);
    private static final Color MINIMAP_PLAYER = Color.RED;
    
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private long lastShotTime = 0;
    private static final long SHOOT_COOLDOWN = 250; // milliseconds between shots
    private PlaySound shootSound;
    
    private BufferedImage[] playerFrames;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private static final long FRAME_DELAY = 100; // milliseconds between frame changes
    private boolean facingRight = true;
    
    private ArrayList<String> dialogues;
    private int currentDialogue = 0;
    private boolean dialogueActive = true;
    private boolean gameStarted = false;
    
    public GameProper() {
        // Basic panel setup
        setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        setBackground(Color.BLACK);
        setFocusable(true);
        
        // Load images
        try {
            playerImage = ImageIO.read(new File("images/idle1.png"));
            dirtTexture = ImageIO.read(new File("images/dirt.png"));
            houseImage = ImageIO.read(new File("images/01_house.png"));
        } catch (Exception e) {
            System.out.println("Error loading images: " + e.getMessage());
        }

        // Setup frame
        frame = new JFrame("Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.add(this);
        
        // Make it fullscreen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        gd.setFullScreenWindow(frame);
        
        // Initialize keys array
        keys = new boolean[256];

        // Replace the old KeyListener with this new one
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keys[e.getKeyCode()] = true;
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (gameSound != null && gameSound.clip != null) {
                        gameSound.clip.stop();
                    }
                    frame.dispose();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keys[e.getKeyCode()] = false;
            }
        });

        // Update the Timer
        Timer timer = new Timer(16, e -> {
            updateGame();
            repaint();
        });
        timer.start();

        // Start background music
        gameSound = new PlaySound();
        gameSound.playLoop("Audio/gamebackground.wav");

        loadPlayerFrames();
        shootSound = new PlaySound();
        
        initializeDialogues();
        
        // Add mouse listener for dialogue progression
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (dialogueActive) {
                    currentDialogue++;
                    if (currentDialogue >= dialogues.size()) {
                        dialogueActive = false;
                        gameStarted = true;
                    }
                    repaint();
                } else if (gameStarted) {
                    // Existing shooting code
                    shoot(e.getX(), e.getY());
                }
            }
        });
    }

    private void updateCamera() {
        // Center the camera on the player
        cameraX = playerX - getWidth()/2 + 35;  // 35 is half of player width
        cameraY = playerY - getHeight()/2 + 50; // 50 is half of player height
        
        // Clamp camera to map bounds
        int maxX = MAP_DATA[0].length() * BLOCK_SIZE - getWidth();
        int maxY = MAP_DATA.length * BLOCK_SIZE - getHeight();
        
        cameraX = Math.max(0, Math.min(cameraX, maxX));
        cameraY = Math.max(0, Math.min(cameraY, maxY));
    }

    private void updateMovement() {
        // Calculate movement based on key presses
        if (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]) {
            playerDY -= MOVE_SPEED;
        }
        if (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]) {
            playerDY += MOVE_SPEED;
        }
        if (keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]) {
            playerDX -= MOVE_SPEED;
        }
        if (keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]) {
            playerDX += MOVE_SPEED;
        }

        // Apply friction
        playerDX *= FRICTION;
        playerDY *= FRICTION;

        // Limit speed
        double speed = Math.sqrt(playerDX * playerDX + playerDY * playerDY);
        if (speed > MAX_SPEED) {
            playerDX = (playerDX / speed) * MAX_SPEED;
            playerDY = (playerDY / speed) * MAX_SPEED;
        }

        // Calculate new position
        int newX = (int)(playerX + playerDX);
        int newY = (int)(playerY + playerDY);

        // Player hitbox dimensions
        int playerWidth = 70;
        int playerHeight = 100;

        // Check all corners for collisions
        boolean canMoveX = true;
        boolean canMoveY = true;

        // Check horizontal movement
        if (playerDX > 0) { // Moving right
            canMoveX = isWalkable((newX + playerWidth) / BLOCK_SIZE * BLOCK_SIZE, playerY) &&
                       isWalkable((newX + playerWidth) / BLOCK_SIZE * BLOCK_SIZE, playerY + playerHeight - 1);
        } else if (playerDX < 0) { // Moving left
            canMoveX = isWalkable(newX / BLOCK_SIZE * BLOCK_SIZE, playerY) &&
                       isWalkable(newX / BLOCK_SIZE * BLOCK_SIZE, playerY + playerHeight - 1);
        }

        // Check vertical movement
        if (playerDY > 0) { // Moving down
            canMoveY = isWalkable(playerX, (newY + playerHeight) / BLOCK_SIZE * BLOCK_SIZE) &&
                       isWalkable(playerX + playerWidth - 1, (newY + playerHeight) / BLOCK_SIZE * BLOCK_SIZE);
        } else if (playerDY < 0) { // Moving up
            canMoveY = isWalkable(playerX, newY / BLOCK_SIZE * BLOCK_SIZE) &&
                       isWalkable(playerX + playerWidth - 1, newY / BLOCK_SIZE * BLOCK_SIZE);
        }

        // Apply movement only if allowed
        if (canMoveX) {
            playerX = newX;
        } else {
            playerDX = 0; // Stop horizontal momentum when hitting a wall
        }

        if (canMoveY) {
            playerY = newY;
        } else {
            playerDY = 0; // Stop vertical momentum when hitting a wall
        }

        // Keep player within map bounds
        playerX = Math.max(0, Math.min(playerX, MAP_DATA[0].length() * BLOCK_SIZE - playerWidth));
        playerY = Math.max(0, Math.min(playerY, MAP_DATA.length * BLOCK_SIZE - playerHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        if (dialogueActive) {
            // Draw semi-transparent black background
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Draw dialogue box
            int boxWidth = getWidth() - 200;
            int boxHeight = 150;
            int boxX = 100;
            int boxY = getHeight() - 200;

            // Draw dialogue box background
            g2d.setColor(new Color(50, 50, 50, 230));
            g2d.fillRect(boxX, boxY, boxWidth, boxHeight);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(boxX, boxY, boxWidth, boxHeight);

            // Draw text
            if (currentDialogue < dialogues.size()) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                drawWrappedText(g2d, dialogues.get(currentDialogue), boxX + 20, boxY + 40, boxWidth - 40);
                
                // Draw "Click to continue" text
                g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                g2d.drawString("Click to continue...", boxX + boxWidth - 150, boxY + boxHeight - 20);
            }
        } else {
            // Regular game rendering
            g2d.translate(-cameraX, -cameraY);

            // Draw map
            for (int y = 0; y < MAP_DATA.length; y++) {
                for (int x = 0; x < MAP_DATA[y].length(); x++) {
                    int drawX = x * BLOCK_SIZE;
                    int drawY = y * BLOCK_SIZE;
                    
                    // First draw dirt background everywhere
                    if (dirtTexture != null) {
                        g2d.drawImage(dirtTexture, drawX, drawY, BLOCK_SIZE, BLOCK_SIZE, null);
                    }
                    
                    // Then draw houses where '1's are
                    if (MAP_DATA[y].charAt(x) == '1' && houseImage != null) {
                        g2d.drawImage(houseImage, drawX, drawY, BLOCK_SIZE, BLOCK_SIZE, null);
                    }
                }
            }
            
            // Draw player with animation
            if (playerFrames != null) {
                int frameIndex = currentFrame + (facingRight ? 0 : 4);
                g2d.drawImage(playerFrames[frameIndex], playerX, playerY, 70, 100, null);
            }

            // Draw bullets
            g2d.setColor(Color.YELLOW);
            for (Bullet bullet : bullets) {
                g2d.fillOval((int)bullet.x - bullet.SIZE/2, 
                             (int)bullet.y - bullet.SIZE/2, 
                             bullet.SIZE, bullet.SIZE);
            }

            g2d.translate(cameraX, cameraY);
            drawMinimap(g2d);
        }
    }

    public void setframe() {
        frame.setVisible(true);
        requestFocusInWindow();
    }

    public static void main(String[] args) {
        // Use SwingUtilities to ensure GUI is created on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            GameProper game = new GameProper();
            game.setframe();
        });
    }

    private boolean isWalkable(int x, int y) {
        // Convert pixel coordinates to map coordinates
        int mapX = x / BLOCK_SIZE;
        int mapY = y / BLOCK_SIZE;
        
        // Check map bounds
        if (mapY < 0 || mapY >= MAP_DATA.length || 
            mapX < 0 || mapX >= MAP_DATA[0].length()) {
            return false;
        }
        
        // Check if the position contains a wall ('1')
        return MAP_DATA[mapY].charAt(mapX) != '1';
    }

    private void drawMinimap(Graphics2D g2d) {
        // Save the current transform
        AffineTransform oldTransform = g2d.getTransform();
        g2d.setTransform(new AffineTransform());

        // Calculate minimap position (top-right corner)
        int minimapX = getWidth() - MINIMAP_SIZE - 20;
        int minimapY = 20;

        // Draw minimap background (semi-transparent black circle)
        g2d.setColor(MINIMAP_BACKGROUND);
        g2d.fillOval(minimapX, minimapY, MINIMAP_SIZE, MINIMAP_SIZE);

        // Create circular clip for minimap
        Ellipse2D.Double clip = new Ellipse2D.Double(minimapX, minimapY, MINIMAP_SIZE, MINIMAP_SIZE);
        g2d.setClip(clip);

        // Calculate minimap center and scale
        int centerX = minimapX + MINIMAP_SIZE / 2;
        int centerY = minimapY + MINIMAP_SIZE / 2;

        // Draw map elements
        for (int y = 0; y < MAP_DATA.length; y++) {
            for (int x = 0; x < MAP_DATA[y].length(); x++) {
                if (MAP_DATA[y].charAt(x) == '1') {
                    // Draw walls
                    g2d.setColor(MINIMAP_WALL);
                    int drawX = centerX + (x * BLOCK_SIZE - playerX) / MINIMAP_SCALE;
                    int drawY = centerY + (y * BLOCK_SIZE - playerY) / MINIMAP_SCALE;
                    g2d.fillRect(drawX, drawY, BLOCK_SIZE / MINIMAP_SCALE, BLOCK_SIZE / MINIMAP_SCALE);
                }
            }
        }

        // Draw player (red dot in center)
        g2d.setColor(MINIMAP_PLAYER);
        g2d.fillOval(centerX - 4, centerY - 4, 8, 8);

        // Reset clip and transform
        g2d.setClip(null);
        g2d.setTransform(oldTransform);
    }

    private void shoot(int targetX, int targetY) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= SHOOT_COOLDOWN) {
            // Create new bullet at player's center
            int bulletStartX = playerX + 35; // half of player width
            int bulletStartY = playerY + 50; // half of player height
            
            // Add bullet to list
            bullets.add(new Bullet(bulletStartX, bulletStartY, 
                                 targetX + cameraX, targetY + cameraY));
            
            // Play shoot sound
            if (shootSound != null) {
                
            }
            
            lastShotTime = currentTime;
        }
    }

    private void loadPlayerFrames() {
        try {
            playerFrames = new BufferedImage[8]; // 4 right-facing, 4 left-facing
            // Load right-facing frames
            playerFrames[0] = ImageIO.read(new File("images/idle1.png"));
            playerFrames[1] = ImageIO.read(new File("images/move1.png"));
            playerFrames[2] = ImageIO.read(new File("images/move2.png"));
            playerFrames[3] = ImageIO.read(new File("images/move3.png"));
            
            // Load left-facing frames
            playerFrames[4] = ImageIO.read(new File("images/idle1_inv.png"));
            playerFrames[5] = ImageIO.read(new File("images/move1_inv.png"));
            playerFrames[6] = ImageIO.read(new File("images/move2_inv.png"));
            playerFrames[7] = ImageIO.read(new File("images/move3_inv.png"));
        } catch (Exception e) {
            System.out.println("Error loading player frames: " + e.getMessage());
        }
    }

    private void updatePlayerAnimation() {
        long currentTime = System.currentTimeMillis();
        
        // Only update animation if moving
        if (Math.abs(playerDX) > 0.1 || Math.abs(playerDY) > 0.1) {
            if (currentTime - lastFrameTime > FRAME_DELAY) {
                currentFrame = (currentFrame + 1) % 4;
                lastFrameTime = currentTime;
            }
        } else {
            currentFrame = 0; // Reset to idle frame when not moving
        }
        
        // Update facing direction
        if (playerDX > 0) {
            facingRight = true;
        } else if (playerDX < 0) {
            facingRight = false;
        }
    }

    private void updateBullets() {
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet bullet = it.next();
            bullet.update();
            if (bullet.isOutOfBounds() || bullet.hitWall()) {
                it.remove();
            }
        }
    }

    private void updateGame() {
        if (!dialogueActive) {
            updateMovement();
            updatePlayerAnimation();
            updateBullets();
            updateCamera();
        }
    }

    private void initializeDialogues() {
        dialogues = new ArrayList<>();
        dialogues.add("Hala nadito na ang mga Guwardiya Sibil! Iisang paraan lang ito matatapos...");
        dialogues.add("[GUWARDIYA SIBIL]: Nadoon na si Basilio!");
    }

    private void drawWrappedText(Graphics2D g2d, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineHeight = fm.getHeight();
        
        for (String word : words) {
            if (fm.stringWidth(line + word) < maxWidth) {
                line.append(word).append(" ");
            } else {
                g2d.drawString(line.toString(), x, y);
                y += lineHeight;
                line = new StringBuilder(word + " ");
            }
        }
        g2d.drawString(line.toString(), x, y);
    }

    // Bullet class definition
    private class Bullet {
        double x, y;
        double dx, dy;
        static final int SPEED = 10;
        static final int SIZE = 10;

        Bullet(double startX, double startY, double targetX, double targetY) {
            this.x = startX;
            this.y = startY;
            
            // Calculate direction
            double angle = Math.atan2(targetY - startY, targetX - startX);
            this.dx = Math.cos(angle) * SPEED;
            this.dy = Math.sin(angle) * SPEED;
        }

        void update() {
            x += dx;
            y += dy;
        }

        boolean isOutOfBounds() {
            // Get map dimensions in pixels (9 columns × 7 rows)
            int mapWidth = 9 * BLOCK_SIZE;  // 9 columns
            int mapHeight = 7 * BLOCK_SIZE; // 7 rows
            
            return x < 0 || x >= mapWidth || y < 0 || y >= mapHeight;
        }

        boolean hitWall() {
            try {
                // Convert bullet position to map coordinates
                int mapX = (int)(x / BLOCK_SIZE);
                int mapY = (int)(y / BLOCK_SIZE);
                
                // Check map bounds (9 columns × 7 rows)
                if (mapY < 0 || mapY >= 7 || mapX < 0 || mapX >= 9) {
                    return true;
                }
                
                // Check if current position is a wall
                return MAP_DATA[mapY].charAt(mapX) == '1';
                
            } catch (Exception e) {
                return true;
            }
        }
    }
}

