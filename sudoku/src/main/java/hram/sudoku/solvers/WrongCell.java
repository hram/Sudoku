package hram.sudoku.solvers;

public class WrongCell {
    int y;
    int x;
    int value;
    int[] OCR = new int[10];

    WrongCell(int y, int x, int value) {
        this.y = y;
        this.x = x;
        this.value = value;
    }
}
