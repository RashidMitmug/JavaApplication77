package javaapplication7;

public class Controls {
    private int playerX;
    private int playerY;
    private int playerSpeed = 5;
    private boolean[] keys = new boolean[256];
    private final int MAP_WIDTH = 1000;
    private final int MAP_HEIGHT = 1000;
    private final int WALL_THICKNESS = 50;
    private static boolean useWASD = true; // Static variable to track control scheme
    
    public Controls() {
        playerX = MAP_WIDTH/2 - 35;
        playerY = MAP_HEIGHT/2 - 50;
    }
    
    // Add this static method to set the control scheme
    public static void setControlScheme(boolean useWASDControls) {
        useWASD = useWASDControls;
    }
    
    public boolean updateMovement() {
        boolean moved = false;
        
        if (useWASD) {
            // WASD controls
            if (keys[87]) { playerY -= playerSpeed; moved = true; } // W
            if (keys[83]) { playerY += playerSpeed; moved = true; } // S
            if (keys[65]) { playerX -= playerSpeed; moved = true; } // A
            if (keys[68]) { playerX += playerSpeed; moved = true; } // D
        } else {
            // Arrow keys
            if (keys[38]) { playerY -= playerSpeed; moved = true; } // UP
            if (keys[40]) { playerY += playerSpeed; moved = true; } // DOWN
            if (keys[37]) { playerX -= playerSpeed; moved = true; } // LEFT
            if (keys[39]) { playerX += playerSpeed; moved = true; } // RIGHT
        }
        
        return moved;
    }
    
    public boolean isMoving() {
        if (useWASD) {
            return keys[87] || keys[83] || keys[65] || keys[68]; // WASD
        } else {
            return keys[38] || keys[40] || keys[37] || keys[39]; // Arrow keys
        }
    }
    
    public void setKey(int keyCode, boolean pressed) {
        keys[keyCode] = pressed;
    }
    
    public boolean isKeyPressed(int keyCode) {
        return keys[keyCode];
    }
    
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public void setPlayerX(int x) { playerX = x; }
    public void setPlayerY(int y) { playerY = y; }
}