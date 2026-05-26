package it.unibz.inf.pp.clash.view.screen.endGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.view.screen.AbstractScreen;
import it.unibz.inf.pp.clash.view.screen.sync.AnimationCounter;

public class EndGameScreen extends AbstractScreen implements Screen {

    // currently, newsnap = sameSnap; needed for the player compositor
    public EndGameScreen(EventHandler eventHandler, AnimationCounter animationCounter,
                         boolean debug,
                         Snapshot previousSnapshot, Snapshot newSnapshot,
                         String victoryMessage) {

        super(debug);

        // disables automatic calls to the render() method (30-50-80 times per second)
        Gdx.graphics.setContinuousRendering(false);

        EndGameScreenCompositor compositor = new EndGameScreenCompositor(eventHandler,
                                                    animationCounter, debug);
        stage.addActor(
                compositor.drawEndGameScreen(previousSnapshot, newSnapshot, victoryMessage)
        );
    }
}
