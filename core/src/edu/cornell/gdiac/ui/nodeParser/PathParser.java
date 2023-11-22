package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.PathNode;

/**
 * A simple path Actor to be used along with the LibGDX Scene2D scenegraph
 * @author Miguel Amor
 * @date 12/14/2023
 */
public class PathParser implements NodeParser{
    @Override
    public String getTypeKey() { return "Path"; }
    //rather than implementing from scratch, use Shapes from 3152 demo

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        JsonValue path = data.get("path");
        float stroke = data.getFloat("stroke");
        String joint = data.getString("joint");
        String endcap = data.getString("endcap");
        float fringe = data.getFloat("fringe");
        boolean stencil = data.getBoolean("stencil");
        Texture t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
        float[] verts;
        int[] corners;
        boolean closed;

        if(path.isArray()) {
            verts = path.asFloatArray();
            corners = new int[verts.length/2];
            for(int i = 0; i < corners.length; i++) corners[i] = i;
            closed = true;
        }
        else {
            if(path.has("vertices")) verts = path.get("vertices").asFloatArray();
            else verts = new float[0];
            if(path.has("corners")) corners = path.get("corners").asIntArray();
            else {
                corners = new int[verts.length/2];
                for(int i = 0; i < corners.length; i++) corners[i] = i;
            }
            closed = path.getBoolean("closed", true);
        }
        if (verts == null || verts.length == 0) verts = new float[]{0, 0, t.getWidth(), 0, t.getWidth(), t.getHeight(), 0, t.getHeight()};
        //PathNode constructor handles illegal joint/endcap strings, so we don't have to do so here
        PathNode node = new PathNode(verts, corners, closed, stroke, joint, endcap, fringe, stencil);

        //TexturedNode data
        String flip = data.getString("flip");
        if (flip.equals("horizontal")) node.setScaleX(-node.getScaleX());
        else if (flip.equals("vertical")) node.setScaleY(-node.getScaleY());
        else if (flip.equals("both")) {
            node.setScaleX(-node.getScaleX());
            node.setScaleY(-node.getScaleY());
        }
        //TODO: handle blending, gradients, and absolute coordinates
        return null;
    }
}
