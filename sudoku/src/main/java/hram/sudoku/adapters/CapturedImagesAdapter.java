package hram.sudoku.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;

import hram.sudoku.utils.IOUtil;

public class CapturedImagesAdapter extends BaseAdapter {
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Sudoku/";
    private static int THUMBNAIL_SIZE = 140;
    File[] files;
    private Context context;

    public CapturedImagesAdapter(Context context) {
        this.context = context;
        files = IOUtil.FindFiles(DATA_PATH, "png");
    }

    public static boolean IsEmpty() {
        return IOUtil.FindFiles(DATA_PATH, "png") != null ? false : true;
    }

    @Override
    public int getCount() {
        return files != null ? files.length : 0;
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
        //Bitmap bm = BitmapFactory.decodeFile(files[position].getPath());

        ImageView iv;
        if (convertView == null) {
            convertView = new ImageView(context);
        }

        iv = (ImageView) convertView;
        iv.setImageBitmap(decodeFile(files[position].getPath()));
        iv.setTag(files[position].getName());
        return iv;
    }

    public Bitmap getBitmap(int position) {
        return decodeFile(files[position].getPath());
    }

    public String getFileName(int position) {
        return files[position].getPath();
    }

    private Bitmap decodeFile(String file) {
        /*
		return  MediaStore.Images.Thumbnails.getThumbnail(
                context.getContentResolver(), Uri.fromFile(file),
                MediaStore.Images.Thumbnails.MINI_KIND,
                (BitmapFactory.Options) null );
		*/
        File image = new File(file);

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image.getPath(), bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
            return null;

        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / THUMBNAIL_SIZE;
        return BitmapFactory.decodeFile(image.getPath(), opts);
    }
}
