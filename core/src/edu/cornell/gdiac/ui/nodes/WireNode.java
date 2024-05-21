package edu.cornell.gdiac.ui.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Null;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathExtruder;
import edu.cornell.gdiac.math.PathFactory;
import edu.cornell.gdiac.math.Poly2;
import edu.cornell.gdiac.render.CUSpriteBatch;

import java.util.Arrays;

/**
 * A simple wireframe Actor to be used along with the LibGDX Scene2D scenegraph
 * Only usable with CUSpriteBatch
 * @author Miguel Amor
 * @date 12/14/2023
 */
public class WireNode extends TexturedNode {
    private Poly2 shape;
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
        setSize(shape.getBounds().width, shape.getBounds().height);
        PE.setEndCap(Poly2.EndCap.BUTT);
        PE.setJoint(Poly2.Joint.SQUARE);
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
        PE.setEndCap(Poly2.EndCap.BUTT);
        PE.setJoint(Poly2.Joint.SQUARE);
    }

    @Override
    public void setTexture(Texture t){
        texture = t;
        //drawable = new TextureRegionDrawable(region.getRegion());
    }

    public void setShape(float[] verts, short[] indices){
        shape = new Poly2(verts, indices);
        this.wireframe = PF.makeTraversal(shape, this.traversal);
        setSize(shape.getBounds().width*getScaleX(), shape.getBounds().height*getScaleY());
    }

    public void setShape(Poly2 newPoly){
        shape = newPoly;
        this.wireframe = PF.makeTraversal(shape, this.traversal);
        setSize(shape.getBounds().width*getScaleX(), shape.getBounds().height*getScaleY());
    }

    /**
     * Update the traversal algorithm. DOES NOT compute a new wireframe to draw.
     * To compute the new wireframe, call {@code traverse()}
     * @param traversal A String representing the new traversal algorithm. Should
     *                  be one of "none", "open", "closed", or "interior". Other
     *                  values default to an interior traversal.
     */
    public void setTraversal(String traversal){
        if (traversal.equals("none")) this.traversal = Poly2.Traversal.NONE;
        else if (traversal.equals("open")) this.traversal = Poly2.Traversal.OPEN;
        else if (traversal.equals("closed")) this.traversal = Poly2.Traversal.CLOSED;
            //default to interior if string is illegal
        else this.traversal = Poly2.Traversal.INTERIOR;
        this.wireframe = PF.makeTraversal(shape, this.traversal);
        for (Path2 path : wireframe){

        }
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
            float[] temp = new float[4];
            for(int i = 0; i < wireframe.length; i+=2){
                temp[0] = shape.vertices[2*wireframe[i]];
                temp[1] = shape.vertices[2*wireframe[i]+1];
                temp[2] = shape.vertices[2*wireframe[i+1]];
                temp[3] = shape.vertices[2*wireframe[i+1]+1];
                this.wireframe[i/2] = new Path2(temp);
            }
        }
    }

    /**
     * Update the stroke width of the wireframe. Default value is 5
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
        for (Path2 segment : wireframe) {
            PE.set(segment);
            //TODO: recalculating every frame might be too slow for practical use
            PE.calculate(stroke);
            ((CUSpriteBatch) batch).draw(texture, PE.getPolygon(), getX(), getY(), getOriginX(), getOriginY(), getScaleX(), getScaleY(), getRotation());
        }
        //TODO: compute fringe and draw it.
    }
}
