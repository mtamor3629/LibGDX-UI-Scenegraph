package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

public class ProgressBarParser implements NodeParser{
    @Override
    public String getTypeKey() { return "Progress"; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        ProgressBar.ProgressBarStyle pBarStyle = new ProgressBar.ProgressBarStyle();
        pBarStyle.background = assetDirectory.getEntry(data.getString("background"), Drawable.class);
        pBarStyle.knobBefore = assetDirectory.getEntry(data.getString("foreground"), Drawable.class);
        //I assumed progress occurs in 1% increments. I intend to correct this to align with CUGL
        //once I am able to confirm how it behaves there.
        ProgressBar2D pBar = new ProgressBar2D(0f, 1f, 0.01f, pBarStyle);
        pBar.setCaps(assetDirectory.getEntry(data.getString("right_cap"), Drawable.class),
                assetDirectory.getEntry(data.getString("left_cap"), Drawable.class));
        return pBar;
    }
}
