package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Wheel extends JDialog {
    private String[] players;
    private int targetIndex;
    private double initialAngle;
    private double finalAngle;
    private long startTime;
    private final long duration = 3000; // Animation lasts 3 seconds.
    private Timer timer;
    private double angle = 0;  // Current angle

    public Wheel(JFrame parent, String[] players, int targetIndex, WheelListener listener) {
        super(parent, "Wheel", true);
        this.players = players;
        this.targetIndex = targetIndex;
        setUndecorated(true);
        getRootPane().setOpaque(false);
        getContentPane().setBackground(new Color(0, 0, 0, 0));
        setBackground(new Color(0, 0, 0, 0));
        setSize(400, 400);
        setLocationRelativeTo(parent);

        double segmentAngle = 2 * Math.PI / players.length;

        // Calculate the desired final angle so that the target slice's center aligns with -π/2.
        double desiredFinalAngle = -Math.PI/2 - (targetIndex * segmentAngle + segmentAngle / 2);
        int fullRotations = 5;
        initialAngle = 0;
        finalAngle = desiredFinalAngle + fullRotations * 2 * Math.PI;
        startTime = System.currentTimeMillis();

        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                double progress = (double) elapsed / duration;
                if (progress >= 1.0) {
                    progress = 1.0;
                    timer.stop();
                    angle = finalAngle;
                    SwingUtilities.invokeLater(() -> {
                        if (listener != null) {
                            listener.onWheelFinished(targetIndex);
                        }
                        dispose();
                    });
                } else {
                    // Ease-out cubic: f(t) = 1 - (1-t)^3
                    double easedProgress = 1 - Math.pow(1 - progress, 3);
                    angle = initialAngle + easedProgress * (finalAngle - initialAngle);
                }
                repaint();
            }
        });
        timer.start();
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        // Create an off-screen image with transparency.
        BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buffer.createGraphics();

        // Clear the off-screen image to fully transparent.
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setComposite(AlphaComposite.SrcOver);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = 150;
        double arcAngle = 2 * Math.PI / players.length;

        AffineTransform originalTransform = g2d.getTransform();
        g2d.rotate(angle, centerX, centerY);

        // Color array for the four slices
        Color[] sliceColors = {
                Color.decode("#67BDC6"), // Custom Blue
                Color.decode("#CD6624"), // Custom Red
                Color.decode("#F2A61D"),  // Custom Yellow
                Color.decode("#9BC749"), // Custom Green
        };

        // Draw the wheel slices with different colors
        for (int i = 0; i < players.length; i++) {
            double startRadian = i * arcAngle;

            // Cycle through the colors
            g2d.setColor(sliceColors[i % sliceColors.length]);
            g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                    (int) Math.toDegrees(startRadian), (int) Math.toDegrees(arcAngle));
            int lineX = centerX + (int) (radius * Math.cos(startRadian));
            int lineY = centerY + (int) (radius * Math.sin(startRadian));
            g2d.setColor(Color.BLACK);
            g2d.drawLine(centerX, centerY, lineX, lineY);
        }

        g2d.drawLine(centerX, centerY, centerX + (int) (radius * Math.cos(0)),
                centerY + (int) (radius * Math.sin(0)));

        // Reset the transform to rotate the labels correctly
        g2d.setTransform(originalTransform);

        // Draw player names on the slices
        for (int i = 0; i < players.length; i++) {
            double sliceCenter = i * arcAngle + arcAngle / 2 + angle;
            int labelX = centerX + (int) (radius * 0.65 * Math.cos(sliceCenter));
            int labelY = centerY + (int) (radius * 0.65 * Math.sin(sliceCenter));
            String text = players[i];
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            g2d.setColor(Color.BLACK);
            g2d.drawString(text, labelX - textWidth / 2, labelY + textHeight / 4);
        }

        // Draw the center arrow
        g2d.setColor(Color.BLACK);
        int[] xPoints = {centerX - 10, centerX + 10, centerX};
        int[] yPoints = {10, 10, 30};
        g2d.fillPolygon(xPoints, yPoints, 3);

        g.drawImage(buffer, 0, 0, null);
        g2d.dispose();
    }

}
