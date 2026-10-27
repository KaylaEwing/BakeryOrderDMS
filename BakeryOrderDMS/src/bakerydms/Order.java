/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026 (Phase 1); updated October 26, 2026 (Phase 2)
 * Class:   Order
 *
 * Purpose: Represents one custom cake order placed at the bakery. This is the
 *          data object that the Bakery Order Management System (DMS) stores
 *          and manages. It holds nine attributes of five different data types
 *          (int, String, double, LocalDate and boolean).
 *
 *          The Order class only stores data. It does not check whether the
 *          data is valid; that is the job of OrderValidator, and the
 *          OrderManager makes sure only valid orders are ever stored.
 */
package bakerydms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * A single custom cake order.
 */
public class Order {

    /** Unique 4-digit number that identifies the order (1000-9999). */
    private int orderId;

    /** Full name of the customer who placed the order. */
    private String customerName;

    /** 10-digit callback number, stored as text in the format 407-555-0142. */
    private String phoneNumber;

    /** Type of cake: Chocolate, Vanilla, Red Velvet or Carrot. */
    private String cakeType;

    /** Number of cakes ordered (1-50). */
    private int quantity;

    /** Price of one cake in U.S. dollars. */
    private double unitPrice;

    /** The day the customer will collect the order. */
    private LocalDate pickupDate;

    /** True once the customer has collected the order. */
    private boolean pickedUp;

    /** Optional notes such as writing on the cake or allergy information. */
    private String specialInstructions;

    /**
     * Creates a new order with every attribute set.
     *
     * @param orderId             unique 4-digit order number
     * @param customerName        customer's full name
     * @param phoneNumber         customer's phone number
     * @param cakeType            type of cake ordered
     * @param quantity            number of cakes
     * @param unitPrice           price of one cake in dollars
     * @param pickupDate          date the order will be collected
     * @param pickedUp            true if the order has already been collected
     * @param specialInstructions notes for the baker (may be empty)
     */
    public Order(int orderId, String customerName, String phoneNumber, String cakeType,
                 int quantity, double unitPrice, LocalDate pickupDate, boolean pickedUp,
                 String specialInstructions) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.cakeType = cakeType;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.pickupDate = pickupDate;
        this.pickedUp = pickedUp;
        // Store "no instructions" as an empty string instead of null so it can always be printed safely.
        this.specialInstructions = (specialInstructions == null) ? "" : specialInstructions;
    }

    /**
     * Makes an independent copy of this order. The OrderManager hands out
     * copies so that code outside the manager can never change a stored order
     * without going through validation first.
     *
     * @return a new Order with the same values as this one
     */
    public Order copy() {
        return new Order(orderId, customerName, phoneNumber, cakeType, quantity,
                unitPrice, pickupDate, pickedUp, specialInstructions);
    }

    /**
     * Calculates the cost of the order before tax (quantity x unit price),
     * rounded to the nearest cent.
     *
     * @return the order subtotal in dollars
     */
    public double calculateSubtotal() {
        // BigDecimal avoids floating-point rounding errors with money.
        return BigDecimal.valueOf(unitPrice)
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Describes whether the order has been collected yet.
     *
     * @return "Picked up" or "Waiting"
     */
    public String getStatus() {
        return pickedUp ? "Picked up" : "Waiting";
    }

    // ---------------------------------------------------------------
    // Getters: return the value of each attribute.
    // ---------------------------------------------------------------

    /** @return the unique order number */
    public int getOrderId() {
        return orderId;
    }

    /** @return the customer's full name */
    public String getCustomerName() {
        return customerName;
    }

    /** @return the customer's phone number */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /** @return the type of cake ordered */
    public String getCakeType() {
        return cakeType;
    }

    /** @return the number of cakes ordered */
    public int getQuantity() {
        return quantity;
    }

    /** @return the price of one cake in dollars */
    public double getUnitPrice() {
        return unitPrice;
    }

    /** @return the date the order will be collected */
    public LocalDate getPickupDate() {
        return pickupDate;
    }

    /** @return true if the customer has collected the order */
    public boolean isPickedUp() {
        return pickedUp;
    }

    /** @return the special instructions, or an empty string if there are none */
    public String getSpecialInstructions() {
        return specialInstructions;
    }

    // ---------------------------------------------------------------
    // Setters: change the value of each attribute. These are the only
    // void methods in the program (besides main), as planned in the UML.
    // ---------------------------------------------------------------

    /** @param orderId the new unique order number */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /** @param customerName the new customer name */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /** @param phoneNumber the new phone number */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /** @param cakeType the new cake type */
    public void setCakeType(String cakeType) {
        this.cakeType = cakeType;
    }

    /** @param quantity the new number of cakes */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /** @param unitPrice the new price of one cake */
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    /** @param pickupDate the new pickup date */
    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    /** @param pickedUp true if the order has been collected */
    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    /** @param specialInstructions the new notes for the baker (null is stored as empty) */
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = (specialInstructions == null) ? "" : specialInstructions;
    }

    /**
     * Builds a one-line, human-readable summary of the order.
     *
     * @return the order as a single line of text
     */
    @Override
    public String toString() {
        return String.format("Order #%d | %s | %s | %d x %s @ $%.2f | Pickup %s | %s | Notes: %s",
                orderId, customerName, phoneNumber, quantity, cakeType, unitPrice,
                pickupDate, getStatus(), specialInstructions.isEmpty() ? "(none)" : specialInstructions);
    }
}
