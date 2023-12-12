package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.Scene2Loader;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

import javax.script.ScriptException;

public class ScrollPaneParser implements NodeParser{
    Scene2Loader loader;
    public ScrollPaneParser(Scene2Loader loader) {
        this.loader = loader;
    }
    @Override
    public String getTypeKey() { return "Scroll"; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        float[] interior = null, pan = null;
        if (data.has("interior")) interior = data.get("interior").asFloatArray();
        if (data.has("pan")) pan = data.get("pan").asFloatArray();
        boolean constrain = data.getBoolean("constrain", false);
        boolean mask = data.getBoolean("mask", false);
        float spin = data.getFloat("spin", 0f);
        float zoom = data.getFloat("zoom", 1f);
        float zoomMax = data.getFloat("zoom max", 1f);
        float zoomMin = data.getFloat("zoom min", 1f);

        JsonValue scale = data.get("scale");
        float sclX = 1, sclY = 1;
        if (scale != null) {
            if (scale.size < 2) {
                sclY = scale.asFloat();
                sclX = scale.asFloat();
            }
            else{
                sclX = scale.getFloat(0);
                sclY = scale.getFloat(1);
            }
        }

        ScrollPane node = new ScrollPane(null);
        float x = 0, y = 0, w = 0, h = 0;
        if (interior != null){
            if (interior.length > 2){
                x = interior[0];
                y = interior[1];
                w = interior[2];
                h = interior[3];
            } else {
                w = interior[0];
                h = interior[1];
            }
        }

        Group content = new Group();
        //TODO: what size is good?
        content.setSize(4000, 4000);
        content.setPosition(x, y);
        content.setOrigin(w*0.5f, h*0.5f);
        JsonValue children = json.get("children");
        for (int i = 0; i < children.size; i++){
            try {
                loader.parseNode(children.get(i), "", content, scaleX*sclX, scaleY*sclY);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        }

        node.setWidget(content);
        content.rotateBy(spin);
        content.setScale(zoom);
        node.setSize(w, h);
        if (pan != null) node.scrollTo(pan[0], pan[1], w, h);
        return node;
    }
}
