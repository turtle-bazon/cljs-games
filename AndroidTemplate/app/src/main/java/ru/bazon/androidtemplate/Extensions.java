package ru.bazon.androidtemplate;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import org.xwalk.core.XWalkExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class Extensions {
    private static final String TAG = "Extensions";
    private static HashMap<String, XWalkExtension> sBuiltinExtensions =
            new HashMap<String, XWalkExtension>();

    public static void load(Context context, FullscreenActivity activity) {
        {
            String jsApiContent = "";
            try {
                jsApiContent = getExtensionJSFileContent(
                        context, LaunchScreenExtension.JS_API_PATH, true);
                sBuiltinExtensions.put(LaunchScreenExtension.JS_API_PATH,
                        new LaunchScreenExtension(jsApiContent, activity));
            } catch (IOException e) {
                Log.w(TAG, "Failed to read JS API file: " + LaunchScreenExtension.JS_API_PATH);
            }
        }
        {
            String jsApiContent = "";
            try {
                jsApiContent = getExtensionJSFileContent(
                        context, ExitAppExtension.JS_API_PATH, true);
                sBuiltinExtensions.put(ExitAppExtension.JS_API_PATH,
                        new ExitAppExtension(jsApiContent, activity));
            } catch (IOException e) {
                Log.w(TAG, "Failed to read JS API file: " + LaunchScreenExtension.JS_API_PATH);
            }
        }
    }

    private static String getExtensionJSFileContent(Context context, String fileName, boolean fromRaw)
            throws IOException {
        String result = "";
        InputStream inputStream = null;
        try {
            if (fromRaw) {
                // If fromRaw is true, Try to find js file in res/raw first.
                // And then try to get it from assets if failed.
                Resources resource = context.getResources();
                String resName = (new File(fileName).getName().split("\\."))[0];
                int resId = resource.getIdentifier(resName, "raw", context.getPackageName());
                if (resId > 0) {
                    try {
                        inputStream = resource.openRawResource(resId);
                    } catch (Resources.NotFoundException e) {
                        Log.w(TAG, "Inputstream failed to open for R.raw." + resName +
                                ", try to find it in assets");
                    }
                }
            }
            if (inputStream == null) {
                AssetManager assetManager = context.getAssets();
                inputStream = assetManager.open(fileName);
            }
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            result = new String(buffer);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return result;
    }
}
