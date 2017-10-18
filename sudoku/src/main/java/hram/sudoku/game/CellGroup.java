package hram.sudoku.game;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents group of cells which must each contain unique number.
 * <p/>
 * Typical examples of instances are sudoku row, column or sector (3x3 group of cells).
 *
 * @author romario
 */
public class CellGroup {
    private Cell[] mCells = new Cell[CellCollection.SUDOKU_SIZE];
    private int mPos = 0;

    public void addCell(Cell cell) {
        mCells[mPos] = cell;
        mPos++;
    }


    /**
     * Validates numbers in given sudoku group - numbers must be unique. Cells with invalid
     * numbers are marked (see {@link Cell#isValid}).
     * <p/>
     * Method expects that cell's invalid properties has been set to false
     * ({@link CellCollection#validate} does this).
     *
     * @return True if validation is successful.
     */
    protected boolean validate() {
        boolean valid = true;

        Map<Integer, Cell> cellsByValue = new HashMap<Integer, Cell>();
        for (int i = 0; i < mCells.length; i++) {
            Cell cell = mCells[i];
            int value = cell.getValue();
            if (cellsByValue.get(value) != null) {
                mCells[i].setValid(false);
                cellsByValue.get(value).setValid(false);
                valid = false;
            } else {
                cellsByValue.put(value, cell);
                // we cannot set cell as valid here, because same cell can be invalid
                // as part of another group
            }
        }

        return valid;
    }

    public boolean contains(int value) {
        for (int i = 0; i < mCells.length; i++) {
            if (mCells[i].getValue() == value) {
                return true;
            }
        }
        return false;
    }
}
