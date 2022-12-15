/*
 * Scene2dLoader.java
 *
 * This is a loader for processing json scene2d declarations.
 *
 * @author Barry Lyu
 * @data   12/7/2022
 */
package edu.cornell.gdiac.ui.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;


/**
 * This class is an {@link AssetLoader} to load scene-graphs as {@link Group} assets.
 *
 * A scene-graph should be a json object with the name "scene2d" abd a list of nodes.
 */
public class Scene2dLoader extends SynchronousAssetLoader<Group,Scene2dLoader.Scene2dParameters> {

    private Group cachedGroup;
    public static class Scene2dParameters extends AssetLoaderParameters<Group> {
        public int stageX;
        public int stageY;
    }


    public Scene2dLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Group load(AssetManager assetManager, String s, FileHandle fileHandle, Scene2dParameters scene2dParameters) {
        JsonReader reader = new JsonReader();
        JsonValue json = reader.parse(fileHandle);
        Group stage = new Group();
        if(json.has("width") && json.has("height")){
            stage.setWidth(json.getInt("width"));
            stage.setHeight(json.getInt("height"));
        }
        JsonValue sceneGraph = json.get("scene2d");
        stage.setName("root");
        if (sceneGraph == null || sceneGraph.isEmpty())
            throw new IllegalArgumentException("corrupted json file, does not contain scene2s specs");
        //iterate through all the nodes
        for (JsonValue actor : sceneGraph) {
            assetManager.load(actor.getString("name"), Actor.class, new Node2dLoader.Node2dParameters());
        }
        cachedGroup = stage;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String s, FileHandle fileHandle, Scene2dParameters scene2dParameters) {
        return null;
    }

    public Actor parseNode(JsonValue actor){
        return null;
    }


}
