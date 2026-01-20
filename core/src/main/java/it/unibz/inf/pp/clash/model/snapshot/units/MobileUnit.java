package it.unibz.inf.pp.clash.model.snapshot.units;

/**
 * Units that can attack and/or can be moved (as opposed to walls for instance).
 */
public interface MobileUnit extends Unit {

    enum UnitColor {ONE, TWO, THREE, ZERO}

    /**
     * @return the unit's color (two units of the same type may have different colors)
     */
    UnitColor getColor();

    /**
     * @return number of turns before this unit attacks; returns a value < 0 if no attack is scheduled for this unit.
     */
    int getAttackCountdown();

    /**
     * Sets the number of turns before this unit attacks; a value < 0 means that no attack is
     * scheduled for this unit.
     */
    void setAttackCountdown(int attackCountDown);

    /**
     * @return The damage that the unit can inflict to an enemy unit or player
     */
    int getAttackDamage();
}
