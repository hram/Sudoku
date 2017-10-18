package hram.sudoku.solvers;

import java.util.ArrayList;
import java.util.Random;

public class SudSolver {
    // order for brute force traverse
    private class CellOrder {
        int x;                        // x coordinate of cell
        int y;                        // y coordinate of cell
    }

    static CellOrder[] m_cellOrder = new CellOrder[81];
    public SudSolverCell[][] cell = new SudSolverCell[9][9];            // sudoku grid values. If the cell is initally empty the value is 0.
    private int m_BruteForceStep;
    static int[] down = {0, 0, 0, 3, 3, 3, 6, 6, 6};
    static int[] up = {2, 2, 2, 5, 5, 5, 8, 8, 8};
    static int[] xx = {0, 1, 2, 0, 1, 2, 0, 1, 2, 3, 4, 5, 3, 4, 5, 3, 4, 5, 6, 7, 8, 6, 7, 8, 6, 7, 8, 0, 1, 2, 0, 1, 2, 0, 1, 2, 3, 4, 5, 3, 4, 5, 3, 4, 5, 6, 7, 8, 6, 7, 8, 6, 7, 8, 0, 1, 2, 0, 1, 2, 0, 1, 2, 3, 4, 5, 3, 4, 5, 3, 4, 5, 6, 7, 8, 6, 7, 8, 6, 7, 8};
    static int[] yy = {0, 0, 0, 1, 1, 1, 2, 2, 2, 0, 0, 0, 1, 1, 1, 2, 2, 2, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 3, 3, 3, 4, 4, 4, 5, 5, 5, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 6, 6, 6, 7, 7, 7, 8, 8, 8, 6, 6, 6, 7, 7, 7, 8, 8, 8};
    static int MAXBRUTEFORCESTEP = 600000;

    public SudSolver() {
        Reset();
    }

    public SudSolver(String puzzle) throws Exception {
        if (puzzle.length() != 9 * 9) {
            throw new Exception();
        }

        Reset();

        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                int index = y * 9 + x;
                int value = Integer.parseInt("" + puzzle.charAt(index));
                cell[y][x].value = value;// substring(index, index + 1));
            }
        }
    }

    private SudSolver(SudSolverCell[][] initCells) {
        Reset();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                cell[y][x].value = initCells[y][x].value;
            }
        }
    }

    public void Reset() {
        m_BruteForceStep = 0;

        // set all candidates to TRUE initially
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                cell[y][x] = new SudSolverCell();
                for (int candidate = 1; candidate < 10; candidate++) {
                    cell[y][x].isCandidate[candidate] = true;
                }
            }
        }

        for (int i = 0; i < m_cellOrder.length; i++) {
            m_cellOrder[i] = new CellOrder();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //Purpose:		Solves the sudoku puzzle.
    //Parameters:
    //Returns:		TRUE if puzzle solved, otherwise FALSE.
    /////////////////////////////////////////////////////////////////////////
    public boolean SolveMe() {
        do {
            if (Step1() == true) {
                //TRACE("\nsimple");
                return true;            // sudoku solved
            }
        } while (Step2() == true);


        // do not proceed if the cell values are invalid
        if (!IsSolvable())
            return false;

        // if Step1 and Step2 did not find a final solution then fall back to
        // brute-force method, which is slow but will find a solution no matter how difficult it is.

        boolean ret = false;
        PrepareRecursionOrder();
        for (int i = 0; i < 3; i++)        // 3 chances (retries) for brute-force method
        {
            m_BruteForceStep = 0;
            ret = BruteForce(-1);
            //TRACE("\nBruteForceStep:%d", m_BruteForceStep);

            if (m_BruteForceStep < MAXBRUTEFORCESTEP)
                break;
            else
                MixRecursionOrder();
        }
        return ret;
    }

    /**
     * Function:	Step1()
     * Purpose:		Tries to solve the sudoku puzzle with "naked single candidate" method.
     * It checks to see if any of the cells have ONLY ONE possible value - only one possible candidate.
     * Step 1. Updates cell[y][x].isCandidate array with all the possible values.
     * Step 2. Searches for single candidates.
     * If any are found the value is entered for this cell and Step 1 is repeated until all remaining unsolved cells contain multiple candidates
     * Parameters:
     * Returns:		TRUE if entire puzzle solved, otherwise FALSE.
     */
    boolean Step1() {
        int x, y, candidate, value = 0, count;
        boolean bFoundAtLeastOne;        // TRUE if found at least one single possible solution
        boolean bSolved;                // TRUE if the entire sudoku is solved

        do {
            bFoundAtLeastOne = false;
            bSolved = true;
            // for each cell update the candidates
            for (y = 0; y < 9; y++) {
                for (x = 0; x < 9; x++) {
                    if (cell[y][x].value == 0)                                // only empty cells, please
                    {
                        count = 0;
                        for (candidate = 1; candidate < 10; candidate++)        // for each cell try with all possible candidates
                        {
                            if (cell[y][x].isCandidate[candidate] == true)    // only candidates, please
                            {
                                cell[y][x].value = candidate;
                                if (IsValidCell(y, x) == true) {
                                    count++;
                                    value = candidate;
                                } else
                                    cell[y][x].isCandidate[candidate] = false;
                            }
                        }
                        if (count == 1) {
                            cell[y][x].value = value;    // set the only possible cell value
                            bFoundAtLeastOne = true;    // and signal the do-while loop to go deeper with the next loop
                        } else {
                            cell[y][x].value = 0;        // reset the cell
                            bSolved = false;            // sudoku is not solved
                        }
                    }
                }
            }
        } while (bFoundAtLeastOne == true);

        return bSolved;
    }

    //////////////////////////////////////////////////////////////////////////
    //Function:	Step2()
    //Purpose:		Tries to solve the sudoku puzzle with "hidden single candidate" method.
    //Searches for only one candidate value in a 3x3block or row or column.
    //This single candidate is a valid value.
    //The array cell[y][x].isCandidate[] must be filled with valid candidates before calling this function.
    //Parameters:
    //Returns:		TRUE if at least one value found, otherwise FALSE.
    /////////////////////////////////////////////////////////////////////////
    boolean Step2() {
        int x, y, candidate, index = 0, count;

        // for each value candidate
        for (candidate = 1; candidate < 10; candidate++) {    // for each row
            for (y = 0; y < 9; y++) {
                count = 0;
                for (x = 0; x < 9; x++) {
                    if (cell[y][x].value == 0)                // only unsolved cells, please
                    {
                        if (cell[y][x].isCandidate[candidate] == true) {
                            count++;
                            index = x;
                        }
                    }
                }
                if (count == 1) {
                    cell[y][index].value = candidate;        // bingo
                    return true;
                }
            }
            // for each column
            for (x = 0; x < 9; x++) {
                count = 0;
                for (y = 0; y < 9; y++) {
                    if (cell[y][x].value == 0)                // only unsolved cells, please
                    {
                        if (cell[y][x].isCandidate[candidate] == true) {
                            count++;
                            index = y;
                        }
                    }
                }
                if (count == 1) {
                    cell[index][x].value = candidate;        // bingo
                    return true;
                }
            }
            // for each 3x3 block
            int i = 0;
            for (y = 0; y < 9; y++) {
                count = 0;
                for (x = 0; x < 9; x++) {
                    if (cell[yy[i]][xx[i]].value == 0)            // only unsolved cells, please
                    {
                        if (cell[yy[i]][xx[i]].isCandidate[candidate] == true) {
                            count++;
                            index = i;
                        }
                    }
                    i++;
                }
                if (count == 1) {
                    cell[yy[index]][xx[index]].value = candidate;    // bingo
                    return true;
                }
            }
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////
    //Function:	IsSolvable()
    //Purpose:		Detects if a given puzzle is solvable.
    //How: Every row, column and 3x3block must have all numbers 1-9 alredy defined as value OR as a candidate.
    //Parameters:
    //Returns:		TRUE if the puzzle is solvable, otherwise FALSE.
    /////////////////////////////////////////////////////////////////////////
    boolean IsSolvable() {
        int x, y, value;
        boolean bFound;
        //static const int xx[81] = {0,1,2,0,1,2,0,1,2, 3,4,5,3,4,5,3,4,5, 6,7,8,6,7,8,6,7,8,   0,1,2,0,1,2,0,1,2, 3,4,5,3,4,5,3,4,5, 6,7,8,6,7,8,6,7,8,   0,1,2,0,1,2,0,1,2, 3,4,5,3,4,5,3,4,5, 6,7,8,6,7,8,6,7,8};
        //static const int yy[81] = {0,0,0,1,1,1,2,2,2, 0,0,0,1,1,1,2,2,2, 0,0,0,1,1,1,2,2,2,   3,3,3,4,4,4,5,5,5, 3,3,3,4,4,4,5,5,5, 3,3,3,4,4,4,5,5,5,   6,6,6,7,7,7,8,8,8, 6,6,6,7,7,7,8,8,8, 6,6,6,7,7,7,8,8,8};

        // for each value or candidate
        for (value = 1; value < 10; value++) {    // for each 3x3 block
            for (y = 0; y < 9; y++) {
                bFound = false;
                int i = y * 9;
                for (x = 0; x < 9; x++) {
                    if ((cell[yy[i]][xx[i]].value == value) ||
                            (cell[yy[i]][xx[i]].value == 0 && cell[yy[i]][xx[i]].isCandidate[value] == true)) {
                        bFound = true;
                        break;
                    }
                    i++;
                }
                if (bFound == false)
                    return false;
            }
            // for each row
            for (y = 0; y < 9; y++) {
                bFound = false;
                for (x = 0; x < 9; x++) {
                    if ((cell[y][x].value == value) ||
                            (cell[y][x].value == 0 && cell[y][x].isCandidate[value] == true)) {
                        bFound = true;
                        break;
                    }
                }
                if (bFound == false)
                    return false;
            }
            // for each column
            for (x = 0; x < 9; x++) {
                bFound = false;
                for (y = 0; y < 9; y++) {
                    if ((cell[y][x].value == value) ||
                            (cell[y][x].value == 0 && cell[y][x].isCandidate[value] == true)) {
                        bFound = true;
                        break;
                    }
                }
                if (bFound == false)
                    return false;
            }
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////////
    //Function:	PrepareRecursionOrder()
    //Purpose:		Initializes the m_cellOrder array with the coordinates.
    //Parameters:
    //Returns:
    /////////////////////////////////////////////////////////////////////////
    void PrepareRecursionOrder() {
        // return if m_cellOrder is already initialized
        if (m_cellOrder[0].x < m_cellOrder[1].x)
            return;

        // populate m_cellOrder array
        int i = 0;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                m_cellOrder[i].x = x;
                m_cellOrder[i].y = y;
                i++;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //Function:	BruteForce()
    //Purpose:		Implementation of 'Brute force' aka 'trial and error' method.
    //Recurses thru all the cells with all the possible values (candidates).
    //Parameters:	step - resursion step; One step = one cell.
    //Returns:		TRUE if puzzle solved, otherwise FALSE.
    /////////////////////////////////////////////////////////////////////////
    boolean BruteForce(int step) {
        step++;
        if (step == 81)
            return true;  // Recursion finished

        m_BruteForceStep++;
        if (m_BruteForceStep > MAXBRUTEFORCESTEP)    // Recursion takes too long
            return false;

        // one step = one cell
        int x = m_cellOrder[step].x;
        int y = m_cellOrder[step].y;

        if (cell[y][x].value > 0)        // skip init values
        {
            if (BruteForce(step))    // go next cell
                return true;
        } else {
            for (int value = 1; value < 10; value++)                // try with all the values for the current cell
            {
                if (cell[y][x].isCandidate[value] == false)        // only candidates, please
                    continue;
                cell[y][x].value = value;    // set the candidate value in the current cell
                if (IsValidCell(y, x))        // if this is a serious candidate
                    if (BruteForce(step))    // go next cell
                        return true;
            }
            // if the possible value is NOT found
            cell[y][x].value = 0;            // reset the cell value
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////
    //Function:	MixRecursionOrder()
    //Purpose:		Randomly scrambles the m_cellOrder array with
    //the hope that scrambled order will yield the brute-force solution faster.
    //Parameters:
    //Returns:
    /////////////////////////////////////////////////////////////////////////
    void MixRecursionOrder() {
        int i, index1 = 0, index2;

        Random rnd = new Random(System.currentTimeMillis());

        // find first empty cell in sequence order
        for (i = 0; i < 81; i++) {
            if (cell[m_cellOrder[i].y][m_cellOrder[i].x].value == 0) {
                index1 = i;
                break;
            }
        }

        // swap first cell in m_cellOrder with a random one.
        // then swap 5 more random cells.
        for (i = 0; i < 6; i++) {
            index2 = rnd.nextInt(81);
            int x = m_cellOrder[index1].x;
            int y = m_cellOrder[index1].y;
            m_cellOrder[index1].x = m_cellOrder[index2].x;
            m_cellOrder[index1].y = m_cellOrder[index2].y;
            m_cellOrder[index2].x = x;
            m_cellOrder[index2].y = y;
            index1 = rnd.nextInt(81);
        }
    }

    /**
     * Purpose:		Calculates whether the sudoku values are valid. This means that every number must be unique for row, column and 3x3 block.
     * Parameters:
     * Returns:		TRUE if values are valid, otherwise FALSE.
     */
    public boolean AreAllCellsValid() {
        int cellCount = 0;
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                if (cell[y][x].value != 0)
                    if (!IsValidCell(y, x))
                        return false;
                    else
                        cellCount++;

        if (cellCount > 11)    // skip incomplete sudoku grid. There must be at least 12 digits in it to be a valid grid.
            return true;
        return false;
    }

    //////////////////////////////////////////////////////////////////////////
    //Function:	IsValidCell()
    //Purpose:		Checks if the celly,cellx sudoku position has a valid value.
    //Parameters:	celly, celly - input cell coordinate
    //Returns:		TRUE if there is no duplicates for input cell, otherwise FALSE.
    /////////////////////////////////////////////////////////////////////////
    boolean IsValidCell(int celly, int cellx) {
        return (GetWrongCell(celly, cellx) == null);
    }

    //////////////////////////////////////////////////////////////////////////
    //Function:	GetWrongCell()
    //Purpose:		Searches for a duplicate value in the same row, column and 3x3 block.
    //Parameters:	celly, celly - input cell coordinate
    //Returns:		pointer to conflicting cell of the input cell. NULL if there is no conflicting cell.
    /////////////////////////////////////////////////////////////////////////
    WrongCell GetWrongCell(int celly, int cellx) {
        int x, y;

        int cellvalue = cell[celly][cellx].value;

        for (x = 0; x < 9; x++)                            //  is duplicate in same row
            if (cell[celly][x].value == cellvalue)
                if (x != cellx)
                    return new WrongCell(celly, x, cell[celly][x].value);

        for (y = 0; y < 9; y++)                            // is duplicate in same column
            if (cell[y][cellx].value == cellvalue)
                if (y != celly)
                    return new WrongCell(y, cellx, cell[y][cellx].value);

        for (y = down[celly]; y <= up[celly]; y++)        // is duplicate in the same rect 3x3
            for (x = down[cellx]; x <= up[cellx]; x++)
                if (cell[y][x].value == cellvalue)
                    if (x != cellx && y != celly)
                        return new WrongCell(y, x, cell[y][x].value);
        return null;
    }

    //////////////////////////////////////////////////////////////////////////
    //Function:	Fixit()
    //Purpose:		Corrects the result of OCR.
    //OCR must conform the sudoku valid values. If it doesn't, take the next best guess based on cell->OCR[] value.
    //FYI: This method is not reliable. You can comment it. Statistically, the program runs almost the same with or without it. This is because sometimes this method adds additional errors in the results, so the benefit is almost annulled with the introduced errors. The real benefit would be to raise the quality of OCR.
    //Parameters:
    //Returns:
    /////////////////////////////////////////////////////////////////////////
    void Fixit() {

        if (AreAllCellsValid()) {
            return;
        }

        ArrayList<WrongCell> vrongs = new ArrayList<WrongCell>();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (cell[y][x].value != 0) {
                    WrongCell wrong = GetWrongCell(y, x);
                    if (wrong == null) {
                        continue;
                    }

                    vrongs.add(new WrongCell(y, x, cell[y][x].value));
                    vrongs.add(wrong);

                    cell[y][x].value = 0;
                    cell[wrong.y][wrong.x].value = 0;
                }
            }
        }

        if (AreAllCellsValid()) {
            for (WrongCell wrong : vrongs) {
                SudSolver sud = new SudSolver(cell);
                sud.cell[wrong.y][wrong.x].value = wrong.value;
                if (sud.SolveMe()) {
                    for (WrongCell wrong2 : vrongs) {
                        if (wrong.x == wrong2.x && wrong.y == wrong2.y) {
                            continue;
                        }
                        int value = sud.cell[wrong2.y][wrong2.x].value;
                        wrong2.OCR[value]++;
                    }
                }
            }

            for (WrongCell wrong : vrongs) {
                int max = 0;
                int value = 0;
                for (int i = 0; i < wrong.OCR.length; i++) {
                    if (wrong.OCR[i] > max) {
                        max = wrong.OCR[i];
                        value = i;
                    }
                }

                cell[wrong.y][wrong.x].value = value;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //Function:	GetSolution()
    //Purpose:		Fills the result[] array.
    //Parameters:	result pointer to array to fill.
    //Returns:
    /////////////////////////////////////////////////////////////////////////
    int[][] GetSolution() {
        int[][] solution = new int[9][9];
        for (int x = 0; x < 9; x++)
            for (int y = 0; y < 9; y++)
                solution[y][x] = cell[y][x].value;
        return solution;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                sb.append(cell[y][x].value);
            }
        }
        return sb.toString();
    }
}
