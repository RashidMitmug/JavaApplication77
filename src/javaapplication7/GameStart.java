package javaapplication7;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.KeyboardFocusManager;

public class GameStart extends JFrame implements ActionListener {

    private JFrame frame;
    private PlaySound bgmusictw;
    private JTextArea textDisplay;
    private Timer typingTimer;
    private int currentDialogue = 0;
    private int currentChar = 0;
    private JButton b1;
    private JButton b2;
    private JLabel bgbg2;
    private JLabel basiliol;
    private JLabel simounr;
    private Clip backgroundMusic;
    
    private String[] dialogues = {
        "Noong nakaraan.. - May isang matapang na lalaki na nakikilala sa panglanang Basilio.",
        
        "Sa kasal nina Paulita Gómez at Juanito Pelaez, nagbago ang landas ni Basilio. Matapos"
            + " ang pagdurusa at kawalang-katarungan, sumiklab ang kanyang galit. Sa harap niya,"
            + " si Simoun—isang tao na may alok na hindi niya maaaring tanggihin.",
        
        "Simoun: (Nakatingin kay Basilio, malamig ang tinig)"+
        " Ngayong alam mo na ang katotohanan, Basilio, hindi na kita hihilingan ng pagdududa o "
            + "alinlangan. Ang tanong ko—handa ka bang ialay ang buhay mo para sa layunin ng kalayaan?",
        
        "Basilio: (Nagpipigil ng emosyon, ngunit seryoso) Ano ang nais mong gawin ko?                                                      " 
            + "Simoun: (Humakbang palapit, bumaba ang boses, puno ng bigat)"
            + " May isang tao, isang mahalaga sa akin, na matagal nang bihag ng mga kamay ng simbahan. Si María Clara.",
         
        
        "Basilio: (Nagulat, ngunit nagpapanatili ng kalmado)\n" +
        "\"Ang babaeng nasa kumbento? Buhay pa siya?\"",
        
        "Simoun: (Tumango, galit at lungkot ang bumabalot sa kanyang tinig)\n" +
"\"Buhay... ngunit hindi magtatagal. Winasak na nila ang kanyang katawan at kaluluwa. Kung siya'y manatili roon, papatayin siya ng kanilang pagkukunwari. At hindi ko iyon hahayaang mangyari.\"    ",
        "Basilio: (Nag-iisip, marahang tumango)\n" +
"\"Kung ito ang nais mong ipagawa, gagawin ko. Ngunit paano natin siya maililigtas?\" ",
        
        "Simoun: (Nakangiti nang bahagya, malamig at matalim)\n" +
"\"Pag-aaralan mo ang kumbento, ang kanilang galaw. Gamitin mo ang utak mo, Basilio. Alam kong kaya mong gamitin ang talino mo sa higit pa sa operasyon. At huwag kang mag-alala... Hindi ka mag-iisa.\"",
        
        "Basilio: (Matiim ang titig, puno ng determinasyon) Kailan natin sisimulan?",
        
        "Simoun: (Puno ng pag-asa at lungkot) Simulan mo na ngayon. Ang kaligtasan ni María Clara ay hindi lamang para sa kanya. Isa siyang simbolo, Basilio. Iligtas mo siya, at magpapadala tayo ng mensahe—na ang mga inaapi ay hindi habambuhay magdurusa."
        
        
    };

    public GameStart() {
        frame = new JFrame("Video Game");
        bgmusictw = new PlaySound();
        bgmusictw.playLoop("Audio/bgmusictw.wav");

        setupBackground();
        setupTextDisplay();
        setupButtons();

        basiliol.setVisible(false);
        simounr.setVisible(false);

        // Add window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopMusic();
            }
        });
    }

    private void setupBackground() {
        bgbg2 = new JLabel(new ImageIcon("images/bgbgtw.jpg"));
        basiliol = new JLabel(new ImageIcon("images/basiliol.png"));
        simounr = new JLabel(new ImageIcon("images/simounr.png"));
    }

private void setupTextDisplay() {
    textDisplay = new JTextArea();
    textDisplay.setFont(new Font("Press Start 2P", Font.PLAIN, 24));
    textDisplay.setForeground(new Color(0xc7, 0x9b, 0x22));
    textDisplay.setBackground(Color.BLACK);
    textDisplay.setOpaque(true);  // Ensures solid background
    textDisplay.setWrapStyleWord(true);
    textDisplay.setLineWrap(true);
    textDisplay.setEditable(false);
    textDisplay.setFocusable(false);
    textDisplay.setMargin(new Insets(20, 20, 20, 20));

    typingTimer = new Timer(25, e -> typeNextCharacter());
}




    private void setupButtons() {
        b1 = new JButton(new ImageIcon("images/b1.png"));
        b2 = new JButton(new ImageIcon("images/b2.png"));

        configureButton(b1);
        configureButton(b2);

        b1.addActionListener(this);
        b2.addActionListener(this);
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
                Image newImg = img.getScaledInstance((int) (icon.getIconWidth() * 1.1),
                        (int) (icon.getIconHeight() * 1.1),
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
                Image newImg = img.getScaledInstance((int) (icon.getIconWidth() * 0.9),
                        (int) (icon.getIconHeight() * 0.9),
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
                    Image newImg = img.getScaledInstance((int) (icon.getIconWidth() * 1.1),
                            (int) (icon.getIconHeight() * 1.1),
                            Image.SCALE_SMOOTH);
                    button.setIcon(new ImageIcon(newImg));
                }
            }
        });

        button.setName(((ImageIcon) button.getIcon()).getDescription());
    }

    private void typeNextCharacter() {
        if (currentChar < dialogues[currentDialogue].length()) {
            textDisplay.append(String.valueOf(dialogues[currentDialogue].charAt(currentChar)));
            currentChar++;
        } else {
            typingTimer.stop();
        }
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
        frame.setLayout(new GraphPaperLayout(new Dimension(24, 17)));

        frame.add(textDisplay, new Rectangle(2, 12, 20, 3));
        frame.add(b1, new Rectangle(0, 15, 4, 2));
        frame.add(b2, new Rectangle(20, 15, 4, 2));
        frame.add(basiliol, new Rectangle(0, 0, 24, 17));
        frame.add(simounr, new Rectangle(14, 0, 24, 17));
        frame.add(bgbg2, new Rectangle(0, 0, 24, 17));
        
        
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        typingTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PlaySound clicks = new PlaySound();
        clicks.playEffect("Audio/clicks.wav");

        if (e.getSource() == b2 && currentDialogue < dialogues.length - 1) {
            currentDialogue++;
            currentChar = 0;
            textDisplay.setText("");
            typingTimer.start();
            if (currentDialogue == 1) {
                basiliol.setVisible(true);
            }
            if (currentDialogue == 2) {
                simounr.setVisible(true);
            }

        } else if (e.getSource() == b1) {
            if (currentDialogue > 0) {
                currentDialogue--;
                currentChar = 0;
                textDisplay.setText("");
                typingTimer.start();
                if (currentDialogue < 1) {
                    basiliol.setVisible(false);
                }
                if (currentDialogue < 2) {
                    simounr.setVisible(false);
                }

            } else {
                startButtonActionPerformed(e);
            }
        }

        
        if (currentDialogue == dialogues.length - 1 && !typingTimer.isRunning()) {
            int choice = JOptionPane.showOptionDialog(
                frame,
                "Would you like to play the tutorial first?",
                "Tutorial Option",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Yes, show tutorial", "No, start game"},
                "Yes, show tutorial"
            );

            if (choice == JOptionPane.YES_OPTION) {
                frame.dispose();
                Tutorial tutorial = new Tutorial();
                tutorial.setframe();
            }
            if (choice == JOptionPane.NO_OPTION) {
                frame.dispose();
                GameProper gameproper = new GameProper();
                gameproper.setframe();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameStart sta = new GameStart();
            sta.setframe();
        });
    }

    // Modify your existing background music initialization
    private void initBackgroundMusic() {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                new File("Audio/menubackground.wav"));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            
            FloatControl gainControl = (FloatControl) backgroundMusic.getControl(
                FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(0.2) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
            
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.out.println("Error playing background music: " + e.getMessage());
        }
    }

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
        stopMusic();
        dispose();
        
        SwingUtilities.invokeLater(() -> {
            GameProper game = new GameProper();
            game.setframe();
        });
    }

    private void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }
}
