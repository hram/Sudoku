package hram.sudoku.solvers;

public class SudSolverCell {
    int value;                                        // The cell recornition value itself. Could be inital or calculated.
    //int[] OCR = new int[10] ;						// OCR probability for each digit 1-9. Smaller value means bigger probability for that digit value.
    boolean[] isCandidate = new boolean[10];        // List of possible values for this cell. Ignored for initally set cells.
    boolean needToFix;
}
