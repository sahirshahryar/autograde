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

    private HashMap<String, EmbeddedMap> maps;

    private HashMap<String, String> variables;

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

    public void addFile(File file) throws RuntimeException {
        ArrayList<String> lines;

        try {
            lines = SourceUtilities.getLines(file);
        } catch (final ManualGradingError e) {
            throw new RuntimeException("Error generating articles from file '"
                    + file.getName() + "': contents could not be read");
        }

        ArrayList<String> summationDeclarations = new ArrayList<>();
        ArrayList<String> textBodies = new ArrayList<>();

        String currentHeading = null, currentText = "";
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);

            if (line.trim().startsWith("//")) {
                continue;
            }

            if (line.trim().startsWith("let ")) {
                summationDeclarations.add(line);
                continue;
            }

            if (line.trim().matches("# ?[a-zA-Z0-9\\-_$]: ?(Article|Map)")) {
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

            currentText += line;
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

                    return articles.get(split[1]).getText();

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
                    List<String> aliases = Arrays.asList(array);
                    return Helper.elegantPrintList(aliases);

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

        throw new IllegalArgumentException("Unknown documentation symbol '" + title + "'");
    }

}
