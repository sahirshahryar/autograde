/*
 * This file is part of AutoGrade, licensed under the MIT License (MIT).
 *
 * Copyright (c) Sahir Shahryar <https://github.com/sahirshahryar>
 *                              <sahirshahryar@uga.edu>
 *
 * Designed for use by the Computer Science Department at the University of Georgia,
 * but free of proprietary technologies and solutions to class assignments.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package grader.util;

/**
 * A generic utility class that is used to store or return multiple
 * values at the same time.
 *
 * @param <A> The data type of the first object.
 * @param <B> The data type of the second object.
 *
 * @author  Sahir Shahryar
 * @since   Monday, June 29, 2015
 * @version 1.0.0
 */
public class Tuple<A, B> {

    /**
     * The first item in this tuple is represented by the generic
     * <A>. It is referred to as the first item or as the field of
     * type A.
     *
     * @see #getFirst()  Associated getter
     * @see #setFirst(A) Associated setter
     */
    private A a;


    /**
     * The second item in this tuple is represented by the generic
     * <B>. It is referred to as the second item or as the field of
     * type B.
     *
     * @see #getSecond()  Associated getter
     * @see #setSecond(B) Associated setter
     */
    private B b;


    /**
     * Initializes the tuple.
     *
     * @see #a Associated <A> field
     * @see #b Associated <B> field
     *
     * @param a The object of type A.
     * @param b The object of type B.
     */
    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }


    /**
     * Gets the value of the field of type A (the first item).
     *
     * @see #a           Associated field
     * @see #setFirst(A) Associated setter
     *
     * @return {@link A}:
     *         The value of the first item (of Tuple&lt;A, B&gt;).
     */
    public A getFirst() {
        return this.a;
    }


    /**
     * Gets the value of the field of type B (the second item).
     *
     * @see #b            Associated field
     * @see #setSecond(B) Associated setter
     *
     * @return {@link B}:
     *         The value of the second item (of Tuple&lt;A, B&gt;).
     */
    public B getSecond() {
        return this.b;
    }


    /**
     * Sets the value of the field of type A (the first item).
     *
     * @see #a          Associated field
     * @see #getFirst() Associated getter
     *
     * @param  a {@link A}:
     *         The new value for this field. The value must be of
     *         the same type <A> as defined upon initialization.
     *
     *
     * @return {@link Tuple}:
     *         The current Tuple, for chaining.
     */
    public Tuple setFirst(A a) {
        this.a = a;
        return this;
    }


    /**
     * Sets the value of the field of type B (the second item).
     *
     * @see #b           Associated field
     * @see #getSecond() Associated getter
     *
     * @param  b {@link B}:
     *         The new value for this field. The value must be of
     *         the same type <B> as defined upon initialization.
     *
     *
     * @return {@link Tuple}:
     *         The current Tuple, for chaining.
     */
    public Tuple setSecond(B b) {
        this.b = b;
        return this;
    }


    public boolean has(Object obj) {
        return this.a.equals(obj) || this.b.equals(obj);
    }

    public Object other(Object one) {
        if (this.a.equals(one)) {
            return this.b;
        } else if (this.b.equals(one)) {
            return this.a;
        } else {
            return null;
        }
    }

}