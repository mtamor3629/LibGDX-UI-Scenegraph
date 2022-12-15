package edu.cornell.gdiac.ui.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

public class Node2dLoader extends SynchronousAssetLoader<Group,Node2dLoader.Node2dParameters> {

    public Node2dLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Group load(AssetManager assetManager, String s, FileHandle fileHandle, Node2dParameters node2dParameters) {
        return null;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String s, FileHandle fileHandle, Node2dParameters node2dParameters) {
        return null;
    }

    public class Node2dParameters extends AssetLoaderParameters<Group> {

        public String parentName;
        public Group parent;
        public float scaleX;
        public float scaleY;
        public JsonValue actor;
        public String layoutType;
        public String nodeType;
        public String name;


        public float angle;
        public float x;
        public float y;
        public float width;
        public float height;
        public float anchorX;
        public float anchorY;

        public JsonValue children;
    }

}
