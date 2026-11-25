package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.switchmaterial.SwitchMaterial;
import edu.hitsz.application.Difficulty;

/** 菜单只负责收集选择，玩法与音频由游戏页面管理。 */
public class MenuActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menu_root), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        RadioGroup group = findViewById(R.id.difficulty_group);
        SwitchMaterial sound = findViewById(R.id.sound_enabled);
        findViewById(R.id.start_game).setOnClickListener(view -> {
            Difficulty difficulty = Difficulty.EASY;
            if (group.getCheckedRadioButtonId() == R.id.difficulty_medium) difficulty = Difficulty.MEDIUM;
            if (group.getCheckedRadioButtonId() == R.id.difficulty_hard) difficulty = Difficulty.HARD;
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra(MainActivity.EXTRA_DIFFICULTY, difficulty.name())
                    .putExtra(MainActivity.EXTRA_SOUND_ENABLED, sound.isChecked()));
        });
    }
}
