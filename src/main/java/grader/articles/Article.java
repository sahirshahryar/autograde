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
package grader.articles;

import grader.frontend.Color;
import grader.util.Helper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static grader.frontend.Color.*;

/**
 *
 *
 * @author  Sahir Shahryar
 * @since   Monday, July 16, 2018
 * @version 1.0.0
 */
public class Article {

    private static final String
            EMPHASIS_BLUE_MATCHER = regexForGroupingChars('*', '*', ".*?"),
            EMPHASIS_YELLOW_MATCHER = regexForGroupingChars('`', '`', ".*?"),
            SYMBOL_REFERENCE_MATCHER = regexForGroupingChars('(', ')', "[^ ]*?"),
            LOOKBEHIND_ASSERTION = "(?<!(?<!\\\\)\\\\)";


    String title;

    String body;

    private ArrayList<String> unresolvableElements = new ArrayList<>();

    public Article(String title, String unrefinedBody) {
        this.title = title;
        this.body = preparseBody(unrefinedBody);
    }

    /**
     * Converts the contents of
     * @param content
     * @return
     */
    private String preparseBody(String content) {
        StringBuilder refinedResult = new StringBuilder();
        String[] split = content.split("\\n");

        int linesInParagraph = 0;
        for (int i = 0; i < split.length; ++i) {
            String line = split[i];

            if (line.isEmpty()) {
                if (i == 0) {
                    continue;
                }

                else if (linesInParagraph == 0) {
                    refinedResult.append('\n');
                }
            }

            if (i > 0 && (split[i - 1].trim().isEmpty())) {
                linesInParagraph = 0;
                refinedResult.append("(RESET)\n\n");
            }

            refinedResult.append(line).append(' ');
            ++linesInParagraph;
        }

        return refinedResult.toString();
    }


    public void resolveBodyElements(ArticleManager references) {
        if (references != null) {
            Pattern resolveSymbolReferences = Pattern.compile(SYMBOL_REFERENCE_MATCHER);
            Matcher matcher = resolveSymbolReferences.matcher(this.body);

            int start = -1, end = -1;
            String match = null;
            while (matcher.find()) {
                if (!unresolvableElements.contains(matcher.group())) {
                    start = matcher.start();
                    end = matcher.end();
                    match = matcher.group();

                    break;
                }
            }

            if (start != -1) {
                String reference = match.substring(1, match.length() - 1);
                if (reference.equalsIgnoreCase("ARTICLE:" + this.title)) {
                    throw new IllegalArgumentException("An article cannot reference itself");
                } else {
                    String resolvedElem = references.getElement(reference);

                    this.body = this.body.substring(0, start)
                            + resolvedElem
                            + this.body.substring(end);

                    // RECURSIVE CALL
                    if (resolvedElem.equals("(" + reference + ")")) {
                        unresolvableElements.add("(" + reference + ")");
                    }

                    this.resolveBodyElements(references);
                    return;
                }
            }
        }


        Pattern resolveBlueEmphasis = Pattern.compile(EMPHASIS_BLUE_MATCHER);
        Matcher matcher = resolveBlueEmphasis.matcher(this.body);
        while (matcher.find()) {
            String match = matcher.group(),
                    repl = BLUE + match.substring(1, match.length() - 1) + RESET;
            this.body = this.body.replace(match, repl);
        }


        Pattern resolveYellowEmphasis = Pattern.compile(EMPHASIS_YELLOW_MATCHER);
        matcher = resolveYellowEmphasis.matcher(this.body);
        while (matcher.find()) {
            String match = matcher.group(),
                    repl = YELLOW + match.substring(1, match.length() - 1) + RESET;
            this.body = this.body.replace(match, repl);
        }


        String[] split = this.body.split("\\n");
        for (int i = 0; i < split.length; ++i) {
            String line = split[i];
            if (line.trim().startsWith("> ")) {
                split[i] = line.replaceFirst("> ", BLUE + "");
            }
        }

        this.body = Helper.join("\n", split);
        this.body = this.body.replaceAll(LOOKBEHIND_ASSERTION + "\\\\\\*", "*");

        this.body = Color.RESET + this.body.replaceAll("\\n *\\n *\\n", "\n\n")
                                           .replaceFirst("\\u001B\\[0m", "")
                                           .trim();
    }


    public String getName() {
        return this.title;
    }

    public String getText() {
        return this.body;
    }


    private static String regexForGroupingChars(char start, char end, String midPattern) {
        String startString = "" + start, endString = "" + end;

        if (Helper.charAnyOf(start, '*', '.', '+', '?', '[', ']', '(', ')', '{', '}')) {
            startString = "\\" + startString;
        }

        if (Helper.charAnyOf(end, '*', '.', '+', '?', '[', ']', '(', ')', '{', '}')) {
            endString = "\\" + endString;
        }

        return LOOKBEHIND_ASSERTION + startString + midPattern
             + LOOKBEHIND_ASSERTION + endString;
    }


}
