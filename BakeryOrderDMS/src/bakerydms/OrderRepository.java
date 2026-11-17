/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 13, 2026 (Phase 4); documented November 16, 2026 (Module 11)
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
 * Describes what the program needs from its data storage: find, add, change
 * and delete orders.
 *
 * <p>This is the interface planned in the Module 5 implementation plan. It says
 * <i>what</i> storage must do but not <i>how</i>, so {@link OrderManager} works
 * with any storage without changing its business logic:
 * <ul>
 *   <li>{@link MySqlOrderRepository} stores orders in a MySQL database (the real program).</li>
 *   <li>{@code InMemoryOrderRepository}, in the test folder, keeps them in a list
 *       so the unit tests run without a MySQL server.</li>
 * </ul>
 * Any method may throw {@link DataAccessException} if the storage cannot be
 * reached. Its message explains the problem in plain English.
 *
 *
 * @author Kayla Ewing
 * @version 4.0 (Phase 4, Module 11)
 */
public interface OrderRepository {

    /**
     * Reads every stored order.
     *
     * @return every stored order, sorted by order ID
     * @throws DataAccessException if the storage cannot be reached
     */
    List<Order> findAll();

    /**
     * Finds one order by its order number.
     *
     * @param orderId the order number to look for
     * @return the order, or an empty Optional if no order has that ID
     * @throws DataAccessException if the storage cannot be reached
     */
    Optional<Order> findById(int orderId);

    /**
     * Gets the orders the Daily Production Report needs: due on the date
     * and not yet picked up.
     *
     * @param pickupDate the pickup date
     * @return the open orders due that day
     * @throws DataAccessException if the storage cannot be reached
     */
    List<Order> findOpenOrdersDueOn(LocalDate pickupDate);

    /**
     * Stores a new order.
     *
     * @param order an order that has already been validated
     * @return true if one order was stored
     * @throws DataAccessException if the storage cannot be reached or refuses the order
     */
    boolean insert(Order order);

    /**
     * Replaces a stored order (any field may change, including the ID).
     *
     * @param orderId the ID the order has now
     * @param order   the order's new values, already validated
     * @return true if one order was changed
     * @throws DataAccessException if the storage cannot be reached or refuses the change
     */
    boolean update(int orderId, Order order);

    /**
     * Removes one order.
     *
     * @param orderId the order to remove
     * @return true if an order was removed, false if no order had that ID
     * @throws DataAccessException if the storage cannot be reached
     */
    boolean delete(int orderId);

    /**
     * Counts the stored orders.
     *
     * @return how many orders are stored
     * @throws DataAccessException if the storage cannot be reached
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
