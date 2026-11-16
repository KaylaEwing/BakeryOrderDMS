/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 13, 2026 (Phase 4)
 * Class:   OrderRepository (interface)
 *
 * Purpose: Describes WHAT the program needs from its data storage (find,
 *          add, change and delete orders) without saying HOW it is stored.
 *          This is the interface planned in the Module 5 implementation
 *          plan. OrderManager only talks to this interface, so the storage
 *          can be swapped without changing any business logic:
 *            - MySqlOrderRepository stores orders in a MySQL database (Phase 4)
 *            - the unit tests use a simple in-memory version
 *
 *          Every method may throw DataAccessException if the storage cannot
 *          be reached; the message explains the problem in plain English.
 */
package bakerydms;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Storage for orders.
 */
public interface OrderRepository {

    /**
     * @return every stored order, sorted by order ID
     */
    List<Order> findAll();

    /**
     * @param orderId the order number to look for
     * @return the order, or an empty Optional if no order has that ID
     */
    Optional<Order> findById(int orderId);

    /**
     * Gets the orders the Daily Production Report needs: due on the date
     * and not yet picked up.
     *
     * @param pickupDate the pickup date
     * @return the open orders due that day
     */
    List<Order> findOpenOrdersDueOn(LocalDate pickupDate);

    /**
     * Stores a new order.
     *
     * @param order an order that has already been validated
     * @return true if one order was stored
     */
    boolean insert(Order order);

    /**
     * Replaces a stored order (any field may change, including the ID).
     *
     * @param orderId the ID the order has now
     * @param order   the order's new values, already validated
     * @return true if one order was changed
     */
    boolean update(int orderId, Order order);

    /**
     * @param orderId the order to remove
     * @return true if an order was removed, false if no order had that ID
     */
    boolean delete(int orderId);

    /**
     * @return how many orders are stored
     */
    int count();

    /**
     * Describes where the data is stored, for status messages.
     *
     * @return for example "bakery_dms on localhost:3306"
     */
    String describe();

    /**
     * Releases the storage (for a database: closes the connection).
     *
     * @return true if it closed cleanly
     */
    boolean close();
}
