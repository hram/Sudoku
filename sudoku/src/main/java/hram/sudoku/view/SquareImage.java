package hram.sudoku.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import hram.sudoku.R;

public class SquareImage extends ImageView {
    public static final int DEFAULT_BOARD_SIZE = 100;

    public static enum FixedAlong {
        width,
        height
    }

    private FixedAlong fixedAlong = FixedAlong.width;

    public SquareImage(Context context) {
        super(context);
    }

    public SquareImage(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SquareImage, 0, 0);

        fixedAlong = FixedAlong.valueOf(array.getString(R.styleable.SquareImage_fixedAlong));
        if (fixedAlong == null) fixedAlong = FixedAlong.width;

        array.recycle();
    }

    public SquareImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    int squareDimen = 1;

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Log.d("SquareImage", "widthMode=" + getMeasureSpecModeString(widthMode));
        //Log.d("SquareImage", "widthSize=" + widthSize);
        //Log.d("SquareImage", "heightMode=" + getMeasureSpecModeString(heightMode));
        //Log.d("SquareImage", "heightSize=" + heightSize);

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

        int square = (fixedAlong == FixedAlong.width) ? width : height;

        if (square > squareDimen) {
            squareDimen = square;
        }

        setMeasuredDimension(squareDimen, squareDimen);
    }

//	private String getMeasureSpecModeString(int mode) {
//		String modeString = null;
//		switch (mode) {
//		case MeasureSpec.AT_MOST:
//			modeString = "MeasureSpec.AT_MOST";
//			break;
//		case MeasureSpec.EXACTLY:
//			modeString = "MeasureSpec.EXACTLY";
//			break;
//		case MeasureSpec.UNSPECIFIED:
//			modeString = "MeasureSpec.UNSPECIFIED";
//			break;
//		}
//		
//		if (modeString == null)
//			modeString = new Integer(mode).toString();
//		
//		return modeString;
//	}
}
