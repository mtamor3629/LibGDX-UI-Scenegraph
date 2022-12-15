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
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.widgets.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * This class provides functionality for parsing a JSON scenegraph specification, and generating the corresponding LibGDX scenegraph
 */
public class Scene2Loader {

    static AssetDirectory assetDirectory;
    static HashMap<String, CustomWidget> widgetList = new HashMap<>();
    static ScriptEngineManager manager = new ScriptEngineManager();;

    /**
     * Takes in a {@link JsonValue} to produce a scene graph of ui elements.
     * And adds the node bindings to the engine
     *
     * @param json The json file to be parsed
     * @param assets The AssetDirectory to be used
     * @param s The parent stage
     * @return The root node of the scene graph
     */
    public static Group genSceneGraph(JsonValue json, AssetDirectory assets, Stage s) throws ScriptException {
        assetDirectory = assets;
        Group stage = new Group();
        stage.setSize(s.getWidth(),s.getHeight());
        stage.setName("root");
        JsonValue sceneGraph = json.get("scene2s");
        if (sceneGraph == null || sceneGraph.isEmpty())
            throw new IllegalArgumentException("corrupted json file, does not contain scene2s specs");
        //iterate through all the nodes
        JsonValue widgets = json.get("widgets");
        if (widgets != null)
            loadWidgets(widgets);
        long currentTime = System.currentTimeMillis();
        for (JsonValue actor : sceneGraph) {
            stage.addActor(parseNode(actor, "",stage, 1, 1));
        }
        return stage;
    }

    /** Loads the widgets from the json file
     *
     * @param widgets The widget section of the JsonFile
     */
    public static void loadWidgets(JsonValue widgets){
        JsonReader reader = new JsonReader();
        widgetList.clear();
        for (JsonValue widget: widgets) {
            JsonValue widgetFile = reader.parse(Gdx.files.internal(widget.asString()));
            widgetList.put(widget.name, new CustomWidget(widgetFile));
        }
    }

    /** Parses a node in the json file
     *
     * @param actor The JsonValue of the node
     * @param parent The parent of the node
     * @param scaleX The scaleX of its parent node
     * @param scaleY The scaleY of its parent node
     * @return The node parsed
     */
    private static Actor parseNode(JsonValue actor,String parentName, Group parent,float scaleX,float scaleY) throws ScriptException {
        //parse basic information
        String name = (parentName.equals("")?"":parentName+"_")+actor.name;
        System.out.println(name);
        String type = actor.getString("type");
        String comment = actor.getString("comment", null);
        JsonValue format = actor.get("format");
        JsonValue data = actor.get("data");
        JsonValue layout = actor.get("layout");
        JsonValue children = actor.get("children");
        boolean hasChild = false;
        WidgetGroup layoutWidget = null;

        //check if it has children
        if(children != null)
            hasChild = true;

        //check if layout manage is required
        if (format != null) {
            String formatType = format.getString("type");
            switch (formatType) {
                case "Anchored":
                    layoutWidget = new AnchoredLayout();
                    break;
                case "Float":
                    String orientation = format.getString("orientation");
                    String xAlign = format.getString("x_alignment");
                    String yAlign = format.getString("y_alignment");
                    layoutWidget = new FloatLayout(orientation,xAlign,yAlign);
                    break;
                case "Grid":
                    int width = format.getInt("width");
                    int height = format.getInt("Height");
                    layoutWidget = new GridLayout(width,height);
                    break;
                case "Constraint":
                    String script = format.getString("constraints");
                    layoutWidget = new ConstraintLayout(manager,script);
                    break;
                default:
                    throw new IllegalArgumentException("Layout type Undefined");
            }
        }

        //parse node type and establish corresponding node
        Actor node = new Group();
        switch (type) {
            case "Node":
                node = new Group();
                if(parent!=null)
                    node.setSize(parent.getWidth(), parent.getHeight());
                break;
            case "NinePatch":
                data.get("texture");
                int leftB = data.get("interior").getInt(0);
                int botB = data.get("interior").getInt(1);
                NinePatch np = new NinePatch(assetDirectory.getEntry(data.getString("texture"),Texture.class),leftB,botB,leftB,botB);
                np.scale(scaleX,scaleY);
                node = new Image(new NinePatchDrawable(np));
                break;
            case "Image":
                Texture t = assetDirectory.getEntry(data.getString("texture"), Texture.class);
                node = new Image(t);
                node.setSize(t.getWidth(),t.getHeight());
                break;
            case "Label":
                Label.LabelStyle lStyle = new Label.LabelStyle();
                lStyle.font = assetDirectory.getEntry(data.getString("font"), BitmapFont.class);
                JsonValue color = data.get("foreground");
                if (color != null)
                    lStyle.fontColor = new Color(color.getInt(0), color.getInt(1), color.getInt(2), color.getInt(3));
                node = new Label(data.getString("text"), lStyle);
                break;
            case "Button":
                Button.ButtonStyle bStyle = new Button.ButtonStyle();
                Actor upnode = parseNode(children.get(data.getString("upnode")),name, (Group) node,scaleX,scaleY);
                children.remove(data.getString("upnode"));
                if (upnode instanceof Image) {
                    Skin skin = new Skin();
                    bStyle.up = ((Image) upnode).getDrawable();
                    bStyle.down = skin.newDrawable(bStyle.up, 0.7f, 0.7f, 0.7f, 1);
                    skin.dispose();
                    node = new Button(bStyle);
                }
                break;
            case "TextButton":
                TextButton.TextButtonStyle tStyle = new ImageTextButton.ImageTextButtonStyle();
                Actor tUp = parseNode(children.get(data.getString("upnode")),name, (Group)node ,scaleX,scaleY);
                children.remove(data.getString("upnode"));
                if (tUp instanceof Image) {
                    Skin skin = new Skin();
                    tStyle.up = ((Image) tUp).getDrawable();
                    tStyle.down = skin.newDrawable(tStyle.up, 0.7f, 0.7f, 0.7f, 1);
                    skin.dispose();
                }
                BitmapFont b = assetDirectory.getEntry("gyparody",BitmapFont.class);
                b.getData().setScale(scaleX,scaleY);
                tStyle.font = b;
                node = new TextButton(data.getString("text"),tStyle);
                break;
            case "Widget":
                return parseNode(widgetList.get(data.getString("key")).getJsonWithVar(data.get("variables")),parentName,parent,scaleX,scaleY);
            default:
                //TODO: Add more node types
                throw new IllegalArgumentException("Node type Undefined");
        }

        //set metadata for nodes if any
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
                if (jsScale.size < 2) {
                    scaleY *= jsScale.asFloat();
                    scaleX *= jsScale.asFloat();
                }
                else{
                    scaleY *= jsScale.getFloat(0);
                    scaleX *= jsScale.getFloat(1);
                }

            }
            if (data.has("angle"))
                node.setRotation(data.getFloat("angle"));
            if (data.has("visible"))
                node.setVisible(data.getBoolean("visible"));
        }

        //fix scaling issue with respect to parent
        node.setSize(node.getWidth()*scaleX,node.getHeight()*scaleY);

        //put binding into engine for constraint layout
        node.setName(name);
        manager.put(name,node);
        System.out.println("putting "+name+" into manager");

        //Convert to group if in the node is not a group for adding children
        if((hasChild && !(node instanceof Group))||layoutWidget!=null){
            Group g = new Group();
            g.addActor(node);
            g.setSize(node.getWidth(), node.getHeight());
            node = g;
        }

        // add layout widget if any
        if(layoutWidget!=null){
            ((Group)node).addActor(layoutWidget);
            layoutWidget.setSize(node.getWidth(),node.getHeight());
        }

        //add children if any
        if(hasChild) {
            if(layoutWidget!=null){
                for (JsonValue child : children) {
                    /*  Layout widget will be the parent of all children and they will be adding it directly.*/
                    parseNode(child, name,layoutWidget, scaleX, scaleY);
                }
            }
            else {
                for (JsonValue child : children) {
                    ((Group) node).addActor((parseNode(child, name,(Group) node, scaleX, scaleY)));
                }
            }
        }

        // perform the layout manager parses if any
        if (layout!=null && layout.has("x_anchor") && parent instanceof AnchoredLayout) {
            boolean abs = layout.getBoolean("absolute",false);
            float xOffset = layout.getFloat("x_offset",0f);
            float yOffset = layout.getFloat("y_offset",0f);
            ((AnchoredLayout) parent).addAnchoredActor(node,layout.getString("x_anchor"),layout.getString("y_anchor"),xOffset,yOffset,abs);
        }
        else if (layout!=null && layout.has("priority") && parent instanceof FloatLayout){
            ((FloatLayout) parent).addFloatActor(node,layout.getInt("priority"));
        }
        else if (layout!=null && layout.has("x_index") && parent instanceof GridLayout) {
            ((GridLayout) parent).addGridActor(node,layout.getInt("y_index"),layout.getInt("x_index"),layout.getString("x_anchor"),layout.getString("y_anchor"));
        }
        else if (layout!=null && layout.has("bindings") && parent instanceof ConstraintLayout){
            ((ConstraintLayout) parent).addConstraintActor(node,name,layout.getString("bindings"));
        }
        return node;
    }
}
