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
        if (data.has("font")) tfStyle.font = assetDirectory.getEntry(data.getString("font"), BitmapFont.class);
        int[] color = data.get("foreground").asIntArray();
        if (color.length >= 4)
            tfStyle.fontColor = new Color(color[0]/255f, color[1]/255f, color[2]/255f, color[3]/255f);
        //TODO: handle background, padding, halign, valign, cursor
        // and use event listener
        TextField node = new TextField(data.getString("text", ""), tfStyle);
        return node;
    }
}
