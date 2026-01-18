package it.unibz.inf.pp.clash.model;

public interface EventHandler {

    /**
     * This method is called when the user starts a new game.
     *
     * @param firstHero name of the hero for the first player
     * @param secondHero name of the hero for the second player
     */
    void newGame(String firstHero, String secondHero);

    /**
     * This method is called if there is an ongoing game and the user interrupts it.
     */
    void exitGame();

    /**
     * This method is called if the active player decides to skip his/her turn.
     */
    void skipTurn();

    /**
     * This method is called when the user calls reinforcements.
     * <p>
     * Note that this may be an "invalid" action.
     * For instance (depending on the rules of the game):
     * - if there is no reinforcement unit,
     * - if there is an ongoing move,
     * - etc.
     */
    void callReinforcement();

    /**
     * This method is called if the user asks for information about the tile ({@code rowIndex}, {@code columnIndex}),
     * or the unit standing on it.
     */
    void requestInformation(int rowIndex, int columnIndex);

    /**
     * This method is called if the user selects the tile with coordinates ({@code rowIndex}, {@code columnIndex}).
     * This tile may for instance be the arrival tile of the ongoing move (if any), or the initial tile of a new move.
     * <p>
     * Note that the coordinates may be "incorrect".
     * For instance (depending on the rules of the game):
     * - if the tile is not on the board of the active player (but instead on the board of the other player),
     * - if there is no ongoing move and the tile is empty,
     * - etc.
     */
    void selectTile(int rowIndex, int columnIndex);

    /**
     * This method is called if the user tries to delete the unit standing on the tile with coordinates
     * ({@code rowIndex}, {@code columnIndex}).
     * <p>
     * Note that the coordinates may be "incorrect".
     * For instance (depending on the rules of the game):
     * - if there is no unit on this tile,
     * - if the unit on this tile cannot be deleted,
     * - if there is an ongoing move,
     * - etc.
     */
    void deleteUnit(int rowIndex, int columnIndex);

    /**
     * This method is called when the user wants to move a unit to another tile.
     * For it to work, the user must first select a tile with a unit on it.
     * <p>
     * This method only signals that the selected unit is ready to be moved.
     * To complete the movement, the user must click on an empty destination tile.
     * <p>
     * If the move button is pressed again before the destination tile is selected, then the action is cancelled.
     */
    void movementSwitch();

    /**
     * This method is called when the user wants to attack a unit, causing it damage
     * For it to work, the user must first select a tile with a unit on it,
     * which becomes the attacking unit.
     * <p>
     * This method only signals that the selected unit is ready to attack.
     * To complete the attack, the user must click on a target (defending) unit.
     * <p>
     * If the attack button is pressed again before the destination tile is selected, then the action is cancelled.
     */
    void attackSwitch();
}
