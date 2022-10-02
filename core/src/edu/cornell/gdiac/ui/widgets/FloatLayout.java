package edu.cornell.gdiac.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;

public class FloatLayout extends WidgetGroup {
    private class FloatNode{
        public Table table;
        public Actor actor;
        public int priority;

        public FloatNode(Actor actor, int priority){
            this.actor = actor;
            this.priority = priority;
        }
    }

    private static class CompareFloatNode implements Comparator<FloatNode> {
        @Override
        public int compare(FloatNode o1, FloatNode o2) {
            return o1.priority-o2.priority;
        }
    }

    private Array<FloatNode> nodes;
    private Table table;
    private int orient; // 0: horizontal, 1: vertical

    public FloatLayout(String orientation,String xAlign,String yAlign){
        super();
        table = new Table();
        table.setFillParent(true);
        if(xAlign.equals("center")||yAlign.equals("center"))
            table.center();
        switch (xAlign) {
            case "left":
                table.left();
                break;
            case "right":
                table.right();
                break;
        }
        switch (yAlign) {
            case "top":
                table.top();
                break;
            case "bottom":
                table.bottom();
                break;
        }

        if(orientation.equals("horizontal"))
            orient = 0;
        else
            orient = 1;

        nodes = new Array<FloatNode>();
        this.setFillParent(true);
    }

    public void addFloatActor(Actor actor, int priority){
        FloatNode f = new FloatNode(actor,priority);
        nodes.add(f);
    }

    @Override
    public void addActor(Actor actor){
    }

    Array<Array<Actor>> layout = new Array<Array<Actor>>();

    @Override
    public void layout() {
        super.layout();
        super.addActor(table);
        table.clearChildren();
        nodes.sort(new CompareFloatNode());
        if(orient == 0){
            float widthSum = 0f;
            for(FloatNode f:nodes){
                widthSum += f.actor.getWidth();
                if(widthSum>=getWidth()){
                    table.row();
                    widthSum = f.actor.getWidth();
                }
                table.add(f.actor).size(f.actor.getWidth(),f.actor.getHeight()).align(table.getAlign());
            }
        }else{
            float heightSum = 0f;
            layout.clear();
            Array<Actor> column = new Array<Actor>();
            for (int i = 0; i < nodes.size; i++) {
                heightSum += nodes.get(i).actor.getHeight();
                if(heightSum>=getHeight()){
                    layout.add(column);
                    column = new Array<Actor>();
                    heightSum = nodes.get(i).actor.getHeight();
                    column.add(nodes.get(i).actor);
                }
                else{
                    column.add(nodes.get(i).actor);
                }
            }
            layout.add(column);
            int max = 0;
            for(Array<Actor> a:layout){
                if(a.size>max)
                    max = a.size;
            }
            for(int i = 0; i < max; i++){
                for(Array<Actor> a:layout){
                    if(a.size>i)
                        table.add(a.get(i)).size(a.get(i).getWidth(),a.get(i).getHeight()).align(table.getAlign());
                }
                table.row();
            }

        }
    }
}
