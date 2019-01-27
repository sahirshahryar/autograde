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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author  Sahir Shahryar
 * @since   Sunday, May 6, 2018
 * @version 1.0.0
 */
public class SourceUtilities {

    /**
     * The following 50 or so words are reserved words in Java. These should never be
     * considered variables.
     *                                                                        <br><br>
     * Note: the token "var" is a bit iffy; because it wasn't originally a Java keyword,
     * the Java compiler seems to do some fuzzing when deciding whether var is a keyword
     * or not. It's almost like the complete opposite of "throw new", where the two
     * keywords are separate but used together so frequently they may as well be one.
     * In this case, it's the syntax that makes "var" a keyword. Hence, the four
     * statements below are completely syntactically valid, even in Java 10:
     *                                                                            <br>
     * 1    var x = 5;                                                            <br>
     * 2    int y = var;                                                          <br>
     * 3    var var = "hello";                                                    <br>
     * 4    var = var;                                                            <br>
     *
     * "var" is only a keyword in lines 1 and 3. So, because "var" is technically a valid
     * Java variable name, even in Java 10, it is excluded from the list of keywords
     * below. The Java 10 tokenizer must be a nightmare.
     */
    public static final String[] JAVA_KEYWORDS = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "false", "final", "finally", "float", "for", "goto", "if",
            "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "true", "try", /* "var", (Java 10) */ "void",
            "volatile", "while"
    };


    /**
     *
     * @param line
     * @return
     */
    public static int getIndentLevel(String line) {
        int tabCount = 0;
        for (int i = 0; i < line.length(); ++i) {
            if (line.charAt(i) == '\t') {
                ++tabCount;
                continue;
            }


            if (line.charAt(i) != ' ') {
                return i + (tabCount * 4);
            }
        }

        return -1;
    }

    /**
     *
     *
     * @param line
     * @param element
     * @return
     */
    public static boolean containsSyntacticElement(String line, String element) {
        char tokenStart = element.charAt(0);

        if (tokenStart == ' ' || tokenStart == '\\' || tokenStart == '"') {
            throw new IllegalArgumentException("Cannot detect the token '" + element +
                    " because it starts with '" + tokenStart + "', a special " +
                    "character");
        }

        boolean inString = false;

        try {
            for (int i = 0; i < line.length(); ++i) {
                char ch = line.charAt(i);
                if (ch == ' ') {
                    continue;
                }

                /**
                 * We've made quite a powerful assertion prior to running this method:
                 * that this is all syntactically valid Java code. So there's no need
                 * to know if the backslash lies OUTSIDE of a quote sequence.
                 */
                if (ch == '\\') {
                    if (line.charAt(i + 1) == '"') {
                        ++i;
                    }
                }

                if (ch == '"') {
                    inString = !inString;
                }

                if (inString) {
                    continue;
                }

                if (ch == element.charAt(0)) {
                    boolean valid = true;
                    for (int j = 1; j < element.length(); ++j) {
                        if (line.charAt(i + j) != element.charAt(j)) {
                            valid = false;
                            break;
                        }
                    }

                    if (valid) {
                        return true;
                    }
                }
            }

            return false;
        }


        /**
         * This is so lazy it shouldn't even be legal.
         */
        catch (final StringIndexOutOfBoundsException e) {
            return false;
        }
    }


    public static ArrayList<String> getLines(File file) throws ManualGradingError {
        return getLines(file, false);
    }

    /**
     *
     * @param file
     * @return
     * @throws ManualGradingError
     */
    public static ArrayList<String> getLines(File file, boolean fixPackage)
            throws ManualGradingError {
        try {
            BufferedReader stream
                    = new BufferedReader(new FileReader(file));
            ArrayList<String> lines = new ArrayList<>();

            String line;
            while ((line = stream.readLine()) != null) {
                if (fixPackage && line.trim().startsWith("package")) {
                    lines.add("package " + AutoGrade.TEMP_PACKAGE + ";");
                }


                lines.add(line.replace("\t", "    "));
            }

            return lines;
        }

        catch (final IOException e) {
            throw new ManualGradingError(e.getMessage());
        }
    }


    /**
     *
     * @param file
     * @return
     * @throws ManualGradingError
     */
    public static ArrayList<String> getImports(File file) throws ManualGradingError {
        ArrayList<String> lines = getSource(file, false, true);
        ArrayList<String> imports = new ArrayList<>();

        for (String line : lines) {
            if (line.startsWith("import ")) {
                String importedFile = line.substring(Math.min(line.length(), 7));
                if (!importedFile.isEmpty()) {
                    imports.add(importedFile.substring(0, importedFile.length() - 1));
                }
            }
        }

        return imports;
    }


    /**
     *
     * @param file
     * @param excludeImports
     * @param stripStrings
     * @return
     * @throws ManualGradingError
     */
    public static ArrayList<String> getSource(File file, boolean excludeImports,
                                              boolean stripStrings)
            throws ManualGradingError {
        ArrayList<String> lines = getLines(file);
        ArrayList<String> source = new ArrayList<>();

        boolean insideComment = false;
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);

            if (stripStrings) {
                line = stripStrings(line);
            }

            /**
             * We need to keep track of whether or not we're inside a comment or not.
             */
            if (insideComment) {
                if (line.contains("*/")) {
                    insideComment = false;

                    line = line.substring(line.indexOf("*/") + 2);
                    if (!line.trim().isEmpty()) {
                        if (excludeImports
                                && (line.startsWith("import")
                                || line.startsWith("package"))) {
                            continue;
                        }

                        source.add(line);
                    }

                }

                continue;
            }

            /**
             * We use the containsSyntacticElement() method to avoid potential trickery
             * that can be done by putting /* inside a string literal, as demonstrated
             * right here.
             */
            if (containsSyntacticElement(line, "/*")) {
                insideComment = true;

                line = line.substring(0, line.indexOf("/*"));

                if (!line.trim().isEmpty()) {
                    if (excludeImports
                            && (line.startsWith("import")
                            || line.startsWith("package"))) {
                        continue;
                    }

                    source.add(line);
                }

                continue;
            }

            /**
             * Check if the line starts with a comment. If so, it should be ignored.
             */
            if (containsSyntacticElement(line, "//")) {
                line = line.substring(0, line.indexOf("//"));

                if (!line.trim().isEmpty()) {
                    if (excludeImports
                            && (line.startsWith("import")
                            || line.startsWith("package"))) {
                        continue;
                    }

                    source.add(line);
                }
            }

            else {
                if (!line.trim().isEmpty()) {
                    if (excludeImports
                            && (line.startsWith("import")
                            || line.startsWith("package"))) {
                        continue;
                    }

                    source.add(line);
                }
            }
        }

        return source;
    }


    /**
     *
     * @param file
     * @return
     * @throws ManualGradingError
     */
    public static ArrayList<String> getComments(File file) throws ManualGradingError  {
        ArrayList<String> lines = getLines(file);
        // TODO
        return null;
    }


    /**
     *
     * @param line
     * @return
     */
    public static String stripStrings(String line) {
        String result = "";
        boolean inString = false, inChar = false;
        for (int i = 0; i < line.length(); ++i) {
            char ch = line.charAt(i);
            switch (ch) {
                case '"':
                    if (!inChar) {
                        inString = !inString;
                    }

                    break;

                case '\\':
                    if (inString || inChar) {
                        i++;
                    }

                    break;

                case '\'':
                    if (!inString) {
                        inChar = !inChar;
                    }
            }

            if (!inString || ch == '"') {
                result += ch;
            }
        }

        return result;
    }


    public static String determineCorrectClassName(File file)
            throws ManualGradingError {

        ArrayList<String> source = SourceUtilities.getSource(file, true, true);

        for (String line : source) {
            if (line.trim().startsWith("public class ")) {
                String[] split = line.split(" ");

                for (int i = 0; i < split.length; ++i) {
                    if (split[i].equals("public")) {
                        if (i + 2 < split.length) {
                            String result = split[i + 2];

                            if (result.endsWith("{")) {
                                result = result.substring(0, result.length() - 1);
                            }

                            return result;
                        }
                    }
                }
            }
        }

        throw new ManualGradingError("Code was too complex to automatically rename;" +
                " please do so manually.");
    }


    public static void writeToFile() {

    }

}
