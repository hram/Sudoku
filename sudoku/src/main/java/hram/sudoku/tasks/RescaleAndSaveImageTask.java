package hram.sudoku.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.FileOutputStream;

import hram.sudoku.Constants;
import hram.sudoku.R;
import hram.sudoku.core.Utils;
import hram.sudoku.game.SudokuGame;
import hram.sudoku.result.Result;
import hram.sudoku.utils.IProgressPublisher;
import hram.sudoku.view.Sudoku;

public class RescaleAndSaveImageTask extends AsyncTask<Void, Integer, Void> implements IProgressPublisher {
    private Context context;
    private ProgressDialog pdia;
    private Bitmap bm;
    private byte[] array;
    // размер захваченной с камеры картинки (полный кадр)
    private int width, height;
    // размер окна в котором производится поиск
    private Rect framingRect;
    private Result res;
    private int orientation;
    private Sudoku board;
    private Bitmap rescaleBitmap;
    private SudokuGame game;

    public RescaleAndSaveImageTask(Context context, Sudoku board, SudokuGame game) {
        this.context = context;
        this.board = board;
        this.game = game;
        this.array = game.getBitmapRaw();
        this.width = game.getBitmapRawWidth();
        this.height = game.getBitmapRawHeight();
        this.framingRect = game.getFramingRect();
        this.res = game.getResult();
        this.orientation = game.getOrientation();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pdia = new ProgressDialog(context);
        pdia.setMessage(context.getString(R.string.msg_process_image));
        pdia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pdia.setMax(100);
        pdia.show();
    }

    @Override
    protected Void doInBackground(Void... params) {

        // картинка будет разрешением как у рамки захвата
        int bmWidth = framingRect.width();
        int bmHeight = framingRect.height();

        try {

            int[] argb = Utils.decodeYUV420SP(array, width, height, framingRect);
            bm = Bitmap.createBitmap(bmWidth, bmHeight, Bitmap.Config.ARGB_8888);

            // argb
            bm.setPixels(argb, 0, bmWidth, 0, 0, bmWidth, bmHeight);

        } catch (Exception e) {
            return null;
        }

        // !!! тут можно ускорить если работать с int[] argb
        // кадрирование картинки в высоком качестве
        rescaleBitmap = Utils.RescaleImage(bm, res.TL, res.TR, res.BL, res.BR, bmWidth, bmHeight, this);

        if (orientation != 0) {
            Matrix rotateMatrix = new Matrix();
            rotateMatrix.postRotate(orientation);

            rescaleBitmap = Bitmap.createBitmap(rescaleBitmap, 0, 0, bmWidth, bmHeight, rotateMatrix, false);
        }

        // сжатие картинки
        //ByteArrayOutputStream out = new ByteArrayOutputStream();
        //rescaleBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        //array = out.toByteArray();

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        pdia.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        /*
        Log.d(Constants.TAG, "RescaleImageTask.onPostExecute()");
		
		SudokuGame s = new SudokuGame();
		s.setCells(CellCollection.fromString(sudokuData));
		
		long sudokuID = mDatabase.insertSudoku(1, s, array, framingRect.width(), framingRect.height());
		
		pdia.dismiss();
		
		playSudoku(sudokuID);
		*/

        // сохранение кропнутой картинки как массива
        FileOutputStream out;
        try {
            SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
            gameSettings.edit().putLong(Constants.LAST_CROPED_SUDOKU_ID, -1).apply();

            out = context.openFileOutput(Constants.LAST_CROPED_SUDOKU_NAME, 0);

            rescaleBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

            gameSettings.edit().putLong(Constants.LAST_CROPED_SUDOKU_ID, game.getId()).apply();
        } catch (Exception e) {
        }

        game.setCropedBitmap(rescaleBitmap);
        board.changeViewMode(rescaleBitmap);
        pdia.dismiss();
    }

    @Override
    public void PublishProgress(int value) {
        publishProgress(value);
    }

}
