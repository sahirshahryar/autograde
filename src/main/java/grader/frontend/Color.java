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
package grader.frontend;

import grader.AutoGrade;

/**
 * An enumeration of color codes that can be used to highlight the answers
 * found inside the word search. Note that Eclipse does not support showing
 * colors inside the terminal, so in order for this to work, students will
 * need to
 *
 * Source: https://stackoverflow.com/questions/5762491/
 *
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   Thursday, April 26, 2018
 * @version 1.0.0
 */
public enum Color {
    RESET(0),
    BLACK(30),
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    PURPLE(35),
    CYAN(36),
    WHITE(37),
    NONE;


    /**
     * The actual ANSI color code that will be printed as part of output.
     */
    private final String code;


    /**
     * Initializes a new value in the enumeration.
     * @param code (int) the numeric portion of the color code.
     */
    Color(int code) {
        this.code = "\u001B[" + code + "m";
    }


    /**
     * Initializes the lack of a color code as an empty string.
     */
    Color() {
        this.code = "";
    }


    /**
     * Using toString() here makes these color codes very convenient when
     * programming them into strings below.
     *
     * @return (String) the full ANSI color code.
     */
    public String toString() {
        return AutoGrade.SHOW_COLORS ? this.code : "";
    }
}