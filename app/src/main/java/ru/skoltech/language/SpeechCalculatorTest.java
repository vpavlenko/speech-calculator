package ru.skoltech.language;

import junit.framework.TestCase;

import ru.skoltech.language.speech_calculator.SpeechCalculator;

public class SpeechCalculatorTest extends TestCase {

    public void testCountNumbersAndArithmeticOperations() throws Exception {
        assertEquals(1, SpeechCalculator.countNumbersAndArithmeticOperations("123"));
        assertEquals(0, SpeechCalculator.countNumbersAndArithmeticOperations("abc"));
        assertEquals(2, SpeechCalculator.countNumbersAndArithmeticOperations("12 abc 45"));
        assertEquals(3, SpeechCalculator.countNumbersAndArithmeticOperations("12.34"));
        assertEquals(5, SpeechCalculator.countNumbersAndArithmeticOperations("3 + 5 - 1"));
    }

    public void testProcessRecognitionResult() throws Exception {

    }
}