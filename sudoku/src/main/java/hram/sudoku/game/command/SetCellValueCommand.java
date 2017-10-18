package hram.sudoku.game.command;

import android.os.Bundle;

import hram.sudoku.game.Cell;

public class SetCellValueCommand extends AbstractCellCommand {

    private int mCellRow;
    private int mCellColumn;
    private int mValue;
    private int mOldValue;

    public SetCellValueCommand(Cell cell, int value) {
        mCellRow = cell.getRowIndex();
        mCellColumn = cell.getColumnIndex();
        mValue = value;
    }

    SetCellValueCommand() {

    }

    @Override
    void saveState(Bundle outState) {
        super.saveState(outState);

        outState.putInt("cellRow", mCellRow);
        outState.putInt("cellColumn", mCellColumn);
        outState.putInt("value", mValue);
        outState.putInt("oldValue", mOldValue);
    }

    @Override
    void restoreState(Bundle inState) {
        super.restoreState(inState);

        mCellRow = inState.getInt("cellRow");
        mCellColumn = inState.getInt("cellColumn");
        mValue = inState.getInt("value");
        mOldValue = inState.getInt("oldValue");
    }

    @Override
    void execute() {
        Cell cell = getCells().getCell(mCellRow, mCellColumn);
        mOldValue = cell.getValue();
        cell.setValue(mValue);
    }

    @Override
    void undo() {
        Cell cell = getCells().getCell(mCellRow, mCellColumn);
        cell.setValue(mOldValue);
    }

}
