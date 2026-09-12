package com.n30ryu.ps3pkglauncher;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private static final String ARMSX3_ACTIVITY =
            "com.armsx3/com.armsx2.MainActivity";

    private static final String BEACON_COMMAND =
            "am start -n com.n30ryu.ps3pkglauncher/com.n30ryu.ps3pkglauncher.MainActivity -e file_path {file_path}";

    private static final long MARKER_MAX_SIZE = 100 * 1024;

    private static final Pattern TITLE_ID =
            Pattern.compile("\\b[A-Za-z0-9]{4}\\d{5}\\b");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        );

        Intent intent = getIntent();
        String filePath = getFilePath(intent);

        if (filePath == null || filePath.trim().isEmpty()) {
            showWelcomeScreen();
            return;
        }

        handleIntent(intent);
    }

    private String getFilePath(Intent intent) {
        if (intent == null) {
            return null;
        }

        String filePath = intent.getStringExtra("file_path");

        if (filePath == null || filePath.trim().isEmpty()) {
            filePath = intent.getStringExtra("path");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            Uri data = intent.getData();

            if (data != null) {
                filePath = data.getPath();
            }
        }

        return filePath;
    }

    private void handleIntent(Intent intent) {
        String filePath = getFilePath(intent);

        if (filePath == null || filePath.trim().isEmpty()) {
            showWelcomeScreen();
            return;
        }

        File file = new File(filePath);

        if (!file.exists()) {
            showError(
                    "File not found:\n\n" + filePath
            );
            return;
        }

        if (!file.isFile()) {
            showError(
                    "The selected path is not a file:\n\n" + filePath
            );
            return;
        }

        if (file.length() > MARKER_MAX_SIZE) {
            launchArmsx3Iso(filePath);
            return;
        }

        String titleId = readTitleId(file);

        if (titleId == null) {
            showError(
                    "The selected ISO is too small to be a real game image " +
                    "and no valid PS3 Title ID was found.\n\n" +
                    filePath
            );
            return;
        }

        launchArmsx3TitleId(titleId);
    }

    private String readTitleId(File file) {
        try {
            byte[] data = Files.readAllBytes(file.toPath());

            String text = new String(data);

            Matcher matcher = TITLE_ID.matcher(text);

            if (matcher.find()) {
                return matcher.group();
            }

        } catch (IOException ignored) {
        }

        return null;
    }

    private void launchArmsx3Iso(String filePath) {
        try {
            Intent launch = new Intent(Intent.ACTION_MAIN);

            launch.setComponent(
                    android.content.ComponentName.unflattenFromString(
                            ARMSX3_ACTIVITY
                    )
            );

            // This is the launch method from the previously working version.
            // Do NOT use file:// or setDataAndType() here.
            launch.putExtra("path", filePath);
            launch.putExtra("file_path", filePath);

            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(launch);

            finishAndRemoveTask();

        } catch (Exception e) {
            showError(
                    "Unable to launch ARMSX3.\n\n" +
                    e.getMessage()
            );
        }
    }

    private void launchArmsx3TitleId(String titleId) {
        try {
            Intent launch = new Intent(Intent.ACTION_MAIN);

            launch.setComponent(
                    android.content.ComponentName.unflattenFromString(
                            ARMSX3_ACTIVITY
                    )
            );

            launch.putExtra("title_id", titleId);

            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(launch);

            finishAndRemoveTask();

        } catch (Exception e) {
            showError(
                    "Unable to launch ARMSX3 for Title ID:\n\n" +
                    titleId +
                    "\n\n" +
                    e.getMessage()
            );
        }
    }

    private void showWelcomeScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        root.setPadding(
                45,
                20,
                45,
                20
        );

        root.setBackgroundColor(Color.BLACK);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER);
        content.setWeightSum(10);

        LinearLayout.LayoutParams contentParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        0.85f
                );

        root.addView(content, contentParams);

        // ---------------------------------------------------------
        // LEFT SIDE
        // ---------------------------------------------------------

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setGravity(Gravity.CENTER);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.icon);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(
                        340,
                        280
                );

        left.addView(icon, iconParams);

        TextView title = new TextView(this);
        title.setText("PS3 PKG Launcher");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        title.setGravity(Gravity.CENTER);

        left.addView(
                title,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        TextView author = new TextView(this);
        author.setText("by N30RYU");
        author.setTextColor(Color.LTGRAY);
        author.setTextSize(16);
        author.setGravity(Gravity.CENTER);

        left.addView(
                author,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout.LayoutParams leftParams =
                new LinearLayout.LayoutParams(
                        390,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );

        leftParams.gravity = Gravity.CENTER_VERTICAL;

        content.addView(left, leftParams);

        // ---------------------------------------------------------
        // RIGHT SIDE
        // ---------------------------------------------------------

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.CENTER_VERTICAL);

        right.setPadding(
                35,
                0,
                0,
                0
        );

        TextView description = new TextView(this);

        description.setText(
                "This app is designed to be used with Beacon Launcher.\n\n" +
                "In Beacon, select PS3 PKG Launcher as your PS3 application, " +
                "enable Custom Launch, and use this command:"
        );

        description.setTextColor(Color.LTGRAY);
        description.setTextSize(17);

        right.addView(
                description,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        // ---------------------------------------------------------
        // COMMAND + COPY BUTTON
        // ---------------------------------------------------------

        LinearLayout commandRow = new LinearLayout(this);
        commandRow.setOrientation(LinearLayout.HORIZONTAL);
        commandRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView command = new TextView(this);

        command.setText(BEACON_COMMAND);
        command.setTextColor(Color.WHITE);
        command.setTextSize(12);
        command.setSingleLine(false);
        command.setMaxLines(2);
        command.setTypeface(Typeface.MONOSPACE);
        command.setGravity(Gravity.CENTER_VERTICAL);

        command.setPadding(
                15,
                0,
                15,
                0
        );

        command.setBackgroundColor(
                Color.rgb(30, 30, 30)
        );

        LinearLayout.LayoutParams commandParams =
                new LinearLayout.LayoutParams(
                        0,
                        86,
                        1
                );

        commandRow.addView(
                command,
                commandParams
        );

        Button copyButton = new Button(this);

        copyButton.setText("COPY");
        copyButton.setTextSize(16);
        copyButton.setAllCaps(false);
        copyButton.setGravity(Gravity.CENTER);

        copyButton.setMinHeight(86);
        copyButton.setMinimumHeight(86);

        LinearLayout.LayoutParams copyParams =
                new LinearLayout.LayoutParams(
                        190,
                        86
                );

        copyParams.setMargins(
                20,
                0,
                0,
                0
        );

        commandRow.addView(
                copyButton,
                copyParams
        );

        copyButton.setOnClickListener(v -> {

            ClipboardManager clipboard =
                    (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);

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

        LinearLayout.LayoutParams commandRowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        86
                );

        commandRowParams.setMargins(
                0,
                30,
                0,
                0
        );

        right.addView(
                commandRow,
                commandRowParams
        );

        LinearLayout.LayoutParams rightParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        6
                );

        rightParams.gravity = Gravity.CENTER_VERTICAL;

        content.addView(
                right,
                rightParams
        );

        // ---------------------------------------------------------
        // CLOSE BUTTON
        // ---------------------------------------------------------

        Button closeButton = new Button(this);

        closeButton.setText("CLOSE");
        closeButton.setTextSize(16);
        closeButton.setAllCaps(false);
        closeButton.setGravity(Gravity.CENTER);

        closeButton.setMinHeight(86);
        closeButton.setMinimumHeight(86);

        closeButton.setOnClickListener(v ->
                finishAndRemoveTask()
        );

        LinearLayout.LayoutParams closeParams =
                new LinearLayout.LayoutParams(
                        240,
                        86
                );

        closeParams.gravity = Gravity.CENTER_HORIZONTAL;

        closeParams.setMargins(
                0,
                0,
                0,
                20
        );

        root.addView(
                closeButton,
                closeParams
        );

        setContentView(root);
    }

    private void showError(String message) {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(
                50,
                50,
                50,
                50
        );

        root.setBackgroundColor(Color.BLACK);

        TextView error = new TextView(this);

        error.setText(message);
        error.setTextColor(Color.WHITE);
        error.setTextSize(17);
        error.setGravity(Gravity.CENTER);

        root.addView(
                error,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        Button close = new Button(this);

        close.setText("CLOSE");
        close.setTextSize(16);
        close.setAllCaps(false);

        close.setOnClickListener(v ->
                finishAndRemoveTask()
        );

        LinearLayout.LayoutParams closeParams =
                new LinearLayout.LayoutParams(
                        220,
                        72
                );

        closeParams.setMargins(
                0,
                30,
                0,
                0
        );

        root.addView(
                close,
                closeParams
        );

        setContentView(root);
    }
}
