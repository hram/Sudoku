package hram.sudoku;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import hram.sudoku.camera.CameraManager;
import hram.sudoku.result.ResultLine;
import hram.sudoku.result.ResultValue;
import hram.sudoku.result.ResultWeight;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View 
{
	//private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
	//private static final long ANIMATION_DELAY = 80L;
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	//private static final int MAX_RESULT_POINTS = 20;
	//private static final int POINT_SIZE = 6;
	private static final int MAX_WEIGHT_POINTS = 100;
	private static final int WEIGHTS_OFFSET = 20;
	private static final int MAX_WEIGHTS_VALUE = 70;
	private static final int OFFSET = 20;

	private CameraManager cameraManager;
	private final Paint paint, linesPaint;
	private final Paint spaint;
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int frameColor;
	private final int laserColor;
	private List<ResultValue> possibleResultLines;
	private float scaleX, scaleY;
	private int frameLeft, frameTop;
	private Canvas canvas;
	private int[] foundWeight = new int[MAX_WEIGHT_POINTS] ;
	private Rect frame;
	private Rect cameraFrame;
	private Rect sFrame;

	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);

		// Initialize these once for performance rather than calling them every time in onDraw().
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		// для отрисовки возможных линий
		linesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesPaint.setStyle(Paint.Style.STROKE);
		linesPaint.setColor(Color.GREEN);
		linesPaint.setStrokeWidth(10);
		
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		frameColor = resources.getColor(R.color.viewfinder_frame);
		laserColor = resources.getColor(R.color.viewfinder_laser);
		possibleResultLines = new ArrayList<ResultValue>();
		
		spaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		spaint.setStyle(Paint.Style.STROKE);
		spaint.setColor(laserColor);
		spaint.setStrokeWidth(2);
	}

	public void setCameraManager(CameraManager cameraManager) 
	{
		this.cameraManager = cameraManager;
	}

	@Override
	public void onDraw(Canvas canvas) 
	{
		// при просмотре в редакторе 
		if(cameraManager == null)
		{
			return;
		}
		
		this.cameraFrame = cameraManager.getFramingRect();
		this.frame = cameraManager.getScaledFramingRect();
		if(cameraFrame == null || frame == null)
		{
			return;
		}
		
		if(sFrame == null)
		{
			sFrame = new Rect(frame.left + OFFSET, frame.top + OFFSET, frame.right - OFFSET + 1, frame.bottom - OFFSET + 1);
		}
		
		this.canvas = canvas;
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		
		if (resultBitmap != null) {
	      // Draw the opaque result bitmap over the scanning rectangle
	      paint.setAlpha(CURRENT_POINT_OPACITY);
	      canvas.drawBitmap(resultBitmap, null, frame, null);
		}

		// отрисовка затемненой области вокруг окна захвата
		paint.setColor(maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		// отрисовка черной рамки в 2 пикселя внутри области захвата
		paint.setColor(frameColor);
		canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
		canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
		canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
		canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);
			  
		float step = (frame.bottom - frame.top - OFFSET * 2) / 3f;
		
		canvas.drawRect(sFrame.left, sFrame.top, sFrame.right, sFrame.bottom, spaint);

		canvas.drawLine(sFrame.left, sFrame.top + step, sFrame.right, sFrame.top + step, spaint);
		canvas.drawLine(sFrame.left, sFrame.top + step + 1, sFrame.right, sFrame.top + step + 1, spaint);
		canvas.drawLine(sFrame.left, sFrame.top + 2 * step, sFrame.right, sFrame.top + 2 * step, spaint);
		canvas.drawLine(sFrame.left, sFrame.top + 2 * step + 1, sFrame.right, sFrame.top + 2 * step + 1, spaint);

		step = (frame.right - frame.left - OFFSET * 2) / 3f;
		
		canvas.drawLine(sFrame.left + step, sFrame.top, sFrame.left + step, sFrame.bottom, spaint);
		canvas.drawLine(sFrame.left + step + 1, sFrame.top, sFrame.left + step + 1, sFrame.bottom, spaint);
		canvas.drawLine(sFrame.left + 2 * step, sFrame.top, sFrame.left + 2 * step, sFrame.bottom, spaint);
		canvas.drawLine(sFrame.left + 2 * step + 1, sFrame.top, sFrame.left + 2 * step + 1, sFrame.bottom, spaint);
	  
		scaleX = frame.width() / (float) cameraFrame.width();
		scaleY = frame.height() / (float) cameraFrame.height();

		frameLeft = frame.left;
		frameTop = frame.top;
		
		// отрисовка найденых граней
		//drawPossibleLines();
		
		// отрисовка графика весов
		//drawWeights();

		postInvalidate();
	}
  
	private void drawLine(ResultLine line)
	{
		//paint.setAlpha(CURRENT_POINT_OPACITY);
		//paint.setColor(Color.WHITE);
		
		Point p1 = line.getPoint1();
		Point p2 = line.getPoint2();
		
		float left = frameLeft + p1.x * scaleX;
		float top = frameTop + p1.y * scaleY;
		float right = frameLeft + p2.x * scaleX + 2;
		float bottom = frameTop + p2.y * scaleY + 2;
		//canvas.drawRect(left - 1, top - 1, right + 1, bottom + 1, linesPaint);
		
		canvas.drawLine(left, top, right, bottom, linesPaint);
	}
	
	private void drawPossibleLines()
	{
		List<ResultValue> currentPossible = possibleResultLines;
		if (currentPossible.isEmpty() == false) 
		{
			possibleResultLines = new ArrayList<ResultValue>();
			synchronized (currentPossible) 
			{
				for (ResultValue value : currentPossible) {
					drawLine((ResultLine)value);
				}
			}
		}
	}
	
	private void drawWeights()
	{
		// отрисовка рамки для весов
		paint.setColor(Color.BLACK);
		canvas.drawRect(WEIGHTS_OFFSET, WEIGHTS_OFFSET, MAX_WEIGHT_POINTS * 2 + WEIGHTS_OFFSET, MAX_WEIGHTS_VALUE + WEIGHTS_OFFSET, paint);
		
		paint.setColor(Color.WHITE);
		
		int i, left, top, right, bottom;
		for(i = 0; i < MAX_WEIGHT_POINTS; i++)
		{
			left = i * 2 + WEIGHTS_OFFSET;
			top = WEIGHTS_OFFSET;
			right = i * 2 + 2 + WEIGHTS_OFFSET;
			bottom = foundWeight[i] + WEIGHTS_OFFSET;
			canvas.drawRect(left, top, right, bottom, paint);
		}
		
		paint.setColor(Color.RED);
		for(i = 1; i < MAX_WEIGHTS_VALUE / 10; i++)
		{
			int y = i * 10 + WEIGHTS_OFFSET;
			canvas.drawLine(WEIGHTS_OFFSET, y, MAX_WEIGHT_POINTS * 2 + WEIGHTS_OFFSET, y, paint);
		}
	}

    /**
     * Очистка если есть картинки с судоку и перерисовка
     */
	public void drawViewfinder() 
	{
		Bitmap resultBitmap = this.resultBitmap;
	    this.resultBitmap = null;
	    if (resultBitmap != null) {
	      resultBitmap.recycle();
	    }
		invalidate();
	}

	/**
	* Draw a bitmap with the result points highlighted instead of the live scanning display.
	*
	* @param barcode An image of the decoded barcode.
	*/	
	public void drawResultBitmap(Bitmap barcode) 
	{
		resultBitmap = barcode;
		//invalidate();
	}

    public void addFoundWeightLines(List<ResultValue> value)
    {
        for (ResultValue v : value)
        {
            addWeight((ResultWeight)v);
        }
    }
  
	public void addPossibleResultLines(List<ResultValue> value) 
	{
        List<ResultValue> lines = possibleResultLines;
        synchronized (lines)
        {
            lines.clear();
            for (ResultValue v : value)
            {
                lines.add(v);
            }
        }
	}

    private void addWeight(ResultWeight value)
    {
        int[] weights = foundWeight;
        synchronized (weights)
        {
            for (int i = 0; i < MAX_WEIGHT_POINTS - 1; i++) {
                weights[i] = weights[i + 1];
            }

            weights[MAX_WEIGHT_POINTS - 1] = value.GetWeight();
        }
    }
}
