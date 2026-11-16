/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 13, 2026 (Phase 4)
 * Class:   DatabaseConnectorTest
 *
 * Purpose: Unit tests for the database connection details. Positive tests
 *          prove valid details are accepted and the connection string is
 *          built by concatenation from what the user typed. Negative tests
 *          prove blank and badly formatted details are rejected with a clear
 *          message, and that a failed connection never crashes the program.
 *          These tests do not need a MySQL server.
 */
package bakerydms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("1. Connecting to the database")
class DatabaseConnectorTest {

    /** The class being tested. */
    private final DatabaseConnector connector = new DatabaseConnector();

    /** An example password; the tests never connect with it. */
    private final char[] password = "example".toCharArray();

    // ---------------- POSITIVE (affirmative) tests ----------------

    @Test
    @DisplayName("PASS: valid details are accepted")
    void validDetailsAccepted() {
        assertTrue(connector.validate("localhost", "3306", "bakery_dms", "root", password).isEmpty());
        assertTrue(connector.validate("127.0.0.1", "", "bakery_dms", "root", new char[0]).isEmpty(),
                "A blank port means 3306, and a blank password is allowed");
        assertTrue(connector.validate("localhost:3307", "", "bakery_dms", "root", password).isEmpty(),
                "A port typed after the server address is accepted");
    }

    @Test
    @DisplayName("PASS: the connection string is built from the user's details")
    void urlIsBuiltFromUserDetails() {
        String url = connector.buildUrl("db.example.com", 3307, "bakery_dms");
        assertTrue(url.startsWith("jdbc:mysql://db.example.com:3307/bakery_dms?"), url);
        assertFalse(url.contains("password"), "The password is never put in the connection string");
    }

    // ---------------- NEGATIVE tests ----------------

    @Test
    @DisplayName("FAIL CASE: blank server, database and username are each reported")
    void blankDetailsRejected() {
        List<String> problems = connector.validate("  ", "3306", "", "", password);
        assertEquals(3, problems.size(), problems.toString());
        String all = String.join(" ", problems);
        assertTrue(all.contains("Server address is blank"));
        assertTrue(all.contains("Database name is blank"));
        assertTrue(all.contains("Username is blank"));
    }

    @Test
    @DisplayName("FAIL CASE: a port that is not 1-65535 is rejected")
    void badPortRejected() {
        assertFalse(connector.validate("localhost", "abc", "bakery_dms", "root", password).isEmpty());
        assertFalse(connector.validate("localhost", "0", "bakery_dms", "root", password).isEmpty());
        assertFalse(connector.validate("localhost", "70000", "bakery_dms", "root", password).isEmpty());
    }

    @Test
    @DisplayName("FAIL CASE: a server address or database name with illegal characters is rejected")
    void badNamesRejected() {
        assertFalse(connector.validate("local host", "3306", "bakery_dms", "root", password).isEmpty());
        assertFalse(connector.validate("localhost", "3306", "bakery;DROP", "root", password).isEmpty());
    }

    @Test
    @DisplayName("FAIL CASE: connecting with blank details throws a readable error, not a crash")
    void connectWithBlankDetailsGivesMessage() {
        DataAccessException error = assertThrows(DataAccessException.class,
                () -> connector.connect("", "3306", "bakery_dms", "root", password));
        assertTrue(error.getMessage().contains("Server address is blank"), error.getMessage());
    }

    @Test
    @DisplayName("FAIL CASE: using the manager before connecting gives a readable error")
    void managerBeforeConnecting() {
        OrderManager manager = new OrderManager(new TestHelper().newValidator(), 0.07);
        assertFalse(manager.isConnected());
        DataAccessException error = assertThrows(DataAccessException.class, manager::getAllOrders);
        assertTrue(error.getMessage().contains("Not connected"), error.getMessage());
    }
}
