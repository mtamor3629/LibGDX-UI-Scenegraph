package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.TexturedNode;

public class NinePatchParser implements NodeParser{
    @Override
    public String getTypeKey() {
        return "NinePatch";
    }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        Texture t;
        if (data.has("texture")) t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
        else t = TexturedNode.defaultTexture();
        int leftB, botB, width, height;
        if (data.has("interior")){
            leftB = data.get("interior").getInt(0);
            botB = data.get("interior").getInt(1);
            width = data.get("interiot").getInt(2);
            height = data.get("interiot").getInt(3);
        } else {
            leftB = t.getWidth()/2;
            botB = t.getHeight()/2;
            width = height = 1;
        }
        NinePatch np = new NinePatch(t,leftB,botB,t.getWidth()-(leftB+width),t.getHeight()-(botB+height));
        np.scale(scaleX,scaleY);
        return new Image(new NinePatchDrawable(np));
    }
}
