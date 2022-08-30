/*
 * UI.java
 *
 * This is the class that processes .json format ui specifications to generate the scene graphs in LibGdx
 *
 * @author Barry Lyu
 * @date   8/30/22
 */

package edu.cornell.gdiac.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.Viewport;

public class UI {
    public static Stage genSceneGraph(JsonValue json){
        Stage stage = new Stage();
        //TODO: Parse the skin
        Skin skin = new Skin();
        //Tree tree = new Tree(skin);
        Image img = new Image(new Texture(Gdx.files.internal("badlogic.jpg")));
        //img.setOrigin(435f,135f);
        img.setPosition(1f,1f);
        //img.setPosition(0,0);
        img.setRotation(30);
        //stage.addActor(tree);
        //tree.addActor(img);
        stage.addActor(img);
/*
        JsonValue sceneGraph = json.get("scene2s");
        if(sceneGraph==null||sceneGraph.isEmpty())
            throw new IllegalArgumentException("corrupted json file, does not contain scene2s specs");
        for(JsonValue actor:sceneGraph){

        }
        throw new Error("Unimplemented");

 */
        return stage;
    }
}
