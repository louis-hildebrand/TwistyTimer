package com.aricneto.twistytimer.puzzle;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.aricneto.twistify.R;
import com.aricneto.twistytimer.utils.Prefs;
import com.aricneto.twistytimer.utils.PuzzleUtils;

import net.gnehzr.tnoodle.scrambles.InvalidScrambleException;
import net.gnehzr.tnoodle.scrambles.Puzzle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import puzzle.CubePuzzle;
import puzzle.ThreeByThreeCubePuzzle;

/**
 * Provides scramble algorithms to be used in the trainer
 */
public abstract class TrainerScrambler {
    // The algorithms were taken from
    // github.com/Roman-/oll_trainer TODO: credit them in the app
    private static Random random = new Random();

    private static String[] Y_ROTATIONS = {"", "y", "y2", "y'"};

    private static CubePuzzle puzzle = new ThreeByThreeCubePuzzle();
    private static CubePuzzle.CubeState solved = puzzle.getSolvedState();
    private static final Logger logger = Logger.getLogger(TrainerScrambler.class.getName());

    private TrainerScrambler() {}

    // The key preceding every trainer entry.
    // The version MUST NOT be decreased, only increased, as previous users may have
    // configurations saved in a different TRAINER version that is incompatible with the current
    // implementation, causing crashes.
    public static final String KEY_TRAINER = "TRAINER_V2";

    public enum TrainerSubset {
        OLL,
        PLL,
        THREE_STYLE_CORNERS;

        @Override
        @NotNull
        public String toString() {
            switch (this) {
                case OLL:
                    return "OLL";
                case PLL:
                    return "PLL";
                case THREE_STYLE_CORNERS:
                    return "3-style corners";
                default:
                    throw new IllegalArgumentException("Unknown trainer subset.");
            }
        }
    };

    /**
     * Saves case selection to preferences, to be fetched later.
     * The case selection can either be a case list (e.g., for the OLL and PLL trainers) or a
     * regular expression (e.g., for the 3-style trainer).
     * Preference will be saved in the format:
     *      key: TRAINER[SUBSET][CATEGORY]
     *      set: ITEMS
     */
    public static void saveCaseSelection(TrainerSubset subset, String category, Set<String> selectedItems) {
        Prefs.getPrefs().edit()
                .putStringSet(KEY_TRAINER + subset.name() + category, selectedItems)
                .apply();
    }

    /**
     * Saves case selection to preferences, to be fetched later.
     * The case selection can either be a case list (e.g., for the OLL and PLL trainers) or a
     * regular expression (e.g., for the 3-style trainer).
     * Preference will be saved in the format:
     *      key: TRAINER[SUBSET][CATEGORY]
     *      set: ITEMS
     */
    public static void saveCaseSelection(TrainerSubset subset, String category, List<String> selectedItems) {
        saveCaseSelection(subset, category, new HashSet<>(selectedItems));
    }

    /**
     * Utility function to rename a trainer category, maintaining trainer subsets.
     */
    public static void renameCategory(TrainerSubset subset, String oldCategoryName, String newCategoryName) {
        Set<String> items = fetchCaseSelection(subset, oldCategoryName);
        Prefs.getPrefs().edit().remove(KEY_TRAINER + subset.name() + oldCategoryName).apply();
        saveCaseSelection(subset, newCategoryName, items);
    }

    /**
     * Fetches the previously-saved case selection for the given subset and category.
     * The case selection can either be a case list (e.g., for the OLL and PLL trainers) or a
     * regular expression (e.g., for the 3-style trainer).
     */
    public static Set<String> fetchCaseSelection(TrainerSubset subset, String category) {
        return Prefs.getPrefs()
                .getStringSet(KEY_TRAINER + subset.name() + category, new HashSet<>());
    }

    /**
     * Fetches the set of selected cases for the given subset and category.
     */
    public static Set<String> fetchSelectedCaseSet(TrainerSubset subset, String category, Context context) {
        Set<String> caseSelection = fetchCaseSelection(subset, category);
        if (subset == TrainerSubset.THREE_STYLE_CORNERS) {
            String letterSchemeStr = Prefs.getString(R.string.pk_corner_letter_scheme, LetterScheme.SPEFFZ_LETTERS);
            // TODO: What if new LetterScheme(...) or CornerSticker.parse(...) fail?
            LetterScheme letterScheme = new LetterScheme(letterSchemeStr);
            String bufferStr = Prefs.getString(R.string.pk_corner_buffer, context.getString(R.string.default_corner_buffer));
            CornerSticker buffer = CornerSticker.parse(bufferStr);
            Pattern p = getRegex(caseSelection);
            if (p == null) {
                return new HashSet<>();
            }
            return findMatchingCases(letterScheme, buffer, p);
        } else {
            return caseSelection;
        }
    }

    /**
     * Compile the given case selection to a regular expression (or null if the case selection
     * is empty).
     */
    private static Pattern getRegex(Set<String> caseSelection) {
        String regex = null;
        for (String s : caseSelection) {
            regex = s;
            break;
        }
        if (regex == null || regex.isBlank()) {
            return null;
        }
        try {
            return Pattern.compile(regex);
        }
        catch (PatternSyntaxException e) {
            return null;
        }
    }

    private static Set<String> findMatchingCases(LetterScheme scheme, CornerSticker buffer, Pattern p) {
        if (scheme == null) {
            throw new IllegalArgumentException("Missing letter scheme.");
        }
        if (p == null) {
            throw new IllegalArgumentException("Missing regex.");
        }

        LetterScheme rotatedScheme = scheme.rotate(bufferToUFR(buffer));

        Set<Character> nonBufferStickers = scheme.getLetters();
        nonBufferStickers.remove(rotatedScheme.fromSpeffz('C'));
        nonBufferStickers.remove(rotatedScheme.fromSpeffz('J'));
        nonBufferStickers.remove(rotatedScheme.fromSpeffz('M'));

        Set<String> selectedCases = new HashSet<>();
        for (char c1 : nonBufferStickers) {
            for (char c2 : nonBufferStickers) {
                if (c1 != c2 && p.matcher("" + c1 + c2).find()) {
                    selectedCases.add("" + c1 + c2);
                }
            }
        }

        return selectedCases;
    }

    /**
     * Rotate the cube so that the given sticker is now at UFR.
     *
     * @param buffer The buffer position, in the Speffz scheme.
     * @return A sequence of whole-cube rotations that will move the given buffer to UFR.
     */
    private static String bufferToUFR(CornerSticker buffer) {
        switch (buffer) {
            case UBL:
                return "y2";
            case UBR:
                return "y";
            case UFR:
                return "";
            case UFL:
                return "y'";
            case FLU:
                return "x y2";
            case FRU:
                return "x y";
            case FRD:
                return "x";
            case FLD:
                return "x y'";
            case RFU:
                return "z' y'";
            case RBU:
                return "z' y2";
            case RBD:
                return "z' y";
            case RFD:
                return "z'";
            case BRU:
                return "x'";
            case BLU:
                return "x' y'";
            case BLD:
                return "x' y2";
            case BRD:
                return "x' y";
            case LBU:
                return "z y";
            case LFU:
                return "z";
            case LFD:
                return "z y'";
            case LBD:
                return "z y2";
            case DLF:
                return "z2";
            case DRF:
                return "z2 y'";
            case DRB:
                return "z2 y2";
            case DLB:
                return "z2 y";
            default:
                throw new IllegalArgumentException(String.format("Cannot rotate %s to C because %s is not a valid letter in the Speffz scheme.", buffer.name(), buffer.name()));
        }
    }

    /**
     * Generates a random trainer case from the selected cases
     */
    public static String generateTrainerCase(Context context, TrainerSubset subset, String category) {
        List<String> allowedCases = new ArrayList<>(fetchSelectedCaseSet(subset, category, context));
        if (allowedCases.isEmpty()) {
            return context.getString(R.string.trainer_help_message);
        }
        String caseName = allowedCases.get(random.nextInt(allowedCases.size()));

        switch (subset) {
            case OLL:
            case PLL:
                return generateOLLPLLTrainerCase(context, subset, caseName);
            case THREE_STYLE_CORNERS:
                String letterSchemeStr = Prefs.getString(R.string.pk_corner_letter_scheme, LetterScheme.SPEFFZ_LETTERS);
                // TODO: What if new LetterScheme(...) or CornerSticker.parse(...) fail?
                LetterScheme letterScheme = new LetterScheme(letterSchemeStr);
                String bufferStr = Prefs.getString(R.string.pk_corner_buffer, context.getString(R.string.default_corner_buffer));
                CornerSticker buffer = CornerSticker.parse(bufferStr);
                return generateThreeStyleTrainerCase(context, subset, caseName, letterScheme, buffer);
            default:
                throw new IllegalArgumentException(String.format("Unsupported trainer subset %s.", subset.name()));
        }
    }

    private static String generateOLLPLLTrainerCase(Context context, TrainerSubset subset, String caseName) {
        String caseAlg = fetchCaseAlgorithm(context, subset.name(), caseName);

        CubePuzzle.CubeState state;
        try {
            state = (CubePuzzle.CubeState) solved.applyAlgorithm(caseAlg);
        } catch (InvalidScrambleException e) {
            e.printStackTrace();
            return "";
        }
        String scramble = ((ThreeByThreeCubePuzzle) puzzle).solveIn(state, 20, null, null);

        return PuzzleUtils.applyRotationForAlgorithm(scramble, Y_ROTATIONS[random.nextInt(4)]);
    }

    private static String generateThreeStyleTrainerCase(Context context, TrainerSubset subset, String caseName, LetterScheme scheme, CornerSticker buffer) {
        String rotateBufferAlg = bufferToUFR(buffer);
        LetterScheme rotatedScheme = scheme.rotate(rotateBufferAlg);

        String speffzCase = rotatedScheme.toSpeffz(caseName);
        String alg = fetchCaseAlgorithm(context, subset.name(), speffzCase);
        String[] algMoves = alg.split("\\s+");

        CubePuzzle.CubeState state;
        try {
            state = (CubePuzzle.CubeState) solved.applyAlgorithm(alg);
        } catch (InvalidScrambleException e) {
            e.printStackTrace();
            return "";
        }
        // Use firstAxisRestriction and lastAxisRestriction to ensure the scramble doesn't closely
        // resemble the solution
        String firstMoveFace = algMoves[0].substring(0, 1);
        if (!isValidAxis(firstMoveFace)) {
            firstMoveFace = null;
        }
        String lastMoveFace = algMoves[algMoves.length - 1].substring(0, 1);
        if (!isValidAxis(lastMoveFace)) {
            lastMoveFace = null;
        }
        logger.info(String.format("Searching for scramble for case \"%s\" with firstAxisRestriction=\"%s\" and lastAxisRestriction=\"%s\".", caseName, lastMoveFace, firstMoveFace));
        String scramble = ((ThreeByThreeCubePuzzle) puzzle).solveIn(state, 20, lastMoveFace, firstMoveFace);

        return PuzzleUtils.applyRotationsForAlgorithm(scramble, invertRotations(rotateBufferAlg));
    }

    private static boolean isValidAxis(String axis) {
        return "U".equals(axis)
                || "F".equals(axis)
                || "R".equals(axis)
                || "B".equals(axis)
                || "L".equals(axis)
                || "D".equals(axis);
    }

    // TODO: Generalize this and move it to `PuzzleUtils`?
    private static String invertRotations(String rotations) {
        if ((rotations == null ? "" : rotations).trim().isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        String[] moves = rotations.split("\\s+");
        for (int i = moves.length - 1; i >= 0; i--) {
            builder.append(invertRotation(moves[i]));
            if (i > 0) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    private static String invertRotation(String rot) {
        switch (rot) {
            case "x":
                return "x'";
            case "x'":
                return "x";
            case "x2":
                return "x2";
            case "y":
                return "y'";
            case "y'":
                return "y";
            case "y2":
                return "y2";
            case "z":
                return "z'";
            case "z'":
                return "z";
            case "z2":
                return "z2";
            default:
                throw new IllegalArgumentException(String.format("'%s' is not a valid rotation.", rot));
        }
    }

    /**
     * Find an algorithm which solves the given case.
     */
    private static String fetchCaseAlgorithm(Context context, String subset, String name) {
        Resources resources = context.getResources();

        // Finds an algorithm resource with a matching name on the file trainer_scrambles.xml
        try {
            // Find the resource
            int resId = resources.getIdentifier(
                    "TRAINER_" + subset + "_" + name.replace(" ", "_").toUpperCase(),
                    "array",
                    context.getPackageName()
            );

            // Split the resource entries
            String[] res = resources.getStringArray(resId);

            // Return one of the entries
            return res[random.nextInt(res.length)];
        } catch (Exception e) {
            Log.e("TRAINER_SCRAMBLE", "Error retrieving scramble: " + e);
        }

        return "U";
    }
}
