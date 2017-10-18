package hram.sudoku.game.command;

import android.os.Bundle;

import hram.sudoku.game.Cell;
import hram.sudoku.game.CellNote;

public class EditCellNoteCommand extends AbstractCellCommand {

    private int mCellRow;
    private int mCellColumn;
    private CellNote mNote;
    private CellNote mOldNote;

    public EditCellNoteCommand(Cell cell, CellNote note) {
        mCellRow = cell.getRowIndex();
        mCellColumn = cell.getColumnIndex();
        mNote = note;
    }

    EditCellNoteCommand() {

    }

    @Override
    void saveState(Bundle outState) {
        super.saveState(outState);

        outState.putInt("cellRow", mCellRow);
        outState.putInt("cellColumn", mCellColumn);
        outState.putString("note", mNote.serialize());
        outState.putString("oldNote", mOldNote.serialize());
    }

    @Override
    void restoreState(Bundle inState) {
        super.restoreState(inState);

        mCellRow = inState.getInt("cellRow");
        mCellColumn = inState.getInt("cellColumn");
        mNote = CellNote.deserialize(inState.getString("note"));
        mOldNote = CellNote.deserialize(inState.getString("oldNote"));
    }

    @Override
    void execute() {
        Cell cell = getCells().getCell(mCellRow, mCellColumn);
        mOldNote = cell.getNote();
        cell.setNote(mNote);
    }

    @Override
    void undo() {
        Cell cell = getCells().getCell(mCellRow, mCellColumn);
        cell.setNote(mOldNote);
    }

}
