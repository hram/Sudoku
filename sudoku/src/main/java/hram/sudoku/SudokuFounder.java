package hram.sudoku;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hram.sudoku.core.BinaryBitmap;
import hram.sudoku.core.ChecksumException;
import hram.sudoku.core.DecodeHintType;
import hram.sudoku.core.FormatException;
import hram.sudoku.core.FoundWeightCallback;
import hram.sudoku.core.NotFoundException;
import hram.sudoku.core.Reader;
import hram.sudoku.core.ResultLinesCallback;
import hram.sudoku.core.common.BitMatrix;
import hram.sudoku.result.Result;
import hram.sudoku.result.ResultLine;
import hram.sudoku.result.ResultValue;
import hram.sudoku.result.ResultWeight;
import hram.sudoku.utils.Log;

public class SudokuFounder implements Reader
{	
	// количество точек анализа прямой
	private int count = 70;
	// глубина анализа
	private int deep = 40; // можно до 40 спустить
	private double oneGradus = Math.PI / 180.0;
	private int maxDimention;
	private int[][] arr;
	private int maxValue;
	private double p, theta;
	private double weightTop, weightBottom, weightLeft, weightRight;
	private float singleWeightMinLevel = 70F;
	private float averageWeightMinLevel = 80F;
	private int step; // шаг анализа зависит от количества точек анализа
	
	public SudokuFounder()
	{
		
	}
	
	public SudokuFounder(int count, int deep)
	{
		this.count = count;
		this.deep = deep;
	}

	@Override
	public Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints) throws ChecksumException, FormatException 
	{
		int blackPixels, allPixels;
		BitMatrix matrix = null;
		
		try {
			matrix = image.getBlackMatrix();
		}catch(NotFoundException e){
			Log.v("SudokuFounder", "Много черных NotFoundException");
	    	return null;
		}
	 
	    int dim = image.getHeight();
	    step = dim / count;
        maxDimention = dim + dim;
        //deep = dim / 20;
        
        allPixels = 0;
        blackPixels = 0;
        maxValue = 0;
	    arr = new int[20][maxDimention * 2];
	    for (int i = 1; i < count - 1; i++)
        {
            int x = step*i;
            for (int y = 0; y < deep; y++, allPixels++)
            {
                // черная точка
                if (matrix.get(x, y))
                {
                    SetPoint(x, y, 80, 100, -80);
                    blackPixels++;
                }
            }
        }
	    
	    double weight = (maxValue * 100.0 / count);
	    
	    Log.v("SudokuFounder", "% сверху " + weight);
	    
	    if(blackPixels > (allPixels * 0.75))
	    {
	    	Log.v("SudokuFounder", "Много черных сверху");
	    	return null;
	    }
	    
	    if(weight < singleWeightMinLevel)
		{
	    	Log.v("SudokuFounder", "Cверху нет линии " + maxValue);
			return null;
		}
	    
		weightTop = weight;
        p -= maxDimention;
        Point ptTop1, ptTop2;
        if (theta != 0)
        {
            double m = -1 / Math.tan(theta);
            double c = p / Math.sin(theta);

            ptTop1 = new Point(0, (int)c);
            ptTop2 = new Point(dim, (int)(m * dim + c));
        }
        else
        {
            ptTop1 = new Point((int)p, 0);
            ptTop2 = new Point((int)p, dim);
        }
        
        allPixels = 0;
        blackPixels = 0;
        maxValue = 0;
        arr = new int[20][maxDimention * 2];
        for (int i = 1; i < count - 1; i++)
        {
            int x = step * i;
            for (int y = dim - 1; y >= dim - deep; y--, allPixels++)
            {
                // черная точка
                if (matrix.get(x, y))
                {
                    SetPoint(x, y, 80, 100, -80);
                    blackPixels++;
                }
            }
        }
        
        weight = (maxValue * 100.0 / count);
        
        Log.v("SudokuFounder", "% снизу " + weight);
        
        if(blackPixels > (allPixels * 0.75))
	    {
	    	Log.v("SudokuFounder", "Много черных снизу");
	    	return null;
	    }
        
        if(weight < singleWeightMinLevel)
		{
	    	Log.v("SudokuFounder", "Снизу нет линии");
			return null;
		}

		weightBottom = weight;
        Point ptBottom1, ptBottom2;
        p -= maxDimention;
        if (theta != 0)
        {
            double m = -1 / Math.tan(theta);
            double c = p / Math.sin(theta);

            ptBottom1 = new Point(0, (int)c);
            ptBottom2 = new Point(dim, (int)(m * dim + c));
        }
        else
        {
            ptBottom1 = new Point((int)p, 0);
            ptBottom2 = new Point((int)p, dim);
        }
        
        dim = image.getWidth();
	    step = dim / count;
        maxDimention = dim + dim;
        
        allPixels = 0;
        blackPixels = 0;
        maxValue = 0;
        arr = new int[20][maxDimention * 2];
        for (int i = 1; i < count - 1; i++)
        {
            int y = step * i;
            for (int x = dim - 1; x >= dim - deep; x--, allPixels++)
            {
                // черная точка
                if (matrix.get(x, y))
                {
                    SetPoint(x, y, -10, 10, 10);
                    blackPixels++;
                }
            }
        }
        
        weight = (maxValue * 100.0 / count);
        
        Log.v("SudokuFounder", "% справа " + weight);
        
        if(blackPixels > (allPixels * 0.75))
	    {
	    	Log.v("SudokuFounder", "Много черных справа");
	    	return null;
	    }
        
        if(weight < singleWeightMinLevel)
		{
	    	Log.v("SudokuFounder", "Справа нет линии");
			return null;
		}

		weightRight = weight;
        Point ptRight1, ptRight2;
        p -= maxDimention;
        if (theta != 0)
        {
            double m = -1 / Math.tan(theta);
            double c = p / Math.sin(theta);

            ptRight1 = new Point(0, (int)c);
            ptRight2 = new Point(dim, (int)(m * dim + c));
        }
        else
        {
            ptRight1 = new Point((int)p, 0);
            ptRight2 = new Point((int)p, dim);
        }
        
        allPixels = 0;
        blackPixels = 0;
        maxValue = 0;
        arr = new int[20][maxDimention * 2];
        for (int i = 1; i < count - 1; i++)
        {
            int y = step * i;
            for (int x = 0; x < deep; x++, allPixels++)
            {
                // черная точка
                if (matrix.get(x, y))
                {
                    SetPoint(x, y, -10, 10, 10);
                    blackPixels++;
                }
            }
        }
        
        weight = (maxValue * 100.0 / count);
        
        Log.v("SudokuFounder", "% слева " + weight);
        
        if(blackPixels > (allPixels * 0.75))
	    {
	    	Log.v("SudokuFounder", "Много черных слева");
	    	return null;
	    }
        
        if(weight < singleWeightMinLevel)
		{
	    	Log.v("SudokuFounder", "Слева нет линии");
			return null;
		}

		weightLeft = weight;
        Point ptLeft1, ptLeft2;
        p -= maxDimention;

        if (theta != 0)
        {
            double m = -1 / Math.tan(theta);
            double c = p / Math.sin(theta);

            ptLeft1 = new Point(0, (int)c);
            ptLeft2 = new Point(dim, (int)(m * dim + c));
        }
        else
        {
            ptLeft1 = new Point((int)p, 0);
            ptLeft2 = new Point((int)p, dim);
        }
        
        double averageWeight = (weightTop + weightBottom + weightRight + weightLeft) / 4F;
		
        // если есть необходимость в подсказках
        if (hints != null)
        {
            FoundWeightCallback fwcb = (FoundWeightCallback) hints.get(DecodeHintType.NEED_FOUND_WEIGHT_CALLBACK);

            // если надо выводить вес
            if (fwcb != null)
            {
                // отрисовка весов найденных линий
                List<ResultValue> lines = new ArrayList<ResultValue>();
                lines.add(new ResultWeight((int) averageWeight));

                fwcb.foundWeightResultLines(lines);
            }
        }
		
		Log.v("SudokuFounder", String.format("Средний вес %f", + averageWeight));
		if(averageWeight < averageWeightMinLevel || DoubleCheck(matrix) == false)
		{
			return null;
		}
		/*
		if(averageWeight > 30)
		{
			Log.d(Constants.TAG, String.format("Вес верхней %d", + weightTop));
	        Log.d(Constants.TAG, String.format("Вес нижней %d", + weightBottom));
	        Log.d(Constants.TAG, String.format("Вес левой %d", + weightLeft));
	        Log.d(Constants.TAG, String.format("Вес правой %d", + weightRight));
		}
        */
        float leftA = ptLeft2.y - ptLeft1.y;
        float leftB = ptLeft1.x - ptLeft2.x;
        float leftC = leftA * ptLeft1.x + leftB * ptLeft1.y;

        float rightA = ptRight2.y - ptRight1.y;
        float rightB = ptRight1.x - ptRight2.x;
        float rightC = rightA * ptRight1.x + rightB * ptRight1.y;

        float topA = ptTop2.y - ptTop1.y;
        float topB = ptTop1.x - ptTop2.x;
        float topC = topA * ptTop1.x + topB * ptTop1.y;

        float bottomA = ptBottom2.y - ptBottom1.y;
        float bottomB = ptBottom1.x - ptBottom2.x;
        float bottomC = bottomA * ptBottom1.x + bottomB * ptBottom1.y;

        // Intersection of left and top
        float detTopLeft = leftA * topB - leftB * topA;
        Point ptTopLeft = new Point((int)((topB * leftC - leftB * topC) / detTopLeft), (int)((leftA * topC - topA * leftC) / detTopLeft));

        // Intersection of top and right
        float detTopRight = rightA * topB - rightB * topA;
        Point ptTopRight = new Point((int)((topB * rightC - rightB * topC) / detTopRight), (int)((rightA * topC - topA * rightC) / detTopRight));

        // Intersection of right and bottom
        float detBottomRight = rightA * bottomB - rightB * bottomA;
        Point ptBottomRight = new Point((int)((bottomB * rightC - rightB * bottomC) / detBottomRight), (int)((rightA * bottomC - bottomA * rightC) / detBottomRight));

        // Intersection of bottom and left
        float detBottomLeft = leftA * bottomB - leftB * bottomA;
        Point ptBottomLeft = new Point((int)((bottomB * leftC - leftB * bottomC) / detBottomLeft), (int)((leftA * bottomC - bottomA * leftC) / detBottomLeft));
	    
        // если есть необходимость в подсказках
        if (hints != null)
        {
            ResultLinesCallback rpcb = (ResultLinesCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);

            // если надо вывести рамку судоку
            if (rpcb != null)
            {
                List<ResultValue> lines = new ArrayList<ResultValue>();
                lines.add(new ResultLine(ptTopLeft, ptTopRight));
                lines.add(new ResultLine(ptBottomLeft, ptBottomRight));
                lines.add(new ResultLine(ptTopLeft, ptBottomLeft));
                lines.add(new ResultLine(ptTopRight, ptBottomRight));

                rpcb.foundPossibleResultLines(lines);
            }
        }
		
	    //return null;
		return new Result(ptTopLeft, ptTopRight, ptBottomLeft, ptBottomRight, (int)averageWeight);
	}
	
	private boolean DoubleCheck(BitMatrix matrix)
	{
		float averageWeight = 0;
		int blackPixels, allPixels;
		
		int dim = matrix.getHeight();
	    step = dim / count;
        maxDimention = dim + dim;
        deep = dim / 20;
        
		int offset1 = dim / 3;
		int offset2 = offset1 * 2;
		
		allPixels = 0;
        blackPixels = 0;
        maxValue = 0;
	    arr = new int[20][maxDimention * 2];
	    for (int i = 1; i < count - 1; i++)
        {
            int x = step*i;
            for (int y = offset1 - deep / 2; y < offset1 + deep / 2; y++, allPixels++)
            {
                // черная точка
                if (matrix.get(x, y))
                {
                    SetPoint(x, y, 80, 100, -80);
                    blackPixels++;
                }
            }
        }
	    
	    double weight = (maxValue * 100.0 / count);
	    
	    Log.v("SudokuFounder", "% по горизонтали offset1 " + weight);
	    
	    if(blackPixels > (allPixels * 0.75))
	    {
	    	Log.v("SudokuFounder", "Много черных по горизонтали offset1");
	    	return false;
	    }
	    
	    if(weight < singleWeightMinLevel)
		{
	    	Log.v("SudokuFounder", "По горизонтали offset1 нет линии");
			return false;
		}
	    
	    averageWeight += weight;
	    
	    allPixels = 0;
        blackPixels = 0;
        maxValue = 0;
	    arr = new int[20][maxDimention * 2];
	    for (int i = 1; i < count - 1; i++)
        {
            int x = step*i;
            for (int y = offset2 - deep / 2; y < offset2 + deep / 2; y++, allPixels++)
            {
                // черная точка
                if (matrix.get(x, y))
                {
                    SetPoint(x, y, 80, 100, -80);
                    blackPixels++;
                }
            }
        }
	    
	    weight = (maxValue * 100.0 / count);
	    
	    Log.v("SudokuFounder", "% по горизонтали offset2 " + weight);
	    
	    if(blackPixels > (allPixels * 0.75))
	    {
	    	Log.v("SudokuFounder", "Много черных по горизонтали offset2");
	    	return false;
	    }
	    
	    if(weight < singleWeightMinLevel)
		{
	    	Log.v("SudokuFounder", "По горизонтали offset2 нет линии");
			return false;
		}
	    
	    averageWeight += weight;
	    
	    dim = matrix.getWidth();
	    step = dim / count;
        maxDimention = dim + dim;
        deep = dim / 20;
        
		offset1 = dim / 3;
		offset2 = offset1 * 2;
        
        allPixels = 0;
        blackPixels = 0;
        maxValue = 0;
        arr = new int[20][maxDimention * 2];
        for (int i = 1; i < count - 1; i++)
        {
            int y = step * i;
            for (int x = offset1 - deep / 2; x < offset1 + deep / 2; x++, allPixels++)
            {
                // черная точка
                if (matrix.get(x, y))
                {
                    SetPoint(x, y, -10, 10, 10);
                    blackPixels++;
                }
            }
        }
        
        weight = (maxValue * 100.0 / count);
        
        Log.v("SudokuFounder", "% по вертикали offset1 " + weight);
        
        if(blackPixels > (allPixels * 0.75))
	    {
	    	Log.v("SudokuFounder", "Много черных по вертикали offset1");
	    	return false;
	    }
        
        if(weight < singleWeightMinLevel)
		{
	    	Log.v("SudokuFounder", "По вертикали offset1 нет линии");
			return false;
		}
        
        averageWeight += weight;
        
        allPixels = 0;
        blackPixels = 0;
        maxValue = 0;
        arr = new int[20][maxDimention * 2];
        for (int i = 1; i < count - 1; i++)
        {
            int y = step * i;
            for (int x = offset2 - deep / 2; x < offset2 + deep / 2; x++, allPixels++)
            {
                // черная точка
                if (matrix.get(x, y))
                {
                    SetPoint(x, y, -10, 10, 10);
                    blackPixels++;
                }
            }
        }
        
        weight = (maxValue * 100.0 / count);
        
        Log.v("SudokuFounder", "% по вертикали offset1 " + weight);
        
        if(blackPixels > (allPixels * 0.75))
	    {
	    	Log.v("SudokuFounder", "Много черных по вертикали offset2");
	    	return false;
	    }
        
        if(weight < singleWeightMinLevel)
		{
	    	Log.v("SudokuFounder", "По вертикали offset2 нет линии");
			return false;
		}
        
        averageWeight += weight;
	    
        averageWeight = averageWeight / 4.0F;
        Log.v("SudokuFounder", String.format("Дополни вес %f", + averageWeight));
	    return averageWeight > averageWeightMinLevel;
	}

	@Override
	public void reset() 
	{
		
	}
	
	private void SetPoint(int x, int y, int minTheta, int maxTheta, int delta)
    {
        for (int theta = minTheta; theta < maxTheta; theta += 1)
        {
            double p = x * Math.cos(theta * oneGradus) + y * Math.sin(theta * oneGradus) + maxDimention;

            int res = arr[theta + delta][(int)p] + 1;
            arr[theta + delta][(int)p] = res;

            if (res > maxValue)
            {
                maxValue = res;
                this.p = p;
                this.theta = theta * oneGradus;
            }
        }   
    }

	@Override
	public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
		return decode(image, null);
	}

}
