package hram.sudoku.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import hram.sudoku.Constants;
import hram.sudoku.data.SudokuDatabase;
import hram.sudoku.game.SudokuGame;

@SuppressLint("Registered")
public class BaseActivity extends Activity {

    protected long sudokuGameID;

    protected boolean isNeedToContinueGame() {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sudokuGameID = gameSettings.getLong(Constants.LAST_SUDOKU_ID, -1);

        if (sudokuGameID == -1) {
            return false;
        }

        SudokuDatabase db = new SudokuDatabase(getApplicationContext());
        SudokuGame sg = db.getSudoku(sudokuGameID);
        if (sg == null) {
            return false;
        }

        if (sg.getState() == SudokuGame.GAME_STATE_COMPLETED) {
            return false;
        }

        return true;
    }
}
