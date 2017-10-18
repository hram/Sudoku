package hram.sudoku.utils;

public class CellsInfo {
    private byte[] cells = new byte[81];
    private int rotate = 0;

    public CellsInfo() {
        for (int i = 0; i < cells.length; i++) {
            cells[i] = -1;
        }
    }

    public byte GetCellValue(int y, int x) {
        return cells[y * 9 + x];
    }

    public void SetCellValue(int y, int x, int value) {
        cells[y * 9 + x] = (byte) value;
    }

    public void SetRotate(int value) {
        rotate = value;
    }
}
