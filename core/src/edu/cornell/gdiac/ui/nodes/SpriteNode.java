package edu.cornell.gdiac.ui.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import edu.cornell.gdiac.util.FilmStrip;

/** A subclass of the LibGDX Scene2D Image Actor that supports the use of animation filmstrips as textures.
*  This does not animate the sprite, it only stores the set of animation frames. Updating which animation
*  frame is displayed at any given time is left up to the programmer.
* @author Miguel Amor
* @date 12/14/2023
* */
public class SpriteNode extends Image {
    //LibGDX's Scene2D doesn't have a sprite Actor, so I made one myself
    private FilmStrip filmstrip;

    public SpriteNode(Texture filmstrip, int span, int cols, int startFrame){
        super();
        /*Fix illegal inputs - will update this once I know how CUGL handles these.
        I based this on behavior listed in CUGL SceneGraph tutorial, but that could
        be just how the parser behaves and not the object itself*/
        if (cols < 1) cols = 1;
        if (span < 1) span = 1;
        if (cols > span) cols = span;
        startFrame = startFrame % span;
        if (startFrame < 0) startFrame += span;

        this.filmstrip = new FilmStrip(filmstrip, span/cols, cols, span);
        this.filmstrip.setFrame(startFrame);
        setSize(this.filmstrip.getRegionWidth()/cols, this.filmstrip.getRegionHeight()/(int)(Math.ceil((double)span/(double)cols)));
    }
    public SpriteNode(Texture filmstrip, int span, int startFrame){ this(filmstrip, span, span, startFrame); }
    public SpriteNode(Texture filmstrip, int startFrame){ this(filmstrip, 1, 1, startFrame); }

    /**
     * Update this SpriteNode's filmstrip with a new one
     * @param filmstrip the filmstrip texture
     * @param span the number of frames in the filmstrip
     * @param cols the number of columns in the filmstrip texture
     * @param startFrame
     */
    public void updateFilmstrip(Texture filmstrip, int span, int cols, int startFrame){
        /*Fix illegal inputs - will update this once I know how CUGL handles these.
        I based this on behavior listed in CUGL SceneGraph tutorial, but that could
        be just how the parser behaves and not the object itself*/
        if (cols < 1) cols = 1;
        if (span < 1) span = 1;
        if (cols > span) cols = span;
        startFrame = startFrame % span;
        if (startFrame < 0) startFrame += span;

        this.filmstrip = new FilmStrip(filmstrip, span/cols, cols, span);
        this.filmstrip.setFrame(startFrame);
        setSize(this.filmstrip.getRegionWidth()/cols, this.filmstrip.getRegionHeight()/(int)(Math.ceil((double)span/(double)cols)));
    }
    public void updateFilmstrip(Texture filmstrip, int span, int startFrame){ updateFilmstrip(filmstrip, span, span, startFrame); }
    public void updateFilmstrip(Texture filmstrip, int startFrame){ updateFilmstrip(filmstrip, 1, 1, startFrame); }

    /**
     * Set the animation frame to draw. Out-of-bounds arguments raise an error.
     * @param frame which frame to draw
     */
    public void setFrame(int frame){ filmstrip.setFrame(frame); }

    public void draw(Batch batch, float parentAlpha){
        batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a*parentAlpha);
        batch.draw(filmstrip, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }
}
