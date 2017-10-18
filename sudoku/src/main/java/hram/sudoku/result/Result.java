package hram.sudoku.result;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

public final class Result implements Parcelable {
    public Point TL, TR, BL, BR;
    public float AverageWeight;

    public Result(Point tl, Point tr, Point bl, Point br, float averageWeight) {
        this.TL = tl;
        this.TR = tr;
        this.BL = bl;
        this.BR = br;
        this.AverageWeight = averageWeight;
    }

    public boolean IsEqual(Result r) {
        return IsEqual(TL, r.TL) && IsEqual(TR, r.TR) && IsEqual(BL, r.BL) && IsEqual(BR, r.BR);
    }

    private boolean IsEqual(Point a, Point b) {
        return a.x == b.x && a.y == b.y;
    }

    private Result(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable((Parcelable) TL, flags);
        out.writeParcelable((Parcelable) TR, flags);
        out.writeParcelable((Parcelable) BL, flags);
        out.writeParcelable((Parcelable) BR, flags);
        out.writeFloat(AverageWeight);
    }

    public void readFromParcel(Parcel in) {
        TL = in.readParcelable(null);
        TR = in.readParcelable(null);
        BL = in.readParcelable(null);
        BR = in.readParcelable(null);
        AverageWeight = in.readFloat();
    }

    public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        public Result[] newArray(int size) {
            return new Result[size];
        }
    };
}
