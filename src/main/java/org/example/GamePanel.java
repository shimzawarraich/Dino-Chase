package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.Timer; // for the countdown timer
import java.awt.Color;


class GamePanel extends JPanel implements KeyListener {
    // game state and networking
    private Client client;    //reference to the main client
    private final int SPEED = 10;    //player movement spped
    private String myId = "me";    //current player's ID
    private Map<String, Player> allPlayers = new HashMap<>(); //all players in the game
    private TriConsumer<String, Integer, Integer> networkSender; //callback for sending movement updates
    private int timeRemaining = 30;
    private final Image backgroundImage; // game map
    private final Image backgroundImageOverlay; // game map
    private final int CENTER_X = 300; // because image is 600px by 600px
    private final int CENTER_Y = 300;
    private final int WALKABLE_RADIUS = 300; // tuned as needed
    private boolean[][] walkableMask; //collision  mask for map
    private static final int MAP_WIDTH = 600; //width
    private static final int MAP_HEIGHT = 600; //height 
    private final List<Image> avatarImages = new ArrayList<>();
    private long flashUntil = 0;
    private Timer gameTimer;
    private boolean gameOver = false;

    private boolean gameStarted = false;


    public GamePanel() {
        //set up panel properties
        setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setMinimumSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setMaximumSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);

        //set up custom cursor behavior
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (client != null) {
                    // Use the client's hover cursor.
                    setCursor(client.hoverCursor);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (client != null) {
                    // Revert to the client's regular cursor.
                    setCursor(client.regularCursor);
                }
            }
        });

        // Repaint every 200ms to keep cooldown text updating
        new Timer(200, e -> repaint()).start();

        // Load background image
        backgroundImage = new ImageIcon(getClass().getClassLoader().getResource("map.png")).getImage();
        backgroundImageOverlay = new ImageIcon(getClass().getClassLoader().getResource("map-overlay.png")).getImage();

        try {
            //load and process collision mask
            BufferedImage mask = ImageIO.read(getClass().getClassLoader().getResource("mask.png"));
            walkableMask = new boolean[MAP_WIDTH][MAP_HEIGHT];
            //create walkable mask from image 
            for (int x = 0; x < MAP_WIDTH; x++) {
                for (int y = 0; y < MAP_HEIGHT; y++) {
                    int rgb = mask.getRGB(x, y);
                    Color c = new Color(rgb);
                    walkableMask[x][y] = !(c.getRed() > 200 && c.getGreen() < 50 && c.getBlue() < 50);
                }
            }
            //load player avatars
            avatarImages.add(ImageIO.read(getClass().getClassLoader().getResource("blue_trex.png")));
            avatarImages.add(ImageIO.read(getClass().getClassLoader().getResource("yellow-stegosaurus.png")));
            avatarImages.add(ImageIO.read(getClass().getClassLoader().getResource("red-triceratops.png")));
            avatarImages.add(ImageIO.read(getClass().getClassLoader().getResource("pink-ceratosaurus.png")));

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void showGameOver() {
        this.gameOver = true;
        SoundPlayer.playOnce("game-start.wav");

        SoundPlayer.stopLoop();
        repaint();
    }

    public boolean getMyPlayerFacingRight() {
        Player me = allPlayers.get(myId);
        return me != null && me.isFacingRight();
    }
    public void startGameTimer() {
        if (gameTimer != null) return;
        SoundPlayer.playOnce("game-start.wav");

        timeRemaining = 30;
        gameTimer = new Timer(1000, e -> {
            if (timeRemaining > 0) {
                timeRemaining--;
                repaint();
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        gameTimer.start();
    }
    public void setClient(Client client) {
        this.client = client;
        if (client != null) {
            setCursor(client.regularCursor);
        }
    }

    public void setMyId(String id) {
        this.myId = id;
        allPlayers.putIfAbsent(id, new Player(id, 100, 100, Color.BLUE));
    }


    public void setNetworkSender(TriConsumer<String, Integer, Integer> sender) {
        this.networkSender = sender;
    }

    public void updateAllPlayers(Map<String, Player> updated) {
        Map<String, Player> previousPlayers = this.allPlayers;
        this.allPlayers = new LinkedHashMap<>();

        int i = 0;
        for (Map.Entry<String, Player> entry : updated.entrySet()) {
            Player updatedPlayer = entry.getValue();
            Player existingPlayer = previousPlayers.get(entry.getKey());

            //assign avatar image
            updatedPlayer.setAvatar((BufferedImage) avatarImages.get(i % avatarImages.size()));

            if (existingPlayer != null) {
                // Only apply cooldown when the player was chaser and is no longer chaser
                if (existingPlayer.isChaser() && !updatedPlayer.isChaser()) {
                    updatedPlayer.setCooldownEndTime(System.currentTimeMillis() + 4000);
                }

                // Show flash if this client becomes the chaser
                if (!existingPlayer.isChaser() && updatedPlayer.isChaser() && entry.getKey().equals(myId)) {
                    flashUntil = System.currentTimeMillis() + 400;
                    System.out.println("[FLASH] You became the chaser! Flash triggered.");
                    SoundPlayer.playOnce("tag.wav");
                }
            }

            allPlayers.put(entry.getKey(), updatedPlayer);
            i++;
        }

        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //show menu if game hasn't started
        if (!gameStarted) {
            // Draw the menu (which includes your background, etc.).
            if (client != null && client.menuActive) {
                client.menu.render(g);
            }
            return;
        }

        if (gameStarted) {
            // Draw the background image
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, MAP_WIDTH, MAP_HEIGHT, this);
            }

            // Draw the players, timer, overlay, etc.
            Player me = allPlayers.get(myId);
            if (me == null) return;

            //draw all players
            for (Player p : allPlayers.values()) {
                if (p.getAvatar() != null) {
                    int drawSize = 50;
                    int offset = (p.size() - drawSize) / 2;
                    int drawX = p.x() + offset;
                    int drawY = p.y() + offset;
                    //handle flashing effect for recently tagged players
                    Graphics2D g2 = (Graphics2D) g.create();
                    if (p.isRecentlyTagged()) {
                        float alpha = System.currentTimeMillis() / 250 % 2 == 0 ? 0.4f : 1.0f;
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    }
                    //draw player facing correct direction
                    if (p.isFacingRight()) {
                        g2.drawImage(p.getAvatar(), drawX, drawY, drawSize, drawSize, this);
                    } else {
                        g2.drawImage(p.getAvatar(), drawX + drawSize, drawY, -drawSize, drawSize, this);
                    }
                    g2.dispose();
                }

                // Draw cooldown timer if recently tagged
                if (p.isRecentlyTagged()) {
                    int secs = p.getCooldownSecondsRemaining();
                    if (secs > 0) {
                        g.setColor(Color.GREEN);
                        g.setFont(new Font("Arial", Font.BOLD, 12));
                        g.drawString(secs + "s", p.x() + 15, p.y() - 30);
                    }
                }

                // Draw the player's name
                g.setColor(p.isChaser() ? Color.RED : Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.drawString(p.id(), p.x(), p.y() - 5);
            }

            // Draw the timer
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Time: " + timeRemaining + "s", 10, 20);

            // Draw overlay and flash effects
            if (backgroundImageOverlay != null) {
                g.drawImage(backgroundImageOverlay, 0, 0, MAP_WIDTH, MAP_HEIGHT, this);
            }
            // Red flash effect when becoming chaser
            if (System.currentTimeMillis() < flashUntil) {
                g.setColor(new Color(255, 0, 0, 80));
                g.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);
            }
            // Game over message
            if (gameOver) {
                g.setColor(new Color(50, 50, 50));
                g.setFont(new Font("Arial", Font.BOLD, 22));
                g.drawString("Game Over!", MAP_WIDTH / 2 - 70, 60);
            }
        }
    }


    @Override
    public void keyPressed(KeyEvent e) {
        Player me = allPlayers.get(myId);
        if (me == null) return;
        // Calculate movement direction
        int dx = 0, dy = 0;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> dy = -SPEED;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> dy = SPEED;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> dx = -SPEED;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> dx = SPEED;
        }

        // Update facing direction if moving horizontally
        if (dx != 0) {
            me.setFacingRight(dx > 0);
            repaint();
        }

        // Calculate new position
        int newX = me.x() + dx;
        int newY = me.y() + dy;
        boolean isWalkable = true;

        // Create a point for the center of the player
        int playerCenterX = newX + me.size() / 2;
        int playerCenterY = newY + me.size() / 2;

        // Distance from center of map
        double distFromCenter = Math.sqrt(Math.pow(playerCenterX - CENTER_X, 2) + Math.pow(playerCenterY - CENTER_Y, 2));

        int margin = 5;
        for (int offsetX : new int[]{margin, me.size() - margin}) {
            for (int offsetY : new int[]{margin, me.size() - margin}) {
                int checkX = newX + offsetX;
                int checkY = newY + offsetY;
                if (checkX < 0 || checkX >= MAP_WIDTH || checkY < 0 || checkY >= MAP_HEIGHT ||
                        !walkableMask[checkX][checkY]) {
                    isWalkable = false;
                    break;
                }
            }
        }

        // players don't overlap (collision check)
        int drawSize = 50;
        int offset = (me.size() - drawSize) / 2;
        int newDrawX = newX + offset;
        int newDrawY = newY + offset;

        for (Player other : allPlayers.values()) {
            if (!other.id().equals(myId)) {
                int otherOffset = (other.size() - drawSize) / 2;
                int otherDrawX = other.x() + otherOffset;
                int otherDrawY = other.y() + otherOffset;

                if (pixelPerfectCollision(
                        me.getAvatar(), newDrawX, newDrawY, drawSize, drawSize,
                        other.getAvatar(), otherDrawX, otherDrawY, drawSize, drawSize
                )) {
                    isWalkable = false;
                    break;
                }
            }
        }




        // Final movement check
        if (isWalkable && distFromCenter <= WALKABLE_RADIUS) {
            if (networkSender != null) {
                networkSender.accept(myId, newX, newY);
            }
        }

    }

    private boolean pixelPerfectCollision(
            BufferedImage img1, int x1, int y1, int w1, int h1,
            BufferedImage img2, int x2, int y2, int w2, int h2
    ) {
        // Calculate overlapping area
        int xOverlap = Math.max(0, Math.min(x1 + w1, x2 + w2) - Math.max(x1, x2));
        int yOverlap = Math.max(0, Math.min(y1 + h1, y2 + h2) - Math.max(y1, y2));

        if (xOverlap <= 0 || yOverlap <= 0) return false;

        // Check each pixel in overlapping area
        for (int i = 0; i < xOverlap; i++) {
            for (int j = 0; j < yOverlap; j++) {
                // Map screen coordinates to image coordinates
                int img1X = (i + Math.max(0, x2 - x1)) * img1.getWidth() / w1;
                int img1Y = (j + Math.max(0, y2 - y1)) * img1.getHeight() / h1;
                int img2X = (i + Math.max(0, x1 - x2)) * img2.getWidth() / w2;
                int img2Y = (j + Math.max(0, y1 - y2)) * img2.getHeight() / h2;
                // Check alpha channels of both pixels
                int pixel1 = img1.getRGB(img1X, img1Y);
                int pixel2 = img2.getRGB(img2X, img2Y);

                if (((pixel1 >> 24) & 0xff) > 20 && ((pixel2 >> 24) & 0xff) > 20) {
                    return true;
                }
            }
        }

        return false;
    }

    public String[] getAllPlayerNames() {
        String[] names = allPlayers.keySet().toArray(new String[0]); 
        java.util.Arrays.sort(names);  // sort alphabetically for consistency
        return names;
    }

    public void startGame() {
        gameStarted = true;
        repaint();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }


    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}

