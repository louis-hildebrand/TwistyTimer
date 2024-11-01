package com.aricneto.twistytimer.puzzle;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.aricneto.twistify.R;
import com.aricneto.twistytimer.utils.Prefs;
import com.aricneto.twistytimer.utils.PuzzleUtils;

import net.gnehzr.tnoodle.scrambles.InvalidScrambleException;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
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
     * Fetches the list of selected cases for the given subset and category.
     */
    public static Set<String> fetchSelectedCases(TrainerSubset subset, String category) {
        Set<String> caseSelection = fetchCaseSelection(subset, category);
        return subset == TrainerSubset.THREE_STYLE_CORNERS
                ? findMatchingCases(getRegex(caseSelection))
                : caseSelection;
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

    private static Set<String> findMatchingCases(Pattern p) {
        // TODO: Add preference for buffer position?
        // TODO: Add preference for lettering scheme?
        if (p == null) {
            return new HashSet<>();
        }

        ArrayList<Character> nonBufferStickers = new ArrayList<>(21);
        for (char c = 'A'; c <= 'X'; c++) {
            if (c != 'C' && c != 'J' && c != 'M') {
                nonBufferStickers.add(c);
            }
        }

        Set<String> selectedCases = new HashSet<String>();
        for (char c1 : nonBufferStickers) {
            for (char c2 : nonBufferStickers) {
                if (c1 != c2) {
                    if (p.matcher("" + c1 + c2).find()) {
                        selectedCases.add("" + c1 + c2);
                    }
                }
            }
        }

        return selectedCases;
    }

    /**
     * Generates a random trainer case from the selected cases
     */
    public static String generateTrainerCase(Context context, TrainerSubset subset, String category) {
        List<String> allowedCases = new ArrayList<>(fetchSelectedCases(subset, category));
        String caseAlg = "";
        String scramble = "";

        CubePuzzle.CubeState state = null;

        if (!allowedCases.isEmpty()) {
            try {
                // Fetch a random setup algorithm and set it as the cube state
                caseAlg = fetchCaseAlgorithm(context, subset.name(), allowedCases.get(random.nextInt(allowedCases.size())));
                state = (CubePuzzle.CubeState) solved.applyAlgorithm(caseAlg);
                // Solve the state
                scramble = ((ThreeByThreeCubePuzzle) puzzle).solveIn(state, 20, null, null);
            } catch (InvalidScrambleException e) {
                e.printStackTrace();
            }
        } else {
            scramble = context.getString(R.string.trainer_help_message);
        }

        if (subset == TrainerSubset.OLL || subset == TrainerSubset.PLL) {
            return PuzzleUtils.applyRotationForAlgorithm(scramble, Y_ROTATIONS[random.nextInt(4)]);
        } else {
            return scramble;
        }
    }

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

            // Split the resouce entries
            String[] res = resources.getStringArray(resId);

            // Return one of the entries
            return res[random.nextInt(res.length)];
        } catch (Exception e) {
            Log.e("TRAINER_SCRAMBLE", "Error retrieving scramble: " + e);
        }

        return "U";
    }
}
