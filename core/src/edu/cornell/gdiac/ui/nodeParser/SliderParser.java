package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.Scene2Loader;
import edu.cornell.gdiac.ui.assets.AssetDirectory;
import edu.cornell.gdiac.ui.nodes.TexturedNode;

import javax.script.ScriptException;

public class SliderParser implements NodeParser{
    private Scene2Loader loader;

    @Override
    public String getTypeKey() { return "Slider"; }

    //TODO: start with default LibGDX sliders and implement custom paths if I have time
//    //I made a new class to support custom paths
//    public class Slider2D extends Slider{
//        public Slider2D(float min, float max, float stepSize, Skin skin) {
//            super(min, max, stepSize, false, skin);
//        }
//        public Slider2D (float min, float max, float stepSize, SliderStyle style) {
//            super(min, max, stepSize, false, style);
//        }
//        public Slider2D (float min, float max, float stepSize, Skin skin, String styleName) {
//            super(min, max, stepSize, false, skin, styleName);
//        }
//        @Override
//        protected float snap(float value){
//            float v = super.snap(value);
//            return (v > this.getMaxValue() || value > this.getMaxValue()) ? v : this.getMaxValue();
//        }
//    }
    public SliderParser(Scene2Loader loader) { this.loader = loader; }

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        int[] bounds = data.get("bounds").asIntArray();
        float[] range;
        if (data.has("range")) range = data.get("range").asFloatArray();
        else range = new float[]{0f, 100f};
        //default to the middle
        float value = data.getFloat("value", (range[1]-range[0])/2f);
        //default to 1% increments
        float defTick = Math.abs((range[1]-range[0])/100f);
        float tick = data.getFloat("tick", defTick);
        if (tick < 0) tick = defTick;
        //should snap default to false?
        boolean snap = data.getBoolean("snap", false);
        /* to be used if I have time for the full slider implementation
        JsonValue knob = data.get("knob");
        JsonValue path = data.get("path");*/
        //TODO: seems like vertical wrapping of a texture doesn't work when drawing a slider
        Slider.SliderStyle sStyle = new Slider.SliderStyle();
        Pixmap p = new Pixmap(1, bounds[3], Pixmap.Format.RGB888);
        p.setColor(Color.WHITE);
        p.fill();
        Texture t = new Texture(p);
        t.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        sStyle.background = new TextureRegionDrawable(t);
        if (data.has("knob")){
            Group node = new Group();
            try {
                Button knob = (Button) loader.parseNode(data.get("knob").get("up"),"", (Group)node ,scaleX,scaleY);
                sStyle.knob = knob.getStyle().up;
                sStyle.knobDown = knob.getStyle().down;
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        }

        Slider s = new Slider(range[0], range[1], tick, false, sStyle);
        if (snap){
            int valsLen = (int) Math.ceil((range[1]-range[0])/tick);
            float[] values = new float[valsLen];
            for(int i = 0; i < valsLen; i++) values[i] = range[0] + i*((range[1]-range[0])/valsLen);
            s.setSnapToValues(values, ((range[1]-range[0])/valsLen)/2f);
        }
        s.setValue(value);
        s.setBounds(bounds[0],bounds[1],bounds[2],bounds[3]);
        //TODO: make use of event listener
        return s;
    }
}
