/*
 * This file is part of AutoGrade, licensed under the MIT License (MIT).
 *
 * Copyright (c) Sahir Shahryar <https://github.com/sahirshahryar>
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
package grader.flag;

/**
 * A list of the various command types that exist for use within the
 * default implementation of the input filetype.
 *
 * @author  Sahir Shahryar
 * @since   Monday, June 29, 2015
 * @version 1.0.0
 */
public enum FlagType {

    /**
     * A BOOLEAN command is a command that has only two states, the ON
     * state and the OFF state. To make a BOOLEAN command take on the
     * ON state, it must simply be entered into the registry
     * (e.g., /registry -f). Otherwise, it is left in the OFF state.
     */
    BOOLEAN,

    /**
     * A VALUED command must accept a value. This value should be
     * positioned immediately after the registry:                          <br>
     * /registry -f value
     */
    VALUED,


    /**
     * An UNREGISTERED command is any command that has not been officially
     * registered under the current <code>FlagMap</code>.
     */
    UNREGISTERED

}
