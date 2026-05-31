package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Hare;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Tigre;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Wolfie;

import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.*;

public class TestSnapshot extends AbstractSnapshot implements Snapshot {

    public TestSnapshot(String firstHeroName, String secondHeroName) {
        super(
                new HeroImpl(firstHeroName, 4),
                new HeroImpl(secondHeroName, 4),
                TestBoardImpl.createEmptyBoard(9, 7),
                Player.FIRST,
                3,
                null
        );
//        this.ongoingMove = new TileCoordinates(6, 1);
        populateTiles();
    }


    private void populateTiles() {

        //Player 1 & 2 always begin with the same units

        // Player 2 (Color Three/Red)
        board.addUnitForPlayer(3, 4, new Wolfie(THREE), Player.SECOND);
        board.addUnitForPlayer(3, 3, new Hare(THREE), Player.SECOND);
        board.addUnitForPlayer(3, 5, new Tigre(THREE), Player.SECOND);

        //Player 1 (Color Zero/White)

        board.addUnitForPlayer(6, 4, new Wolfie(ZERO), Player.FIRST);
        board.addUnitForPlayer(6, 3, new Hare(ZERO), Player.FIRST);
        board.addUnitForPlayer(6, 5, new Tigre(ZERO), Player.FIRST);

    }

    @Override
    public int getSizeOfReinforcement(Player player) {
        return this.getNumberOfRemainingActions();   // fixed for both players, to make the game fair
    }
}
