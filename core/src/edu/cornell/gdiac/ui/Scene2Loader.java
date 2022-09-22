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
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.widgets.AnchoredLayout;

/**
 * This class provides functionality for parsing a JSON scenegraph specification, and generating the corresponding LibGDX scenegraph
 */
public class Scene2Loader {
    /**
     * Takes in a {@link JsonValue} to produce a scene graph of ui elements
     */

    static AssetDirectory assetDirectory;
    public static Group genSceneGraph(JsonValue json, AssetDirectory assets,Stage s) {
        assetDirectory = assets;
        Group stage = new Group();
        stage.setSize(s.getWidth(),s.getHeight());
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
            stage.addActor(parseNode(actor, stage,1,1));
        }
        return stage;
    }

    public static Table capsuleToAnchor(Actor actor, String xAnchor, String yAnchor, float xOffset, float yOffset){
        Table table = new Table();
        table.setFillParent(true);
        table.add(actor);
        Cell<Actor> cell = table.getCell(actor);
        cell.size(actor.getWidth(),actor.getHeight());
        if(yAnchor.equals("center")||xAnchor.equals("center"))
            table.center();

        switch (xAnchor) {
            case "left":
                table.left();
                break;
            case "right":
                table.right();
                break;
            case "fill":
                cell.fillX();
                break;
        }
        switch (yAnchor) {
            case "top":
                table.top();
                break;
            case "bottom":
                table.bottom();
                break;
            case "fill":
                cell.fillY();
                break;
        }

        cell.pad(yOffset,xOffset,yOffset,xOffset);
        return table;
    }

    public static Group genAltAltSceneGraph(AssetDirectory assets,Stage stage) {
        AnchoredLayout layout = new AnchoredLayout();
        Texture t = assets.getEntry("background",Texture.class);
        Image i = new Image(t);
        i.setSize(t.getWidth(),t.getHeight());
        i.setOrigin(0,0.5f);

        layout.addAnchoredActor(i,"left","fill",0,0);
        return layout;
    }
    public static Group genAltSceneGraph(AssetDirectory assets,Stage stage){
        Group g = new Group();
        g.setSize(stage.getWidth(),stage.getHeight());
        Table t = new Table();
        g.addActor(t);
        //t.setDebug(true);
        t.setFillParent(true);

        Image i = new Image(assets.getEntry("background",Texture.class));
        i.setSize(stage.getWidth()*2, stage.getHeight());

        i.setOrigin(0,0.5f);
        t.add(i).width(i.getWidth());
        t.left();

        Table t2 = new Table();
        g.addActor(t2);
        t2.setFillParent(true);

        Image menu = new Image(assets.getEntry("menuboard",Texture.class));
        menu.setOrigin(0.5f,1.0f);
        menu.setScale(0.8f);
        Group subG = new Group();
        subG.setSize(menu.getWidth()*0.8f, menu.getHeight()*0.8f);
        subG.addActor(menu);
        t2.add(subG);
        //t2.getCell(subG).size(menu.getPrefWidth()*0.8f,menu.getPrefHeight()*0.8f);
        t2.center();
        t2.top();
        Button.ButtonStyle bStyle = new Button.ButtonStyle();
        Image left = new Image(assets.getEntry("left",Texture.class));
        //left.setScale(0.8f);
        Skin skin = new Skin();
        bStyle.up = left.getDrawable();
        bStyle.down = skin.newDrawable(bStyle.up,0.7f,0.7f,0.7f,1);
        Button leftB = new Button(bStyle);
        leftB.setSize(leftB.getWidth()*0.8f,leftB.getHeight()*0.8f);
        //subG.addActor(leftB);
        subG.addActor(capsuleToAnchor(leftB,"left","bottom",subG.getWidth()*0.1f,subG.getHeight()*0.1f));

        Image right = new Image(assets.getEntry("right",Texture.class));
        //left.setScale(0.8f);
        Button.ButtonStyle bStyle2 = new Button.ButtonStyle();
        bStyle2.up = right.getDrawable();
        bStyle2.down = skin.newDrawable(bStyle2.up,0.7f,0.7f,0.7f,1);

        Button rightB = new Button(bStyle2);
        rightB.setSize(rightB.getWidth()*0.8f,rightB.getHeight()*0.8f);
        //subG.addActor(leftB);
        subG.addActor(capsuleToAnchor(rightB,"right","bottom",subG.getWidth()*0.1f,subG.getHeight()*0.1f));

        NinePatch n = new NinePatch(assets.getEntry("menubutton",Texture.class),33,33,40,40);
        NinePatchDrawable nD = new NinePatchDrawable(n);
        n.scale(0.8f,0.8f);
        TextButton.TextButtonStyle tStyle = new ImageTextButton.ImageTextButtonStyle();
        BitmapFont b = assets.getEntry("gyparody",BitmapFont.class);
        b.getData().setScale(0.8f);
        tStyle.font = b;
        tStyle.up=nD;
        tStyle.down = skin.newDrawable(nD,0.7f,0.7f,0.7f,1);
        TextButton text = new TextButton("Click Me",tStyle);
        text.setSize(300,75);
        Table tbl = capsuleToAnchor(text,"center","middle",0,0);
        tbl.getCell(text).size(300*0.8f,75*0.8f);
        subG.addActor(tbl);
        skin.dispose();
        System.out.println(text.getScaleX());
        return g;
    }


    private static Actor parseNode(JsonValue actor,Group parent,float scaleX,float scaleY){
        String name = ((parent==null)?"":(parent.getName()+"."))+actor.name;
        System.out.println(name);
        String type = actor.getString("type");
        String comment = actor.getString("comment", null);
        JsonValue format = actor.get("format");
        JsonValue data = actor.get("data");
        JsonValue layout = actor.get("layout");
        JsonValue children = actor.get("children");
        boolean hasChild = false;
        boolean anchored = false;

        if(children != null)
            hasChild = true;

        if (format != null) {
            String formatType = format.getString("type");
            switch (formatType) {
                case "Anchored":
                    anchored = true;
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
                Actor upnode = parseNode(children.get(data.getString("upnode")), (Group) node,scaleX,scaleY);
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
                Actor tUp = parseNode(children.get(data.getString("upnode")), (Group)node ,scaleX,scaleY);
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
        }

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
                    scaleY = jsScale.asFloat();
                    scaleX = jsScale.asFloat();
                }
                else{
                    scaleY=jsScale.getFloat(0);
                    scaleX = jsScale.getFloat(1);
                }

            }
            if (data.has("angle"))
                node.setRotation(data.getFloat("angle"));
            if (data.has("visible"))
                node.setVisible(data.getBoolean("visible"));
        }

        node.setSize(node.getWidth()*scaleX,node.getHeight()*scaleY);

        if(hasChild && !(node instanceof Group)){
            Group g = new Group();
            g.addActor(node);
            g.setSize(node.getWidth(), node.getHeight());
            node = g;
        }

        if(hasChild) {
            for (JsonValue child : children) {
                ((Group) node).addActor((parseNode(child, (Group) node,scaleX,scaleY)));
            }
        }

        if (layout!=null && layout.has("x_anchor")) {
            boolean abs = layout.getBoolean("absolute",false);
            float xOffset = layout.getFloat("x_offset",0f);
            float yOffset = layout.getFloat("y_offset",0f);
            if(!abs && parent != null){
                xOffset*=parent.getWidth();
                yOffset*=parent.getHeight();
            }
            return capsuleToAnchor(node,layout.getString("x_anchor"),layout.getString("y_anchor"),xOffset,yOffset);
        }

        return node;

    }

    /**
     * Parse a node of the scenegraph, inherit properties from its parent.
     *
     * @param actor  the JsonValue to be parsed
     * @param parent the parent node of this graph, or null if the node is the root;
     */

    private static Actor parseNodeOld(JsonValue actor, Group parent) {
        Actor node = new Actor();
        String name = ((parent==null)?"":(parent.getName()+"."))+actor.name;
        System.out.println(name);
        String type = actor.getString("type");
        String comment = actor.getString("comment", null);
        JsonValue format = actor.get("format");
        JsonValue data = actor.get("data");
        JsonValue layout = actor.get("layout");
        JsonValue children = actor.get("children");


        if(actor.name.equals("button1"))
            return new Actor();

        if (children != null) {
            node = new Group();
            node.setName(name);
        }

        switch (type) {
            case "Node":
            case "NinePatch":
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
                Actor upnode = parseNode(children.get(data.getString("upnode")),(Group)node, node.getScaleX(),node.getScaleY());
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
            default:
                throw new IllegalArgumentException("Undefined Type: " + type);
        }



        //TODO: finish format stuff
        if (format != null) {
            String formatType = format.getString("type");
            switch (formatType) {
                case "Anchored":

                    if(!(node instanceof Group)){
                        Group group = new Group();
                        group.addActor(node);
                        node = group;
                    }

                    node = new Group();
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
            } else if (layout.has("x_anchor")) {

                Table t = new Table();
                t.add(node);
                t.setFillParent(true);
                String xAnchor = layout.getString("x_anchor");
                String yAnchor = layout.getString("y_anchor");
                if(yAnchor.equals("center"))
                    t.center();
                else if(xAnchor.equals("center"))
                    t.center();

                if(xAnchor.equals("left"))
                    t.left();
                else if(xAnchor.equals("right"))
                    t.right();
                else if (xAnchor.equals("fill"))
                    t.getCell(node).fillX();
                if(yAnchor.equals("top"))
                    t.top();
                else if(yAnchor.equals("bottom"))
                    t.bottom();
                else if (yAnchor.equals("fill"))
                    t.getCell(node).fillY();
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
               // ((Group)node).addActor((parseNode(child, (Group) node)));
            }
        }

        return node;
    }
}
