package hram.sudoku;

import java.util.List;

import hram.sudoku.core.FoundWeightCallback;
import hram.sudoku.result.ResultValue;

final class ViewfinderFoundWeightCallback implements FoundWeightCallback {

    private final ViewfinderView viewfinderView;

    ViewfinderFoundWeightCallback(ViewfinderView viewfinderView) {
        this.viewfinderView = viewfinderView;
    }

    @Override
    public void foundWeightResultLines(List<ResultValue> lines) {
        viewfinderView.addFoundWeightLines(lines);
    }

}
