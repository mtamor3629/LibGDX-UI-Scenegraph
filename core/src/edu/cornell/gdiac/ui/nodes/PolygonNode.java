package edu.cornell.gdiac.ui.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import edu.cornell.gdiac.math.PathExtruder;
import edu.cornell.gdiac.math.Poly2;
import edu.cornell.gdiac.render.CUSpriteBatch;

/**
 * A simple polygon Actor to be used along with the LibGDX Scene2D scenegraph
 * Only usable with CUSpriteBatch
 * @author Miguel Amor
 * @date 12/14/2023
 */
public class PolygonNode extends TexturedNode{
    private float fringe;
    private Poly2 shape;

    /**
     * Initialize with an empty Texture and TextureRegion
     * @param verts vertices of this PolygonNode
     * @param indices indices from triangulation of this PolygonNode
     * @param fringe fringe width of this PolygonNode
     */
    public PolygonNode(float[] verts, short[] indices, float fringe){
        this.fringe = fringe;
        shape = new Poly2(verts, indices);
        //might be able to use this to draw a fringe
        // PE = new PathExtruder(verts, true);
    }

    /**
     * Initialize with the given Texture and a corresponding TextureRegion
     * @param t texture of this PolygonNode
     * @param verts vertices of this PolygonNode
     * @param indices indices from triangulation of this PolygonNode
     * @param fringe fringe width of this PolygonNode
     */
    public PolygonNode(Texture t, float[] verts, short[] indices, float fringe){
        this.fringe = fringe;
        shape = new Poly2(verts, indices);
        texture = t;
        setSize(shape.getBounds().width, shape.getBounds().height);
        //might be able to use this to draw a fringe
        // PE = new PathExtruder(verts, true);
    }

    @Override
    public void setTexture(Texture t){
        texture = t;
    }

    /**
     * Update the polygon that this node represents
     * @param verts vertices of this PolygonNode
     * @param indices indices from triangulation of this PolygonNode
     */
    public void setShape(float[] verts, short[] indices){
        shape = new Poly2(verts, indices);
        setSize(shape.getBounds().width*getScaleX(), shape.getBounds().height*getScaleY());
    }

    /**
     * Update the polygon that this node represents
     * @param newPoly the new polygon
     */
    public void setShape(Poly2 newPoly){
        shape = newPoly;
        setSize(shape.getBounds().width*getScaleX(), shape.getBounds().height*getScaleY());
    }

    /**
     * Draw this PolygonNode
     * @param batch Must be an instance of PolygonSpriteBatch
     * @param parentAlpha The parent alpha, to be multiplied with this actor's alpha, allowing the parent's alpha to affect all
     *           children.
     */
    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.setColor(getColor());
        ((CUSpriteBatch) batch).draw(texture, shape, getX(), getY(), getOriginX(), getOriginY(), getScaleX(), getScaleY(), getRotation());
        //if fringe width is below a small epsilon, don't waste time calculating/drawing it
        if(fringe <= 0.0001) return;
        //TODO: compute fringe and draw it.
    }
}
