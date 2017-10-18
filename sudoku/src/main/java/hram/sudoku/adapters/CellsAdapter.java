package hram.sudoku.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import hram.sudoku.core.SudokuBitmap;
import hram.sudoku.core.Utils;
import hram.sudoku.core.common.BitMatrix;
import hram.sudoku.utils.Cache;
import hram.sudoku.utils.IOUtil;

public class CellsAdapter extends BaseAdapter {
    private Context context;
    private String fileName;
    private ArrayList<byte[]> cells = new ArrayList<byte[]>();
    private SudokuBitmap bm;
    private TessBaseAPI api;
    private Paint paint;

    public CellsAdapter(Context context, String fileName, TessBaseAPI api) {
        this.context = context;
        this.fileName = fileName;
        this.api = api;

        try {
            GetCells();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        //float cellTextSize = GetCellHeight() * 0.25f;
        //paint.setTextSize(cellTextSize);
        paint.setTextSize((int) (14 * scale));
        //paint.setStrokeWidth(20); // Text Size
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
    }

    @Override
    public int getCount() {
        return cells.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView iv;
        if (convertView == null) {
            convertView = new ImageView(context);
        }

        iv = (ImageView) convertView;

        int[] pixels = Utils.GraySkaleByteToInt(cells.get(position), bm.GetWidth() / 9, bm.GetHeight() / 9);
        // сохранение картинки в png формате
        Bitmap bitmap = Bitmap.createBitmap(bm.GetWidth() / 9, bm.GetHeight() / 9, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, bm.GetWidth() / 9, 0, 0, bm.GetWidth() / 9, bm.GetHeight() / 9);

        Matrix matrix = new Matrix();
        matrix.postScale(3f, 3f);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        byte[] arr = GetCell(position);
        api.setImage(arr, GetCellWidth(), GetCellHeight(), 1, GetCellWidth());

        String text = api.getUTF8Text();
        Canvas c = new Canvas(bitmap);
        c.drawText(text, 10, 20, paint);

        iv.setImageBitmap(bitmap);
        return iv;
    }

    public byte[] GetCell(int position) {
        return cells.get(position);
    }

    private void GetCells() throws IOException {
        File f = Cache.GetFile(context, fileName + ".resc");
        FileInputStream fis = new FileInputStream(f);

        byte[] arr = IOUtil.toByteArray(fis);
        fis.close();

        int dim = (int) Math.sqrt(arr.length);
        bm = new SudokuBitmap(arr, dim, dim); // 261

        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                // ячейка в градациях серого
                byte[] cell = bm.GetCell(y, x);

                // черно-белая ячейка
                BitMatrix matrix = bm.GetBlackMatrix(cell);
                if (matrix == null || bm.IsEmpty(matrix)) {
                    continue;
                }

                for (int ii = 0; ii < matrix.getHeight(); ++ii) // y
                {
                    for (int jj = 0; jj < matrix.getWidth(); ++jj) // x
                    {
                        byte gray = cell[jj + matrix.getWidth() * ii];
                        cell[jj + matrix.getWidth() * ii] = matrix.get(jj, ii) ? (byte) (gray & 0xFF) : (byte) 0xFF;
                    }
                }

                cells.add(cell);
            }
        }
    }

    public int GetCellWidth() {
        return bm.GetCellWidth();
    }

    public int GetCellHeight() {
        return bm.GetCellHeight();
    }
}
