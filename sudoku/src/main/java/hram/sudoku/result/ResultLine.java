package hram.sudoku.result;

import android.graphics.Point;

/**
 * <p>Encapsulates a point of interest in an image containing a barcode. Typically, this
 * would be the location of a finder pattern or the corner of the barcode, for example.</p>
 *
 * @author Sean Owen
 */
public class ResultLine extends ResultValue {

    private final Point point1;
    private final Point point2;
    private final int weight;

    public ResultLine(Point point1, Point point2) {
        this.point1 = point1;
        this.point2 = point2;
        this.weight = -1;
    }

    public ResultLine(Point point1, Point point2, int weight) {
        this.point1 = point1;
        this.point2 = point2;
        this.weight = weight;
    }

    public final Point getPoint1() {
        return point1;
    }

    public final Point getPoint2() {
        return point2;
    }

    public final int getWeigth() {
        return weight;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(50);
        result.append('(');
        result.append(point1.x);
        result.append(':');
        result.append(point1.y);
        result.append(',');
        result.append(point2.x);
        result.append(':');
        result.append(point2.y);
        result.append(')');
        return result.toString();
    }

    @Override
    public ResulType GetType() {
        return ResulType.LINE;
    }
}
