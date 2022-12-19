package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

public class ImageParser implements NodeParser{

    @Override
    public String getTypeKey() {
        return "Image";
    }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        Texture t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
        Actor node = new Image(t);
        node.setSize(t.getWidth(),t.getHeight());
        return node;
    }
}
