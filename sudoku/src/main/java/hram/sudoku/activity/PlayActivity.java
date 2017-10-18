package hram.sudoku.activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hram.sudoku.Constants;
import hram.sudoku.R;
import hram.sudoku.data.SudokuDatabase;
import hram.sudoku.game.GameTimeFormat;
import hram.sudoku.game.HintsQueue;
import hram.sudoku.game.SudokuGame;
import hram.sudoku.game.SudokuGame.OnPuzzleSolvedListener;
import hram.sudoku.inputmethod.IMControlPanel;
import hram.sudoku.inputmethod.IMControlPanelStatePersister;
import hram.sudoku.inputmethod.IMNumpad;
import hram.sudoku.inputmethod.IMSingleNumber;
import hram.sudoku.utils.Timer;
import hram.sudoku.view.Sudoku;

public class PlayActivity extends BaseActivity {
    private SudokuDatabase mDatabase;
    private GameTimer mGameTimer;
    private Handler mGuiHandler;
    private boolean mShowTime = true;
    private boolean mFillInNotesEnabled = false;
    private boolean mFullScreen;

    private ViewGroup mRootLayout;
    private Sudoku mSudokuBoard;
    private TextView mTimeLabel;

    private long mSudokuGameID;
    private SudokuGame mSudokuGame;

    private IMControlPanel mIMControlPanel;
    private IMControlPanelStatePersister mIMControlPanelStatePersister;
    private IMSingleNumber mIMSingleNumber;
    private IMNumpad mIMNumpad;

    private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();

    private HintsQueue mHintsQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play);

        mRootLayout = (ViewGroup) findViewById(R.id.root_layout);
        mSudokuBoard = (Sudoku) findViewById(R.id.sudoku);
        mTimeLabel = (TextView) findViewById(R.id.time_label);

        mDatabase = new SudokuDatabase(getApplicationContext());
        mHintsQueue = new HintsQueue(this);
        mGameTimer = new GameTimer();

        mGuiHandler = new Handler();

        // create sudoku game instance
        if (savedInstanceState == null) {
            // activity runs for the first time, read game from database
            mSudokuGameID = getIntent().getLongExtra(Constants.EXTRA_SUDOKU_ID, 0);
            mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
        } else {
            // activity has been running before, restore its state
            mSudokuGame = new SudokuGame();
            mSudokuGame.restoreState(savedInstanceState);
            mGameTimer.restoreState(savedInstanceState);
        }

        if (mSudokuGame == null) {
            //mSudokuGame = SudokuGame.createEmptyGame();
            finish();
            return;
        }

        // read game settings
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        gameSettings.edit().putLong(Constants.LAST_SUDOKU_ID, mSudokuGame.getId()).apply();

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
            mSudokuGame.start();
        } else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();
        }

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
            mSudokuBoard.setReadOnly(true);
        }

        mSudokuBoard.setGame(mSudokuGame);
        mSudokuGame.setOnPuzzleSolvedListener(onSolvedListener);

        mIMControlPanel = (IMControlPanel) findViewById(R.id.input_methods);
        mIMControlPanel.initialize(mSudokuBoard, mSudokuGame, mHintsQueue);

        mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);

        mIMSingleNumber = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_SINGLE_NUMBER);
        mIMNumpad = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // read game settings
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int screenPadding = gameSettings.getInt("screen_border_size", 0);
        mRootLayout.setPadding(screenPadding, screenPadding, screenPadding, screenPadding);

        mFillInNotesEnabled = gameSettings.getBoolean("fill_in_notes_enabled", false);

        mSudokuBoard.setHighlightWrongVals(gameSettings.getBoolean("highlight_wrong_values", true));
        mSudokuBoard.setHighlightTouchedCell(gameSettings.getBoolean("highlight_touched_cell", true));

        mShowTime = gameSettings.getBoolean("show_time", true);
        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();

            if (mShowTime) {
                mGameTimer.start();
            }
        }
        mTimeLabel.setVisibility(mFullScreen && mShowTime ? View.VISIBLE : View.GONE);

        //mIMPopup.setEnabled(gameSettings.getBoolean("im_popup", true));
        mIMSingleNumber.setEnabled(gameSettings.getBoolean("im_single_number", true));
        mIMNumpad.setEnabled(gameSettings.getBoolean("im_numpad", true));
        mIMNumpad.setMoveCellSelectionOnPress(gameSettings.getBoolean("im_numpad_move_right", false));
        mIMSingleNumber.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMSingleNumber.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
        mIMNumpad.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMNumpad.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));

        mIMControlPanel.activateFirstInputMethod(); // make sure that some input method is activated
        mIMControlPanelStatePersister.restoreState(mIMControlPanel);

        updateTime();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // we will save game to the database as we might not be able to get back
        mDatabase.updateSudoku(mSudokuGame);

        mGameTimer.stop();
        mIMControlPanel.pause();
        mIMControlPanelStatePersister.saveState(mIMControlPanel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDatabase.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mGameTimer.stop();

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.pause();
        }

        mSudokuGame.saveState(outState);
        mGameTimer.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Occurs when puzzle is solved.
     */
    private OnPuzzleSolvedListener onSolvedListener = new OnPuzzleSolvedListener() {

        @Override
        public void onPuzzleSolved() {
            mSudokuBoard.setReadOnly(true);

            new AlertDialog.Builder(PlayActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.well_done)
                    .setMessage(getString(R.string.congrats, mGameTimeFormatter.format(mSudokuGame.getTime())))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }

    };

    /**
     * Update the time of game-play.
     */
    void updateTime() {
        if (mShowTime) {
            setTitle(mGameTimeFormatter.format(mSudokuGame.getTime()));
            mTimeLabel.setText(mGameTimeFormatter.format(mSudokuGame.getTime()));
        } else {
            setTitle(R.string.app_name);
        }

    }

    // This class implements the game clock.  All it does is update the
    // status each tick.
    private final class GameTimer extends Timer {

        GameTimer() {
            super(1000);
        }

        @Override
        protected boolean step(int count, long time) {
            updateTime();

            // Run until explicitly stopped.
            return false;
        }

    }
}
