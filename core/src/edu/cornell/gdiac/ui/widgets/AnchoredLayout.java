/*
 * AnchoredLayout.java
 *
 * @author Barry Lyu
 * @date   12/20/22
 */
package edu.cornell.gdiac.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Array;

/**
 * This class represents a layout manager that allows for anchoring of actors. The layout manager will perform
 * synonymously to the CUGL scenegraph counterpart.
 */
public class AnchoredLayout extends WidgetGroup {

    /**
     * This class represents a layout cell for the AnchoredLayout. Each node in the layout will be wrapped in an
     * anchored node.
     */
    private class AnchoredNode{
        public Table table;
        public Actor actor;
        String xAnchor, yAnchor;
        float offsetX, offsetY;
        boolean abs;

        public AnchoredNode(Actor actor,String xAnchor, String yAnchor,boolean abs){
            this.actor = actor;
            this.xAnchor = xAnchor;
            this.yAnchor = yAnchor;
            this.abs = abs;
        }

        public void setOffset(float x, float y){
            this.offsetX = x;
            this.offsetY = y;
        }

        /**
         * This method uses a Table to simulate the anchor points.
         */
        public Table getLayout(AnchoredLayout layout){
            if(table== null){
                table = new Table();
            }
            table.reset();
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
            float width = layout.getWidth();
            float height = layout.getHeight();
            if(abs){
                width=1;
                height=1;
            }
            float padLeft = offsetX>0?offsetX*width:0;
            float padRight = offsetX<0?-offsetX*width:0;
            float padTop = offsetY<0?-offsetY*height:0;
            float padBottom = offsetY>0?offsetY*height:0;
            cell.pad(padTop,padLeft,padBottom,padRight);
            return table;
        }
    }

    /** The list of anchored nodes */
    public Array<AnchoredNode> anchors;

    public AnchoredLayout(){
        super();
        anchors = new Array<>();
        this.setFillParent(true);
    }

    /**
     * This method adds an anchored actor to the layout
     *
     * @param actor The actor to add
     * @param xAnchor The x anchor point for the node (left, right, center, fill)
     * @param yAnchor The y anchor point for the node (top, bottom, center, fill)
     * @param abs Whether the anchor points are absolute or relative
     * @param xOffset The x offset with respect to the anchor point
     * @param yOffset The y offset with respect to the anchor point
     */
    public void addAnchoredActor(Actor actor, String xAnchor, String yAnchor, float xOffset, float yOffset, boolean abs){
        AnchoredNode node = new AnchoredNode(actor,xAnchor,yAnchor,abs);
        node.setOffset(xOffset, yOffset);
        anchors.add(node);
    }

    /**
     * This method is overriden to prevent adding unanchored actors to the layout
     * DO NOT USE this method to add actors to the layout.
     */
    @Override
    public void addActor(Actor actor) {
    }

    /**
     * Layouts all the anchored nodes in this manager.
     */
    @Override
    public void layout(){
        this.clearChildren();
        for(AnchoredNode node: anchors){
           super.addActor(node.getLayout(this));
        }
    }
}
