package edu.cornell.gdiac.ui.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public abstract class TexturedNode extends Widget {
    protected Texture texture;
    protected Drawable drawable;
    protected boolean _flipHorizontal, _flipVertical;
    public void setTexture (Texture t) {
        texture = t;
        drawable = new TextureRegionDrawable(new TextureRegion(t));
    }
    public void flipHorizontal(boolean flip) {
        _flipHorizontal = flip;
        float scl = flip ? -1*Math.abs(getScaleX()) : Math.abs(getScaleX());
        setScaleX(scl);
    }
    public void flipVertical(boolean flip) {
        _flipVertical = flip;
        float scl = flip ? -1*Math.abs(getScaleY()) : Math.abs(getScaleY());
        setScaleY(scl);
    }
}
