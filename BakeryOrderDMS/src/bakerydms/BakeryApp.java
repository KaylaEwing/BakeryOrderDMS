/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026 (Phase 1); updated November 13, 2026 (Phase 4); documented November 16, 2026 (Module 11)
 * Class:   BakeryApp
 *
 * Purpose: The starting point of the Bakery Order Management System.
 *          The program is used only through a graphical window. This class
 *          builds the objects the program needs (validator, order manager,
 *          database connector) and opens the BakeryOrderWindow. The program
 *          does not connect to any database by itself: the user types their
 *          own MySQL server details in the window and clicks Connect.
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
 * Starts the Bakery Order Management System.
 *
 * <p>This is the program's entry point. {@link #main(String[])} creates a
 * {@code BakeryApp}, and {@link #start()} wires the three layers together:
 * <ol>
 *   <li>an {@link OrderValidator} with the four cake types the bakery sells,</li>
 *   <li>an {@link OrderManager} (business logic) using a 7% sales tax rate,</li>
 *   <li>a {@link DatabaseConnector} (data layer), and</li>
 *   <li>the {@link BakeryOrderWindow} (presentation layer), opened on the Swing thread.</li>
 * </ol>
 * The program starts with no database connected; the user enters their MySQL
 * details in the window. It also installs a last-resort error handler, so an
 * unexpected error is shown in a message box instead of closing the program.
 *
 *
 * @author Kayla Ewing
 * @version 4.0 (Phase 4, Module 11)
 */
public class BakeryApp {

    /** Sales tax rate used for order totals and the daily report (7%). */
    private final double taxRate = 0.07;

    /**
     * Creates the application. Nothing is shown until {@link #start()} is called.
     */
    public BakeryApp() {
        // nothing to set up: every field is given its value where it is declared
    }

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
        OrderManager manager = new OrderManager(validator, taxRate);
        DatabaseConnector connector = new DatabaseConnector();

        // Last safety net: if anything unexpected is ever thrown, show a message and keep running.
        Thread.setDefaultUncaughtExceptionHandler((thread, error) ->
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                        "Something unexpected went wrong, but the program is still running.\nDetails: "
                                + error.getClass().getSimpleName(),
                        "Unexpected Problem", JOptionPane.ERROR_MESSAGE)));

        // Swing windows must be created on Swing's own thread (the Event Dispatch Thread).
        SwingUtilities.invokeLater(() -> new BakeryOrderWindow(manager, connector).show());
        return true;
    }
}
