/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026 (Phase 1); updated October 26, 2026 (Phase 2)
 * Class:   DailyReport
 *
 * Purpose: Holds the result of the custom action, the Daily Production
 *          Report. For one pickup date it stores how many cakes of each type
 *          must be baked, how many orders are due, and the expected revenue
 *          (subtotal + sales tax). The numbers are calculated by
 *          OrderManager.generateDailyReport(); this class only stores and
 *          formats them. It has no setters, so a report can never be changed
 *          after it is created.
 */
package bakerydms;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The calculated result of the Daily Production Report.
 */
public class DailyReport {

    /** The pickup date the report covers. */
    private final LocalDate reportDate;

    /** Number of cakes to bake for each cake type, in menu order. */
    private final Map<String, Integer> cakeCounts;

    /** Number of open (not picked up) orders due on the report date. */
    private final int orderCount;

    /** Total cost of those orders before tax. */
    private final double subtotal;

    /** Sales tax on the subtotal. */
    private final double tax;

    /** Subtotal plus tax: the money the bakery expects to take in that day. */
    private final double expectedRevenue;

    /** Sales tax rate used, e.g. 0.07 for 7%. */
    private final double taxRate;

    /**
     * Creates a finished report.
     *
     * @param reportDate      the pickup date the report covers
     * @param cakeCounts      cakes to bake for each cake type
     * @param orderCount      number of open orders due that day
     * @param subtotal        total before tax
     * @param tax             sales tax amount
     * @param expectedRevenue subtotal plus tax
     * @param taxRate         the tax rate used (0.07 = 7%)
     */
    public DailyReport(LocalDate reportDate, Map<String, Integer> cakeCounts, int orderCount,
                       double subtotal, double tax, double expectedRevenue, double taxRate) {
        this.reportDate = reportDate;
        // Copy the map so later changes to the original cannot change this report.
        this.cakeCounts = Collections.unmodifiableMap(new LinkedHashMap<>(cakeCounts));
        this.orderCount = orderCount;
        this.subtotal = subtotal;
        this.tax = tax;
        this.expectedRevenue = expectedRevenue;
        this.taxRate = taxRate;
    }

    /** @return the pickup date the report covers */
    public LocalDate getReportDate() {
        return reportDate;
    }

    /** @return a read-only map of cake type to number of cakes */
    public Map<String, Integer> getCakeCounts() {
        return cakeCounts;
    }

    /** @return the number of open orders due on the report date */
    public int getOrderCount() {
        return orderCount;
    }

    /** @return the total before tax */
    public double getSubtotal() {
        return subtotal;
    }

    /** @return the sales tax amount */
    public double getTax() {
        return tax;
    }

    /** @return the subtotal plus tax */
    public double getExpectedRevenue() {
        return expectedRevenue;
    }

    /**
     * Adds up the cakes of every type.
     *
     * @return the total number of cakes to bake
     */
    public int getTotalCakes() {
        int total = 0;
        for (int count : cakeCounts.values()) {
            total += count;
        }
        return total;
    }

    /**
     * @return true if at least one open order is due on the report date
     */
    public boolean hasOrders() {
        return orderCount > 0;
    }

    /**
     * Builds the printable report shown to the user.
     *
     * @return the report as multi-line text
     */
    public String format() {
        String dayName = reportDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
        StringBuilder sb = new StringBuilder();
        String line = "=".repeat(46);
        sb.append(line).append(System.lineSeparator());
        sb.append("          DAILY PRODUCTION REPORT").append(System.lineSeparator());
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format("Pickup date: %s (%s)%n", reportDate, dayName));

        // Nothing due: say so clearly instead of printing a table of zeros.
        if (!hasOrders()) {
            sb.append("No open orders are due on this date. Nothing to bake.").append(System.lineSeparator());
            sb.append(line);
            return sb.toString();
        }

        sb.append(String.format("Open orders due: %d%n%n", orderCount));
        sb.append("Cakes to bake:").append(System.lineSeparator());
        for (Map.Entry<String, Integer> entry : cakeCounts.entrySet()) {
            sb.append(String.format("   %-14s %4d%n", entry.getKey(), entry.getValue()));
        }
        sb.append("   ").append("-".repeat(19)).append(System.lineSeparator());
        sb.append(String.format("   %-14s %4d%n%n", "Total cakes", getTotalCakes()));
        // Labels are padded to the same width so the dollar amounts line up.
        String taxLabel = String.format("Sales tax (%.0f%%):", taxRate * 100);
        sb.append(String.format("%-20s %11s%n", "Subtotal:", money(subtotal)));
        sb.append(String.format("%-20s %11s%n", taxLabel, money(tax)));
        sb.append(String.format("%-20s %11s%n", "Expected revenue:", money(expectedRevenue)));
        sb.append(line);
        return sb.toString();
    }

    /**
     * Formats an amount as dollars, e.g. 1234.5 becomes "$1,234.50".
     *
     * @param amount the amount in dollars
     * @return the amount as text with a dollar sign
     */
    private String money(double amount) {
        return String.format("$%,.2f", amount);
    }
}
