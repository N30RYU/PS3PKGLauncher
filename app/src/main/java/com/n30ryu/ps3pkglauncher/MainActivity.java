package com.n30ryu.ps3pkglauncher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        showDiagnostic(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showDiagnostic(intent);
    }

    private void showDiagnostic(Intent intent) {

        StringBuilder info = new StringBuilder();

        info.append("=== PS3 PKG LAUNCHER DIAGNOSTIC ===\n\n");

        info.append("ACTION:\n");
        info.append(String.valueOf(intent.getAction()));
        info.append("\n\n");

        info.append("DATA:\n");
        info.append(String.valueOf(intent.getDataString()));
        info.append("\n\n");

        info.append("TYPE:\n");
        info.append(String.valueOf(intent.getType()));
        info.append("\n\n");

        info.append("FLAGS:\n0x");
        info.append(Integer.toHexString(intent.getFlags()));
        info.append("\n\n");

        info.append("COMPONENT:\n");
        info.append(String.valueOf(intent.getComponent()));
        info.append("\n\n");

        info.append("PACKAGE:\n");
        info.append(String.valueOf(intent.getPackage()));
        info.append("\n\n");

        info.append("REFERRER:\n");
        try {
            info.append(String.valueOf(getReferrer()));
        } catch (Exception e) {
            info.append("ERROR: ").append(e.getMessage());
        }
        info.append("\n\n");

        info.append("CATEGORIES:\n");
        if (intent.getCategories() == null) {
            info.append("NONE\n");
        } else {
            for (String category : intent.getCategories()) {
                info.append(category).append("\n");
            }
        }
        info.append("\n");

        info.append("EXTRA_STREAM:\n");
        try {
            info.append(String.valueOf(
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
            ));
        } catch (Exception e) {
            info.append("ERROR: ").append(e.getMessage());
        }
        info.append("\n\n");

        info.append("EXTRA_TEXT:\n");
        try {
            info.append(String.valueOf(
                    intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
            ));
        } catch (Exception e) {
            info.append("ERROR: ").append(e.getMessage());
        }
        info.append("\n\n");

        info.append("ALL EXTRAS:\n");

        Bundle extras = intent.getExtras();

        if (extras == null || extras.isEmpty()) {
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
            info.append("\n");
        }

        info.append("\n");
        info.append("=== PS3 DIRECTORY ===\n\n");

        File ps3Dir = new File(
                "/storage/emulated/0/roms/ps3/"
        );

        info.append("EXISTS: ");
        info.append(ps3Dir.exists());
        info.append("\n");

        info.append("READABLE: ");
        info.append(ps3Dir.canRead());
        info.append("\n\n");

        if (ps3Dir.exists() && ps3Dir.isDirectory()) {

            File[] files = ps3Dir.listFiles();

            if (files == null) {
                info.append("listFiles() = NULL\n");
            } else {

                Arrays.sort(
                        files,
                        Comparator.comparing(File::getName)
                );

                for (File file : files) {

                    if (file.getName()
                            .toLowerCase()
                            .endsWith(".iso")) {

                        info.append(file.getName());
                        info.append("\n");

                        info.append("  path: ");
                        info.append(file.getAbsolutePath());
                        info.append("\n");

                        info.append("  size: ");
                        info.append(file.length());
                        info.append("\n");

                        info.append("  modified: ");
                        info.append(file.lastModified());
                        info.append("\n\n");
                    }
                }
            }
        }

        info.append("=== END DIAGNOSTIC ===");

        TextView tv = new TextView(this);

        tv.setText(info.toString());
        tv.setTextSize(15);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(30, 30, 30, 30);
        tv.setGravity(Gravity.START);
        tv.setTextIsSelectable(true);

        ScrollView scroll = new ScrollView(this);

        scroll.setBackgroundColor(Color.BLACK);

        scroll.addView(
                tv,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(scroll);
    }
}
