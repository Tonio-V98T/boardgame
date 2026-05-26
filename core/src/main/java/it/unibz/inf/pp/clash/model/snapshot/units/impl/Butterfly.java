package it.unibz.inf.pp.clash.model.snapshot.units.impl;

import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

public class Butterfly extends AbstractMobileUnit implements MobileUnit {

    public Butterfly(MobileUnit.UnitColor color) {
        super(5, color);
    }

    @Override
    public int getMaxTileMovement() {
        return 0;
    }
}
