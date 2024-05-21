package edu.cornell.gdiac.render;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * This class defines a general purpose scissor mask.
 *
 * A scissor mask is used to prevent portions of a 2d shape from showing. The
 * mask is a transformed rectangle, and any pixel outside of this region is
 * dropped. Unlike {@link CUGradient}, a scissor is applied to a region of the
 * framebuffer and is not a texture that can be applied to a surface. Therefore,
 * the scissor mask region must be defined in terms of pixels (or at least in
 * the same coordinate system as the vertices it is masking).
 *
 * A scissor mask is defined by three values (in terms of largest to smallest data):
 *
 * <ul>
 *    <li>An affine transform (for offset and rotation)</li>
 *    <li>A size vector for the extent</li>
 *    <li>A "fringe" value for edge aliasing</li>
 * </ul>
 *
 * Unpacking this data into std140 format is a 16 element array of floats (the
 * fringe is expanded into a per-axis value for the shader). And this is the format
 * that this data is represented in the {@link #getData} method so that it can be
 * passed to a {@link CUUniformBuffer} for improved performance. It is also possible
 * to get access to the individual components of the scissor mask, to pass them
 * to a shader directly (though the transform must be inverted first if it is passed
 * directly).
 *
 * Scissor masks can be intersected.  However, a scissor mask must always be a
 * transformed rectangle, and not all quadrilateral intersections are guaranteed
 * to be transformed rectangles.  Therefore, these intersections are always an
 * an approximation, with the intersecting scissor mask converted into an
 * axis-aligned rectangle in the coordinate space of the current scissor mask.
 */
public class CUScissor {
    /** The primary scissor transform (for OpenGL) */
    private Affine2 scissor;
    /** The inverse scissor transform (for OpenGL)  */
    private Affine2 inverse;

    /** The coordinate space transform (for intersections) */
    private Affine2 transform;
    /** The scissor bounds */
    private Rectangle bounds;
    /** The anti-aliasing fringe */
    private float fringe;

    /**
     * Creates a degenerate scissor of size 0.
     */
    public CUScissor() {
        scissor = new Affine2();
        inverse = new Affine2();
        transform = new Affine2();
        bounds = new Rectangle();
        setZero();
    }

    /**
     * Creates a copy with the resources of the given scissor mask.
     *
     * The original scissor mask is no longer safe to use after calling this
     * constructor.
     *
     * @param mask    The scissor mask to take from
     */
    public CUScissor(CUScissor mask) {
        set(mask);
    }

    /**
     * Initializes a scissor with the given bounds and fringe.
     *
     * The fringe is the size of the scissor border in pixels.  A value less than
     * 0 gives a sharp transition, where larger values have more gradual transitions.
     *
     * @param rect      The scissor mask bounds
     * @param fringe    The size of the scissor border in pixels
     *
     * @return true if initialization was successful.
     */
    public CUScissor(Rectangle rect, float fringe) {
        this();
        set(rect,fringe);
    }

    /**
     * Initializes a scissor with the given transformed bounds and fringe.
     *
     * The fringe is the size of the scissor border in pixels.  A value less than
     * 0 gives a sharp transition, where larger values have more gradual transitions.
     *
     * @param rect      The scissor mask bounds
     * @param aff       The scissor mask transform
     * @param fringe    The size of the scissor border in pixels
     *
     * @return true if initialization was successful.
     */
    public CUScissor(Rectangle rect, Affine2 aff, float fringe) {
        this();
        set(rect, aff, fringe);
    }

    /**
     * Initializes a scissor with the given transformed bounds and fringe.
     *
     * The fringe is the size of the scissor border in pixels.  A value less than
     * 0 gives a sharp transition, where larger values have more gradual transitions.
     *
     * @param rect      The scissor mask bounds
     * @param mat       The scissor mask transform
     * @param fringe    The size of the scissor border in pixels
     *
     * @return true if initialization was successful.
     */
    public CUScissor(Rectangle rect, Matrix4 mat, float fringe) {
        this();
        set(rect,mat,fringe);
    }

    //region Setters
    /**
     * Sets this to be a degenerate scissor of size 0.
     *
     * All pixels will be dropped by this mask.
     *
     * @return this scissor mask, returned for chaining
     */
    public CUScissor setZero() {
        inverse.idt();
        transform.idt();
        bounds.set(0,0,0,0);
        fringe = 1;
        return this;
    }

    /**
     * Sets this scissor mask to be a copy of the given one.
     *
     * @param mask    The scissor mask to copy
     *
     * @return this scissor mask, returned for chaining
     */
    public CUScissor set(CUScissor mask) {
        scissor = mask.scissor;
        inverse = mask.inverse;
        transform = mask.transform;
        bounds = mask.bounds;
        fringe = mask.fringe;
        return this;
    }

    /**
     * Sets the scissor mask to have the given bounds and fringe.
     *
     * Any previous transforms are dropped when this method is called.
     *
     * The fringe is the size of the scissor border in pixels.  A value less than
     * 0 gives a sharp transition, where larger values have more gradual transitions.
     *
     * @param rect         The scissor mask bounds
     * @param fringe    The size of the scissor border in pixels
     *
     * @return this scissor mask, returned for chaining
     */
    public CUScissor set(Rectangle rect, float fringe) {
        transform.idt();
        bounds = rect;
        this.fringe = fringe;
        recompute();
        return this;
    }

    /**
     * Sets the scissor mask to have the given transformed bounds and fringe.
     *
     * Any previous transforms are dropped when this method is called.
     *
     * The fringe is the size of the scissor border in pixels.  A value less than
     * 0 gives a sharp transition, where larger values have more gradual transitions.
     *
     * @param rect      The scissor mask bounds
     * @param aff       The scissor mask transform
     * @param fringe    The size of the scissor border in pixels
     *
     * @return this scissor mask, returned for chaining
     */
    public CUScissor set(Rectangle rect, Affine2 aff, float fringe) {
        transform = aff;
        bounds = rect;
        this.fringe = fringe;
        recompute();
        return this;
    }

    /**
     * Sets the scissor mask to have the given transformed bounds and fringe.
     *
     * Any previous transforms are dropped when this method is called.
     *
     * The fringe is the size of the scissor border in pixels.  A value less than
     * 0 gives a sharp transition, where larger values have more gradual transitions.
     *
     * @param rect      The scissor mask bounds
     * @param mat       The scissor mask transform
     * @param fringe    The size of the scissor border in pixels
     *
     * @return this scissor mask, returned for chaining
     */
    public CUScissor set(Rectangle rect, Matrix4 mat, float fringe) {
        transform.set(mat);
        bounds = rect;
        this.fringe = fringe;
        recompute();
        return this;
    }
    //endregion

    //region Attributes
    /**
     * Returns (a copy of) the bounding box of this scissor mask
     *
     * The bounding box is axis-aligned.  It ignored the transform component
     * of the scissor mask.
     *
     * @return the bounding box of this scissor mask
     */
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    /**
     * Sets the bounding box of this scissor mask
     *
     * The bounding box is axis-aligned. It ignores the transform component
     * of the scissor mask.
     *
     * @param bounds    The bounding box of this scissor mask
     */
    public void setBounds(Rectangle bounds) {
        this.bounds.set(bounds);
        recompute();
    }

    /**
     * Returns (a copy of) the transform component of this scissor mask
     *
     * If the scissor mask is not rotated or otherwise transformed, this
     * value will be the identity.
     *
     * This value only contains the transform on the scissor mask bounding
     * box.  It is not the same as the scissor matrix in a scissor shader.  Do
     * not pass this information directly to the shader.  Use either the method
     * {@link #getData} or {@link #getComponents} depending on whether or not you
     * need std140 representation.
     *
     * @return the transform component of this scissor mask
     */
    public Affine2 getTransform() {
        return new Affine2(transform);
    }

    /**
     * Sets the transform component of this scissor mask
     *
     * If the scissor mask is not rotated or otherwise transformed, this
     * value should be the identity.
     *
     * This value only contains the transform on the scissor mask bounding
     * box.  It is not the same as the scissor matrix in a scissor shader.  Do
     * not pass this information directly to the shader.  Use either the method
     * {@link #getData} or {@link #getComponents} depending on whether or not you
     * need std140 representation.
     *
     * @param transform The transform component of this scissor mask
     */
    public void setTransform(Affine2 transform) {
        this.transform = transform;
        recompute();
    }

    /**
     * Sets the transform component of this scissor mask
     *
     * If the scissor mask is not rotated or otherwise transformed, this
     * value should be the identity.
     *
     * This value only contains the transform on the scissor mask bounding
     * box.  It is not the same as the scissor matrix in a scissor shader.  Do
     * not pass this information directly to the shader.  Use either the method
     * {@link #getData} or {@link #getComponents} depending on whether or not you
     * need std140 representation.
     *
     * @param transform The transform component of this scissor mask
     */
    public void setTransform(Matrix4 transform) {
        this.transform.set(transform);
        recompute();
    }

    /**
     * Returns the edge fringe of this scissor mask
     *
     * The fringe is the size of the scissor border in pixels.  A value less than
     * 0 gives a sharp transition, where larger values have more gradual transitions.
     *
     * @return the edge fringe of this scissor mask
     */
    public float getFringe() {
        return fringe;
    }

    /**
     * Sets the edge fringe of this scissor mask
     *
     * The fringe is the size of the scissor border in pixels.  A value less than
     * 0 gives a sharp transition, where larger values have more gradual transitions.
     *
     * @param fringe    The edge fringe of this scissor mask
     */
    public void setFringe(float fringe) {
        this.fringe = fringe;
    }

    //endregion

    //region Transforms
    /**
     * Applies a rotation to this scissor mask.
     *
     * The rotation is in radians, counter-clockwise about the given axis.
     *
     * @param angle The angle (in radians).
     *
     * @return This scissor mask, after rotation.
     */
    public CUScissor rotate(float angle) {
        transform.rotate(angle);
        recompute();
        return this;
    }

    /**
     * Applies a uniform scale to this scissor mask.
     *
     * @param value The scalar to multiply by.
     *
     * @return This scissor mask, after scaling.
     */
    public CUScissor scale(float value) {
        if (value == 0) {
            setZero();
        } else {
            transform.scale(value, value);
        }
        recompute();
        return this;
    }

    /**
     * Applies a non-uniform scale to this scissor mask.
     *
     * @param s        The vector storing the individual scaling factors
     *
     * @return This scissor mask, after scaling.
     */
    public CUScissor scale(Vector2 s) {
        if (s.x == 0 || s.y == 0) {
            setZero();
        } else {
            transform.scale(s);
        }
        recompute();
        return this;
    }

    /**
     * Applies a non-uniform scale to this scissor mask.
     *
     * @param sx    The amount to scale along the x-axis.
     * @param sy    The amount to scale along the y-axis.
     *
     * @return This scissor mask, after scaling.
     */
    public CUScissor scale(float sx, float sy) {
        if (sx == 0 || sy == 0) {
            setZero();
        } else {
            transform.scale(sx, sy);
        }
        recompute();
        return this;
    }

    /**
     * Applies a translation to this gradient.
     *
     * @param t     The vector storing the individual translation offsets
     *
     * @return This scissor mask, after translation.
     */
    public CUScissor translate(Vector2 t) {
        transform.translate(t);
        recompute();
        return this;
    }

    /**
     * Applies a translation to this gradient.
     *
     * @param tx    The translation offset for the x-axis.
     * @param ty    The translation offset for the y-axis.
     *
     * @return This scissor mask, after translation.
     */
    public CUScissor translate(float tx, float ty) {
        transform.translate(tx, ty);
        recompute();
        return this;
    }

    /**
     * Applies the given transform to this scissor mask.
     *
     * This transform is applied after the existing gradient transform
     * (which is natural, since the transform defines the gradient shape).
     * To pre-multiply a transform, set the transform directly.
     *
     * @param mat The matrix to multiply by.
     *
     * @return A reference to this (modified) scissor mask for chaining.
     */
    public CUScissor mul(Matrix4 mat) {
        Affine2 aff = new Affine2();
        aff.set(mat);
        return mul(aff);
    }

    /**
     * Applies the given transform to this scissor mask.
     *
     * This transform is applied after the existing gradient transform
     * (which is natural, since the transform defines the gradient shape).
     * To pre-multiply a transform, set the transform directly.
     *
     * @param mat The matrix to multiply by.
     *
     * @return A reference to this (modified) scissor mask for chaining.
     */
    public CUScissor mul(Affine2 mat) {
        transform.mul(mat);
        recompute();
        return this;
    }

    //endregion

    //region Scissor Intersection

    /**
     * Temporary function to replace the intersect method from the C++ Rectangle class.
     * In the future, would be desirable to create a custom Rectangle class.
     * @param myRect
     * @param rect
     */
    private void rectIntersect(Rectangle myRect, Rectangle rect) {
        // Assumes that width and height for Rectangle object are always positive
        float minX = Math.max(myRect.getX(), rect.getX());
        float minY = Math.max(myRect.getY(), rect.getY());
        float maxX = Math.min(myRect.getX()+myRect.getWidth(), rect.getX()+rect.getWidth());
        float maxY = Math.min(myRect.getY() + myRect.getHeight(), rect.getY()+rect.getHeight());
        if (maxX - minX < 0 || maxY - minY < 0) {
            minX = maxX = minY = maxY = 0;
        }
        myRect.set(minX, minY, maxX-minX, maxY-minY);
    }

    /**
     * Temporary function to replace the transform method from the C++ Rectangle class.
     * In the future, would be desirable to create a custom Rectangle class.
     * @param myRect
     * @param transform
     */
    private void rectTransform(Rectangle myRect, Affine2 transform) {
        Affine2 trans = new Affine2();
        Vector2 off = new Vector2();
        myRect.getCenter(off); // Equivalent to origin + size/2?
        trans.translate(off);
        trans.mul(transform); // Assume *= in C++ is mul

        Vector2 size = new Vector2();
        size = myRect.getSize(size);
        off = new Vector2(size.x / 2, size.y / 2); // Assume size/2 is element-wise division
        off.set(off.x * Math.abs(trans.m00) + off.y * Math.abs(trans.m02),
                off.x * Math.abs(trans.m01) + off.y*Math.abs(trans.m10)); // Assume m[0] = m00, m[1] = m01, etc.

        myRect.setX(trans.m11 - off.x);
        myRect.setY(trans.m12 - off.y);
        myRect.setSize(off.x * 2, off.y * 2);
    }

    /**
     * Intersects the given scissor mask with this one.
     *
     * The intersection will take place in the coordinate system of this scissor
     * mask. The other mask will be transformed to be in this coordinate space.
     * This transformation will compute the bounding box of the transformed scissor
     * and interesect it with the bounding box of this scissor.
     *
     * As long as the scissors have the same rotational angle, this will have the
     * expected effect of intersecting two scissors. However, if their rotational
     * angles differ, the transformed scissor will be the axis-aligned bounding
     * box (in the coordinate system of this scissor mask) of the original. This
     * my result in revealing areas once hidden.
     *
     * @param mask	The scissor mask to intersect with this one
     *
     * @return a reference to the scissor for chaining
     */
    public CUScissor intersect(CUScissor mask) {
        Affine2 transform = this.transform;
        transform.inv();
        this.transform.mul(transform);
        rectTransform(mask.bounds, transform);
        rectIntersect(bounds, mask.bounds);
        recompute();
        return this;
    }

    /**
     * Returns the intersection of the given scissor mask with this one.
     *
     * The intersection will take place in the coordinate system of this scissor
     * mask. The other mask will be transformed to be in this coordinate space.
     * This transformation will compute the bounding box of the transformed scissor
     * and interesect it with the bounding box of this scissor.
     *
     * As long as the scissors have the same rotational angle, this will have the
     * expected effect of intersecting two scissors. However, if their rotational
     * angles differ, the transformed scissor will be the axis-aligned bounding
     * box (in the coordinate system of this scissor mask) of the original. This
     * my result in revealing areas once hidden.
     *
     * This scissor mask will not be affected by this method.
     *
     * @param mask	The scissor mask to intersect with this one
     *
     * @return the intersection of the given scissor mask with this one.
     */
    CUScissor getIntersection(CUScissor mask) {
        Affine2 transform = this.transform;
        transform.inv();
        transform.preMul(mask.transform);

        CUScissor result = new CUScissor(this);
        rectTransform(mask.bounds, transform);
        rectIntersect(result.bounds, mask.bounds);
        result.recompute();
        return result;
    }
    //endregion

    //region Conversion

    /**
     * Reads the scissor mask into the provided array
     *
     * The scissor mask is written to the given array in std140 format.
     * That is (1) 12 floats for the affine transform (as a 3x3 homogenous
     * matrix), (2) 2 floats for the extent, and (3) 2 floats for the
     * fringe (one for each axis). Values are written in this order.
     *
     * @param array     The array to store the values
     *
     * @return a reference to the array for chaining
     */
    public float[] getData(float[] array, int offset) {
        array[offset   ] = inverse.m00;
        array[offset+1 ] = inverse.m10;
        array[offset+2 ] = 0;
        array[offset+3 ] = 0;
        array[offset+4 ] = inverse.m01;
        array[offset+5 ] = inverse.m11;
        array[offset+6 ] = 0;
        array[offset+7 ] = 0;
        array[offset+8 ] = inverse.m02;
        array[offset+9 ] = inverse.m12;
        array[offset+10] = 1;
        array[offset+11] = 0;

        array[offset+12] = bounds.width/2;
        array[offset+13] = bounds.height/2;
        array[offset+14] = (float)Math.sqrt(scissor.m00*scissor.m00 + scissor.m01*scissor.m01) / fringe;
        array[offset+15] = (float)Math.sqrt(scissor.m10*scissor.m10 + scissor.m11*scissor.m11) / fringe;
        return array;
    }

    /**
     * Reads the scissor mask into the provided array
     *
     * The scissor mask is written to the array so that it can be passed
     * the the shader one component at a time (e.g. NOT in std140 format).
     * It differs from getData in that it only uses 9 floats for the
     * affine transform (as a 3x3 homogenous matrix).
     *
     * @param array     The array to store the values
     *
     * @return a reference to the array for chaining
     */
    public float[] getComponents(float[] array, int offset) {
        array[offset   ] = inverse.m00;
        array[offset+1 ] = inverse.m10;
        array[offset+2 ] = 0;
        array[offset+3 ] = inverse.m01;
        array[offset+4 ] = inverse.m11;
        array[offset+5 ] = 0;
        array[offset+6 ] = inverse.m02;
        array[offset+7 ] = inverse.m12;
        array[offset+8 ] = 1;

        array[offset+9 ] = bounds.width/2;
        array[offset+10] = bounds.height/2;
        array[offset+11] = (float)Math.sqrt(scissor.m00*scissor.m00 + scissor.m01*scissor.m01) / fringe;
        array[offset+12] = (float)Math.sqrt(scissor.m10*scissor.m10 + scissor.m11*scissor.m11) / fringe;
        return array;
    }

    /**
     * Returns a string representation of this scissor for debuggging purposes.
     *
     * If verbose is true, the string will include class information.  This
     * allows us to unambiguously identify the class.
     *
     * @return a string representation of this scissor for debuggging purposes.
     */
    @Override
    public String toString() {
        String result = "Scissor\n";
        result += String.format("|  %.4f, %.4f, %.4f  |   |  %.4f | \n", scissor.m00, scissor.m01, scissor.m02, bounds.width/2);
        result += String.format("|  %.4f, %.4f, %.4f  |;  |  %.4f |; ",  scissor.m10, scissor.m11, scissor.m12, bounds.height/2);

        float a = (float)Math.sqrt(scissor.m00*scissor.m00 + scissor.m01*scissor.m01) / fringe;
        float b = (float)Math.sqrt(scissor.m10*scissor.m10 + scissor.m11*scissor.m11) / fringe;
        result += String.format(" %.4f x %.4f",  a, b);
        return result;
    }

    //endregion

    /**
     * Recomputes the internal transform for OpenGL.
     */
    void recompute() {
        scissor.idt();
        scissor.translate(bounds.x + bounds.width/2, bounds.y + bounds.height/2);
        scissor = scissor.mul(transform);
        inverse = scissor;
        inverse.inv();
    }
}
