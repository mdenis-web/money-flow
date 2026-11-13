package com.moneyflow.util;

import com.moneyflow.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {
    @Test
    void parseAmountOk() {
        assertEquals(1500.0, Validator.parseAmount("1500"));
    }

    @Test
    void parseAmountWithComma() {
        assertEquals(1500.5, Validator.parseAmount("1,500.5"));
    }

    @Test
    void parseAmountNegative() {
        assertThrows(ValidationException.class, () -> Validator.parseAmount("-10"));
    }

    @Test
    void parseAmountGarbage() {
        assertThrows(ValidationException.class, () -> Validator.parseAmount("abc"));
    }

    @Test
    void emptyString() {
        assertThrows(ValidationException.class, () -> Validator.requireNotEmpty(" ", "login"));
    }

    @Test
    void positiveCheck() {
        assertThrows(ValidationException.class, () -> Validator.requirePositive(0, "amount"));
    }
}
