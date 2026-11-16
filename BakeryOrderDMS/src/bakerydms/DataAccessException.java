/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 13, 2026 (Phase 4)
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
 * A database problem, with a message that is safe to show the user.
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
