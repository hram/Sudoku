package hram.sudoku;

import hram.sudoku.core.BitmapCallback;
import hram.sudoku.result.ResultBitmap;

final class ViewfinderBitmapCallback implements BitmapCallback {

    private final ViewfinderView viewfinderView;

    ViewfinderBitmapCallback(ViewfinderView viewfinderView) {
        this.viewfinderView = viewfinderView;
    }

    @Override
    public void bitmapResult(ResultBitmap value) {
        viewfinderView.drawResultBitmap(value.getBitmap());
    }
}
