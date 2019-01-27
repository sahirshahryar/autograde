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

import grader.backend.ManualGradingError;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;

import static javax.tools.JavaCompiler.CompilationTask;

/**
 * A compiler that converts plain Java source code into a loaded class directly.
 * This wouldn't be necessary if Java didn't block us from deleting .class files. It
 * does, for some reason, and I cannot stand dealing with the reconfirmation prompt
 * every time I rerun AutoGrade on the same folder twice, so now it's time to write this.
 *
 * In all honesty, the more I think about it, the more I like this solution. It's so
 * clean, considering it means that AutoGrade leaves behind absolutely NO junk in its
 * executing folder -- not even a 'temp' directory where files are manipulate.
 *
 * @author  Sahir Shahryar
 * @since   Sunday, July 15, 2018
 * @version 1.0.0
 */
public class InternalCompiler {

    private static InternalClassLoader classLoader = new InternalClassLoader();

    public static Class<?> compile(File file, String... javacArgs)
            throws ManualGradingError {
        try {
            if (!file.getName().endsWith(".java")) {
                throw new RuntimeException("Attempted to compile some non-.java file!");
            }


        classLoader = new InternalClassLoader();

        String[] oldNameElements = file.getName().split("/");
        String oldName = oldNameElements[oldNameElements.length - 1];

        String newName = SourceUtilities.determineCorrectClassName(file) + ".java";

        File newFile = new File(file.getAbsoluteFile().getParent()
                + "/" + newName);

        boolean deletionRequired = !oldName.equals(newName);

        if (deletionRequired && newFile.exists()) {
            newFile.delete();
        }

        try {
            Files.copy(file.toPath(), newFile.toPath());
        } catch (final IOException e) {
            //
        }

        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

        InternalJavaFileManager manager
                = new InternalJavaFileManager(javac.getStandardFileManager(null, null,
                                                                           null),
                                              classLoader);

        ArrayList<String> argsList = new ArrayList<>();

        for (String option : javacArgs) {
            Collections.addAll(argsList, option.split(" "));
        }

        ArrayList<InternalSource> sources = new ArrayList<>();
        sources.add(new InternalSource(newFile));

        CompilationTask compile = javac.getTask(null, manager, null,
                argsList, null, sources);


        if (!compile.call()) {
            if (deletionRequired) {
                newFile.delete();
            }

            throw new ManualGradingError("Unable to compile class " + file.getName());
        }

        if (deletionRequired) {
            newFile.delete();
        }

        try {
            return classLoader.loadClass(sources.get(0).getFullName(), true);
        } catch (final ClassNotFoundException e) {
            throw new ManualGradingError("Unable to load class " + file.getName());
        }

        }

        catch (final ManualGradingError e) {
            throw e;
        }

        catch (final Throwable t) {
            throw new ManualGradingError("Unable to compile class " + file.getName());
        }
    }


}
