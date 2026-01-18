package it.unibz.inf.pp.clash.view.singletons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit;
import it.unibz.inf.pp.clash.model.snapshot.units.Unit;
import it.unibz.inf.pp.clash.view.exceptions.UnknownUnitTypeException;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor.*;
import static com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import static it.unibz.inf.pp.clash.view.singletons.ImageManager.IconSize.*;
import static it.unibz.inf.pp.clash.view.singletons.ColorManager.GuiColor;
import static it.unibz.inf.pp.clash.view.singletons.ColorManager.GuiColor.*;
import static it.unibz.inf.pp.clash.view.singletons.Dimensions.Resolution;
import static it.unibz.inf.pp.clash.view.singletons.ImageManager.ColorStyle.*;
import static it.unibz.inf.pp.clash.model.snapshot.units.MobileUnit.UnitColor;

/**
 * Implements the singleton design pattern.
 * <p>
 * Image textures are loaded in memory in a lazy fashion (i.e. on demand).
 */
public class ImageManager {

    public enum Icon{HEART, COUNTDOWN, SWORDS, REINFORCEMENT, SKIP, EXIT, MOVE, ATTACK}

    public enum IconSize{SMALL, MEDIUM, LARGE}

    enum ColorStyle{DEFAULT, SELECTED}

    private static final ImageManager instance = new ImageManager();

    private ImageManager(){

        portraits = new HashMap<>();
        icons = new MultiKeyMap<>();
        backgrounds = new HashMap<>();
        pixmaps = new HashSet<>();
        mobileUnits = new MultiKeyMap<>();
        staticUnits = new MultiKeyMap<>();

        updateResolution(Dimensions.instance().getActiveResolution());
    }

    public static ImageManager instance() {
        return instance;
    }

    public void updateResolution(Resolution activeResolution) {
        dispose();
        String imageFolder = "images/png/"+ activeResolution.asString+"/";
        portraitFolder = imageFolder+"portraits/";
        iconFolder = imageFolder+"icons/";
        unitFolder = imageFolder+"units/";
    }

    public Table getColoredTable(GuiColor backgroundType) {
        Texture texture = getBackground(backgroundType);
        Table table = new Table();
        table.setBackground(new TextureRegionDrawable(texture));
        return table;
    }

    public ImageButton getEmptyCellButton(boolean inverted) {
        return getImageButton(
                getBackground(EMPTY_CELL),
                getBackground(SELECTED_CELL_BACKGROUND),
                inverted
        );
    }

    public Image getPortrait(String heroName){
        Texture texture = portraits.get(heroName);
        if(texture == null){
            texture = new Texture(Gdx.files.internal(portraitFolder + heroToFile.get(heroName)));
            portraits.put(
                    heroName,
                    texture
            );
        }
        return new Image(texture);
    }

    public Image getIcon(Icon icon, IconSize size){
        return getImage(
                icons,
                new MultiKey<>(
                        icon,
                        size
                ),
                iconFolder + sizeToSubfolder.get(size) + iconToFile.get(icon)
        );
    }

    public ImageButton getIconButton(Icon icon, IconSize size) {
        String pathPrefix = iconFolder + sizeToSubfolder.get(size);
        String filename = iconToFile.get(icon);

        return getImageButton(
                staticUnits,
                new MultiKey<>(icon, size, DEFAULT),
                pathPrefix+filename,
                new MultiKey<>(icon, size, SELECTED),
                pathPrefix+"inverted/"+filename,
                false
        );
    }

    public ImageButton getStaticUnitButton(Unit unit, boolean inverted) {
        String pathPrefix = unitFolder;
        Class<? extends Unit> type = unit.getClass();
        String filename = staticUnitToFile.get(type);

        return getImageButton(
                staticUnits,
                new MultiKey<>(type, DEFAULT),
                pathPrefix+filename,
                new MultiKey<>(type, SELECTED),
                pathPrefix+"inverted/"+filename,
                inverted
        );
    }

    public ImageButton getMobileUnitButton(Unit unit, UnitColor color, boolean inverted) throws UnknownUnitTypeException {
        String pathPrefix = unitFolder + colorToSubfolder.get(color);
        Class<? extends Unit> type = unit.getClass();
        String filename = mobileUnitToFile.get(type);
        if (filename == null) {
            throw new UnknownUnitTypeException(type);
        }

        return getImageButton(
                mobileUnits,
                new MultiKey<>(type, color, DEFAULT),
                pathPrefix+filename,
                new MultiKey<>(type, color, SELECTED),
                pathPrefix+"inverted/"+filename,
                inverted
        );
    }

    public void dispose(){
        clearTextures(portraits);
        clearTextures(icons);
        clearTextures(staticUnits);
        clearTextures(mobileUnits);
        clearTextures(backgrounds);
        pixmaps.forEach(Pixmap::dispose);
        pixmaps.clear();
    }

    private final Map<Icon, String> iconToFile = FileManager.instance().loadIconPropertyFile();
    private final Map<Class, String> mobileUnitToFile = FileManager.instance().loadMobileUnitPropertyFile();
    private final Map<Class, String> staticUnitToFile = FileManager.instance().loadStaticUnitPropertyFile();
    private final Map<String, String> heroToFile = FileManager.instance().loadPortraitPropertyFile();

    private String portraitFolder;
    private String iconFolder;
    private String unitFolder;

    private static final Map<IconSize, String> sizeToSubfolder = Map.of(
            SMALL, "small/",
            MEDIUM, "medium/",
            LARGE, "large/"
    );

    private static final Map<MobileUnit.UnitColor, String> colorToSubfolder = Map.of(
            ONE, "color_one/",
            TWO, "color_two/",
            THREE, "color_three/"
    );

    // Keeps track of all created pixmaps, for performance only (so that they can be freed).
    private final Set<Pixmap> pixmaps;

    private final Map<GuiColor, Texture> backgrounds;

    // Maps the name of a hero to its portrait.
    private final Map<String, Texture> portraits;

    /**
     * Stores the images that represent mobile units on the board.
     * <p>
     * Each key in the map is of type, <Class, Color, ColorStyle>, where:
     * <p>
     *      "Class" is the Java class for this type of unit (e.g. "Unicorn"),
     *      "Color" is the unit color,
     *      "ColorStyle" specifies whether the foreground or background is colored.
     */
    private final MultiKeyMap<Object, Texture> mobileUnits;

    /**
     * Similar to {@link ImageManager#mobileUnits}, but for static units.
     * <p>
     * Each key in the map is of type, <Class, ColorStyle>, where these two attributes have the same meaning as in
     * the keys of {@link ImageManager#mobileUnits}
     * <p>
     */
    private final MultiKeyMap<Object, Texture> staticUnits;

    /**
     * Similar to {@link ImageManager#mobileUnits}, but for icons.
     * <p>
     * Each key in the map is of type, <Class, IsonSize, ColorStyle>, where
     *      "IconSize" is either small, medium or large, and
     *      the two other attributes have the same meaning as in the keys of {@link ImageManager#mobileUnits}
     */
    private final MultiKeyMap<Object, Texture> icons;


    private Texture getBackground(GuiColor backgroundType) {
        Texture texture = backgrounds.get(backgroundType);
        if(texture == null){
            Pixmap pixmap = new Pixmap(1,1, Pixmap.Format.RGB565);
            pixmap.setColor(ColorManager.instance().getColor(backgroundType));
            pixmap.fill();
            pixmaps.add(pixmap);
            texture = new Texture(pixmap);
            backgrounds.put(
                    backgroundType,
                    texture
            );
        }
        return texture;
    }

    private Image getImage(MultiKeyMap<Object, Texture> map, MultiKey<Object> key, String path) {
                return new Image(
                        loadTexture(
                                map,
                                key,
                                path
                        ));
    }

    private ImageButton getImageButton(Texture defaultTexture, Texture alternativeTexture, boolean inverted){
        if(inverted){
            return getImageButton(
                    alternativeTexture,
                    defaultTexture,
                    false
            );
        }
        TextureRegionDrawable defaultDrawable = getDrawable(defaultTexture);
        TextureRegionDrawable alternativeTextureDrawable = getDrawable(alternativeTexture);

        ImageButton button = new ImageButton(defaultDrawable);
        ImageButtonStyle style = button.getStyle();

        style.up = defaultDrawable;
        style.over = alternativeTextureDrawable;
        style.imageOver = alternativeTextureDrawable;
        style.down = alternativeTextureDrawable;
        style.imageDown = alternativeTextureDrawable;
        return button;
    }

    private ImageButton getImageButton(MultiKeyMap<Object, Texture> map,
                                       MultiKey<Object> defaultImageKey,
                                       String defaultImagePath,
                                       MultiKey<Object> alternativeImageKey,
                                       String alternativeImagePath,
                                       boolean inverted
    ) {
        return getImageButton(
               loadTexture(
                        map,
                        defaultImageKey,
                        defaultImagePath
               ),
               loadTexture(
                       map,
                       alternativeImageKey,
                       alternativeImagePath
               ),
                inverted
        );
    }

    private TextureRegionDrawable getDrawable(Texture texture) {
        return new TextureRegionDrawable(
                new TextureRegion(texture)
        );
    }


    private Texture loadTexture(MultiKeyMap<Object, Texture> map, MultiKey<?> key, String path) {
        Texture texture = map.get(key);
        if(texture == null){
            texture =  new Texture(Gdx.files.internal(path));
            map.put(key, texture);
        }
        return texture;
    }

    private void clearTextures(Map<?, Texture> map) {
        map.forEach((key, value) -> value.dispose());
        map.clear();
    }


}
