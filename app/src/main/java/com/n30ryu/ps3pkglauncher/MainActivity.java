package com.n30ryu.ps3pkglauncher;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private static final String ARMSX3_ACTIVITY =
            "com.armsx3/com.armsx2.MainActivity";

    private static final String BEACON_COMMAND =
            "am start -n com.n30ryu.ps3pkglauncher/com.n30ryu.ps3pkglauncher.MainActivity -e file_path {file_path}";

    // Files up to 100 KB are treated as fake ISO markers.
    private static final long MARKER_MAX_SIZE = 100 * 1024;

    private static final Pattern TITLE_ID =
            Pattern.compile("\\b[A-Za-z0-9]{4}\\d{5}\\b");

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        // PS3 PKG Launcher is designed for the Odin 3 in landscape mode.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Intent intent = getIntent();

        String filePath = getFilePath(intent);

        // No game was supplied -> show the information screen.
        if (filePath == null || filePath.trim().isEmpty()) {
            showWelcomeScreen();
            return;
        }

        handleIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        String filePath = getFilePath(intent);

        if (filePath == null || filePath.trim().isEmpty()) {
            showWelcomeScreen();
            return;
        }

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        new Thread(() -> {

            try {

                String filePath = getFilePath(intent);

                if (filePath == null || filePath.trim().isEmpty()) {
                    showWelcomeScreen();
                    return;
                }

                File file = new File(filePath);

                if (!file.exists()) {
                    toast("File not found:\n" + filePath);
                    finishSafe();
                    return;
                }

                long size = file.length();

                if (size > MARKER_MAX_SIZE) {

                    // Real PS3 ISO -> open directly in ARMSX3.
                    launchArmsx3Iso(filePath);

                } else {

                    // Small file -> read Title ID.
                    String text = readText(file);

                    String titleId = extractTitleId(text);

                    if (titleId == null) {
                        toast("Title ID not found in:\n" + file.getName());
                        finishSafe();
                        return;
                    }

                    launchArmsx3TitleId(titleId);
                }

            } catch (Exception e) {

                toast("PS3 PKG Launcher:\n" +
                        (e.getMessage() != null
                                ? e.getMessage()
                                : "Unknown error"));

                finishSafe();
            }

        }).start();
    }

    private String getFilePath(Intent intent) {

        if (intent == null) {
            return null;
        }

        // Main method used by Beacon.
        String path = intent.getStringExtra("file_path");

        if (path != null && !path.trim().isEmpty()) {
            return normalizePath(path);
        }

        // Compatibility with other launch methods.
        Uri uri = intent.getData();

        if (uri != null) {

            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }

            if (uri.getScheme() == null) {
                return uri.toString();
            }
        }

        String[] keys = {
                "path",
                "game_path",
                "rom_path",
                "file",
                "file_uri",
                "uri"
        };

        for (String key : keys) {

            String value = intent.getStringExtra(key);

            if (value != null && !value.trim().isEmpty()) {
                return normalizePath(value);
            }
        }

        return null;
    }

    private String normalizePath(String path) {

        path = path.trim();

        if (path.startsWith("file://")) {
            return Uri.parse(path).getPath();
        }

        return path;
    }

    private String readText(File file) throws Exception {

        try (InputStream input = new FileInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];

            int read;

            while ((read = input.read(buffer)) != -1) {

                output.write(buffer, 0, read);

                if (output.size() > MARKER_MAX_SIZE) {
                    break;
                }
            }

            return new String(
                    output.toByteArray(),
                    StandardCharsets.UTF_8
            );
        }
    }

    private String extractTitleId(String text) {

        if (text == null) {
            return null;
        }

        Matcher matcher =
                TITLE_ID.matcher(text.toUpperCase(Locale.ROOT));

        if (matcher.find()) {
            return matcher.group().toUpperCase(Locale.ROOT);
        }

        return null;
    }

    private void launchArmsx3Iso(String filePath) {

        runOnUiThread(() -> {

            try {

                Intent launch = new Intent(Intent.ACTION_VIEW);

                launch.setComponent(
                        ComponentName.unflattenFromString(ARMSX3_ACTIVITY)
                );

                launch.setDataAndType(
                        Uri.parse("file://" + filePath),
                        "application/octet-stream"
                );

                launch.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                );

                startActivity(launch);

                finishAndRemoveTask();

            } catch (Exception e) {

                toast("Could not open the ISO in ARMSX3.");
                finishAndRemoveTask();
            }
        });
    }

    private void launchArmsx3TitleId(String titleId) {

        runOnUiThread(() -> {

            try {

                Intent launch = new Intent(Intent.ACTION_MAIN);

                launch.setComponent(
                        ComponentName.unflattenFromString(ARMSX3_ACTIVITY)
                );

                launch.putExtra("title_id", titleId);

                launch.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                );

                startActivity(launch);

                finishAndRemoveTask();

            } catch (Exception e) {

                toast("Could not launch ARMSX3.");
                finishAndRemoveTask();
            }
        });
    }

    private void showWelcomeScreen() {

        runOnUiThread(() -> {

            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            );

            // Root layout.
            LinearLayout root = new LinearLayout(this);
            root.setOrientation(LinearLayout.HORIZONTAL);
            root.setGravity(Gravity.CENTER);
            root.setPadding(60, 35, 60, 35);
            root.setBackgroundColor(Color.BLACK);

            // Left side: icon.
            LinearLayout left = new LinearLayout(this);
            left.setOrientation(LinearLayout.VERTICAL);
            left.setGravity(Gravity.CENTER);

            ImageView icon = new ImageView(this);
            icon.setImageResource(R.mipmap.ic_launcher);
            icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            left.addView(
                    icon,
                    new LinearLayout.LayoutParams(
                            260,
                            260
                    )
            );

            TextView title = new TextView(this);
            title.setText("PS3 PKG Launcher");
            title.setTextColor(Color.WHITE);
            title.setTextSize(25);
            title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            title.setGravity(Gravity.CENTER);

            left.addView(
                    title,
                    new LinearLayout.LayoutParams(
                            300,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );

            root.addView(
                    left,
                    new LinearLayout.LayoutParams(
                            330,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    )
            );

            // Right side.
            LinearLayout right = new LinearLayout(this);
            right.setOrientation(LinearLayout.VERTICAL);
            right.setGravity(Gravity.CENTER_VERTICAL);

            TextView description = new TextView(this);

            description.setText(
                    "This app is designed to be used with Beacon Launcher.\n\n" +
                    "In Beacon, select PS3 PKG Launcher as your PS3 application, " +
                    "enable Custom Launch, and use this command:"
            );

            description.setTextColor(Color.LTGRAY);
            description.setTextSize(18);
            description.setGravity(Gravity.CENTER_VERTICAL);

            right.addView(
                    description,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );

            // Command box.
            TextView command = new TextView(this);

            command.setText(BEACON_COMMAND);
            command.setTextColor(Color.WHITE);
            command.setTextSize(15);
            command.setTypeface(Typeface.MONOSPACE);
            command.setGravity(Gravity.CENTER_VERTICAL);
            command.setPadding(20, 15, 20, 15);
            command.setBackgroundColor(Color.rgb(30, 30, 30));

            LinearLayout.LayoutParams commandParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            commandParams.setMargins(0, 20, 0, 20);

            right.addView(command, commandParams);

            // Buttons.
            LinearLayout buttons = new LinearLayout(this);
            buttons.setOrientation(LinearLayout.HORIZONTAL);
            buttons.setGravity(Gravity.CENTER);

            Button copyButton = new Button(this);
            copyButton.setText("Copy Command");
            copyButton.setTextSize(16);

            copyButton.setOnClickListener(v -> {

                ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(
                                Context.CLIPBOARD_SERVICE
                        );

                ClipData clip =
                        ClipData.newPlainText(
                                "Beacon Custom Launch",
                                BEACON_COMMAND
                        );

                clipboard.setPrimaryClip(clip);

                Toast.makeText(
                        this,
                        "Command copied to clipboard.",
                        Toast.LENGTH_SHORT
                ).show();
            });

            Button closeButton = new Button(this);
            closeButton.setText("Close");
            closeButton.setTextSize(16);

            closeButton.setOnClickListener(v ->
                    finishAndRemoveTask()
            );

            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(
                            220,
                            65
                    );

            buttonParams.setMargins(10, 0, 10, 0);

            buttons.addView(copyButton, buttonParams);
            buttons.addView(closeButton, buttonParams);

            right.addView(buttons);

            root.addView(
                    right,
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    )
            );

            setContentView(root);
        });
    }

    private void toast(String message) {

        runOnUiThread(() ->
                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show()
        );
    }

    private void finishSafe() {
        runOnUiThread(this::finishAndRemoveTask);
    }
}
