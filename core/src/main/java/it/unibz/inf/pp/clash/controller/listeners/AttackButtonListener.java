package it.unibz.inf.pp.clash.controller.listeners;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import it.unibz.inf.pp.clash.model.EventHandler;

public class AttackButtonListener extends ClickListener {
    private final EventHandler eventHandler;

    public AttackButtonListener(EventHandler eventHandler) {
        super(Input.Buttons.LEFT);
        this.eventHandler = eventHandler;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        System.out.println("Movement button clicked");
        eventHandler.attackSwitch();
    }
}
