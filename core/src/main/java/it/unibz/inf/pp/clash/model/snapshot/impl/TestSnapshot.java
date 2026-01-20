package it.unibz.inf.pp.clash.model.snapshot.impl;

import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.AbstractSnapshot;
import it.unibz.inf.pp.clash.model.snapshot.impl.TestBoardImpl;
import it.unibz.inf.pp.clash.model.snapshot.impl.HeroImpl;


import it.unibz.inf.pp.clash.model.snapshot.units.impl.Hare;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Tigre;
import it.unibz.inf.pp.clash.model.snapshot.units.impl.Wolfie;

import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.*;

public class TestSnapshot extends AbstractSnapshot implements Snapshot {

    public TestSnapshot(String firstHeroName, String secondHeroName) {
        super(
                new HeroImpl(firstHeroName, 20),
                new HeroImpl(secondHeroName, 20),
                TestBoardImpl.createEmptyBoard(11, 7),
                Player.FIRST,
                3,
                null
        );
//        this.ongoingMove = new TileCoordinates(6, 1);
        populateTiles();
    }


    private void populateTiles() {

        Wolfie wolf = new Wolfie();
        Hare rabbit = new Hare();
        Tigre tiger = new Tigre();

        //Player 2

        board.addUnit(5, 4, wolf);
        board.addUnit(5, 3, rabbit);
        board.addUnit(5, 5, tiger);

        //Player 1

        board.addUnit(6, 4, wolf);
        board.addUnit(6, 3, rabbit);
        board.addUnit(6, 5, tiger);

    }

    @Override
    public int getSizeOfReinforcement(Player player) {

        if (player == Player.FIRST) {
            return 3;
        }
        return 3;
    }
}
