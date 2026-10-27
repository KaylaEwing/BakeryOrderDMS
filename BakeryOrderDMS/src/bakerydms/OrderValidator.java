/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026 (Phase 1); updated October 26, 2026 (Phase 2)
 * Class:   OrderValidator
 *
 * Purpose: Holds every data rule for an Order in one place. Both the file
 *          loader and the manual entry screens use these same rules, so bad
 *          data can never get into the system no matter how it is entered.
 *
 *          Rules:
 *            - Order ID: whole number from 1000 to 9999, unique
 *            - Customer name: 2-30 letters, spaces, apostrophes, hyphens, periods
 *            - Phone: exactly 10 digits (dashes, spaces and parentheses allowed)
 *            - Cake type: Chocolate, Vanilla, Red Velvet or Carrot
 *            - Quantity: 1 to 50
 *            - Unit price: more than $0.00, at most $1000.00, max 2 decimals
 *            - Pickup date: a real date from today up to 2 years ahead
 *            - Special instructions: optional, 100 characters or fewer
 */
package bakerydms;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Checks and cleans up Order data.
 */
public class OrderValidator {

    /** The cake types the bakery sells, in the order they are shown to the user. */
    private final List<String> validCakeTypes;

    // Limits for each field. These are instance constants (not static) per course rules.
    private final int minOrderId = 1000;
    private final int maxOrderId = 9999;
    private final int minNameLength = 2;
    private final int maxNameLength = 30;
    private final int minQuantity = 1;
    private final int maxQuantity = 50;
    private final double maxPrice = 1000.00;
    private final int maxInstructionsLength = 100;
    private final int maxYearsAhead = 2;

    /** Accepts dates like 2027-02-13. STRICT rejects impossible dates such as 2027-02-30. */
    private final DateTimeFormatter isoFormat =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Accepts dates like 2/13/2027 or 02/13/2027. */
    private final DateTimeFormatter usFormat =
            DateTimeFormatter.ofPattern("M/d/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Creates a validator for the given list of cake types.
     *
     * @param validCakeTypes the cake types the bakery sells (must not be empty)
     * @throws IllegalArgumentException if the list is null or empty
     */
    public OrderValidator(List<String> validCakeTypes) {
        if (validCakeTypes == null || validCakeTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one cake type is required.");
        }
        this.validCakeTypes = List.copyOf(validCakeTypes);
    }

    /** @return a read-only list of the cake types the bakery sells */
    public List<String> getValidCakeTypes() {
        return validCakeTypes;
    }

    /**
     * Checks that an order ID is in the allowed 4-digit range.
     * (Uniqueness is checked by OrderManager, which knows the stored orders.)
     *
     * @param orderId the ID to check
     * @return true if the ID is from 1000 to 9999
     */
    public boolean isValidOrderId(int orderId) {
        return orderId >= minOrderId && orderId <= maxOrderId;
    }

    /**
     * Checks a customer name. It must start with a letter and contain only
     * letters, spaces, apostrophes, hyphens or periods (e.g. "Mary-Ann O'Neil Jr.").
     *
     * @param name the name to check
     * @return true if the name follows the rules
     */
    public boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= minNameLength
                && trimmed.length() <= maxNameLength
                && trimmed.matches("\\p{L}[\\p{L} .'-]*");
    }

    /**
     * Turns any reasonable phone number into the standard 407-555-0142 format.
     * Spaces, dashes, dots, parentheses and a leading +1 are allowed in the input.
     *
     * @param rawPhone the phone number as typed
     * @return the formatted number, or an empty Optional if it is not 10 digits
     */
    public Optional<String> formatPhone(String rawPhone) {
        if (rawPhone == null) {
            return Optional.empty();
        }
        // Remove the characters people normally type between digits.
        String digits = rawPhone.replaceAll("[\\s().+-]", "");
        if (!digits.matches("\\d+")) {
            return Optional.empty();
        }
        // Allow a U.S. country code in front, e.g. 1-407-555-0142.
        if (digits.length() == 11 && digits.startsWith("1")) {
            digits = digits.substring(1);
        }
        if (digits.length() != 10) {
            return Optional.empty();
        }
        return Optional.of(digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6));
    }

    /**
     * @param phone the phone number to check
     * @return true if the phone number contains exactly 10 digits
     */
    public boolean isValidPhone(String phone) {
        return formatPhone(phone).isPresent();
    }

    /**
     * Finds the official spelling of a cake type, ignoring upper/lower case and
     * extra spaces (so "red  VELVET" becomes "Red Velvet").
     *
     * @param rawCakeType the cake type as typed
     * @return the matching cake type, or an empty Optional if the bakery does not sell it
     */
    public Optional<String> matchCakeType(String rawCakeType) {
        if (rawCakeType == null) {
            return Optional.empty();
        }
        String cleaned = rawCakeType.trim().replaceAll("\\s+", " ");
        for (String type : validCakeTypes) {
            if (type.equalsIgnoreCase(cleaned)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * @param cakeType the cake type to check
     * @return true if the bakery sells this cake type
     */
    public boolean isValidCakeType(String cakeType) {
        return matchCakeType(cakeType).isPresent();
    }

    /**
     * @param quantity the number of cakes
     * @return true if the quantity is from 1 to 50
     */
    public boolean isValidQuantity(int quantity) {
        return quantity >= minQuantity && quantity <= maxQuantity;
    }

    /**
     * Checks a unit price. It must be a real number (not NaN or Infinity),
     * more than zero, at most $1000.00, with no more than 2 decimal places.
     *
     * @param price the price of one cake
     * @return true if the price follows the rules
     */
    public boolean isValidPrice(double price) {
        if (!Double.isFinite(price) || price <= 0 || price > maxPrice) {
            return false;
        }
        // stripTrailingZeros turns 28.50 into 28.5 so only real decimal places are counted.
        return BigDecimal.valueOf(price).stripTrailingZeros().scale() <= 2;
    }

    /**
     * Checks that a pickup date is not too early and not more than 2 years ahead.
     *
     * @param date            the pickup date to check
     * @param earliestAllowed the earliest date accepted (normally today)
     * @param today           today's date, used to work out the 2-year limit
     * @return true if the date is inside the allowed range
     */
    public boolean isValidPickupDate(LocalDate date, LocalDate earliestAllowed, LocalDate today) {
        return date != null
                && !date.isBefore(earliestAllowed)
                && !date.isAfter(today.plusYears(maxYearsAhead));
    }

    /**
     * Checks special instructions. They are optional, but must be 100
     * characters or fewer and contain no hidden control characters.
     *
     * @param instructions the instructions to check (null or blank means none)
     * @return true if the instructions follow the rules
     */
    public boolean isValidInstructions(String instructions) {
        if (instructions == null) {
            return true;
        }
        String trimmed = instructions.trim();
        return trimmed.length() <= maxInstructionsLength
                && trimmed.chars().noneMatch(Character::isISOControl);
    }

    /**
     * Converts text into a date. Accepts YYYY-MM-DD (2027-02-13) or
     * MM/DD/YYYY (2/13/2027). Impossible dates such as 2027-02-30 are rejected.
     *
     * @param rawDate the date as typed
     * @return the date, or an empty Optional if the text is not a real date
     */
    public Optional<LocalDate> parseDate(String rawDate) {
        if (rawDate == null) {
            return Optional.empty();
        }
        String trimmed = rawDate.trim();
        try {
            return Optional.of(LocalDate.parse(trimmed, isoFormat));
        } catch (DateTimeParseException isoProblem) {
            // Not in YYYY-MM-DD form; try MM/DD/YYYY next.
            try {
                return Optional.of(LocalDate.parse(trimmed, usFormat));
            } catch (DateTimeParseException usProblem) {
                return Optional.empty();
            }
        }
    }

    /**
     * Converts text to a whole number. Letters, decimals, blanks and numbers
     * too large for an int are rejected instead of throwing an exception.
     *
     * @param text the text to convert, e.g. "1004"
     * @return the number, or an empty Optional if the text is not a whole number
     */
    public Optional<Integer> parseWholeNumber(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String trimmed = text.trim();
        // At most 10 digits so the check below is the only way parseInt can fail.
        if (!trimmed.matches("[+-]?\\d{1,10}")) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(trimmed));
        } catch (NumberFormatException tooLarge) {
            return Optional.empty(); // e.g. 9999999999 is bigger than an int can hold
        }
    }

    /**
     * Converts text such as "28", "28.50" or "$28.50" to a dollar amount.
     * Words, negative numbers, "NaN", "Infinity" and "1e3" are rejected.
     * (Whether the amount is allowed is checked separately by isValidPrice.)
     *
     * @param text the text to convert
     * @return the amount, or an empty Optional if the text is not a plain dollar amount
     */
    public Optional<Double> parseMoney(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("$")) {
            trimmed = trimmed.substring(1).trim();
        }
        if (!trimmed.matches("\\d{1,7}(\\.\\d+)?")) {
            return Optional.empty();
        }
        return Optional.of(Double.parseDouble(trimmed));
    }

    /**
     * Converts a yes/no answer. Accepts y, yes, true, n, no or false in any case.
     *
     * @param text the text to convert
     * @return true for yes, false for no, or an empty Optional for anything else
     */
    public Optional<Boolean> parseYesNo(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String answer = text.trim().toLowerCase();
        if (answer.equals("y") || answer.equals("yes") || answer.equals("true")) {
            return Optional.of(true);
        }
        if (answer.equals("n") || answer.equals("no") || answer.equals("false")) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    /**
     * Turns the user's cake choice into a cake type. The user may type the
     * menu number shown on screen (1-4) or the cake's name.
     *
     * @param choice the number or name typed
     * @return the cake type, or an empty Optional if the choice is not valid
     */
    public Optional<String> matchCakeChoice(String choice) {
        Optional<Integer> number = parseWholeNumber(choice);
        if (number.isPresent()) {
            int n = number.get();
            return (n >= 1 && n <= validCakeTypes.size())
                    ? Optional.of(validCakeTypes.get(n - 1))
                    : Optional.empty();
        }
        return matchCakeType(choice);
    }

    /**
     * Returns a cleaned-up copy of an order: extra spaces removed, the phone
     * number put in 407-555-0142 form and the cake type spelled correctly.
     * Fields that are invalid are left as they are so validate() can report them.
     *
     * @param order the order to clean up
     * @return a new, cleaned-up Order (the original is not changed)
     */
    public Order normalize(Order order) {
        Order cleaned = order.copy();
        if (cleaned.getCustomerName() != null) {
            cleaned.setCustomerName(cleaned.getCustomerName().trim().replaceAll("\\s+", " "));
        }
        formatPhone(cleaned.getPhoneNumber()).ifPresent(cleaned::setPhoneNumber);
        matchCakeType(cleaned.getCakeType()).ifPresent(cleaned::setCakeType);
        cleaned.setSpecialInstructions(cleaned.getSpecialInstructions().trim());
        return cleaned;
    }

    /**
     * Runs every rule against an order and collects a message for each problem.
     *
     * @param order           the order to check
     * @param earliestAllowed the earliest pickup date accepted
     * @param today           today's date
     * @return a list of problems; an empty list means the order is valid
     */
    public List<String> validate(Order order, LocalDate earliestAllowed, LocalDate today) {
        List<String> errors = new ArrayList<>();
        if (order == null) {
            errors.add("No order was provided.");
            return errors;
        }
        if (!isValidOrderId(order.getOrderId())) {
            errors.add(ruleFor("orderId"));
        }
        if (!isValidName(order.getCustomerName())) {
            errors.add(ruleFor("name"));
        }
        if (!isValidPhone(order.getPhoneNumber())) {
            errors.add(ruleFor("phone"));
        }
        if (!isValidCakeType(order.getCakeType())) {
            errors.add(ruleFor("cakeType"));
        }
        if (!isValidQuantity(order.getQuantity())) {
            errors.add(ruleFor("quantity"));
        }
        if (!isValidPrice(order.getUnitPrice())) {
            errors.add(ruleFor("price"));
        }
        if (!isValidPickupDate(order.getPickupDate(), earliestAllowed, today)) {
            errors.add(ruleFor("date"));
        }
        if (!isValidInstructions(order.getSpecialInstructions())) {
            errors.add(ruleFor("instructions"));
        }
        return errors;
    }

    /**
     * Explains the rule for a field. Used for error messages so the user
     * always knows how to fix what they typed.
     *
     * @param field the field name (orderId, name, phone, cakeType, quantity, price, date, instructions)
     * @return a plain-English description of the rule
     */
    public String ruleFor(String field) {
        switch (field) {
            case "orderId":
                return "Order ID must be a 4-digit number from " + minOrderId + " to " + maxOrderId + ".";
            case "name":
                return "Customer name must be " + minNameLength + "-" + maxNameLength
                        + " characters, start with a letter, and use only letters, spaces, ' - or .";
            case "phone":
                return "Phone number must have exactly 10 digits, e.g. 407-555-0142.";
            case "cakeType":
                return "Cake type must be one of: " + String.join(", ", validCakeTypes) + ".";
            case "quantity":
                return "Quantity must be a whole number from " + minQuantity + " to " + maxQuantity + ".";
            case "price":
                return String.format("Unit price must be more than $0.00 and at most $%.2f, with no more than 2 decimal places.", maxPrice);
            case "date":
                return "Pickup date must be a real date from today up to " + maxYearsAhead
                        + " years ahead (YYYY-MM-DD or MM/DD/YYYY).";
            case "instructions":
                return "Special instructions must be " + maxInstructionsLength + " characters or fewer.";
            default:
                return "Invalid value.";
        }
    }
}
