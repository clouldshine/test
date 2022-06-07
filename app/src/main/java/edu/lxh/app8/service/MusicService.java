package edu.lxh.app8.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import edu.lxh.app8.MainActivity;
import edu.lxh.app8.R;
import edu.lxh.app8.data.MusicData;
import edu.lxh.app8.data.PlayStatus;

public class MusicService extends Service {
    public interface MusicPlayListener {
        void onMusicPlayStatus(MusicData musicData);
    }
    private final int MESSAGE_UPDATE_UI = 0x1000;
    private final int DELAY_SEND_MESSAGE = 500;
    private MediaPlayer mMediaPlayer = null;
    private MusicData mMusicData = new MusicData();
    private MusicPlayListener mMusicPlayListener = null;
    private Handler mHeardHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg == null) return;
            switch (msg.what) {
                case MESSAGE_UPDATE_UI: {
                    if (mMusicPlayListener == null || mMediaPlayer == null || mMusicData == null
                            || !mMediaPlayer.isPlaying() || mMusicData.getStatus() != PlayStatus.PLAYING) return;
                    mMusicData.setCurrentPosition(mMediaPlayer.getCurrentPosition());
                    mMusicPlayListener.onMusicPlayStatus(mMusicData);
                    if (hasMessages(MESSAGE_UPDATE_UI)) removeMessages(MESSAGE_UPDATE_UI);
                    sendMessageDelayed(obtainMessage(MESSAGE_UPDATE_UI), DELAY_SEND_MESSAGE);
                } break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String SERVICE_ID = "MusicServiceID";
            final String SERVICE_NAME = "MusicService";
            Intent nfIntent = new Intent(this, MainActivity.class);
            Notification.Builder builder = new Notification.Builder(this, SERVICE_ID)
                    .setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText("运行中...")
                    .setWhen(System.currentTimeMillis());
            NotificationChannel notificationChannel = new NotificationChannel(SERVICE_ID, SERVICE_NAME, NotificationManager.IMPORTANCE_MIN);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            startForeground(1, builder.build());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlay();
        mMusicData = null;
        mMusicPlayListener = null;
        mHeardHandler.removeCallbacksAndMessages(null);
        mHeardHandler = null;
    }

    private MediaPlayer.OnCompletionListener mMediaPlayCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mMusicPlayListener != null) {
                mMusicData.setStatus(PlayStatus.STOP).setCurrentPosition(0);
                mMusicPlayListener.onMusicPlayStatus(mMusicData);
            } else {
                mMusicData.reset();
                stopSelf();
            }
        }
    };

    private MediaPlayer.OnErrorListener mMediaPlayerErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e("service error", String.format("error[%d]: extra -> %d", what, extra));
            mMusicData.reset();
            if (mMusicPlayListener != null) {
                mMusicPlayListener.onMusicPlayStatus(mMusicData);
            } else {
                stopSelf();
            }
            return false;
        }
    };

    private void initAndPlay(String uriString) {
        releaseMediaPlay();
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(mMediaPlayCompletionListener);
            mMediaPlayer.setOnErrorListener(mMediaPlayerErrorListener);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(new FileInputStream(new File(uriString)).getFD());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            // set data info
            mMusicData.setMusicPath(uriString).setStatus(PlayStatus.PLAYING)
                    .setDuration(mMediaPlayer.getDuration())
                    .setCurrentPosition(mMediaPlayer.getCurrentPosition());
            // update activity ui
            if (mHeardHandler.hasMessages(MESSAGE_UPDATE_UI)) mHeardHandler.removeMessages(MESSAGE_UPDATE_UI);
            mHeardHandler.sendMessageDelayed(mHeardHandler.obtainMessage(MESSAGE_UPDATE_UI), DELAY_SEND_MESSAGE);
            if (mMusicPlayListener != null) mMusicPlayListener.onMusicPlayStatus(mMusicData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resume() {
        if (mMediaPlayer == null) {
            mMediaPlayerErrorListener.onError(null, -1, -1);
        } else {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
            mMusicData.setStatus(PlayStatus.RESUME);
            if (mMusicPlayListener != null) mMusicPlayListener.onMusicPlayStatus(mMusicData);
        }
    }

    private void play() {
        if (mMediaPlayer == null) {
            mMediaPlayerErrorListener.onError(null, -1, -1);
        } else {
            if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
            mMusicData.setStatus(PlayStatus.PLAYING);
            if (mHeardHandler.hasMessages(MESSAGE_UPDATE_UI)) mHeardHandler.removeMessages(MESSAGE_UPDATE_UI);
            mHeardHandler.sendMessageDelayed(mHeardHandler.obtainMessage(MESSAGE_UPDATE_UI), DELAY_SEND_MESSAGE);
            if (mMusicPlayListener != null) mMusicPlayListener.onMusicPlayStatus(mMusicData);
        }
    }

    private void stop() {
        if (mMediaPlayer == null) {
            mMediaPlayerErrorListener.onError(null, -1, -1);
        } else {
            mMediaPlayer.stop();
            if (null != mMediaPlayer) {
                mMediaPlayer.release();
            }
            mMediaPlayer = null;
            mMusicData.setStatus(PlayStatus.STOP).setCurrentPosition(0);
            if (mMusicPlayListener != null) mMusicPlayListener.onMusicPlayStatus(mMusicData);
        }
    }

    private void releaseMediaPlay() {
        if (mMusicData == null) {
            mMusicData = new MusicData();
        } else {
            mMusicData.reset();
        }
        if (null != mMediaPlayer) {
            mMediaPlayer.release();
        }
        mMediaPlayer = null;
    }

    public class MusicBinder extends Binder {

        public void setMusicPlayListener(MusicPlayListener listener) {
            MusicService.this.mMusicPlayListener = listener;
            if (MusicService.this.mMusicPlayListener != null && mMusicData != null) {
                MusicService.this.mMusicPlayListener.onMusicPlayStatus(mMusicData);
                if (mMusicData.getStatus() == PlayStatus.PLAYING && mHeardHandler != null) {
                    if (mHeardHandler.hasMessages(MESSAGE_UPDATE_UI)) mHeardHandler.removeMessages(MESSAGE_UPDATE_UI);
                    mHeardHandler.sendMessageDelayed(mHeardHandler.obtainMessage(MESSAGE_UPDATE_UI), DELAY_SEND_MESSAGE);
                }
            }
        }

        public void playOrPause(String uriString) {
            if (TextUtils.isEmpty(uriString)) return;
            switch (mMusicData.getStatus()) {
                case NON:
                case STOP: { MusicService.this.initAndPlay(uriString); } break;
                case PLAYING: { MusicService.this.resume(); }break;
                case RESUME: { MusicService.this.play(); }break;
            }
        }

        public void stop() {
            MusicService.this.stop();
        }
    }

}
