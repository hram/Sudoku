package hram.sudoku;

import java.util.List;

import hram.sudoku.core.ResultLinesCallback;
import hram.sudoku.result.ResultValue;

final class ViewfinderResultLinesCallback implements ResultLinesCallback {

    private final ViewfinderView viewfinderView;

    ViewfinderResultLinesCallback(ViewfinderView viewfinderView) {
        this.viewfinderView = viewfinderView;
    }

    @Override
    public void foundPossibleResultLines(List<ResultValue> lines) {
        viewfinderView.addPossibleResultLines(lines);
    }
}
