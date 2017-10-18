package hram.sudoku.activity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import hram.sudoku.Constants;
import hram.sudoku.R;
import hram.sudoku.adapters.CapturedImagesAdapter;
import hram.sudoku.adapters.CapturedImagesPageAdapter;
import hram.sudoku.core.BinaryBitmap;
import hram.sudoku.core.LuminanceSource;
import hram.sudoku.core.MultiFormatReader;
import hram.sudoku.core.RGBLuminanceSource;
import hram.sudoku.core.ReaderException;
import hram.sudoku.core.common.HybridBinarizer;
import hram.sudoku.result.Result;
import hram.sudoku.view.HorizontalListView;

public class GalleryActivity extends Fragment {
    private CapturedImagesAdapter adapter;
    private ViewPager pager;
    private MultiFormatReader multiFormatReader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rv = inflater.inflate(R.layout.activity_gallery, container, false);

        adapter = new CapturedImagesAdapter(getActivity());

        HorizontalListView listview = (HorizontalListView) rv.findViewById(R.id.lvSmall);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                pager.setCurrentItem(arg2);
            }
        });

        pager = (ViewPager) rv.findViewById(R.id.view_pager);
        pager.setAdapter(new CapturedImagesPageAdapter(getActivity(), adapter));

        multiFormatReader = new MultiFormatReader();

        // имитация захвата и отправка в вктивити превью
        rv.findViewById(R.id.button3).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = adapter.getFileName(pager.getCurrentItem());

                Bitmap bm = BitmapFactory.decodeFile(fileName);
                int[] pixels = new int[bm.getWidth() * bm.getHeight()];
                bm.getPixels(pixels, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());

                LuminanceSource source = new RGBLuminanceSource(bm.getWidth(), bm.getHeight(), pixels);

                Result res = null;
                try {
                    BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(source));
                    res = multiFormatReader.decodeWithState(bb);
                } catch (ReaderException e) {
                    Toast.makeText(getActivity(), "Ошибка обработки", Toast.LENGTH_LONG).show();
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

                FileOutputStream out;
                try {
                    out = getActivity().openFileOutput(Constants.LAST_SUDOKU_NAME, 0);
                    bm.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.close();

                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.FILE_NAME, fileName);
                    bundle.putParcelable(Constants.SUDOKU_REGION, res);
                    bundle.putParcelable(Constants.SUDOKU_FRAMING_RECT, new Rect(0, 0, bm.getWidth(), bm.getHeight()));
                    bundle.putInt(Constants.SUDOKU_WIDTH, bm.getWidth());
                    bundle.putInt(Constants.SUDOKU_HEIGHT, bm.getHeight());
                    bundle.putInt(Constants.SUDOKU_FILE_TYPE, Constants.FILE_TYPE_ARGB_8888);
                    //bundle.putInt(Constants.SUDOKU_ORIENTATION, 90);

                    Intent intent = new Intent(getActivity(), PreviewActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);

                } catch (FileNotFoundException e) {
                    Toast.makeText(getActivity(), "Ошибка обработки FileNotFoundException", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Ошибка обработки IOException", Toast.LENGTH_LONG).show();
                }
            }
        });

        return rv;
    }
}
