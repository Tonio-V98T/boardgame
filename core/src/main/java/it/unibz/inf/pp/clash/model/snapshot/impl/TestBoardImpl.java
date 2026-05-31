package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.exceptions.OccupiedTileException;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Currently main Board class
 */
public class TestBoardImpl implements Board {

    final Unit[][] grid;

    // armies for player 1 and 2
    private ArrayList<MobileUnit> armyP1 = new ArrayList<>();
    private ArrayList<MobileUnit> armyP2 = new ArrayList<>();

    /**
     * Constructor
     * @param grid array of arrays representing the board tiles
     */
    public TestBoardImpl(Unit[][] grid) {
        this.grid = grid;
    }

    /**
     * Static method for creating a board
     * @param maxRowIndex highest row-wise index
     * @param maxColumnIndex highest column-wise index
     * @return a new Board
     */
    public static Board createEmptyBoard(int maxRowIndex, int maxColumnIndex){
        // create board grid
        Unit[][] grid = new Unit[maxRowIndex + 1][maxColumnIndex + 1];

        // create new board through the default constructor
        return new TestBoardImpl(grid);

    }

    /**
     * Alternative implementation of the constructor
     * might be reused in the future, instead of the default one
     * @param maxRowIndex highest row-wise index
     * @param maxColumnIndex highest column-wise index
     */
    public TestBoardImpl(int maxRowIndex, int maxColumnIndex){
        Unit[][] tmpGrid = new Unit[maxRowIndex + 1][maxColumnIndex + 1];
        this.grid = tmpGrid;
    }


    @Override
    public int getMaxRowIndex() {
        // in a 2D array of arrays, the first dim is rows ->
        // the external shell is an array containing rows ->
        // its length is the number of rows
        return this.grid.length -1;
    }

    @Override
    public int getMaxColumnIndex() {
        // while the internal shell is a row containing columns
        // if all columns have same length,
        // then num of columns is the length of the row array
        return this.grid[0].length -1; // could be [0], [1], ...
    }

    @Override
    public boolean areValidCoordinates(int rowIndex, int columnIndex) {

        boolean rowValidity = rowIndex >= 0 && rowIndex <= getMaxRowIndex();
        boolean colValidity = columnIndex >= 0 && columnIndex <= getMaxColumnIndex();

        return rowValidity && colValidity;
    }

    /**
        Helper method: avoids repeating code in the following methods
     */
    private void checkBoundaries    (int rowIndex, int columnIndex)
                                    throws CoordinatesOutOfBoardException {

        if (!areValidCoordinates(rowIndex, columnIndex)) {
            throw new CoordinatesOutOfBoardException(   rowIndex, columnIndex,
                                                        getMaxRowIndex(), getMaxColumnIndex());
        }
    }

    /**
     * Retrieves current unit (if any) at selected tile
     * @param rowIndex row index of tile
     * @param columnIndex col index of tile
     * @return current unit standing on tile (if any)
     */
    @Override
    public Optional<Unit> getUnit   (int rowIndex, int columnIndex)
                                    throws CoordinatesOutOfBoardException {
        // check if tile selected is within board dimensions
        this.checkBoundaries(rowIndex, columnIndex);

        // ofNullable() is used to wrap an existing variable as optional
        return Optional.ofNullable(grid[rowIndex][columnIndex]);
    }


    /**
     * Used to retrieve the active player's army
     * @param isP1 whether active player is P1
     * @return active player's army
     */
    public ArrayList<MobileUnit> getActivePlayerArmy(boolean isP1){
        if (isP1){
            return this.armyP1;
        }
        return this.armyP2;
    }


    @Override
    public void addUnit(int rowIndex, int columnIndex, Unit unit)
                        throws OccupiedTileException, CoordinatesOutOfBoardException {
        // check if tile selected is within board dimensions
        this.checkBoundaries(rowIndex, columnIndex);

        // check if there is already a unit on the tile
        if (this.grid[rowIndex][columnIndex] != null) {
            throw new OccupiedTileException(grid[rowIndex][columnIndex]);
        }

        // else, assign chosen unit to tile
        this.grid[rowIndex][columnIndex] = unit;
    }

    /**
     * Adds a unit to the board, and to current player's army
     * @param rowIndex y-axis of tile where the unit will be placed
     * @param columnIndex x-axis of tile where the unit will be placed
     * @param unit unit to be added
     * @param player active player
     * @throws OccupiedTileException
     * @throws CoordinatesOutOfBoardException
     */
    @Override
    public void addUnitForPlayer(int rowIndex, int columnIndex, Unit unit, Snapshot.Player player)
            throws OccupiedTileException, CoordinatesOutOfBoardException {
        checkBoundaries(rowIndex, columnIndex);
        // update board
        if (grid[rowIndex][columnIndex] != null) {
            throw new OccupiedTileException(grid[rowIndex][columnIndex]);
        }
        grid[rowIndex][columnIndex] = unit;

        if (unit instanceof MobileUnit mobile) {
            // update player army
            switch (player) {
                case FIRST -> armyP1.add(mobile);
                case SECOND -> armyP2.add(mobile);
            }
        }
    }

    @Override
    public void removeUnit  (int rowIndex, int columnIndex)
                            throws CoordinatesOutOfBoardException {
        // check if tile selected is within board dimensions
        this.checkBoundaries(rowIndex, columnIndex);

        // remove unit
        this.grid[rowIndex][columnIndex] = null;

    }

    /**
     * Removes a unit from its owner's army, and from the board
     * @param rowIndex
     * @param columnIndex
     * @throws CoordinatesOutOfBoardException
     */
    public void removeUnitForPlayer(int rowIndex, int columnIndex)
            throws CoordinatesOutOfBoardException {
        // check boundaries
        checkBoundaries(rowIndex, columnIndex);

        // update player army
        if (armyP1.contains(getUnit(rowIndex, columnIndex).get())){
            armyP1.remove(getUnit(rowIndex, columnIndex).get());
        } else {armyP2.remove(getUnit(rowIndex, columnIndex).get());}

        // update board
        grid[rowIndex][columnIndex] = null;
    }
}
