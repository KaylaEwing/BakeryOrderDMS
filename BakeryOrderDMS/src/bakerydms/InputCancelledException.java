/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026 (Phase 1); updated October 26, 2026 (Phase 2)
 * Class:   InputCancelledException
 *
 * Purpose: Signals that the user typed "cancel" in the middle of adding,
 *          updating or removing an order. BakeryApp catches it and returns
 *          to the main menu without changing any data.
 */
package bakerydms;

/**
 * Thrown when the user types "cancel" at a prompt.
 */
@SuppressWarnings("serial") // never serialized, so no version ID is needed
public class InputCancelledException extends RuntimeException {

    /**
     * Creates the exception with a standard message.
     */
    public InputCancelledException() {
        super("The user cancelled.");
    }
}
