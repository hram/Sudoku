/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package hram.sudoku.inputmethod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;

import hram.sudoku.Constants;
import hram.sudoku.R;
import hram.sudoku.activity.CaptureActivity;
import hram.sudoku.activity.GameSettingsActivity;
import hram.sudoku.game.Cell;
import hram.sudoku.game.HintsQueue;
import hram.sudoku.game.SudokuGame;
import hram.sudoku.inputmethod.IMControlPanelStatePersister.StateBundle;
import hram.sudoku.tasks.RescaleAndSaveImageTask;
import hram.sudoku.view.Sudoku;


/**
 * Base class for several input methods used to edit sudoku contents.
 *
 * @author romario
 */
public abstract class InputMethod {

    // TODO: I should not have mPrefix for fields used in subclasses, create proper getters
    protected Context mContext;
    protected IMControlPanel mControlPanel;
    protected SudokuGame mGame;
    protected Sudoku mBoard;
    protected HintsQueue mHintsQueue;

    private String mInputMethodName;
    protected View mInputMethodView;

    protected boolean mActive = false;
    private boolean mEnabled = true;

    public InputMethod() {

    }

    protected void initialize(Context context, IMControlPanel controlPanel, SudokuGame game, Sudoku board, HintsQueue hintsQueue) {
        mContext = context;
        mControlPanel = controlPanel;
        mGame = game;
        mBoard = board;
        mHintsQueue = hintsQueue;
        mInputMethodName = this.getClass().getSimpleName();
    }

    public boolean isInputMethodViewCreated() {
        return mInputMethodView != null;
    }

    public View getInputMethodView() {
        if (mInputMethodView == null) {
            mInputMethodView = createControlPanelView();
            View switchModeView = mInputMethodView.findViewById(R.id.switch_input_mode);
            ImageButton switchModeButton = (ImageButton) switchModeView;
            //switchModeButton.setText(getAbbrName());
            //switchModeButton.getBackground().setColorFilter(new LightingColorFilter(Color.CYAN, 0));
            switchModeButton.setImageResource(getImageResource());
            onControlPanelCreated(mInputMethodView);
        }

        return mInputMethodView;
    }

    /**
     * This should be called when activity is paused (so InputMethod can do some cleanup,
     * for example properly dismiss dialogs because of WindowLeaked exception).
     */
    public void pause() {
        onPause();
    }

    protected void onPause() {

    }

    /**
     * This should be unique name of input method.
     *
     * @return
     */
    protected String getInputMethodName() {
        return mInputMethodName;
    }

    public abstract int getNameResID();

    public abstract int getHelpResID();

    public abstract int getImageResource();

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;

        if (!enabled) {
            mControlPanel.activateNextInputMethod();
        }
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void activate() {
        mActive = true;
        onActivated();
    }

    public void deactivate() {
        mActive = false;
        onDeactivated();
    }

    protected abstract View createControlPanelView();

    protected void onControlPanelCreated(View controlPanel) {

    }

    protected void onActivated() {
    }

    protected void onDeactivated() {
    }

    /**
     * Called when cell is selected. Please note that cell selection can
     * change without direct user interaction.
     *
     * @param cell
     */
    protected void onCellSelected(Cell cell) {

    }

    /**
     * Called when cell is tapped.
     *
     * @param cell
     */
    protected void onCellTapped(Cell cell) {

    }

    protected void onSaveState(StateBundle outState) {
    }

    protected void onRestoreState(StateBundle savedState) {
    }

    protected void startCaprure() {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
        gameSettings.edit().putLong(Constants.LAST_SUDOKU_ID, -1).apply();

        Intent intent = new Intent(mContext, CaptureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
    }

    protected void startSettings() {
        Intent intent = new Intent(mContext, GameSettingsActivity.class);
        mContext.startActivity(intent);
    }

    protected void clearNotes() {
        new AlertDialog.Builder(mContext)
                .setIcon(android.R.drawable.ic_menu_delete)
                .setTitle(R.string.app_name)
                .setMessage(R.string.clear_all_notes_confirm)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mGame.clearAllNotes();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    protected void changeViewMode() {
        Bitmap bm = mGame.getCropedBitmap(mContext);
        if (bm != null) {
            mBoard.changeViewMode(bm);
            return;
        }

        new RescaleAndSaveImageTask(mContext, mBoard, mGame).execute();
    }

    protected void undo() {
        mGame.undo();
    }

    protected void restart() {
        new AlertDialog.Builder(mContext)
                .setIcon(android.R.drawable.ic_menu_rotate)
                .setTitle(R.string.app_name)
                .setMessage(R.string.restart_confirm)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Restart game
                        mGame.reset();
                        mGame.start();
                        mBoard.setReadOnly(false);
                        //if (mShowTime) {
                        //	mGameTimer.start();
                        //}
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
