package edu.cornell.gdiac.ui.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntSet;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathExtruder;
import edu.cornell.gdiac.math.Poly2;

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
    private PolygonRegion region;

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
    }

    @Override
    public void setTexture(Texture t){
        super.setTexture(t);
        region = extruded.makePolyRegion(new TextureRegion(t));
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
        TextureRegion tr = region.getRegion();
        drawable.draw(batch, tr.getRegionWidth(), tr.getRegionHeight(), tr.getRegionX(), tr.getRegionY());
        //if fringe width is below a small epsilon, don't waste time calculating/drawing it
        if(fringe <= 0.0001) return;
        //TODO: use path extruder to compute fringe and draw it. How can I make it fade out?
    }
}
