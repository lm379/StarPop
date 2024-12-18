package cn.lm379.starpop.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;


import cn.lm379.starpop.R;
import cn.lm379.starpop.interfaces.MainViewInterface;
import cn.lm379.starpop.view.MainView;

public class MainActivity extends AppCompatActivity implements MainViewInterface {

    private MediaPlayer media;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去除标题栏
        Objects.requireNonNull(getSupportActionBar()).hide();
        // 取消状态栏
        setContentView(R.layout.activity_main);
        MainView mainView = findViewById(R.id.myMainView);
        mainView.setMainViewInterface(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (media == null) {
            playMusic();
        } else {
            stopMusic();
            playMusic();
        }
    }

    @Override
    public void startGame() {
        Intent intent = new Intent();
        intent.setClass(this.getBaseContext(), GameActivity.class);
        startActivity(intent);
        stopMusic();
    }

    @Override
    public void exitGame() {
        stopMusic();
        finish();
    }

    private void playMusic() {
        media = MediaPlayer.create(getBaseContext(), R.raw.stardew_valley_overture);
        media.start();

        // 循环播放监听
        media.setOnCompletionListener(mp -> media.start());
    }

    private void stopMusic() {
        if (media.isPlaying()) {
            media.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMusic();
    }
}
