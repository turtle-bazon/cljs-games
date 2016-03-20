package ru.bazongroup.bubbles;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class BubblesApplication extends Application {
    public String getApplicationLabel() {
        PackageManager packageManager = getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (String) (applicationInfo != null ?
                packageManager.getApplicationLabel(applicationInfo) : "(unknown)");
    }

    public String getVersionName() {
        PackageManager packageManager = getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (packageInfo != null) ?
                String.valueOf(packageInfo.versionName) : "(unknown)";
    }
}
