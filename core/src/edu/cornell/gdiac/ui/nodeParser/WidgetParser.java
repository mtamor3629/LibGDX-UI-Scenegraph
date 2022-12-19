package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.Scene2Loader;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

import javax.script.ScriptException;

public class WidgetParser implements NodeParser{

    private Scene2Loader loader;

    public WidgetParser(Scene2Loader loader) {
        this.loader = loader;
    }
    @Override
    public String getTypeKey() {
        return "Widget";
    }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        String parentName = parent.getName();
        if(parentName == null) {
            parentName = "root";
        }
        try {
            return loader.parseNode(loader.widgetList.get(data.getString("key")).getJsonWithVar(data.get("variables")),parentName, (Group) parent,scaleX,scaleY);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}
