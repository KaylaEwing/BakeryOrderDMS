/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 1, 2026 (Phase 3)
 * Class:   Theme
 *
 * Purpose: Keeps the look of the GUI in one place: the bakery color
 *          palette, fonts, and ready-styled buttons, labels and text fields.
 *          Every window part asks the Theme for its components, so the whole
 *          program looks consistent and colors can be changed in one spot.
 *
 *          Palette (same colors as the Module 4 and 5 slides):
 *            ink #2D211B (dark brown text and header), cream #F8F3EA
 *            (background), card #FFFCF6, raspberry #A3354C (main buttons),
 *            blue #2C6A8A (update), green #2E6B3F (success messages).
 */
package bakerydms;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Colors, fonts and styled components for the bakery GUI.
 */
public class Theme {

    // ---- Colors ----
    /** Dark brown used for the header and main text. */
    private final Color ink = new Color(0x2D211B);
    /** Window background. */
    private final Color cream = new Color(0xF8F3EA);
    /** Slightly darker cream used for bars and stripes. */
    private final Color creamDark = new Color(0xEFE6D6);
    /** Background of the white "cards". */
    private final Color card = new Color(0xFFFCF6);
    /** Light brown used for borders. */
    private final Color line = new Color(0xD9CDBD);
    /** Softer brown for hints and secondary text. */
    private final Color muted = new Color(0x6B5A4E);
    /** Raspberry: main action color. */
    private final Color raspberry = new Color(0xA3354C);
    /** Pale pink used in the header and selected rows. */
    private final Color pink = new Color(0xF6DFE3);
    /** Blue used for the Update button. */
    private final Color blue = new Color(0x2C6A8A);
    /** Green used for success messages. */
    private final Color green = new Color(0x2E6B3F);
    /** Red used for error messages and borders. */
    private final Color error = new Color(0xB3261E);

    /** The font family used for normal text, chosen from what this computer has. */
    private final String bodyFamily;
    /** The font family used for titles. */
    private final String titleFamily;

    /**
     * Creates the theme and picks fonts that exist on this computer
     * (Segoe UI on Windows, Helvetica on Mac, otherwise a safe fallback).
     */
    public Theme() {
        this.bodyFamily = firstInstalled(Arrays.asList("Segoe UI", "Helvetica Neue", "Arial", "DejaVu Sans"), "SansSerif");
        this.titleFamily = firstInstalled(Arrays.asList("Georgia", "Cambria", "DejaVu Serif"), "Serif");
    }

    /**
     * Finds the first font family in the list that is installed.
     *
     * @param wanted   font families in order of preference
     * @param fallback the family to use if none are installed
     * @return the chosen family name
     */
    private String firstInstalled(List<String> wanted, String fallback) {
        try {
            List<String> installed = Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            for (String family : wanted) {
                if (installed.contains(family)) {
                    return family;
                }
            }
        } catch (RuntimeException e) {
            // If the font list can't be read, Java's built-in font is used instead.
        }
        return fallback;
    }

    // ---- Color getters ----

    /** @return the dark ink color */
    public Color getInk() { return ink; }
    /** @return the cream background color */
    public Color getCream() { return cream; }
    /** @return the darker cream color */
    public Color getCreamDark() { return creamDark; }
    /** @return the card background color */
    public Color getCard() { return card; }
    /** @return the border color */
    public Color getLine() { return line; }
    /** @return the muted text color */
    public Color getMuted() { return muted; }
    /** @return the raspberry accent color */
    public Color getRaspberry() { return raspberry; }
    /** @return the pale pink color */
    public Color getPink() { return pink; }
    /** @return the success green color */
    public Color getGreen() { return green; }
    /** @return the error red color */
    public Color getError() { return error; }

    // ---- Fonts ----

    /**
     * @param style Font.PLAIN, Font.BOLD or Font.ITALIC
     * @param size  point size
     * @return the body font in that style and size
     */
    public Font body(int style, int size) {
        return new Font(bodyFamily, style, size);
    }

    /**
     * @param size point size
     * @return the bold title font
     */
    public Font title(int size) {
        return new Font(titleFamily, Font.BOLD, size);
    }

    /**
     * @param size point size
     * @return a fixed-width font, used where numbers must line up
     */
    public Font mono(int size) {
        return new Font(Font.MONOSPACED, Font.PLAIN, size);
    }

    // ---- Buttons ----

    /** @param text button text @return a raspberry button for the main action on a panel */
    public JButton primaryButton(String text) {
        return button(text, raspberry, Color.WHITE, raspberry.darker());
    }

    /** @param text button text @return a blue button (used for Update) */
    public JButton blueButton(String text) {
        return button(text, blue, Color.WHITE, blue.darker());
    }

    /** @param text button text @return a light button with a dark outline */
    public JButton secondaryButton(String text) {
        return button(text, card, ink, line);
    }

    /** @param text button text @return a dark button with a pink outline, for the dark header (Exit) */
    public JButton headerButton(String text) {
        return button(text, ink, cream, pink);
    }

    /** @param text button text @return a light button with a red outline (used for Delete) */
    public JButton dangerButton(String text) {
        return button(text, card, raspberry, raspberry);
    }

    /**
     * Builds a flat, colored button that looks the same on Windows, Mac and Linux.
     * BasicButtonUI is used so the operating system does not replace the colors.
     *
     * @param text       button text
     * @param background fill color
     * @param foreground text color
     * @param border     outline color
     * @return the styled button
     */
    private JButton button(String text, Color background, Color foreground, Color border) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setFont(body(Font.BOLD, 14));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        // Darken slightly while the mouse is over the button, so it feels clickable.
        Color hover = blend(background, ink, 0.12f);
        button.getModel().addChangeListener(e ->
                button.setBackground(button.getModel().isRollover() ? hover : background));
        return button;
    }

    /**
     * Mixes two colors.
     *
     * @param a      the first color
     * @param b      the second color
     * @param amount how much of the second color to use (0 to 1)
     * @return the mixed color
     */
    private Color blend(Color a, Color b, float amount) {
        float keep = 1 - amount;
        return new Color(
                Math.round(a.getRed() * keep + b.getRed() * amount),
                Math.round(a.getGreen() * keep + b.getGreen() * amount),
                Math.round(a.getBlue() * keep + b.getBlue() * amount));
    }

    // ---- Labels and text fields ----

    /** @param text label text @return a bold label for a form field */
    public JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(body(Font.BOLD, 13));
        label.setForeground(ink);
        return label;
    }

    /** @param text hint text @return a small gray hint shown under a field */
    public JLabel hint(String text) {
        JLabel label = new JLabel(text);
        label.setFont(body(Font.PLAIN, 11));
        label.setForeground(muted);
        return label;
    }

    /** @param text heading text @return a section heading such as "Order Details" */
    public JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(title(18));
        label.setForeground(ink);
        return label;
    }

    /**
     * @param columns approximate width in characters
     * @return a text field with the bakery border and padding
     */
    public JTextField textField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(body(Font.PLAIN, 14));
        field.setForeground(ink);
        field.setBackground(Color.WHITE);
        field.setBorder(fieldBorder(false));
        return field;
    }

    /**
     * The border for a text field: brown normally, red when its value is wrong.
     *
     * @param hasError true to show the red error border
     * @return the border
     */
    public Border fieldBorder(boolean hasError) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(hasError ? error : line, hasError ? 2 : 1),
                BorderFactory.createEmptyBorder(hasError ? 5 : 6, hasError ? 7 : 8, hasError ? 5 : 6, hasError ? 7 : 8));
    }

    /**
     * A card border: thin outline with padding inside.
     *
     * @param padding space inside the card, in pixels
     * @return the border
     */
    public Border cardBorder(int padding) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line, 1),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }
}
