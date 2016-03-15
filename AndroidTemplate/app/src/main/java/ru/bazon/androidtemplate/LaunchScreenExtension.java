package ru.bazon.androidtemplate;

import android.app.Activity;

import org.xwalk.core.XWalkExtension;

import java.util.Date;

public class LaunchScreenExtension extends XWalkExtension {
    private static String name = "launchScreen";
    public static String JS_API_PATH = "launch_screen.js";

    private long createTime;
    private int showMs = 3000;

    private LaunchScreenManager launchScreenManager;

    public LaunchScreenExtension(String jsApi, Activity activity) {
        super(name, jsApi);
        launchScreenManager = new LaunchScreenManager(activity);
        launchScreenManager.displayLaunchScreen("complete", "");
        createTime = new Date().getTime();
    }

    @Override
    public void onMessage(int i, String s) {
        long curTime = new Date().getTime();
        long leftMs = createTime + showMs - curTime;
        long offsetMs = 100 < leftMs ? leftMs : 100;
        try {
            Thread.sleep(offsetMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        launchScreenManager.performHideLaunchScreen();
    }

    @Override
    public String onSyncMessage(int i, String s) {
        launchScreenManager.performHideLaunchScreen();
        return null;
    }
}
