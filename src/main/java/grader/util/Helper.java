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
package grader.util;

import grader.frontend.Color;
import grader.frontend.CommandHandler;

import java.util.*;

/**
 * The Helper class provides a variety of helpful methods that can be used elsewhere.
 *
 * @author  Sahir Shahryar
 * @since   Wednesday, April 25, 2018
 * @version 1.0.0
 */
public final class Helper {

    /**
     * Compares two floating-point numbers for rough equality.
     *
     * @param a (double) one number
     * @param b (double) another number
     *
     * @return (boolean) true if the two numbers are within 0.01 of each other;
     *         false otherwise.
     */
    public static boolean roughlyEqual(double a, double b) {
        if (Double.isNaN(a)) {
            return Double.isNaN(b);
        }

        if (Double.isNaN(b)) {
            return false;
        }

        return Math.abs(a - b) < 0.01;
    }


    public static <T> boolean listsEqual(List<T> a, List<T> b) {
        if (a == null || b == null) {
            return a == b;
        }

        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); ++i) {
            if (!a.get(i).equals(b.get(i))) {
                return false;
            }
        }

        return true;
    }


    public static <T> boolean listContainsAll(List<T> candidate, List<T> standard) {
        for (T item : candidate) {
            if (!standard.contains(item)) {
                return false;
            }
        }

        return true;
    }


    public static Tuple<Double, String> firstMissing(List<Double> candidate,
                                                     HashMap<Double, String> standard) {
        for (Double required : standard.keySet()) {
            boolean found = false;
            for (Double item : candidate) {
                if (roughlyEqual(item, required)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return new Tuple<>(required, standard.get(required));
            }
        }

        return null;
    }


    /**
     *
     * @param array
     * @return
     */
    public static String arrayToString(double[] array) {
        if (array.length == 0) {
            return "{}";
        }

        String output = "";
        for (int i = 0; i < array.length; ++i) {
            output += (i == 0 ? "{ " : ", ") + array[i];
        }


        return output + " }";
    }


    /**
     *
     * @param array
     * @return
     */
    public static <T> String arrayToString(T[] array) {
        if (array.length == 0) {
            return "{}";
        }

        boolean needsQuotations = !array.getClass().isPrimitive();

        String output = "";
        for (int i = 0; i < array.length; ++i) {
            output += (i == 0 ? "{ " : ", ")
                    + (needsQuotations ? "\"" : "")
                    + array[i]
                    + (needsQuotations ? "\"" : "");
        }


        return output + " }";
    }


    /**
     *
     * @param glue
     * @param args
     * @return
     */
    public static String join(String glue, String[] args) {
        if (args.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(args[0]);

        for (int i = 1; i < args.length; ++i) {
            sb.append(glue).append(args[i]);
        }

        return sb.toString();
    }

    public static String join(String glue, List<String> args) {
        if (args.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(args.get(0));

        for (int i = 1; i < args.size(); ++i) {
            sb.append(glue).append(args.get(i));
        }

        return sb.toString();
    }

    public static <T> String elegantPrintList(List<T> list) {
        return elegantPrintList(list, Color.NONE, Color.NONE, false);
    }


    public static <T> String elegantPrintList(List<T> list, boolean useOr) {
        return elegantPrintList(list, Color.NONE, Color.NONE, useOr);
    }

    /**
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> String elegantPrintList(List<T> list, Color itemColor,
                                              Color separatorColor, boolean useOr) {
        Color endColor = (separatorColor == Color.NONE) ? Color.NONE : Color.RESET;
        String finalLabel = useOr ? ", or " : ", and ";

        switch (list.size()) {
            case 0: return "";
            case 1: return itemColor + list.get(0).toString() + endColor;

            case 2: return itemColor + list.get(0).toString() + separatorColor
                           + finalLabel.substring(1) + itemColor
                           + list.get(1).toString() + endColor;

            default:
                StringBuilder sb = new StringBuilder(itemColor.toString());

                sb.append(list.get(0).toString());

                for (int i = 1; i < list.size(); ++i) {
                    sb.append(separatorColor)
                      .append((i + 1 == list.size()) ? finalLabel : ", ")
                      .append(itemColor)
                      .append(list.get(i).toString());
                }

                sb.append(endColor);

                return sb.toString();
        }
    }

    public static String elegantPrintMap(Map<String, String> map,
                                         Iterable<String> order) {
        return elegantPrintMap(map, order, "  ", "  ",
                               Color.CYAN, Color.RESET, 80, true);
    }


    /**
     *
     * @param map
     * @return
     */
    public static String elegantPrintMap(Map<String, String> map) {
        ArrayList<String> order = new ArrayList<>(map.keySet());
        Collections.sort(order);
        return elegantPrintMap(map, order);
    }


    /**
     *
     * @param map
     * @param lMargin
     * @param rMargin
     * @param keyColor
     * @param valueColor
     * @param overallWidth
     * @param hardWrap
     * @return
     */
    public static String elegantPrintMap(Map<String, String> map, Iterable<String> order,
                                         String lMargin, String rMargin,
                                         Color keyColor, Color valueColor,
                                         int overallWidth, boolean hardWrap) {
        // wew lad
        /* String widestTerm = map.keySet()
                               .stream()
                               .max(Comparator.comparing(Color::trueLength))
                               .get();
                                so sad this doesn't work any more */

        int maxWidth = -1;
        String widestTerm = null;
        for (String key : map.keySet()) {
            int width = Color.trueLength(key);
            if (width > maxWidth) {
                maxWidth = width;
                widestTerm = key;
            }
        }

        if (maxWidth >= overallWidth) {
            throw new IllegalArgumentException("Dictionary term '" +
                    widestTerm + "' is as wide as the overall margin (" +
                    overallWidth + "), making it impossible to fit any definitions");
        }

        int totalWidth = lMargin.length() + maxWidth + rMargin.length();
        String secondLineMargin = generateSpaces(totalWidth);

        StringBuilder result = new StringBuilder();

        for (String term : order) {
            result.append('\n')
                  .append(lMargin)
                  .append(keyColor)
                  .append(padRightAlign(term, maxWidth))
                  .append(Color.RESET)
                  .append(rMargin);

            ArrayList<String> lines = new ArrayList<>();

            String definition = map.get(term);

            String[] initialSplit = definition.split("\\n");
            for (String distinctLine : initialSplit) {
                String[] wrappedSplit = wrap(distinctLine,
                        overallWidth - maxWidth, hardWrap).split("\\n");
                for (String line : wrappedSplit) {
                    lines.add(line);
                }
            }

            if (lines.isEmpty()) {
                continue;
            }

            result.append(valueColor)
                  .append(lines.get(0));

            for (int i = 1; i < lines.size(); ++i) {
                 result.append('\n')
                       .append(secondLineMargin)
                       .append(lines.get(i));
            }

            result.append(Color.RESET);
        }

        return result.substring(1);
    }


    /**
     *
     * @param text
     * @param width
     * @return
     */
    private static String padRightAlign(String text, int width) {
        return generateSpaces(width - Color.trueLength(text)) + text;
    }


    /**
     *
     * @param spaces
     * @return
     */
    private static String generateSpaces(int spaces) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < spaces; ++i) {
            sb.append(' ');
        }

        return sb.toString();
    }


    /**
     *
     * @param text
     * @param width
     * @param hardMargin
     * @return
     */
    public static String wrap(String text, int width, boolean hardMargin) {
        String[] split = text.split(" ");
        StringBuilder result = new StringBuilder();

        int currentPosition = 0;
        for (String word : split) {
            int endingPos = currentPosition + word.length();

            if (endingPos >= width) {
                if (currentPosition == 0) {
                    if (hardMargin) {
                        result.append(word.substring(0, width - 1))
                              .append('-')
                              .append('\n')
                              .append(word.substring(width - 1));
                        currentPosition = word.substring(width - 1).length();
                    }

                    else {
                        result.append(' ')
                              .append(word)
                              .append('\n');
                        currentPosition = 0;
                    }
                }

                else {
                    result.append('\n')
                          .append(word);
                    currentPosition = word.length();
                }
            }

            else {
                result.append(' ')
                      .append(word);

                currentPosition += word.length() + 1;
            }
        }

        String wrappedAnswer = result.toString();

        return wrappedAnswer.startsWith(" ") ? wrappedAnswer.substring(1)
                                             : wrappedAnswer;
    }


    /**
     *
     * @param data
     * @return
     */
    public static HashMap<Double, Integer> frequencyMap(Number[] data) {
        HashMap<Double, Integer> map = new HashMap<>();

        for (int i = 0; i < data.length; ++i) {
            double value = data[i].doubleValue();
            if (!map.containsKey(value)) {
                map.put(value, count(data, value, 0));
            }
        }

        return map;
    }


    /**
     * Helper method: counts the number of times a specific value occurs inside the given
     * array.
     *
     * @param data
     * @param value
     * @param start
     * @return
     */
    private static int count(Number[] data, double value, int start) {
        int count = 0;
        for (int i = start; i < data.length; ++i) {
            if (Helper.roughlyEqual(data[i].doubleValue(), value)) {
                ++count;
            }
        }

        return count;
    }


    public static boolean charAnyOf(char ref, char... possibilities) {
        for (char possible : possibilities) {
            if (ref == possible) {
                return true;
            }
        }

        return false;
    }


    /**
     *
     *
     * See https://en.wikipedia.org/wiki/Levenshtein_distance for details on the
     * implementation.
     *
     * @param a
     * @param b
     * @return
     */
    public static int levenshtein(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();

        if (a.equals(b)) {
            return 0;
        }

        return lev(a, b, a.length(), b.length());
    }


    /**
     *
     * @param a
     * @param b
     * @param i
     * @param j
     * @return
     */
    private static int lev(String a, String b, int i, int j) {
        if (Math.min(i, j) == 0) {
            return 0;
        }

        return min(lev(a, b, i - 1, j) + 1,
                   lev(a, b, i, j - 1) + 1,
                   lev(a, b, i - 1, j - 1) + ind(a.charAt(i), b.charAt(j)));
    }


    /**
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    private static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }


    /**
     * Helper method for calculating the Levenshtein distance between two Strings.
     *
     * @param a
     * @param b
     * @return
     */
    private static int ind(char a, char b) {
        return (a == b) ? 1 : 0;
    }

}
