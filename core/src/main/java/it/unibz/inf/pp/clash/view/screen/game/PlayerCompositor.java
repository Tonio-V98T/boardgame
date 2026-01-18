package it.unibz.inf.pp.clash.view.screen.game;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import it.unibz.inf.pp.clash.controller.listeners.MovementButtonListener;
import it.unibz.inf.pp.clash.controller.listeners.AttackButtonListener;
import it.unibz.inf.pp.clash.controller.listeners.ReinforcementButtonListener;
import it.unibz.inf.pp.clash.controller.listeners.SkipTurnButtonListener;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player;
import it.unibz.inf.pp.clash.view.screen.Compositor;
import it.unibz.inf.pp.clash.view.screen.sync.AnimationCounter;
import it.unibz.inf.pp.clash.view.singletons.ColorManager;
import it.unibz.inf.pp.clash.view.singletons.Dimensions;
import it.unibz.inf.pp.clash.view.singletons.ImageManager;
import it.unibz.inf.pp.clash.view.singletons.FontManager;


import static it.unibz.inf.pp.clash.view.singletons.ImageManager.Icon;
import static it.unibz.inf.pp.clash.view.singletons.ImageManager.IconSize;
import static it.unibz.inf.pp.clash.view.singletons.ImageManager.Icon.*;
import static it.unibz.inf.pp.clash.view.singletons.ImageManager.IconSize.LARGE;
import static it.unibz.inf.pp.clash.view.singletons.ImageManager.IconSize.MEDIUM;
import static it.unibz.inf.pp.clash.view.singletons.ColorManager.GuiColor;
import static it.unibz.inf.pp.clash.model.snapshot.Snapshot.Player.*;

public class PlayerCompositor extends Compositor {

    public PlayerCompositor(EventHandler eventHandler, AnimationCounter animationCounter, boolean debug) {
        super(eventHandler, animationCounter, debug);
    }

    public Table drawPlayer(Snapshot previousSnapshot, Snapshot newSnapshot, Player player) {

        Table playerTable = new Table();
        playerTable.setDebug(debug);

        Hero previousHero = previousSnapshot != null? previousSnapshot.getHero(player):null;
        Hero newHero = newSnapshot.getHero(player);

        boolean firstPlayerActive = newSnapshot.getActivePlayer() == FIRST;

        switch (player) {
            case FIRST -> {
                addReinforcementButton(previousSnapshot, newSnapshot, player, playerTable, firstPlayerActive);
                addSmallVerticalSpace(playerTable);                // was large
                addSkipTurnButton(playerTable, firstPlayerActive);
                // BEGIN TEST
                //addSmallHorizontalSpace(playerTable);               // TEST
                addMovementButton(playerTable, firstPlayerActive);  // TEST
                addAttackButton(playerTable, firstPlayerActive);
                // END TEST
                addMediumVerticalSpace(playerTable);                // was large
                addCountdown(previousSnapshot, newSnapshot, playerTable, firstPlayerActive);
                addSmallVerticalSpace(playerTable);
                addHealth(previousHero, newHero, playerTable);
                addSmallVerticalSpace(playerTable);
                addPortrait(newHero, playerTable);
                addSmallVerticalSpace(playerTable);
                addName(newHero, playerTable);
                addMediumVerticalSpace(playerTable);
            }
            case SECOND -> {
                addMediumVerticalSpace(playerTable);
                addName(newHero, playerTable);
                addSmallVerticalSpace(playerTable);
                addPortrait(newHero, playerTable);
                addSmallVerticalSpace(playerTable);
                addHealth(previousHero, newHero, playerTable);
                addSmallVerticalSpace(playerTable);
                addCountdown(previousSnapshot, newSnapshot, playerTable, !firstPlayerActive);
                addMediumVerticalSpace(playerTable);                // was large
                addSkipTurnButton(playerTable, !firstPlayerActive);
                // BEGIN TEST
                addMovementButton(playerTable, !firstPlayerActive);
                addAttackButton(playerTable, !firstPlayerActive);
                //addMediumVerticalSpace(playerTable); // currently too large with vertical spaces. why?
                // END TEST
                addSmallVerticalSpace(playerTable);                 // was large
                addReinforcementButton(previousSnapshot, newSnapshot, player, playerTable, !firstPlayerActive);
            }
        }
        return playerTable;
    }

    private void addSkipTurnButton(Table playerTable, boolean isActive) {
        if(isActive) {
            ImageButton button = getImageButton(SKIP, LARGE, false);
            button.addListener(new SkipTurnButtonListener(eventHandler));
            addSquareImageButton(
                    playerTable,
                    button,
                    Dimensions.instance().getLargeSquareIconLength()
            );
        } else {
            addVerticalSpace(playerTable, Dimensions.instance().getLargeSquareIconLength());
        }
        // COMMENTED OUT FOR MOVEMENT
        //playerTable.row();
    }

    /**
     * Currently works
     * @param playerTable
     * @param isActive
     */
    private void addMovementButton(Table playerTable, boolean isActive){
        if(isActive) {
            ImageButton button = getImageButton(MOVE, LARGE, false);
            button.addListener(new MovementButtonListener(eventHandler));
            addSquareImageButton(
                    playerTable,
                    button,
                    Dimensions.instance().getLargeSquareIconLength()
            );
        } else {
            addVerticalSpace(playerTable, Dimensions.instance().getLargeSquareIconLength());
        }
        playerTable.row();
    }

    /**
     * Add image, and test it!
     * @param playerTable
     * @param isActive
     */
    private void addAttackButton(Table playerTable, boolean isActive){
        if(isActive) {
            ImageButton button = getImageButton(ATTACK, LARGE, false);
            button.addListener(new AttackButtonListener(eventHandler));
            addSquareImageButton(
                    playerTable,
                    button,
                    Dimensions.instance().getLargeSquareIconLength()
            );
        } else {
            addVerticalSpace(playerTable, Dimensions.instance().getLargeSquareIconLength());
        }
        playerTable.row();
    }

    private void addReinforcementButton(Snapshot previousSnapshot, Snapshot newSnapshot, Player player,
                                        Table playerTable, boolean activePlayer) {

        int reinforcement = newSnapshot.getSizeOfReinforcement(player);
        boolean animation = previousSnapshot != null && reinforcement != previousSnapshot.getSizeOfReinforcement(player);

        if(activePlayer) {
            ImageButton button = getImageButton(
                    REINFORCEMENT,
                    LARGE,
                    animation
            );

            button.addListener(
                    new ReinforcementButtonListener(eventHandler)
            );

            addSquareImageButton(
                    playerTable,
                    button,
                    Dimensions.instance().getLargeSquareIconLength()
            );
        } else {

            addSquareIcon(
                    playerTable,
                    createIcon(REINFORCEMENT, LARGE, animation),
                    Dimensions.instance().getLargeSquareIconLength()
            );
        }

        playerTable.add(
                createLabel(
                        reinforcement,
                        GuiColor.REINFORCEMENT,
                        animation
                ));

        playerTable.row();
    }

    private void addHealth(Hero previousHero, Hero newHero, Table playerTable) {
        boolean animation = previousHero != null && previousHero.getHealth() != newHero.getHealth();

        addSquareIcon(
                playerTable,
                createIcon(HEART, MEDIUM, animation),
                Dimensions.instance().getMediumSquareIconLength()
        );

        playerTable.add(createLabel(
                "  "+ newHero.getHealth(),
                GuiColor.HERO_HEALTH,
                animation
        ));

        playerTable.row();
    }

    private void addCountdown(Snapshot previousSnapshot, Snapshot newSnapshot, Table playerTable, boolean activePLayer) {
        if(activePLayer) {
            int remainingActions = newSnapshot.getNumberOfRemainingActions();
            boolean animation = previousSnapshot != null && remainingActions != previousSnapshot.getNumberOfRemainingActions();

            addSquareIcon(
                    playerTable,
                    createIcon(COUNTDOWN, MEDIUM, animation),
                    Dimensions.instance().getMediumSquareIconLength()
            );
            playerTable.add(createLabel(
                    "  " + remainingActions,
                    GuiColor.COUNTDOWN,
                    animation
            ));
        } else {
            playerTable.add(createLabel( "" ));
        }
        playerTable.row();
    }

    private void addName(Hero newHero, Table playerTable) {
        playerTable.add(
                createLabel(
                        String.valueOf(newHero.getName()))
        ).colspan(2);
        playerTable.row();
    }

    private void addPortrait(Hero newHero, Table playerTable) {
        int portraitLength = Dimensions.instance().getSquarePortraitLength();
        playerTable.add(
                ImageManager.instance().getPortrait(
                        newHero.getName()
                ))
                .colspan(2)
                .width(portraitLength)
                .height(portraitLength);
        playerTable.row();
    }

    private ImageButton getImageButton(Icon icon, IconSize iconSize, boolean animation) {
        ImageButton button = ImageManager.instance().getIconButton(icon, iconSize);
        if(animation) {
            addFadeInAnimation(button);
        }
        return button;
    }

    private void addSquareImageButton(Table playerTable, ImageButton icon, int iconLength) {
        playerTable.add(icon)
                .width(iconLength)
                .height(iconLength);
    }

    private void addSquareIcon(Table playerTable, Image icon, int iconLength) {
        playerTable.add(icon)
                .width(iconLength)
                .height(iconLength);

    }

    private Image createIcon(Icon icon, IconSize iconSize, boolean animation) {
        Image image = ImageManager.instance().getIcon(
                icon,
                iconSize
        );
        if(animation) {
            addFadeInAnimation(image);
        }
        return image;
    }

    private Label createLabel(String value) {
        return createLabel(value, null, false);
    }

    private Label createLabel(int value, GuiColor color, boolean animation) {
        return createLabel(String.valueOf(value), color, animation);
    }

    private Label createLabel(String value, GuiColor color, boolean animation) {
        Label label = FontManager.instance().getLabel(
                value,
                FontManager.FontType.DEFAULT,
                ColorManager.instance().getColor(color)
        );
        if (animation){
            addFadeInAnimation(label);
        }
        return label;
    }
}

