package edu.hitsz;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.hitsz.application.BaseGame;
import edu.hitsz.application.Difficulty;
import edu.hitsz.application.EasyGame;
import edu.hitsz.application.MediumGame;
import edu.hitsz.application.HardGame;
import edu.hitsz.application.GameEvent;
import edu.hitsz.music.GameAudioManager;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_SOUND_ENABLED = "sound_enabled";
    private BaseGame game;
    private Difficulty difficulty;
    private GameAudioManager audio;
    private Handler gameHandler;
    private boolean resumed;
    private Integer finalScore;
    private AlertDialog resultDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_root), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        difficulty = Difficulty.fromExtra(getIntent().getStringExtra(EXTRA_DIFFICULTY));
        ((TextView) findViewById(R.id.game_difficulty)).setText(difficulty.getLabel());
        findViewById(R.id.leave_game).setOnClickListener(view -> finish());
        audio = new GameAudioManager(this, getIntent().getBooleanExtra(EXTRA_SOUND_ENABLED, true));
        game = createGame();

        gameHandler = new Handler(Looper.getMainLooper(), message -> {
            if (isFinishing() || isDestroyed()) return true;
            switch (message.what) {
                case GameEvent.HIT: audio.playEffect(R.raw.bullet_hit); break;
                case GameEvent.SUPPLY: audio.playEffect(R.raw.get_supply); break;
                case GameEvent.BOMB: audio.playEffect(R.raw.bomb_explosion); break;
                case GameEvent.BOSS: audio.setBossPresent(message.arg1 != 0); break;
                case GameEvent.GAME_OVER:
                    if (finalScore == null) {
                        finalScore = message.arg1;
                        audio.gameOver();
                        showResult();
                    }
                    break;
                default: return false;
            }
            return true;
        });
        game.setEventHandler(gameHandler);
        ((FrameLayout) findViewById(R.id.game_container)).addView(game);
        if (savedInstanceState != null && savedInstanceState.containsKey("final_score")) {
            finalScore = savedInstanceState.getInt("final_score");
            audio.gameOver();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        resumed = true;
        if (finalScore == null && game.isGameOver()) {
            finalScore = game.getScore();
            audio.gameOver();
        }
        // 结算页也恢复前台状态；finished 会阻止音乐，重开时则可立即播放。
        audio.resume();
        if (finalScore == null) {
            audio.setBossPresent(game.hasBoss());
            game.resumeGame();
        }
        showResult();
    }

    @Override protected void onPause() {
        resumed = false;
        game.pauseGame();
        audio.pause();
        for (int event = GameEvent.HIT; event <= GameEvent.BOSS; event++) gameHandler.removeMessages(event);
        super.onPause();
    }

    private void showResult() {
        if (!resumed || finalScore == null || isFinishing() || isDestroyed()
                || (resultDialog != null && resultDialog.isShowing())) return;
        resultDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.game_over)
                .setMessage(getString(R.string.game_result, difficulty.getLabel(), finalScore))
                .setCancelable(false)
                .setPositiveButton(R.string.play_again, (dialog, which) -> startNewRound())
                .setNegativeButton(R.string.leave_game, (dialog, which) -> finish())
                .show();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        if (finalScore != null) outState.putInt("final_score", finalScore);
        else if (game.isGameOver()) outState.putInt("final_score", game.getScore());
        super.onSaveInstanceState(outState);
    }

    private BaseGame createGame() {
        if (difficulty == Difficulty.HARD) return new HardGame(this);
        if (difficulty == Difficulty.MEDIUM) return new MediumGame(this);
        return new EasyGame(this);
    }

    private void startNewRound() {
        game.pauseGame();
        game.setEventHandler(null);
        gameHandler.removeCallbacksAndMessages(null);
        FrameLayout container = findViewById(R.id.game_container);
        container.removeAllViews();
        if (resultDialog != null) {
            resultDialog.dismiss();
            resultDialog = null;
        }
        finalScore = null;
        audio.resetForNewGame();
        game = createGame();
        game.setEventHandler(gameHandler);
        container.addView(game);
        if (resumed) game.resumeGame();
    }

    @Override protected void onDestroy() {
        game.pauseGame();
        game.setEventHandler(null);
        gameHandler.removeCallbacksAndMessages(null);
        if (resultDialog != null) resultDialog.dismiss();
        audio.release();
        super.onDestroy();
    }
}
