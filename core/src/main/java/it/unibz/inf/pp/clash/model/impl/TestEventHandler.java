package it.unibz.inf.pp.clash.model.impl;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.impl.dummy.DummySnapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Fairy;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.ZeroVoid;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;

import java.util.Optional;

public class TestEventHandler implements EventHandler {

        private final DisplayManager DM;
        private DummySnapshot currentSnap;

        private boolean isP1 = true;            // current player

        private Optional<Unit> selectedUnit = Optional.of(new ZeroVoid());    // current unit
        private int selectedRowIndex;
        private int selectedColumnIndex;

        private boolean isMoveSelected = false; // if move button was clicked
        private Optional<Unit> movingUnit = Optional.of(new ZeroVoid());
        private int movingRowIndex;
        private int movingColumnIndex;

    public TestEventHandler(DisplayManager displayManager) {
        this.DM = displayManager;
    }

    @Override
    public void newGame(String firstHero, String secondHero) {

        // TODO: change snapshot depending on difficulty (or level)
        this.currentSnap = new DummySnapshot(firstHero, secondHero);    // create snapshot
        this.updateGameUI("Welcome to the game!");                                         // update UI
    }

    @Override
    public void exitGame() {
        // ??? implementing goodbye msg?
        this.DM.drawHomeScreen();
    }

    /*
        In-game mechanics
     */

    @Override
    public void skipTurn() {
        // invert turns
        this.invertTurns();

        // messages
        String player = "";
        if (this.isP1){player = "P1";}
        else {player = "P2";}
        String msg = "Turn skipped;\n" + player + " now playing";

        this.updateGameMsg(msg);            // UI
    }


    @Override
    public void requestInformation(int rowIndex, int columnIndex) {
        // retrieve unit and its info (by hovering)
        Optional<Unit> chosenUnit = this.getUnitAsOptional(rowIndex, columnIndex);
        String msg = this.unitInfo(chosenUnit);

        this.updateGameMsg(msg);            // UI (hover)
    }

    @Override
    public void selectTile(int rowIndex, int columnIndex) {
        // retrieve tile(unit) and its info
        Optional<Unit> chosenUnit = this.getUnitAsOptional(rowIndex, columnIndex);

        // check if selection (LX click) is the destination of a unit movement
        if (this.isMoveSelected &&
                this.movingUnit.isPresent() &&
                chosenUnit.isEmpty()){
            this.moveUnit(rowIndex, columnIndex);
            return;
        }
        
        // store unit and its coordinates (for reinforcements and movement)
        this.setSelectedUnitInfo(rowIndex, columnIndex, chosenUnit);

        // UI (LX click, no movement)
        String msg = "";
        if (this.isMoveSelected && this.movingUnit.isPresent()) {
            msg = "Cannot move: " + this.unitInfo(chosenUnit) + "\non destination tile";
        }
        else if (this.isMoveSelected) {
            msg = "Cannot move: " + this.unitInfo(chosenUnit) + "\nwas deleted";
            this.isMoveSelected = false;
        }
        else {msg = "Selected " + this.unitInfo(chosenUnit);}
        this.updateGameMsg(msg);            // UI (left click)
    }


    @Override
    public void callReinforcement() {
        String msg = "";
        // if unit already on cell: abort
        if (this.selectedUnit.isPresent()){
            this.updateGameMsg("Select an empty cell first");            // UI
            return;
        }

        // else empty cell: add reinf. unit
        Fairy tmpUnit = new Fairy(UnitColor.ONE);
        this.currentSnap.getBoard().addUnit(this.selectedRowIndex,
                                            this.selectedColumnIndex,
                                            tmpUnit);
        this.selectedUnit = Optional.of(tmpUnit);   // update as last selected cell

        // UI
        msg = "Added " + this.unitInfo(Optional.of(tmpUnit)) +
                "\nat cell (" + this.selectedRowIndex + ", " +
                this.selectedColumnIndex + ")";
        this.updateGameUI(msg);
    }

    
    @Override
    public void deleteUnit(int rowIndex, int columnIndex) {
        Optional<Unit> chosenUnit = this.getUnitAsOptional(rowIndex, columnIndex);

        // if empty tile
        if (chosenUnit.isEmpty()){
            this.updateGameMsg("No unit to be removed");            // UI
            return;
        }

        // else: unit is present on tile
        this.currentSnap.getBoard().removeUnit(rowIndex, columnIndex);
        // empty tile if it was also the last selected cell: this.selectedUnit = Optional.of(null);
        if (chosenUnit.equals(this.selectedUnit)){this.selectedUnit = Optional.empty();}
        if (chosenUnit.equals(this.movingUnit)){this.movingUnit = Optional.empty();}

        String msg = "Removed " + this.unitInfo(chosenUnit);
        this.updateGameUI(msg);        // UI
    }

    @Override
    public void movementSwitch() {
        // if movement button was already selected -> cancel action
        if (this.isMoveSelected){
            this.isMoveSelected = false;
            this.updateGameMsg("Movement cancelled");            // UI
            return;
        }

        // if no unit is selected -> exit
        if (this.selectedUnit.isEmpty() ||
                this.selectedUnit.get() instanceof ZeroVoid) {
            this.updateGameMsg("No unit to be moved");            // UI
            return;
        }

        // else, turn movement switch on
        this.isMoveSelected = true;
        this.setMovingUnitInfo(this.selectedRowIndex, this.selectedColumnIndex, this.selectedUnit);
        this.updateGameMsg("Please select a destination tile");            // UI
    }

    /*
        Helper methods
     */
    public String unitInfo(Optional<Unit> chosenUnit) {
        String msg = "Unit with: \n{";

        // if empty tile
        if (chosenUnit.isEmpty()) {
            msg = "Empty tile";
            return msg;
        }

        // else: is unit
        Unit extractedUnit = chosenUnit.get();
        int health = extractedUnit.getHealth();
        // if mobile
        if (extractedUnit instanceof MobileUnit mobile){        // pattern var :)
            UnitColor color = mobile.getColor();
            int atkCountdown = mobile.getAttackCountdown();
            // color & atk countdown
            msg = msg + "Color: " + color + ",\n" +
                    "Attacks in: " + atkCountdown + " turns,\n";
        }
        return msg + "Health: " + health + "}";
    }

    /**
     * Updates the game UI, by drawing the updated snapshot.
     * Displays a message regarding the changes in the board (or snapshot)
     * @param msg A message describing the changes in the snapshot (unit deleted, moved, reinf. added)
     */
    public void updateGameUI(String msg){
        this.DM.drawSnapshot(this.currentSnap, msg);
    }

    /**
     * Updates only the message in the game UI.
     * Uses less resources, when compared with redrawing the whole UI
     * @param msg A message describing the changes in the game (skip turn, hover on or select tile)
     */
    public void updateGameMsg(String msg){
        try {this.DM.updateMessage(msg);}
        catch (NoGameOnScreenException e) {throw new RuntimeException(e);}
    }

    /**
     * Inverts truth of the boolean representing whether P1 is playing.
     * Called from the outer method skipTurn()
     */
    public void invertTurns(){
        this.isP1 = !this.isP1;
    }

    /**
     * Retrieves the unit in the underlying board at given coordinates.
     * If the tile is empty, it is signalled as a null.
     * Makes an outer call to the snapshot, and an inner call to its board.
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    public Optional<Unit> getUnitAsOptional(int rowIndex, int columnIndex){
        return this.currentSnap.getBoard().getUnit(rowIndex, columnIndex);
    }

    /**
     * Stores info and coordinates of last selected (LX click) unit or tile.
     * @param rowIndex
     * @param columnIndex
     * @param chosenUnit
     */
    public void setSelectedUnitInfo(int rowIndex, int columnIndex, Optional<Unit> chosenUnit){
        this.selectedUnit = chosenUnit;
        this.selectedRowIndex = rowIndex;
        this.selectedColumnIndex = columnIndex;
    }

    /**
     * Stores info and coordinates of unit that is to be moved.
     * @param rowIndex
     * @param columnIndex
     * @param chosenUnit
     */
    public void setMovingUnitInfo(int rowIndex, int columnIndex, Optional<Unit> chosenUnit){
        this.movingUnit = chosenUnit;    // store unit for movement
        this.movingRowIndex = rowIndex;
        this.movingColumnIndex = columnIndex;
    }

    /**
     * Inner method to move a unit to an empty destination tile.
     * Called from the outer method selectTile()
     * @param rowIndex
     * @param columnIndex
     */
    public void moveUnit(int rowIndex, int columnIndex){
        // move unit to new tile
        this.currentSnap.getBoard().addUnit(
                rowIndex,
                columnIndex,
                this.movingUnit.get());

        // delete unit from old tile
        this.currentSnap.getBoard().removeUnit(this.movingRowIndex, this.movingColumnIndex);

        // switch movement off
        this.isMoveSelected = false;

        // store moved unit as the currently selected one
        this.setSelectedUnitInfo(rowIndex, columnIndex, this.movingUnit);

        // UI
        String msg = "Moved " + this.unitInfo(Optional.of(this.movingUnit.get())) +
                "\nfrom cell (" + this.movingRowIndex + ", " + this.movingColumnIndex + ")" +
                "\nto cell (" + this.selectedRowIndex + ", " + this.selectedColumnIndex + ")";
        this.updateGameUI(msg);
    }
}
