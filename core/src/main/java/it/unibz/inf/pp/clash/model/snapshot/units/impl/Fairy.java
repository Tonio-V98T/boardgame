package it.unibz.inf.pp.clash.model.snapshot.units.impl;

import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

public class Fairy extends AbstractMobileUnit implements MobileUnit {

    public Fairy(UnitColor color) {
        super(2,  color);
    }

    @Override
    public int getMaxTileMovement() {
        return 0;
    }
}
