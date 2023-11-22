package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

public class TextFieldParser implements NodeParser {
    @Override
    public String getTypeKey() { return "Textfield"; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle();
        tfStyle.font = assetDirectory.getEntry(data.getString("font"), BitmapFont.class);
        JsonValue color = data.get("foreground");
        if (color != null)
            tfStyle.fontColor = new Color(color.getInt(0), color.getInt(1), color.getInt(2), color.getInt(3));
        return new TextField(data.getString("text"), tfStyle);
    }
}
