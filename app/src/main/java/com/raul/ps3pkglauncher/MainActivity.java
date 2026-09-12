package com.n30ryu.ps3pkglauncher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        showIntentInfo(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showIntentInfo(intent);
    }

    private void showIntentInfo(Intent intent) {

        StringBuilder info = new StringBuilder();

        info.append("=== BEACON INTENT DIAGNOSTIC ===\n\n");

        info.append("ACTION:\n");
        info.append(String.valueOf(intent.getAction()));
        info.append("\n\n");

        info.append("DATA:\n");
        info.append(String.valueOf(intent.getDataString()));
        info.append("\n\n");

        info.append("TYPE:\n");
        info.append(String.valueOf(intent.getType()));
        info.append("\n\n");

        info.append("FLAGS:\n");
        info.append("0x");
        info.append(Integer.toHexString(intent.getFlags()));
        info.append("\n\n");

        Uri data = intent.getData();

        if (data != null) {
            info.append("DATA SCHEME:\n");
            info.append(String.valueOf(data.getScheme()));
            info.append("\n\n");

            info.append("DATA PATH:\n");
            info.append(String.valueOf(data.getPath()));
            info.append("\n\n");
        }

        info.append("EXTRA_STREAM:\n");
        try {
            Object stream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            info.append(String.valueOf(stream));
        } catch (Exception e) {
            info.append("ERROR: ");
            info.append(e.getMessage());
        }
        info.append("\n\n");

        info.append("EXTRA_TEXT:\n");
        try {
            Object text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
            info.append(String.valueOf(text));
        } catch (Exception e) {
            info.append("ERROR: ");
            info.append(e.getMessage());
        }
        info.append("\n\n");

        info.append("ALL EXTRAS:\n");

        Bundle extras = intent.getExtras();

        if (extras == null) {
            info.append("NO EXTRAS\n");
        } else {
            for (String key : extras.keySet()) {
                info.append("\n");
                info.append(key);
                info.append(" = ");

                try {
                    Object value = extras.get(key);
                    info.append(String.valueOf(value));
                } catch (Exception e) {
                    info.append("ERROR: ");
                    info.append(e.getMessage());
                }
            }
        }

        info.append("\n\n=== END ===");

        TextView textView = new TextView(this);

        textView.setText(info.toString());
        textView.setTextSize(16);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(30, 30, 30, 30);
        textView.setGravity(Gravity.START);
        textView.setTextIsSelectable(true);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.BLACK);
        scrollView.addView(
                textView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(scrollView);
    }
}
