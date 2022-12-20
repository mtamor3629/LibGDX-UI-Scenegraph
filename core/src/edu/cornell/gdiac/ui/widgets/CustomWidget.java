/*
 * CustomWidget.java
 *
 * @author Barry Lyu
 * @date   12/20/22
 */
package edu.cornell.gdiac.ui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;

/**
 * This class represents a CustomWidget parser. It parses custom widgets by replacing the fields with the specified variables.
 */
public class CustomWidget{
    HashMap<String, JsonValue> properties;
    JsonValue json;

    /**
     * Creates a new custom widget with the given JSON
     * @param json The JSON to create the custom widget with
     */
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

    /**
     * @return The modified JSON of the custom widget with variable fields replaced.
     */
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
