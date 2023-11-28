package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.math.PolyTriangulator;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.PolygonNode;
import edu.cornell.gdiac.ui.nodes.WireNode;

public class WireParser implements NodeParser{
    //the earclipping triangulator from the Shapes demo
    private PolyTriangulator PT = new PolyTriangulator();
    private DelaunayTriangulator DT = new DelaunayTriangulator();

    @Override
    public String getTypeKey() { return "Wire"; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        JsonValue poly = data.get("polygon");
        JsonValue wire = data.get("wireframe");
        String traversal = data.getString("traversal", "");

        short[] wireframe;
        if (wire != null) wireframe = wire.asShortArray();
        else wireframe = null;

        float[] verts = null;
        short[] indices = null;
        Texture t = null;

        if (data.has("texture")){
            t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
            //if polygon is missing, use a rectangle the size of the texture
            if (poly == null) verts = new float[]{0, 0, t.getWidth(), 0, t.getWidth(), t.getHeight(), 0, t.getHeight()};
        }

        if (poly != null && poly.isArray()) {
            //if polygon not empty and is array
            verts = poly.asFloatArray();
            PT.set(verts);
            PT.calculate();
            indices = PT.getTriangulation();
        } else if (poly != null) {
            //if polygon not empty and not array
            JsonValue v = poly.get("vertices");
            if (v != null) verts = v.asFloatArray();
            JsonValue i = poly.get("indices");
            if (i != null) indices = i.asShortArray();
            String triangulator = poly.getString("triangulator");
            if (verts != null && indices == null) {
                //if verts are null, we can't triangulate them. if indices are not null, we want to use them as-is.
                if (triangulator.equals("monotone")) indices = new short[0];//what to do in this case? seems like LibGDX doesn't support a monotone triangulator
                else if (triangulator.equals("delaunay")) indices = DT.computeTriangles(verts, false).toArray();
                else if (triangulator.equals("earclip")) {
                    PT.set(verts);
                    PT.calculate();
                    indices = PT.getTriangulation();
                }
                else /*undefined triangulator*/ indices = new short[0];
            }
        } else {
            //if polygon empty
            PT.set(verts);
            PT.calculate();
            indices = PT.getTriangulation();
        }
        //if vertices or indices are empty
        if (indices == null) indices = new short[0];
        if (verts == null) verts = new float[0];

        float fringe = 0;
        if (data.has("fringe") && data.get("fringe").isNumber()) fringe = data.getFloat("fringe");

        WireNode node;
        if (t != null) node = new WireNode(t, verts, indices, wireframe, traversal);
        else /*TODO: use a blank texture*/ node = new WireNode(verts, indices, wireframe, traversal);

        //TexturedNode data
        String flip = data.getString("flip", "");
        if (flip.equals("horizontal")) node.setScaleX(-node.getScaleX());
        else if (flip.equals("vertical")) node.setScaleY(-node.getScaleY());
        else if (flip.equals("both")) {
            node.setScaleX(-node.getScaleX());
            node.setScaleY(-node.getScaleY());
        }
        //TODO: handle blending, gradients, and absolute coordinates, fix flip
        return node;
    }
}
