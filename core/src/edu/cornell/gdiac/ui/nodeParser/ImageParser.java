package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.TexturedNode;

public class ImageParser implements NodeParser{

    @Override
    public String getTypeKey() {
        return "Image";
    }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");

        Texture t;
        if (data.has("texture")) t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
        else t = TexturedNode.defaultTexture();

        Actor node = new Image(t);
        node.setSize(t.getWidth(),t.getHeight());
        //how do Images differ from normal PolygonNodes?

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
