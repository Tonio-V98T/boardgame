package it.unibz.inf.pp.clash.model.snapshot;

import it.unibz.inf.pp.clash.model.exceptions.CoordinatesOutOfBoardException;
import it.unibz.inf.pp.clash.model.exceptions.OccupiedTileException;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Wolfie;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Board for an ongoing game.
 * This is a two-dimensional grid.
 * <p>
 * Tile coordinates are natural numbers and start at 0.
 * The top left tile has coordinates (0,0).
 * <p>
 * So for instance, the tile with row index 1 and column index 3 is on the second row (from top to bottom) and
 * fourth column (from left to right).
 * <p>
 * A tile may have a unit standing on it.
 * A same unit may stand on multiple (adjacent) tiles.
 */
public interface Board {

    void addUnitForPlayer(int rowIndex, int columnIndex, Unit unit, Snapshot.Player player);

    void removeUnitForPlayer(int rowIndex, int columnIndex);

    public ArrayList<MobileUnit> getArmyP1();
    public ArrayList<MobileUnit> getArmyP2();

    /**
     * A pair of coordinates for a tile.
     */
    record TileCoordinates(int rowIndex, int columnIndex){}

    /**
     * @return the maximum possible index for a column (a.k.a. size of a row -1)
     */
    int getMaxColumnIndex();

    /**
     * @return the maximum possible index for a row (a.k.a. size of a column -1)
     */
    int getMaxRowIndex();

    /**
     * @return true if the input coordinates are within the board's boundaries.
     */
    boolean areValidCoordinates(int rowIndex, int columnIndex);

    /**
     * @return the unit standing on the tile at the input coordinates, if any.
     * @throws CoordinatesOutOfBoardException if the coordinates are out of this board's boundaries.
     */
    Optional<Unit> getUnit(int rowIndex, int columnIndex);

    /**
     * Adds the input unit to the tile at the input coordinates.
     *
     * @throws OccupiedTileException if there is already a unit on this tile.
     * @throws CoordinatesOutOfBoardException if the coordinates are out of this board's boundaries.
     */
    void addUnit(int rowIndex, int columnIndex, Unit unit);

    /**
     * Removes the unit currently standing on the tile at the input coordinates.
     * If there was no unit, then this method has no effect.
     *
     * @throws CoordinatesOutOfBoardException if the coordinates are out of this board's boundaries.
     */
    void removeUnit(int rowIndex, int columnIndex);

}
