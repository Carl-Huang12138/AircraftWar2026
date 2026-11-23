package edu.hitsz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.application.EasyGame;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 实验二直接加载游戏 SurfaceView 视图
        setContentView(new EasyGame(this));
    }
}
