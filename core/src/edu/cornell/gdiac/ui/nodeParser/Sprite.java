package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Sprite extends Image {
    //LibGDX's Scene2D doesn't have a sprite Actor, so I made one myself
    private TextureRegion[] frames;
    private int currentFrame;
    private TextureRegionDrawable trDrawable;

    private TextureRegion[] splitStrip (Texture filmstrip, int span, int cols){
        //code in this helper function is copied from my CS3152 project
        //get filmstrip
        TextureRegion[][] tempFrames = TextureRegion.split(filmstrip, filmstrip.getWidth()/cols, filmstrip.getHeight()/(int)(Math.ceil((double)span/(double)cols)));
        TextureRegion[] out = new TextureRegion[span];
        //order frames properly and place in array
        int index = 0;
        for (int i=0; i<tempFrames.length; i++) {
            for (int j = 0; j < tempFrames[0].length; j++) {
                out[index] = tempFrames[i][j];
                index++;
            }
        }
        return out;
    }

    public Sprite(Texture filmstrip, int span, int cols, int startFrame){
        super();
        /*Fix illegal inputs - will update this once I know how CUGL handles these.
        I based this on behavior listed in CUGL SceneGraph tutorial, but that could
        be just how the parser behaves and not the object itself*/
        if (cols < 1) cols = 1;
        if (span < 1) span = 1;
        if (cols > span) cols = span;
        startFrame = startFrame % span;
        if (startFrame < 0) startFrame += span;
        //set starting values
        frames = splitStrip(filmstrip, span, cols);
        currentFrame = startFrame;
        trDrawable = new TextureRegionDrawable(frames[currentFrame]);
        setDrawable(trDrawable);
        setSize(frames[currentFrame].getRegionWidth(), frames[currentFrame].getRegionHeight());
    }
    public Sprite(Texture filmstrip, int span, int startFrame){ this(filmstrip, span, span, startFrame); }
    public Sprite(Texture filmstrip, int startFrame){ this(filmstrip, 1, 1, startFrame); }

    public void updateFilmstrip(Texture filmstrip, int span, int cols, int startFrame){
        /*Fix illegal inputs - will update this once I know how CUGL handles these.
        I based this on behavior listed in CUGL SceneGraph tutorial, but that could
        be just how the parser behaves and not the object itself*/
        if (cols < 1) cols = 1;
        if (span < 1) span = 1;
        if (cols > span) cols = span;
        startFrame = startFrame % span;
        if (startFrame < 0) startFrame += span;
        //set values
        frames = splitStrip(filmstrip, span, cols);
        currentFrame = startFrame;
        trDrawable.setRegion(frames[currentFrame]);
        setSize(frames[currentFrame].getRegionWidth(), frames[currentFrame].getRegionHeight());
    }
    public void updateFilmstrip(Texture filmstrip, int span, int startFrame){ updateFilmstrip(filmstrip, span, span, startFrame); }
    public void updateFilmstrip(Texture filmstrip, int startFrame){ updateFilmstrip(filmstrip, 1, 1, startFrame); }

    public void setFrame(int frame){ currentFrame = frame; }

    public void draw(Batch batch, float parentAlpha){
        //make sure to set correct animation frame before drawing
        trDrawable.setRegion(frames[currentFrame]);
        super.setDrawable(trDrawable);
        super.draw(batch, parentAlpha);
    }
}
