package ru.bazongroup.bubbles.extensions;

import android.app.Activity;

import org.xwalk.core.XWalkExtension;

import ru.bazongroup.bubbles.BubblesApplication;
import ru.bazongroup.bubbles.R;
import ru.bazongroup.bubbles.activities.AboutDialog;

public class AboutExtension extends XWalkExtension {
    private static String name = "aboutExtension";
    public static String JS_API_PATH = "extension_about.js";

    private Activity activity;

    public AboutExtension(String jsApi, Activity activity) {
        super(name, jsApi);
        this.activity = activity;
    }

    @Override
    public void onMessage(int i, String s) {
        showAboutDialogOnUiThread();
    }

    @Override
    public String onSyncMessage(int i, String s) {
        showAboutDialogOnUiThread();
        return null;
    }


    private void showAboutDialogOnUiThread() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAboutDialog();
            }
        });
    }

    private void showAboutDialog() {
        BubblesApplication app = (BubblesApplication) activity.getApplication();
        AboutDialog about = new AboutDialog(activity, app.getApplicationLabel(), app.getVersionName(),
                R.mipmap.ic_launcher);
        about.setTitle(R.string.about);
        about.show();
    }
}
