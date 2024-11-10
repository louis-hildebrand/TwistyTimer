package com.aricneto.twistytimer.puzzle;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class LetterSchemeTestCase {
    @Test
    public void testGetLettersSpeffz() {
        LetterScheme letterScheme = LetterScheme.speffz();
        assertEquals(makeCharSet("ABCDEFGHIJKLMNOPQRSTUVWX"), letterScheme.getLetters());
        Set<Character> s = letterScheme.getLetters();
        s.removeAll(makeCharSet("ABCDEFGHIJKLMY0123"));
        assertEquals(makeCharSet("NOPQRSTUVWX"), s);
        assertEquals(makeCharSet("ABCDEFGHIJKLMNOPQRSTUVWX"), letterScheme.getLetters());
    }

    @Test
    public void testGetLettersNonstandard() {
        LetterScheme letterScheme = new LetterScheme("ZYXWVUTSRQPONMLKJIHGFECA");
        assertEquals(makeCharSet("ACEFGHIJKLMNOPQRSTUVWXYZ"), letterScheme.getLetters());
        Set<Character> s = letterScheme.getLetters();
        s.removeAll(makeCharSet("ABCDEFGHIJKLMY0123"));
        assertEquals(makeCharSet("NOPQRSTUVWXZ"), s);
        assertEquals(makeCharSet("ACEFGHIJKLMNOPQRSTUVWXYZ"), letterScheme.getLetters());
    }

    @Test
    public void testSpeffz() {
        LetterScheme letterScheme = LetterScheme.speffz();

        for (char c1 = 'A'; c1 <= 'X'; c1 += 1) {
            String s1 = Character.toString(c1);
            assertEquals(s1, letterScheme.toSpeffz(s1));
            assertEquals(s1, letterScheme.fromSpeffz(s1));
            for (char c2 = 'A'; c2 <= 'X'; c2 += 1) {
                String s2 = "" + c1 + c2;
                assertEquals(s2, letterScheme.toSpeffz(s2));
                assertEquals(s2, letterScheme.fromSpeffz(s2));
            }
        }
    }

    /**
     * Test a lettering scheme that's similar to Speffz, but iterates over the faces in the order
     * UFRBLD rather than the standard ULFRBD.
     */
    @Test
    public void testUFRBLD() {
        LetterScheme letterScheme = new LetterScheme("ABCDQRSTEFGHIJKLMNOPUVWX");

        assertEquals('A', letterScheme.toSpeffz('A'));
        assertEquals('B', letterScheme.toSpeffz('B'));
        assertEquals('C', letterScheme.toSpeffz('C'));
        assertEquals('D', letterScheme.toSpeffz('D'));
        assertEquals('I', letterScheme.toSpeffz('E'));
        assertEquals('J', letterScheme.toSpeffz('F'));
        assertEquals('K', letterScheme.toSpeffz('G'));
        assertEquals('L', letterScheme.toSpeffz('H'));
        assertEquals('M', letterScheme.toSpeffz('I'));
        assertEquals('N', letterScheme.toSpeffz('J'));
        assertEquals('O', letterScheme.toSpeffz('K'));
        assertEquals('P', letterScheme.toSpeffz('L'));
        assertEquals('Q', letterScheme.toSpeffz('M'));
        assertEquals('R', letterScheme.toSpeffz('N'));
        assertEquals('S', letterScheme.toSpeffz('O'));
        assertEquals('T', letterScheme.toSpeffz('P'));
        assertEquals('E', letterScheme.toSpeffz('Q'));
        assertEquals('F', letterScheme.toSpeffz('R'));
        assertEquals('G', letterScheme.toSpeffz('S'));
        assertEquals('H', letterScheme.toSpeffz('T'));
        assertEquals('U', letterScheme.toSpeffz('U'));
        assertEquals('V', letterScheme.toSpeffz('V'));
        assertEquals('W', letterScheme.toSpeffz('W'));
        assertEquals('X', letterScheme.toSpeffz('X'));

        assertEquals('A', letterScheme.fromSpeffz('A'));
        assertEquals('B', letterScheme.fromSpeffz('B'));
        assertEquals('C', letterScheme.fromSpeffz('C'));
        assertEquals('D', letterScheme.fromSpeffz('D'));
        assertEquals('Q', letterScheme.fromSpeffz('E'));
        assertEquals('R', letterScheme.fromSpeffz('F'));
        assertEquals('S', letterScheme.fromSpeffz('G'));
        assertEquals('T', letterScheme.fromSpeffz('H'));
        assertEquals('E', letterScheme.fromSpeffz('I'));
        assertEquals('F', letterScheme.fromSpeffz('J'));
        assertEquals('G', letterScheme.fromSpeffz('K'));
        assertEquals('H', letterScheme.fromSpeffz('L'));
        assertEquals('I', letterScheme.fromSpeffz('M'));
        assertEquals('J', letterScheme.fromSpeffz('N'));
        assertEquals('K', letterScheme.fromSpeffz('O'));
        assertEquals('L', letterScheme.fromSpeffz('P'));
        assertEquals('M', letterScheme.fromSpeffz('Q'));
        assertEquals('N', letterScheme.fromSpeffz('R'));
        assertEquals('O', letterScheme.fromSpeffz('S'));
        assertEquals('P', letterScheme.fromSpeffz('T'));
        assertEquals('U', letterScheme.fromSpeffz('U'));
        assertEquals('V', letterScheme.fromSpeffz('V'));
        assertEquals('W', letterScheme.fromSpeffz('W'));
        assertEquals('X', letterScheme.fromSpeffz('X'));

        assertEquals("PL", letterScheme.toSpeffz("LH"));
        assertEquals("HT", letterScheme.fromSpeffz("LH"));
    }

    @Test
    public void testInvalidLetterSchemeNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new LetterScheme(null));
        assertEquals("Invalid length: expected 24 but got 0.", ex.getMessage());
    }

    @Test
    public void testInvalidLetterSchemeTooShort() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new LetterScheme("ABCDEFGHIJKLMNOPQRSTUVW "));
        assertEquals("Invalid length: expected 24 but got 23.", ex.getMessage());
    }

    @Test
    public void testInvalidLetterSchemeTooLong() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new LetterScheme("ABCDEFGHIJKLMNOPQRSTUVWXY"));
        assertEquals("Invalid length: expected 24 but got 25.", ex.getMessage());
    }

    @Test
    public void testInvalidLetterSchemeDuplicateLetter() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new LetterScheme("ABCDEFGHIJKLMNOPQRSTUVWC"));
        assertEquals("The letter 'C' appears multiple times.", ex.getMessage());
    }

    @Test
    public void testInvalidLetter() {
        LetterScheme letterScheme = new LetterScheme("ZYXWVUTSRQPONMLKJIHGFEDC");

        IllegalArgumentException ex;

        ex = assertThrows(IllegalArgumentException.class, () -> letterScheme.toSpeffz("A"));
        assertEquals("'A' is not a valid letter in this scheme.", ex.getMessage());
        ex = assertThrows(IllegalArgumentException.class, () -> letterScheme.toSpeffz("B"));
        assertEquals("'B' is not a valid letter in this scheme.", ex.getMessage());
        for (char c = 'C'; c <= 'Z'; c += 1) {
            letterScheme.toSpeffz(Character.toString(c));
        }

        ex = assertThrows(IllegalArgumentException.class, () -> letterScheme.fromSpeffz("Y"));
        assertEquals("'Y' is not a valid letter in the Speffz scheme.", ex.getMessage());
        ex = assertThrows(IllegalArgumentException.class, () -> letterScheme.fromSpeffz("Z"));
        assertEquals("'Z' is not a valid letter in the Speffz scheme.", ex.getMessage());
        for (char c = 'A'; c <= 'X'; c += 1) {
            letterScheme.fromSpeffz(Character.toString(c));
        }
    }

    private static Set<Character> makeCharSet(String s) {
        Set<Character> set = new HashSet<>(s.length());
        for (int i = 0; i < s.length(); i++) {
            set.add(s.charAt(i));
        }
        return set;
    }
}
