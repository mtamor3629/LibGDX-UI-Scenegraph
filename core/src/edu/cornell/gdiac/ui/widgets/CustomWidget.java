package edu.cornell.gdiac.ui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;

public class CustomWidget{
    HashMap<String, JsonValue> properties;
    JsonValue json;
    public CustomWidget(JsonValue json){
        this.json = json;
        properties = new HashMap<String, JsonValue>();
        JsonReader reader = new JsonReader();
        String comment = json.getString("comments");
        JsonValue variables = json.get("variables");
        JsonValue content = json.get("contents");
        for(JsonValue var: variables){
            JsonValue pointer = content;
            for(String str: var.asStringArray()){
                if(pointer.has(str)){
                    pointer = pointer.get(str);
                }
                else {
                    pointer = null;
                    break;
                }
            }
            if(pointer!=null)
                properties.put(var.name,pointer);
        }
    }

    //object, array, stringValue, doubleValue, longValue, booleanValue, nullValue
    public JsonValue getJsonWithVar(JsonValue variables){
        for(JsonValue var: variables){
            if(properties.containsKey(var.name)){
                JsonValue pointer = properties.get(var.name);
                if(pointer!=null){
                    pointer.setType(var.type());
                    switch (var.type()){
                        case object:
                        case array:
                            pointer.child = var.child;
                            break;
                        case stringValue:
                            pointer.set(var.asString());
                            break;
                        case booleanValue:
                            pointer.set(var.asBoolean());
                            break;
                    }
                }
                else{
                    properties.put(var.name,var);
                }
            }
        }
        return json.get("contents");
    }
}
