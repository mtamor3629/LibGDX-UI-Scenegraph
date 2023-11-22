package edu.cornell.gdiac.ui.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public abstract class TexturedNode extends Widget {
    protected Drawable drawable;
    private boolean _flipHorizontal, _flipVertical;

    public void setTexture (Texture t) { drawable = new TextureRegionDrawable(new TextureRegion(t)); }
    public void flipHorizontal(boolean flip) { _flipHorizontal = flip; }
    public void flipVertical(boolean flip) { _flipVertical = flip; }
}
