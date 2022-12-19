package edu.cornell.gdiac.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import org.graalvm.compiler.hotspot.stubs.DivisionByZeroExceptionStub;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;

/**
 * This class represents a constraint Layout which allows for the declaration of constraints for layout management.
 */
public class ConstraintLayout extends WidgetGroup {

    /**
     * This class represents a constraint in the ConstraintLayout.
     */
    private class Constrain{
        /** The specifier in script */
        private String handle;
        /** The expressions binded to the contraint */
        public String[] expressions;
        /** The current value of constraint solving */
        public float currVal;
        /** Setters for putting values back */
        private String setters;

        /**
         * Creates a new constraint with the given handle and setter.
         *
         * @param handle The specifier of the constraint
         * @param expressions The expressions binded to the constraint
         */
        public Constrain(String handle, String expressions){
            this.handle = handle;
            this.expressions = expressions.split(";");
            this.setters = "";
            this.currVal = 0f;
        }

        public void addSetter(String setter){
            this.setters += "\n"+setter;
        }

        /**
         * Returns the sum of Linear Loss of the constraint with respect to the expressions.
         */
        public float getLoss(float value) throws ScriptException {
            float loss = 0;
            for (String str:expressions) {
                if(str.isEmpty())
                    continue;
                Object o = engine.eval(str);
                if(o instanceof Float || o instanceof Integer || o instanceof Double){
                    loss += value - ((Number) o).floatValue();
                }
            }
            return Math.abs(loss);
        }

        public float getLoss() throws ScriptException {
            return getLoss(currVal);
        }

        /**
         * Sets {@link #currVal} to the linked setters;
         * Requires the engine to be configured with the setter handles
         */
        public void applyVal() throws ScriptException {
            String temp = setters.replaceAll("__value__",currVal+"");
            engine.eval(temp);
        }
    }

    private ScriptEngine engine;
    private ArrayList<Constrain> constraints;

    private static final boolean DEBUG = false;

    @Override
    public void addActor(Actor actor){}

    public void addConstraintActor(Actor actor, String actorName, String bindings) throws ScriptException {
        super.addActor(actor);
        addSetter(actorName,bindings);
    }

    /**
     * Creates a new ConstraintLayout with the given script.
     *
     * @param script The script to be used for the layout
     * @throws ScriptException If the script is invalid
     */
    public ConstraintLayout(ScriptEngineManager manager, String script) throws ScriptException {
        super();
        this.constraints = parseConstraints(script);
        this.engine = manager.getEngineByName("nashorn");
        setBindings();
        this.setFillParent(true);

    }

    /** put the bindings into the engine */
    private void setBindings(){
        for (Constrain c:constraints) {
            engine.put(c.handle,c.currVal);
        }
    }

    /**
     * Parses the constraints from the given script.
     *
     * Constraints follow the syntax: { "handle:expression:hint"}
     *
     * @param script
     * @return
     * @throws ScriptException
     */
    private ArrayList<Constrain> parseConstraints(String script) throws ScriptException {
        ArrayList<Constrain> constraints = new ArrayList<>();
        //parse the constraint definitions
        for(String str:script.split("\n")){
            if(str.contains(":")){
                String[] parts = str.split(":");
                String handle = parts[0].trim();
                //check if the handle already exists
                Constrain c = null;
                for(Constrain con:constraints){
                    if(con.handle.equals(handle)){
                        c = con;
                        break;
                    }
                }
                if(c == null){
                    // add a new constraint if it doesn't exist
                    c = new Constrain(handle,parts[1]);
                    if(parts.length > 2){
                        // set the current value with hint
                        c.currVal = Float.parseFloat(parts[2]);
                    }
                    constraints.add(c);
                }else{
                    throw new ScriptException("Duplicate handle: "+handle);
                }
            }
            else
                throw new ScriptException("Invalid Constraint Definition");
        }
        return constraints;
    }

    /** Adds a setter to the constraint with the given handle */
    public void addSetter(String actorName,String bindings) throws ScriptException {
        for(String str:bindings.split("\n")){
            if(str.contains(":")){
                String[] parts = str.split(":");
                for(int i = 0; i < scriptAttributes.length; i++){
                    if(parts[0].equals(scriptAttributes[i])){
                        String handle = parts[1];
                        for(Constrain c:constraints){
                            if(c.handle.equals(handle)){
                                if (DEBUG)
                                    System.out.println("Adding setter: "+actorName+"."+scriptAttributes[i]+" = __value__");
                                c.addSetter(actorName+scriptSetter[i]);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            else
                throw new ScriptException("Invalid Constraint Definition");
        }
    }

    private static String[] scriptAttributes = new String[]{
            "width","height","x","y","scaleX","scaleY","rotation"
    };

    private static String[] scriptLink = new String[]{
            ".getWidth()",".getHeight()",".getX()",".getY()",".getScaleX()",".getScaleY()",".getRotation()"
    };

    private static String[] scriptSetter = new String[]{
            ".setWidth(__value__)",".setHeight(__value__)",".setX(__value__)",".setY(__value__)",".setScaleX(__value__)",".setScaleY(__value__)",".setRotation(__value__)"
    };

    /*private static String bindingConversion(String actorName, String str){
        for(int i = 0; i < scriptAttributes.length; i++){
            str = str.replaceAll(scriptAttributes[i],scriptLink[i]);
        }
        return str;
    }

    private String augmentScript(Actor node, String nodeName, String script){
        String[] lines = script.split("\n");
        StringBuilder returnString = new StringBuilder();
        for(String str:lines){
            String[] split = str.split(":");
            for(int i = 0; i < scriptAttributes.length; i++){
                //split[1] = split[1].replaceAll("[,:.(){}\\[\\] ]"+scriptAttributes[i]+"[,:.(){}\\[\\] ]",nodeName+scriptLink[i]);
                split[1] = split[1].replaceAll(scriptAttributes[i],nodeName+scriptLink[i]);
            }
            switch(split[0]){
                case "width":
                    str = nodeName+".setWidth("+split[1]+");";
                    break;
                case "height":
                    str = nodeName+".setHeight("+split[1]+");";
                    break;
                case "x":
                    str = nodeName+".setX("+split[1]+");";
                    break;
                case "y":
                    str = nodeName+".setY("+split[1]+");";
                    break;
                case "scaleX":
                    str = nodeName+".setScaleX("+split[1]+");";
                    break;
                case "scaleY":
                    str = nodeName+".setScaleY("+split[1]+");";
                    break;
                case "rotation":
                    str = nodeName+".setRotation("+split[1]+");";
                    break;
            }
            returnString.append(str).append("\n");
        }
        return returnString.toString();
    }*/

    private float getLoss() throws ScriptException {
        float loss = 0;
        for(Constrain c:constraints){
            loss += c.getLoss();
        }
        return loss;
    }

    private double computeGrad(int index, double delta) throws ScriptException {
        double loss = getLoss();
        constraints.get(index).currVal += delta;
        double loss_new = getLoss();
        constraints.get(index).currVal -= delta;
        return (loss_new-loss)/delta;
    }

    private float[] computeGradient() throws ScriptException {
        float[] grad = new float[constraints.size()];
        for(int i = 0; i < grad.length; i++){
            grad[i] = (float)computeGrad(i,1e-2);
            if (DEBUG)
                System.out.println("Value: "+constraints.get(i).currVal+" Grad: "+grad[i]+" Loss: "+constraints.get(i).getLoss());
        }
        return grad;
    }

    private final float ADAM_BETA1 = 0.9f, ADAM_BETA2 = 0.999f, ADAM_EPSILON = 1e-8f;

    /** perform gradient descent on the specified function */
    private void adamGrad(int maxIter,float learningRate){
        float[] m = new float[constraints.size()];
        float[] v = new float[constraints.size()];
        for(int i = 0; i < maxIter; i++) {
            float[] grad = null;
            try {
                grad = computeGradient();
            } catch (ScriptException e) {
                e.printStackTrace();
            }
            try {
                if (DEBUG)
                    System.out.println("Iteration: "+i+" Loss: "+getLoss());
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
            for (int j = 0; j < grad.length; j++) {
                Constrain c = constraints.get(j);
                m[j] = ADAM_BETA1 * m[j] + (1 - ADAM_BETA1) * grad[j];
                v[j] = ADAM_BETA2 * v[j] + (1 - ADAM_BETA2) * grad[j] * grad[j];
                float m_hat = m[j] / (1 - ADAM_BETA1);
                float v_hat = v[j] / (1 - ADAM_BETA2);
                c.currVal -= learningRate * m_hat / (float) Math.sqrt(v_hat + ADAM_EPSILON);
            }
            setBindings();
            try {
                if(getLoss() < 1)
                    break;
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void layout() {
        if(DEBUG)
            System.out.println("Layout");
        long start = System.currentTimeMillis();
        engine.put("width",this.getWidth());
        engine.put("height",this.getHeight());
        adamGrad(2000,10f);
        if (DEBUG)
            System.out.println("Time taken: "+(System.currentTimeMillis()-start));
        try {
            for (Constrain c : constraints) {
                c.applyVal();
                if(DEBUG)
                    System.out.println(c.handle + " " + c.currVal);
            }
        }
        catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
