package hram.sudoku.core;

import hram.sudoku.result.ResultBitmap;

/**
 * Callback which is invoked when a possible result point (significant point in
 * the barcode image such as a corner) is found.
 */
public interface BitmapCallback {

    void bitmapResult(ResultBitmap value);

}
