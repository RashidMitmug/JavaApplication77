package javaapplication7;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class JavaApplication7 {
    private JFrame frame;
    private PlaySound menuSound;
    JLabel title;
    public JavaApplication7() {
        
       


        title = new JLabel(new ImageIcon("images/titleima.png"));
        
        frame = new JFrame("Main Menu");
        menuSound = new PlaySound();
        menuSound.playLoop("Audio/dkbg.wav");
        
        frame.setContentPane(new JLabel(new ImageIcon("images/mainmenubg.jpg")));
        frame.setLayout(new GraphPaperLayout(new Dimension(24, 17)));
        
        frame.add(title, new Rectangle(10, 3, 16, 4));
        JButton playButton = new JButton(new ImageIcon("images/startima.png"));
        JButton controlsButton = new JButton(new ImageIcon("images/settingsima.png"));
        JButton settingsButton = new JButton(new ImageIcon("images/settingsima.png"));
        
        frame.add(playButton, new Rectangle(3, 4, 8, 2));
        frame.add(controlsButton, new Rectangle(3, 8, 8, 2));
        frame.add(settingsButton, new Rectangle(3, 12, 8, 2));
        
        configureButton(playButton);
        configureButton(controlsButton);
        configureButton(settingsButton);
        
        playButton.addActionListener(e -> {
            PlaySound click = new PlaySound();
            click.playEffect("Audio/clicks.wav");
            menuSound.clip.stop();
            frame.dispose();
            GameStart game = new GameStart();
            game.setframe();
        });
        
        controlsButton.addActionListener(e -> {
            PlaySound click = new PlaySound();
            click.playEffect("Audio/clicks.wav");
            menuSound.clip.stop();
            frame.dispose();
            ControlsMenu controls = new ControlsMenu();
            controls.setframe();
        });
        
        settingsButton.addActionListener(e -> {
            PlaySound click = new PlaySound();
            click.playEffect("Audio/clicks.wav");
            
        });
        
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    private void configureButton(JButton button) {
        button.setBackground(new Color(0, 0, 0, 0));
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon icon = (ImageIcon) button.getIcon();
                Image img = icon.getImage();
                Image newImg = img.getScaledInstance((int)(icon.getIconWidth() * 1.1), 
                                                   (int)(icon.getIconHeight() * 1.1), 
                                                   Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(newImg));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon originalIcon = new ImageIcon(button.getName());
                button.setIcon(originalIcon);
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
        
        button.setName(((ImageIcon)button.getIcon()).getDescription());
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
    
        public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JavaApplication7 menu = new JavaApplication7();
            menu.setframe();
        });
    }
}
        