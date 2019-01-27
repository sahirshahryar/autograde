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
package grader.scripts.ancillary;

import grader.backend.*;
import grader.util.Helper;
import grader.util.Tuple;
import grader.reflect.ReflectionAssistant;
import grader.reflect.SourceUtilities;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs an automated analysis of the student's programming style, checking for a
 * number of things, such as indentation, comment frequency, the presence of the
 * Academic Honesty Policy comment, and (gasp!) variable names. Variable name
 * determination is the most iffy of all, because determining if a variable is
 * relevant to a program's function is beyond the scope of this program, but there
 * are algorithmic ways to do it. And since all of the output is verified by a human
 * anyway, why not at least highlight potential problems?
 *
 * @author  Sahir Shahryar
 * @since   Saturday, April 28, 2018
 * @version 1.0.0
 */
public class StyleAnalysis
  implements AncillaryScript {

    /**
     * This threshold determines the minimum value returned by
     * {@link #getIndentationScore(File)} in order for a student to be penalized for
     * poor indentation.
     */
    private static final float INDENTATION_RATIO_THRESHOLD = 0.5F;


    /**
     * This threshold determines the minimum value of the 'degree' variable inside
     * {@link #getIndentationScore(File)} required for a student to receive a deduction
     * for that particular indentation value. See the latter half of the source code
     * for the above method for more information.
     */
    private static final float INDENTATION_DEGREE_THRESHOLD = 0.2F;


    /**
     * This threshold determines the minimum frequency of comment characters relative
     * to all characters in order in order for a student to not be penalized for a lack
     * of comments. So, for example, if this value is 0.25F, at least 25% of all
     * NON-WHITESPACE characters must be inside a comment for the student to pass the
     * commenting test. You may be surprised, but 25% isn't all that much.
     *
     * See {@link #getCommentingScore(File, Tuple)} for more information.
     */
    private static final float COMMENT_FREQUENCY_THRESHOLD = 0.25F;


    /**
     * This is a list of acceptable variable names, which can be set externally
     * depending on the script being used.
     */
    public static String[] ACCEPTABLE_VARIABLE_NAMES = {};


    /**
     * This is a list of known class names, which can also be set externally depending
     * on the script being used. THIS IS NOT THE SAME THING AS THE ABOVE ARRAY.
     * The regular expression used to scrape potential variable names from the code
     * is not very smart, so some class names can be specified in here to automatically
     * toss them out.
     */
    public static String[] KNOWN_CLASS_NAMES = {};


    /**
     * This String[] contains some common variables, constants, and objects that one may
     * find in Java code. These should not show up in the final list of variable names.
     *
     * @see #getVariableNames(File) method where this is used
     */
    public static final String[] COMMON_JAVA_NAMES = {
            "i", "j", "k", "MAX_VALUE", "MIN_VALUE", "NaN", "Object", "Scanner",
            "String", "temp"
    };


    /**
     * Implemented from {@link AncillaryScript}.
     *
     * @param student
     * @return
     * @throws ManualGradingError
     */
    public Feedback addAdditionalFeedback(Student student)
            throws ManualGradingError {

        Feedback feedback = student.getFeedback();
        if (feedback == null) {
            return new Feedback("StyleAnalysis");
        }

        if (student.getSubmissions().size() == 0) {
            return feedback;
        }

        float sum = 0.0F;
        for (ELCSubmission submission : student.getSubmissions()) {
            sum += getIndentationScore(submission.getFile());
        }

        float average = sum / student.getSubmissions().size();

        if (average > INDENTATION_RATIO_THRESHOLD) {
            feedback.deductPoints(3.0);
            feedback.addNote("–3: Please indent your code more clearly; it makes it much " +
                    "easier for other programmers to follow. (Avg. ratio = " +
                    average + ")");
        }

        sum = 0.0F;

        for (ELCSubmission submission : student.getSubmissions()) {
            sum += getCommentingScore(submission.getFile(), null);
        }

        average = sum / student.getSubmissions().size();

        if (average < COMMENT_FREQUENCY_THRESHOLD) {
            feedback.deductPoints(3.0);
            feedback.addNote("–3: Please include some more comments explaining " +
                    "important parts of your code. (Avg. ratio = " + average + ")");
        }

        return feedback;
    }


    /**
     * Perform a stylistic analysis of the student's indentation. This is done by a
     * pretty fuzzy algorithm; it's not looking at individual stylistic errors so much
     * as a "map" of indentation changes. The key is to generate a map that is fair
     * and accurate.
     *
     * By dividing the sum of bad stylistic decisions to total stylistic decisions made,
     * we receive a floating-point number between 0 and 1. This gives us a pretty good
     * idea of how good or bad the indentation style is.
     *
     * @param file (File) the file being scored.
     *
     * @return (float) the score, a decimal number between
     *         0 (perfect) and 1 (unreadable).
     *
     * @throws ManualGradingError thrown if the file cannot be read
     */
    private static float getIndentationScore(File file)
            throws ManualGradingError {
        /**
         * points will be added to whenever the code contains something this algorithm
         * considers "stylistically good"; deductions will be added to whenever the code
         * contains something considered "stylistically bad". The ratio of deductions
         * to total points and deductions will be the return value.
         */
        float points = 0.0F, deductions = 0.0F;

        /**
         * Get the contents of the file.
         */
        ArrayList<String> lines = SourceUtilities.getLines(file);

        /**
         * The array 'indent' stores the indentation values of the lines that we
         * want to consider. If a line should not be considered (e.g., a comment, blank
         * line, or continuation of a multi-line statement), its corresponding value
         * in 'indent' will be -1. While processing, parts of a multi-line statement
         * will temporarily have the value -2, which will later be overwritten. This
         * isn't perfect but it eliminates a LOT of noise in more verbose code.
         *
         * The array 'indentationDelta' contains the indentation of each line relative
         * to the last properly-considered line. The last proper line is found by
         * skipping over -1 or stray -2 values in 'indent'.
         */
        int[] indent = new int[lines.size()], indentationDelta = new int[lines.size()];
        indentationDelta[0] = 0;

        /**
         * Loop over the contents of the file to fill out 'indent'.
         */
        boolean insideComment = false;
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);

            /**
             * We need to keep track of whether or not we're inside a comment or not.
             * This also isn't perfect, since it doesn't consider a multiline comment
             * starting and ending on the same line. But since we're trying to calculate
             * a fuzzy score, not a hard and fast score, it doesn't really matter.
             */
            if (insideComment) {
                indent[i] = -1;

                if (line.contains("*/")) {
                    insideComment = false;
                }

                continue;
            }

            /**
             * We use the containsSyntacticElement() method to avoid potential trickery
             * that can be done by putting /* inside a string literal.
             */
            if (SourceUtilities.containsSyntacticElement(line, "/*")) {
                insideComment = true;
                indent[i] = -1;
                continue;
            }

            /**
             * Check if the line starts with a comment. If so, it should be ignored.
             */
            String trim = line.trim();
            if (trim.isEmpty() || trim.startsWith("//")) {
                indent[i] = -1;
                continue;
            }

            /**
             * Determine the indent level of the line to start with.
             */
            indent[i] = SourceUtilities.getIndentLevel(line);

            /**
             * What a mess of an if-statement!
             *
             * If it doesn't end with a semicolon, curly brace, closing parenthesis, or
             * colon (case labels), discount it.
             */
            if (!trim.endsWith(";") && !trim.endsWith("{")
                    && !trim.endsWith("}") && !trim.endsWith(")")
                    && !trim.endsWith(":")) {
                indent[i] = -2;
            }

            /**
             * This logic exists to clean up indentation levels for incomplete
             * statements. Like much of the parsing happening here, this isn't perfect,
             * but it's good enough to strip out a lot of noise.
             *
             * For the sake of argument, let's say we have the statements
             *
             *     WordBoard board = new WordBoard(10, 10,
             *                                     seed);
             *     char[][] board = board.getBoard();
             *
             * If we don't ignore the second line, we have a LOT of noise added on.
             * The logic below allows us to map this code's indentation as
             *
             *  4 |     WordBoard board = new WordBoard(10, 10,
             * -1 |                                     seed);
             *  4 |     char[][] board = board.getBoard();
             *
             * instead of having 36 as the value for line 2. In this scenario it doesn't
             * matter, but if the second line had a value of 5, it would affect the
             * score disproportionately negatively, since the delta array would contain
             * { 0, 32, -31 } instead of { 0, 32, -32 } and thus 32 would not have
             * a complementary -32, causing a double penalty. When we skip over the
             * second line, we get { 0, 0, 0 } instead:
             *
             *     delta[0] = 0, always.
             *     delta[1] = 0, because indent[1] = -1, so we skip over it
             *     delta[2] = 0, because we get the difference between indent[2] and
             *                   indent[j], where j is the index of the "most recent"
             *                   (i.e., immediately preceding i) value in 'indent' that
             *                   is not -1 or -2. j = 0, so we get indent[2] - indent[0]
             *                   = 4 - 4 = 0.
             */
            else if (trim.endsWith(";") || trim.endsWith("{")) {
                /**
                 * Loop back up until we find a line which doesn't have an indent level
                 * of -2. Then j will be equal to the index of the line ABOVE the first
                 * line with -2 as an index (i.e., the first line in the multi-line
                 * statement).
                 */
                int j;
                for (j = i - 1; j >= 0; --j) {
                    if (indent[j] != -2) {
                        break;
                    }
                }

                /**
                 * Set the indent level of the first line in the multi-line statement to
                 * its actual indentation level.
                 */
                indent[j + 1] = SourceUtilities.getIndentLevel(lines.get(j + 1));

                /**
                 * If we actually moved back up lines, set the indentation level of every
                 * line up until the line we're currently on to -1. Then we can skip over
                 * it easily later.
                 */
                if (i - j > 1) {
                    for (int k = j + 2; k <= i; ++k) {
                        indent[k] = -1;
                    }
                }
            }
        }

        /**
         * We've now "parsed" the file for only the relevant details (i.e., no comments,
         * and no duplicated multi-line statements). Now generate the indentation depth
         * map from the 'indent' array.
         *
         * As explained above, each value in indentationDelta is set to the indent level
         * of the current line, minus the indentation level of the last complete line
         * (i.e., not whitespace).
         */
        for (int i = 0; i < lines.size(); ++i) {
            int prevIndent = 0;

            /**
             * Don't calculate the indentation delta if the line was marked as one that
             * we should ignore.
             */
            if (indent[i] == -1) {
                continue;
            }

            /**
             * If this line shouldn't be ignored, then calculate the change in
             * indentation relative to the last line that wasn't ignored.
             */
            for (int j = i - 1; j >= 0; --j) {
                if (indent[j] > -1) {
                    prevIndent = indent[j];
                    break;
                }
            }

            indentationDelta[i] = indent[i] - prevIndent;
        }

        /**
         * Create a list of indents (values in indentationDelta > 0) and a list of
         * outdents (values in indentationDelta < 0). Changes of 0 are ignored.
         */
        ArrayList<Integer> indents = new ArrayList<>(),
                          outdents = new ArrayList<>();

        for (int delta : indentationDelta) {
            if (delta > 0) {
                indents.add(delta);
            } else if (delta < 0) {
                outdents.add(delta);
            }
        }

        /**
         * The total weight is the sum of indents and outdents. Each offense is relative
         * to the total weight, so if there's one particularly egregious indentation in
         * a 100-line file, it won't automatically ruin the student's score. On the other
         * hand, a great amount of inconsistency in the code gets weighted pretty heavily
         * against the student.
         */
        int totalWeightage = indents.size() + outdents.size();

        /**
         * Create maps of the different indent and outdent levels. This way we can count
         * how many times a particular degree of indent or outdent occurred. We use a
         * map with doubles as the key (even though the keys are the indent levels, which
         * are integers), because that's the return value of the helper method below.
         */
        HashMap<Double, Integer> indentFrequencyMap
                = Helper.frequencyMap(indents.toArray(new Integer[indents.size()]));

        HashMap<Double, Integer> outdentFrequencyMap
                = Helper.frequencyMap(outdents.toArray(new Integer[outdents.size()]));

        /**
         * This list keeps track of the indent values we've covered. We loop over the
         * keys of 'indentFrequencyMap', but if there's an outdent value whose opposite
         * doesn't appear in 'indentFrequencyMap', then the student needs to be
         * penalized (slightly).
         */
        ArrayList<Double> coveredIndentValues = new ArrayList<>();

        /**
         * Loop over the indent values.
         */
        for (double indentValue : indentFrequencyMap.keySet()) {
            /**
             * This occurs if there's an indent value that doesn't have a matching
             * outdent. The deduction is relative to the total number of indents and
             * outdents.
             */
            if (!outdentFrequencyMap.containsKey(-indentValue)) {
                float deduction
                        = (float) indentFrequencyMap.get(indentValue) / totalWeightage;
                deductions += deduction;
            }

            /**
             * Typically, indent values will fall into this category: there are some
             * indents and some outdents with complementary values (e.g., 4 and -4).
             */
            else {
                /**
                 * First determine the number of times we indent by this amount and the
                 * number of times we outdent by this amount.
                 */
                float inFreq = indentFrequencyMap.get(indentValue),
                     outFreq = outdentFrequencyMap.get(-indentValue);

                /**
                 * 'degree' is a number between 0 and 1 that represents how one-sided
                 * the frequencies of indent and outdent are. The closer 'degree' is
                 * to 1, the more lopsided the student's code. For example, if there's
                 * one indent of 4 but 40 outdents of 4, then the degree is
                 *
                 * a.   |1 - 40| / (1 + 40) = 39 / 41 = 0.951.
                 *
                 * On the other hand, if the number of indents and outdents are very
                 * close to each other, then 'degree' is relatively close to 0. So with
                 * 71 indents of 4 and 69 outdents of 4 (these are real results for
                 * StatScript.java), the degree is
                 *
                 * b.  |71 - 69| / (71 + 69) = 2 / 140 = 0.014.
                 *
                 * The degree is used to determine whether the student will be rewarded
                 * or penalized for this particular indentation. As of right now, the
                 * threshold for 'degree' is 0.2, so any value in excess results in a
                 * penalty.
                 *
                 * 'weight', on the other hand, is the AMOUNT that gets given to the
                 * student, either as a penalty or as an award. This is determined by
                 * how many indents and outdents are being considered out of the sum of
                 * all indents and outdents (i.e., 'totalWeightage'). For example, if
                 * a program has 152 total indents and outdents (totalWeightage = 152),
                 * then in the above scenarios, the weight is as follows:
                 *
                 * a. 41 / 152 = 0.270.
                 * b. 140 / 152 = 0.921.
                 *
                 * So in the first case, the student is penalized 0.270 points, and in
                 * the second case, the student is awarded 0.921 points.
                 */
                float degree = Math.abs(inFreq - outFreq) / (inFreq + outFreq),
                      weight = (inFreq + outFreq) / totalWeightage;

                /**
                 * The threshold here is arbitrary, but I think 0.2 is a fair middle
                 * ground.
                 */
                if (degree > INDENTATION_DEGREE_THRESHOLD) {
                    deductions += weight;
                } else {
                    points += weight;
                }
            }

            coveredIndentValues.add(indentValue);
        }

        /**
         * Now we loop over all the outdent values that didn't have a matching
         * indent. As before, the deduction is relative to the total weightage.
         */
        for (double indentValue : outdentFrequencyMap.keySet()) {
            indentValue = -indentValue;

            if (coveredIndentValues.contains(indentValue)) {
                continue;
            }

            float deduction
                    = (float) outdentFrequencyMap.get(-indentValue) / totalWeightage;
            deductions += deduction;
        }


        /**
         * A happy little accident that occurred when devising this algorithm is that
         * points + deductions (seemingly) always sums up to exactly 1.0. It makes
         * sense, considering we distribute parts of the sum that is 'totalWeightage'
         * to 'points' and 'deductions', but I'm still scared, so we're going to leave
         * that quotient there just in case.
         */
        float ratio = deductions / (points + deductions);

        /**
         * This should't happen, but just in case the user submitted an empty file to
         * mess with us, we'll be ready.
         */
        if (Float.isNaN(ratio)) {
            ratio = 0.0F;
        }
        
        return ratio;
    }


    /**
     * Perform a stylistic analysis of the student's comments. Here we can determine
     * the ratio of in-comment characters to code characters. This one's pretty
     * straightforward; we simply divide the number of characters that are inside a
     * comment to the total number of characters in the file to receive a number between
     * 0 and 1. A ratio of around 0.015 should be sufficient to pass.
     *
     * @param file                    (File) the file being scored.
     * @param academicCommentLocation (Tuple) the location of the Academic Honesty
     *                                Policy comment. It doesn't count as a comment.
     *
     * @return (float) a number between 0 and 1.
     *         0 = no comments; 1 = exclusively comments.
     *
     * @throws ManualGradingError thrown if the file cannot be read
     */
    private static float getCommentingScore(File file,
                                     Tuple<Integer, Integer> academicCommentLocation)
            throws ManualGradingError {
        int lengthWithoutComments = 0, lengthWithComments = 0;

        ArrayList<String> source = SourceUtilities.getSource(file, false, false);

        for (String line : source) {
            lengthWithoutComments += line.trim().length();
        }

        ArrayList<String> lines = SourceUtilities.getLines(file);

        for (int i = 0; i < lines.size(); ++i) {
            if (academicCommentLocation != null) {
                if (i >= academicCommentLocation.getFirst() &&
                        i <= academicCommentLocation.getSecond()) {
                    continue;
                }
            }

            lengthWithComments += lines.get(i).trim().length();
        }

        int commentCharacters = lengthWithComments - lengthWithoutComments;

        return (float) commentCharacters / lengthWithComments;
    }


    /**
     * Returns a list of variable declarations. From there, we can use the Levenshtein
     * distance (the number of deletions, insertions, or substitutions required to
     * turn one string into another) to find if each variable name is somewhat close
     * to something that might be considered descriptive. This part is supposed to be
     * filtered by a human. It may not automatically detect unconventional variable names
     * as still being informative, but it can certainly highlight bad variable names such
     * as 'x' or 'aBoolean'.
     *
     * @param file
     * @return
     * @throws ManualGradingError
     */
    public static ArrayList<String> getVariableNames(File file)
            throws ManualGradingError {

        String className = null;
        if (file.getName().endsWith(".java")) {
            className = ReflectionAssistant.determineCorrectClassName(file);
        }

        ArrayList<String> lines   = SourceUtilities.getSource(file, true, true),
                          imports = SourceUtilities.getImports(file);

        String fileContents = Helper.join("\n", lines.toArray(new String[lines.size()]));

        Pattern regex = Pattern.compile("[$_a-zA-Z][$_a-zA-Z0-9]*(?=[ ;])");
        Matcher matcher = regex.matcher(fileContents);

        ArrayList<String> result = new ArrayList<>();

        /**
         * Labels are a sin but they're genuinely useful here.
         */
        outer: while (matcher.find()) {
            String match = matcher.group();

            if (result.contains(match)) {
                continue;
            }

            for (String keyword : SourceUtilities.JAVA_KEYWORDS) {
                if (match.equals(keyword)) {
                    continue outer;
                }
            }

            for (String knownClass : KNOWN_CLASS_NAMES) {
                if (match.equals(knownClass)) {
                    continue outer;
                }
            }

            for (String commonClass : COMMON_JAVA_NAMES) {
                if (match.equals(commonClass)) {
                    continue outer;
                }
            }

            for (String importedClass : imports) {
                if (importedClass.endsWith(match)) {
                    continue outer;
                }
            }

            if (match.equals(className)) {
                continue;
            }

            result.add(match);
        }

        return result;
    }


    private static boolean hasAcademicHonestyComment(File file)
            throws ManualGradingError {
        return determineAHCommentLocation(file) != null;
    }


    private static Tuple<Integer, Integer> determineAHCommentLocation(File file)
            throws ManualGradingError {
        



        return new Tuple<>(0, 0);
    }






}
