package hram.sudoku.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collection;

import hram.sudoku.R;
import hram.sudoku.game.Cell;
import hram.sudoku.game.CellCollection;
import hram.sudoku.game.CellCollection.OnChangeListener;
import hram.sudoku.game.CellNote;
import hram.sudoku.game.SudokuGame;

public class Sudoku extends View {
    public static final int DEFAULT_BOARD_SIZE = 100;
    private static final int NO_COLOR = 0;

    private float cellWidth;
    private float cellHeight;

    private Paint mLinePaint;
    private Paint mSectorLinePaint;
    private Paint mCellValuePaint;
    private Paint mCellValueReadonlyPaint;
    private Paint mCellNotePaint;
    private Paint mCellValueInvalidPaint;

    private int mNumberLeft;
    private int mNumberTop;
    private float mNoteTop;

    private int mSectorLineWidth;

    // фоны
    private Paint mBackgroundColorSecondary;
    private Paint mBackgroundColorReadOnly;
    private Paint mBackgroundColorTouched;
    private Paint mBackgroundColorSelected;
    private Paint mBackgroundColorTheSame;
    private Paint mBackgroundColorTheSameTr;

    private Bitmap bitmap;
    private Rect rect;

    private SudokuGame mGame;
    private CellCollection mCells;
    // клетка до которой дотронулись/держим палец над
    private Cell mTouchedCell;
    // клетка над которой отпустили палец TODO: should I synchronize access to mSelectedCell?
    private Cell mSelectedCell;

    private OnCellTappedListener mOnCellTappedListener;
    private OnCellSelectedListener mOnCellSelectedListener;

    private boolean mReadonly = false;
    private boolean mHighlightWrongVals = true;
    private boolean mHighlightTouchedCell = true;
    private boolean mHighlightTheSameVals = true;
    private boolean mAutoHideTouchedCellHint = true;

    // режим отображения false - обычный судоку, true - отображается захваченная картинка
    private boolean viewMode;

    public Sudoku(Context context) {
        this(context, null);
    }

    public Sudoku(Context context, AttributeSet attrs) {
        super(context, attrs);

        setFocusable(true);
        setFocusableInTouchMode(true);

        mLinePaint = new Paint();
        mSectorLinePaint = new Paint();
        mCellValuePaint = new Paint();
        mCellValueReadonlyPaint = new Paint();
        mCellValueInvalidPaint = new Paint();
        mCellNotePaint = new Paint();
        mBackgroundColorSecondary = new Paint();
        mBackgroundColorReadOnly = new Paint();
        mBackgroundColorTouched = new Paint();
        mBackgroundColorSelected = new Paint();
        mBackgroundColorTheSame = new Paint();
        mBackgroundColorTheSameTr = new Paint();

        mCellValuePaint.setAntiAlias(true);
        mCellValueReadonlyPaint.setAntiAlias(true);
        mCellValueInvalidPaint.setAntiAlias(true);
        mCellNotePaint.setAntiAlias(true);
        mCellValueInvalidPaint.setColor(Color.RED);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudokuBoardView/*, defStyle, 0*/);

        setLineColor(a.getColor(R.styleable.SudokuBoardView_lineColor, Color.BLACK));
        setSectorLineColor(a.getColor(R.styleable.SudokuBoardView_sectorLineColor, Color.BLACK));
        setTextColor(a.getColor(R.styleable.SudokuBoardView_textColor, Color.BLACK));
        setTextColorReadOnly(a.getColor(R.styleable.SudokuBoardView_textColorReadOnly, Color.BLACK));
        setTextColorNote(a.getColor(R.styleable.SudokuBoardView_textColorNote, Color.BLACK));
        setBackgroundColor(a.getColor(R.styleable.SudokuBoardView_backgroundColor, Color.WHITE));
        setBackgroundColorSecondary(a.getColor(R.styleable.SudokuBoardView_backgroundColorSecondary, NO_COLOR));
        setBackgroundColorReadOnly(a.getColor(R.styleable.SudokuBoardView_backgroundColorReadOnly, 0xFFCCCCCC));
        setBackgroundColorTouched(a.getColor(R.styleable.SudokuBoardView_backgroundColorTouched, Color.rgb(50, 50, 255)));
        setBackgroundColorSelected(a.getColor(R.styleable.SudokuBoardView_backgroundColorSelected, Color.YELLOW));
        mBackgroundColorTheSame.setColor(0xFF03a9f4);
        mBackgroundColorTheSameTr.setColor(0x4003a9f4);

        a.recycle();
    }

    public int getLineColor() {
        return mLinePaint.getColor();
    }

    public void setLineColor(int color) {
        mLinePaint.setColor(color);
    }

    public int getSectorLineColor() {
        return mSectorLinePaint.getColor();
    }

    public void setSectorLineColor(int color) {
        mSectorLinePaint.setColor(color);
    }

    public int getTextColor() {
        return mCellValuePaint.getColor();
    }

    public void setTextColor(int color) {
        mCellValuePaint.setColor(color);
    }

    public int getTextColorReadOnly() {
        return mCellValueReadonlyPaint.getColor();
    }

    public void setTextColorReadOnly(int color) {
        mCellValueReadonlyPaint.setColor(color);
    }

    public int getTextColorNote() {
        return mCellNotePaint.getColor();
    }

    public void setTextColorNote(int color) {
        mCellNotePaint.setColor(color);
    }

    public int getBackgroundColorSecondary() {
        return mBackgroundColorSecondary.getColor();
    }

    public void setBackgroundColorSecondary(int color) {
        mBackgroundColorSecondary.setColor(color);
        mBackgroundColorSecondary.setColor(0xFF7F7F7F);
    }

    public int getBackgroundColorReadOnly() {
        return mBackgroundColorReadOnly.getColor();
    }

    public void setBackgroundColorReadOnly(int color) {
        mBackgroundColorReadOnly.setColor(color);
    }

    public int getBackgroundColorTouched() {
        return mBackgroundColorTouched.getColor();
    }

    public void setBackgroundColorTouched(int color) {
        mBackgroundColorTouched.setColor(color);
        mBackgroundColorTouched.setAlpha(100);
    }

    public int getBackgroundColorSelected() {
        return mBackgroundColorSelected.getColor();
    }

    public void setBackgroundColorSelected(int color) {
        mBackgroundColorSelected.setColor(color);
        mBackgroundColorSelected.setAlpha(100);
    }

    public void setGame(SudokuGame game) {
        mGame = game;
        //bitmap = game.getBitmap();
        setCells(game.getCells());
    }

    public void setCells(CellCollection cells) {
        mCells = cells;

        if (mCells != null) {
            if (!mReadonly) {
                mSelectedCell = mCells.getCell(4, 4); // first cell will be selected by default
                onCellSelected(mSelectedCell);
            }

            mCells.addOnChangeListener(new OnChangeListener() {
                @Override
                public void onChange() {
                    postInvalidate();
                }
            });
        }

        postInvalidate();
    }

    public CellCollection getCells() {
        return mCells;
    }

    public Cell getSelectedCell() {
        return mSelectedCell;
    }

    public void setReadOnly(boolean readonly) {
        mReadonly = readonly;
        postInvalidate();
    }

    public boolean isReadOnly() {
        return mReadonly;
    }

    public void setHighlightWrongVals(boolean highlightWrongVals) {
        mHighlightWrongVals = highlightWrongVals;
        postInvalidate();
    }

    public boolean getHighlightWrongVals() {
        return mHighlightWrongVals;
    }

    public void setHighlightTouchedCell(boolean highlightTouchedCell) {
        mHighlightTouchedCell = highlightTouchedCell;
    }

    public boolean getHighlightTouchedCell() {
        return mHighlightTouchedCell;
    }

    public void setHighlightTheSameVals(boolean highlightTheSameVals) {
        mHighlightTheSameVals = highlightTheSameVals;
        postInvalidate();
    }

    public boolean getHighlightTheSameVals() {
        return mHighlightTheSameVals;
    }

    public void setAutoHideTouchedCellHint(boolean autoHideTouchedCellHint) {
        mAutoHideTouchedCellHint = autoHideTouchedCellHint;
    }

    public boolean getAutoHideTouchedCellHint() {
        return mAutoHideTouchedCellHint;
    }

    /**
     * Registers callback which will be invoked when user taps the cell.
     *
     * @param l
     */
    public void setOnCellTappedListener(OnCellTappedListener l) {
        mOnCellTappedListener = l;
    }

    protected void onCellTapped(Cell cell) {
        if (mOnCellTappedListener != null) {
            mOnCellTappedListener.onCellTapped(cell);
        }
    }

    /**
     * Registers callback which will be invoked when cell is selected. Cell selection
     * can change without user interaction.
     *
     * @param l
     */
    public void setOnCellSelectedListener(OnCellSelectedListener l) {
        mOnCellSelectedListener = l;
    }

    public void hideTouchedCellHint() {
        mTouchedCell = null;
        postInvalidate();
    }

    public void changeViewMode(Bitmap bm) {
        bitmap = bm;
        viewMode = !viewMode;
        postInvalidate();
    }

    public void setBitmap(Bitmap bm) {
        bitmap = bm;
        viewMode = true;
        postInvalidate();
    }

    protected void onCellSelected(Cell cell) {
        if (mOnCellSelectedListener != null) {
            mOnCellSelectedListener.onCellSelected(cell);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);


//		  Log.d(TAG, "widthMode=" + getMeasureSpecModeString(widthMode));
//        Log.d(TAG, "widthSize=" + widthSize);
//        Log.d(TAG, "heightMode=" + getMeasureSpecModeString(heightMode));
//        Log.d(TAG, "heightSize=" + heightSize);

        int width = -1, height = -1;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = DEFAULT_BOARD_SIZE;
            if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
                width = widthSize;
            }
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = DEFAULT_BOARD_SIZE;
            if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
                height = heightSize;
            }
        }

        if (widthMode != MeasureSpec.EXACTLY) {
            width = height;
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            height = width;
        }

        if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
            width = widthSize;
        }
        if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
            height = heightSize;
        }

        cellWidth = (width - getPaddingLeft() - getPaddingRight()) / 9.0f;
        cellHeight = (height - getPaddingTop() - getPaddingBottom()) / 9.0f;

        setMeasuredDimension(width, height);

        float cellTextSize = cellHeight * 0.75f;
        mCellValuePaint.setTextSize(cellTextSize);
        mCellValueReadonlyPaint.setTextSize(cellTextSize);
        mCellValueInvalidPaint.setTextSize(cellTextSize);
        mCellNotePaint.setTextSize(cellHeight / 3.0f);
        // compute offsets in each cell to center the rendered number
        mNumberLeft = (int) ((cellWidth - mCellValuePaint.measureText("9")) / 2);
        mNumberTop = (int) ((cellHeight - mCellValuePaint.getTextSize()) / 2);

        // add some offset because in some resolutions notes are cut-off in the top
        mNoteTop = cellHeight / 50.0f;

        computeSectorLineWidth(width, height);

        rect = new Rect(getPaddingLeft(), getPaddingTop(), width - getPaddingRight(), height - getPaddingBottom());
    }

    private void computeSectorLineWidth(int widthInPx, int heightInPx) {
        int sizeInPx = widthInPx < heightInPx ? widthInPx : heightInPx;
        float dipScale = getContext().getResources().getDisplayMetrics().density;
        float sizeInDip = sizeInPx / dipScale;

        float sectorLineWidthInDip = 2.0f;

        if (sizeInDip > 150) {
            sectorLineWidthInDip = 3.0f;
        }

        mSectorLineWidth = (int) (sectorLineWidthInDip * dipScale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth() - getPaddingRight();
        int height = getHeight() - getPaddingBottom();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (viewMode && bitmap != null) {
            canvas.drawBitmap(bitmap, null, rect, null);
        }

        //canvas.drawRect(3 * cellWidth + paddingLeft, paddingTop, 6 * cellWidth + paddingLeft, 3 * cellWidth + paddingTop, mBackgroundColorSecondary);
        //canvas.drawRect(paddingLeft, 3 * cellWidth + paddingTop, 3 * cellWidth  + paddingLeft, 6 * cellWidth + paddingTop, mBackgroundColorSecondary);
        //canvas.drawRect(6 * cellWidth + paddingLeft, 3 * cellWidth + paddingTop, 9 * cellWidth + paddingLeft, 6 * cellWidth + paddingTop, mBackgroundColorSecondary);
        //canvas.drawRect(3 * cellWidth + paddingLeft, 6 * cellWidth + paddingTop, 6 * cellWidth + paddingLeft, 9 * cellWidth + paddingTop, mBackgroundColorSecondary);

        // отрисовка цифр
        if (mCells != null) {
            int cellLeft, cellTop;
            boolean hasBackgroundColorReadOnly = mBackgroundColorReadOnly.getColor() != NO_COLOR;

            float numberAscent = mCellValuePaint.ascent();
            float noteAscent = mCellNotePaint.ascent();
            float noteWidth = cellWidth / 3f;

            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    Cell cell = mCells.getCell(row, col);

                    cellLeft = Math.round((col * cellWidth) + paddingLeft);
                    cellTop = Math.round((row * cellHeight) + paddingTop);

                    // draw read-only field background
                    if (viewMode == false && !cell.isEditable() && hasBackgroundColorReadOnly) {
                        if (mBackgroundColorReadOnly.getColor() != NO_COLOR) {
                            canvas.drawRect(cellLeft, cellTop, cellLeft + cellWidth, cellTop + cellHeight, mBackgroundColorReadOnly);
                        }
                    }

                    // draw cell Text
                    int value = cell.getValue();

                    // если не пустая ячейка
                    if (value != 0) {
                        // если включена подсветка одинаковых как выбраная
                        if (mHighlightTheSameVals && mSelectedCell != null) {
                            if (value == mSelectedCell.getValue()) {
                                canvas.drawRect(cellLeft, cellTop, cellLeft + cellWidth, cellTop + cellHeight, viewMode ? mBackgroundColorTheSameTr : mBackgroundColorTheSame);
                            }
                        }

                        Paint cellValuePaint = cell.isEditable() ? mCellValuePaint : mCellValueReadonlyPaint;

                        // если включена подсветка ошибок
                        if (mHighlightWrongVals && !cell.isValid()) {
                            cellValuePaint = mCellValueInvalidPaint;
                        }

                        // если рисуем подложку то выводим только новые цифры
                        if (viewMode) {
                            if (cell.isEditable()) {
                                canvas.drawText(Integer.toString(value), cellLeft + mNumberLeft, cellTop + mNumberTop - numberAscent, cellValuePaint);
                            }
                        } else {
                            canvas.drawText(Integer.toString(value), cellLeft + mNumberLeft, cellTop + mNumberTop - numberAscent, cellValuePaint);
                        }
                    }
                    // если пустая ячейка
                    else {
                        // если есть подсказки то рисуем их
                        if (!cell.getNote().isEmpty()) {
                            Collection<Integer> numbers = cell.getNote().getNotedNumbers();
                            for (Integer number : numbers) {
                                int n = number - 1;
                                int c = n % 3;
                                int r = n / 3;
                                //canvas.drawText(Integer.toString(number), cellLeft + c*noteWidth + 2, cellTop + noteAscent + r*noteWidth - 1, mNotePaint);
                                canvas.drawText(Integer.toString(number), cellLeft + c * noteWidth + 2, cellTop + mNoteTop - noteAscent + r * noteWidth - 1, mCellNotePaint);
                            }
                        }
                    }

                } // по столбцам
            }// по строкам

            // подсветка выделенной ячейки
            if (!mReadonly && mSelectedCell != null) {
                cellLeft = Math.round(mSelectedCell.getColumnIndex() * cellWidth) + paddingLeft;
                cellTop = Math.round(mSelectedCell.getRowIndex() * cellHeight) + paddingTop;
                canvas.drawRect(cellLeft, cellTop, cellLeft + cellWidth, cellTop + cellHeight, mBackgroundColorSelected);
            }

            // подсветка ячейки под пальцем
            if (mHighlightTouchedCell && mTouchedCell != null) {
                cellLeft = Math.round(mTouchedCell.getColumnIndex() * cellWidth) + paddingLeft;
                cellTop = Math.round(mTouchedCell.getRowIndex() * cellHeight) + paddingTop;
                canvas.drawRect(cellLeft, paddingTop, cellLeft + cellWidth, height, mBackgroundColorTouched);
                canvas.drawRect(paddingLeft, cellTop, width, cellTop + cellHeight, mBackgroundColorTouched);
            }
        }

        if (viewMode == false) {
            // отрисовка тонких вертикальных линий
            for (int c = 0; c <= 9; c++) {
                float x = (c * cellWidth) + paddingLeft;
                canvas.drawLine((int) x, paddingTop, (int) x, height, mLinePaint);
            }

            // отрисовка тонких горизонтальных линий
            for (int r = 0; r <= 9; r++) {
                float y = r * cellHeight + paddingTop;
                canvas.drawLine(paddingLeft, y, width, y, mLinePaint);
            }

            int sectorLineWidth1 = mSectorLineWidth / 2;
            int sectorLineWidth2 = sectorLineWidth1 + (mSectorLineWidth % 2);

            // отрисовка толстых вертикальных линий
            for (int c = 0; c <= 9; c = c + 3) {
                float x = (c * cellWidth) + paddingLeft;
                canvas.drawRect(x - sectorLineWidth1, paddingTop - sectorLineWidth1, x + sectorLineWidth2, height + sectorLineWidth2, mSectorLinePaint);
            }

            // отрисовка толстых горизонтальных линий
            for (int r = 0; r <= 9; r = r + 3) {
                float y = r * cellHeight + paddingTop;
                canvas.drawRect(paddingLeft - sectorLineWidth1, y - sectorLineWidth1, width + sectorLineWidth2, y + sectorLineWidth2, mSectorLinePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mReadonly) {
            int x = (int) event.getX() - 10;
            int y = (int) event.getY() - 10;

            if (x < 0) x = 0;
            if (y < 0) y = 0;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    mTouchedCell = getCellAtPoint(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    mSelectedCell = getCellAtPoint(x, y);
                    invalidate(); // selected cell has changed, update board as soon as you can

                    if (mSelectedCell != null) {
                        onCellTapped(mSelectedCell);
                        onCellSelected(mSelectedCell);
                    }

                    if (mAutoHideTouchedCellHint) {
                        mTouchedCell = null;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mTouchedCell = null;
                    break;
            }
            postInvalidate();
        }

        return !mReadonly;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!mReadonly) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    return moveCellSelection(0, -1);
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    return moveCellSelection(1, 0);
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    return moveCellSelection(0, 1);
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    return moveCellSelection(-1, 0);
                case KeyEvent.KEYCODE_0:
                case KeyEvent.KEYCODE_SPACE:
                case KeyEvent.KEYCODE_DEL:
                    // clear value in selected cell
                    // TODO: I'm not really sure that this is thread-safe
                    if (mSelectedCell != null) {
                        if (event.isShiftPressed() || event.isAltPressed()) {
                            setCellNote(mSelectedCell, CellNote.EMPTY);
                        } else {
                            setCellValue(mSelectedCell, 0);
                            moveCellSelectionRight();
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    if (mSelectedCell != null) {
                        onCellTapped(mSelectedCell);
                    }
                    return true;
            }

            if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9) {
                int selNumber = keyCode - KeyEvent.KEYCODE_0;
                Cell cell = mSelectedCell;

                if (event.isShiftPressed() || event.isAltPressed()) {
                    // add or remove number in cell's note
                    setCellNote(cell, cell.getNote().toggleNumber(selNumber));
                } else {
                    // enter number in cell
                    setCellValue(cell, selNumber);
                    moveCellSelectionRight();
                }
                return true;
            }
        }


        return false;
    }


    /**
     * Moves selected cell by one cell to the right. If edge is reached, selection
     * skips on beginning of another line.
     */
    public void moveCellSelectionRight() {
        if (!moveCellSelection(1, 0)) {
            int selRow = mSelectedCell.getRowIndex();
            selRow++;
            if (!moveCellSelectionTo(selRow, 0)) {
                moveCellSelectionTo(0, 0);
            }
        }
        postInvalidate();
    }

    private void setCellValue(Cell cell, int value) {
        if (cell.isEditable()) {
            if (mGame != null) {
                mGame.setCellValue(cell, value);
            } else {
                cell.setValue(value);
            }
        }
    }

    private void setCellNote(Cell cell, CellNote note) {
        if (cell.isEditable()) {
            if (mGame != null) {
                mGame.setCellNote(cell, note);
            } else {
                cell.setNote(note);
            }
        }
    }


    /**
     * Moves selected by vx cells right and vy cells down. vx and vy can be negative. Returns true,
     * if new cell is selected.
     *
     * @param vx Horizontal offset, by which move selected cell.
     * @param vy Vertical offset, by which move selected cell.
     */
    private boolean moveCellSelection(int vx, int vy) {
        int newRow = 0;
        int newCol = 0;

        if (mSelectedCell != null) {
            newRow = mSelectedCell.getRowIndex() + vy;
            newCol = mSelectedCell.getColumnIndex() + vx;
        }

        return moveCellSelectionTo(newRow, newCol);
    }


    /**
     * Moves selection to the cell given by row and column index.
     *
     * @param row Row index of cell which should be selected.
     * @param col Columnd index of cell which should be selected.
     * @return True, if cell was successfuly selected.
     */
    private boolean moveCellSelectionTo(int row, int col) {
        if (col >= 0 && col < CellCollection.SUDOKU_SIZE
                && row >= 0 && row < CellCollection.SUDOKU_SIZE) {
            mSelectedCell = mCells.getCell(row, col);
            onCellSelected(mSelectedCell);

            postInvalidate();
            return true;
        }

        return false;
    }

    /**
     * Returns cell at given screen coordinates. Returns null if no cell is found.
     *
     * @param x
     * @param y
     * @return
     */
    private Cell getCellAtPoint(int x, int y) {
        if (mCells == null) {
            return null;
        }

        // take into account padding
        int lx = x - getPaddingLeft();
        int ly = y - getPaddingTop();

        int row = (int) (ly / cellHeight);
        int col = (int) (lx / cellWidth);

        if (col >= 0 && col < CellCollection.SUDOKU_SIZE
                && row >= 0 && row < CellCollection.SUDOKU_SIZE) {
            return mCells.getCell(row, col);
        } else {
            return null;
        }
    }

    /**
     * Occurs when user tap the cell.
     *
     * @author romario
     */
    public interface OnCellTappedListener {
        void onCellTapped(Cell cell);
    }

    /**
     * Occurs when user selects the cell.
     *
     * @author romario
     */
    public interface OnCellSelectedListener {
        void onCellSelected(Cell cell);
    }
}
