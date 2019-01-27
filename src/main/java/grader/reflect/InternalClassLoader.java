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
package grader.reflect;

import java.util.HashMap;

/**
 * (pops open soda) Ahh, now that's a refreshing class loader. [This pun, based on this
 * class's original name (RefreshingClassLoader) is now obsolete and no longer
 * supported.]
 *
 * Since Java's default class loader doesn't allow you to reload classes easily, this
 * ClassLoader extension does so automatically -- for classes compiled by
 * InternalCompiler.
 *
 * @author  Sahir Shahryar
 * @since   Saturday, April 28, 2018
 * @version 2.0.0
 *          RefreshingClassLoader -> InternalClassLoader
 *          No longer judges based on a specific package, but rather checks if a class
 *          is contained within the internal map of classes.
 *
 *          1.0.0
 */
public class InternalClassLoader extends ClassLoader {



    private final HashMap<String, InternalBytecode> internalClasses;


    /**
     * Initializes the InternalClassLoader.
     */
    public InternalClassLoader() {
        this.internalClasses = new HashMap<>();
    }


    /**
     *
     *
     * @param name
     * @param bytecode
     */
    public void addClass(String name, InternalBytecode bytecode) {
        this.allLoadedClasses();
        this.internalClasses.put(name, bytecode);
    }


    public void allLoadedClasses() {
        for (String name : this.internalClasses.keySet()) {
            System.out.println("class " + name);
        }
    }



    /**
     * Overrides the loadClass() method from ClassLoader.
     *
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        if (!internalClasses.containsKey(name)) {
            return super.loadClass(name, resolve);
        }

        InternalBytecode bytecode = internalClasses.get(name);

        byte[] data = bytecode.getBytes();

        Class clazz = defineClass(name, data, 0, data.length);

        if (resolve) {
            super.resolveClass(clazz);
        }

        return clazz;
    }

}
