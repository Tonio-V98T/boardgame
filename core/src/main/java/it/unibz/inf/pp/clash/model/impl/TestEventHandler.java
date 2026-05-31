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
import static it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player.SECOND;
import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.THREE;
import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.ZERO;

public class TestEventHandler implements EventHandler {

        private final DisplayManager DM;
        private TestSnapshot currentSnap;
        private boolean isP1 = true;            // current player

        private Optional<Unit> selectedUnit = Optional.empty();    // current unit
        private int selectedRowIndex;
        private int selectedColumnIndex;

        private boolean isMoveSelected = false; // if move button was clicked
        private Optional<Unit> movingUnit = Optional.empty();
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
        this.currentSnap = new TestSnapshot(firstHero, secondHero);    // create snapshot
        this.updateGameUI("Welcome to the game!");                                         // update UI
    }

    @Override
    public void exitGame() {
        this.DM.drawHomeScreen();
    }

    /* ************************************************
                    Skip Turn
       ************************************************ */

    /**
     * Used when pressing the skip turn button.
     * Inverts turns (P1 -> P2, or P2->P1),
     * decreases atk countdown for active player's units,
     * resets remaining actions to 3,
     * resets selected/moving/attacking units.
     */
    @Override
    public void skipTurn() {

        // invert turns, and set new player as active
        this.invertTurns();

        // decrease atk countdown for current player's units
        this.decreaseAttackCountdown();

        // reset remaining actions for player ("remaining actions" is a global variable)
        this.currentSnap.setNumberOfRemainingActions(3);

        // reset last selected cell / moving unit / attacking unit
        this.selectedUnit = Optional.empty();
        this.resetMovementSelection();
        this.resetAttackSelection();

        // messages
        String player = "P1";
        if (!this.isP1){player = "P2";}

        this.updateGameUI("Turn skipped;\n" + player + " now playing");
    }

    /**
     * Inverts truth of the boolean representing whether P1 is playing.
     * In Snapshot, sets the new player as active.
     */
    public void invertTurns(){
        // invert players
        this.isP1 = !this.isP1;

        // set new player as active
        if (this.isP1) {
            this.currentSnap.setActivePlayer(FIRST);
        } else {
            this.currentSnap.setActivePlayer(SECOND);
        }
    }

    /**
     * Retrieves the army of active player, and decreases each unit's attack countdown by 1.
     */
    public void decreaseAttackCountdown(){

        for (MobileUnit mobile : this.currentSnap.getBoard().getActivePlayerArmy(this.isP1)){
            if(mobile.getAttackCountdown() > 0) {
                mobile.setAttackCountdown(mobile.getAttackCountdown() - 1);
            }
        }
    }

    /* ************************************************
                Tile/Unit selection
       ************************************************ */

    /**
     * Called when pressing LX mouse button on a cell.
     * It performs one of three actions:
     * 1) MOVEMENT: if corresponding button was selected, distance is within unit's range,
     * and destination tile is empty -> moves previously-selected unit to target cell;
     * 2) ATTACK: if corresponding button was selected, target cell borders the attacker's cell,
     * and there's an opponent unit standing on target cell -> perform attack
     * 3) SELECT: if the cell is empty, or contains an allied unit, it stores the selection.
     * @param rowIndex y-index of selected cell (LX click)
     * @param columnIndex x-index of selected cell (LX click)
     */
    @Override
    public void selectTile(int rowIndex, int columnIndex) {
        Optional<Unit> unitToSelect = this.getUnitAsOptional(rowIndex, columnIndex);

        // Movem. sequence: movem. button -> distance -> empty destination tile
        if (this.isMoveSelected){
            if (this.isValidMovementDistance(rowIndex, columnIndex)){
                if (unitToSelect.isEmpty()){
                    this.moveUnit(rowIndex, columnIndex);
                    return;
                }
                this.updateGameMsg("Cannot move: " + this.unitInfo(rowIndex, columnIndex) + "\non destination tile");
                return;
            }
            this.updateGameMsg("Cannot move: selected tile exceeds this unit's walking distance");
            return;
        }

        // Atk check : atk button -> atk range -> is defending unit
        // allies are checked in attackUnit()
        if (this.isAttackSelected){
            if (this.isValidAttackDistance(rowIndex, columnIndex)){
                if (unitToSelect.isPresent()){
                    this.attackUnit(rowIndex, columnIndex);
                    return;
                }
                this.updateGameMsg("Cannot attack: no enemy unit on selected tile");
                return;
            }
            this.updateGameMsg("Cannot attack: target tile exceeds this unit's range");
            return;
        }

        // if in own army, or empty tile, store
        if (unitToSelect.isEmpty() ||
                this.isUnitAllied((MobileUnit) unitToSelect.get())){
            // store unit and its coordinates (for reinforcements and movement)
            this.setSelectedUnitInfo(rowIndex, columnIndex);//, chosenUnit);

            // UI (LX click, no attack & no movement)
            String msg = "Selected " + this.unitInfo(rowIndex, columnIndex);
            this.updateGameMsg(msg);
            return;
        }
        this.updateGameMsg("Please select an empty tile, or one of your units.");
    }

    /**
     * Stores info and coordinates of last selected (LX click) unit or tile.
     * @param rowIndex y-index of selected cell
     * @param columnIndex x-index of selected cell
     */
    public void setSelectedUnitInfo(int rowIndex, int columnIndex){
        this.selectedUnit = this.getUnitAsOptional(rowIndex, columnIndex);;
        this.selectedRowIndex = rowIndex;
        this.selectedColumnIndex = columnIndex;
    }

    /* ************************************************
                    Movement
       ************************************************ */

    /**
     * Called when pressing the movement button.
     * It turns on the movement flag, and registers currently-selected unit as moving.
     * Cancelled when:
     * 1) 0 remaining actions -> exit;
     * 2) movement flag already on -> deselect movement;
     * 3) attack flag already on -> deselect attack;
     * 4) no unit selected -> exit.
     */
    @Override
    public void movementSwitch() {

        // ACTIONS check: return if zero actions left
        if (this.currentSnap.getNumberOfRemainingActions() <= 0){
            this.updateGameMsg("No actions left for this turn");
            return;
        }

        // if movement button was already selected -> cancel action
        if (this.isMoveSelected){
            this.resetMovementSelection();
            this.updateGameMsg("Movement cancelled; " +
                    "if you want to proceed with movement, press this button again");
            return;
        }

        // if atk button was selected -> cancel attack
        if (this.isAttackSelected){
            this.resetAttackSelection();
            this.updateGameMsg("Attack cancelled; " +
                    "if you want to proceed with attacking, press this button again");
            return;
        }

        // if no unit is selected -> exit
        if (this.selectedUnit.isEmpty()) {
            this.updateGameMsg("No unit to be moved");
            return;
        }

        // else, turn movement switch on
        this.isMoveSelected = true;
        this.setMovingUnitInfo(this.selectedRowIndex, this.selectedColumnIndex, this.selectedUnit);
        this.updateGameMsg("Please select a destination tile");            // UI
    }

    /**
     * Moves a unit to an empty destination tile; called from selectTile().
     * @param targetRowIndex y-axis of destination tile
     * @param targetColumnIndex x-axis of destination tile
     */
    public void moveUnit(int targetRowIndex, int targetColumnIndex){
        // move unit to new tile
        this.currentSnap.getBoard().addUnitForPlayer(
                targetRowIndex,
                targetColumnIndex,
                this.movingUnit.get(),
                this.currentSnap.getActivePlayer());

        // delete unit from old tile
        this.currentSnap.getBoard().removeUnitForPlayer(this.movingRowIndex, this.movingColumnIndex);

        // switch movement off
        this.isMoveSelected = false;

        // store moved unit as the currently selected one
        this.setSelectedUnitInfo(targetRowIndex, targetColumnIndex);

        // decrease player actions by 1
        this.currentSnap.setNumberOfRemainingActions(this.currentSnap.getNumberOfRemainingActions() - 1);

        // UI
        String msg = "Moved " + this.unitInfo(targetRowIndex, targetColumnIndex) +
                "\nfrom cell (" + this.movingRowIndex + ", " + this.movingColumnIndex + ")" +
                "\nto cell (" + this.selectedRowIndex + ", " + this.selectedColumnIndex + ")";
        this.updateGameUI(msg);
    }

    /**
     * Stores info and coordinates of unit that is to be moved.
     * @param rowIndex y-axis of moving unit
     * @param columnIndex x-axis of moving unit
     * @param movingUnit Optional of the moving unit
     */
    public void setMovingUnitInfo(int rowIndex, int columnIndex, Optional<Unit> movingUnit){
        this.movingUnit = movingUnit;    // store unit for movement
        this.movingRowIndex = rowIndex;
        this.movingColumnIndex = columnIndex;
    }

    /**
     * Check if the destination tile of a movement is a valid target.
     * Each unit has a certain distance (number of tiles) it can travel at any movement.
     * This function checks whether the destination cell does not exceed said distance.
     * Diagonal movement is considered as the sum of horizontal and vertical tiles required to reach the destination.
     * Units can move through other units, as long as the destination cell is empty
     * @param destinationRowIndex y-axis of the cell where the unit is to be moved
     * @param destinationColumnIndex x-axis of the cell where the unit is to be moved
     * @return whether the destination is within movement range
     */
    public boolean isValidMovementDistance(int destinationRowIndex, int destinationColumnIndex){

        int rowMovement = Math.abs(destinationRowIndex - this.movingRowIndex);
        int columnMovement = Math.abs(destinationColumnIndex -this.movingColumnIndex);
        int diagonalMovement = rowMovement + columnMovement;

        MobileUnit mobile = (MobileUnit) this.movingUnit.get();

        if (diagonalMovement <= mobile.getMaxTileMovement()){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Turn off movement flag, and reset moving unit to null
     */
    public void resetMovementSelection(){
        this.isMoveSelected = false;
        this.movingUnit = Optional.empty();
    }

    /* ************************************************
                Attacking
       ************************************************ */

    /**
     * Called when pressing the attack button.
     * It turns on the attack flag, and registers currently-selected unit as attacking.
     * Cancelled when:
     * 1) 0 remaining actions -> exit;
     * 2) attack flag already on -> deselect attack;
     * 3) movement flag already on -> deselect movement;
     * 4) no unit selected -> exit;
     * 5) Attacking unit's countdown isn't zero yet -> exit.
     */
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
            this.resetAttackSelection();
            this.updateGameMsg("Attack cancelled");
            return;
        }

        // if movement button was selected -> cancel movement (but proceed with atk)
        if (this.isMoveSelected){
            this.resetMovementSelection();
            this.updateGameMsg("Movement cancelled; " +
                    "if you want to attack, press this button again");
            return;
        }

        // EMPTY tile check -> exit
        if (this.selectedUnit.isEmpty()){
            this.updateGameMsg("Select a unit before declaring the attack");
            return;
        }

        // else -> check atkCountdown
        // if 0 -> turn attack switch on
        MobileUnit mobile = (MobileUnit) this.selectedUnit.get();
        int attackCountdown = mobile.getAttackCountdown();
        if (attackCountdown != 0){
            this.updateGameMsg("This unit's attack countdown isn't zero yet");
            return;
        }

        this.isAttackSelected = true;
        this.setAttackingUnitInfo(this.selectedRowIndex, this.selectedColumnIndex, Optional.of(mobile));
        this.updateGameMsg("Choose unit or player to attack");
    }

    /**
     * Inflicts damage to target (defending) unit; called from selectTile(), if the attack flag is on.
     * Attack is cancelled if the defending unit is an ally.
     * When defending unit dies, its owner loses 1HP.
     * If a player has 0HP, the game ends.
     * @param defenderRowIndex y-axis of defending unit
     * @param defenderColumnIndex x-axis of defending unit
     */
    public void attackUnit(int defenderRowIndex, int defenderColumnIndex){

        // defending unit presence already checked in selectTile()
        MobileUnit defenderUnit = (MobileUnit) this.getUnitAsOptional(defenderRowIndex, defenderColumnIndex).get();

        // should only target the oppo's units: if unit is allied -> exit
        if (this.isUnitAllied(defenderUnit)){
            this.updateGameMsg("Cannot attack your own unit");
            return;
        }

        // deal damage to unit (defender health - damage)
        defenderUnit.setHealth(defenderUnit.getHealth() - this.attackerDamage);

        // reset attackCountdown for the attacking unit
        this.attackingUnit.get().setAttackCountdown(3);

        // decrease player actions by 1
        this.currentSnap.setNumberOfRemainingActions(this.currentSnap.getNumberOfRemainingActions() - 1);

        // if unit's health is zero or less -> delete
        // and subtract 1 HP from opposing player
        // (remember: because the attack takes priority, the target was not set as selectedUnit)
        if (defenderUnit.getHealth() <= 0){
            this.currentSnap.getBoard().removeUnitForPlayer(defenderRowIndex, defenderColumnIndex);

            Hero Opponent = this.currentSnap.getOpponentHero(this.currentSnap.getActivePlayer());
            Opponent.setHealth(Opponent.getHealth() - 1);

            // if oppo has zero life, current player wins
            if (Opponent.getHealth() <= 0){
                this.callEndGame();
                return;     // MANDATORY, breaks from the rendering loop (which would draw the snapshot again)
            }

        }

        // update UI
        String msg = this.unitInfo(this.attackingRowIndex, this.attackingColumnIndex) +
                "\non cell (" + this.attackingRowIndex + ", " + this.attackingColumnIndex + ")" +
                "\nattacked " +
                this.unitInfo(defenderRowIndex, defenderColumnIndex) +
                "\non cell (" + defenderRowIndex + ", " + defenderColumnIndex + ")";
        this.updateGameUI(msg);

        // turn attack off and attacker to non-attacking state (set to empty)
        this.resetAttackSelection();
    }

    /**
     * Ends the game, drawing the EndGameScreen, and resetting global variables.
     * Called from attackUnit().
     */
    public void callEndGame(){
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

        // reset global variables (selected/attacking/moving), in case of new game
        this.resetAttackSelection();
        this.resetMovementSelection();
        this.selectedUnit = Optional.empty();
        this.isP1 = true;
    }

    /**
     * Stores info and coordinates of unit that is selected as attacker.
     * @param attackerRowIndex y-axis of attacking unit
     * @param attackerColumnIndex x-axis of attacking unit
     * @param attacker optional of attacking unit
     */
    public void setAttackingUnitInfo(int attackerRowIndex, int attackerColumnIndex, Optional<MobileUnit> attacker){
        this.attackingUnit = attacker;    // store unit for movement
        this.attackingRowIndex = attackerRowIndex;
        this.attackingColumnIndex = attackerColumnIndex;
        this.attackerDamage = attacker.get().getAttackDamage();
    }

    /**
     * Checks if attacker is within 1 tile from defending unit.
     * Differently from movement, this includes defending units standing diagonally from the attacker
     * @param defenderRowIndex y-axis of defending unit
     * @param defenderColumnIndex x-axis of defending unit
     * @return whether attacker and defender stand in neighbouring tiles
     */
    public boolean isValidAttackDistance(int defenderRowIndex, int defenderColumnIndex) {

        boolean rowAttack = Math.abs(defenderRowIndex - this.attackingRowIndex) <= 1;
        boolean columnAttack = Math.abs(defenderColumnIndex -this.attackingColumnIndex) <= 1;

        if (rowAttack && columnAttack) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Turn off attack flag, and reset attacking unit to null
     */
    public void resetAttackSelection(){
        this.isAttackSelected = false;
        this.attackingUnit = Optional.empty();
    }

    /* ************************************************
                Reinforcements
       ************************************************ */

    /**
     * Called when pressing the reinforcement button.
     * It spawns a random unit on a selected empty cell, which must either be at the end of the player's board side,
     * or close to an allied unit.
     * Cancelled when:
     * 1) 0 remaining actions -> exit;
     * 2) attack flag already on -> deselect attack;
     * 3) movement flag already on -> deselect movement;
     * 4) unit already on target cell -> exit;
     * 5) target cell not at the end of active player's board side, nor close to allied unit -> exit.
     */
    @Override
    public void callReinforcement() {
        //String msg = "";

            // ACTIONS check: return if zero actions left
            if (this.currentSnap.getNumberOfRemainingActions() <= 0){
                this.updateGameMsg("No actions left for this turn");
                return;
            }

            // if atk button was selected -> cancel attack (but proceed with movement)
            if (this.isAttackSelected){
                this.resetAttackSelection();
                this.updateGameMsg("Attack cancelled; " +
                        "if you want to add reinforcements, press this button again");
                return;
            }

            // if movement button was selected -> cancel movement (but proceed with atk)
            if (this.isMoveSelected){
                this.resetMovementSelection();
                this.updateGameMsg("Movement cancelled; " +
                        "if you want to add reinforcements, press this button again");
                return;
            }

            // if another unit on cell -> exit
            if (this.selectedUnit.isPresent() ||
                // following prevents errors when switching turns
                this.getUnitAsOptional(this.selectedRowIndex, this.selectedColumnIndex).isPresent()){
                this.updateGameMsg("Select an empty cell first");            // UI
                return;
            }

            // if not at end of own board side, or close to allied units: abort
            if (!this.isValidReinforcement(this.selectedRowIndex, this.selectedColumnIndex)){
                this.updateGameMsg("Reinforcement unit must be positioned at " +
                        "the end of your board side, or close to an ally");            // UI
                return;
            }

        // else: add random reinforcement unit and select it (coordinates are already selected)
        this.selectedUnit = this.randomReinforcement();
        this.currentSnap.getBoard().addUnitForPlayer(this.selectedRowIndex,
                                            this.selectedColumnIndex,
                                            this.selectedUnit.get(),
                                            this.currentSnap.getActivePlayer());

        // decrease player actions by 1
        this.currentSnap.setNumberOfRemainingActions(this.currentSnap.getNumberOfRemainingActions() - 1);

        // UI
        String msg =   "Added " + this.unitInfo(this.selectedRowIndex, this.selectedColumnIndex) +
                "\nat cell (" + this.selectedRowIndex + ", " +
                this.selectedColumnIndex + ")";
        this.updateGameUI(msg);
    }

    /**
     * Check if the tile destined for reinforcement unit is valid.
     * It is defined as valid when:
     * 1) tile is at the end of current player's board side
     * 2) or tile is next to (=bordering) an allied unit
     * @param rowIndex y-axis of cell selected as spawn point
     * @param columnIndex x-axis of cell selected as spawn point
     * @return whether the cell selected for reinforcement spawn is located at a valid position
     */
    public boolean isValidReinforcement(int rowIndex, int columnIndex){

        // 1) reinforcement is placed at the end of player's board side [locationalValidity]
        if (this.isP1 && (rowIndex == this.currentSnap.getBoard().getMaxRowIndex())) {
            return true;
        } else if ((!this.isP1) && rowIndex == 0) {
            return true;
        }

        // 2) else, reinforcement is placed close to allied unit
        int[] validRows = {rowIndex, rowIndex + 1, rowIndex - 1};
        int[] validColumns = {columnIndex, columnIndex + 1, columnIndex - 1};

        int validRowsLength = 3;
        int validColumnsLength = 3;

        // if at top or bottom row, exclude from check the row indices outside the board
        if (rowIndex == 0){
            validRows = new int[]{rowIndex, rowIndex + 1};
            validRowsLength = 2;
        } else if (rowIndex == this.currentSnap.getBoard().getMaxRowIndex()) {
            validRows = new int[]{rowIndex, rowIndex - 1};
            validRowsLength = 2;
        }

        // if at leftmost or rightmost column, exclude from check the column indices outside the board
        if (columnIndex == 0){
            validColumns = new int[]{columnIndex, columnIndex + 1};
            validColumnsLength = 2;
        } else if (columnIndex == this.currentSnap.getBoard().getMaxColumnIndex()) {
            validColumns = new int[]{columnIndex, columnIndex - 1};
            validColumnsLength = 2;
        }

        // checks if the 8 tiles surrounding the selected destination hold an ally
        for (int i = 0; i < validRowsLength; i++){
            for (int j = 0; j < validColumnsLength; j++){
                Optional<Unit> currentTile = this.getUnitAsOptional(validRows[i], validColumns[j]);
                if (currentTile.isPresent()){
                    MobileUnit mobile = (MobileUnit) currentTile.get();
                    if (this.isUnitAllied(mobile)){
                        return true;
                    }
                }
            }
        }
        // if not at own side's end, or no proximity to allies
        return false;
    }

    /**
     * Creates a random optional(unit) out of the three units in the game.
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

    /* ************************************************
                    Helper methods
       ************************************************ */

    /**
     * Retrieves the unit in the underlying board at given coordinates.
     * If the tile is empty, it is signalled as a null.
     * Makes first a call to the snapshot, and then a call to its underlying board.
     * @param rowIndex y-axis of selected cell
     * @param columnIndex x-axis of selected cell
     * @return the optional containing either the unit on selected tile, or an empty cell
     */
    public Optional<Unit> getUnitAsOptional(int rowIndex, int columnIndex){
        return this.currentSnap.getBoard().getUnit(rowIndex, columnIndex);
    }

    /**
     * Prints info on the unit (or empty tile) standing on the selected cell
     * @param rowIndex y-axis of selected cell
     * @param columnIndex x-axis of selected cell
     * @return a message containing info on the element standing on the cell
     */
    public String unitInfo(int rowIndex, int columnIndex) {

        Optional<Unit> chosenUnit = this.getUnitAsOptional(rowIndex, columnIndex);

        // if empty tile
        if (chosenUnit.isEmpty()) {return "Empty tile";}

        // else: is mobile unit
        MobileUnit mobile = (MobileUnit) chosenUnit.get();
        UnitColor color = mobile.getColor();
        int health = mobile.getHealth();
        int atkCountdown = mobile.getAttackCountdown();

        return "Unit with: \n{" +
                "Color: " + color + ",\n" +
                "Attacks in: " + atkCountdown + " turns,\n" +
                "Health: " + health + "}";
    }

    /**
     * Returns whether the (mobile) unit belongs to the active player's army.
     * @param unit
     * @return
     */
    public boolean isUnitAllied(MobileUnit unit){
        return this.currentSnap.getBoard().getActivePlayerArmy(this.isP1).contains(unit);
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
     * While hovering on a cell, returns info on the unit standing there.
     * Or it reports if the tile is empty.
     * @param rowIndex y-axis of the cell being currently hovered on
     * @param columnIndex x-axis of the cell being currently hovered on
     */
    @Override
    public void requestInformation(int rowIndex, int columnIndex) {
        this.updateGameMsg(this.unitInfo(rowIndex, columnIndex));
    }


    /* *********************************************
                Deprecated Methods
       ********************************************* */

    /**
     * DEPRECATED: actions do not depend anymore on the y-axis
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

    /**
     * DEPRECATED: player cannot arbitrarily choose to delete a unit
     * In a different universe, this could become the "attackUnit()"
     * @param rowIndex
     * @param columnIndex
     */
    @Override
    public void deleteUnit(int rowIndex, int columnIndex) {
        return;
    }
}
