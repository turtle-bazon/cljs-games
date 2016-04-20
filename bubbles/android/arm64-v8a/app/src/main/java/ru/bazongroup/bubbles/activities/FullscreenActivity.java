package ru.bazongroup.bubbles.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.xwalk.core.XWalkView;

import java.lang.reflect.Method;

import ru.bazongroup.bubbles.R;
import ru.bazongroup.bubbles.extensions.Extensions;

public class FullscreenActivity extends AppCompatActivity {
    private View contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        contentView = findViewById(R.id.fullscreen_view);

        hideSystemUi();

        // crosswalk
        org.xwalk.core.internal.extension.api.launchscreen.LaunchScreenExtension a;
        Extensions.load(this, this);
        XWalkView mXWalkView = (XWalkView) findViewById(R.id.activity_main);
        mXWalkView.evaluateJavascript(String.format("var mobile = true;"), null);
        mXWalkView.load("file:///android_asset/www/index.html", null);
//        mXWalkView.load("http://cljs-games.bazon.ru/bubbles/", null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUi();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void hideSystemUi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;

        View view = getWindow().getDecorView();
        int code = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        view.setSystemUiVisibility(code);
    }
}
