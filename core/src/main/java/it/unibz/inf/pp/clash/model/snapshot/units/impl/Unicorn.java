package it.unibz.inf.pp.clash.model.snapshot.units.impl;

import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

public class Unicorn extends AbstractMobileUnit implements MobileUnit {

    public Unicorn(UnitColor color) {
        super(3, color);
    }


    @Override
    public int getMaxTileMovement() {
        return 0;
    }
}
