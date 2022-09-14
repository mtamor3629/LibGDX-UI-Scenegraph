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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.assets.SoundLoader;
import jdk.internal.net.http.frame.GoAwayFrame;

import javax.swing.text.Style;

/**
 * This class provides functionality for parsing a JSON scenegraph specification, and generating the corresponding LibGDX scenegraph
 */
public class Scene2Loader {
    /**
     * Takes in a {@link JsonValue} to produce a scene graph of ui elements
     */

    static AssetDirectory assetDirectory;

    public static Group genSceneGraph(JsonValue json, AssetDirectory assets) {
        assetDirectory = assets;
        Group stage = new Group();
        //TODO: Parse the skin etc
        Skin skin = new Skin();
        //Image img = new Image(new Texture(Gdx.files.internal("badlogic.jpg")));
        //img.setPosition(1f, 1f);
        //img.setRotation(30);
        //stage.addActor(img);
        JsonValue sceneGraph = json.get("scene2s");
        if (sceneGraph == null || sceneGraph.isEmpty())
            throw new IllegalArgumentException("corrupted json file, does not contain scene2s specs");
        //iterate through all the nodes
        for (JsonValue actor : sceneGraph) {
            stage.addActor(parseNode(actor, stage));
        }
        return stage;
    }

    /**
     * Parse a node of the scenegraph, inherit properties from its parent.
     *
     * @param actor  the JsonValue to be parsed
     * @param parent the parent node of this graph, or null if the node is the root;
     */

    private static Actor parseNode(JsonValue actor, Group parent) {
        Actor node = new Actor();
        String name = ((parent==null)?"":(parent.getName()+"."))+actor.name;
        String type = actor.getString("type");
        String comment = actor.getString("comment", null);
        JsonValue format = actor.get("format");
        JsonValue data = actor.get("data");
        JsonValue layout = actor.get("layout");
        JsonValue children = actor.get("children");


        if (children != null) {
            node = new Group();
        }

        switch (type) {
            case "Node":
                break;
            case "Image":
                Texture t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
                node = new Image(t);
                break;
            case "Label":
                Label.LabelStyle lStyle = new Label.LabelStyle();
                lStyle.font = assetDirectory.getEntry(data.getString("font"), BitmapFont.class);
                JsonValue color = data.get("foreground");
                if(color!=null)
                    lStyle.fontColor = new Color(color.getInt(0),color.getInt(1),color.getInt(2),color.getInt(3));
                node = new Label(data.getString("text"),lStyle);
                break;
            case "Button":
                Button.ButtonStyle bStyle = new Button.ButtonStyle();
                Actor upnode = parseNode(children.get(data.getString("upnode")),(Group)node);
                children.remove(data.getString("upnode"));
                if(upnode instanceof Image) {
                    Skin skin = new Skin();
                    bStyle.up = ((Image) upnode).getDrawable();
                    bStyle.down = skin.newDrawable(bStyle.up,0.7f,0.7f,0.7f,1);
                    skin.dispose();
                }
                node = new Button(bStyle);

                break;
            case "TextButton":
                //node = new TextButton();
                break;
            case "ImageButton":
                //node = new ImageButton();
                break;
            case "CheckBox":
                //node = new CheckBox();
                break;
            case "ButtonGroup":
                //node = new ButtonGroup<Button>();
                break;
            case "TextField":
                //node = new TextField();
                break;
            case "TextArea":
                //node = new TextArea();
                break;
            case "List":
                //node = new List<>();
                break;
            case "SelectBox":
                //node = new SelectBox<>();
                break;
            case "ProgressBar":
                //node = new ProgressBar();
                break;
            case "Slider":
                //node = new Slider();
                break;
            case "Window":
                //node = new Window();
                break;
            case "Touchpad":
                //node = new Touchpad();
                break;
            case "Dialog":
                //node = new Dialog();
                break;
            case "NinePatch":
                //node = new NinePatch();
                NinePatch n = new NinePatch();
                n.
                break;
            default:
                throw new IllegalArgumentException("Undefined Type: " + type);
        }



        //TODO: finish format stuff
        if (format != null) {
            String formatType = format.getString("type");
            switch (formatType) {
                case "Anchored":
                    node = new Table();
                    break;
                case "Float":
                    String orientation = format.getString("orientation");
                    String xAlign = format.getString("x_alignment");
                    String yAlign = format.getString("y_alignment");
                    break;
                case "Grid":
                    int width = format.getInt("width");
                    int height = format.getInt("Height");
                    break;
                default:
                    throw new IllegalArgumentException("Layout type Undefined");
            }
        }

        node.setName(name);


        if (layout != null) {
            if (layout.has("priority")) {
                int priority = layout.getInt("priority");
            } else if (layout.has("x_index")) {
                int x_index = layout.getInt("x_index");
                int y_index = layout.getInt("y_index");
                String xAnchor = format.getString("x_anchor");
                String yAnchor = format.getString("y_anchor");
            } else if (layout.has("")) {
            }
        }


        //TODO: finish data stuff
        if (data != null) {
            JsonValue jsPos = data.get("bounds");
            if (jsPos != null) {
                node.setPosition(jsPos.getFloat(0), jsPos.getFloat(1));
            }
            JsonValue jsSize = data.get("size");
            if (jsSize != null) {
                node.setSize(jsSize.getFloat(0), jsSize.getFloat(1));
            }
            JsonValue jsAnchor = data.get("anchor");
            if (jsAnchor != null) {
                node.setOrigin(jsAnchor.getFloat(0), jsAnchor.getFloat(1));
            }
            JsonValue jsScale = data.get("scale");
            if (jsScale != null) {
                if (jsScale.size < 2)
                    node.setScale(jsScale.asFloat());
                else
                    node.setScale(jsScale.getFloat(0), jsScale.getFloat(1));
            }
            if (data.has("angle"))
                node.setRotation(data.getFloat("angle"));
            if (data.has("visible"))
                node.setVisible(data.getBoolean("visible"));
        }

        if(children != null && node instanceof Group) {
            for (JsonValue child : children) {
                ((Group)node).addActor((parseNode(child, (Group) node)));
            }
        }

        return node;
    }
}
