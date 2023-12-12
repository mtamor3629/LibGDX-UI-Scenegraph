package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.TexturedNode;

public class TextFieldParser implements NodeParser {
    @Override
    public String getTypeKey() { return "Textfield"; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle();
        if (data.has("font")) {
            BitmapFont b = assetDirectory.getEntry(data.getString("font"), BitmapFont.class);
            b.getData().setScale(scaleX, scaleY);
            tfStyle.font = b;
        }

        int[] color, bgColor;
        if (data.has("foreground")) color = data.get("foreground").asIntArray();
        else color = new int[]{0, 0, 0, 255};
        if (color.length >= 4)
            tfStyle.fontColor = new Color(color[0]/255f, color[1]/255f, color[2]/255f, color[3]/255f);

        if (data.has("background")) bgColor = data.get("background").asIntArray();
        else bgColor = new int[]{0, 0, 0, 0};
        Pixmap p = new Pixmap(1,1, Pixmap.Format.RGB888);
        p.setColor(new Color(bgColor[0],bgColor[1],bgColor[2],bgColor[3]));
        p.fill();
        Texture t = new Texture(p);
        t.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        tfStyle.background = new TextureRegionDrawable(t);

        //TODO: handle padding, halign, valign, cursor
        // and use event listener
        TextField node = new TextField(data.getString("text", ""), tfStyle);
        return node;
    }
}
