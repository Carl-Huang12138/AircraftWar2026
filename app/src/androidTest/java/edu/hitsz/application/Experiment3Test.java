package edu.hitsz.application;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Bitmap;
import android.widget.FrameLayout;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.io.File;
import java.io.FileOutputStream;

import edu.hitsz.MainActivity;
import edu.hitsz.MenuActivity;
import edu.hitsz.R;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.bullet.HeroBullet;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class Experiment3Test {
    @Test public void powerUpPropsActivateImmediatelyAndExpireAfterFiveSeconds() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent("EASY"))) {
            scenario.onActivity(activity -> {
                BaseGame game = game(activity);
                game.pauseGame();
                synchronized (game.gameLock) {
                    new edu.hitsz.prop.BulletProp(0, 0, 0, 0).active();
                    assertEquals(3, game.heroAircraft.shoot().size());
                    new edu.hitsz.prop.BulletPlusProp(0, 0, 0, 0).active();
                    assertEquals(20, game.heroAircraft.shoot().size());
                }
            });
            Thread.sleep(5_200);
            scenario.onActivity(activity -> {
                BaseGame game = game(activity);
                synchronized (game.gameLock) { assertEquals(1, game.heroAircraft.shoot().size()); }
            });
        }
    }

    @Test public void replayRestoresAudioAfterResultWasBackgrounded() throws Exception {
        assertReplayAudioAfterResultLifecycle(false);
    }

    @Test public void replayRestoresAudioAfterResultWasRecreated() throws Exception {
        assertReplayAudioAfterResultLifecycle(true);
    }

    private void assertReplayAudioAfterResultLifecycle(boolean recreate) throws Exception {
        Intent intent = gameIntent("EASY").putExtra(MainActivity.EXTRA_SOUND_ENABLED, true);
        AtomicReference<BaseGame> reference = new AtomicReference<>();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                BaseGame current = game(activity);
                reference.set(current);
                synchronized (current.gameLock) { current.heroAircraft.decreaseHp(Integer.MAX_VALUE); }
            });
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (!reference.get().isGameOver() && System.nanoTime() < deadline) Thread.sleep(20);
            assertTrue(reference.get().isGameOver());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            onView(withText("游戏结束")).inRoot(isDialog()).check(matches(isDisplayed()));
            if (recreate) scenario.recreate();
            else {
                scenario.moveToState(Lifecycle.State.CREATED);
                scenario.moveToState(Lifecycle.State.RESUMED);
            }
            // 等系统窗口过渡完成；仅等待主线程空闲不能保证触摸已可注入。
            InstrumentationRegistry.getInstrumentation().getUiAutomation().waitForIdle(500, 5000);
            onView(withText("再来一局")).inRoot(isDialog()).perform(click());
            AtomicInteger stream = new AtomicInteger();
            deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
            while (stream.get() == 0 && System.nanoTime() < deadline) {
                scenario.onActivity(activity -> {
                    assertTrue("Replay must reactivate music after result lifecycle", audio(activity).isBackgroundPlaying());
                    stream.set(audio(activity).playEffect(R.raw.bullet_hit));
                });
                Thread.sleep(30);
            }
            assertTrue("Replay must reactivate effects after result lifecycle", stream.get() > 0);
        }
    }

    private edu.hitsz.music.GameAudioManager audio(MainActivity activity) {
        try {
            java.lang.reflect.Field field = MainActivity.class.getDeclaredField("audio");
            field.setAccessible(true);
            return (edu.hitsz.music.GameAudioManager) field.get(activity);
        } catch (ReflectiveOperationException exception) { throw new AssertionError(exception); }
    }

    @Test public void profileColdAndWarmRoundsAcrossBackgroundMusicLoop() throws Exception {
        Intent intent = gameIntent("EASY").putExtra(MainActivity.EXTRA_SOUND_ENABLED, true);
        AtomicReference<BaseGame> reference = new AtomicReference<>();
        long launch = System.nanoTime();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            android.os.Bundle startup = new android.os.Bundle();
            startup.putString("profile", "cold_launch_ms=" + (System.nanoTime() - launch) / 1_000_000
                    + ", background_bytes=" + ImageManager.BACKGROUND_IMAGE.getAllocationByteCount());
            InstrumentationRegistry.getInstrumentation().sendStatus(0, startup);
            for (int round = 0; round < 2; round++) {
                scenario.onActivity(activity -> {
                    BaseGame game = game(activity);
                    reference.set(game);
                    synchronized (game.gameLock) { game.enemySpawnCounter = -100000; }
                });
                BaseGame current = reference.get();
                long start;
                int startTime;
                synchronized (current.gameLock) {
                    startTime = current.timeCount;
                    start = System.nanoTime();
                }
                int duration = round == 0 ? 35000 : 5000;
                int soundSuccess = 0;
                while (System.nanoTime() - start < TimeUnit.MILLISECONDS.toNanos(duration)) {
                    AtomicInteger stream = new AtomicInteger();
                    scenario.onActivity(activity -> {
                        assertTrue("Music must continue in round", audio(activity).isBackgroundPlaying());
                        stream.set(audio(activity).playEffect(R.raw.bullet_hit));
                    });
                    if (stream.get() > 0) soundSuccess++;
                    Thread.sleep(500);
                }
                long wall;
                int advanced;
                synchronized (current.gameLock) {
                    advanced = current.timeCount - startTime;
                    wall = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                }
                android.os.Bundle result = new android.os.Bundle();
                result.putString("profile", "round=" + round + ", wall_ms=" + wall + ", logic_ms=" + advanced
                        + ", effective_fps=" + (advanced / 40.0 * 1000 / wall) + ", effects=" + soundSuccess);
                InstrumentationRegistry.getInstrumentation().sendStatus(0, result);
                assertTrue(soundSuccess > 0);
                if (round == 0) {
                    scenario.onActivity(activity -> {
                        synchronized (current.gameLock) { current.heroAircraft.decreaseHp(Integer.MAX_VALUE); }
                    });
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
                    while (!current.isGameOver() && System.nanoTime() < deadline) Thread.sleep(20);
                    assertTrue(current.isGameOver());
                    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                    onView(withText("再来一局")).inRoot(isDialog()).perform(click());
                    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                }
            }
        }
    }

    @Test public void frameBudgetKeepsRealGameTimeCloseToWallTime() throws Exception {
        AtomicReference<BaseGame> reference = new AtomicReference<>();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent("EASY"))) {
            scenario.onActivity(activity -> {
                BaseGame game = game(activity);
                reference.set(game);
                synchronized (game.gameLock) { game.enemySpawnCounter = -100000; }
            });
            BaseGame game = reference.get();
            int before;
            long start;
            synchronized (game.gameLock) {
                before = game.timeCount;
                start = System.nanoTime();
            }
            Thread.sleep(3000);
            long wall;
            int advanced;
            synchronized (game.gameLock) {
                advanced = game.timeCount - before;
                wall = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            }
            android.os.Bundle result = new android.os.Bundle();
            result.putString("frame_budget", "wall=" + wall + ", advanced=" + advanced
                    + ", fps=" + advanced / 40.0 * 1000 / wall);
            InstrumentationRegistry.getInstrumentation().sendStatus(0, result);
            assertTrue("Draw time must fit inside the 40ms budget", advanced >= wall * 0.88);
            assertTrue("Game time must not run faster than real time", advanced <= wall * 1.10);
        }
    }

    @Test public void replayKeepsMusicEffectsAndGameLoopAliveForThreeRounds() throws Exception {
        Intent intent = gameIntent("EASY").putExtra(MainActivity.EXTRA_SOUND_ENABLED, true);
        AtomicReference<BaseGame> reference = new AtomicReference<>();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            for (int round = 0; round < 3; round++) {
                AtomicInteger stream = new AtomicInteger();
                long readyDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
                while (stream.get() == 0 && System.nanoTime() < readyDeadline) {
                    scenario.onActivity(activity -> {
                        reference.set(game(activity));
                        assertTrue("Music stopped after replay", audio(activity).isBackgroundPlaying());
                        stream.set(audio(activity).playEffect(R.raw.bullet_hit));
                    });
                    Thread.sleep(30);
                }
                assertTrue("Effects stopped after replay round " + round, stream.get() > 0);
                BaseGame current = reference.get();
                int before;
                synchronized (current.gameLock) { before = current.timeCount; }
                Thread.sleep(200);
                synchronized (current.gameLock) { assertTrue("New round must advance", current.timeCount > before); }
                scenario.onActivity(activity -> {
                    synchronized (current.gameLock) { current.heroAircraft.decreaseHp(Integer.MAX_VALUE); }
                });
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
                while (!current.isGameOver() && System.nanoTime() < deadline) Thread.sleep(20);
                assertTrue(current.isGameOver());
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                onView(withText("再来一局")).inRoot(isDialog()).perform(click());
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            }
        }
    }

    private Intent gameIntent(String difficulty) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return new Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_DIFFICULTY, difficulty)
                .putExtra(MainActivity.EXTRA_SOUND_ENABLED, false);
    }

    private BaseGame game(MainActivity activity) {
        return (BaseGame) ((FrameLayout) activity.findViewById(R.id.game_container)).getChildAt(0);
    }

    private void screenshot(String name) throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File directory = new File(context.getExternalFilesDir(null), "exp3-evidence");
        assertTrue(directory.isDirectory() || directory.mkdirs());
        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull(bitmap);
        try (FileOutputStream output = new FileOutputStream(new File(directory, name + ".png"))) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output));
        } finally { bitmap.recycle(); }
    }

    @Test public void menuStartsEveryDifficultyAndReturns() throws Exception {
        try (ActivityScenario<MenuActivity> scenario = ActivityScenario.launch(MenuActivity.class)) {
            int[] ids = {R.id.difficulty_easy, R.id.difficulty_medium, R.id.difficulty_hard};
            String[] names = {"简单模式", "普通模式", "困难模式"};
            onView(withId(R.id.sound_enabled)).check(matches(isChecked()));
            screenshot("menu");
            onView(withId(R.id.sound_enabled)).perform(click());
            for (int i = 0; i < ids.length; i++) {
                onView(withId(ids[i])).perform(scrollTo(), click());
                onView(withId(R.id.start_game)).perform(scrollTo(), click());
                onView(withId(R.id.game_difficulty)).check(matches(withText(names[i])));
                Thread.sleep(1200);
                screenshot(new String[]{"easy", "medium", "hard"}[i]);
                onView(withId(R.id.leave_game)).perform(click());
                onView(withId(R.id.start_game)).check(matches(isDisplayed()));
            }
        }
    }

    @Test public void actualSceneMatchesIntentAndInvalidValueFallsBack() {
        String[] values = {"EASY", "MEDIUM", "HARD", "invalid"};
        Class<?>[] classes = {EasyGame.class, MediumGame.class, HardGame.class, EasyGame.class};
        int[] hp = {150, 100, 80, 150};
        for (int i = 0; i < values.length; i++) {
            final int index = i;
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent(values[i]))) {
                scenario.onActivity(activity -> {
                    BaseGame game = game(activity);
                    assertEquals(classes[index], game.getClass());
                    synchronized (game.gameLock) { assertEquals(hp[index], game.heroAircraft.getHp()); }
                });
            }
        }
    }

    @Test public void heroFiresEvery240msInEveryModeEvenAfterDifficultyRises() {
        for (String mode : new String[]{"EASY", "MEDIUM", "HARD"}) {
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent(mode))) {
                scenario.onActivity(activity -> {
                    BaseGame game = game(activity);
                    game.pauseGame();
                    synchronized (game.gameLock) {
                        assertEquals(240, game.heroShootCycle * game.timeInterval, 0.001);
                        assertEquals("Initial enemy firing interval for " + mode,
                                "EASY".equals(mode) ? 30 : ("MEDIUM".equals(mode) ? 20 : 18),
                                game.enemyShootCycle, 0.001);
                        game.enemyAircraft.clear();
                        game.heroBullets.clear();
                        game.enemyBullets.clear();
                        game.heroShootCounter = 0;
                        game.enemySpawnCounter = 0;
                        game.timeCount = 0;
                        for (int i = 0; i < 30; i++) game.action();
                        assertEquals("Five shots in 1200ms: " + mode, 5, game.heroBullets.size());
                        game.timeCount = 10000;
                        game.updateDifficulty();
                        assertEquals("Difficulty must not slow the hero", 240,
                                game.heroShootCycle * game.timeInterval, 0.001);
                        assertTrue("Enemy frequency must still depend on difficulty",
                                game.enemyShootCycle >= 15 && game.enemyShootCycle <= 30);
                    }
                });
            }
        }
    }

    @Test public void tripleShotMatchesNormalCadenceWhileRingKeepsOriginalCadence() {
        for (String mode : new String[]{"EASY", "MEDIUM", "HARD"}) {
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent(mode))) {
                scenario.onActivity(activity -> {
                    BaseGame game = game(activity);
                    game.pauseGame();
                    synchronized (game.gameLock) {
                        int originalCycle = "EASY".equals(mode) ? 15 : ("MEDIUM".equals(mode) ? 20 : 22);
                        AtomicInteger shots = new AtomicInteger();
                        edu.hitsz.strategy.ShootStrategy[] special = {
                            new edu.hitsz.strategy.HeroScatterShootStrategy() {
                                @Override public java.util.List<edu.hitsz.bullet.BaseBullet> shoot(AbstractAircraft hero) {
                                    shots.incrementAndGet(); return super.shoot(hero);
                                }
                            },
                            new edu.hitsz.strategy.HeroRingShootStrategy() {
                                @Override public java.util.List<edu.hitsz.bullet.BaseBullet> shoot(AbstractAircraft hero) {
                                    shots.incrementAndGet(); return super.shoot(hero);
                                }
                            }
                        };
                        for (edu.hitsz.strategy.ShootStrategy strategy : special) {
                            int expectedCycle = strategy instanceof edu.hitsz.strategy.HeroScatterShootStrategy
                                    ? 6 : originalCycle;
                            game.heroAircraft.setShootStrategy(strategy);
                            game.enemyAircraft.clear();
                            game.enemyBullets.clear();
                            game.heroBullets.clear();
                            game.heroShootCounter = 0;
                            game.enemySpawnCounter = -10000;
                            game.timeCount = 0;
                            shots.set(0);
                            for (int i = 0; i < expectedCycle * 2; i++) game.action();
                            assertEquals("Two volleys at the mode's interval: " + mode, 2, shots.get());
                        }
                        game.heroAircraft.setShootStrategy(new edu.hitsz.strategy.HeroShootStrategy());
                        game.heroBullets.clear();
                        game.heroShootCounter = 0;
                        for (int i = 0; i < 30; i++) game.action();
                        assertEquals("Normal mode returns to 240ms", 5, game.heroBullets.size());
                    }
                });
            }
        }
    }

    @Test public void hardBossHasReducedHpAndFixedCadenceAboveMedium() {
        for (String mode : new String[]{"MEDIUM", "HARD"}) {
            boolean hard = "HARD".equals(mode);
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent(mode))) {
                scenario.onActivity(activity -> {
                    BaseGame game = game(activity);
                    game.pauseGame();
                    synchronized (game.gameLock) {
                        game.enemyAircraft.clear();
                        game.spawnBoss();
                        AbstractAircraft first = game.enemyAircraft.get(0);
                        assertEquals(hard ? 650 : 500, first.getHp());
                        game.enemyAircraft.clear();
                        game.bossExisting = false;
                        game.spawnBoss();
                        AbstractAircraft boss = game.enemyAircraft.get(0);
                        assertEquals(hard ? 800 : 500, boss.getHp());
                        AtomicInteger volleys = new AtomicInteger();
                        boss.setShootStrategy(aircraft -> {
                            volleys.incrementAndGet();
                            return java.util.Collections.emptyList();
                        });
                        game.heroAircraft.setShootStrategy(aircraft -> java.util.Collections.emptyList());
                        game.enemySpawnCounter = -100000;
                        game.enemyShootCounter = 0;
                        game.bossShootCounter = 0;
                        game.timeCount = 0;
                        for (int tick = 0; tick < 380; tick++) game.action();
                        assertEquals("Volleys in 15.2 seconds", hard ? 20 : 19, volleys.get());
                        assertEquals(hard ? 760 : 800, game.getBossShootCycle() * game.timeInterval, 0.001);
                    }
                });
            }
        }
    }

    @Test public void pauseStopsTimeAndWorkerThenResumeContinues() throws Exception {
        AtomicReference<BaseGame> reference = new AtomicReference<>();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent("EASY"))) {
            scenario.onActivity(activity -> reference.set(game(activity)));
            BaseGame game = reference.get();
            Thread.sleep(250);
            scenario.moveToState(Lifecycle.State.CREATED);
            int pausedTime;
            synchronized (game.gameLock) { pausedTime = game.timeCount; }
            Thread.sleep(200);
            synchronized (game.gameLock) {
                assertEquals(pausedTime, game.timeCount);
                assertFalse(game.mbLoop);
            }
            scenario.moveToState(Lifecycle.State.RESUMED);
            Thread.sleep(200);
            synchronized (game.gameLock) { assertTrue(game.timeCount > pausedTime); }
        }
        Thread.sleep(100);
        assertFalse(reference.get().mbLoop);
    }

    @Test public void deathDeliversOneScoreMessageOnMainThread() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<Looper> deliveredOn = new AtomicReference<>();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent("EASY"))) {
            scenario.onActivity(activity -> {
                BaseGame game = game(activity);
                game.setEventHandler(new Handler(Looper.getMainLooper(), message -> {
                    if (message.what == GameEvent.GAME_OVER) {
                        deliveredOn.set(Looper.myLooper());
                        assertEquals(137, message.arg1);
                        calls.incrementAndGet();
                        latch.countDown();
                    }
                    return true;
                }));
                synchronized (game.gameLock) {
                    game.score = 137;
                    game.heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            });
            assertTrue("No game-over message", latch.await(3, TimeUnit.SECONDS));
            Thread.sleep(150);
            assertEquals(1, calls.get());
            assertSame(Looper.getMainLooper(), deliveredOn.get());
        }
    }

    @Test public void resultDialogCanRestartWithFreshHero() throws Exception {
        AtomicReference<BaseGame> reference = new AtomicReference<>();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent("HARD"))) {
            scenario.onActivity(activity -> {
                BaseGame game = game(activity);
                reference.set(game);
                synchronized (game.gameLock) { game.heroAircraft.decreaseHp(Integer.MAX_VALUE); }
            });
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (!reference.get().isGameOver() && System.nanoTime() < deadline) Thread.sleep(20);
            assertTrue(reference.get().isGameOver());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            onView(withText("游戏结束")).inRoot(isDialog()).check(matches(isDisplayed()));
            screenshot("game-over-test");
            // 系统重建须保留结算；用户主动重开则必须清空。
            scenario.recreate();
            onView(withText("再来一局")).inRoot(isDialog()).perform(click());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                BaseGame game = game(activity);
                assertNotSame("Restart must replace the previous session", reference.get(), game);
                synchronized (game.gameLock) {
                    assertEquals(80, game.heroAircraft.getHp());
                    assertEquals(0, game.score);
                    assertFalse(game.gameOverFlag);
                }
            });
            onView(withId(R.id.game_difficulty)).check(matches(withText("困难模式")));
        }
    }

    @Test public void bodyCollisionWorksWithoutHeroBulletsAndOneBulletHitsOnlyOneEnemy() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent("EASY"))) {
            scenario.onActivity(activity -> {
                BaseGame game = game(activity);
                game.pauseGame();
                synchronized (game.gameLock) {
                    game.enemyAircraft.clear();
                    game.heroBullets.clear();
                    game.enemyBullets.clear();
                    game.heroAircraft.setLocation(500, 1500);
                    MobEnemy first = new MobEnemy(500, 500, 0, 0, 30);
                    MobEnemy second = new MobEnemy(500, 500, 0, 0, 30);
                    game.enemyAircraft.add(first);
                    game.enemyAircraft.add(second);
                    game.heroBullets.add(new HeroBullet(500, 500, 0, 0, 30));
                    game.action();
                    assertEquals(1, game.enemyAircraft.size());
                    assertEquals(30, second.getHp());
                    game.heroBullets.clear();
                    second.setLocation(500, 1500);
                    game.action();
                    assertEquals(0, game.heroAircraft.getHp());
                    assertTrue(game.isGameOver());
                }
            });
        }
    }

    @Test public void bossSpawnsDiesAndWaitsForNextScoreThreshold() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(gameIntent("MEDIUM"))) {
            scenario.onActivity(activity -> {
                BaseGame game = game(activity);
                game.pauseGame();
                synchronized (game.gameLock) {
                    game.score = 1000;
                    game.enemySpawnCounter = 1000;
                    game.action();
                    assertTrue(game.hasBoss());
                    AbstractAircraft boss = game.enemyAircraft.stream()
                            .filter(enemy -> enemy instanceof BossAircraft).findFirst().orElseThrow(AssertionError::new);
                    game.heroBullets.add(new HeroBullet(boss.getLocationX(), boss.getLocationY(), 0, 0, 10000));
                    game.action();
                    assertFalse(game.hasBoss());
                    game.enemySpawnCounter = 1000;
                    game.action();
                    assertFalse("Boss must wait for more score", game.hasBoss());
                    assertTrue(game.scoreThreshold > game.score);
                }
            });
        }
    }
}
