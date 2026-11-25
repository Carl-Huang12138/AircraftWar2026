package edu.hitsz.application;

import org.junit.Test;
import static org.junit.Assert.*;

public class DifficultyTest {
    @Test public void unknownOrMissingDifficultyFallsBackToEasy() {
        assertEquals(Difficulty.EASY, Difficulty.fromExtra(null));
        assertEquals(Difficulty.EASY, Difficulty.fromExtra("unknown"));
    }

    @Test public void menuValuesRoundTrip() {
        for (Difficulty difficulty : Difficulty.values()) {
            assertEquals(difficulty, Difficulty.fromExtra(difficulty.name()));
        }
    }
}
