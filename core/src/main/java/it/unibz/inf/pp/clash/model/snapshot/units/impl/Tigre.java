package it.unibz.inf.pp.clash.model.snapshot.units.impl;

import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;

public class Tigre implements MobileUnit {

    int health;
    UnitColor color;

    int attackCountdown;
    int attackDamage;

    int maxTileMovement;

    public Tigre (){
        this.health = 4;
        this.color = UnitColor.ZERO;
        this.attackCountdown = 3;
        this.attackDamage = 3;
        this.maxTileMovement = 1;
    }

    public Tigre (UnitColor color){
        this.health = 4;
        this.color = color;
        this.attackCountdown = 3;
        this.attackDamage = 3;
        this.maxTileMovement = 1;
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

    @Override
    public int getMaxTileMovement() {
        return this.maxTileMovement;
    }
}
