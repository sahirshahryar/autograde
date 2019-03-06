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

import grader.backend.ManualGradingError;
import grader.flag.FlagSet;
import grader.frontend.Color;
import grader.frontend.CommandHandler;
import grader.reflect.SourceUtilities;
import grader.util.Helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author  Sahir Shahryar
 * @since   Monday, July 16, 2018
 * @version 1.0.0
 */
public class ArticleManager {

    private HashMap<String, Article> articles;

    public HashMap<String, EmbeddedMap> maps;

    private HashMap<String, String> variables;

    private ArrayList<String> pendingSummationDeclarations = new ArrayList<>();

    public ArticleManager() {
        this.articles = new HashMap<>();
        this.maps = new HashMap<>();
    }

    public void addArticle(Article article) {
        this.articles.put(article.getName(), article);
    }

    public void addMap(EmbeddedMap map) {
        this.maps.put(map.getName(), map);
    }

    public void setVariable(String variable, String value) {
        this.variables.put(variable, value);
    }

    public void addFile(File file, boolean postponeAllowed) throws RuntimeException {
        try {
            addFile(new FileReader(file), postponeAllowed);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void addFile(Reader r, boolean postponeAllowed) throws RuntimeException {
        ArrayList<String> lines;

        try {
            lines = SourceUtilities.getLines(r, false);
        } catch (final ManualGradingError e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        ArrayList<String> textBodies = new ArrayList<>();

        String currentHeading = null, currentText = "";
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);

            if (line.trim().startsWith("//")) {
                continue;
            }

            if (line.trim().startsWith("let ")) {
                this.pendingSummationDeclarations.add(line);
                continue;
            }

            if (line.trim().matches("# ?[a-zA-Z0-9\\-_$]+: ?(Article|Map)")) {
                if (currentHeading != null) {
                    textBodies.add(currentHeading + "\n" + currentText);
                }

                currentHeading = line;
                currentText = "";

                continue;
            }

            if (currentHeading == null) {
                continue;
            }

            currentText += "\n" + line;
        }

        for (String textBody : textBodies) {
            String[] data = textBody.split("\\n", 2);

            String[] headerData = data[0].split("#", 2)[1].split(":", 2);
            String name = headerData[0].trim(), type = headerData[1].trim();

            if (type.equalsIgnoreCase("Article")) {
                Article newArticle = new Article(name, data[1]);
                this.addArticle(newArticle);
            }

            else {
                EmbeddedMap newMap = new EmbeddedMap(name, data[1]);
                this.addMap(newMap);
            }
        }

        ArrayList<String> successfulSummations = new ArrayList<>();
        for (String declaration : this.pendingSummationDeclarations) {
            String[] elements = declaration.split(" *[+=:] *", 4);

            if (elements.length != 4) {
                throw new RuntimeException("Malformed summation '" + declaration + "'");
            }

            String title = elements[0].substring(elements[0].indexOf(" ") + 1);

            if (!elements[1].equalsIgnoreCase("Map")) {
                throw new IllegalArgumentException("Cannot sum up anything except " +
                        "Maps (" + declaration + ")");
            }

            String leftName = elements[2].substring(1);
            if (!maps.containsKey(leftName)) {
                if (postponeAllowed) {
                    continue;
                } else {
                    throw new IllegalArgumentException("Unknown map '"
                            + elements[2] + "'");
                }
            }

            EmbeddedMap left = maps.get(leftName);

            String right = elements[3];
            if (right.startsWith("FLAGS:")) {
                String label = right.split(":", 2)[1];
                if (CommandHandler.COMMAND_FLAGSETS.containsKey(label)) {
                    FlagSet f = CommandHandler.COMMAND_FLAGSETS.get(label);
                    EmbeddedMap result
                            = left.combineWith(title, f.flagDescriptions(),
                                               f.orderForArticles());
                    this.maps.put(title, result);
                    successfulSummations.add(declaration);
                } else {
                    throw new IllegalArgumentException("No flags for '" + label + "'");
                }
            }
        }

        this.pendingSummationDeclarations.removeAll(successfulSummations);
    }



    public String getElement(String title) {
        if (title.contains(":")) {
            String[] split = title.split(":", 2);
            switch (split[0].toUpperCase()) {
                case "ARTICLES":
                case "ARTICLE":
                    if (!articles.containsKey(split[1])) {
                        throw new IllegalArgumentException("No article '" + split[1] + "'");
                    }

                    Article article = articles.get(split[1]);
                    article.resolveBodyElements(this);
                    return article.getText();

                case "MAPS":
                case "MAP":
                    if (!maps.containsKey(split[1])) {
                        throw new IllegalArgumentException("No map '" + split[1] + "'");
                    }

                    return maps.get(split[1]).toString();

                case "ALIASES":
                case "ALIAS":
                    if (!CommandHandler.COMMAND_ALIASES.containsKey(split[1])) {
                        throw new IllegalArgumentException("No command '" + split[1] + "'");
                    }

                    String[] array = CommandHandler.COMMAND_ALIASES.get(split[1]);
                    List<String> aliases = new ArrayList<>();

                    for (String alias : array) {
                        aliases.add(Color.YELLOW + alias + Color.RESET);
                    }

                    return Helper.elegantPrintList(aliases, true);

                case "FLAGS":
                case "FLAG":
                    if (!CommandHandler.COMMAND_FLAGSETS.containsKey(split[1])) {
                        throw new IllegalArgumentException("No flags for '" + split[1] + "'");
                    }

                    FlagSet f = CommandHandler.COMMAND_FLAGSETS.get(split[1]);
                    return Helper.elegantPrintMap(f.flagDescriptions());

                default:
                    throw new IllegalArgumentException("Unknown symbol class '" + split[0] + "'");
            }
        }

        for (Color c : Color.values()) {
            if (title.equalsIgnoreCase(c.name())) {
                return c.toString();
            }
        }

        return "(" + title + ")";
    }

}
