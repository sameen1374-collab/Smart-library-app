package com.example.librarymanagementsystem;

/**
 * Central place for all membership-related rules.
 * Change numbers here and they apply everywhere (Issue, Fine, Renew, etc.)
 */
public class Membership {

    public static final String BASIC = "Basic";
    public static final String PREMIUM = "Premium";

    // How many days a book can be kept before it's due
    public static final int BASIC_LOAN_DAYS = 14;
    public static final int PREMIUM_LOAN_DAYS = 21;

    // How many books a user can have issued at the same time
    public static final int BASIC_BOOK_LIMIT = 2;
    public static final int PREMIUM_BOOK_LIMIT = 5;

    // Fine per day late (same for both tiers, as decided)
    public static final double FINE_PER_DAY = 10.0;

    // How many days a membership lasts before it needs renewal
    public static final int MEMBERSHIP_VALIDITY_DAYS = 365;

    public static int getLoanDays(String membershipType) {
        if (PREMIUM.equalsIgnoreCase(membershipType)) {
            return PREMIUM_LOAN_DAYS;
        }
        return BASIC_LOAN_DAYS;
    }

    public static int getBookLimit(String membershipType) {
        if (PREMIUM.equalsIgnoreCase(membershipType)) {
            return PREMIUM_BOOK_LIMIT;
        }
        return BASIC_BOOK_LIMIT;
    }
}