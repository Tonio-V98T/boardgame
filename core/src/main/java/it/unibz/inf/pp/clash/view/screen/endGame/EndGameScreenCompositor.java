package it.unibz.inf.pp.clash.view.screen.endGame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import it.unibz.inf.pp.clash.controller.listeners.ExitButtonListener;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.view.screen.Compositor;
import it.unibz.inf.pp.clash.view.screen.game.BoardCompositor;
import it.unibz.inf.pp.clash.view.screen.game.PlayerCompositor;
import it.unibz.inf.pp.clash.view.screen.sync.AnimationCounter;
import it.unibz.inf.pp.clash.view.singletons.Dimensions;
import it.unibz.inf.pp.clash.view.singletons.FontManager;
import it.unibz.inf.pp.clash.view.singletons.ImageManager;
import it.unibz.inf.pp.clash.view.singletons.SkinManager;

import static it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player.FIRST;
import static it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player.SECOND;
import static it.unibz.inf.pp.clash.view.singletons.FontManager.FontType.INFORMATION;
import static it.unibz.inf.pp.clash.view.singletons.ImageManager.Icon.EXIT;
import static it.unibz.inf.pp.clash.view.singletons.ImageManager.IconSize.LARGE;

public class EndGameScreenCompositor extends Compositor {

    private final PlayerCompositor playerCompositor;

    public EndGameScreenCompositor(EventHandler eventHandler, AnimationCounter animationCounter, boolean debug) {
        super(eventHandler, animationCounter,debug);

        playerCompositor = new PlayerCompositor(eventHandler, animationCounter, debug);
    }

    /**
     * Main method
     * Parameter "Game game" removed, as it is only used in the event of a CloseApplicationListener
     * Main structure to be implemented from GameCompostitor (and NOT HomeScreenCompositor)
     */

    public Table drawEndGameScreen(Snapshot previousSnapshot, Snapshot newSnapshot,
                                   String victoryMessage){//, Table mainTable) {

        Table mainTable = createMainTable(); //from homeScreenCompositor; the gameCompositor has it as parameter

        Dimensions dimensions = Dimensions.instance();

        mainTable.add(
                playerCompositor.drawPlayerEndGame(
                        previousSnapshot,
                        newSnapshot,
                        SECOND
                )).top();

        // display victory message on a new line
        mainTable.row();
        Label messageLabel = drawMessage(
                victoryMessage
        );
        mainTable.add(messageLabel)
                .width(dimensions.getInfoboxWidth())
                .pad(50);

        mainTable.row();
        mainTable.add();
        mainTable.add();
        mainTable.row();

        mainTable.add(
                playerCompositor.drawPlayerEndGame(
                        previousSnapshot,
                        newSnapshot, FIRST
                )).bottom();

        mainTable.add(drawExitButton())
                .expandY().bottom();

        return mainTable;
    }

    /**
     * Helper methods
     */

    private Label drawMessage(String text) {
        Label label = FontManager.instance().getLabel(
                text,
                INFORMATION
        );
        label.setWrap(true);
        return label;
    }

    private Table drawExitButton() {
        Table table = new Table();
        table.add(getExitButton());
        table.row();
        addMediumVerticalSpace(table);
        return table;
    }

    private ImageButton getExitButton() {
        ImageButton button = ImageManager.instance().getIconButton(EXIT, LARGE);
        button.addListener(
                new ExitButtonListener(eventHandler)
        );
        return button;
    }
}
