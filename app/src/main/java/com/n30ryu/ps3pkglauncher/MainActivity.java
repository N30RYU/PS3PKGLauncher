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

private void showWelcomeScreen() { runOnUiThread(() -> { setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ); // ========================================================= // ROOT // ========================================================= LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER_HORIZONTAL); root.setPadding(45, 20, 45, 20); root.setBackgroundColor(Color.BLACK); // ========================================================= // MAIN CONTENT // ========================================================= LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.HORIZONTAL); content.setGravity(Gravity.CENTER); content.setWeightSum(10); root.addView( content, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, 0, 1 ) ); // ========================================================= // LEFT // ========================================================= LinearLayout left = new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL); left.setGravity(Gravity.CENTER_HORIZONTAL); // Large icon.png ImageView icon = new ImageView(this); icon.setImageResource(R.drawable.icon); icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE); left.addView( icon, new LinearLayout.LayoutParams( 340, 280 ) ); // PS3 PKG Launcher TextView title = new TextView(this); title.setText("PS3 PKG Launcher"); title.setTextColor(Color.WHITE); title.setTextSize(24); title.setTypeface(Typeface.DEFAULT, Typeface.BOLD); title.setGravity(Gravity.CENTER); left.addView( title, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) ); // by N30RYU TextView author = new TextView(this); author.setText("by N30RYU"); author.setTextColor(Color.LTGRAY); author.setTextSize(16); author.setGravity(Gravity.CENTER); left.addView( author, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) ); LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams( 390, LinearLayout.LayoutParams.MATCH_PARENT ); leftParams.gravity = Gravity.CENTER_VERTICAL; content.addView(left, leftParams); // ========================================================= // RIGHT // ========================================================= LinearLayout right = new LinearLayout(this); right.setOrientation(LinearLayout.VERTICAL); right.setGravity(Gravity.CENTER_VERTICAL); right.setPadding(35, 0, 0, 0); TextView description = new TextView(this); description.setText( "This app is designed to be used with Beacon Launcher.\n\n" + "In Beacon, select PS3 PKG Launcher as your PS3 application, " + "enable Custom Launch, and use this command:" ); description.setTextColor(Color.LTGRAY); description.setTextSize(17); description.setGravity(Gravity.CENTER_VERTICAL); right.addView( description, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) ); // ========================================================= // COMMAND + COPY // ========================================================= LinearLayout commandRow = new LinearLayout(this); commandRow.setOrientation(LinearLayout.HORIZONTAL); commandRow.setGravity(Gravity.CENTER_VERTICAL); LinearLayout.LayoutParams commandRowParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, 78 ); commandRowParams.setMargins(0, 18, 0, 0); TextView command = new TextView(this); command.setText(BEACON_COMMAND); command.setTextColor(Color.WHITE); command.setTextSize(12); command.setTypeface(Typeface.MONOSPACE); command.setGravity(Gravity.CENTER_VERTICAL); command.setPadding(15, 0, 15, 0); command.setBackgroundColor(Color.rgb(30, 30, 30)); commandRow.addView( command, new LinearLayout.LayoutParams( 0, LinearLayout.LayoutParams.MATCH_PARENT, 1 ) ); Button copyButton = new Button(this); copyButton.setText("COPY"); copyButton.setTextSize(16); copyButton.setAllCaps(false); copyButton.setGravity(Gravity.CENTER); copyButton.setMinHeight(68); copyButton.setMinimumHeight(68); LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams( 150, 68 ); copyParams.setMargins(15, 0, 0, 0); commandRow.addView(copyButton, copyParams); copyButton.setOnClickListener(v -> { ClipboardManager clipboard = (ClipboardManager) getSystemService( Context.CLIPBOARD_SERVICE ); ClipData clip = ClipData.newPlainText( "Beacon Custom Launch", BEACON_COMMAND ); clipboard.setPrimaryClip(clip); Toast.makeText( this, "Command copied to clipboard.", Toast.LENGTH_SHORT ).show(); }); right.addView(commandRow, commandRowParams); LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams( 0, LinearLayout.LayoutParams.MATCH_PARENT, 6 ); rightParams.gravity = Gravity.CENTER_VERTICAL; content.addView(right, rightParams); // ========================================================= // CLOSE // ========================================================= Button closeButton = new Button(this); closeButton.setText("CLOSE"); closeButton.setTextSize(16); closeButton.setAllCaps(false); closeButton.setGravity(Gravity.CENTER); closeButton.setMinHeight(68); closeButton.setMinimumHeight(68); closeButton.setOnClickListener(v -> finishAndRemoveTask() ); LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams( 220, 68 ); closeParams.gravity = Gravity.CENTER_HORIZONTAL; closeParams.setMargins(0, 10, 0, 0); root.addView(closeButton, closeParams); setContentView(root); }); }

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
