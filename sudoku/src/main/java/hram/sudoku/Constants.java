package hram.sudoku;

public abstract class Constants {
    public static final String FOUND_RESULTS = "foundResults";
    public static final String FILE_NAME = "fileName";
    public static final String SUDOKU_REGION = "sudokuRegion";
    public static final String SUDOKU_WIDTH = "sudokuWidth";
    public static final String SUDOKU_HEIGHT = "sudokuHeight";
    public static final String SUDOKU_FRAMING_RECT = "sudokuFramingRect";
    public static final String SUDOKU_FILE_TYPE = "sudokuFileType";
    //public static final String SUDOKU_ORIENTATION = "sudokuOrientation";

    public static final int FILE_TYPE_YUV420SP = 1;
    public static final int FILE_TYPE_ARGB_8888 = 2;

    public static final String LAST_SUDOKU_NAME = "LastSudoku.raw";
    public static final String RESCALED_SUDOKU_NAME = "RescaledSudoku.raw";
    public static final String LAST_SUDOKU_ID = "last_sudoku_id";
    public static final String LAST_CROPED_SUDOKU_NAME = "LastCropedSudoku.raw";
    public static final String LAST_CROPED_SUDOKU_ID = "last_croped_sudoku_id";

    public static final String EXTRA_SUDOKU_ID = "sudoku_id";

    public static final int CELL_SIZE = 31;
    public static final int PREVIEW_SIZE = CELL_SIZE * 9;
}
