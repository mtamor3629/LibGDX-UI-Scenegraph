/*
 * FloatLayout.java
 *
 * @author Barry Lyu
 * @date   12/20/22
 */
package edu.cornell.gdiac.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;

/**
 * This class represents a float layout manager where actors are placed horizontally or vertically by priority.
 * Actors with higher priority are placed first, and if the width exceeds the maximum width, the actor is placed on
 * the next row, vice versa for vertical layout.
 */
public class FloatLayout extends WidgetGroup {

    /**
     * This class represents a node in the FloatLayout that wraps an actor and its priority.
     */
    private class FloatNode{
        public Table table;
        public Actor actor;
        public int priority;

        public FloatNode(Actor actor, int priority){
            this.actor = actor;
            this.priority = priority;
        }
    }

    /**
     * This class represents a comparator for FloatNode that compares the priority of two FloatNode.
     */
    private static class CompareFloatNode implements Comparator<FloatNode> {
        @Override
        public int compare(FloatNode o1, FloatNode o2) {
            return o1.priority-o2.priority;
        }
    }

    /** The list of FloatNode in the FloatLayout */
    private Array<FloatNode> nodes;
    /** The Table used to arrange the nodes */
    private Table table;
    /** The orientation of the FloatLayout (0: horizontal, 1: vertical) */
    private int orient;

    /**
     * Creates a new FloatLayout with the given orientation.
     *
     * @param orientation The orientation of the FloatLayout (0: horizontal, 1: vertical)
     * @param xAlign the horizontal alignment of the layout, see {@link AnchoredLayout#addAnchoredActor(Actor, String, String, float, float, boolean)}
     */
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

    /**
     * Adds an actor to the FloatLayout with the given priority.
     *
     * @param actor The actor to add
     * @param priority The priority of the actor
     */
    public void addFloatActor(Actor actor, int priority){
        FloatNode f = new FloatNode(actor,priority);
        nodes.add(f);
    }

    /**
     * This method is overriden to prevent adding irrelevant actors to the FloatLayout.
     * DO NOT USE this method at all.
     */
    @Override
    public void addActor(Actor actor){
    }

    /** The 2D array of Actors in the float layout */
    Array<Array<Actor>> layout = new Array<Array<Actor>>();

    /**
     * Lays out the actors in the FloatLayout. Elements are wrapped around automatically.
     */
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
