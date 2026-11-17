/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 13, 2026 (Phase 4); documented November 16, 2026 (Module 11)
 * Class:   DataAccessException
 *
 * Purpose: Reports a database problem (no connection, wrong password,
 *          server stopped, missing table...) in plain English. The
 *          database classes catch Java's SQLException and turn it into
 *          this exception with a message the user can understand. The GUI
 *          catches it and shows the message, so a database problem never
 *          crashes the program.
 */
package bakerydms;

/**
 * Signals a database problem, with a message written for the user.
 *
 * <p>{@link DatabaseConnector} and {@link MySqlOrderRepository} catch every
 * {@link java.sql.SQLException} and turn it into this exception with a
 * plain-English message, such as "Access denied for user root. The username or
 * password is incorrect." {@link BakeryOrderWindow} shows the message in a
 * dialog, so a database problem never crashes the program. It is an unchecked
 * exception so that {@link OrderRepository} methods stay simple.
 *
 *
 * @author Kayla Ewing
 * @version 4.0 (Phase 4, Module 11)
 */
@SuppressWarnings("serial") // never saved to disk, so no version ID is needed
public class DataAccessException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message a plain-English description of the problem
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Creates the exception and keeps the original database error for debugging.
     *
     * @param message a plain-English description of the problem
     * @param cause   the original error from the database driver
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
