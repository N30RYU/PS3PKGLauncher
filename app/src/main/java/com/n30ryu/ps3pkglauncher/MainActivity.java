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

public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle state) { super.onCreate(state); showIntentInfo(getIntent()); }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); showIntentInfo(intent); }
    private void showIntentInfo(Intent intent) {
        StringBuilder info = new StringBuilder();
        info.append("=== BEACON INTENT DIAGNOSTIC ===\n\n");
        info.append("ACTION:\n").append(String.valueOf(intent.getAction())).append("\n\n");
        info.append("DATA:\n").append(String.valueOf(intent.getDataString())).append("\n\n");
        info.append("TYPE:\n").append(String.valueOf(intent.getType())).append("\n\n");
        info.append("FLAGS:\n0x").append(Integer.toHexString(intent.getFlags())).append("\n\n");
        Uri data = intent.getData();
        if (data != null) { info.append("DATA SCHEME:\n").append(String.valueOf(data.getScheme())).append("\n\n"); info.append("DATA PATH:\n").append(String.valueOf(data.getPath())).append("\n\n"); }
        info.append("EXTRA_STREAM:\n");
        try { info.append(String.valueOf(intent.getParcelableExtra(Intent.EXTRA_STREAM))); } catch (Exception e) { info.append("ERROR: ").append(e.getMessage()); }
        info.append("\n\nEXTRA_TEXT:\n");
        try { info.append(String.valueOf(intent.getCharSequenceExtra(Intent.EXTRA_TEXT))); } catch (Exception e) { info.append("ERROR: ").append(e.getMessage()); }
        info.append("\n\nALL EXTRAS:\n");
        Bundle extras = intent.getExtras();
        if (extras == null) info.append("NO EXTRAS\n"); else for (String key : extras.keySet()) { info.append("\n").append(key).append(" = "); try { info.append(String.valueOf(extras.get(key))); } catch (Exception e) { info.append("ERROR: ").append(e.getMessage()); } }
        info.append("\n\n=== END ===");
        TextView tv = new TextView(this); tv.setText(info.toString()); tv.setTextSize(16); tv.setTextColor(Color.WHITE); tv.setPadding(30,30,30,30); tv.setGravity(Gravity.START); tv.setTextIsSelectable(true);
        ScrollView sv = new ScrollView(this); sv.setBackgroundColor(Color.BLACK); sv.addView(tv,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)); setContentView(sv);
    }
}
