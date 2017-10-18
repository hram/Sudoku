package hram.sudoku.result;

public abstract class ResultHandler {
    /**
     * A string describing the kind of barcode that was found, e.g. "Found contact info".
     *
     * @return The resource ID of the string.
     */
    public abstract int getDisplayTitle();

}
