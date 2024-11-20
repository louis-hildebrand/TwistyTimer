package com.aricneto.twistytimer.puzzle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;

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

    // How to permute letters when a rotation is applied.
    // newLetters.charAt(i) is given by oldLetters.charAt(permutation[i]).
    private static final int[] NO_OP = {
             0,  1,  2,  3,
             4,  5,  6,  7,
             8,  9, 10, 11,
            12, 13, 14, 15,
            16, 17, 18, 19,
            20, 21, 22, 23
    };
    private static final int[] ROTATE_X = {
             8,  9, 10, 11,
             5,  6,  7,  4,
            20, 21, 22, 23,
            15, 12, 13, 14,
             2,  3,  0,  1,
            18, 19, 16, 17
    };
    private static final int[] ROTATE_X2 = compose(ROTATE_X, ROTATE_X);
    private static final int[] ROTATE_X_PRIME = compose(ROTATE_X2, ROTATE_X);
    private static final int[] ROTATE_Y = {
             3,  0,  1,  2,
             8,  9, 10, 11,
            12, 13, 14, 15,
            16, 17, 18, 19,
             4,  5,  6,  7,
            21, 22, 23, 20
    };
    private static final int[] ROTATE_Y2 = compose(ROTATE_Y, ROTATE_Y);
    private static final int[] ROTATE_Y_PRIME = compose(ROTATE_Y2, ROTATE_Y);
    private static final int[] ROTATE_Z = compose(compose(ROTATE_X, ROTATE_Y), ROTATE_X_PRIME);
    private static final int[] ROTATE_Z2 = compose(ROTATE_Z, ROTATE_Z);
    private static final int[] ROTATE_Z_PRIME = compose(ROTATE_Z2, ROTATE_Z);

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

    private String getLettersInOrder() {
        StringBuilder builder = new StringBuilder();
        for (char c : SPEFFZ_LETTERS.toCharArray()) {
            builder.append(this.speffzLetterToCustomLetter.get(c));
        }
        return builder.toString();
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

    /**
     * Compute the letter scheme you'd get by writing the current letter scheme onto the physical
     * stickers and then applying the given sequence of rotations. For example,
     * {@code new LetterScheme("ABCDEFGHIJKLMNOPQRSTUVWX").rotate("y") ==
     * new LetterScheme("DABCIJKLMNOPQRSTEFGHVWXU")}
     *
     * @param rotations A sequence of rotations (x, y', z2, etc.)
     * @return The new letter scheme.
     */
    public LetterScheme rotate(String rotations) {
        if (rotations == null || rotations.trim().isEmpty()) {
            return this;
        }

        int[] permutation = NO_OP;
        for (String move : rotations.split("\\s+")) {
            switch (move) {
                case "x":
                    permutation = compose(permutation, ROTATE_X);
                    break;
                case "x'":
                    permutation = compose(permutation, ROTATE_X_PRIME);
                    break;
                case "x2":
                    permutation = compose(permutation, ROTATE_X2);
                    break;
                case "y":
                    permutation = compose(permutation, ROTATE_Y);
                    break;
                case "y'":
                    permutation = compose(permutation, ROTATE_Y_PRIME);
                    break;
                case "y2":
                    permutation = compose(permutation, ROTATE_Y2);
                    break;
                case "z":
                    permutation = compose(permutation, ROTATE_Z);
                    break;
                case "z'":
                    permutation = compose(permutation, ROTATE_Z_PRIME);
                    break;
                case "z2":
                    permutation = compose(permutation, ROTATE_Z2);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("'%s' is not a valid rotation.", move));
            }
        }

        String currentLetters = this.getLettersInOrder();
        StringBuilder newLetters = new StringBuilder();
        for (int i = 0; i < SPEFFZ_LETTERS.length(); i++) {
            newLetters.append(currentLetters.charAt(permutation[i]));
        }
        return new LetterScheme(newLetters.toString());
    }

    private static int[] compose(int[] p1, int[] p2) {
        if (p1.length != p2.length) {
            throw new IllegalArgumentException("Cannot compose permutations due to length mismatch.");
        }
        int[] out = new int[p1.length];
        for (int i = 0; i < p1.length; i++) {
            out[i] = p1[p2[i]];
        }
        return out;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof LetterScheme)) {
            return false;
        }
        LetterScheme that = (LetterScheme) obj;
        return this.getLettersInOrder().equals(that.getLettersInOrder());
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("LetterScheme(\"%s\")", this.getLettersInOrder());
    }
}
