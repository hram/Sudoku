package hram.sudoku.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import hram.sudoku.BuildConfig;
import hram.sudoku.Constants;
import hram.sudoku.R;
import hram.sudoku.core.GrayScaleBitmap;
import hram.sudoku.core.LuminanceSource;
import hram.sudoku.core.PlanarYUVLuminanceSource;
import hram.sudoku.core.SudokuBitmap;
import hram.sudoku.core.Utils;
import hram.sudoku.core.common.BitMatrix;
import hram.sudoku.data.SudokuDatabase;
import hram.sudoku.game.Cell;
import hram.sudoku.game.CellCollection;
import hram.sudoku.game.SudokuGame;
import hram.sudoku.result.Result;
import hram.sudoku.solvers.SudSolver;
import hram.sudoku.tess.TessAPI;
import hram.sudoku.utils.IOUtil;
import hram.sudoku.utils.IProgressPublisher;
import hram.sudoku.utils.Log;
import hram.sudoku.view.Sudoku;

public class PreviewActivity extends BaseActivity {
    private Result res;
    // размер захваченной с камеры картинки (полный кадр)
    private int width, height;
    // размер окна в котором производится поиск
    private Rect framingRect;
    private int fileType;
    // картинка полного размерa
    //private byte[] raw = null;

    private boolean wasRotated;
    private float degrees;
    private boolean wasRescaled;
    private int orientation;

    private TextView tv;
    private Bitmap bm;
    private hram.sudoku.view.Sudoku sudokuView;

    private SudokuDatabase mDatabase;
    private String sudokuData;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (isNeedToContinueGame()) {
            Intent intent = new Intent(this, PlayActivity.class);
            intent.putExtra(Constants.EXTRA_SUDOKU_ID, sudokuGameID);
            startActivity(intent);
            finish();
            return;
        }

        // если не первое открытие окна
        if (icicle != null) {
            // если уже поворачивалась
            if (icicle.containsKey("wasRotated")) {
                wasRotated = icicle.getBoolean("wasRotated");
                degrees = icicle.getFloat("degrees");
            }

            // если уже отмасштабированная
            if (icicle.containsKey("wasRescaled")) {
                wasRescaled = icicle.getBoolean("wasRescaled");
            }

            if (icicle.containsKey("sudokuData")) {
                sudokuData = icicle.getString("sudokuData");
            }

            if (icicle.containsKey("orientation")) {
                orientation = icicle.getInt("orientation");
            }

            if (icicle.containsKey("framingRect")) {
                framingRect = icicle.getParcelable("framingRect");
            }
        }

        setContentView(R.layout.activity_preview);

        // чтение данных о судоку
        Bundle bundle = getIntent().getExtras();
        bundle.setClassLoader(Result.class.getClassLoader());
        res = (Result) bundle.getParcelable(Constants.SUDOKU_REGION);
        width = bundle.getInt(Constants.SUDOKU_WIDTH);
        height = bundle.getInt(Constants.SUDOKU_HEIGHT);
        fileType = bundle.getInt(Constants.SUDOKU_FILE_TYPE);
        framingRect = bundle.getParcelable(Constants.SUDOKU_FRAMING_RECT);

        sudokuView = (Sudoku) findViewById(R.id.sudoku);

        tv = (TextView) findViewById(R.id.textView1);
        tv.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        mDatabase = new SudokuDatabase(getApplicationContext());

        try {

            // если была отмасштабированна то читаем из файла
            if (wasRescaled) {
                FileInputStream fis = openFileInput(Constants.RESCALED_SUDOKU_NAME);
                bm = BitmapFactory.decodeStream(fis);
                fis.close();
                sudokuView.setBitmap(bm);
            }
            // иначе запускаем процесс масштабирования
            else {
                new RescaleImageTask().execute();
            }
        } catch (Exception e) {
            finish();
        }
        /*
        findViewById(R.id.toLeft).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				orientation -= 90;
				new RescaleImageTask().execute();
				//Rotate(-90);
			}
		});
	    
	    findViewById(R.id.toRight).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				orientation += 90;
				new RescaleImageTask().execute();
				//Rotate(90);
			}
		});
	    */
        // играть
        findViewById(R.id.btNext).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FileInputStream fis = openFileInput(Constants.LAST_SUDOKU_NAME);
                    byte[] raw = IOUtil.toByteArray(fis);
                    fis.close();

                    SudokuGame s = new SudokuGame();
                    s.setCells(CellCollection.fromString(sudokuData));

                    // сохранение картинки размером окна в котором производится поиск
                    long sudokuID = mDatabase.insertSudoku(1, s, raw, width, height, framingRect, res, orientation);
                    playSudoku(sudokuID);

                } catch (Exception e) {
                }
            }
        });

        // решить судоку и показать результат тут же
        findViewById(R.id.btSolve).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SudSolver solver = new SudSolver(sudokuData);

                    if (solver.AreAllCellsValid() == false || solver.SolveMe() == false) {
                        ShowSudSolverError();
                        return;
                    }

                    String solved = solver.toString();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < 81; i++) {
                        if (sudokuData.charAt(i) == '0') {
                            sb.append(solved.charAt(i));
                        } else {
                            sb.append("0");
                        }
                    }

                    CellCollection cells = CellCollection.deserialize(sb.toString());
                    for (int row = 0; row < 9; row++) {
                        for (int col = 0; col < 9; col++) {
                            Cell cell = cells.getCell(row, col);
                            cell.setEditable(cell.getValue() != 0);
                        }
                    }
                    //SudokuGame game = new SudokuGame();
                    //game.setCells(CellCollection.deserialize(sb.toString()));
                    //sudokuView.setGame(game);
                    sudokuView.setReadOnly(true);
                    sudokuView.setCells(cells);

                } catch (Exception e) {
                    ShowSudSolverError();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sudokuData != null) {
            tv.setText(sudokuData);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SaveRescaled();
        outState.putBoolean("wasRotated", wasRotated);
        outState.putBoolean("wasRescaled", wasRescaled);
        outState.putFloat("degrees", degrees);
        outState.putString("sudokuData", sudokuData);
        outState.putInt("orientation", orientation);
        outState.putParcelable("framingRect", framingRect);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.close();
    }

    private void SaveRescaled() {
        // сохранение кропнутой картинки как массива
        FileOutputStream out;
        try {
            out = openFileOutput(Constants.RESCALED_SUDOKU_NAME, 0);

            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

            wasRescaled = true;

        } catch (Exception e) {
            wasRescaled = false;
        }
    }

    private void playSudoku(long sudokuID) {
        Intent i = new Intent(this, PlayActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Constants.EXTRA_SUDOKU_ID, sudokuID);
        startActivity(i);
    }

    private void ShowSudSolverError() {
        Toast.makeText(PreviewActivity.this, "Ошибка распознавания сдоку. Попробуйте отсканировать еще раз.", Toast.LENGTH_LONG).show();
    }

    class RescaleImageTask extends AsyncTask<Void, Integer, Void> implements IProgressPublisher {
        private ProgressDialog pdia;
        private StringBuilder sb = new StringBuilder();
        private TessBaseAPI api;
        private SudokuBitmap sbm;
        private BitMatrix bitMatrix;

        // плоскость картинки для ускорения операций
        // необходим размер в int так как регионов может быть больше 128
        private int[][] indexes = new int[Constants.PREVIEW_SIZE][Constants.PREVIEW_SIZE];

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pdia = new ProgressDialog(PreviewActivity.this);
            pdia.setMessage(getString(R.string.msg_process_image));
            pdia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pdia.setMax(100);
            pdia.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ProcessSudoku();
            } catch (Exception e) {
                Log.e("RescaleImageTask", "Ошибка поиска судоку", e);
                finish();
            }
            return null;
        }

        private void ProcessSudoku() {
            Log.d("RescaleImageTask", "RescaleImageTask.doInBackground()");

            api = TessAPI.getInstance(PreviewActivity.this).GetApi();

            GrayScaleBitmap rescaleImage = null;

            int bmWidth = framingRect.width();
            int bmHeight = framingRect.height();

            try {
                FileInputStream fis = openFileInput(Constants.LAST_SUDOKU_NAME);

                switch (fileType) {
                    case Constants.FILE_TYPE_YUV420SP:
                        Log.d("RescaleImageTask", "Обработка в формате FILE_TYPE_YUV420SP");
                        LuminanceSource source = new PlanarYUVLuminanceSource(IOUtil.toByteArray(fis), width, height, framingRect.left, framingRect.top, bmWidth, bmHeight, false);

                        // для нейронной сети нужны картинки 29 х 29 по идее надо 29 х 9 = 261
                        rescaleImage = Utils.RescaleImage(new GrayScaleBitmap(source.getMatrix(), bmWidth, bmHeight), res.TL, res.TR, res.BL, res.BR, Constants.PREVIEW_SIZE, Constants.PREVIEW_SIZE, this);
                        break;
                    case Constants.FILE_TYPE_ARGB_8888:
                        Log.d("RescaleImageTask", "Обработка в формате FILE_TYPE_ARGB_8888");
                        bm = BitmapFactory.decodeStream(fis);
                }

                fis.close();

            } catch (Exception e) {
                Log.e("RescaleImageTask", "Ошибка чтения файла", e);
                finish();
                return;
            }

            try {
                Log.d("RescaleImageTask", "Получение SudokuBitmap и BitMatrix");

                // откадрированная картинка размером Constants.PREVIEW_SIZE х Constants.PREVIEW_SIZE
                sbm = new SudokuBitmap(rescaleImage.GetBytes(), Constants.PREVIEW_SIZE, Constants.PREVIEW_SIZE);

                bitMatrix = sbm.GetBlackMatrix();
            } catch (Exception e) {
                Log.e("RescaleImageTask", "Ошибка получение ч/б картинки", e);
                finish();
                return;
            }

            Log.d("RescaleImageTask", "Инициализация indexes[][]");
            int right = Constants.PREVIEW_SIZE - 1;
            int bottom = Constants.PREVIEW_SIZE - 1;
            for (int y = 0; y < Constants.PREVIEW_SIZE; y++) {
                indexes[y][0] = 1;
                indexes[y][Constants.CELL_SIZE * 1] = 1;
                indexes[y][Constants.CELL_SIZE * 2] = 1;
                indexes[y][Constants.CELL_SIZE * 3] = 1;
                indexes[y][Constants.CELL_SIZE * 4] = 1;
                indexes[y][Constants.CELL_SIZE * 5] = 1;
                indexes[y][Constants.CELL_SIZE * 6] = 1;
                indexes[y][Constants.CELL_SIZE * 7] = 1;
                indexes[y][Constants.CELL_SIZE * 8] = 1;
                indexes[y][right] = 1;

                indexes[y][1] = 1;
                indexes[y][Constants.CELL_SIZE * 1 - 1] = 1;
                indexes[y][Constants.CELL_SIZE * 2 - 1] = 1;
                indexes[y][Constants.CELL_SIZE * 3 - 1] = 1;
                indexes[y][Constants.CELL_SIZE * 4 - 1] = 1;
                indexes[y][Constants.CELL_SIZE * 5 - 1] = 1;
                indexes[y][Constants.CELL_SIZE * 6 - 1] = 1;
                indexes[y][Constants.CELL_SIZE * 7 - 1] = 1;
                indexes[y][Constants.CELL_SIZE * 8 - 1] = 1;
                indexes[y][right - 1] = 1;

                indexes[y][Constants.CELL_SIZE * 1 - 2] = 1;
                indexes[y][Constants.CELL_SIZE * 2 - 2] = 1;
                indexes[y][Constants.CELL_SIZE * 3 - 2] = 1;
                indexes[y][Constants.CELL_SIZE * 4 - 2] = 1;
                indexes[y][Constants.CELL_SIZE * 5 - 2] = 1;
                indexes[y][Constants.CELL_SIZE * 6 - 2] = 1;
                indexes[y][Constants.CELL_SIZE * 7 - 2] = 1;
                indexes[y][Constants.CELL_SIZE * 8 - 2] = 1;
            }

            for (int x = 0; x < Constants.PREVIEW_SIZE; x++) {
                indexes[0][x] = 1;
                indexes[Constants.CELL_SIZE * 1][x] = 1;
                indexes[Constants.CELL_SIZE * 2][x] = 1;
                indexes[Constants.CELL_SIZE * 3][x] = 1;
                indexes[Constants.CELL_SIZE * 4][x] = 1;
                indexes[Constants.CELL_SIZE * 5][x] = 1;
                indexes[Constants.CELL_SIZE * 6][x] = 1;
                indexes[Constants.CELL_SIZE * 7][x] = 1;
                indexes[Constants.CELL_SIZE * 8][x] = 1;
                indexes[bottom][x] = 1;

                indexes[1][x] = 1;
                indexes[Constants.CELL_SIZE * 1 - 1][x] = 1;
                indexes[Constants.CELL_SIZE * 2 - 1][x] = 1;
                indexes[Constants.CELL_SIZE * 3 - 1][x] = 1;
                indexes[Constants.CELL_SIZE * 4 - 1][x] = 1;
                indexes[Constants.CELL_SIZE * 5 - 1][x] = 1;
                indexes[Constants.CELL_SIZE * 6 - 1][x] = 1;
                indexes[Constants.CELL_SIZE * 7 - 1][x] = 1;
                indexes[Constants.CELL_SIZE * 8 - 1][x] = 1;

                indexes[Constants.CELL_SIZE * 1 + 1][x] = 1;
                indexes[Constants.CELL_SIZE * 2 + 1][x] = 1;
                indexes[Constants.CELL_SIZE * 3 + 1][x] = 1;
                indexes[Constants.CELL_SIZE * 4 + 1][x] = 1;
                indexes[Constants.CELL_SIZE * 5 + 1][x] = 1;
                indexes[Constants.CELL_SIZE * 6 + 1][x] = 1;
                indexes[Constants.CELL_SIZE * 7 + 1][x] = 1;
                indexes[Constants.CELL_SIZE * 8 + 1][x] = 1;

                indexes[bottom - 1][x] = 1;
            }

            Log.d("RescaleImageTask", "Помечаем на плоскости черные точки");
            // помечаем на плоскости черные точки
            // в дальнейшем лучше если вся работа будет с этой плоскостью
            for (int y = 2; y < sbm.GetHeight() - 2; y++) {
                for (int x = 2; x < sbm.GetWidth() - 2; x++) {
                    if (bitMatrix.get(x, y)) {
                        indexes[y][x] = 1;
                    }
                }
            }

            Log.d("RescaleImageTask", "Алгоритсм поиска регионов");
            int lastProgress = 0;

            // статистика по регионам
            int[] counts = new int[1000];
            // количеств регионов
            int neighbors = 0;
            int index = 1;

            int kn, km;
            int a, b, c;
            // алгоритсм поиска регионов
            // в indexes помечается индекс региона начиная с 2
            // в counts количество точек в каждом регионе
            for (int y = 1; y < Constants.PREVIEW_SIZE; y++) {
                for (int x = 1; x < Constants.PREVIEW_SIZE; x++) {
                    a = indexes[y][x];
                    if (a == 0) {
                        continue;
                    }

                    kn = x - 1;
                    if (kn == 0) {
                        kn = 1;
                        c = 0;
                    } else {
                        c = indexes[y][kn];
                    }

                    km = y - 1;
                    if (km == 0) {
                        km = 1;
                        b = 0;
                    } else {
                        b = indexes[km][x];
                    }

                    if (b == 0 & c == 0) {
                        index++;
                        counts[index]++;
                        indexes[y][x] = index;
                        neighbors++;
                    } else if (b != 0 & c == 0) {
                        indexes[y][x] = b;
                        counts[b]++;
                    } else if (b == 0 & c != 0) {
                        indexes[y][x] = c;
                        counts[c]++;
                    } else if (b != 0 & c != 0) {
                        if (b == c) {
                            indexes[y][x] = b;
                        } else {
                            indexes[y][x] = b;
                            ReNumber(c, b);
                            counts[b] = counts[b] + counts[c];
                            counts[c] = 0;
                        }
                    }
                }


                int currentProgress = (int) (y * 100F / Constants.PREVIEW_SIZE);
                if ((currentProgress - lastProgress) >= 10) {
                    PublishProgress(currentProgress);
                    lastProgress = currentProgress;
                }
            }

            Log.d("RescaleImageTask", "На плоскости indexes заливаем большие и маленькие регионы белым");
            // ? в исходной картинке bitMatrix заливаем большие и маленькие регионы белым bitMatrix.flip(x, y)
            // на плоскости indexes заливаем большие и маленькие регионы белым bitMatrix.flip(x, y)
            for (int y = 0; y < sbm.GetHeight() - 0; y++) {
                for (int x = 0; x < sbm.GetWidth() - 0; x++) {
                    int value = indexes[y][x];
                    if (value > 0 && (counts[value] > 100 || counts[value] < 12)) {
                        //if(Constants.DEBUG && bm != null)
                        //{
                        //	// подсвечиваем на картинке крассным что было удалено
                        //	bm.setPixel(x, y, Color.fromArgb(0xFF, 0, 0).Value);
                        //}

                        indexes[y][x] = 0;
                    }
                    //else if(Constants.DEBUG && bm != null && bitMatrix.get(x, y))
                    //{
                    //	// подсвечиваем на картинке синим нужные нам цифры
                    //	bm.setPixel(x, y, Color.fromArgb(0, 0, 0xFF).Value);
                    //}
                }
            }

            int errorrs[] = new int[4];
            int cellWidth = sbm.GetCellWidth();
            int cellHeight = sbm.GetCellHeight();

            StringBuilder sb0 = new StringBuilder();
            StringBuilder sb90 = new StringBuilder();
            StringBuilder sb180 = new StringBuilder();
            StringBuilder sb270 = new StringBuilder();

            lastProgress = 0;
            Log.d("RescaleImageTask", "Распознаем цифры");
            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    int currentProgress = (y * 9 + x) * 100 / 81;
                    if ((currentProgress - lastProgress) >= 10) {
                        PublishProgress(currentProgress);
                        lastProgress = currentProgress;
                    }

                    // ячейка в градациях серого
                    byte[] cell = sbm.GetCell(y, x, indexes);

                    // если пустая ячейка (большая часть или все белые)
                    if (sbm.IsEmpty(cell)) {
                        sb0.append("0");
                        sb90.append("0");
                        sb180.append("0");
                        sb270.append("0");
                        continue;
                    }

                    String s0;
                    if (errorrs[0] < 3) {
                        s0 = recognise(cell, cellWidth, cellHeight);
                        if (s0.equals("#")) {
                            errorrs[0]++;
                        }
                    } else {
                        s0 = "%";
                    }
                    sb0.append(s0);

                    String s90;
                    if (errorrs[1] < 3) {
                        s90 = recognise(sbm.GetCell(y, x, 90, indexes), cellWidth, cellHeight);
                        if (s90.equals("#")) {
                            errorrs[1]++;
                        }
                    } else {
                        s90 = "%";
                    }
                    sb90.append(s90);

                    String s180;
                    if (errorrs[2] < 3) {
                        s180 = recognise(sbm.GetCell(y, x, 180, indexes), cellWidth, cellHeight);
                        if (s180.equals("#")) {
                            errorrs[2]++;
                        }
                    } else {
                        s180 = "%";
                    }
                    sb180.append(s180);

                    String s270;
                    if (errorrs[3] < 3) {
                        s270 = recognise(sbm.GetCell(y, x, 270, indexes), cellWidth, cellHeight);
                        if (s270.equals("#")) {
                            errorrs[3]++;
                        }
                    } else {
                        s270 = "%";
                    }
                    sb270.append(s270);

                    Log.d("RescaleImageTask", String.format("y = %d x = %d\r\ns0 = %s\r\ns90 = %s\r\ns180 = %s\r\ns270 = %s\r\n", y, x, s0, s90, s180, s270));
                }
            }

            Log.d("RescaleImageTask", String.format("errors[%d %d %d %d]", errorrs[0], errorrs[1], errorrs[2], errorrs[3]));

            if (errorrs[0] == 0 && isSudokuValid(sb0.toString())) {
                sb.append(sb0);
                orientation = 0;
            } else if (errorrs[1] == 0 && isSudokuValid(sb90.toString())) {
                String tmp = sb90.toString();
                for (int y = 0; y < 9; y++) {
                    for (int x = 0; x < 9; x++) {
                        sb.append(tmp.charAt(((8 - x) * 9) + y));
                    }
                }
                orientation = 90;
            } else if (errorrs[2] == 0 && isSudokuValid(sb180.toString())) {
                sb.append(sb180.reverse());
                orientation = 180;
            } else if (errorrs[3] == 0 && isSudokuValid(sb270.toString())) {
                sb.append(sb270);
                orientation = 270;
            }

            try {
                // для отрисовки кадрированной картинки
                Log.d("RescaleImageTask", "Масштабирование картинки для отрисовки");
                bm = Bitmap.createBitmap(Constants.PREVIEW_SIZE, Constants.PREVIEW_SIZE, Bitmap.Config.ARGB_8888);
                bm.setPixels(Utils.GraySkaleByteToInt(rescaleImage.GetBytes(), Constants.PREVIEW_SIZE, Constants.PREVIEW_SIZE), 0, Constants.PREVIEW_SIZE, 0, 0, Constants.PREVIEW_SIZE, Constants.PREVIEW_SIZE);

                if (orientation != 0) {
                    Matrix rotateMatrix = new Matrix();
                    rotateMatrix.postRotate(orientation);

                    bm = Bitmap.createBitmap(bm, 0, 0, Constants.PREVIEW_SIZE, Constants.PREVIEW_SIZE, rotateMatrix, false);

                    //rescaleImage = new GrayScaleBitmap(rescaleImage.GetRotate(orientation), Constants.PREVIEW_SIZE, Constants.PREVIEW_SIZE);
                }
            } catch (Exception e) {
                Log.e("RescaleImageTask", "Ошибка масштабирования для отрисовки", e);
                finish();
                return;
            }
        }

        private boolean isSudokuValid(String data) {
            try {
                SudSolver solver = new SudSolver(data);

                return solver.AreAllCellsValid() && solver.SolveMe();
            } catch (Exception e) {
                return false;
            }
        }

        private String recognise(byte[] cell, int cellWidth, int cellHeight) {
            api.setImage(cell, cellWidth, cellHeight, 1, cellWidth);

            try {
                String str = api.getUTF8Text();
                if (str.length() == 1) {
                    return str;
                }
            } catch (Exception e) {
            }

            return "#";
        }

        private void ReNumber(int c, int b) {
            for (int y = 0; y < sbm.GetHeight(); y++) {
                for (int x = 0; x < sbm.GetWidth(); x++) {
                    if (indexes[y][x] == c) indexes[y][x] = b;
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            pdia.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Log.d("RescaleImageTask", "RescaleImageTask.onPostExecute()");

            if (pdia != null) {
                pdia.dismiss();
            }

            if (sb.length() == 0) {
                Intent intent = new Intent(PreviewActivity.this, RecognitionErrorActivity.class);
                intent.putExtras(getIntent().getExtras());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                finish();
                return;
            }

            sudokuData = sb.toString();

            try {
                SudSolver solver = new SudSolver(sudokuData);

                if (solver.AreAllCellsValid()) {
                    tv.setText("OK: " + sudokuData);
                } else {
                    tv.setText("NO: " + sudokuData);
                }
            } catch (Exception e) {
                tv.setText("SE: " + sudokuData);
            }

            if (bm != null) {
                sudokuView.setBitmap(bm);
            }
        }

        @Override
        public void PublishProgress(int value) {
            publishProgress(value);
        }

    }
}
