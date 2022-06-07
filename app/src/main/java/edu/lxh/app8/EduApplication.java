package edu.lxh.app8;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import edu.lxh.app8.service.MusicService;

public class EduApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, MusicService.class));
        } else {
            startService(new Intent(this, MusicService.class));
        }
    }
}
