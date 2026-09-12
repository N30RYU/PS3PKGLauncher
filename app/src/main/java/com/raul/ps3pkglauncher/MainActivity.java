package com.raul.ps3pkglauncher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final String ARMSX3_PACKAGE = "com.armsx3";
    private static final String ARMSX3_ACTIVITY = "com.armsx3/com.armsx2.MainActivity";
    private static final Pattern TITLE_ID = Pattern.compile("\\b[A-Za-z0-9]{4}\\d{5}\\b");

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        handleIntent(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        new Thread(() -> {
            try {
                String text = readIncomingText(intent);
                String titleId = extractTitleId(text);
                if (titleId == null) {
                    toast("No se encontró un Title ID en el archivo");
                    finishSafe();
                    return;
                }
                launchArmsx3(titleId);
            } catch (Exception e) {
                toast("PS3 PKG Launcher: no se pudo leer el archivo");
                finishSafe();
            }
        }).start();
    }

    private String readIncomingText(Intent intent) throws Exception {
        Uri uri = intent.getData();
        if (uri == null) {
            Object stream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (stream instanceof Uri) uri = (Uri) stream;
        }
        String[] keys = {"file_uri", "file_path", "path", "game_path", "rom_path", "uri", "file"};
        if (uri == null) {
            for (String key : keys) {
                String value = intent.getStringExtra(key);
                if (value != null && !value.trim().isEmpty()) {
                    uri = Uri.parse(value.trim());
                    break;
                }
            }
        }
        if (uri == null) throw new IOException("No URI/path received");

        InputStream in;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            in = getContentResolver().openInputStream(uri);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            in = new FileInputStream(new File(uri.getPath()));
        } else if (uri.getScheme() == null) {
            in = new FileInputStream(new File(uri.toString()));
        } else {
            in = getContentResolver().openInputStream(uri);
        }
        if (in == null) throw new IOException("Cannot open URI");
        try (InputStream input = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096]; int n;
            while ((n = input.read(buf)) != -1 && out.size() < 1024 * 1024) out.write(buf, 0, n);
            return new String(out.toByteArray(), StandardCharsets.UTF_8).replace("\\uFEFF", "");
        }
    }

    private String extractTitleId(String text) {
        if (text == null) return null;
        Matcher m = TITLE_ID.matcher(text.toUpperCase(Locale.ROOT));
        return m.find() ? m.group().toUpperCase(Locale.ROOT) : null;
    }

    private void launchArmsx3(String titleId) {
        runOnUiThread(() -> {
            try {
                Intent launch = new Intent(Intent.ACTION_MAIN);
                launch.setComponent(android.content.ComponentName.unflattenFromString(ARMSX3_ACTIVITY));
                launch.putExtra("title_id", titleId);
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(launch);
                finish();
            } catch (Exception e) {
                toast("No se pudo abrir ARMSX3");
                finish();
            }
        });
    }

    private void toast(String message) { runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show()); }
    private void finishSafe() { runOnUiThread(this::finish); }
}
