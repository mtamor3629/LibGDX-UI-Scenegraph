package edu.cornell.gdiac.ui.nodes;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * A subclass of the LibGDX Scene2D ProgressBar actor with support for end caps and which always
 * sets the progress bar to be horizontal. To rotate it, rotate the root node.
 * @author Miguel Amor
 * @date 12/14/2023
 */
public class ProgressBarNode extends ProgressBar {
    /*I wanted to homogenize with the functionality supported by CUGL progress bars. LibGDX
    doesn't have support for caps on the progress bar, so I had to make a new class to be
    able to draw the caps. The CUGL scenegraph tutorial implies that vertical progress bars
    are not allowed, so I set vertical to false. It also implies that there are no knobs on
    progress bars and that there is only one foreground texture (the one before the knob).*/
    private TextureRegion rCap, lCap;
    public ProgressBarNode(float min, float max, float stepSize, Skin skin){
        super(min, max, stepSize, false, skin);
    }
    public ProgressBarNode(float min, float max, float stepSize, Skin skin, String styleName){ super(min, max, stepSize, false, skin, styleName); }
    public ProgressBarNode(float min, float max, float stepSize, ProgressBarStyle style){ super(min, max, stepSize, false, style); }
    public void setCaps (TextureRegion rCap, TextureRegion lCap){
        this.rCap=rCap;
        this.lCap=lCap;
    }
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
        //This assumes that the centers of the caps align with the edges of the progress bar.
        //I intend to correct this to align with CUGL once I am able to confirm how they behave there.
        //Will likely need to adjust this to accommodate rotation of the root node
        //TODO: why are the caps stretched?
        if (rCap != null)
            batch.draw(rCap, getX()+getWidth()-rCap.getRegionWidth()/2, getY(), rCap.getRegionWidth(),
                    rCap.getRegionHeight());
        if (lCap != null)
            batch.draw(lCap, getX()-lCap.getRegionWidth()/2, getY(), lCap.getRegionWidth(),
                    lCap.getRegionHeight());
    }
}
