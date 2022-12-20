package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.Scene2Loader;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

import javax.script.ScriptException;

public class TextButtonParser implements NodeParser{
    private Scene2Loader loader;

    public TextButtonParser(Scene2Loader loader) {
        this.loader = loader;
    }
    @Override
    public String getTypeKey() {
        return "TextButton";
    }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        JsonValue children = json.get("children");
        TextButton.TextButtonStyle tStyle = new ImageTextButton.ImageTextButtonStyle();
        Group node = new Group();
        Actor tUp = null;
        try {
            tUp = loader.parseNode(children.get(data.getString("upnode")),"", (Group)node ,scaleX,scaleY);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        children.remove(data.getString("upnode"));
        if (tUp instanceof Image) {
            Skin skin = new Skin();
            tStyle.up = ((Image) tUp).getDrawable();
            tStyle.down = skin.newDrawable(tStyle.up, 0.7f, 0.7f, 0.7f, 1);
            skin.dispose();
        }
        BitmapFont b = assetDirectory.getEntry("gyparody",BitmapFont.class);
        b.getData().setScale(scaleX,scaleY);
        tStyle.font = b;
        node = new TextButton(data.getString("text"),tStyle);
        return node;
    }
}
