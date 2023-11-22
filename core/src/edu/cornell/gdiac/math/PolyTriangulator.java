/*
 * PolyTriangulator.java
 *
 * This class is a better version of the LibGDX EarclippingTriangulator that supports holes and trims degenerate
 * triangles. Notice that triangulation is spread out over multiple methods: initialization, calculation, and
 * materialization. This is to enable this calculation to take place in a separate thread.
 *
 * We find that this is our general go-to-class for triangulation. While earclipping is an O(n^2) algorithm, the lower
 * overhead of this class makes it more performant in smaller applications.
 *
 * @author Walker White
 * @date   1/5/2023
 */
package edu.cornell.gdiac.math;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * A factory class for producing solid Poly2 objects from a set of vertices.
 *
 * For all but the simplist of shapes, it is important to have a triangulator
 * that can divide up the polygon into triangles for drawing. This class is an
 * implementation of the the ear clipping algorithm to triangulate polygons.
 * This algorithm supports complex polygons, namely those with interior holes
 * (but not self-crossings). All triangles produced are guaranteed to be
 * counter-clockwise.
 *
 * While LibGDX has an EarclippingTriangulator class, that class does not
 * support holes and it does not trim degenerate triangles, making this class
 * a superior choice for many applications. The running time of this algorithm 
 * is O(n^2), making it one of the slower triangulation methods available. 
 * However, it has very low overhead, making it better than the alternatives 
 * in simple cases.
 *
 * As with all factories, the methods are broken up into three phases:
 * initialization, calculation, and materialization. To use the factory, you
 * first set the data (in this case a set of vertices or another Poly2) with the
 * initialization methods. You then call the calculation method. Finally,
 * you use the materialization methods to access the data in several different
 * ways.
 *
 * This division allows us to support multithreaded calculation if the data
 * generation takes too long. However, note that this factory is not thread
 * safe in that you cannot access data while it is still in mid-calculation.
 */
public class PolyTriangulator {
    /** An intermediate class for processing vertices */
    private class Vertex {
        /** The index position of this vertex in the input set */
        public int index = 0;
        /** The vertex x-coordinate */
        public float xcoord = 0.0f;
        /** The vertex y-coordinate */
        public float ycoord = 0.0f;
        /** The (current) interior angle of this vertex */
        public float angle = 0.0f;
        /** Whether or not this vertex is (currently) an ear */
        public boolean eartip = false;
        /** Whether or not this vertex is (currently) active */
        public boolean active = false;
        /** The next vertex along this path */
        public Vertex next;
        /** The previous vertex along this path */
        public Vertex prev;

        /**
         * Initializes a default vertex
         */
        public Vertex() {
            // Everything else is Java default
            active = true;
        }

        /**
         * Initializes a vertex from the given input set
         *
         * The array is considered to be composed of alternating x and y values.
         * The pos refers to the vertex position, and so the x is 2*pos and the
         * y is at 2*pos+1.
         *
         * @param input The input set
         * @param pos   The vertex position
         */
        public Vertex(float[] input, int pos) {
            active = true;
            index = pos;
            xcoord = input[2*pos  ];
            ycoord = input[2*pos+1];
        }

        /**
         * Initializes a vertex from the given input set
         *
         * The array is considered to be composed of alternating x and y values.
         * The pos refers to the vertex position, and so the x is 2*pos and the
         * y is at 2*pos+1.
         *
         * @param input The input set
         * @param pos   The vertex position
         */
        public Vertex(FloatArray input, int pos) {
            active = true;
            index = pos;
            xcoord = input.items[2*pos  ];
            ycoord = input.items[2*pos+1];
        }

        /**
         * Sets this vertex to be the value of the given input set.
         *
         * The array is considered to be composed of alternating x and y values.
         * The pos refers to the vertex position, and so the x is 2*pos and the
         * y is at 2*pos+1.
         *
         * @param input The input set
         * @param pos   The vertex position
         */
        public void set(float[] input, int pos) {
            index = pos;
            xcoord = input[2*pos  ];
            ycoord = input[2*pos+1];
            angle = 0;
            eartip = false;
            active = true;
            next = null;
            prev = null;
        }

        /**
         * Sets this vertex to be the value of the given input set.
         *
         * The array is considered to be composed of alternating x and y values.
         * The pos refers to the vertex position, and so the x is 2*pos and the
         * y is at 2*pos+1.
         *
         * @param input The input set
         * @param pos   The vertex position
         */
        public void set(FloatArray input, int pos) {
            index = pos;
            xcoord = input.items[2*pos  ];
            ycoord = input.items[2*pos+1];
            angle = 0;
            eartip = false;
            active = true;
            next = null;
            prev = null;
        }

        /**
         * Copies this vertex into the specified location
         *
         * All values, including the next and prev pointers are copied.
         * This method is necessary for hole slicing.
         */
        public void copy(Vertex dst) {
            dst.index  = index;
            dst.xcoord  = xcoord;
            dst.ycoord  = ycoord;
            dst.angle  = angle;
            dst.eartip = eartip;
            dst.active = active;
            dst.next = next;
            dst.prev = prev;
        }

        /**
         * Returns true if this vertex has a convex interior angle.
         *
         * @return true if this vertex has a convex interior angle.
         */
        public boolean convex() {
            return convex(prev.xcoord,prev.ycoord,xcoord,ycoord,next.xcoord,next.ycoord);
        }

        /**
         * Returns true if the angle defined by the three points is convex.
         *
         * The defined angle is centered at p2, with p1 going into p2 and p2
         * going out to p3.
         *
         * @param p1x    The x-coordinate of the start of the angle
         * @param p1y    The y-coordinate of the start of the angle
         * @param p2x    The x-coordinate of the center of the angle
         * @param p2y    The y-coordinate of the center of the angle
         * @param p3x    The x-coordinate of the end of the angle
         * @param p3y    The y-coordinate of the end of the angle
         *
         * @return true if the angle defined by the three points is convex.
         */
        public boolean convex(float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
            float tmp = (p3y - p1y) * (p2x - p1x) - (p3x - p1x) * (p2y - p1y);
            return tmp > 0;
        }

        /**
         * Returns true if p is inside this vertex's ear region
         *
         * The ear region for a vertex is the triangle defined by it and its
         * two neighbors.
         *
         * @param px    The x-coordinate of the point to check
         * @param py    The y-coordinate of the point to check
         *
         * @return true if p is inside this vertex's ear region
         */
        public boolean inside(float px, float py) {
            if (convex(prev.xcoord, prev.ycoord, px, py, xcoord, ycoord)) { return false; }
            if (convex(xcoord, ycoord, px, py, next.xcoord, next.ycoord)) { return false; }
            if (convex(next.xcoord, next.ycoord, px, py, prev.xcoord, prev.ycoord)) { return false; }
            return true;
        }

        /**
         * Returns true if p is inside of the cone defined by this vertex
         *
         * The vertex cone is centered at this vertex and defined by the
         * lines (not line segments) to the two neighbors.
         *
         * @param px    The x-coordinate of the point to check
         * @param py    The y-coordinate of the point to check
         *
         * @return true if p is inside of the cone defined by this vertex
         */
        public boolean incone(float px, float py) {
            if (convex()) {
                return (convex(prev.xcoord, prev.ycoord, xcoord, ycoord, px, py) &&
                        convex(xcoord, ycoord, next.xcoord, next.ycoord, px, py));
            }
            return (convex(prev.xcoord, prev.ycoord, xcoord, ycoord, px, py) ||
                    convex(xcoord, ycoord, next.xcoord, next.ycoord, px, py));
        }

        /**
         * Updates this vertex to determine ear status
         *
         * This method should be called whenever an ear is clipped from the
         * vertex set.
         */
        public void update() {
            float v1x = prev.xcoord-xcoord;
            float v1y = prev.ycoord-ycoord;
            float v3x = next.xcoord-xcoord;
            float v3y = next.ycoord-ycoord;

            // Normalize each one
            float v1len2 = (float)Math.sqrt( v1x*v1x+v1y*v1y );
            if (v1len2 != 0) {
                v1x /= v1len2;
                v1y /= v1len2;
            }

            float v3len2 = (float)Math.sqrt( v3x*v3x+v3y*v3y );
            if (v3len2 != 0) {
                v3x /= v3len2;
                v3y /= v3len2;
            }

            angle = v1x * v3x + v1y * v3y;
            if (convex()) {
                eartip = true;
                Vertex curr = next.next;
                while (curr != prev) {
                    boolean test = curr.index != index && curr.index != prev.index && curr.index != next.index;
                    if (test && inside(curr.xcoord,curr.ycoord)) {
                        eartip = false;
                    }
                    curr = curr.next;
                }
            } else {
                eartip = false;
            }
        }
    }

    /** The vertices to process */
    private Vertex[] vertices;
    /** The number of vertices to process */
    private int  vertsize = 0;

    /** The number of points on the exterior */
    private int exterior = 0;
    /** The (raw) set of vertices to use in the calculation */
    private FloatArray input;
    /** The offset and size of the hole positions in the input */
    private IntArray holes;
    /** The output results of the triangulation */
    private ShortArray output;

    /** Whether or not the calculation has been run */
    private boolean calculated = false;

    /**
     * Creates a triangulator with no vertex data.
     */
    public PolyTriangulator() {
        input = new FloatArray();
        holes = new IntArray();
        output = new ShortArray();
    }

    /**
     * Creates a triangulator with the given vertex data.
     *
     * The path is assumed to be the outer hull, and does not include any
     * holes (which may be specified later). The vertex data is copied.
     * The triangulator does not retain any references to the original
     * data.
     *
     * @param points    The vertices to triangulate
     */
    public PolyTriangulator(float[] points) {
        input = new FloatArray();
        holes = new IntArray();
        output = new ShortArray();
        set(points);
    }

    /**
     * Creates a triangulator with the given vertex data.
     *
     * The path is assumed to be the outer hull, and does not include any
     * holes (which may be specified later). The vertex data is copied.
     * The triangulator does not retain any references to the original
     * data.
     *
     * @param points      The vertices to triangulate
     */
    public PolyTriangulator(FloatArray points) {
        input = new FloatArray();
        holes = new IntArray();
        output = new ShortArray();
        set(points);
    }

    /**
     * Creates a triangulator with the given vertex data.
     *
     * The path is assumed to be the outer hull, and does not include any
     * holes (which may be specified later). The vertex data is copied.
     * The triangulator does not retain any references to the original
     * data.
     *
     * @param path      The vertices to triangulate
     */
    public PolyTriangulator(Path2 path) {
        input = new FloatArray();
        holes = new IntArray();
        output = new ShortArray();
        set(path);
    }

    /**
     * Sets the exterior vertex data for this triangulator.
     *
     * The vertices are assumed to be the outer hull, and do not
     * include any holes (which may be specified later). The vertices
     * should define the hull in a counter-clockwise traversal.
     *
     * The vertex data is copied. The triangulator does not retain any
     * references to the original data. Hull points are added first.
     * That is, when the triangulation is computed, the lowest indices
     * all refer to these points, in the order that they were provided.
     *
     * This method resets all interal data. The triangulation is lost,
     * as well as any previously added holes. You will need to re-add
     * any lost data and reperform the calculation.
     *
     * @param points    The vertices to triangulate
     */
    public void set(float[] points) {
        set(points,0,points.length);
    }

    /**
     * Sets the exterior vertex data for this triangulator.
     *
     * The vertices are assumed to be the outer hull, and do not
     * include any holes (which may be specified later). The vertices
     * should define the hull in a counter-clockwise traversal.
     *
     * The vertex data is copied. The triangulator does not retain any
     * references to the original data. Hull points are added first.
     * That is, when the triangulation is computed, the lowest indices
     * all refer to these points, in the order that they were provided.
     *
     * This method resets all interal data. The triangulation is lost,
     * as well as any previously added holes. You will need to re-add
     * any lost data and reperform the calculation.
     *
     * @param points    The vertices to triangulate
     * @param offset    The offset into the array
     * @param length    The length of the array
     */
    public void set(float[] points, int offset, int length) {
        if (Path2.orientation( points, offset, length ) >= 0) {
            throw new IllegalArgumentException( "Path orientiation is not CCW" );
        }
        reset();
        exterior = length/2;
        input.addAll( points, offset, length );
    }

    /**
     * Sets the exterior vertex data for this triangulator.
     *
     * The vertices are assumed to be the outer hull, and do not
     * include any holes (which may be specified later). The vertices
     * should define the hull in a counter-clockwise traversal.
     *
     * The vertex data is copied. The triangulator does not retain any
     * references to the original data. Hull points are added first.
     * That is, when the triangulation is computed, the lowest indices
     * all refer to these points, in the order that they were provided.
     *
     * This method resets all interal data. The triangulation is lost,
     * as well as any previously added holes. You will need to re-add
     * any lost data and reperform the calculation.
     *
     * @param points    The vertices to triangulate
     */
    public void set(FloatArray points) {
        set(points.items,0,points.size);
    }

    /**
     * Sets the exterior vertex data for this triangulator.
     *
     * The path is assumed to be the outer hull, and does not include
     * any holes (which may be specified later). The path should define
     * the hull in a counter-clockwise traversal.
     *
     * The vertex data is copied. The triangulator does not retain any
     * references to the original data. Hull points are added first.
     * That is, when the triangulation is computed, the lowest indices
     * all refer to these points, in the order that they were provided.
     *
     * This method resets all interal data. The triangulation is lost,
     * as well as any previously added holes. You will need to re-add
     * any lost data and reperform the calculation.
     *
     * @param path    The vertices to triangulate
     */
    public void set(Path2 path) {
        if (path.orientation() >= 0 ) {
            throw new IllegalArgumentException( "Path orientiation is not CCW" );
        }
        reset();
        exterior = path.size();
        input.addAll(path.vertices);
    }

    /**
     * Adds the given hole to the triangulation.
     *
     * The hole is assumed to be a closed path with no self-crossings.
     * In addition, it is assumed to be inside the polygon outer hull, with
     * vertices ordered in clockwise traversal. If any of these is not true,
     * the results are undefined.
     *
     * The vertex data is copied. The triangulator does not retain any
     * references to the original data. Hole points are added after
     * the hull points, in order. That is, when the triangulation is
     * computed, if the hull is size n, then the hull points are
     * indices 0..n-1, while n is the index of a hole point.
     *
     * Any holes added to the triangulator will be lost if the exterior
     * polygon is changed via the {@link #set} method.
     *
     * @param points    The hole vertices
     */
    public void addHole(float[] points) {
        addHole( points, 0, points.length );
    }

    /**
     * Adds the given hole to the triangulation.
     *
     * The hole is assumed to be a closed path with no self-crossings.
     * In addition, it is assumed to be inside the polygon outer hull, with
     * vertices ordered in clockwise traversal. If any of these is not true,
     * the results are undefined.
     *
     * The vertex data is copied. The triangulator does not retain any
     * references to the original data. Hole points are added after
     * the hull points, in order. That is, when the triangulation is
     * computed, if the hull is size n, then the hull points are
     * indices 0..n-1, while n is the index of a hole point.
     *
     * Any holes added to the triangulator will be lost if the exterior
     * polygon is changed via the {@link #set} method.
     *
     * @param points    The hole vertices
     * @param offset    The offset into the array
     * @param length    The length of the array
     */
    public void addHole(float[] points, int offset, int length) {
        if (Path2.orientation( points, offset, length ) <= 0) {
            throw new IllegalArgumentException( "Hole orientiation is not CW" );
        }
        int size = input.size;
        holes.add(size);
        holes.add(length);
        input.addAll( points, offset, length );
    }

    /**
     * Adds the given hole to the triangulation.
     *
     * The hole is assumed to be a closed path with no self-crossings.
     * In addition, it is assumed to be inside the polygon outer hull, with
     * vertices ordered in clockwise traversal. If any of these is not true,
     * the results are undefined.
     *
     * The vertex data is copied. The triangulator does not retain any
     * references to the original data. Hole points are added after
     * the hull points, in order. That is, when the triangulation is
     * computed, if the hull is size n, then the hull points are
     * indices 0..n-1, while n is the index of a hole point.
     *
     * Any holes added to the triangulator will be lost if the exterior
     * polygon is changed via the {@link #set} method.
     *
     * @param points    The hole vertices
     */
    public void addHole(FloatArray points) {
        addHole( points.items, 0, points.size );
    }

    /**
     * Adds the given hole to the triangulation.
     *
     * The hole path should be a closed path with no self-crossings.
     * In addition, it is assumed to be inside the polygon outer hull,
     * with vertices ordered in clockwise traversal. If any of these is
     * not true, the results are undefined.
     *
     * The vertex data is copied. The triangulator does not retain any
     * references to the original data. Hole points are added after
     * the hull points, in order. That is, when the triangulation is
     * computed, if the hull is size n, then the hull points are
     * indices 0..n-1, while n is the index of a hole point.
     *
     * Any holes added to the triangulator will be lost if the exterior
     * polygon is changed via the {@link #set} method.
     *
     * @param path      The hole path
     */
    public void addHole(Path2 path) {
        if (path.orientation() <= 0 ) {
            throw new IllegalArgumentException( "Path orientiation is not CW" );
        }
        int size = input.size;
        holes.add(size);
        holes.add(path.vertices.length);
        input.addAll(path.vertices);
    }

    /**
     * Clears all internal data, but still maintains the initial vertex data.
     *
     * This method also retains any holes. It only clears the triangulation results.
     */
    public void reset() {
        vertsize = 0;
        output.clear();
        calculated = false;
    }

    /**
     * Clears all internal data, including the initial vertex data.
     *
     * When this method is called, you will need to set a new vertices before
     * calling calculate. In addition, any holes will be lost as well.
     */
    public void clear() {
        reset();
        input.clear();
        holes.clear();
    }

    /**
     * Performs a triangulation of the current vertex data.
     */
    public void calculate() {
        reset();
        if (exterior > 0) {
            allocateVertices();
            removeHoles();
            computeTriangles();
        }
        calculated = true;
    }

    /**
     * Returns an array of indices representing the triangulation.
     *
     * The indices represent positions in the original vertex list, which
     * included holes as well. Positions are ordered as follows: first the
     * exterior hull, and then all holes in order.
     *
     * The triangulator does not retain a reference to the returned array;
     * it is safe to modify it. If the calculation is not yet performed,
     * this method will return the empty list.
     *
     * @return an array of indices representing the triangulation.
     */
    public short[] getTriangulation() {
        short[] result = new short[output.size];
        System.arraycopy( output.items, 0, result, 0, output.size );
        return result;
    }

    /**
     * Stores the triangulation indices in the given buffer.
     *
     * The indices represent positions in the original vertex list, which
     * included both holes and Steiner points. Positions are ordered as
     * follows: first the exterior hull, then all holes in order, and
     * finally the Steiner points.
     *
     * The indices will be appended to the provided array. You should clear
     * the array first if you do not want to preserve the original data.
     * If the calculation is not yet performed, this method will do nothing.
     *
     * @param buffer    The buffer to store the triangulation indices
     *
     * @return the number of elements added to the buffer
     */
    public int getTriangulation(ShortArray buffer) {
        buffer.addAll( output );
        return output.size;
    }

    /**
     * Returns a polygon representing the triangulation.
     *
     * This polygon is the proper triangulation, constrained to the interior
     * of the polygon hull. It contains the vertices of the exterior polygon,
     * as well as any holes.
     *
     * The triangulator does not maintain references to this polygon and it
     * is safe to modify it. If the calculation is not yet performed, this
     * method will return the empty polygon.
     *
     * @return a polygon representing the triangulation.
     */
    public Poly2 getPolygon() {
        Poly2 poly = new Poly2(  );
        if (calculated) {
            poly.vertices = new float[input.size];
            System.arraycopy( input.items, 0, poly.vertices, 0, input.size );
            poly.indices  = new short[output.size];
            System.arraycopy( output.items, 0, poly.indices, 0 , output.size );
        }
        return poly;

    }

    /**
     * Stores the triangulation in the given buffer.
     *
     * The polygon produced is the proper triangulation, constrained to the
     * interior of the polygon hull. It contains the vertices of the exterior
     * polygon, as well as any holes.
     *
     * This method will append the vertices to the given polygon. If the buffer
     * is not empty, the indices will be adjusted accordingly. You should clear
     * the buffer first if you do not want to preserve the original data.
     *
     * If the calculation is not yet performed, this method will do nothing.
     *
     * @param buffer    The buffer to store the triangulated polygon
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 getPolygon(Poly2 buffer) {
        if (calculated) {
            int offset = buffer.vertices.length;
            if (offset > 0) {
                float[] array1 = new float[offset+input.size];
                System.arraycopy( buffer.vertices, 0, array1, 0, offset );
                System.arraycopy( input.items, 0, array1, offset, input.size );

                short[] array2 = new short[buffer.indices.length+output.size];
                System.arraycopy( buffer.indices, 0, array2, 0, buffer.indices.length );
                System.arraycopy( output.items, 0, array2, buffer.indices.length, output.size );

                buffer.vertices = array1;
                buffer.indices = array2;
            } else {
                buffer.vertices = new float[input.size];
                System.arraycopy( input.items, 0, buffer.vertices, 0, input.size );
                buffer.indices  = new short[output.size];
                System.arraycopy( output.items, 0, buffer.indices, 0 , output.size );
            }
        }
        return buffer;
    }

    /**
     * Allocates the doubly-linked list(s) to manage the vertices
     */
    private void allocateVertices() {
        int holepos = -1;
        int needed = input.size/2+2*holes.size;
        if (vertices == null || needed > vertices.length) {
            vertices = new Vertex[needed];
            for(int ii = 0; ii < needed; ii++) {
                vertices[ii] = new Vertex();
            }
        }
        vertsize = 0;
        for(int pos = 0; pos < input.size/2; pos++) {
            vertices[pos].set(input,pos);
            int start = holepos == -1 ? 0 : holes.items[2*holepos]/2;
            int end   = holepos == -1 ? exterior : (holes.items[2*holepos]+holes.items[2*holepos+1])/2;
            if (pos == start) {
                vertices[pos].prev = vertices[end-1];
            } else {
                vertices[pos].prev = vertices[pos-1];
            }
            if (pos == end-1) {
                holepos++;
                vertices[pos].next = vertices[start];
            } else {
                vertices[pos].next = vertices[pos+1];
            }
        }
        vertsize = input.size/2;
    }

    /**
     * Slices out holes, merging vertices into one doubly-linked list
     */
    private void removeHoles() {
        if (holes.size == 0) {
            return;
        }

        int   holessize = holes.size/2;
        int[] holesleft = new int[2*holessize];
        System.arraycopy( holes.items, 0, holesleft, 0, 2*holessize );

        while (holessize > 0) {
            // Find the hole point with the largest x.
            boolean hasholes = false;
            int holepart = 0;
            int holeindx = 0;
            for(int ii = 0; ii < holessize; ii++) {
                if (!hasholes) {
                    hasholes = true;
                    holepart = ii;
                    holeindx = holesleft[2*ii];
                }
                for(int jj = 0; jj < holesleft[2*ii+1]; jj++) {
                    int index = (holesleft[2*ii]+jj)/2;
                    if (vertices[index].xcoord > vertices[holeindx/2].xcoord) {
                        holepart = ii;
                        holeindx = holesleft[2*ii]+jj;
                    }
                }
            }


            Vertex holepoint = vertices[holeindx/2];
            Vertex bestpoint = null;

            // Find the point on the exterior to connect it to
            boolean repeat = true;
            Vertex curr = vertices[0];
            while (repeat || curr != vertices[0]) {
                if (curr.xcoord > holepoint.xcoord && curr.incone(holepoint.xcoord,holepoint.ycoord)) {
                    boolean checkit = true;

                    if (bestpoint != null) {
                        float v1x = curr.xcoord-holepoint.xcoord;
                        float v1y = curr.ycoord-holepoint.ycoord;
                        float v1n = (float)Math.sqrt( v1x*v1x+v1y*v1y );
                        if (v1n != 0.0f) {
                            v1x /= v1n;
                        }

                        float v2x = bestpoint.xcoord-holepoint.xcoord;
                        float v2y = bestpoint.ycoord-holepoint.ycoord;
                        float v2n = (float)Math.sqrt( v2x*v2x+v2y*v2y );
                        if (v2n != 0.0f) {
                            v2x /= v2n;
                        }
                        if (v2x > v1x) {
                            checkit = false;
                        }
                    }
                    if (checkit) {
                        boolean pointvisible = true;
                        for(int ii = 0; pointvisible && ii < holessize; ii++) {
                            for(int jj = 0; pointvisible && jj < holesleft[2*ii+1]; jj++) {
                                int checkindx = (holesleft[2*ii]+jj)/2;
                                Vertex checkpt = vertices[checkindx];
                                Vertex nextpt  = checkpt.next;
                                if (intersects(holepoint.xcoord, holepoint.ycoord, curr.xcoord, curr.ycoord,
                                        checkpt.xcoord, checkpt.ycoord, nextpt.xcoord, nextpt.ycoord)) {
                                    pointvisible = false;
                                }
                            }
                        }
                        if (pointvisible) {
                            bestpoint = curr;
                        }
                    }
                }
                repeat = false;
                curr = curr.next;
            }

            if (bestpoint == null) {
                return;
            }

            // Copy the split points
            Vertex holecopy = vertices[vertsize];
            Vertex bestcopy = vertices[vertsize+1];
            vertsize += 2;

            holepoint.copy(holecopy);
            bestpoint.copy(bestcopy);

            // Split at the divider
            bestpoint.prev.next = bestcopy;
            holepoint.prev.next = holecopy;

            bestpoint.prev = holecopy;
            holecopy.next = bestpoint;
            bestcopy.next = holepoint;
            holepoint.prev = bestcopy;

            // Remove this hole
            holesleft[2*holepart  ] = holesleft[2*holessize-2];
            holesleft[2*holepart+1] = holesleft[2*holessize-1];
            holessize--;
        }
    }

    /**
     * Computes the triangle indices for the active vertices.
     */
    private void computeTriangles() {
        // Degenerate case
        if (vertsize == 3) {
            output.add(0);
            output.add(1);
            output.add(2);
            return;
        }

        // Find some initial ears
        for(int ii = 0; ii < vertsize; ii++) {
            vertices[ii].update();
        }

        output.ensureCapacity(3*(vertsize-3));
        for(int ii = 0; ii < vertsize-3; ii++) {
            Vertex bestear = null;
            // Find the most extruded ear.
            for(int jj = 0; jj < vertsize; jj++) {
                Vertex v = vertices[jj];
                if (v.active && v.eartip) {
                    if (bestear == null) {
                        bestear = v;
                    } else if (v.angle > bestear.angle) {
                        bestear = v;
                    }
                }
            }

            if (bestear == null) {
                System.out.println("Could not find a suitable ear");
                return;
            }

            output.add(bestear.prev.index);
            output.add(bestear.index);
            output.add(bestear.next.index);

            // Cut off the ear
            bestear.active = false;
            bestear.prev.next = bestear.next;
            bestear.next.prev = bestear.prev;

            if (ii != vertsize - 4) {
                bestear.prev.update();
                bestear.next.update();
            }
        }

        // Scan for first missing ear
        boolean open = true;
        for(int ii = 0; open && ii < vertsize; ii++) {
            if (vertices[ii].active) {
                output.add(vertices[ii].prev.index);
                output.add(vertices[ii].index);
                output.add(vertices[ii].next.index);
                open = false;
            }
        }
    }

    /**
     * Returns true if the lines defined by the two pairs of vectors intersect
     *
     * @param p11x  The x-coordinate of the first vector of first line
     * @param p11y  The y-coordinate of the first vector of first line
     * @param p12x  The x-coordinate of the second vector of first line
     * @param p12y  The y-coordinate of the second vector of first line
     * @param p21x  The x-coordinate of the first vector of second line
     * @param p21y  The y-coordinate of the first vector of second line
     * @param p22x  The x-coordinate of the second vector of second line
     * @param p22y  The y-coordinate of the second vector of second line
     *
     * @return true if the lines defined by the two pairs of vectors intersect
     */
    private static boolean intersects(float p11x, float p11y, float p12x, float p12y,
                                      float p21x, float p21y, float p22x, float p22y) {
        if (    (p11x == p21x && p11y == p21y) ||
                (p11x == p22x && p11y == p22y) ||
                (p12x == p21x && p12y == p21y) ||
                (p12x == p22x && p12y == p22y)) {
            return false;
        }

        float v1ortx = p12y-p11y;
        float v1orty = p11x-p12x;
        float v2ortx = p22y-p21y;
        float v2orty = p21x-p22x;
        float dot21 = v1ortx*(p21x-p11x)+v1orty*(p21y-p11y);
        float dot22 = v1ortx*(p22x-p11x)+v1orty*(p22y-p11y);
        float dot11 = v2ortx*(p11x-p21x)+v2orty*(p11y-p21y);
        float dot12 = v2ortx*(p12x-p21x)+v2orty*(p12y-p21y);

        return !(dot11 * dot12 > 0 || dot21 * dot22 > 0);
    }
}
