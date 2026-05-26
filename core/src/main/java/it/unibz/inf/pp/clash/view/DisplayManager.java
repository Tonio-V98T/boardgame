package it.unibz.inf.pp.clash.view;

import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.view.exceptions.IncomparableBoardSizesException;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;

public interface DisplayManager {

    /**
     * Displays the home screen.
     */
    void drawHomeScreen();

    /**
     * Displays the input {@code snapshot} on screen, together with the input {@code message}.
     * If there was another snapshot on screen when this method is called, then a fade-in animation will highlight
     * differences between the two snapshots.
     *
     * @param snapshot the snapshot to be drawn on screen
     * @param message the message to be displayed
     * @throws IncomparableBoardSizesException if there was a snapshot on screen when this method is called and
     * the dimensions of its board does not match the dimensions of the board of the input {@code snapshot}.
     */
    void drawSnapshot(Snapshot snapshot, String message);

    /**
     * If a game screen is currently displayed, then updates the message displayed on screen.
     * Otherwise this method has no effect.
     * <p>
     * Calling this method is equivalent to calling {@link DisplayManager#drawSnapshot} with the current snapshot and a
     * new message, but more efficient.
     *
     * @param message the message to be displayed
     */
    void updateMessage(String message) throws NoGameOnScreenException;

    void setEventHandler(EventHandler eventHandler);

    /**
     * Testing End game screen
     */

    void drawEndGameScreen(Snapshot previousSnapshot, Snapshot newSnapshot,
                           String victoryMessage);

}
