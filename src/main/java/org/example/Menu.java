package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class Menu {

    // UI Elements
    private Rectangle playButton; // Play game button bounds
    private Rectangle helpButton; // Help/instructions button bounds
    private Rectangle loserboardButton; // Loserboard button bounds
    private BufferedImage menuBackground; // Background image for menu
    private BufferedImage dinoLogo; // Game logo image
    private Point mousePosition = new Point(0, 0); // Current mouse position for hover effects
    private Rectangle quitButton; // Quit game button bounds


    public Menu(){
        try {
            // Load menu graphics
            menuBackground = ImageIO.read(new File("src/main/resources/menu.png"));
            dinoLogo = ImageIO.read(new File("src/main/resources/dino-logo-2.png"));
        } catch (IOException | IllegalArgumentException e){
            e.printStackTrace();
        }

        // Set up button dimensions and positions
        int buttonWidth = 200;
        int buttonHeight = 50;
        int centerX = 300 - buttonWidth / 2; // Center buttons horizontally (600px width)

        // Initialize button rectangles
        playButton = new Rectangle(centerX, 250, buttonWidth, buttonHeight);
        helpButton = new Rectangle(centerX, 320, buttonWidth, buttonHeight);
        loserboardButton = new Rectangle(centerX, 390, buttonWidth, buttonHeight);
        quitButton = new Rectangle(centerX, 460, buttonWidth, buttonHeight);
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw background
        if (menuBackground!= null){
            g2d.drawImage(menuBackground, 0, 0, 600, 600, null);
        } else {
            // Fallback background if image fails to load
            g2d.setColor(new Color(90, 130, 90));
            g2d.fillRect(0, 0, 600,600);
        }


        // Draw the Dino logo image at the top
        if (dinoLogo != null) {
            int logoWidth = 400;
            int logoHeight = 250;
            int x = (600 - logoWidth) / 2; // Center horizontally
            int y = 0; // Top of screen
            g2d.drawImage(dinoLogo, x, y, logoWidth, logoHeight, null);
        }


        // Set up button font
        Font buttonFont = new Font("Arial", Font.BOLD, 20);

        g2d.setColor(Color.WHITE);

        // Draw all menu buttons
        drawButton(g2d, playButton, "Play", buttonFont);
        drawButton(g2d, helpButton, "Help", buttonFont);
        drawButton(g2d, loserboardButton, "Loserboard", buttonFont);
        drawButton(g2d, quitButton, "Quit", buttonFont);

    }

    private void drawButton(Graphics2D g2, Rectangle rect, String text, Font font) {
        // Determine colors based on hover state
        boolean hover = rect.contains(mousePosition);
        Color fill = hover ? new Color(220, 180, 90) : new Color(240, 210, 100); // Hover/normal fill
        Color border = new Color(100, 60, 20); // Border color

        // Draw button background
        g2.setColor(fill);
        g2.fill(rect);

        // Draw button border
        g2.setColor(border);
        g2.setStroke(new BasicStroke(3));
        g2.draw(rect);

        // Draw button text
        g2.setFont(font);
        g2.setColor(Color.BLACK);
        drawCenteredString(g2, text, rect);
    }

    private void drawCenteredString(Graphics2D g2, String text, Rectangle rect) {
        FontMetrics metrics = g2.getFontMetrics(g2.getFont());
        // Calculate centered position
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g2.drawString(text, x, y);
    }

     // Button click detection methods
    public boolean isPlayClicked(MouseEvent e) {
        return playButton.contains(e.getPoint());
    }

    public boolean isHelpClicked(MouseEvent e) {
        return helpButton.contains(e.getPoint());
    }

    public boolean isLoserboardClicked(MouseEvent e) {
        return loserboardButton.contains(e.getPoint());
    }

    public boolean isQuitClicked(MouseEvent e) {return quitButton.contains(e.getPoint()); }


    public void checkHover(Point point) {
        mousePosition = point;
    }
}
