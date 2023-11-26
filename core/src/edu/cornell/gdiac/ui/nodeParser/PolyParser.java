package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.math.PolyTriangulator;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.PolygonNode;

public class PolyParser implements NodeParser{
    //the earclipping triangulator from the Shapes demo
    private PolyTriangulator PT = new PolyTriangulator();
    private DelaunayTriangulator DT = new DelaunayTriangulator();

    @Override
    public String getTypeKey() {return "Poly";}

    /**
     * Parses the given json file and returns a corresponding PolygonNode.
     * Vertices should be specified counterclockwise. This parser DOES NOT
     * check the correctness of given lists of vertices/triangle indices.
     * This is left up to the json author.
     * @param json the json file with the current node as parent
     * @param assetDirectory the asset directory to load assets from
     * @param scaleX
     * @param scaleY
     * @param parent the parent of the node, used to carry over names, etc.
     * @return A new PolygonNode containing data parsed from the given json file
     */
    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        JsonValue poly = data.get("polygon");

        float sclX = 1.0f, sclY = 1.0f;
        if (data.has("scale")){
            if (data.get("scale").size < 2) sclX = sclY = data.getFloat("scale");
            else {
                JsonValue scl = data.get("scale");
                sclX = scl.getFloat(0);
                sclY = scl.getFloat(1);
            }
        }


        float[] verts = null;
        short[] indices = null;
        PolygonNode node;
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


        if (t != null) node = new PolygonNode(t, verts, indices, fringe);
        else /*TODO: use a blank texture*/ node = new PolygonNode(verts, indices, fringe);

        node.setScaleX(sclX);
        node.setScaleY(sclY);

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
