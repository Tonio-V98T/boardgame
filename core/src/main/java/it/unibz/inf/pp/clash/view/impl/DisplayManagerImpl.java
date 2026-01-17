package it.unibz.inf.pp.clash.view.impl;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import it.unibz.inf.pp.clash.model.EventHandler;
import it.unibz.inf.pp.clash.model.snapshot.Snapshot;
import it.unibz.inf.pp.clash.view.DisplayManager;
import it.unibz.inf.pp.clash.view.exceptions.NoGameOnScreenException;
import it.unibz.inf.pp.clash.view.screen.game.GameScreen;
import it.unibz.inf.pp.clash.view.screen.home.HomeScreen;
import it.unibz.inf.pp.clash.view.singletons.FileManager;
import it.unibz.inf.pp.clash.view.singletons.FontManager;
import it.unibz.inf.pp.clash.view.singletons.ImageManager;
import it.unibz.inf.pp.clash.view.singletons.SkinManager;

import java.util.Optional;
import java.util.Random;

public class DisplayManagerImpl extends Game implements DisplayManager {


    private final boolean debug;
    private final float animationDuration;
    private EventHandler eventHandler;
    private HomeScreen homeScreen;
    private GameScreen gameScreen;


    /**
     * @param debug if true, then the compositor will draw in debug mode.
     *              In particular, the borders of all cells in tables will be drawn on screen.
     *              Should be set to false when drawing the actual graphical user interface.
     */
    public DisplayManagerImpl(boolean debug) {
        this.animationDuration = FileManager.instance().parseAnimationDuration(1);
        this.debug = debug;
    }

    public DisplayManagerImpl() {
        this(false);
    }

    @Override
    public void create() {
        drawHomeScreen();
    }

    @Override
    public void dispose() {
        Optional.ofNullable(gameScreen).ifPresent(GameScreen::dispose);
        Optional.ofNullable(homeScreen).ifPresent(HomeScreen::dispose);
        SkinManager.instance().dispose();
        ImageManager.instance().dispose();
        FontManager.instance().dispose();
    }


    private String motivationMessage(){
        String [] motivationalMessages = {
                "You are a loser",
                "You can win now",
                "Really? this is the best move ever",
                "Daje Daje"
        };
        int rnd = new Random().nextInt(motivationalMessages.length);
        return motivationalMessages[rnd] + "\n";
    }

    @Override
    public void updateMessage(String message) throws NoGameOnScreenException {
        if(gameScreen == null || getScreen() != gameScreen){
            throw new NoGameOnScreenException();
        }
        message = this.motivationMessage() + message;
        gameScreen.updateMessage(message);
    }

    @Override
    public void drawSnapshot(Snapshot snapshot, String message) {
        if (gameScreen == null) {
            gameScreen = new GameScreen(
                    eventHandler,
                    animationDuration,
                    debug
            );
            setScreen(gameScreen);
        }
        message = this.motivationMessage() + message;
        gameScreen.drawSnapshot(snapshot, message);
    }

    @Override
    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void drawHomeScreen(){
        if(homeScreen == null) {
            homeScreen = new HomeScreen(this, eventHandler, debug);
        }
        if(gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }

        setScreen(homeScreen);
        homeScreen.render(Gdx.graphics.getDeltaTime());
    }
}
