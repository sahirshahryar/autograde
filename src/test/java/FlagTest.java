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
import grader.flag.FlagParser;
import grader.flag.FlagSet;

/**
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   9/4/18
 * @version 1.0.0
 */
public class FlagTest {

    public static void main(String[] args) {
        FlagSet set = new FlagSet().accepts("test", "3")
                                   .withAliases("t");

        FlagParser parse = new FlagParser(set, "NetPayScript.java submissions -t -1");

        System.out.println(parse.getValue("test"));
    }


}