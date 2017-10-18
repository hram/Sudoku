package hram.sudoku.data;

public class DataController {
    private static DataController instance = new DataController();

    public static DataController Instance() {
        return instance;
    }

    public Game GetLastGame() {
        return null;
    }
}
