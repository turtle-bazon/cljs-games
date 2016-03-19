package ru.bazon.androidtemplate.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.ImageView;
import android.widget.TextView;

import ru.bazon.androidtemplate.R;
import ru.bazon.androidtemplate.helpers.RawTextReader;

public class AboutDialog extends Dialog {
    private final Context context;
    private final String appName;
    private final String versionName;
    private final int appIconId;

    public AboutDialog(Context context, String appName, String versionName, int appIconId) {
        super(context);
        this.context = context;
        this.appName = appName;
        this.versionName = versionName;
        this.appIconId = appIconId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setImage();
        setLegalText();
        setInfoText();
    }

    private void setImage() {
        setContentView(R.layout.about);
        ImageView ivAppIcon = (ImageView) findViewById(R.id.about_icon);
        ivAppIcon.setImageResource(appIconId);
    }

    private void setLegalText() {
        TextView tvLegal = (TextView) findViewById(R.id.legal_text);
        tvLegal.setText(Html.fromHtml(RawTextReader.readRawTextFile(context, R.raw.legal)));
        tvLegal.setLinkTextColor(tvLegal.getTextColors());
        Linkify.addLinks(tvLegal, Linkify.ALL);
    }

    private void setInfoText() {
        TextView tvInfoText = (TextView) findViewById(R.id.info_text);
        String infoText = RawTextReader.readRawTextFile(context, R.raw.info);
        infoText = infoText.replace("[appName]", appName).replace("[versionName]", versionName);
        tvInfoText.setText(Html.fromHtml(infoText));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            tvInfoText.setLinkTextColor(Color.WHITE);
        }
        Linkify.addLinks(tvInfoText, Linkify.ALL);
    }
}
