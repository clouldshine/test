package edu.lxh.app8.data;

public enum PlayStatus {
    NON(-1),
    PLAYING(0),
    RESUME(1),
    STOP(2);

    // =====================================================
    private int mVal = -1;

    PlayStatus(int value) {
        mVal = value;
    }

    // =====================================================
    public static PlayStatus getStatus(int status) {
        if (status == PLAYING.mVal) return PLAYING;
        else if (status == RESUME.mVal) return RESUME;
        else if (status == STOP.mVal) return STOP;
        else return NON;
    }

    public static int getValue(PlayStatus status) {
        return status.mVal;
    }

}
