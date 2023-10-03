package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ProgressBar2D extends ProgressBar {
    /*I wanted to homogenize with the functionality supported by CUGL progress bars. LibGDX
    doesn't have support for caps on the progress bar, so I had to make a new class to be
    able to draw the caps. The CUGL scenegraph tutorial implies that vertical progress bars
    are not allowed, so I set vertical to false. It also implies that there are no knobs on
    progress bars and that there is only one foreground texture (the one before the knob).*/
    private Drawable rCap, lCap;
    public ProgressBar2D (float min, float max, float stepSize, Skin skin){
        super(min, max, stepSize, false, skin);
    }
    public ProgressBar2D (float min, float max, float stepSize, Skin skin, String styleName){ super(min, max, stepSize, false, skin, styleName); }
    public ProgressBar2D (float min, float max, float stepSize, ProgressBarStyle style){ super(min, max, stepSize, false, style); }
    public void setCaps (Drawable rCap, Drawable lCap){
        this.rCap=rCap;
        this.lCap=lCap;
    }
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
        //This assumes that the centers of the caps align with the edges of the progress bar.
        //I intend to correct this to align with CUGL once I am able to confirm how they behave there.
        if (rCap != null) rCap.draw(batch, getX()+getWidth()/2f, getY(), rCap.getMinWidth(), getHeight());
        if (lCap != null) lCap.draw(batch, getX()-getWidth()/2f, getY(), lCap.getMinWidth(), getHeight());
    }
}
