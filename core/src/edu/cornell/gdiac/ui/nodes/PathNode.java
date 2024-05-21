package edu.cornell.gdiac.ui.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathExtruder;
import edu.cornell.gdiac.math.Poly2;
import edu.cornell.gdiac.render.CUSpriteBatch;

/**
 * A simple polygon Actor to be used along with the LibGDX Scene2D scenegraph.
 * Although fringe width and whether to stencil are required by the constructor,
 * the corresponding features are not yet implemented.
 * Only usable with CUSpriteBatch
 * @author Miguel Amor
 * @date 12/14/2023
 */
public class PathNode extends TexturedNode {
    // path elements
    private float[] verts;
    private int[] corners;
    private boolean closed;
    // PathNode elements
    private float stroke;
    private Poly2.Joint joint;
    private Poly2.EndCap endcap;
    private float fringe;
    private boolean stencil;

    private Path2 path;
    private PathExtruder PE;

    //what we need for drawing
    private Poly2 extruded;

    /**
     * Initialize with an empty Texture and TextureRegion
     * @param verts the vertices of this PathNode
     * @param corners the corners of the Path
     * @param closed whether this Path is closed
     * @param stroke the stroke width of this path
     * @param joint a string representing the joint type of this path
     * @param endcap a string representing the endcap type of this path
     * @param fringe the fringe width of this path
     * @param stencil whether to stencil this path
     */
    public PathNode(float[] verts, int[] corners, boolean closed, float stroke, String joint, String endcap, float fringe, boolean stencil){
        this.verts = verts;
        this.corners = corners;
        this.closed = closed;
        this.stroke = stroke;

        if (joint.equals("mitre")) this.joint = Poly2.Joint.MITRE;
        else if (joint.equals("round")) this.joint = Poly2.Joint.ROUND;
        //default to square if string is illegal
        //what's the difference between square and bevel joints (if any)?
        else this.joint = Poly2.Joint.SQUARE;

        if (endcap.equals("round")) this.endcap = Poly2.EndCap.ROUND;
        else if (endcap.equals("square")) this.endcap = Poly2.EndCap.SQUARE;
        //default to butt (no endcap) if string is illegal
        else this.endcap = Poly2.EndCap.BUTT;

        this.fringe = fringe;
        this.stencil = stencil;

        path = new Path2(verts);
        path.closed = closed;
        path.corners.addAll(corners);

        PE = new PathExtruder(path);
        PE.setEndCap(this.endcap);
        PE.setJoint(this.joint);
        PE.calculate(stroke);
        extruded = PE.getPolygon();
        setSize(extruded.getBounds().width, extruded.getBounds().height);
    }

    /**
     * Initialize with the given Texture and a corresponding TextureRegion
     * @param t the texture to fill this Path with
     * @param verts the vertices of this PathNode
     * @param corners the corners of the Path
     * @param closed whether this Path is closed
     * @param stroke the stroke width of this path
     * @param joint a string representing the joint type of this path
     * @param endcap a string representing the endcap type of this path
     * @param fringe the fringe width of this path
     * @param stencil whether to stencil this path
     */
    public PathNode(Texture t, float[] verts, int[] corners, boolean closed, float stroke, String joint, String endcap, float fringe, boolean stencil){
        this.verts = verts;
        this.corners = corners;
        this.closed = closed;
        this.stroke = stroke;

        if (joint.equals("mitre")) this.joint = Poly2.Joint.MITRE;
        else if (joint.equals("round")) this.joint = Poly2.Joint.ROUND;
        //default to square if string is illegal
        //what's the difference between square and bevel joints (if any)?
        else this.joint = Poly2.Joint.SQUARE;

        if (endcap.equals("round")) this.endcap = Poly2.EndCap.ROUND;
        else if (endcap.equals("square")) this.endcap = Poly2.EndCap.SQUARE;
        //default to butt (no endcap) if string is illegal
        else this.endcap = Poly2.EndCap.BUTT;

        this.fringe = fringe;
        this.stencil = stencil;

        path = new Path2(verts);
        path.closed = closed;
        path.corners.addAll(corners);

        PE = new PathExtruder(path);
        PE.setEndCap(this.endcap);
        PE.setJoint(this.joint);
        PE.calculate(stroke);
        extruded = PE.getPolygon();
        setSize(extruded.getBounds().width*getScaleX(), extruded.getBounds().height*getScaleY());
    }

    @Override
    public void setTexture(Texture t){
        super.setTexture(t);
    }

    /**
     * Update the path that this node represents
     * @param path the new path
     */
    public void setPath(Path2 path){
        this.path = path;
        PE.set(path);
        PE.calculate(stroke);
        extruded = PE.getPolygon();
        setSize(extruded.getBounds().width*getScaleX(), extruded.getBounds().height*getScaleY());
    }

    /**
     * Update the polygon that this node represents
     * @param verts the vertices of this PathNode
     * @param corners the corners of the Path
     * @param closed whether this Path is closed
     */
    public void setPath(float[] verts, int[] corners, boolean closed){
        this.verts = verts;
        this.corners = corners;
        this.closed = closed;

        path = new Path2(verts);
        path.closed = closed;
        path.corners.addAll(corners);

        PE.set(path);
        PE.calculate(stroke);
        extruded = PE.getPolygon();
        setSize(extruded.getBounds().width, extruded.getBounds().height);
    }

    /**
     * Update the stroke width of the path
     * @param stroke
     */
    public void setStroke(float stroke){
        this.stroke = stroke;
        PE.calculate(stroke);
        extruded = PE.getPolygon();
    }

    /**
     * Update the joint type
     * @param joint A String representing the new joint type. Should
     *              be one of "mitre", "round", or "square". Other values
     *              default to a square joint.
     */
    public void setJoint(String joint){
        if (joint.equals("mitre")) this.joint = Poly2.Joint.MITRE;
        else if (joint.equals("round")) this.joint = Poly2.Joint.ROUND;
        //default to square if string is illegal
        else this.joint = Poly2.Joint.SQUARE;
    }

    /**
     * Update the endcap type
     * @param endcap A String representing the new endcap type. Should
     *              be one of "butt", "round", or "square". Other values
     *              default to a butt endcap.
     */
    public void setEndcap(String endcap){
        if (endcap.equals("round")) this.endcap = Poly2.EndCap.ROUND;
        else if (endcap.equals("square")) this.endcap = Poly2.EndCap.SQUARE;
        //default to butt (no endcap) if string is illegal
        else this.endcap = Poly2.EndCap.BUTT;
    }

    /**
     * Draw this PathNode
     * @param batch Must be an instance of PolygonSpriteBatch
     * @param parentAlpha The parent alpha, to be multiplied with this actor's alpha, allowing the parent's alpha to affect all
     *           children.
     */
    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
        batch.setColor(getColor());
        ((CUSpriteBatch) batch).draw(texture, extruded, getX(), getY(), getOriginX(), getOriginY(), getScaleX(), getScaleY(), getRotation());
        //if fringe width is below a small epsilon, don't waste time calculating/drawing it
        if(fringe <= 0.0001) return;
        //TODO: compute fringe and draw it.
        //TODO: stenciling. should be possible when CUSpriteBatch is fixed
    }
}
