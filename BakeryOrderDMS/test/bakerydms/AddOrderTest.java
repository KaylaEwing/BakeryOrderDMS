/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 26, 2026 (Phase 2)
 * Class:   AddOrderTest
 *
 * Purpose: Unit tests for CREATE. Positive tests prove a valid order is
 *          added and can be found with exactly the data entered. Negative
 *          tests prove every kind of bad data is rejected and the system is
 *          left unchanged.
 */
package bakerydms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("2. Adding orders")
class AddOrderTest {

    /** Creates the manager and sample data. */
    private final TestHelper helper = new TestHelper();

    /** A fresh manager holding the six sample orders. */
    private OrderManager manager;

    /** A valid pickup date for new orders. */
    private final LocalDate pickup = LocalDate.of(2027, 2, 13);

    /** Starts every test with the same six sample orders. */
    @BeforeEach
    void setUp() {
        manager = helper.managerWithSampleOrders();
    }

    /**
     * Checks that an add was rejected for the expected reason and that
     * nothing was stored.
     *
     * @param errors        what addOrder returned
     * @param expectedWords words the error message should contain
     */
    private void assertRejected(List<String> errors, String expectedWords) {
        assertFalse(errors.isEmpty(), "The order should have been rejected");
        assertTrue(String.join(" ", errors).contains(expectedWords),
                "Expected the message to mention \"" + expectedWords + "\" but got: " + errors);
        assertEquals(6, manager.getOrderCount(), "A rejected order must not be stored");
        assertTrue(manager.findOrderById(1026).isEmpty(), "Order 1026 must not exist");
    }

    // ---------------- POSITIVE (affirmative) tests ----------------

    @Test
    @DisplayName("PASS: a valid order is added and can be found with the same data")
    void validOrderIsAdded() {
        List<String> errors = manager.addOrder(1026, "Dana Lee", "407-555-9876", "Red Velvet",
                2, 28.00, pickup, "Write Happy Birthday");

        assertTrue(errors.isEmpty(), "No errors expected, got: " + errors);
        assertEquals(7, manager.getOrderCount());

        Order saved = manager.findOrderById(1026).orElseThrow();
        assertEquals("Dana Lee", saved.getCustomerName());
        assertEquals("407-555-9876", saved.getPhoneNumber());
        assertEquals("Red Velvet", saved.getCakeType());
        assertEquals(2, saved.getQuantity());
        assertEquals(28.00, saved.getUnitPrice(), 0.001);
        assertEquals(pickup, saved.getPickupDate());
        assertFalse(saved.isPickedUp(), "New orders start as not picked up");
        assertEquals("Write Happy Birthday", saved.getSpecialInstructions());
    }

    @Test
    @DisplayName("PASS: the new order's total is $59.92 (2 x $28.00 + 7% tax)")
    void newOrderTotalIsCorrect() {
        manager.addOrder(1026, "Dana Lee", "407-555-9876", "Red Velvet", 2, 28.00, pickup, "");
        assertEquals(59.92, manager.calculateTotal(manager.findOrderById(1026).orElseThrow()), 0.001);
    }

    @Test
    @DisplayName("PASS: messy input is cleaned up (phone format, cake spelling, spaces)")
    void inputIsNormalized() {
        List<String> errors = manager.addOrder(1026, "  Dana   Lee ", "(407) 555 9876", "red VELVET",
                1, 28, pickup, "  ");

        assertTrue(errors.isEmpty(), errors.toString());
        Order saved = manager.findOrderById(1026).orElseThrow();
        assertEquals("Dana Lee", saved.getCustomerName());
        assertEquals("407-555-9876", saved.getPhoneNumber());
        assertEquals("Red Velvet", saved.getCakeType());
        assertEquals("", saved.getSpecialInstructions());
    }

    @Test
    @DisplayName("PASS: an order due today is accepted")
    void orderDueTodayIsAccepted() {
        assertTrue(manager.addOrder(1026, "Dana Lee", "4075559876", "Vanilla", 1, 22,
                helper.getToday(), "").isEmpty());
    }

    // ---------------- NEGATIVE tests ----------------

    @Test
    @DisplayName("FAIL CASE: a duplicate order ID is rejected")
    void duplicateIdIsRejected() {
        List<String> errors = manager.addOrder(1001, "Dana Lee", "407-555-9876", "Vanilla", 1, 22, pickup, "");
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("already exists"));
        assertEquals(6, manager.getOrderCount());
        // The original order 1001 is unchanged.
        assertEquals("Chocolate", manager.findOrderById(1001).orElseThrow().getCakeType());
    }

    @Test
    @DisplayName("FAIL CASE: an order ID outside 1000-9999 is rejected")
    void badIdIsRejected() {
        assertFalse(manager.addOrder(999, "Dana Lee", "407-555-9876", "Vanilla", 1, 22, pickup, "").isEmpty());
        assertFalse(manager.addOrder(10000, "Dana Lee", "407-555-9876", "Vanilla", 1, 22, pickup, "").isEmpty());
        assertEquals(6, manager.getOrderCount());
    }

    @Test
    @DisplayName("FAIL CASE: a blank or invalid customer name is rejected")
    void badNameIsRejected() {
        assertRejected(manager.addOrder(1026, "", "407-555-9876", "Vanilla", 1, 22, pickup, ""), "Customer name");
        assertRejected(manager.addOrder(1026, "J", "407-555-9876", "Vanilla", 1, 22, pickup, ""), "Customer name");
        assertRejected(manager.addOrder(1026, "123abc", "407-555-9876", "Vanilla", 1, 22, pickup, ""), "Customer name");
        assertRejected(manager.addOrder(1026, null, "407-555-9876", "Vanilla", 1, 22, pickup, ""), "Customer name");
    }

    @Test
    @DisplayName("FAIL CASE: a phone number without 10 digits is rejected")
    void badPhoneIsRejected() {
        assertRejected(manager.addOrder(1026, "Dana Lee", "555-1234", "Vanilla", 1, 22, pickup, ""), "Phone");
        assertRejected(manager.addOrder(1026, "Dana Lee", "phone", "Vanilla", 1, 22, pickup, ""), "Phone");
    }

    @Test
    @DisplayName("FAIL CASE: a cake type the bakery doesn't sell is rejected")
    void badCakeTypeIsRejected() {
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Lemon", 1, 22, pickup, ""), "Cake type");
    }

    @Test
    @DisplayName("FAIL CASE: quantities 0, -1 and 51 are rejected")
    void badQuantityIsRejected() {
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 0, 22, pickup, ""), "Quantity");
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", -1, 22, pickup, ""), "Quantity");
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 51, 22, pickup, ""), "Quantity");
    }

    @Test
    @DisplayName("FAIL CASE: prices of $0, negative, over $1000, 3 decimals or NaN are rejected")
    void badPriceIsRejected() {
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 1, 0, pickup, ""), "Unit price");
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 1, -5, pickup, ""), "Unit price");
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 1, 1000.01, pickup, ""), "Unit price");
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 1, 28.999, pickup, ""), "Unit price");
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 1, Double.NaN, pickup, ""), "Unit price");
    }

    @Test
    @DisplayName("FAIL CASE: a pickup date in the past, too far ahead, or missing is rejected")
    void badDateIsRejected() {
        LocalDate yesterday = helper.getToday().minusDays(1);
        LocalDate tooFar = helper.getToday().plusYears(2).plusDays(1);
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 1, 22, yesterday, ""), "Pickup date");
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 1, 22, tooFar, ""), "Pickup date");
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 1, 22, null, ""), "Pickup date");
    }

    @Test
    @DisplayName("FAIL CASE: special instructions over 100 characters are rejected")
    void longInstructionsAreRejected() {
        String tooLong = "x".repeat(101);
        assertRejected(manager.addOrder(1026, "Dana Lee", "407-555-9876", "Vanilla", 1, 22, pickup, tooLong),
                "Special instructions");
    }

    @Test
    @DisplayName("FAIL CASE: a missing (null) order is rejected without crashing")
    void nullOrderIsRejected() {
        assertFalse(manager.addOrder(null).isEmpty());
        assertEquals(6, manager.getOrderCount());
    }
}
