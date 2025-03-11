package javaapplication7;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class ControlsMenu {
    private JFrame frame;
    private PlaySound menuSound;
    
    public ControlsMenu() {
        frame = new JFrame("Controls");
        menuSound = new PlaySound();
        menuSound.playLoop("Audio/dkbg.wav");
        
        // Load background image with error checking
        ImageIcon bgImage = loadImage("images/mainmenubg.jpg");
        if (bgImage != null) {
            frame.setContentPane(new JLabel(bgImage));
        } else {
            System.out.println("Failed to load background image");
            frame.setContentPane(new JLabel());
            frame.getContentPane().setBackground(Color.BLACK);
        }
        
        frame.setLayout(new GraphPaperLayout(new Dimension(24, 17)));
        
        // Load button images with error checking
        JButton wasdButton = createButton("images/02_wasd.png");
        JButton arrowButton = createButton("images/02_arrow.png");
        JButton backButton = createButton("images/02_back.png");
        
        frame.add(wasdButton, new Rectangle(3, 4, 8, 2));
        frame.add(arrowButton, new Rectangle(3, 8, 8, 2));
        frame.add(backButton, new Rectangle(3, 12, 8, 2));
        
        configureButton(wasdButton);
        configureButton(arrowButton);
        configureButton(backButton);
        
        wasdButton.addActionListener(e -> {
            PlaySound click = new PlaySound();
            click.playEffect("Audio/clicks.wav");
            GameProper.setControlScheme(true);
            JOptionPane.showMessageDialog(frame, "WASD Controls Selected");
        });
        
        arrowButton.addActionListener(e -> {
            PlaySound click = new PlaySound();
            click.playEffect("Audio/clicks.wav");
            GameProper.setControlScheme(false);
            JOptionPane.showMessageDialog(frame, "Arrow Keys Controls Selected");
        });
        
        backButton.addActionListener(e -> {
            PlaySound click = new PlaySound();
            click.playEffect("Audio/clicks.wav");
            menuSound.clip.stop();
            frame.dispose();
            JavaApplication7 mainMenu = new JavaApplication7();
            mainMenu.setframe();
        });
        
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private ImageIcon loadImage(String path) {
        try {
            java.io.File file = new java.io.File(path);
            if (!file.exists()) {
                System.out.println("File not found: " + path);
                return null;
            }
            return new ImageIcon(path);
        } catch (Exception e) {
            System.out.println("Error loading image " + path + ": " + e.getMessage());
            return null;
        }
    }
    
    private JButton createButton(String imagePath) {
        ImageIcon icon = loadImage(imagePath);
        JButton button;
        if (icon != null) {
            button = new JButton(icon);
        } else {
            // Create fallback button if image fails to load
            button = new JButton(imagePath.substring(imagePath.lastIndexOf('/') + 1));
            button.setForeground(Color.WHITE);
            button.setBackground(Color.DARK_GRAY);
        }
        configureButton(button);
        return button;
    }
    
    private void configureButton(JButton button) {
        button.setBackground(new Color(0, 0, 0, 0));
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        
        if (button.getIcon() != null) {
            button.setName(((ImageIcon)button.getIcon()).getDescription());
        }
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.getIcon() != null) {
                    ImageIcon icon = (ImageIcon) button.getIcon();
                    Image img = icon.getImage();
                    Image newImg = img.getScaledInstance(
                        (int)(icon.getIconWidth() * 1.1), 
                        (int)(icon.getIconHeight() * 1.1), 
                        Image.SCALE_SMOOTH);
                    button.setIcon(new ImageIcon(newImg));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.getName() != null) {
                    ImageIcon originalIcon = new ImageIcon(button.getName());
                    button.setIcon(originalIcon);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                ImageIcon icon = (ImageIcon) button.getIcon();
                Image img = icon.getImage();
                Image newImg = img.getScaledInstance((int)(icon.getIconWidth() * 0.9), 
                                                   (int)(icon.getIconHeight() * 0.9), 
                                                   Image.SCALE_SMOOTH);
                ImageIcon darkIcon = new ImageIcon(newImg);
                Image darkImg = createDarkerImage(darkIcon.getImage());
                button.setIcon(new ImageIcon(darkImg));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.getBounds().contains(e.getPoint())) {
                    ImageIcon icon = new ImageIcon(button.getName());
                    Image img = icon.getImage();
                    Image newImg = img.getScaledInstance((int)(icon.getIconWidth() * 1.1), 
                                                       (int)(icon.getIconHeight() * 1.1), 
                                                       Image.SCALE_SMOOTH);
                    button.setIcon(new ImageIcon(newImg));
                }
            }
        });
    }
    
    private Image createDarkerImage(Image img) {
        BufferedImage darkImg = new BufferedImage(
            img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = darkImg.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, darkImg.getWidth(), darkImg.getHeight());
        g.dispose();
        return darkImg;
    }
    
    public void setframe() {
        frame.setVisible(true);
    }
}
