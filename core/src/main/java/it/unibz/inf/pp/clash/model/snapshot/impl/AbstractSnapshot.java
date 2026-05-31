package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.snapshot.Board;
import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;

import java.util.Optional;

import static it.unibz.inf.pp.clash.model.snapshot.Board.TileCoordinates;

public abstract class AbstractSnapshot implements Snapshot {

    protected final Board board;
    private final Hero firstHero;
    private final Hero secondHero;

    private Player activeplayer;
    private int actionsRemaining;
    protected TileCoordinates ongoingMove;

    protected AbstractSnapshot(Hero firstHero, Hero secondHero, Board board, Player activeplayer, int actionsRemaining,
                            TileCoordinates ongoingMove) {
        this.board = board;
        this.firstHero = firstHero;
        this.secondHero = secondHero;
        this.activeplayer = activeplayer;
        this.actionsRemaining = actionsRemaining;
        this.ongoingMove = ongoingMove;
    }

    public Board getBoard() {
        return board;
    }

    @Override
    public Hero getHero(Player player) {
        return switch (player) {
            case FIRST -> firstHero;
            case SECOND -> secondHero;
        };
    }

    @Override
    public Player getActivePlayer() {
        return activeplayer;
    }

    @Override
    public Optional<TileCoordinates> getOngoingMove() {
        return Optional.ofNullable(ongoingMove);
    }

    @Override
    public int getNumberOfRemainingActions() {
        return actionsRemaining;
    }

    public void setNumberOfRemainingActions(int newRemainingActions) {
        this.actionsRemaining = newRemainingActions;
    }

    public void setActivePlayer(Player player){
        this.activeplayer = player;
    }

    public Hero getOpponentHero(Player player) {
        return switch (player) {
            case FIRST -> secondHero;
            case SECOND -> firstHero;
        };
    }

    /* ************************************
                Deprecated
       ************************************ */

    public Player getOpponentPlayer(boolean isP1){
        if (isP1) {return Player.SECOND;}
        else return Player.FIRST;
    }

    public void setOngoingMove(int rowIndex, int columnIndex){
        this.ongoingMove = new TileCoordinates(rowIndex, columnIndex);
    }
}
