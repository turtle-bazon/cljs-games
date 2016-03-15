package ru.bazon.androidtemplate;

import android.app.Activity;

import org.xwalk.core.XWalkExtension;

public class ExitAppExtension extends XWalkExtension {
    private static String name = "exitAppExtension";
    public static String JS_API_PATH = "exit_app_extension.js";

    private Activity activity;

    public ExitAppExtension(String jsApi, Activity activity) {
        super(name, jsApi);
        this.activity = activity;
    }

    @Override
    public void onMessage(int i, String s) {
        activity.finish();
    }

    @Override
    public String onSyncMessage(int i, String s) {
        activity.finish();
        return null;
    }
}
