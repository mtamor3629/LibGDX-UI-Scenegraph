package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.Scene2Loader;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

import javax.script.ScriptException;

public class ButtonParser implements NodeParser{
    Scene2Loader loader;

    public ButtonParser(Scene2Loader loader) {
        this.loader = loader;
    }
    @Override
    public String getTypeKey() {
        return "Button";
    }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        Button.ButtonStyle bStyle = new Button.ButtonStyle();
        JsonValue children = json.get("children");
        JsonValue data = json.get("data");
        Actor upnode = null;
        Group node = new Group();
        try {
            upnode = loader.parseNode(children.get(data.getString("upnode")),"", node,scaleX,scaleY);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        children.remove(data.getString("upnode"));
        if (upnode instanceof Image) {
            Skin skin = new Skin();
            bStyle.up = ((Image) upnode).getDrawable();
            bStyle.down = skin.newDrawable(bStyle.up, 0.7f, 0.7f, 0.7f, 1);
            skin.dispose();
            node = new Button(bStyle);
        }
        return node;
    }
}
