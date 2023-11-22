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

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        JsonValue poly = data.get("polygon");
        float[] verts;
        short[] indices;
        if (poly.isArray()) {
            verts = poly.asFloatArray();
            PT.set(verts);
            indices = PT.getTriangulation();
        }
        else {
            verts = poly.get("vertices").asFloatArray();
            indices = poly.get("indices").asShortArray();
            String triangulator = poly.getString("triangulator");
            if (indices == null) {
                if (triangulator.equals("monotone")) indices = new short[0];//what to do in this case? seems like LibGDX doesn't support a monotone triangulator
                else if (triangulator.equals("delaunay")) indices = DT.computeTriangles(verts, false).toArray();
                else if (triangulator.equals("earclip")) {
                    PT.set(verts);
                    indices = PT.getTriangulation();
                }
                else indices = new short[0];
            }
        }
        float fringe = 0;
        if (data.has("fringe") && data.get("fringe").isNumber()) fringe = data.getFloat("fringe");
        Texture t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
        //if vertices are empty, use a rectangle the size of the texture
        if (verts == null || verts.length == 0) verts = new float[]{0, 0, t.getWidth(), 0, t.getWidth(), t.getHeight(), 0, t.getHeight()};
        PolygonNode node = new PolygonNode(t, verts, indices, fringe);

        //TexturedNode data
        String flip = data.getString("flip");
        if (flip.equals("horizontal")) node.setScaleX(-node.getScaleX());
        else if (flip.equals("vertical")) node.setScaleY(-node.getScaleY());
        else if (flip.equals("both")) {
            node.setScaleX(-node.getScaleX());
            node.setScaleY(-node.getScaleY());
        }
        //TODO: handle blending, gradients, and absolute coordinates
        return node;
    }
}
