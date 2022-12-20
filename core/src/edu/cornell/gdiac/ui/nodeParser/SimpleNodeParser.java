package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

public class SimpleNodeParser implements NodeParser{

    @Override
    public String getTypeKey() {
        return "Node";
    }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        Group node = new Group();
        if(parent!=null)
            node.setSize(parent.getWidth(), parent.getHeight());
        return node;
    }
}
