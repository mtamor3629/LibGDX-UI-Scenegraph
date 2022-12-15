package edu.cornell.gdiac.ui.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import javax.swing.*;

public class Node2dParser implements AssetParser<Group> {
    private JsonValue root;

    @Override
    public Class<Group> getType() {
        return Group.class;
    }

    /**
     * Resets the parser iterator for the given directory.
     *
     * The value directory is assumed to be the root of a larger JSON structure.
     * The individual assets are defined by subtrees in this structure.
     *
     * @param directory    The JSON representation of the asset directory
     */
    public void reset(JsonValue directory) {
        root = directory;
        root = root.getChild("scene2d" );
    }

    /**
     * Returns true if there are still assets left to generate
     */
    public boolean hasNext() {
        return root.get("children") != null;
    }
    @Override
    public void processNext(AssetManager manager, ObjectMap<String, String> keymap) {
        String file = root.asString();
        keymap.put(root.name(),file);
        manager.load( file, Group.class, null );
        root = root.next();
    }
}
