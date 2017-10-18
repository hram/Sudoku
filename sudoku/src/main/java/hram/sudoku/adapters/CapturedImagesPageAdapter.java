package hram.sudoku.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import hram.sudoku.R;

public class CapturedImagesPageAdapter extends PagerAdapter {
    CapturedImagesAdapter adapter;
    LayoutInflater inflater;

    public CapturedImagesPageAdapter(Context context, CapturedImagesAdapter adapter) {
        this.adapter = adapter;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public Object instantiateItem(View collection, int position) {
        View page = inflater.inflate(R.layout.item_page_adapter, null);
        ImageView image = (ImageView) page.findViewById(R.id.image);
        image.setImageBitmap(adapter.getBitmap(position));
        ((ViewPager) collection).addView(page, 0);
        return page;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
        ((ViewPager) collection).removeView((View) view);
    }

    @Override
    public int getCount() {
        return adapter.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

}
