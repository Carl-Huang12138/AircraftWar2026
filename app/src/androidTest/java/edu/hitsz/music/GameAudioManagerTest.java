package edu.hitsz.music;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hitsz.R;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GameAudioManagerTest {
    @Test public void explicitNewRoundRestoresMusicAndLoadedEffectsImmediately() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AtomicReference<GameAudioManager> reference = new AtomicReference<>();
        AtomicInteger stream = new AtomicInteger();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            GameAudioManager audio = new GameAudioManager(context, true);
            reference.set(audio);
            audio.resume();
        });
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (stream.get() == 0 && System.nanoTime() < deadline) {
                InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                        stream.set(reference.get().playEffect(R.raw.bullet_hit)));
                Thread.sleep(20);
            }
            assertTrue(stream.get() > 0);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                GameAudioManager audio = reference.get();
                for (int round = 0; round < 3; round++) {
                    audio.gameOver();
                    assertFalse(audio.isBackgroundPlaying());
                    audio.resetForNewGame();
                    assertTrue(audio.isBackgroundPlaying());
                    assertTrue("No new sample-loading window after replay", audio.playEffect(R.raw.bullet_hit) > 0);
                }
                audio.pause();
                audio.resetForNewGame();
                assertFalse("Reset in background must stay silent", audio.isBackgroundPlaying());
                audio.resume();
                assertTrue(audio.isBackgroundPlaying());
                audio.release();
                audio.resetForNewGame();
                assertFalse(audio.isBackgroundPlaying());
            });
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> reference.get().release());
        }
    }

    @Test public void bossMusicKeepsNormalTrackPositionAndShortEffectsReallyPlay() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AtomicReference<GameAudioManager> reference = new AtomicReference<>();
        AtomicInteger stream = new AtomicInteger();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            GameAudioManager audio = new GameAudioManager(context, true);
            reference.set(audio);
            audio.resume();
        });
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (stream.get() == 0 && System.nanoTime() < deadline) {
                InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                        stream.set(reference.get().playEffect(R.raw.bullet_hit)));
                Thread.sleep(20);
            }
            assertTrue("A loaded effect must return a real SoundPool stream", stream.get() > 0);
            Thread.sleep(350);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                GameAudioManager audio = reference.get();
                assertTrue(audio.playEffect(R.raw.bomb_explosion) > 0);
                assertTrue(audio.playEffect(R.raw.get_supply) > 0);
                audio.pause();
                int normalPosition = audio.getBackgroundPositionMillis();
                assertTrue(normalPosition > 0);
                audio.setBossPresent(true);
                audio.resume();
                assertTrue(audio.isBackgroundPlaying());
                audio.pause();
                audio.setBossPresent(false);
                assertTrue("Normal BGM must retain its paused position",
                        audio.getBackgroundPositionMillis() >= normalPosition);
                audio.resume();
                assertTrue(audio.isBackgroundPlaying());
                audio.gameOver();
                assertTrue(audio.playEffect(R.raw.game_over) > 0);
            });
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> reference.get().release());
        }
    }

    @Test public void enabledMusicSwitchesPausesAndCannotRestartAfterGameOver() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            GameAudioManager audio = new GameAudioManager(context, true);
            try {
                audio.resume();
                assertTrue(audio.isBackgroundPlaying());
                assertFalse(audio.isBossPresent());
                audio.setBossPresent(true);
                assertTrue(audio.isBossPresent());
                assertTrue(audio.isBackgroundPlaying());
                audio.pause();
                assertFalse(audio.isBackgroundPlaying());
                audio.setBossPresent(false);
                assertFalse(audio.isBackgroundPlaying());
                audio.resume();
                assertTrue(audio.isBackgroundPlaying());
                audio.gameOver();
                assertFalse(audio.isBackgroundPlaying());
                audio.resume();
                assertFalse(audio.isBackgroundPlaying());
            } finally {
                audio.release();
                audio.release();
                audio.resume();
                assertFalse(audio.isBackgroundPlaying());
            }
        });
    }

    @Test public void disabledAudioRemainsSilentAcrossAllTransitions() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            GameAudioManager audio = new GameAudioManager(context, false);
            audio.resume();
            audio.setBossPresent(true);
            audio.playEffect(R.raw.bullet_hit);
            assertFalse(audio.isBackgroundPlaying());
            assertEquals(0, audio.playEffect(R.raw.get_supply));
            audio.pause();
            audio.gameOver();
            audio.release();
            assertEquals(0, audio.playEffect(R.raw.bullet_hit));
        });
    }
}
