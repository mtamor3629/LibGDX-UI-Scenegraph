package edu.cornell.gdiac.ui.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Null;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathExtruder;
import edu.cornell.gdiac.math.PathFactory;
import edu.cornell.gdiac.math.Poly2;

/**
 * A simple wireframe Actor to be used along with the LibGDX Scene2D scenegraph
 * @author Miguel Amor
 * @date 12/14/2023
 */
public class WireNode extends TexturedNode {
    private Poly2 shape;
    private PolygonRegion region;
    private PathFactory PF = new PathFactory();
    //initialize the extruder with a dummy path
    private PathExtruder PE = new PathExtruder(new float[]{0,0},false);
    private Poly2.Traversal traversal;
    private Path2[] wireframe;
    private float stroke = 5f;
    private float fringe;

    /**
     * Initialize with the given Texture
     * @param t texture of this WireNode
     * @param verts vertices of the underlying polygon
     * @param indices indices from triangulation of the underlying polygon
     * @param wireframe Indices of the wireframe. If this is null, the provided traversal algorithm
     *                  will be used to compute the segments. Otherwise, this list of point indices
     *                  will be interpreted as the list of segments. Must be of even length.
     * @param traversal the traversal algorithm to use
     */
    public WireNode(Texture t, float[] verts, short[] indices, @Null short[] wireframe, String traversal, float fringe) {
        shape = new Poly2(verts, indices);
        texture = t;
        this.fringe = fringe;

        setTraversal(traversal);
        setWireframe(wireframe);
    }

    /**
     * Initialize with an empty Texture
     * @param verts vertices of the underlying polygon
     * @param indices indices from triangulation of the underlying polygon
     * @param wireframe Indices of the wireframe. If this is null, the provided traversal algorithm
     *                  will be used to compute the segments. Otherwise, this list of point indices
     *                  will be interpreted as the list of segments. Must be of even length.
     * @param traversal the traversal algorithm to use
     */
    public WireNode(float[] verts, short[] indices, @Null short[] wireframe, String traversal, float fringe) {
        shape = new Poly2(verts, indices);
        this.fringe = fringe;

        setTraversal(traversal);
        setWireframe(wireframe);
    }

    @Override
    public void setTexture(Texture t){
        texture = t;
        region = shape.makePolyRegion(new TextureRegion(t));
        //drawable = new TextureRegionDrawable(region.getRegion());
    }

    public void setShape(float[] verts, short[] indices){
        shape = new Poly2(verts, indices);
        this.wireframe = PF.makeTraversal(shape, this.traversal);
    }

    public void setShape(Poly2 newPoly){
        shape = newPoly;
        this.wireframe = PF.makeTraversal(shape, this.traversal);
    }

    /**
     * Update the traversal algorithm. DOES NOT compute a new wireframe to draw.
     * To compute the new wireframe, call {@code traverse()}
     * @param traversal a String representing the new traversal algorithm
     */
    public void setTraversal(String traversal){
        if (traversal.equals("none")) this.traversal = Poly2.Traversal.NONE;
        else if (traversal.equals("open")) this.traversal = Poly2.Traversal.OPEN;
        else if (traversal.equals("closed")) this.traversal = Poly2.Traversal.CLOSED;
            //default to interior if string is illegal
        else this.traversal = Poly2.Traversal.INTERIOR;
        this.wireframe = PF.makeTraversal(shape, this.traversal);
    }

    /**
     * Compute a new wireframe to draw using the traversal algorithm
     */
    public void traverse(){ this.wireframe = PF.makeTraversal(shape, traversal); }

    /**
     * Update the wireframe to draw
     * @param wireframe Indices of the wireframe. If this is null, the traversal algorithm will be
     *                  used to compute the segments. Otherwise, this list of point indices will be
     *                  interpreted as the list of segments. Must be of even length.
     */
    public void setWireframe(@Null short[] wireframe){
        if (wireframe == null) traverse();
        else {
            this.wireframe = new Path2[wireframe.length/2];
            // the Path2 constructor copies the input array, so we can reuse a temp array to save space
            float[] temp = new float[2];
            for(int i = 0; i < wireframe.length; i+=2){
                temp[0] = shape.vertices[wireframe[i]];
                temp[1] = shape.vertices[wireframe[i+1]];
                this.wireframe[i/2] = new Path2(temp);
            }
        }
    }

    /**
     * Update the stroke width of the wireframe
     * @param stroke
     */
    public void setStroke(float stroke){ this.stroke = stroke;}

    /**
     * Draw this WireNode
     * @param batch Must be an instance of PolygonSpriteBatch
     * @param parentAlpha The parent alpha, to be multiplied with this actor's alpha, allowing the parent's alpha to affect all
     *           children.
     */
    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.setColor(getColor());
        //TODO: changing origin seems not to do anything
        //TODO: make textures tile to fill a larger region
        for (Path2 segment : wireframe) {
            PE.set(segment);
            //TODO: what should the stroke width be? Also, recalculating every time might(?) be too slow
            PE.calculate(stroke);
            region = PE.getPolygon().makePolyRegion(new TextureRegion(texture));
            ((PolygonSpriteBatch) batch).draw(region, getX(), getY(), getOriginX(), getOriginY(),
                    region.getRegion().getRegionWidth(), region.getRegion().getRegionHeight(),
                    getScaleX(), getScaleY(), getRotation());
        }
    }
}
