package hram.sudoku.result;

import android.graphics.Bitmap;

/**
 * Created by hram on 16.04.2015.
 */
public class ResultBitmap extends ResultValue {

    Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public ResultBitmap(Bitmap resultBitmap) {
        this.bitmap = resultBitmap;
    }

    @Override
    public ResulType GetType() {
        return ResulType.BITMAP;
    }
}
