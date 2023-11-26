package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.ProgressBarNode;

public class ProgressBarParser implements NodeParser{
    @Override
    public String getTypeKey() { return "Progress"; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        ProgressBar.ProgressBarStyle pBarStyle = new ProgressBar.ProgressBarStyle();
        Texture bg = assetDirectory.getEntry(data.getString("background"), Texture.class);
        Texture fg = assetDirectory.getEntry(data.getString("foreground"), Texture.class);
        pBarStyle.background = new TextureRegionDrawable(new TextureRegion(bg));
        pBarStyle.knobBefore = new TextureRegionDrawable(new TextureRegion(fg));
        //I assumed progress occurs in 1% increments. I intend to correct this to align with CUGL
        //once I am able to confirm how it behaves there.
        ProgressBarNode pBar = new ProgressBarNode(0f, 1f, 0.01f, pBarStyle);
        pBar.setSize(Math.max(bg.getWidth(), fg.getWidth()), Math.max(bg.getHeight(), fg.getHeight()));
        Texture rCap = assetDirectory.getEntry(data.getString("right_cap"), Texture.class);
        Texture lCap = assetDirectory.getEntry(data.getString("left_cap"), Texture.class);
        pBar.setCaps(new TextureRegion(rCap), new TextureRegion(lCap));
        //TODO: make use of event listener
        return pBar;
    }
}
