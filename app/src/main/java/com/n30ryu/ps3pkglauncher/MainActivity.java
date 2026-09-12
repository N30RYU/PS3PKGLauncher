package com.n30ryu.ps3pkglauncher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
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

    // Archivos de hasta 100 KB se consideran marcadores de PKG.
    private static final long MARKER_MAX_SIZE = 100 * 1024;

    private static final Pattern TITLE_ID =
            Pattern.compile("\\b[A-Za-z0-9]{4}\\d{5}\\b");

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        new Thread(() -> {

            try {

                String filePath = getFilePath(intent);

                if (filePath == null || filePath.trim().isEmpty()) {
                    toast("PS3 PKG Launcher: no se recibió file_path");
                    finishSafe();
                    return;
                }

                File file = new File(filePath);

                if (!file.exists()) {
                    toast("Archivo no encontrado:\n" + filePath);
                    finishSafe();
                    return;
                }

                long size = file.length();

                if (size > MARKER_MAX_SIZE) {

                    // ISO PS3 real → abrir directamente en ARMSX3.
                    launchArmsx3Iso(filePath);

                } else {

                    // Archivo pequeño → leer Title ID.
                    String text = readText(file);

                    String titleId = extractTitleId(text);

                    if (titleId == null) {
                        toast("No se encontró Title ID en:\n" + file.getName());
                        finishSafe();
                        return;
                    }

                    launchArmsx3TitleId(titleId);
                }

            } catch (Exception e) {

                toast("PS3 PKG Launcher:\n" +
                        (e.getMessage() != null
                                ? e.getMessage()
                                : "error desconocido"));

                finishSafe();
            }

        }).start();
    }

    private String getFilePath(Intent intent) {

        // Nuestro método principal desde Beacon:
        String path = intent.getStringExtra("file_path");

        if (path != null && !path.trim().isEmpty()) {
            return normalizePath(path);
        }

        // Compatibilidad con otras formas de lanzamiento.
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

                Intent launch = new Intent(Intent.ACTION_MAIN);

                launch.setComponent(
                        android.content.ComponentName
                                .unflattenFromString(ARMSX3_ACTIVITY)
                );

                launch.putExtra("path", filePath);
                launch.putExtra("file_path", filePath);

                launch.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                );

                startActivity(launch);

                finish();

            } catch (Exception e) {

                toast("No se pudo abrir el ISO en ARMSX3");
                finish();
            }
        });
    }

    private void launchArmsx3TitleId(String titleId) {

        runOnUiThread(() -> {

            try {

                Intent launch = new Intent(Intent.ACTION_MAIN);

                launch.setComponent(
                        android.content.ComponentName
                                .unflattenFromString(ARMSX3_ACTIVITY)
                );

                launch.putExtra("title_id", titleId);

                launch.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                );

                startActivity(launch);

                finish();

            } catch (Exception e) {

                toast("No se pudo abrir ARMSX3");
                finish();
            }
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
        runOnUiThread(this::finish);
    }
}
