package edu.cornell.gdiac.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import sun.tools.jconsole.Tab;

public class GridLayout extends WidgetGroup {
    private class GridNode{
        public Actor actor;
        String xAnchor, yAnchor;
    }

    private GridNode[][] grid;

    private Table table;

    public GridLayout(int rows, int cols){
        super();
        this.setFillParent(true);
        grid = new GridNode[rows][cols];
        table = new Table();
        table.setFillParent(true);
    }

    public void addGridActor(Actor actor, int row, int col,String xAnchor,String yAnchor){
        GridNode node = new GridNode();
        node.actor = actor;
        node.xAnchor = xAnchor;
        node.yAnchor = yAnchor;
        grid[row][col] = node;
    }

    @Override
    public void layout() {
        super.layout();
        super.addActor(table);
        table.clearChildren();
        table.left().bottom();
        table.setFillParent(true);
        for (int i = grid.length - 1; i >= 0; i--) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] != null) {
                    Cell<Actor> cell = table.add(grid[i][j].actor);
                    float minWidth = Math.max(grid[i][j].actor.getWidth(),getWidth()/grid[i].length);
                    float minHeight = Math.max(grid[i][j].actor.getHeight(),getHeight()/grid.length);;
                    cell.size(grid[i][j].actor.getWidth(),grid[i][j].actor.getHeight());
                    cell.minSize(minWidth,minHeight);
                    //cell.setActorHeight(getHeight()/grid.length);
                    //cell.setActorWidth(getWidth()/grid[i].length);
                    String xAnchor = grid[i][j].xAnchor;
                    String yAnchor = grid[i][j].yAnchor;
                    assert xAnchor!=null;
                    assert yAnchor!=null;
                    if(yAnchor.equals("center")||xAnchor.equals("center"))
                        cell.center();
                    switch (yAnchor) {
                        case "top":
                            cell.top();
                            break;
                        case "bottom":
                            cell.bottom();
                            break;
                        case "fill":
                            cell.fillY();
                            break;
                    }
                    switch (xAnchor) {
                        case "left":
                            cell.left();
                            break;
                        case "right":
                            cell.right();
                            break;
                        case "fill":
                            cell.fillX();
                            break;
                    }
                    //cell.left();
                }
                else {
                    Cell cell =table.add();
                    cell.expand();
                    //cell.size(getWidth()/grid[0].length,getHeight()/grid.length);
                }
            }
            table.row();
        }
    }

}
