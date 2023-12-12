package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

public class LabelParser implements NodeParser{
    @Override
    public String getTypeKey() {
        return "Label";
    }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        Label.LabelStyle lStyle = new Label.LabelStyle();
        if (data.has("font")) {
            BitmapFont b = assetDirectory.getEntry(data.getString("font"), BitmapFont.class);
            b.getData().setScale(scaleX, scaleY);
            lStyle.font = b;
        }
        JsonValue color = data.get("foreground");
        if (color != null)
            lStyle.fontColor = new Color(color.getInt(0), color.getInt(1), color.getInt(2), color.getInt(3));
        return new Label(data.getString("text"), lStyle);
    }
}
