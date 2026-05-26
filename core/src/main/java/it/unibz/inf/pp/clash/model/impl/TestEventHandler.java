package it.unibz.inf.pp.clash.model.impl;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.TestSnapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.*;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import static it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player.FIRST;
import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.THREE;
import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.ZERO;

public class TestEventHandler implements EventHandler {

        private final DisplayManager DM;
        private TestSnapshot currentSnap;

        private boolean isP1 = true;            // current player

        private Optional<Unit> selectedUnit = Optional.of(new ZeroVoid());    // current unit
        private int selectedRowIndex;
        private int selectedColumnIndex;

        private boolean isMoveSelected = false; // if move button was clicked
        private Optional<Unit> movingUnit = Optional.of(new ZeroVoid());
        private int movingRowIndex;
        private int movingColumnIndex;

        private boolean isAttackSelected = false;
        private Optional<MobileUnit> attackingUnit;
        private int attackingRowIndex;
        private int attackingColumnIndex;
        private int attackerDamage;

    public TestEventHandler(DisplayManager displayManager) {
        this.DM = displayManager;
    }

    @Override
    public void newGame(String firstHero, String secondHero) {

        // TODO: change snapshot depending on difficulty (or level)
        this.currentSnap = new TestSnapshot(firstHero, secondHero);    // create snapshot
        this.updateGameUI("Welcome to the game!");                                         // update UI
    }

    @Override
    public void exitGame() {
        // implementing goodbye -> not at the moment
        this.DM.drawHomeScreen();
    }

    /*
        In-game mechanics
     */

    @Override
    public void skipTurn() {

        // invert turns, and continue game
        this.invertTurns();


        // BEGIN: Atk Countdown
        if (this.isP1){
            this.currentSnap.setActivePlayer(FIRST);
            // decrease atk countdown
            for (MobileUnit mobile : this.currentSnap.getBoard().getArmyP1()){
                if(mobile.getAttackCountdown() > 0) {
                    mobile.setAttackCountdown(mobile.getAttackCountdown() - 1);
                }
            }
        }
        else {
            this.currentSnap.setActivePlayer(Snapshot.Player.SECOND);

            // decrease atk countdown
            for (MobileUnit mobile : this.currentSnap.getBoard().getArmyP2()){
                if(mobile.getAttackCountdown() > 0) {
                    mobile.setAttackCountdown(mobile.getAttackCountdown() - 1);
                }
            }
        }
        // END: Atk Countdown

        // reset remaining actions for player (remember: it's a global variable)
        this.currentSnap.setNumberOfRemainingActions(3);

        // reset last selected cell / moving unit / attacking unit
        this.selectedUnit = Optional.empty();
        this.isMoveSelected = false;
        this.movingUnit = Optional.empty();
        this.isAttackSelected = false;
        this.attackingUnit = Optional.empty();

        // messages
        String player = "";
        if (this.isP1){player = "P1";}
        else {player = "P2";}
        String msg = "Turn skipped;\n" + player + " now playing";

        //this.updateGameMsg(msg);            // UI
        this.updateGameUI(msg);
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

            // removed for implementing free movement
            // PLAYER check: only select units/tiles on their side of the board
//            if (!this.areValidPlayerCoordinates(rowIndex)){
//                this.updateGameMsg("Select a valid tile (on your board side)");
//                return;
//            }

            // MOVEMENT check: if selection (LX click) is the destination of a unit movement
            if (this.isMoveSelected &&
                    this.movingUnit.isPresent() &&
                    chosenUnit.isEmpty()){
                if (this.isValidMovement(rowIndex, columnIndex)) {
                    this.moveUnit(rowIndex, columnIndex);
                } else {
                    this.updateGameMsg("Selected tile exceeds this unit's walking distance");
                }
                return;
            }

            // ATTACK check: if selected target (LX) is the destination of a unit attack
            if (this.isAttackSelected &&
                    this.attackingUnit.isPresent() &&
                    chosenUnit.isPresent()){
                if(this.isValidAttack(rowIndex, columnIndex)){
                    this.attackUnit(rowIndex, columnIndex, chosenUnit);
                } else {
                    this.updateGameMsg("Defending unit exceeds this unit's attack range");
                }
                return;
            }

        // if opponent's unit, do not store as selection
//        if (this.isP1 && this.currentSnap.getBoard().getArmyP2().contains(chosenUnit.get())){
//            this.updateGameMsg("Cannot select an opponent's unit");
//            return;
//        }

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

        // UI (LX click, no attack & no movement)
        else {msg = "Selected " + this.unitInfo(chosenUnit);}
        this.updateGameMsg(msg);            // UI (left click)
    }


    @Override
    public void callReinforcement() {
        String msg = "";

            // ACTIONS check: return if zero actions left
            if (this.currentSnap.getNumberOfRemainingActions() <= 0){
                this.updateGameMsg("No actions left for this turn");
                return;
            }

            // removed for free movement
            // PLAYER check: only select units/tiles on their side of the board
//            if (!this.areValidPlayerCoordinates(this.selectedRowIndex)){
//                this.updateGameMsg("Select a valid tile (on your board side)");
//                return;
//            }

            // if atk button was selected -> cancel attack (but proceed with movement)
            if (this.isAttackSelected){
                this.isAttackSelected = false;
                this.attackingUnit = Optional.empty();
                //this.attackingRowIndex

                this.updateGameMsg("Attack cancelled; " +
                        "if you want to add reinforcements, press this button again");
                return;
            }

            // if movement button was selected -> cancel movement (but proceed with atk)
            if (this.isMoveSelected){
                this.isMoveSelected = false;
                this.movingUnit = Optional.empty();
                //this.attackingRowIndex

                this.updateGameMsg("Movement cancelled; " +
                        "if you want to add reinforcements, press this button again");
                return;
            }

            // if unit already on cell: abort
            if (this.selectedUnit.isPresent()){
                this.updateGameMsg("Select an empty cell first");            // UI
                return;
            }

            // if not at end of own board side, or close to allied units: abort
            if (!this.isValidReinforcement(this.selectedRowIndex, this.selectedColumnIndex)){
                this.updateGameMsg("Reinforcement unit must be positioned at " +
                        "the end of your board side, or close to an ally");            // UI
                return;
            }

        // else empty cell: add random reinforcement unit and select it
        this.selectedUnit = this.randomReinforcement();

        this.currentSnap.getBoard().addUnitForPlayer(this.selectedRowIndex,
                                            this.selectedColumnIndex,
                                            this.selectedUnit.get(),
                                            this.currentSnap.getActivePlayer());
        //this.selectedUnit = Optional.of(tmpUnit);   // update as last selected cell

        // decrease player actions by 1
        this.currentSnap.setNumberOfRemainingActions(this.currentSnap.getNumberOfRemainingActions() - 1);

        // UI
        msg =   "Added " + this.unitInfo(this.selectedUnit) +
                "\nat cell (" + this.selectedRowIndex + ", " +
                this.selectedColumnIndex + ")";
        this.updateGameUI(msg);
    }

    
    @Override
    public void deleteUnit(int rowIndex, int columnIndex) {
        Optional<Unit> chosenUnit = this.getUnitAsOptional(rowIndex, columnIndex);

            // removed for free movement
            // 1) player check: only select units/tiles on their side of the board
//            if (!this.areValidPlayerCoordinates(rowIndex)){
//                this.updateGameMsg("Tile not valid for deletion (not on your board side)");
//                return;
//            }
            // 2) if empty tile
            if (chosenUnit.isEmpty()){
                this.updateGameMsg("No unit to be removed");
                return;
            }

        // else: unit is present on tile
        this.currentSnap.getBoard().removeUnitForPlayer(rowIndex, columnIndex);
        // empty tile if it was also the last selected cell or moving unit
        if (chosenUnit.equals(this.selectedUnit)){this.selectedUnit = Optional.empty();}
        if (chosenUnit.equals(this.movingUnit)){this.movingUnit = Optional.empty();}

        String msg = "Removed " + this.unitInfo(chosenUnit);
        this.updateGameUI(msg);        // UI
    }

    @Override
    public void movementSwitch() {

        // ACTIONS check: return if zero actions left
        if (this.currentSnap.getNumberOfRemainingActions() <= 0){
            this.updateGameMsg("No actions left for this turn");
            return;
        }

        // if movement button was already selected -> cancel action
        if (this.isMoveSelected){
            this.isMoveSelected = false;
            this.updateGameMsg("Movement cancelled");
            return;
        }

        // if atk button was selected -> cancel attack (but proceed with movement)
        if (this.isAttackSelected){
            this.isAttackSelected = false;
            this.attackingUnit = Optional.empty();
            //this.attackingRowIndex

            this.updateGameMsg("Attack cancelled; " +
                    "if you want to proceed with movement, press this button again");
            return;
        }

        // if no unit is selected -> exit
        if (this.selectedUnit.isEmpty() ||
                this.selectedUnit.get() instanceof ZeroVoid) {
            this.updateGameMsg("No unit to be moved");
            return;
        }

        // if unit is not in current player's army -> exit
        // in this case, the game is simple, so no casting for selectedUnit is necessary
        if ((this.isP1 && this.currentSnap.getBoard().getArmyP2().contains(this.selectedUnit.get())) ||
           ((!this.isP1) && this.currentSnap.getBoard().getArmyP1().contains(this.selectedUnit.get()))){
            this.updateGameMsg("Cannot move an opponent's unit");
            return;
        }

        // else, turn movement switch on
        this.isMoveSelected = true;
        this.setMovingUnitInfo(this.selectedRowIndex, this.selectedColumnIndex, this.selectedUnit);
        this.updateGameMsg("Please select a destination tile");            // UI
    }

    public void attackSwitch(){
        // suppose to use a button, as with movement
        // thus, starting point is the selected unit

        // ACTIONS check: return if zero actions left
        if (this.currentSnap.getNumberOfRemainingActions() <= 0){
            this.updateGameMsg("No actions left for this turn");
            return;
        }

        // if switch already true -> turn off attack
        if (this.isAttackSelected){
            this.isAttackSelected = false;
            this.updateGameMsg("Attack cancelled");
            return;
        }

        // if movement button was selected -> cancel movement (but proceed with atk)
        if (this.isMoveSelected){
            this.isMoveSelected = false;
            this.movingUnit = Optional.empty();
            //this.attackingRowIndex

            this.updateGameMsg("Movement cancelled; " +
                    "if you want to attack, press this button again");
            return;
        }

        // EMPTY tile check -> exit
        if (this.selectedUnit.isEmpty()){
            this.updateGameMsg("Select a unit before declaring the attack");
            return;
        }

        // if unit is not in current player's army -> exit
        // in this case, the game is simple, so no casting for selectedUnit is necessary
        if ((this.isP1 && this.currentSnap.getBoard().getArmyP2().contains(this.selectedUnit.get())) ||
           ((!this.isP1) && this.currentSnap.getBoard().getArmyP1().contains(this.selectedUnit.get()))){
            this.updateGameMsg("Cannot attack with an opponent's unit");
            return;
        }

        // STATIC unit check -> exit
        if (!(this.selectedUnit.get() instanceof MobileUnit mobile)){
            this.updateGameMsg("A static unit can't attack");
            return;
        }

        // else -> check atkCountdown
        // if 0 -> turn attack switch on
        int attackCountdown = mobile.getAttackCountdown();
        if (attackCountdown != 0){
            this.updateGameMsg("This unit's attack countdown isn't zero yet");
            return;
        }

        this.isAttackSelected = true;
        this.attackingUnit = Optional.of(mobile);
        //this.attackerDamage = mobile.getAttackDamage();
        this.attackingRowIndex = this.selectedRowIndex;
        this.attackingColumnIndex = this.selectedColumnIndex;
        this.attackerDamage = this.attackingUnit.get().getAttackDamage();
        this.updateGameMsg("Choose unit or player to attack");
    }

    public void attackUnit(int rowIndex, int columnIndex, Optional<Unit> chosenUnit){

        // should only target the oppo's units
        // if unit is not in opponent's army -> exit
        // in this case, the game is simple, so no casting for selectedUnit is necessary
        if ((this.isP1 && this.currentSnap.getBoard().getArmyP1().contains(chosenUnit.get())) ||
                ((!this.isP1) && this.currentSnap.getBoard().getArmyP2().contains(chosenUnit.get()))){
            this.updateGameMsg("Cannot attack your own unit");
            return;
        }

        // deal damage to unit (health - damage)
        chosenUnit.get().setHealth(chosenUnit.get().getHealth() - this.attackerDamage);

        // reset attackCountdown for the attacking unit
        this.attackingUnit.get().setAttackCountdown(3);

        // decrease player actions by 1
        this.currentSnap.setNumberOfRemainingActions(this.currentSnap.getNumberOfRemainingActions() - 1);

        // if unit's health is zero or less -> delete
        // and subtract 1 HP from opposing player
        // (remember: because the attack takes priority, the target was not set as selectedUnit)
        if (chosenUnit.get().getHealth() <= 0){
            this.currentSnap.getBoard().removeUnitForPlayer(rowIndex, columnIndex);

            Hero Opponent = this.currentSnap.getOpponentHero(this.currentSnap.getActivePlayer());
            Opponent.setHealth(Opponent.getHealth() - 1);

            // if oppo has zero life, current player wins
            if (Opponent.getHealth() <= 0){

                // show winning player name
                String victoryMsg;
                String victoryHeroName = this.currentSnap.getHero(this.currentSnap.getActivePlayer()).getName();
                if (this.isP1){
                    victoryMsg = "Player 1 " + victoryHeroName + " has won! Wow!";
                } else {
                    victoryMsg = "Player 2 " + victoryHeroName + " has won! Wow!";
                }

                // End Game UI
                this.DM.drawEndGameScreen(this.currentSnap, this.currentSnap,
                                victoryMsg);

                // reset global variables (selected/attacking/etc), in case of new game
                // reset attacking unit
                this.isAttackSelected = false;
                this.attackingUnit = Optional.empty();
                // reset last selected cell
                this.selectedUnit = Optional.empty();
                // reset moving unit
                this.isMoveSelected = false;
                this.movingUnit = Optional.empty();

                return;     // MANDATORY, breaks from the rendering loop (which would draw the snapshot again)
            }

        }

        // update UI
        String msg = this.unitInfo(Optional.of(this.attackingUnit.get())) +
                "\non cell (" + this.attackingRowIndex + ", " + this.attackingColumnIndex + ")" +
                "\nattacked" +
                this.unitInfo(chosenUnit) +
                "\non cell (" + rowIndex + ", " + columnIndex + ")";
        this.updateGameUI(msg);

        // turn attack off and attacker to non-attacking state (set to empty)
        this.isAttackSelected = false;
        this.attackingUnit = Optional.empty();
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
        //this.currentSnap.setOngoingMove(rowIndex, columnIndex); // ongoing move (now only works as destination)
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
        this.currentSnap.getBoard().addUnitForPlayer(
                rowIndex,
                columnIndex,
                this.movingUnit.get(),
                this.currentSnap.getActivePlayer());

        // delete unit from old tile
        this.currentSnap.getBoard().removeUnitForPlayer(this.movingRowIndex, this.movingColumnIndex);

        // switch movement off
        this.isMoveSelected = false;

        // store moved unit as the currently selected one
        this.setSelectedUnitInfo(rowIndex, columnIndex, this.movingUnit);

        // decrease player actions by 1
        this.currentSnap.setNumberOfRemainingActions(this.currentSnap.getNumberOfRemainingActions() - 1);

        // UI
        String msg = "Moved " + this.unitInfo(Optional.of(this.movingUnit.get())) +
                "\nfrom cell (" + this.movingRowIndex + ", " + this.movingColumnIndex + ")" +
                "\nto cell (" + this.selectedRowIndex + ", " + this.selectedColumnIndex + ")";
        this.updateGameUI(msg);
    }

    /**
     * Checks if the player can select, add, or delete a unit.
     * It relies on the y-coordinate (rowIndex)
     * @param rowIndex row index of tile
     * //@param columnIndex col index of tile
     * @return whether the player is allowed to select or delete said unit/tile
     */
    public boolean areValidPlayerCoordinates(int rowIndex){

        // col validity is always true (different story for the board implementation)
        // check row validity; y-axis begins from the TOP
        int maxRowIndexBoard = this.currentSnap.getBoard().getMaxRowIndex(); // always odd

        if (this.currentSnap.getActivePlayer() == FIRST){
            return rowIndex <= maxRowIndexBoard &&
                    rowIndex >= (maxRowIndexBoard+1)/2;     // P1: (half of max):max
        }
        else {
            return rowIndex < (maxRowIndexBoard+1)/2;               // P2: 0:(half of max -1)
        }
    }

    public boolean isValidMovement(int rowIndex, int columnIndex){

        int rowMovement = Math.abs(rowIndex - this.movingRowIndex);
        int columnMovement = Math.abs(columnIndex -this.movingColumnIndex);
        int diagonalMovement = rowMovement + columnMovement;

        MobileUnit mobile = (MobileUnit) this.movingUnit.get();

        if (diagonalMovement <= mobile.getMaxTileMovement()){
            return true;
        } else {
            return false;
        }
    }

    public boolean isValidAttack(int rowIndex, int columnIndex) {

        boolean rowAttack = Math.abs(rowIndex - this.attackingRowIndex) <= 1;
        boolean columnAttack = Math.abs(columnIndex -this.attackingColumnIndex) <= 1;

        if (rowAttack && columnAttack) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isValidReinforcement(int rowIndex, int columnIndex){

        // first possibility: reinforcement is placed at the end of player's board side

        //boolean locationalValidity = false;
        if (this.isP1 && (rowIndex == this.currentSnap.getBoard().getMaxRowIndex())) {
            //locationalValidity = true;
            return true;
        } else if ((!this.isP1) && rowIndex == 0) {
            //locationalValidity = true;
            return true;
        }

        // second alternative: reinforcement is placed close to own unit
        int[] validRows = {rowIndex, rowIndex + 1, rowIndex - 1};
        int[] validColumns = {columnIndex, columnIndex + 1, columnIndex - 1};

        int validRowsLength = 3;
        int validColumnsLength = 3;

        if (rowIndex == 0){
            validRows = new int[]{rowIndex, rowIndex + 1};
            validRowsLength = 2;
        } else if (rowIndex == this.currentSnap.getBoard().getMaxRowIndex()) {
            validRows = new int[]{rowIndex, rowIndex - 1};
            validRowsLength = 2;
        } //else {
            //int[] validRows = {rowIndex, rowIndex + 1, rowIndex - 1};
        //}

        if (columnIndex == 0){
            validColumns = new int[]{columnIndex, columnIndex + 1};
            validColumnsLength = 2;
        } else if (columnIndex == this.currentSnap.getBoard().getMaxColumnIndex()) {
            validColumns = new int[]{columnIndex, columnIndex - 1};
            validColumnsLength = 2;
        } //else {
            //int[] validColumns = {columnIndex, columnIndex + 1, columnIndex - 1};
        //}

        //boolean isAllyClose = false;

        for (int i = 0; i < validRowsLength; i++){
            for (int j = 0; j < validColumnsLength; j++){
                Optional<Unit> currentTile = this.getUnitAsOptional(validRows[i], validColumns[j]);
                if (currentTile.isPresent()){
                    MobileUnit mobile = (MobileUnit) currentTile.get();
                    if ((this.isP1 && this.currentSnap.getBoard().getArmyP1().contains(mobile)) ||
                            ((!this.isP1) && this.currentSnap.getBoard().getArmyP2().contains(mobile))){
                        //isAllyClose = true;
                        //break;
                        return true;
                    }
                }
            }
        }

        // if not at own side's end, or no proximity to allies
        return false;
    }

    /**
     * TMP
     * Creates a random optional(unit) out of three units.
     * Called in the outer method callReinforcement()
     * @return the random optional(unit)
     */
    public Optional<Unit> randomReinforcement(){

        UnitColor playerColor;

        if (this.isP1){
            playerColor = ZERO;
        } else {
            playerColor = THREE;
        }

        Wolfie wolf = new Wolfie(playerColor);
        Hare rabbit = new Hare(playerColor);
        Tigre tiger = new Tigre(playerColor);

        Unit[] unitArray = {wolf, rabbit, tiger};
        int rndmIndex = new Random().nextInt(unitArray.length);

        return Optional.of(unitArray[rndmIndex]);
    }
}
