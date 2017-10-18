package hram.sudoku.game.command;

import android.os.Bundle;

/**
 * Generic interface for command in application.
 *
 * @author romario
 */
public abstract class AbstractCommand {

    public static AbstractCommand newInstance(String commandClass) {
        if (commandClass.equals(ClearAllNotesCommand.class.getSimpleName())) {
            return new ClearAllNotesCommand();
        } else if (commandClass.equals(EditCellNoteCommand.class.getSimpleName())) {
            return new EditCellNoteCommand();
        } else if (commandClass.equals(FillInNotesCommand.class.getSimpleName())) {
            return new FillInNotesCommand();
        } else if (commandClass.equals(SetCellValueCommand.class.getSimpleName())) {
            return new SetCellValueCommand();
        } else {
            throw new IllegalArgumentException(String.format("Unknown command class '%s'.", commandClass));
        }
    }

    private boolean mIsCheckpoint;

    void saveState(Bundle outState) {
        outState.putBoolean("isCheckpoint", mIsCheckpoint);
    }

    void restoreState(Bundle inState) {
        mIsCheckpoint = inState.getBoolean("isCheckpoint");
    }

    public boolean isCheckpoint() {
        return mIsCheckpoint;
    }

    public void setCheckpoint(boolean isCheckpoint) {
        mIsCheckpoint = isCheckpoint;
    }

    public String getCommandClass() {
        return getClass().getSimpleName();
    }

    /**
     * Executes the command.
     */
    abstract void execute();

    /**
     * Undo this command.
     */
    abstract void undo();

}
