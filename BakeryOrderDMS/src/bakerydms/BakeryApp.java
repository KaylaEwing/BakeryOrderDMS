/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026 (Phase 1); updated November 1, 2026 (Phase 3)
 * Class:   BakeryApp
 *
 * Purpose: The starting point of the Bakery Order Management System.
 *          In Phase 3 the program is used only through a graphical window,
 *          so this class builds the objects the program needs (validator,
 *          file loader, order manager) and opens the BakeryOrderWindow.
 *
 *          main() is the only static method in the project. Java requires
 *          the program's entry point to be static; main() simply creates a
 *          BakeryApp object and calls start(), and all other code is in
 *          instance methods.
 */
package bakerydms;

import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Starts the bakery program.
 */
public class BakeryApp {

    /** Sales tax rate used for order totals and the daily report (7%). */
    private final double taxRate = 0.07;

    /**
     * Program entry point: creates the app and starts it.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        new BakeryApp().start();
    }

    /**
     * Sets up the look of the program and opens the main window.
     *
     * @return true if the window was opened, false if this computer has no screen
     */
    public boolean start() {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("The Bakery Order Manager needs a screen to show its window.");
            return false;
        }
        // Use the operating system's own look (Windows, Mac...) for text boxes and file pickers.
        // If that fails, Java's default look is used instead, so this can never stop the program.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Using Java's default look: " + e.getMessage());
        }

        OrderValidator validator = new OrderValidator(List.of("Chocolate", "Vanilla", "Red Velvet", "Carrot"));
        OrderFileLoader loader = new OrderFileLoader(",", validator);
        OrderManager manager = new OrderManager(validator, loader, taxRate);

        // Last safety net: if anything unexpected is ever thrown, show a message and keep running.
        Thread.setDefaultUncaughtExceptionHandler((thread, error) ->
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                        "Something unexpected went wrong, but the program is still running.\nDetails: "
                                + error.getClass().getSimpleName(),
                        "Unexpected Problem", JOptionPane.ERROR_MESSAGE)));

        // Swing windows must be created on Swing's own thread (the Event Dispatch Thread).
        SwingUtilities.invokeLater(() -> new BakeryOrderWindow(manager).show());
        return true;
    }
}
