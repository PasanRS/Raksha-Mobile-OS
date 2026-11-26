package org.raksha.pixel.control;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public class WelcomeAudioService extends Service {
    private static final String CHANNEL_ID = "raksha_awakening";
    private MediaPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable safetyStop = this::finishPlayback;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null && Build.VERSION.SDK_INT >= 26) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID, "Raksha Awakening", NotificationManager.IMPORTANCE_LOW);
                ch.setSound(null, null);
                ch.setShowBadge(false);
                nm.createNotificationChannel(ch);
            }
            Notification n = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Raksha Mobile OS")
                    .setContentText("Awakening...")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setOngoing(true)
                    .build();
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(101, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(101, n);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (player != null) {
                player.release();
                player = null;
            }
            MediaPlayer mp = MediaPlayer.create(this, R.raw.raksha_boot_tone);
            if (mp != null) {
                player = mp;
                mp.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
                mp.setVolume(0.95f, 0.95f);
                mp.setOnCompletionListener(p -> finishPlayback());
                mp.setOnErrorListener((p, what, extra) -> {
                    finishPlayback();
                    return true;
                });
                mp.start();
                handler.removeCallbacks(safetyStop);
                handler.postDelayed(safetyStop, 14000);
                return START_NOT_STICKY;
            }
        } catch (Throwable ignored) {
        }
        finishPlayback();
        return START_NOT_STICKY;
    }

    private void finishPlayback() {
        handler.removeCallbacks(safetyStop);
        if (player != null) {
            try { player.stop(); } catch (Throwable ignored) {}
            try { player.release(); } catch (Throwable ignored) {}
            player = null;
        }
        try { stopForeground(STOP_FOREGROUND_REMOVE); } catch (Throwable ignored) {}
        stopSelf();
    }

    @Override
    public void onDestroy() {
        finishPlayback();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
