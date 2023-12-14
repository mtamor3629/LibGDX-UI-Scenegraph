package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.SpriteNode;
import edu.cornell.gdiac.ui.nodes.TexturedNode;

public class SpriteParser implements NodeParser{
    @Override
    public String getTypeKey() { return "Sprite"; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");

        Texture t;
        if (data.has("texture")) t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
        else t = TexturedNode.defaultTexture();

        int span = data.getInt("span", 1);
        int cols = data.getInt("cols", span);
        int frame = data.getInt("frame", 0);

        SpriteNode node = new SpriteNode(t, span, cols, frame);
        node.setSize(t.getWidth()/cols,t.getHeight()*cols/span);

//        //TexturedNode data
//        String flip = data.getString("flip", "");
//        if (flip.equals("horizontal")) node.setWidth(-node.getWidth());
//        else if (flip.equals("vertical")) node.setHeight(-node.getHeight());
//        else if (flip.equals("both")) {
//            node.setWidth(-node.getWidth());
//            node.setHeight(-node.getHeight());
//        }
        //TODO: handle blending, gradients, and absolute coordinates, fix flip
        return node;
    }
}
