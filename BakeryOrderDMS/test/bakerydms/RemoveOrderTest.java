/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 26, 2026 (Phase 2); documented November 16, 2026 (Module 11)
 * Class:   RemoveOrderTest
 *
 * Purpose: Unit tests for DELETE. The positive test proves the chosen order
 *          is removed and can no longer be found, while every other order
 *          stays. Negative tests prove that removing an order that does not
 *          exist returns false and changes nothing.
 */
package bakerydms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DELETE. The positive test proves the chosen order is
 * removed and can no longer be found, while every other order stays.
 * Negative tests prove that removing an order that does not exist returns
 * false and changes nothing.
 *
 * @author Kayla Ewing
 * @version 4.0 (Phase 4, Module 11)
 */
@DisplayName("3. Removing orders")
class RemoveOrderTest {

    /**
     * Creates the test class. JUnit makes a new one for every test, so
     * no test can affect another.
     */
    RemoveOrderTest() {
        // the fields are set up in their declarations and in setUp()
    }

    /** Creates the manager and sample data. */
    private final TestHelper helper = new TestHelper();

    /** A fresh manager holding the six sample orders. */
    private OrderManager manager;

    /** Starts every test with the same six sample orders. */
    @BeforeEach
    void setUp() {
        manager = helper.managerWithSampleOrders();
    }

    // ---------------- POSITIVE (affirmative) tests ----------------

    /**
     * Positive test: an existing order is removed and no longer exists.
     */
    @Test
    @DisplayName("PASS: an existing order is removed and no longer exists")
    void existingOrderIsRemoved() {
        assertTrue(manager.findOrderById(1002).isPresent(), "Order 1002 should exist before the test");

        boolean removed = manager.removeOrder(1002);

        assertTrue(removed);
        assertTrue(manager.findOrderById(1002).isEmpty(), "Order 1002 should be gone");
        assertEquals(5, manager.getOrderCount());
        assertTrue(manager.getAllOrders().stream().noneMatch(o -> o.getOrderId() == 1002),
                "Order 1002 must not appear when all orders are displayed");
    }

    /**
     * Positive test: removing one order leaves every other order unchanged.
     */
    @Test
    @DisplayName("PASS: removing one order leaves every other order unchanged")
    void otherOrdersAreKept() {
        manager.removeOrder(1002);

        assertTrue(manager.findOrderById(1001).isPresent());
        assertTrue(manager.findOrderById(1003).isPresent());
        assertEquals("Red Velvet", manager.findOrderById(1003).orElseThrow().getCakeType());
    }

    /**
     * Positive test: a removed order's ID can be used again for a new order.
     */
    @Test
    @DisplayName("PASS: a removed order's ID can be used again for a new order")
    void removedIdCanBeReused() {
        manager.removeOrder(1002);
        assertTrue(manager.addOrder(1002, "New Customer", "407-555-0000", "Carrot", 1, 24,
                helper.getReportDate(), "").isEmpty());
    }

    // ---------------- NEGATIVE tests ----------------

    /**
     * Negative test: removing an ID that doesn't exist returns false and changes nothing.
     */
    @Test
    @DisplayName("FAIL CASE: removing an ID that doesn't exist returns false and changes nothing")
    void missingOrderIsNotRemoved() {
        assertFalse(manager.removeOrder(9999));
        assertEquals(6, manager.getOrderCount());
    }

    /**
     * Negative test: removing the same order twice fails the second time.
     */
    @Test
    @DisplayName("FAIL CASE: removing the same order twice fails the second time")
    void cannotRemoveTwice() {
        assertTrue(manager.removeOrder(1002));
        assertFalse(manager.removeOrder(1002));
        assertEquals(5, manager.getOrderCount());
    }

    /**
     * Negative test: removing from an empty system returns false.
     */
    @Test
    @DisplayName("FAIL CASE: removing from an empty system returns false")
    void removeFromEmptySystem() {
        OrderManager empty = helper.newManager();
        assertFalse(empty.removeOrder(1001));
        assertEquals(0, empty.getOrderCount());
    }
}
