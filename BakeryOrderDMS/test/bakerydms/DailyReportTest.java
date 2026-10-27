/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 26, 2026 (Phase 2)
 * Class:   DailyReportTest
 *
 * Purpose: Unit tests for the CUSTOM ACTION, the Daily Production Report.
 *          Positive tests check the calculation: cakes per type, number of
 *          orders, subtotal, 7% tax and expected revenue. Negative tests
 *          prove picked-up orders and other days are left out, a day with
 *          no orders reports zero, and a missing date is rejected.
 *
 *          Expected numbers for 2027-02-13 with the sample orders:
 *            Chocolate 3 (1001 x2 + 1004 x1), Vanilla 1, Red Velvet 3, Carrot 0
 *            Subtotal = 3 x $25 + 1 x $22 + 3 x $28 = $181.00
 *            Tax      = $181.00 x 7% = $12.67
 *            Revenue  = $193.67
 */
package bakerydms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("5. Custom action: Daily Production Report")
class DailyReportTest {

    /** Creates the manager and sample data. */
    private final TestHelper helper = new TestHelper();

    /** A fresh manager holding the six sample orders. */
    private OrderManager manager;

    /** The report date used by most tests (2027-02-13). */
    private LocalDate reportDate;

    /** Starts every test with the same six sample orders. */
    @BeforeEach
    void setUp() {
        manager = helper.managerWithSampleOrders();
        reportDate = helper.getReportDate();
    }

    // ---------------- POSITIVE (affirmative) tests ----------------

    @Test
    @DisplayName("PASS: counts the cakes of each type due that day")
    void countsCakesPerType() {
        DailyReport report = manager.generateDailyReport(reportDate);

        assertEquals(3, report.getCakeCounts().get("Chocolate"));
        assertEquals(1, report.getCakeCounts().get("Vanilla"));
        assertEquals(3, report.getCakeCounts().get("Red Velvet"));
        assertEquals(0, report.getCakeCounts().get("Carrot"));
        assertEquals(7, report.getTotalCakes());
        assertEquals(4, report.getOrderCount());
    }

    @Test
    @DisplayName("PASS: calculates subtotal $181.00, tax $12.67, revenue $193.67")
    void calculatesRevenue() {
        DailyReport report = manager.generateDailyReport(reportDate);

        assertEquals(181.00, report.getSubtotal(), 0.001);
        assertEquals(12.67, report.getTax(), 0.001);
        assertEquals(193.67, report.getExpectedRevenue(), 0.001);
    }

    @Test
    @DisplayName("PASS: the printed report shows the calculated totals")
    void formattedReportShowsTotals() {
        String text = manager.generateDailyReport(reportDate).format();
        assertTrue(text.contains("$193.67"), text);
        assertTrue(text.contains("Open orders due: 4"), text);
    }

    @Test
    @DisplayName("PASS: the report changes when an order is updated (marked picked up)")
    void reportReflectsUpdates() {
        manager.updateField(1001, 8, "yes"); // 2 chocolate cakes collected

        DailyReport report = manager.generateDailyReport(reportDate);
        assertEquals(1, report.getCakeCounts().get("Chocolate"));
        assertEquals(3, report.getOrderCount());
        assertEquals(140.17, report.getExpectedRevenue(), 0.001); // ($181 - $50) = $131 + $9.17 tax
    }

    @Test
    @DisplayName("PASS: the report changes when an order is added")
    void reportReflectsNewOrder() {
        manager.addOrder(1026, "Dana Lee", "407-555-9876", "Red Velvet", 2, 28.00, reportDate, "");

        DailyReport report = manager.generateDailyReport(reportDate);
        assertEquals(5, report.getCakeCounts().get("Red Velvet"));
        assertEquals(253.59, report.getExpectedRevenue(), 0.001); // ($181 + $56) x 1.07
    }

    // ---------------- NEGATIVE tests ----------------

    @Test
    @DisplayName("FAIL CASE: a picked-up order (1005, Carrot) is NOT counted")
    void pickedUpOrdersExcluded() {
        DailyReport report = manager.generateDailyReport(reportDate);
        assertEquals(0, report.getCakeCounts().get("Carrot"),
                "Order 1005 was already picked up and must not be baked again");
    }

    @Test
    @DisplayName("FAIL CASE: an order for a different day (1006) is NOT counted")
    void otherDaysExcluded() {
        DailyReport nextDay = manager.generateDailyReport(reportDate.plusDays(1));
        assertEquals(1, nextDay.getOrderCount());
        assertEquals(1, nextDay.getCakeCounts().get("Red Velvet"));
        assertEquals(29.96, nextDay.getExpectedRevenue(), 0.001);
    }

    @Test
    @DisplayName("FAIL CASE: a day with no orders reports zero instead of crashing")
    void emptyDayReportsZero() {
        DailyReport report = manager.generateDailyReport(LocalDate.of(2027, 12, 25));

        assertFalse(report.hasOrders());
        assertEquals(0, report.getTotalCakes());
        assertEquals(0.00, report.getExpectedRevenue(), 0.001);
        assertTrue(report.format().contains("No open orders"));
    }

    @Test
    @DisplayName("FAIL CASE: a report on an empty system reports zero")
    void emptySystemReportsZero() {
        DailyReport report = helper.newManager().generateDailyReport(reportDate);
        assertEquals(0, report.getOrderCount());
        assertEquals(4, report.getCakeCounts().size(), "All four cake types are still listed");
    }

    @Test
    @DisplayName("FAIL CASE: a missing (null) date is rejected with an error")
    void nullDateRejected() {
        assertThrows(IllegalArgumentException.class, () -> manager.generateDailyReport(null));
    }
}
