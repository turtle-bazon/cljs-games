package ru.bazon.androidtemplate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ViewSwitcher;

import org.xwalk.core.XWalkDownloadListener;
import org.xwalk.core.XWalkView;

public class FullscreenActivity extends AppCompatActivity {
    // default
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    // crosswalk
    private XWalkView mXWalkView;

    // viewSwitcher
    private ViewSwitcher switcher;
    private static final int REFRESH_SCREEN = 1;

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            switcher.showNext();

//            Intent intent = new Intent(FullscreenActivity.this, MainActivity.class);
//            startActivity(intent);
//            FullscreenActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mContentView = findViewById(R.id.profileSwitcher);

        // crosswalk
        mXWalkView = (XWalkView) findViewById(R.id.activity_main);
        mXWalkView.load("file:///android_asset/www/index.html", null);

        // viewSwitcher
        switcher = (ViewSwitcher) findViewById(R.id.profileSwitcher);
//        startScan();

        // app
        delayedRun(8000);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hide();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedRun(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
