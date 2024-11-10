package com.aricneto.twistytimer.puzzle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a lettering scheme, as used in blindfolded solving.
 */
public class LetterScheme {
    public static final String SPEFFZ_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWX";

    private final Map<Character, Character> speffzLetterToCustomLetter;
    private final Map<Character, Character> customLetterToSpeffzLetter;

    /**
     * @param letters The letters of this lettering scheme, in the standard Speffz order: UBL, UBR,
     *                UFR, UFL, LBU, LFU, LFD, LBD, FLU, FRU, FRD, FLD, RFU, RBU, RBD, RFD, BRU,
     *                BLU, BLD, BRD, DLF, DRF, DRB, DLB.
     */
    public LetterScheme(String letters) {
        if (letters == null) {
            letters = "";
        } else {
            letters = letters.trim();
        }
        if (letters.length() != 24) {
            throw new IllegalArgumentException(String.format("Invalid length: expected 24 but got %d.", letters.length()));
        }

        this.customLetterToSpeffzLetter = new HashMap<>(24);
        this.speffzLetterToCustomLetter = new HashMap<>(24);
        for (char speffz = 'A'; speffz <= 'X'; speffz++) {
            char custom = letters.charAt(speffz - 'A');
            speffzLetterToCustomLetter.put(speffz, custom);
            if (customLetterToSpeffzLetter.containsKey(custom)) {
                throw new IllegalArgumentException(String.format("The letter '%c' appears multiple times.", custom));
            }
            customLetterToSpeffzLetter.put(custom, speffz);
        }
    }

    /**
     * The Speffz lettering scheme.
     */
    public static LetterScheme speffz() {
        return new LetterScheme(SPEFFZ_LETTERS);
    }

    /**
     * The set of letters used in this scheme.
     */
    public Set<Character> getLetters() {
        // Make a copy so the map isn't modified
        return new HashSet<>(this.customLetterToSpeffzLetter.keySet());
    }

    /**
     * Convert from this lettering scheme into the Speffz lettering scheme.
     * For example, {@code new LetterScheme("ABCDQRSTEFGHIJKLMNOPUVWX").toSpeffz('L') == 'P'}.
     *
     * @param c A letter in this lettering scheme.
     * @return The corresponding letter in the Speffz lettering scheme.
     */
    public char toSpeffz(char c) {
        Character speffz = this.customLetterToSpeffzLetter.get(c);
        if (speffz == null) {
            throw new IllegalArgumentException(String.format("'%c' is not a valid letter in this scheme.", c));
        }
        return speffz;
    }

    /**
     * Convert from this lettering scheme into the Speffz lettering scheme.
     * For example, {@code new LetterScheme("ABCDQRSTEFGHIJKLMNOPUVWX").toSpeffz("LH") == "PL"}.
     *
     * @param letters A string (e.g., a 3-style case) in this lettering scheme.
     * @return The provided string, translated letter-by-letter to the Speffz lettering scheme.
     */
    public String toSpeffz(String letters) {
        if (letters == null || "".equals(letters.trim())) {
            return "";
        }
        StringBuilder translatedString = new StringBuilder(letters.length());
        for (char custom : letters.toCharArray()) {
            translatedString.append(this.toSpeffz(custom));
        }
        return translatedString.toString();
    }

    /**
     * Convert from the Speffz lettering scheme into this lettering scheme.
     * For example, {@code new LetterScheme("ABCDQRSTEFGHIJKLMNOPUVWX").fromSpeffz('L') == 'H'}.
     *
     * @param c A letter in the Speffz lettering scheme.
     * @return The corresponding letter in this lettering scheme.
     */
    public char fromSpeffz(char c) {
        Character custom = this.speffzLetterToCustomLetter.get(c);
        if (custom == null) {
            throw new IllegalArgumentException(String.format("'%c' is not a valid letter in the Speffz scheme.", c));
        }
        return custom;
    }

    /**
     * Convert from the Speffz lettering scheme into this lettering scheme.
     * For example, {@code new LetterScheme("ABCDQRSTEFGHIJKLMNOPUVWX").fromSpeffz("LH") == "HT"}.
     *
     * @param letters A string (e.g., a 3-style case) in the Speffz lettering scheme.
     * @return The provided string, translated letter-by-letter to this lettering scheme.
     */
    public String fromSpeffz(String letters) {
        if (letters == null || "".equals(letters.trim())) {
            return "";
        }
        StringBuilder translatedString = new StringBuilder(letters.length());
        for (char speffz : letters.toCharArray()) {
            translatedString.append(this.fromSpeffz(speffz));
        }
        return translatedString.toString();
    }
}
