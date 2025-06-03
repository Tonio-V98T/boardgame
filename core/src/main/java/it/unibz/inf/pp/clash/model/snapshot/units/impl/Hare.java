package it.unibz.inf.pp.clash.model.snapshot.units.impl;

import it.unibz.inf.pp.clash.model.snapshot.Hero;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

public class Hare implements MobileUnit {


    int health;
    UnitColor colour;
    private int attackCountdown;

    public Hare() {
        this.health = 5;
        this.colour = UnitColor.ONE;
        this.attackCountdown = 0;

    }

    @Override
    public UnitColor getColor() {
        return this.colour;
    }

    @Override
    public int getAttackCountdown() {
        return this.attackCountdown;
    }

    @Override
    public void setAttackCountdown(int attackCountDown) {
        this.attackCountdown = attackCountDown;
    }

    @Override
    public int getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }


}
