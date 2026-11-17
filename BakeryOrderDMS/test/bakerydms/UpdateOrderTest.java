/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 26, 2026 (Phase 2); documented November 16, 2026 (Module 11)
 * Class:   UpdateOrderTest
 *
 * Purpose: Unit tests for UPDATE. Every one of the 9 attributes is changed
 *          using text exactly as a user would type it (positive tests), and
 *          each attribute is also given bad input to prove the change is
 *          rejected and the stored order stays the same (negative tests).
 *
 *          Field numbers: 1 Order ID, 2 Customer name, 3 Phone, 4 Cake type,
 *          5 Quantity, 6 Unit price, 7 Pickup date, 8 Picked up,
 *          9 Special instructions.
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

/**
 * Unit tests for UPDATE. Every one of the 9 attributes is changed using text
 * exactly as a user would type it (positive tests), and each attribute is
 * also given bad input to prove the change is rejected and the stored order
 * stays the same (negative tests). Field numbers: 1 Order ID, 2 Customer
 * name, 3 Phone, 4 Cake type, 5 Quantity, 6 Unit price, 7 Pickup date, 8
 * Picked up, 9 Special instructions.
 *
 * @author Kayla Ewing
 * @version 4.0 (Phase 4, Module 11)
 */
@DisplayName("4. Updating orders")
class UpdateOrderTest {

    /**
     * Creates the test class. JUnit makes a new one for every test, so
     * no test can affect another.
     */
    UpdateOrderTest() {
        // the fields are set up in their declarations and in setUp()
    }

    /** Creates the manager and sample data. */
    private final TestHelper helper = new TestHelper();

    /** A fresh manager holding the six sample orders. */
    private OrderManager manager;

    /** Order 1001 as it was before each test (Chocolate x2 @ $25, 2027-02-13). */
    private Order original;

    /** Starts every test with the same six sample orders. */
    @BeforeEach
    void setUp() {
        manager = helper.managerWithSampleOrders();
        original = manager.findOrderById(1001).orElseThrow();
    }

    /**
     * Updates one field of order 1001 and checks the update was accepted.
     *
     * @param field    field number (1-9)
     * @param newValue the text the user typed
     * @return the order after the update
     */
    private Order updateAndGet(int field, String newValue) {
        List<String> errors = manager.updateField(1001, field, newValue);
        assertTrue(errors.isEmpty(), "Update should succeed but got: " + errors);
        return manager.findOrderById(1001).orElseThrow();
    }

    /**
     * Tries a bad update on order 1001 and checks it was rejected and nothing changed.
     *
     * @param field    field number (1-9)
     * @param badValue the bad text the user typed
     */
    private void assertUpdateRejected(int field, String badValue) {
        List<String> errors = manager.updateField(1001, field, badValue);
        assertFalse(errors.isEmpty(), "\"" + badValue + "\" should be rejected for field " + field);
        Order after = manager.findOrderById(1001).orElseThrow();
        assertEquals(original.toString(), after.toString(), "A rejected update must not change the order");
    }

    // ---------------- POSITIVE (affirmative) tests: every attribute ----------------

    /**
     * Positive test: 1 Order ID can be changed to an unused ID.
     */
    @Test
    @DisplayName("PASS: 1 Order ID can be changed to an unused ID")
    void updateOrderId() {
        assertTrue(manager.updateField(1001, 1, "1050").isEmpty());
        assertTrue(manager.findOrderById(1001).isEmpty(), "The old ID should be gone");
        assertEquals("Chocolate", manager.findOrderById(1050).orElseThrow().getCakeType());
        assertEquals(6, manager.getOrderCount(), "Changing an ID must not add or remove orders");
    }

    /**
     * Positive test: 2 Customer name can be changed.
     */
    @Test
    @DisplayName("PASS: 2 Customer name can be changed")
    void updateName() {
        assertEquals("Mia Chen-Lopez", updateAndGet(2, "Mia Chen-Lopez").getCustomerName());
    }

    /**
     * Positive test: 3 Phone number can be changed (and is reformatted).
     */
    @Test
    @DisplayName("PASS: 3 Phone number can be changed (and is reformatted)")
    void updatePhone() {
        assertEquals("321-555-0199", updateAndGet(3, "321.555.0199").getPhoneNumber());
    }

    /**
     * Positive test: 4 Cake type can be changed by name or menu number.
     */
    @Test
    @DisplayName("PASS: 4 Cake type can be changed by name or menu number")
    void updateCakeType() {
        assertEquals("Vanilla", updateAndGet(4, "vanilla").getCakeType());
        assertEquals("Carrot", updateAndGet(4, "4").getCakeType());
    }

    /**
     * Positive test: 5 Quantity can be changed, and the total follows.
     */
    @Test
    @DisplayName("PASS: 5 Quantity can be changed, and the total follows")
    void updateQuantity() {
        Order after = updateAndGet(5, "4");
        assertEquals(4, after.getQuantity());
        assertEquals(107.00, manager.calculateTotal(after), 0.001); // 4 x $25 + 7%
    }

    /**
     * Positive test: 6 Unit price can be changed (with or without $).
     */
    @Test
    @DisplayName("PASS: 6 Unit price can be changed (with or without $)")
    void updatePrice() {
        assertEquals(30.00, updateAndGet(6, "30").getUnitPrice(), 0.001);
        assertEquals(27.50, updateAndGet(6, "$27.50").getUnitPrice(), 0.001);
    }

    /**
     * Positive test: 7 Pickup date can be changed (YYYY-MM-DD or MM/DD/YYYY).
     */
    @Test
    @DisplayName("PASS: 7 Pickup date can be changed (YYYY-MM-DD or MM/DD/YYYY)")
    void updatePickupDate() {
        assertEquals(LocalDate.of(2027, 2, 14), updateAndGet(7, "2027-02-14").getPickupDate());
        assertEquals(LocalDate.of(2027, 3, 1), updateAndGet(7, "3/1/2027").getPickupDate());
    }

    /**
     * Positive test: 8 Picked up can be changed to yes and back to no.
     */
    @Test
    @DisplayName("PASS: 8 Picked up can be changed to yes and back to no")
    void updatePickedUp() {
        assertTrue(updateAndGet(8, "y").isPickedUp());
        assertFalse(updateAndGet(8, "No").isPickedUp());
    }

    /**
     * Positive test: 9 Special instructions can be changed and cleared.
     */
    @Test
    @DisplayName("PASS: 9 Special instructions can be changed and cleared")
    void updateInstructions() {
        assertEquals("Write Be Mine, in red", updateAndGet(9, "Write Be Mine, in red").getSpecialInstructions());
        assertEquals("", updateAndGet(9, "").getSpecialInstructions());
    }

    /**
     * Positive test: updating one field leaves all other fields unchanged.
     */
    @Test
    @DisplayName("PASS: updating one field leaves all other fields unchanged")
    void otherFieldsUnchanged() {
        Order after = updateAndGet(5, "4");
        assertEquals(original.getOrderId(), after.getOrderId());
        assertEquals(original.getCustomerName(), after.getCustomerName());
        assertEquals(original.getPhoneNumber(), after.getPhoneNumber());
        assertEquals(original.getCakeType(), after.getCakeType());
        assertEquals(original.getUnitPrice(), after.getUnitPrice(), 0.001);
        assertEquals(original.getPickupDate(), after.getPickupDate());
        assertEquals(original.isPickedUp(), after.isPickedUp());
    }

    // ---------------- NEGATIVE tests: bad input for every attribute ----------------

    /**
     * Negative test: 1 Order ID - letters, out of range, or already used.
     */
    @Test
    @DisplayName("FAIL CASE: 1 Order ID - letters, out of range, or already used")
    void badOrderId() {
        assertUpdateRejected(1, "abc");
        assertUpdateRejected(1, "999");
        assertUpdateRejected(1, "1003"); // belongs to another order
        assertUpdateRejected(1, "99999999999"); // too big for an int
    }

    /**
     * Negative test: 2 Customer name - blank, one letter, or symbols.
     */
    @Test
    @DisplayName("FAIL CASE: 2 Customer name - blank, one letter, or symbols")
    void badName() {
        assertUpdateRejected(2, "");
        assertUpdateRejected(2, "J");
        assertUpdateRejected(2, "!!");
    }

    /**
     * Negative test: 3 Phone - too short or words.
     */
    @Test
    @DisplayName("FAIL CASE: 3 Phone - too short or words")
    void badPhone() {
        assertUpdateRejected(3, "555-1234");
        assertUpdateRejected(3, "call me");
    }

    /**
     * Negative test: 4 Cake type - not sold or menu number out of range.
     */
    @Test
    @DisplayName("FAIL CASE: 4 Cake type - not sold or menu number out of range")
    void badCakeType() {
        assertUpdateRejected(4, "Lemon");
        assertUpdateRejected(4, "7");
    }

    /**
     * Negative test: 5 Quantity - word, decimal, 0 or 100.
     */
    @Test
    @DisplayName("FAIL CASE: 5 Quantity - word, decimal, 0 or 100")
    void badQuantity() {
        assertUpdateRejected(5, "two");
        assertUpdateRejected(5, "1.5");
        assertUpdateRejected(5, "0");
        assertUpdateRejected(5, "100");
    }

    /**
     * Negative test: 6 Unit price - word, NaN, negative, zero or 3 decimals.
     */
    @Test
    @DisplayName("FAIL CASE: 6 Unit price - word, NaN, negative, zero or 3 decimals")
    void badPrice() {
        assertUpdateRejected(6, "abc");
        assertUpdateRejected(6, "NaN");
        assertUpdateRejected(6, "-5");
        assertUpdateRejected(6, "0");
        assertUpdateRejected(6, "28.999");
    }

    /**
     * Negative test: 7 Pickup date - not a date, impossible date, or in the past.
     */
    @Test
    @DisplayName("FAIL CASE: 7 Pickup date - not a date, impossible date, or in the past")
    void badPickupDate() {
        assertUpdateRejected(7, "tomorrow");
        assertUpdateRejected(7, "2027-02-30");
        assertUpdateRejected(7, "2020-01-01");
    }

    /**
     * Negative test: 8 Picked up - anything other than yes/no.
     */
    @Test
    @DisplayName("FAIL CASE: 8 Picked up - anything other than yes/no")
    void badPickedUp() {
        assertUpdateRejected(8, "maybe");
        assertUpdateRejected(8, "");
    }

    /**
     * Negative test: 9 Special instructions - over 100 characters.
     */
    @Test
    @DisplayName("FAIL CASE: 9 Special instructions - over 100 characters")
    void badInstructions() {
        assertUpdateRejected(9, "x".repeat(101));
    }

    /**
     * Negative test: an order ID that doesn't exist, or field number 0 or 10.
     */
    @Test
    @DisplayName("FAIL CASE: an order ID that doesn't exist, or field number 0 or 10")
    void badOrderOrField() {
        assertFalse(manager.updateField(9999, 2, "Someone").isEmpty());
        assertUpdateRejected(0, "anything");
        assertUpdateRejected(10, "anything");
    }
}
