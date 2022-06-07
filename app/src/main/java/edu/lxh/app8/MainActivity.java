package edu.lxh.app8;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.lxh.app8.data.MusicData;
import edu.lxh.app8.data.PlayStatus;
import edu.lxh.app8.service.MusicService;
import edu.lxh.app8.tools.PermissionsTool;
import edu.lxh.app8.tools.SPTool;
import edu.lxh.app8.tools.UriTool;

public class MainActivity extends AppCompatActivity {
    // select
    private TextView mVMusicPath, mVMusicButton, mVMusicLoad;
    // player
    private ProgressBar mVMusicPrgBar;
    private LinearLayout mVMusicInfoLayout;
    private TextView mVMusicInfo, mVMusicPlayResume, mVMusicStop;
    // background service
    private MusicService.MusicBinder mMusicBinder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews().initEvents();
        bindService(new Intent(this, MusicService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    private MainActivity initViews() {
        mVMusicPath = findViewById(R.id.select_music_path);
        mVMusicButton = findViewById(R.id.select_music);
        mVMusicLoad = findViewById(R.id.select_music_init);
        mVMusicPrgBar = findViewById(R.id.music_progress);
        mVMusicInfoLayout = findViewById(R.id.music_info_panel);
        mVMusicInfo = findViewById(R.id.music_info);
        mVMusicPlayResume = findViewById(R.id.music_play);
        mVMusicStop = findViewById(R.id.music_stop);
        final String selectMusicFromSP = SPTool.getInstance(this).getString("select_music_file", "");
        mVMusicPath.setText(selectMusicFromSP);
        return this;
    }

    private MainActivity initEvents() {
        mVMusicButton.setOnClickListener(mOnClickListener);
        mVMusicLoad.setOnClickListener(mOnClickListener);
        mVMusicPlayResume.setOnClickListener(mOnClickListener);
        mVMusicStop.setOnClickListener(mOnClickListener);
        return this;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof MusicService.MusicBinder) {
                mMusicBinder = (MusicService.MusicBinder) service;
                mMusicBinder.setMusicPlayListener(mMusicPlayListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mMusicBinder != null) {
                mMusicBinder.setMusicPlayListener(null);
            }
            mMusicBinder = null;
        }
    };

    private MusicService.MusicPlayListener mMusicPlayListener = data -> {
        if (data == null) return;
        mVMusicPrgBar.setMax(data.getDuration());
        mVMusicPrgBar.setProgress(data.getCurrentPosition());
        mVMusicInfo.setText(data.getMusicPath());
        switch (data.getStatus()) {
            case PLAYING: {
                mVMusicPlayResume.setText("暂停播放 ||");
                mVMusicButton.setEnabled(false);
                mVMusicLoad.setEnabled(false);
                mVMusicStop.setEnabled(true);
            } break;
            case RESUME: {
                mVMusicPlayResume.setText("播放歌曲 >|");
                mVMusicButton.setEnabled(false);
                mVMusicLoad.setEnabled(false);
                mVMusicStop.setEnabled(true);
            } break;
            case STOP: {
                mVMusicButton.setEnabled(true);
                mVMusicLoad.setEnabled(true);
                mVMusicStop.setEnabled(false);
            } break;
        }
        if (data.getStatus() != PlayStatus.NON) {
            mVMusicPrgBar.setVisibility(View.VISIBLE);
            mVMusicInfoLayout.setVisibility(View.VISIBLE);
            mVMusicPlayResume.setVisibility(View.VISIBLE);
            mVMusicStop.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsTool.onRequestPermissionsResult(requestCode, permissions, grantResults,
                MainActivity.this, 0x1000, mSelectMusicRunnable);
    }

    private Runnable mSelectMusicRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "选择文件"), 0x1001);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0x1001: {
                if (resultCode != RESULT_OK) return;
                Uri uri = data.getData();
                if (uri == null) return;
                String uriString = "";
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    uriString = UriTool.getPath(MainActivity.this, uri);
                } else {
                    uriString = UriTool.getRealPathFromUri(MainActivity.this, uri);
                }
                if (TextUtils.isEmpty(uriString)) {
                    mVMusicLoad.setEnabled(false);
                } else {
                    mVMusicLoad.setEnabled(true);
                }
                mVMusicPath.setText(uriString != null ? uriString : "");
            }
            break;
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null == v) return;
            switch (v.getId()) {
                case R.id.select_music: {
                    if (PermissionsTool.hasPermissions(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        mSelectMusicRunnable.run();
                    } else {
                        PermissionsTool.requestPermissions(MainActivity.this, 0x1000,
                                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                break;
                case R.id.select_music_init: {
                    final String uriString = mVMusicPath.getText() != null ? mVMusicPath.getText().toString() : "";
                    if (TextUtils.isEmpty(uriString)) {
                        mVMusicLoad.setEnabled(false);
                    } else {
                        mVMusicPrgBar.setVisibility(View.VISIBLE);
                        mVMusicPrgBar.setMax(100);
                        mVMusicPrgBar.setProgress(0);
                        mVMusicInfoLayout.setVisibility(View.VISIBLE);
                        mVMusicInfo.setText(uriString);
                        mVMusicPlayResume.setVisibility(View.VISIBLE);
                        mVMusicStop.setVisibility(View.VISIBLE);
                        mVMusicStop.setEnabled(false);
                    }
                }
                break;
                case R.id.music_play: {
                    if (mMusicBinder != null && mVMusicInfo.getText() != null
                            && !TextUtils.isEmpty(mVMusicInfo.getText().toString())) {
                        mMusicBinder.playOrPause(mVMusicInfo.getText().toString());
                    }
                }
                break;
                case R.id.music_stop: {
                    if (mMusicBinder != null) mMusicBinder.stop();
                }
                break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVMusicPath.getText() != null) {
            SPTool.getInstance(this).putString("select_music_file", mVMusicPath.getText().toString());
        } else {
            SPTool.getInstance(this).putString("select_music_file", "");
        }
        if (mMusicBinder != null) {
            mMusicBinder.setMusicPlayListener(null);
        }
        mMusicBinder = null;
        unbindService(mServiceConnection);
    }
}