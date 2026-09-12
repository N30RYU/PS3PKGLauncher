private void launchArmsx3Iso(String filePath) {

    runOnUiThread(() -> {

        try {

            Intent launch = new Intent(Intent.ACTION_VIEW);

            launch.setComponent(
                    android.content.ComponentName
                            .unflattenFromString(ARMSX3_ACTIVITY)
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

            // Cerrar completamente PS3 PKG Launcher.
            finishAndRemoveTask();

        } catch (Exception e) {

            toast("No se pudo abrir el ISO en ARMSX3");
            finishAndRemoveTask();
        }
    });
}
