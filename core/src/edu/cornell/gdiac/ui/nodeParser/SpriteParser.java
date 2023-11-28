package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.Sprite;

public class SpriteParser implements NodeParser{
    @Override
    public String getTypeKey() { return "Sprite"; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        Texture t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
        int span = data.getInt("span", 1);
        int cols = data.getInt("cols", span);
        int frame = data.getInt("frame", 0);

        Sprite node = new Sprite(t, span, cols, frame);
        node.setSize(t.getWidth()/cols,t.getHeight()*cols/span);

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
