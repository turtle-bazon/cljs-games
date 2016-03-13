package ru.bazon.androidtemplate;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;

import org.xwalk.core.XWalkView;

import java.lang.reflect.Method;

public class FullscreenActivity extends AppCompatActivity {
    private final Handler mHideSplashHandler = new Handler();
    private View mContentView;
    private XWalkView mXWalkView;
    private LaunchScreenManager launchScreenManager;

    private final Runnable mShowGameRunnable = new Runnable() {
        @Override
        public void run() {
            launchScreenManager.performHideLaunchScreen();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mContentView = findViewById(R.id.fullscreen_view);

        // crosswalk
        mXWalkView = (XWalkView) findViewById(R.id.activity_main);
        Point realSize = getDeviceRealSize();
        mXWalkView.evaluateJavascript(String.format("var mobile = true; var deviceWidth = %s; var deviceHeight = %s;",
                realSize.x, realSize.y), null);
        mXWalkView.load("file:///android_asset/www/index.html", null);

        hide();

        launchScreenManager = new LaunchScreenManager(this);
        launchScreenManager.displayLaunchScreen("complete", "");
        delayedRun(5000);
    }

    private Point getDeviceRealSize() {
        Display display = getWindowManager().getDefaultDisplay();
        int realWidth;
        int realHeight;

        if (Build.VERSION.SDK_INT >= 17) {
            //new pleasant way to get real metrics
            DisplayMetrics realMetrics = new DisplayMetrics();
            display.getRealMetrics(realMetrics);
            realWidth = realMetrics.widthPixels;
            realHeight = realMetrics.heightPixels;

        } else if (Build.VERSION.SDK_INT >= 14) {
            //reflection for this weird in-between time
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                //this may not be 100% accurate, but it's all we've got
                realWidth = display.getWidth();
                realHeight = display.getHeight();
                Log.e("Display Info", "Couldn't use reflection to get the real display metrics.");
            }

        } else {
            //This should be close, as lower API devices should not have window navigation bars
            realWidth = display.getWidth();
            realHeight = display.getHeight();
        }
        return new Point(realWidth, realHeight);
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
        mHideSplashHandler.removeCallbacks(mShowGameRunnable);
        mHideSplashHandler.postDelayed(mShowGameRunnable, delayMillis);
    }
}
