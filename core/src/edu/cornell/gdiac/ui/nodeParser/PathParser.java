package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.PathNode;
import edu.cornell.gdiac.ui.nodes.PolygonNode;

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
        float stroke = data.getFloat("stroke", /*what should the default stroke width be?*/1);
        String joint = data.getString("joint");
        String endcap = data.getString("endcap");
        float fringe = data.getFloat("fringe", 0);
        boolean stencil = data.getBoolean("stencil", false);
        float[] verts;
        int[] corners;
        boolean closed;

        Texture t = null;
        if (data.has("texture")) t = assetDirectory.getEntry(data.getString("texture"), Texture.class);

        if(path != null && path.isArray()) {
            //if path not empty and is array
            verts = path.asFloatArray();
            corners = new int[verts.length/2];
            for(int i = 0; i < corners.length; i++) corners[i] = i;
            closed = true;
        } else if (path != null) {
            //if path not empty and not array
            if(path.has("vertices")) verts = path.get("vertices").asFloatArray();
            else verts = new float[0];
            if(path.has("corners")) corners = path.get("corners").asIntArray();
            else {
                corners = new int[verts.length/2];
                for(int i = 0; i < corners.length; i++) corners[i] = i;
            }
            closed = path.getBoolean("closed", true);
        } else {
            //if path empty
            verts = new float[]{0, 0, t.getWidth(), 0, t.getWidth(), t.getHeight(), 0, t.getHeight()};
            corners = new int[verts.length/2];
            for(int i = 0; i < corners.length; i++) corners[i] = i;
            closed = true;
        }
        //PathNode constructor handles illegal joint/endcap strings, so we don't have to do so here
        PathNode node = t == null ? new PathNode(verts, corners, closed, stroke, joint, endcap, fringe, stencil)
                : new PathNode(t, verts, corners, closed, stroke, joint, endcap, fringe, stencil);

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
