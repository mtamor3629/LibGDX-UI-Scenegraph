package edu.cornell.gdiac.ui.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import edu.cornell.gdiac.math.PathExtruder;
import edu.cornell.gdiac.math.Poly2;

/**
 * A simple polygon Actor to be used along with the LibGDX Scene2D scenegraph
 * @author Miguel Amor
 * @date 12/14/2023
 */
public class PolygonNode extends TexturedNode{
    private float fringe;
    private Poly2 shape;
    private PolygonRegion region;
    private PathExtruder PE;

    /**
     * Initialize with an empty Texture and TextureRegion
     * @param verts vertices of this PolygonNode
     * @param indices indices from triangulation of this PolygonNode
     * @param fringe fringe width of this PolygonNode
     */
    public PolygonNode(float[] verts, short[] indices, float fringe){
        this.fringe = fringe;
        shape = new Poly2(verts, indices);
        region = shape.makePolyRegion(new TextureRegion());
        PE = new PathExtruder(verts, true);
    }

    /**
     * Initialize with the given Texture and a corresponding TextureRegion
     * @param verts vertices of this PolygonNode
     * @param indices indices from triangulation of this PolygonNode
     * @param fringe fringe width of this PolygonNode
     */
    public PolygonNode(Texture t, float[] verts, short[] indices, float fringe){
        this.fringe = fringe;
        shape = new Poly2(verts, indices);
        super.setTexture(t);
        region = shape.makePolyRegion(new TextureRegion(t));
        PE = new PathExtruder(verts, true);
    }

    @Override
    public void setTexture(Texture t){
        super.setTexture(t);
        region = shape.makePolyRegion(new TextureRegion(t));
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        TextureRegion tr = region.getRegion();
        drawable.draw(batch, tr.getRegionWidth(), tr.getRegionHeight(), tr.getRegionX(), tr.getRegionY());
        //if fringe width is below a small epsilon, don't waste time calculating/drawing it
        if(fringe <= 0.0001) return;
        //TODO: use path extruder to compute fringe and draw it. How can I make it fade out?
    }
}
