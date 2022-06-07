package edu.lxh.app8.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicData implements Parcelable {

    private int currentPosition = 0;
    private int duration = 100;
    private String musicPath = "";
    private int status = -1;

    public MusicData() { }

    protected MusicData(Parcel in) {
        currentPosition = in.readInt();
        duration = in.readInt();
        musicPath = in.readString();
        status = in.readInt();
        //dd22222
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public MusicData setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public MusicData setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public MusicData setMusicPath(String musicPath) {
        this.musicPath = musicPath;
        return this;
    }

    public PlayStatus getStatus() {
        return PlayStatus.getStatus(status);
    }

    public MusicData setStatus(PlayStatus status) {
        this.status = PlayStatus.getValue(status);
        return this;
    }

    public MusicData reset() {
        currentPosition = 0;
        duration = 100;
        musicPath = "";
        status = -1;
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(currentPosition);
        dest.writeInt(duration);
        dest.writeString(musicPath);
        dest.writeInt(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MusicData> CREATOR = new Creator<MusicData>() {
        @Override
        public MusicData createFromParcel(Parcel in) {
            return new MusicData(in);
        }

        @Override
        public MusicData[] newArray(int size) {
            return new MusicData[size];
        }
    };
}
