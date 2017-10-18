package hram.sudoku.activity;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import hram.sudoku.Constants;
import hram.sudoku.R;
import hram.sudoku.adapters.CapturedImagesAdapter;
import hram.sudoku.adapters.CellsAdapter;
import hram.sudoku.core.BinaryBitmap;
import hram.sudoku.core.GrayScaleBitmap;
import hram.sudoku.core.LuminanceSource;
import hram.sudoku.core.MultiFormatReader;
import hram.sudoku.core.RGBLuminanceSource;
import hram.sudoku.core.ReaderException;
import hram.sudoku.core.Utils;
import hram.sudoku.core.common.HybridBinarizer;
import hram.sudoku.result.Result;
import hram.sudoku.tess.TessAPI;
import hram.sudoku.utils.Cache;
import hram.sudoku.view.HorizontalListView;

public class TestTessAPI extends Fragment {
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Sudoku/";
    public static final String lang = "eng";

    private MultiFormatReader multiFormatReader;
    private Bitmap bitmap;
    private GridView grid;
    private ImageView imageView;
    private CellsAdapter cellsAdapter;
    private TessBaseAPI api;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rv = inflater.inflate(R.layout.activity_tess_test, container, false);

        api = TessAPI.getInstance(getActivity()).GetApi();

        imageView = (ImageView) rv.findViewById(R.id.imageView1);
        grid = (GridView) rv.findViewById(R.id.gridView1);

        // распознавание цифры
        grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                byte[] arr = cellsAdapter.GetCell(position);

                api.setImage(arr, cellsAdapter.GetCellWidth(), cellsAdapter.GetCellHeight(), 1, cellsAdapter.GetCellWidth());
                Toast.makeText(getActivity(), "Обнаружено " + api.getUTF8Text(), Toast.LENGTH_SHORT).show();
                /*
                Bitmap bmp = Bitmap.createBitmap(cellsAdapter.GetCellWidth(), cellsAdapter.GetCellHeight(), Bitmap.Config.ARGB_8888);
				bmp.setPixels(Utils.GraySkaleByteToInt(arr, cellsAdapter.GetCellWidth(), cellsAdapter.GetCellHeight()), 0, cellsAdapter.GetCellWidth(), 0, 0, cellsAdapter.GetCellWidth(), cellsAdapter.GetCellHeight());
				Toast.makeText(TestTessAPI.this, "Обнаружено " + GetCode(bmp), Toast.LENGTH_SHORT).show();
				*/
            }
        });

        HorizontalListView listview = (HorizontalListView) rv.findViewById(R.id.listview);
        listview.setAdapter(new CapturedImagesAdapter(getActivity()));

        // выбор судоку для анализа (клик по судоку из горизонтального списка)
        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {

                String name = (String) view.getTag();
                ShowRescaled(name);
            }
        });

        multiFormatReader = new MultiFormatReader();

        //ShowRescaled("2012-11-11 00-23-23.png");

        // поворот влево
        rv.findViewById(R.id.btToLeft).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Matrix matrix = new Matrix();
                matrix.postRotate(-90);

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                imageView.setImageBitmap(bitmap);//Cache.DecodeFile(this, fileName));
                SaveRescaled();
            }
        });

        // поворот вправо
        rv.findViewById(R.id.btToRight).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                imageView.setImageBitmap(bitmap);//Cache.DecodeFile(this, fileName));
                SaveRescaled();
            }
        });

        // вырезание клеток и распознавание
        rv.findViewById(R.id.btGetCells).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = (String) imageView.getTag();
                cellsAdapter = new CellsAdapter(getActivity(), fileName, api);
                grid.setAdapter(cellsAdapter);
            }
        });

        return rv;
    }

    private String GetCode(Bitmap bmp) {
        TessBaseAPI api = TessAPI.getInstance(getActivity()).GetApi();

        api.setImage(bmp);

        return api.getUTF8Text();
    }

    private void SaveRescaled() {
        String fileName = (String) imageView.getTag();

        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixels);
        byte[] arr = source.getMatrix();

        File rawFile = Cache.CteateFile(getActivity(), fileName + ".resc");

        FileOutputStream raw;
        try {
            raw = new FileOutputStream(rawFile);
            raw.write(arr);
            raw.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    private void ShowRescaled(String fileName) {
        try {

            File f = Cache.GetFile(getActivity(), fileName);

            Bitmap bm = BitmapFactory.decodeFile(f.getPath());
            int[] pixels = new int[bm.getWidth() * bm.getHeight()];
            bm.getPixels(pixels, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());

            LuminanceSource source = new RGBLuminanceSource(bm.getWidth(), bm.getHeight(), pixels);

            Result res = null;
            try {
                BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(source));
                res = multiFormatReader.decodeWithState(bb);
            } catch (ReaderException e) {
                return;
            }

            if (res == null) {
                Toast.makeText(getActivity(), "Судоку не найден", Toast.LENGTH_LONG).show();
                return;
            }

            res.TL.x += 6;
            res.TL.y += 6;

            res.TR.y += 6;
            res.BL.x += 6;

            // для нейронной сети нужны картинки 29 х 29 по идее надо 29 х 9 = 261
            GrayScaleBitmap rescaleImage = Utils.RescaleImage(new GrayScaleBitmap(source.getMatrix(), bm.getWidth(), bm.getHeight()), res.TL, res.TR, res.BL, res.BR, Constants.PREVIEW_SIZE, Constants.PREVIEW_SIZE);

            pixels = Utils.GraySkaleByteToInt(rescaleImage.GetBytes(), rescaleImage.GetWidth(), rescaleImage.GetHeight());

            bitmap = Bitmap.createBitmap(rescaleImage.GetWidth(), rescaleImage.GetHeight(), Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, rescaleImage.GetWidth(), 0, 0, rescaleImage.GetWidth(), rescaleImage.GetHeight());

            Matrix matrix = new Matrix();
            matrix.postRotate(180);

            //bitmap = Bitmap.createBitmap(bitmap, 0, 0, bm.GetWidth(), bm.GetHeight(), matrix, false);

            imageView.setImageBitmap(bitmap);//Cache.DecodeFile(this, fileName));
            imageView.setTag(fileName);

            //SaveRescaled();

        } catch (Exception e) {
        }
    }
    /*
    private GrayScaleBitmap rescale(String fileName) throws IOException
    {
		FileInputStream fis;
		FileOutputStream fos;
		
		File f = Cache.GetFile(this, fileName);
		
		Bitmap bm = BitmapFactory.decodeFile(f.getPath());
		int[] pixels = new int[bm.getWidth() * bm.getHeight()];
	    bm.getPixels(pixels, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
		
	    LuminanceSource source = new RGBLuminanceSource(bm.getWidth(), bm.getHeight(), pixels);
		
		Result res = null;
        try {
            BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(source));
            res = multiFormatReader.decodeWithState(bb);
        } catch (ReaderException e) {
            return null;
        }
        
        if(res == null)
        {
        	Toast.makeText(this, "Судоку не найден", 1).show();
        	return null;
        }
        
        res.TL.x += 6;
        res.TL.y += 6;
        
        res.TR.y += 6;
        res.BL.x += 6;
        
        // для нейронной сети нужны картинки 29 х 29 по идее надо 29 х 9 = 261  
        GrayScaleBitmap rescaleImage = Utils.RescaleImage(new GrayScaleBitmap(source.getMatrix(), bm.getWidth(), bm.getHeight()), res.TL, res.TR, res.BL, res.BR, Constants.PREVIEW_SIZE, Constants.PREVIEW_SIZE);
        
        //fos = new FileOutputStream(new File(f.getPath() + "resc"));
        //fos.write(rescaleImage.GetBytes());
        //fos.close();
        
        return rescaleImage;
    }
    */
}
