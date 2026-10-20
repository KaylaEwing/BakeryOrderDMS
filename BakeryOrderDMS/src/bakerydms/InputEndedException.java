/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026
 * Class:   InputEndedException
 *
 * Purpose: Signals that the keyboard input stream was closed (for example,
 *          the user pressed Ctrl+D or Ctrl+Z). Without this, Scanner would
 *          throw an error and crash the program. BakeryApp catches it and
 *          ends the program politely instead.
 */
package bakerydms;

/**
 * Thrown when there is no more keyboard input to read.
 */
@SuppressWarnings("serial") // never serialized, so no version ID is needed
public class InputEndedException extends RuntimeException {

    /**
     * Creates the exception with a standard message.
     */
    public InputEndedException() {
        super("Keyboard input was closed.");
    }
}
