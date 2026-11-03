/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 1, 2026 (Phase 3)
 * Class:   CakeLogo
 *
 * Purpose: Draws the bakery's cake logo with Java 2D graphics. The picture
 *          is drawn in code instead of loaded from an image file, so it can
 *          never go missing and looks sharp at any size. It is shown in the
 *          window's header and used as the window's taskbar icon.
 */
package bakerydms;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Draws a two-tier cake with a candle.
 */
public class CakeLogo {

    /** Colors used in the drawing. */
    private final Color frosting = new Color(0xF6DFE3);
    private final Color sponge = new Color(0xA3354C);
    private final Color plate = new Color(0xEFE6D6);
    private final Color candle = new Color(0xF8F3EA);
    private final Color flame = new Color(0xF2B33D);
    private final Color outline = new Color(0x2D211B);

    /**
     * Draws the logo into a new image.
     *
     * @param size width and height of the image in pixels
     * @return the finished picture, with a transparent background
     */
    public BufferedImage createImage(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Draw on a 100 x 100 grid, then scale it to the size asked for.
            double s = size / 100.0;
            g.scale(s, s);
            g.setStroke(new BasicStroke(3f));

            // Plate
            g.setColor(plate);
            g.fill(new RoundRectangle2D.Double(8, 82, 84, 10, 10, 10));

            // Bottom tier: sponge with a frosting band on top
            g.setColor(sponge);
            g.fill(new RoundRectangle2D.Double(14, 54, 72, 30, 8, 8));
            g.setColor(frosting);
            g.fill(new RoundRectangle2D.Double(14, 54, 72, 12, 8, 8));
            drawDrips(g, 14, 64, 72);

            // Top tier
            g.setColor(sponge);
            g.fill(new RoundRectangle2D.Double(28, 32, 44, 24, 8, 8));
            g.setColor(frosting);
            g.fill(new RoundRectangle2D.Double(28, 32, 44, 10, 8, 8));
            drawDrips(g, 28, 40, 44);

            // Candle and flame
            g.setColor(candle);
            g.fill(new RoundRectangle2D.Double(46, 14, 8, 20, 3, 3));
            g.setColor(sponge);
            g.fillRect(46, 20, 8, 3);
            g.fillRect(46, 27, 8, 3);
            g.setColor(flame);
            g.fill(new Ellipse2D.Double(45, 1, 10, 14));

            // Outlines so the shape stands out on light and dark backgrounds
            g.setColor(outline);
            g.draw(new RoundRectangle2D.Double(14, 54, 72, 30, 8, 8));
            g.draw(new RoundRectangle2D.Double(28, 32, 44, 24, 8, 8));
            g.draw(new RoundRectangle2D.Double(46, 14, 8, 20, 3, 3));
        } finally {
            g.dispose(); // always release the drawing resources
        }
        return image;
    }

    /**
     * Draws small rounded "drips" of frosting along the bottom of a frosting band.
     *
     * @param g     where to draw
     * @param left  left edge of the band
     * @param top   y position where the drips start
     * @param width width of the band
     * @return the number of drips drawn
     */
    private int drawDrips(Graphics2D g, int left, int top, int width) {
        g.setColor(frosting);
        int count = 0;
        for (int x = left + 6; x < left + width - 6; x += 12) {
            g.fill(new Ellipse2D.Double(x, top - 2, 7, 8));
            count++;
        }
        return count;
    }
}
