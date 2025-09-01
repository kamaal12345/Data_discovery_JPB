package com.jio.utils;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class PiiPatterns {

    public static final Map<String, Pattern> PII_PATTERNS = new HashMap<>();
    public static final Map<String, Predicate<String>> PII_VALIDATORS = new HashMap<>();

    // Valid Indian State & UT codes for Driving License
    private static final Set<String> STATE_CODES = Set.of(
            "AP","AR","AS","BR","CG","CH","DD","DL","DN","GA","GJ","HP","HR",
            "JH","JK","KA","KL","LA","LD","MH","ML","MN","MP","MZ","NL","OD",
            "PB","PY","RJ","SK","TN","TR","TS","UK","UP","WB"
    );
    
    private static final Set<String> VALID_BANK_CODES = Set.of(
    	    "SBIN", "HDFC", "ICIC", "PUNB", "UTIB", "AXIS", "KARB", "KKBK", "CNRB",
    	    "IDIB", "BARB", "MAHB", "BKID", "ORBC", "IOBA", "VIJB", "UBIN", "YESB"
    	);

    static {
        // ============================
        // PII REGEX PATTERNS
        // ============================
        PII_PATTERNS.put("pan", Pattern.compile("\\b[A-Z]{5}[0-9]{4}[A-Z]{1}\\b"));
        PII_PATTERNS.put("aadhaar", Pattern.compile("\\b[2-9]{1}[0-9]{3}[- ]?[0-9]{4}[- ]?[0-9]{4}\\b"));
        PII_PATTERNS.put("voter", Pattern.compile("\\b[A-Z]{3}[0-9]{7}\\b"));
        PII_PATTERNS.put("dl", Pattern.compile("\\b([A-Z]{2}[- ]?[0-9]{2})(19|20)[0-9]{2}[0-9]{7}\\b"));
        PII_PATTERNS.put("passport", Pattern.compile("\\b[A-Z]{1}[0-9]{7}\\b"));
        PII_PATTERNS.put("ifsc", Pattern.compile("\\b[A-Z]{4}0[A-Z0-9]{6}\\b"));
        PII_PATTERNS.put("micr", Pattern.compile("\\b[0-9]{9}\\b"));
        PII_PATTERNS.put("account_number", Pattern.compile("\\b[0-9]{9,18}\\b"));
        PII_PATTERNS.put("cif_number", Pattern.compile("\\b[0-9]{10,12}\\b"));
        PII_PATTERNS.put("debit_card", Pattern.compile("\\b[0-9]{16}\\b"));
        PII_PATTERNS.put("credit_card", Pattern.compile(
                "\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13})\\b"));
        PII_PATTERNS.put("ckyc", Pattern.compile("\\b[0-9]{14}\\b"));
        PII_PATTERNS.put("loan_account", Pattern.compile("\\b(LOAN|LA|HL|PL|AL|BL|SL)[0-9]{12,16}\\b"));
        PII_PATTERNS.put("fd_account", Pattern.compile("\\b(FD|TD)[0-9]{12,16}\\b"));
        PII_PATTERNS.put("customer_id", Pattern.compile("\\b(C|CUST|CUS)[0-9]{8,12}\\b"));
        PII_PATTERNS.put("demat_account", Pattern.compile("\\b[0-9]{8}(?:[0-9]{8})?(?:[0-9]{4})?\\b"));
        PII_PATTERNS.put("email", Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"));
        PII_PATTERNS.put("phone", Pattern.compile("\\b(\\+91[\\-\\s]?)?[0]?[6789]\\d{9}\\b"));
        PII_PATTERNS.put("vehicle_number", Pattern.compile("\\b[A-Z]{2}[ -]?[0-9]{1,2}[ -]?[A-Z]{1,3}[ -]?[0-9]{4}\\b"));

        // ============================
        // VALIDATORS
        // ============================
        PII_VALIDATORS.put("aadhaar", PiiPatterns::isValidAadhaar);
        PII_VALIDATORS.put("credit_card", PiiPatterns::isValidLuhn);
        PII_VALIDATORS.put("debit_card", PiiPatterns::isValidLuhn);
        PII_VALIDATORS.put("account_number", s -> s.length() >= 9 && s.length() <= 18);
        PII_VALIDATORS.put("voter", s -> s.matches("^[A-Z]{3}[0-9]{7}$"));
        PII_VALIDATORS.put("dl", PiiPatterns::isValidDrivingLicense);
        PII_VALIDATORS.put("ifsc",PiiPatterns::isValidIFSC);
        PII_VALIDATORS.put("vehicle_number", PiiPatterns::isValidVehicleNumber);
        PII_VALIDATORS.put("phone", PiiPatterns::isValidPhoneNumber);
    }

    // Aadhaar Verhoeff checksum
    private static boolean isValidAadhaar(String aadhaar) {
        String num = aadhaar.replaceAll("\\s|-", "");
        if (num.length() != 12 || !num.matches("\\d+")) return false;
        return verhoeffValidate(num);
    }

    // Driving License Validator (State + Format)
    private static boolean isValidDrivingLicense(String dl) {
        String normalized = dl.replaceAll(" ", "").replaceAll("-", "").toUpperCase();
        if (normalized.length() < 15) return false;

        String stateCode = normalized.substring(0, 2);
        if (!STATE_CODES.contains(stateCode)) return false;

        return normalized.matches("^[A-Z]{2}[0-9]{2}(19|20)[0-9]{2}[0-9]{7}$");
    }
    
    
    private static boolean isValidPhoneNumber(String raw) {
        if (raw == null) return false;

        // Remove spaces, dashes, parentheses
        String number = raw.replaceAll("[\\s\\-()]", "");

        // Allow +91 or 0 prefix
        if (number.startsWith("+91")) {
            number = number.substring(3);
        } else if (number.startsWith("91") && number.length() == 12) {
            // Case: "919876543210"
            number = number.substring(2);
        } else if (number.startsWith("0") && number.length() == 11) {
            number = number.substring(1);
        }

        // Must be 10 digits now
        if (!number.matches("^[6-9][0-9]{9}$")) {
            return false;
        }

        return true;
    }

    
    private static boolean isValidVehicleNumber(String raw) {
        String normalized = raw.toUpperCase().replaceAll("[\\s-]", "");
        
        // Format check
        if (!normalized.matches("^[A-Z]{2}\\d{1,2}[A-Z]{1,3}\\d{4}$")) {
            return false;
        }

        // Validate state code
        String state = normalized.substring(0, 2);
        if (!STATE_CODES.contains(state)) {
            return false;
        }

        // Validate last 4 are not all 0000
        String last4 = normalized.substring(normalized.length() - 4);
        if (last4.equals("0000")) {
            return false;
        }

        return true;
    }

    
    private static boolean isValidIFSC(String raw) {
        if (!raw.matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
            return false;
        }
        String bankCode = raw.substring(0, 4);
        return VALID_BANK_CODES.contains(bankCode);
    }


    // Luhn checksum for cards
    private static boolean isValidLuhn(String number) {
        number = number.replaceAll("\\s+", "");
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    // Verhoeff algorithm for Aadhaar
    private static final int[][] d = {
            {0,1,2,3,4,5,6,7,8,9},
            {1,2,3,4,0,6,7,8,9,5},
            {2,3,4,0,1,7,8,9,5,6},
            {3,4,0,1,2,8,9,5,6,7},
            {4,0,1,2,3,9,5,6,7,8},
            {5,9,8,7,6,0,4,3,2,1},
            {6,5,9,8,7,1,0,4,3,2},
            {7,6,5,9,8,2,1,0,4,3},
            {8,7,6,5,9,3,2,1,0,4},
            {9,8,7,6,5,4,3,2,1,0}
    };

    private static final int[][] p = {
            {0,1,2,3,4,5,6,7,8,9},
            {1,5,7,6,2,8,3,0,9,4},
            {5,8,0,3,7,9,6,1,4,2},
            {8,9,1,6,0,4,3,5,2,7},
            {9,4,5,3,1,2,6,8,7,0},
            {4,2,8,6,5,7,3,9,0,1},
            {2,7,9,3,8,0,6,4,1,5},
            {7,0,4,6,9,1,3,2,5,8}
    };

    private static final int[] inv = {0,4,3,2,1,5,6,7,8,9};

    private static boolean verhoeffValidate(String num) {
        int c = 0;
        int[] myArray = StringToReversedIntArray(num);
        for (int i = 0; i < myArray.length; i++) {
            c = d[c][p[(i % 8)][myArray[i]]];
        }
        return c == 0;
    }

    private static int[] StringToReversedIntArray(String num) {
        int[] myArray = new int[num.length()];
        for (int i = 0; i < num.length(); i++) {
            myArray[i] = Integer.parseInt(num.substring(num.length() - i - 1, num.length() - i));
        }
        return myArray;
    }
}
