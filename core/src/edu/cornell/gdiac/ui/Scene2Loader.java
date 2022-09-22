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
        //layout.setSize(stage.getWidth(), stage.getHeight());
        Texture t = assets.getEntry("background",Texture.class);
        Image i = new Image(t);
        i.setSize(t.getWidth(),t.getHeight());
        i.setOrigin(0,0.5f);

        layout.addAnchoredActor(i,"left","fill",0,0,false);
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
        String type = actor.getString("type");
        String comment = actor.getString("comment", null);
        JsonValue format = actor.get("format");
        JsonValue data = actor.get("data");
        JsonValue layout = actor.get("layout");
        JsonValue children = actor.get("children");
        boolean hasChild = false;
        boolean anchored = false;
        AnchoredLayout anchoredLayout = null;

        if(children != null)
            hasChild = true;

        if (format != null) {
            String formatType = format.getString("type");
            switch (formatType) {
                case "Anchored":
                    anchoredLayout = new AnchoredLayout();
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
                System.out.println("TextButton");
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
            System.out.println(name+": "+g.getWidth());
            node = g;
        }

        if(anchoredLayout!=null){
            ((Group)node).addActor(anchoredLayout);
            anchoredLayout.setSize(node.getWidth(),node.getHeight());
        }

        if(hasChild) {
            if(anchoredLayout!=null){
                for (JsonValue child : children) {
                    parseNode(child, anchoredLayout, scaleX, scaleY);
                }
            }
            else {
                for (JsonValue child : children) {
                    ((Group) node).addActor((parseNode(child, (Group) node, scaleX, scaleY)));
                }
            }
        }

        if (layout!=null && layout.has("x_anchor") && parent instanceof AnchoredLayout) {
            boolean abs = layout.getBoolean("absolute",false);
            float xOffset = layout.getFloat("x_offset",0f);
            float yOffset = layout.getFloat("y_offset",0f);
            ((AnchoredLayout) parent).addAnchoredActor(node,layout.getString("x_anchor"),layout.getString("y_anchor"),xOffset,yOffset,abs);
        }

        return node;

    }
}
