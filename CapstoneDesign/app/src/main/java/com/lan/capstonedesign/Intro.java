package com.lan.capstonedesign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by kslee7746 on 2016. 11. 30..
 */

public class Intro extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);

        // 액션 바 감추기
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();

        // 2초 후 인트로 액티비티 제거
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intro.this, MainActivity.class);
                startActivity(intent);

                finish();
            }
        }, 1500);
    }
}
