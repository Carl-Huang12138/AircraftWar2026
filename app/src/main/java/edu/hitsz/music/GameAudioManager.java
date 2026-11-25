package edu.hitsz.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;
import androidx.annotation.MainThread;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import edu.hitsz.R;

/** 由 Activity 主线程持有；不保存 Activity 引用，不访问游戏模型或界面。 */
@MainThread
public final class GameAudioManager {
    private static final String TAG = "GameAudioManager";
    private final Context context;
    private final boolean enabled;
    private final Map<Integer, Integer> samples = new HashMap<>();
    private final Set<Integer> loaded = new HashSet<>();
    private final ArrayDeque<Integer> streams = new ArrayDeque<>();
    private SoundPool soundPool;
    private MediaPlayer background;
    private MediaPlayer normalMusic;
    private MediaPlayer bossMusic;
    private boolean active;
    private boolean released;
    private boolean finished;
    private boolean bossPresent;
    private boolean pendingGameOver;

    public GameAudioManager(Context context, boolean enabled) {
        this.context = context.getApplicationContext();
        this.enabled = enabled;
        if (!enabled) return;
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        soundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(4).build();
        soundPool.setOnLoadCompleteListener((pool, sample, status) -> {
            if (released) return;
            if (status == 0) {
                loaded.add(sample);
                Integer endSample = samples.get(R.raw.game_over);
                if (pendingGameOver && endSample != null && sample == endSample) {
                    pendingGameOver = false;
                    playEffect(R.raw.game_over);
                }
            } else {
                Log.w(TAG, "SoundPool failed to load sample " + sample + ": " + status);
            }
        });
        for (int resource : new int[]{R.raw.bullet_hit, R.raw.bomb_explosion, R.raw.get_supply, R.raw.game_over}) {
            samples.put(resource, soundPool.load(this.context, resource, 1));
        }
    }

    public void resume() {
        if (released) return;
        active = true;
        if (enabled && !finished) startBackground();
    }

    public void pause() {
        active = false;
        pendingGameOver = false;
        if (released || !enabled) return;
        if (background != null && background.isPlaying()) background.pause();
        while (!streams.isEmpty()) soundPool.stop(streams.removeFirst());
    }

    public void setBossPresent(boolean present) {
        if (released || bossPresent == present) return;
        if (background != null && background.isPlaying()) background.pause();
        bossPresent = present;
        background = present ? bossMusic : normalMusic;
        if (enabled && active && !finished) startBackground();
    }

    private void startBackground() {
        if (background == null) {
            int resource = bossPresent ? R.raw.bgm_boss : R.raw.bgm;
            background = MediaPlayer.create(context, resource);
            if (background == null) {
                Log.e(TAG, "Unable to create background player");
                return;
            }
            background.setLooping(true);
            if (bossPresent) bossMusic = background;
            else normalMusic = background;
            background.setOnErrorListener((player, what, extra) -> {
                Log.e(TAG, "Background playback failed: " + what + "/" + extra);
                if (background == player) background = null;
                if (normalMusic == player) normalMusic = null;
                if (bossMusic == player) bossMusic = null;
                player.release();
                return true;
            });
        }
        if (!background.isPlaying()) background.start();
    }

    /** 返回 SoundPool 的真实 streamId；未就绪或关闭时返回 0，不重放过时碰撞。 */
    public int playEffect(int resource) {
        if (released || !enabled || !active || (finished && resource != R.raw.game_over)) return 0;
        Integer sample = samples.get(resource);
        if (sample == null || !loaded.contains(sample)) {
            if (resource == R.raw.game_over) pendingGameOver = true;
            return 0;
        }
        int stream = soundPool.play(sample, 1f, 1f, 1, 0, 1f);
        if (stream != 0) {
            streams.addLast(stream);
            if (streams.size() > 16) streams.removeFirst();
        }
        return stream;
    }

    public void gameOver() {
        if (released || finished) return;
        finished = true;
        if (background != null && background.isPlaying()) background.pause();
        playEffect(R.raw.game_over);
    }

    /** 明确开始新一局；复用已解码的音效与音乐，避免重开时再次加载。 */
    public void resetForNewGame() {
        if (released) return;
        pendingGameOver = false;
        if (soundPool != null) {
            while (!streams.isEmpty()) soundPool.stop(streams.removeFirst());
        }
        if (background != null && background.isPlaying()) background.pause();
        if (normalMusic != null) normalMusic.seekTo(0);
        if (bossMusic != null) bossMusic.seekTo(0);
        bossPresent = false;
        finished = false;
        background = normalMusic;
        if (enabled && active) startBackground();
    }

    public boolean isBackgroundPlaying() {
        return !released && background != null && background.isPlaying();
    }

    public boolean isBossPresent() { return bossPresent; }

    /** 当前背景曲目的播放位置，便于诊断暂停恢复行为。 */
    public int getBackgroundPositionMillis() {
        return !released && background != null ? background.getCurrentPosition() : 0;
    }

    private void releaseBackground() {
        if (normalMusic != null) normalMusic.release();
        if (bossMusic != null) bossMusic.release();
        normalMusic = null;
        bossMusic = null;
        background = null;
    }

    public void release() {
        if (released) return;
        pause();
        released = true;
        releaseBackground();
        if (soundPool != null) {
            soundPool.setOnLoadCompleteListener(null);
            soundPool.release();
            soundPool = null;
        }
        loaded.clear();
        samples.clear();
    }
}
