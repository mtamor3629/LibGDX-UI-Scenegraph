package edu.cornell.gdiac.ui.nodeParser;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.ui.assets.AssetDirectory;

public class SliderParser implements NodeParser{
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

    @Override
    public Actor process(JsonValue json, AssetDirectory assetDirectory, float scaleX, float scaleY, Actor parent) {
        JsonValue data = json.get("data");
        float[] bounds = data.get("bounds").asFloatArray();
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
        // JsonValue knob = data.get("knob");
        // JsonValue path = data.get("path");
        Slider s = new Slider(range[0], range[1], tick, false, new Slider.SliderStyle());
        if (snap){
            int valsLen = (int) Math.ceil((range[1]-range[0])/tick);
            float[] values = new float[valsLen];
            for(int i = 0; i < valsLen; i++) values[i] = range[0] + i*((range[1]-range[0])/valsLen);
            s.setSnapToValues(values, ((range[1]-range[0])/valsLen)/2f);
        }
        return s;
    }
}
