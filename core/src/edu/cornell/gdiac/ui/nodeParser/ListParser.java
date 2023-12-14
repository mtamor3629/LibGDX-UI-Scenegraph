package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.Scene2Loader;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.TexturedNode;

import javax.script.ScriptException;

public class ListParser implements NodeParser {
    Scene2Loader loader;
    public ListParser(Scene2Loader loader) {
        this.loader = loader;
    }
    @Override
    public String getTypeKey() { return "List"; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        List.ListStyle lStyle = new List.ListStyle();

        //background
        String key = null;
        if (data.has("background")) key = data.getString("background");
        Texture bg = null;
        if (key != null && assetDirectory.hasEntry(key, Texture.class)) bg = assetDirectory.getEntry(key, Texture.class);
        if (bg != null) bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        else bg = TexturedNode.defaultTexture();
        lStyle.background = new TextureRegionDrawable(bg);

        //selected background
        key = null;
        if (data.has("selection")) key = data.getString("selection");
        Texture sbg = null;
        if (key != null && assetDirectory.hasEntry(key, Texture.class)) sbg = assetDirectory.getEntry(key, Texture.class);
        if (sbg != null) sbg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        else {
            Pixmap p = new Pixmap(1,1, Pixmap.Format.RGB888);
            p.setColor(Color.BLUE);
            p.fill();
            sbg = new Texture(p);
            sbg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        }
        lStyle.selection = new TextureRegionDrawable(sbg);

        //over
        key = null;
        if (data.has("over")) key = data.getString("over");
        Texture over = null;
        if (key != null && assetDirectory.hasEntry(key, Texture.class)) over = assetDirectory.getEntry(key, Texture.class);
        if (over != null) over.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        else over = TexturedNode.defaultTexture();
        lStyle.over = new TextureRegionDrawable(over);

        //down
        key = null;
        if (data.has("down")) key = data.getString("down");
        Texture down = null;
        if (key != null && assetDirectory.hasEntry(key, Texture.class)) down = assetDirectory.getEntry(key, Texture.class);
        if (down != null) bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        else down = TexturedNode.defaultTexture();
        lStyle.down = new TextureRegionDrawable(down);

        //font
        JsonValue fontColor = data.get("font_color");
        if (fontColor!=null)
            lStyle.fontColorUnselected = new Color(fontColor.getInt(0)/255f, fontColor.getInt(1)/255f, fontColor.getInt(2)/255f, fontColor.getInt(3)/255f);
        else lStyle.fontColorUnselected = Color.BLACK;
        JsonValue sFontColor = data.get("selected_font_color");
        if (fontColor!=null)
            lStyle.fontColorSelected = new Color(sFontColor.getInt(0)/255f, sFontColor.getInt(1)/255f, sFontColor.getInt(2)/255f, sFontColor.getInt(3)/255f);
        else lStyle.fontColorSelected = Color.WHITE;
        if (data.has("font")){
            BitmapFont b = assetDirectory.hasEntry(data.getString("font"), BitmapFont.class) ?
                    assetDirectory.getEntry(data.getString("font"), BitmapFont.class) : null;
            if (b != null) b.getData().setScale(scaleX, scaleY);
            lStyle.font = b;
        }

        List<String> node = new List<>(lStyle);
        if (data.has("items")){
            Array<String> items = new Array<>();
            for(JsonValue item : data.get("items")) items.add(item.asString());
            node.setItems(items);
        }

        //-1 represents nothing selected
        int selected = data.getInt("selected", -1);
        if (selected < -1 || selected >= node.getItems().size) selected = -1;
        node.setSelectedIndex(selected);
        return node;
    }
}
