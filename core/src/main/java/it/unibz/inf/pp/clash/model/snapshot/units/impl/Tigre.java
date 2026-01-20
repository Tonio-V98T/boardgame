package it.unibz.inf.pp.clash.model.snapshot.units.impl;

import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

public class Tigre implements MobileUnit {

    int health;
    UnitColor color;

    int attackCountdown;
    int attackDamage;

    public Tigre (){
        this.health = 4;
        this.color = UnitColor.ZERO;
        this.attackCountdown = 4;
        this.attackDamage = 3;
    }

    @Override
    public UnitColor getColor() {
        return this.color;
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
    public int getAttackDamage(){return this.attackDamage;}

    @Override
    public int getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }
}
