package org.raksha.pixel.control;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControlActivity extends Activity {
    private static final int BG = Color.parseColor("#070B12");
    private static final int CARD_BG = Color.parseColor("#101824");
    private static final int CYAN = Color.parseColor("#00F5FF");
    private static final int GOLD = Color.parseColor("#D4A63A");
    private static final int GREEN = Color.parseColor("#34D399");
    private static final int MUTED = Color.parseColor("#9AA6B8");
    private static final int BORDER_CYAN = Color.parseColor("#2A00F5FF");
    private static final int BORDER_GOLD = Color.parseColor("#38D4A63A");

    private final ExecutorService exec = Executors.newCachedThreadPool();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private TextView llmStatus;
    private TextView kaliStatus;
    private TextView logView;
    private TextView audioBtn;

    private int dp(float v) {
        return (int) (getResources().getDisplayMetrics().density * v + 0.5f);
    }

    private GradientDrawable roundedBox(int fill, int stroke, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radiusDp));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    private TextView label(String text, float sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return tv;
    }

    private TextView button(String text, int bg, int fg, View.OnClickListener onClick) {
        TextView b = label(text, 13, fg, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14), dp(10), dp(14), dp(10));
        b.setBackground(roundedBox(bg, fg == Color.BLACK ? 0 : Color.argb(90, Color.red(fg), Color.green(fg), Color.blue(fg)), 12));
        b.setOnClickListener(onClick);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        b.setLayoutParams(lp);
        return b;
    }

    private LinearLayout card(String title, int accentColor, int borderColor) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(roundedBox(CARD_BG, borderColor, 18));
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(10), 0, 0);
        c.setLayoutParams(lp);
        TextView h = label(title, 15, accentColor, true);
        h.setPadding(0, 0, 0, dp(6));
        c.addView(h);
        return c;
    }

    private LinearLayout row(View... views) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(6);
        r.setLayoutParams(lp);
        for (View v : views) r.addView(v);
        return r;
    }

    private void appendLog(String msg) {
        ui.post(() -> {
            if (logView == null) return;
            CharSequence prev = logView.getText();
            String next = (prev == null || prev.length() == 0) ? msg : prev + "\n" + msg;
            if (next.length() > 3500) next = next.substring(next.length() - 3500);
            logView.setText(next);
        });
    }

    private void runCmd(String labelName, String... cmd) {
        appendLog("▶ " + labelName + " ...");
        exec.execute(() -> {
            try {
                Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        appendLog("  " + line);
                    }
                }
                int rc = p.waitFor();
                appendLog(rc == 0 ? "✓ " + labelName + " completed." : "✗ " + labelName + " exited (" + rc + ")");
            } catch (Exception e) {
                appendLog("✗ " + labelName + ": " + e.getMessage());
            }
            refreshStatus();
        });
    }

    private void openPkg(String pkg, String fallbackName) {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } else {
                Toast.makeText(this, fallbackName + " not installed (" + pkg + ")", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not open " + fallbackName, Toast.LENGTH_SHORT).show();
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            Toast.makeText(this, "Open in browser: " + url, Toast.LENGTH_SHORT).show();
        }
    }

    private void playAudio(int rawResId, String name) {
        stopAudio();
        try {
            player = MediaPlayer.create(this, rawResId);
            if (player != null) {
                player.setOnCompletionListener(mp -> {
                    stopAudio();
                    ui.post(() -> { if (audioBtn != null) audioBtn.setText("▶ Play Tone"); });
                });
                player.start();
                if (audioBtn != null) audioBtn.setText("■ Stop Audio");
                appendLog("♪ Playing " + name);
            }
        } catch (Exception e) {
            appendLog("Audio error: " + e.getMessage());
        }
    }

    private void stopAudio() {
        if (player != null) {
            try {
                if (player.isPlaying()) player.stop();
                player.release();
            } catch (Exception ignored) {}
            player = null;
        }
    }

    private void refreshStatus() {
        exec.execute(() -> {
            boolean llmUp = false;
            try {
                HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:8081/health").openConnection();
                c.setConnectTimeout(900);
                c.setReadTimeout(900);
                llmUp = (c.getResponseCode() == 200);
                c.disconnect();
            } catch (Exception ignored) {}

            boolean modelBaked = new File("/system/etc/openclaw/models/Qwen3.5-4B-Q4_K_M.gguf").exists();
            boolean kaliExtracted = new File("/data/local/nhsystem/kali-arm64/usr/bin").isDirectory();
            boolean kaliTarball = new File("/system/etc/raksha/kali/kali-nethunter-rootfs-full-arm64.tar.xz").exists();
            boolean busyboxOk = new File("/system/bin/busybox_nh").exists();

            final boolean fLlm = llmUp;
            final String llmText = fLlm
                ? "ONLINE • http://127.0.0.1:8081/v1 (raksha-qwen3.5-4b)"
                : (modelBaked
                    ? "OFFLINE • Qwen3.5-4B (2.6 GB) baked & ready — tap Start LLM"
                    : "Model file not found");
            final String kaliText = (kaliExtracted ? "READY • Full Kali ARM64 chroot deployed" :
                (kaliTarball ? "OFFLINE ARCHIVE BAKED (1.75 GB) • Tap Deploy Kali Chroot" : "Archive missing"))
                + (busyboxOk ? " | BusyBox OK" : " | BusyBox missing");

            ui.post(() -> {
                if (llmStatus != null) {
                    llmStatus.setText(llmText);
                    llmStatus.setTextColor(fLlm ? GREEN : GOLD);
                }
                if (kaliStatus != null) {
                    kaliStatus.setText(kaliText);
                    kaliStatus.setTextColor(kaliExtracted ? GREEN : CYAN);
                }
            });
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(22), dp(16), dp(28));
        scroll.addView(root);

        // Hero Header with Naga Raksha mask + Neon-Cyan Sinhala "රාක්ෂ" wordmark
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setBackground(roundedBox(Color.parseColor("#0D1520"), BORDER_CYAN, 22));
        hero.setPadding(dp(16), dp(18), dp(16), dp(16));

        ImageView mask = new ImageView(this);
        mask.setImageResource(R.drawable.raksha_mask);
        mask.setScaleType(ImageView.ScaleType.FIT_CENTER);
        hero.addView(mask, new LinearLayout.LayoutParams(dp(96), dp(96)));

        ImageView wordmark = new ImageView(this);
        wordmark.setImageResource(R.drawable.raksha_wordmark);
        wordmark.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams wmLp = new LinearLayout.LayoutParams(dp(200), dp(72));
        wmLp.topMargin = dp(6);
        hero.addView(wordmark, wmLp);

        TextView sub = label("RAKSHA OS 1.0 • AWAKENING  |  ANDROID 17", 11, GOLD, true);
        sub.setGravity(Gravity.CENTER);
        hero.addView(sub);

        audioBtn = button("▶ Boot Sound", Color.parseColor("#142836"), CYAN, v -> {
            if (player != null && player.isPlaying()) {
                stopAudio();
                audioBtn.setText("▶ Boot Sound");
            } else {
                playAudio(R.raw.raksha_boot_tone, "Raksha Boot Sound");
            }
        });
        TextView welcomeBtn = button("♪ Welcome Tone", Color.parseColor("#241E10"), GOLD,
            v -> playAudio(R.raw.raksha_welcome, "Raksha Desktop Welcome"));
        hero.addView(row(audioBtn, welcomeBtn));
        root.addView(hero);

        // Card 1: Native Local LLM (Qwen3.5-4B)
        LinearLayout llmCard = card("1. LOCAL AI ENGINE (QWEN 3.5 4B)", CYAN, BORDER_CYAN);
        llmStatus = label("Checking http://127.0.0.1:8081/health ...", 13, MUTED, false);
        llmCard.addView(llmStatus);
        llmCard.addView(row(
            button("Start LLM", Color.parseColor("#004D57"), CYAN,
                v -> runCmd("Start Qwen3.5-4B", "/system/bin/sh", "/system/bin/raksha-llama-server", "start")),
            button("Stop LLM", Color.parseColor("#2A181B"), Color.parseColor("#FCA5A5"),
                v -> runCmd("Stop Qwen3.5-4B", "/system/bin/sh", "/system/bin/raksha-llama-server", "stop")),
            button("Copy URL", Color.parseColor("#1D2636"), Color.WHITE, v -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("OpenClaw URL", "http://127.0.0.1:8081/v1"));
                Toast.makeText(this, "Copied: http://127.0.0.1:8081/v1 (model: raksha-qwen3.5-4b)", Toast.LENGTH_LONG).show();
            })
        ));
        root.addView(llmCard);

        // Card 2: OpenClaw & Messaging Pairing
        LinearLayout clawCard = card("2. OPENCLAW AGENT & MESSAGING PAIRING", GOLD, BORDER_GOLD);
        clawCard.addView(label(
            "Local Provider URL: http://127.0.0.1:8081/v1\nModel Alias: raksha-qwen3.5-4b  (API key: local)\nStart LLM above first, then open Claw or pair WhatsApp/Telegram inside OpenClaw onboarding.",
            12, MUTED, false));
        clawCard.addView(row(
            button("Open Claw", Color.parseColor("#2B220F"), GOLD, v -> openPkg("com.openclaw.android", "Claw")),
            button("Claw Companion", Color.parseColor("#1D2636"), CYAN, v -> openPkg("ai.openclaw.companion", "Claw Companion")),
            button("Web UI :18789", Color.parseColor("#142836"), Color.WHITE, v -> {
                runCmd("Start OpenClaw Web UI", "/system/bin/sh", "/system/bin/openclaw", "start");
                ui.postDelayed(() -> openUrl("http://127.0.0.1:18789"), 1200);
            })
        ));
        root.addView(clawCard);

        // Card 3: Kali NetHunter Full Offline Chroot
        LinearLayout kaliCard = card("3. KALI NETHUNTER FULL SUITE (OFFLINE)", CYAN, BORDER_CYAN);
        kaliStatus = label("Checking Kali ARM64 chroot...", 13, MUTED, false);
        kaliCard.addView(kaliStatus);
        kaliCard.addView(label("Tip: Open OneOS Guest App below and enable Guest Root before deploying Kali chroot.", 11, MUTED, false));
        kaliCard.addView(row(
            button("Deploy Full Kali", Color.parseColor("#004D57"), CYAN,
                v -> runCmd("Deploy Kali ARM64 Full", "su", "-c", "/system/bin/raksha-kali-install")),
            button("NetHunter", Color.parseColor("#1D2636"), Color.WHITE, v -> openPkg("com.offsec.nethunter", "NetHunter")),
            button("NH Terminal", Color.parseColor("#1D2636"), Color.WHITE, v -> openPkg("com.offsec.nhterm", "NetHunter Terminal"))
        ));
        root.addView(kaliCard);

        // Card 4: Root, Audio & System Hub
        LinearLayout sysCard = card("4. ROOT, AUDIO & SYSTEM HUB", GOLD, BORDER_GOLD);
        sysCard.addView(row(
            button("OneOS (Guest Root)", Color.parseColor("#2B220F"), GOLD, v -> openPkg("com.fahadali.oneos.guest", "OneOS")),
            button("JamesDSP", Color.parseColor("#1D2636"), CYAN, v -> openPkg("me.timschneeberger.rootlessjamesdsp", "RootlessJamesDSP"))
        ));
        sysCard.addView(row(
            button("Termux", Color.parseColor("#1D2636"), Color.WHITE, v -> openPkg("com.termux", "Termux")),
            button("F-Droid", Color.parseColor("#1D2636"), Color.WHITE, v -> openPkg("org.fdroid.fdroid", "F-Droid")),
            button("Settings", Color.parseColor("#1D2636"), Color.WHITE,
                v -> startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
        ));
        root.addView(sysCard);

        // Live Output Console
        LinearLayout logCard = card("LIVE SERVICE OUTPUT", MUTED, BORDER_CYAN);
        logView = label("Ready. Tap any control above to start services or check status.", 11, Color.parseColor("#CBD5E1"), false);
        logView.setTypeface(Typeface.MONOSPACE);
        logCard.addView(logView);
        root.addView(logCard);

        setContentView(scroll);
        refreshStatus();

        // Auto-play Raksha boot welcome sound once on first boot
        SharedPreferences prefs = getSharedPreferences("raksha_prefs", MODE_PRIVATE);
        if (getIntent().getBooleanExtra("first_boot_onboarding", false) || !prefs.getBoolean("welcome_played", false)) {
            prefs.edit().putBoolean("welcome_played", true).apply();
            ui.postDelayed(() -> playAudio(R.raw.raksha_boot_tone, "Raksha Awakening Tone"), 450);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override
    protected void onDestroy() {
        stopAudio();
        exec.shutdownNow();
        super.onDestroy();
    }
}
