package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

/**
 * This interface parses an asset of type {@code Actor}
 */
public interface NodeParser {
    /**
     * Returns the name of this node type
     * This is used to find the corresponding NodeParser for a node type
     */
    public String getTypeKey();

    /**
     * Returns the established Actor Node from the json file
     * @param json the json file with the current node as parent
     * @param assetDirectory the asset directory to load assets from
     */
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent);
}