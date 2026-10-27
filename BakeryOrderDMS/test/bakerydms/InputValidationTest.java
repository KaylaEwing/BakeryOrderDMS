/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 26, 2026 (Phase 2)
 * Class:   InputValidationTest
 *
 * Purpose: Unit tests for the methods that turn what the user types into
 *          numbers, prices, dates and yes/no answers. These are the checks
 *          that stop a user from crashing the program with bad input, so
 *          each one is tested with good input (positive) and bad input
 *          (negative). Bad input must return "empty", never throw.
 */
package bakerydms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("6. Input validation (user can't crash the program)")
class InputValidationTest {

    /** The validator being tested. */
    private final OrderValidator validator = new TestHelper().newValidator();

    @Test
    @DisplayName("PASS: whole numbers are read correctly")
    void readsWholeNumbers() {
        assertEquals(1004, validator.parseWholeNumber("1004").orElseThrow());
        assertEquals(7, validator.parseWholeNumber("  7 ").orElseThrow());
    }

    @Test
    @DisplayName("FAIL CASE: words, decimals, blanks and huge numbers are not whole numbers")
    void rejectsBadWholeNumbers() {
        assertTrue(validator.parseWholeNumber("abc").isEmpty());
        assertTrue(validator.parseWholeNumber("1.5").isEmpty());
        assertTrue(validator.parseWholeNumber("").isEmpty());
        assertTrue(validator.parseWholeNumber(null).isEmpty());
        assertTrue(validator.parseWholeNumber("99999999999").isEmpty());
    }

    @Test
    @DisplayName("PASS: dollar amounts are read with or without $")
    void readsMoney() {
        assertEquals(28.0, validator.parseMoney("28").orElseThrow(), 0.001);
        assertEquals(28.5, validator.parseMoney("$28.50").orElseThrow(), 0.001);
    }

    @Test
    @DisplayName("FAIL CASE: NaN, Infinity, 1e3, negatives and words are not dollar amounts")
    void rejectsBadMoney() {
        assertTrue(validator.parseMoney("NaN").isEmpty());
        assertTrue(validator.parseMoney("Infinity").isEmpty());
        assertTrue(validator.parseMoney("1e3").isEmpty());
        assertTrue(validator.parseMoney("-5").isEmpty());
        assertTrue(validator.parseMoney("twenty").isEmpty());
    }

    @Test
    @DisplayName("PASS: dates are read in YYYY-MM-DD and MM/DD/YYYY form")
    void readsDates() {
        assertEquals(LocalDate.of(2027, 2, 13), validator.parseDate("2027-02-13").orElseThrow());
        assertEquals(LocalDate.of(2027, 2, 13), validator.parseDate("2/13/2027").orElseThrow());
    }

    @Test
    @DisplayName("FAIL CASE: impossible or badly written dates are rejected")
    void rejectsBadDates() {
        assertTrue(validator.parseDate("2027-02-30").isEmpty());
        assertTrue(validator.parseDate("Feb 13").isEmpty());
        assertTrue(validator.parseDate("13/13/2027").isEmpty());
        assertTrue(validator.parseDate("").isEmpty());
    }

    @Test
    @DisplayName("PASS: yes/no answers are read in any case")
    void readsYesNo() {
        assertTrue(validator.parseYesNo("Y").orElseThrow());
        assertTrue(validator.parseYesNo("yes").orElseThrow());
        assertFalse(validator.parseYesNo("No").orElseThrow());
        assertFalse(validator.parseYesNo("false").orElseThrow());
    }

    @Test
    @DisplayName("FAIL CASE: anything other than yes/no is rejected")
    void rejectsBadYesNo() {
        assertTrue(validator.parseYesNo("maybe").isEmpty());
        assertTrue(validator.parseYesNo("").isEmpty());
    }

    @Test
    @DisplayName("PASS/FAIL: phone numbers must have exactly 10 digits")
    void phoneRules() {
        assertEquals("407-555-0142", validator.formatPhone("(407) 555-0142").orElseThrow());
        assertEquals("407-555-0142", validator.formatPhone("+1 407 555 0142").orElseThrow());
        assertTrue(validator.formatPhone("555-0142").isEmpty());
        assertTrue(validator.formatPhone("407-555-01AB").isEmpty());
    }

    @Test
    @DisplayName("PASS/FAIL: cake choice accepts menu numbers 1-4 or names only")
    void cakeChoiceRules() {
        assertEquals("Red Velvet", validator.matchCakeChoice("3").orElseThrow());
        assertEquals("Red Velvet", validator.matchCakeChoice("red  velvet").orElseThrow());
        assertTrue(validator.matchCakeChoice("0").isEmpty());
        assertTrue(validator.matchCakeChoice("5").isEmpty());
        assertTrue(validator.matchCakeChoice("Lemon").isEmpty());
    }
}
