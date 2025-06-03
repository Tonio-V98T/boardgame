package it.unibz.inf.pp.clash.model.snapshot.units.impl;

import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

public class Chef implements MobileUnit {
    int getAttackCountdown;
    int health;
    UnitColor colour;
    private int attackCountdown;

    public Chef(){
        this.health = 10;
        this.colour = UnitColor.ONE;
        this.getAttackCountdown = 0;


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
    this.attackCountdown = attackCountdown;

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
