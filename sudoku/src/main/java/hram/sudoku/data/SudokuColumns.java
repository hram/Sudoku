/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package hram.sudoku.data;

import android.provider.BaseColumns;

public abstract class SudokuColumns implements BaseColumns {
    public static final String FOLDER_ID = "folder_id";
    public static final String CREATED = "created";
    public static final String STATE = "state";
    public static final String TIME = "time";
    public static final String LAST_PLAYED = "last_played";
    public static final String DATA = "data";
    public static final String PUZZLE_NOTE = "puzzle_note";
    public static final String BITMAP = "bitmap";
    public static final String BM_WIDTH = "bitmapWidth";
    public static final String BM_HEIGHT = "bitmapHeight";
    public static final String FR_LEFT = "framingRectLeft";
    public static final String FR_TOP = "framingRectTop";
    public static final String FR_RIGHT = "framingRectRight";
    public static final String FR_BOTTOM = "framingRectBottom";
    public static final String RES_TL_X = "resTL_X";
    public static final String RES_TL_Y = "resTL_Y";
    public static final String RES_TR_X = "resTR_X";
    public static final String RES_TR_Y = "resTR_Y";
    public static final String RES_BL_X = "resBL_X";
    public static final String RES_BL_Y = "resBL_Y";
    public static final String RES_BR_X = "resBR_X";
    public static final String RES_BR_Y = "resBR_Y";
    public static final String ORIENTATION = "orientation";
}
