package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

public class NinePatchParser implements NodeParser{
    @Override
    public String getTypeKey() {
        return "NinePatch";
    }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        int leftB = data.get("interior").getInt(0);
        int botB = data.get("interior").getInt(1);
        NinePatch np = new NinePatch(assetDirectory.getEntry(data.getString("texture"), Texture.class),leftB,botB,leftB,botB);
        np.scale(scaleX,scaleY);
        return new Image(new NinePatchDrawable(np));
    }
}
