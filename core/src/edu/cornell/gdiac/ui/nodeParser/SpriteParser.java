package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

public class SpriteParser implements NodeParser{
    @Override
    public String getTypeKey() { return "Sprite"; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        Texture t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
        int span = data.getInt("span", 1);
        int cols = data.getInt("cols", span);
        //will update this once I confirm what the start frame defaults to in CUGL,
        //but I think that 0 is a safe assumption
        int frame = data.getInt("frame", 0);
        return new Sprite(t, span, cols, frame);
    }
}
