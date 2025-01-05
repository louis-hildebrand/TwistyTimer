package com.aricneto.twistytimer.puzzle;

import java.io.Serializable;

public class TrainerCase implements Serializable {
    private final String name;
    private final String scramble;
    private final boolean isValid;

    private TrainerCase(String name, String scramble, boolean isValid) {
        this.name = name;
        this.scramble = scramble;
        this.isValid = isValid;
    }

    public static TrainerCase makeValid(String name, String scramble) {
        return new TrainerCase(name, scramble, true);
    }

    public static TrainerCase makeInvalid(String msg) {
        return new TrainerCase("", msg, false);
    }

    /**
     * The name of this case.
     */
    public String getName() {
        return name;
    }

    /**
     * For a valid trainer case, this is the scramble sequence.
     * For an invalid trainer case, this is the error message to show to the user.
     */
    public String getScramble() {
        return scramble;
    }

    /**
     * Whether or not this trainer case is valid.
     * For example, if the user has not selected any cases to train, an invalid trainer case could be generated.
     */
    public boolean isValid() {
        return isValid;
    }
}
