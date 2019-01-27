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

import grader.AutoGrade;
import grader.backend.ManualGradingError;
import grader.util.Helper;

import javax.tools.SimpleJavaFileObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author  Sahir Shahryar
 * @since   7/15/18
 * @version 1.0.0
 */
public class InternalSource
     extends SimpleJavaFileObject {

    private String content;
    private String name;

    public InternalSource(File file) throws ManualGradingError {
        super(file.toURI(), Kind.SOURCE);
        
        this.name = ReflectionAssistant.determineCorrectClassName(file);

        ArrayList<String> lines = SourceUtilities.getLines(file, true);
        this.content = Helper.join("\n", lines);
    }


    public String getName() {
        return name;
    }

    /**
     * WORKAROUND: It seems that, for the moment, lying to the classloader about where a
     * file is truly located causes some problems. So for now we're just not going to do
     * that.
     *
     * @return
     */
    public String getFullName() {
        return name;
    }


    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return content;
    }

}
