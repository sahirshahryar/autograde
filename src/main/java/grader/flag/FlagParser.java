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
package grader.flag;

import grader.util.Helper;

import java.util.ArrayList;

import static grader.frontend.Color.*;

/**
 * The {@code FlagParser} is, as its name suggests, the standard
 * processor for registry input. It provides support for flags and multi-word
 * arguments.
 *
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   Monday, June 29, 2015
 * @version 1.0.0
 */
public class FlagParser {

    /**
     * This string represents what constitutes a command.
     */
    private static final String FLAG_REGEX = "--?[a-zA-Z~!@#$%^&*_+?<>:|\\-]+";


    /**
     * The set of flags that are recognized by this FlagParser.
     */
    private final FlagSet flags;


    /**
     * The list of Strings that constitute our final, parsed arguments.
     */
    private final ArrayList<String> finalArgs;


    /**
     * This value is useful when parsing commands. For example, in CommandHandler, by
     * setting offset = 1, we can use get(0) to refer to the first ARGUMENT, even though
     * get(0) should technically refer to the 2nd argument (index 1).
     */
    private int offset = 0;


    /**
     * Constructs a new FlagParser object with the given FlagSet and arguments.
     *
     * @param flags (FlagSet) the set of flags recognized by this parser.
     * @param args  (String...) the array of arguments, separated by spaces.
     */
    public FlagParser(FlagSet flags, String... args) {
        this.flags = flags;
        this.finalArgs = this.processArgs(args);
    }


    /**
     * Constructs a new FlagParser with an empty FlagSet and the given arguments.
     *
     * @param args (String...) the array of arguments, separated by spaces.
     */
    public FlagParser(String... args) {
        this(new FlagSet(), args);
    }


    /**
     * This method provides the number of arguments provided in total.
     * Arguments such as flags are excluded from this number. This method subtracts
     * the offset from the total length.
     *
     * @return (int) the size of the {@link #finalArgs} list, minus {@link #offset}.
     */
    public int length() {
        return this.finalArgs.size() - offset;
    }


    /**
     * Returns the absolute number of arguments provided (i.e., without accounting for
     * the offset).
     *
     * @return (int) the size of the {@link #finalArgs} list.
     */
    public int absoluteLength() {
        return this.finalArgs.size();
    }


    /**
     * @return (FlagSet) the set of flags recognized by this parser. Also contains the
     *         data of flags that were used in the command after parsing is complete.
     */
    public FlagSet getFlagSet() {
        return this.flags;
    }


    /**
     * Checks if the given command was used in the command.
     *
     * @param f (String) the command's key.
     *
     * @return (boolean) true if the command was used; false otherwise.
     */
    public boolean hasFlag(String f) {
        return this.flags.hasFlag(f);
    }


    /**
     * Returns the value of the given command, assuming it was used.
     *
     * @param f (String) the command's key.
     *
     * @return (String) the value of the command if it was used. If the command was a boolean
     *         command, either "true" or "false" is returned depending on whether or not the
     *         command was used.
     */
    public String getValue(String f) {
        Flag flag = this.flags.resolve(f);

        if (flag == null) {
            return null;
        }

        f = flag.getFlag();

        switch (this.flags.getType(f)) {
            case BOOLEAN:
                return this.hasFlag(f) ? "true" : "false";

            case VALUED:
                if (this.hasFlag(f)) {
                    return this.flags.getFlag(f).getValue();
                }

                return "";

            default:
            case UNREGISTERED:
                return null;
        }
    }


    /**
     * Sets the argument offset to the given number.
     *
     * @param offset (int) a non-negative number.
     */
    public void setOffset(int offset) {
        if (offset >= 0) {
            this.offset = offset;
        }
    }


    /**
     * Fetches the argument at the given position.
     *
     * @param pos (int) the index of retrieval.
     *
     * @return (String) the argument at index (pos + {@link #offset}).
     */
    public String get(int pos) {
        return this.finalArgs.get(pos + offset);
    }


    /**
     * Fetches the argument at the given absolute position.
     *
     * @param pos (int) the absolute index of retrieval.
     *
     * @return (String) the argument at index {@code pos}.
     */
    public String getAbsolute(int pos) {
        return this.finalArgs.get(pos);
    }


    /**
     * @return (String[]) the contents of {@link #finalArgs} from index {@link #offset}
     *         to {@code finalArgs.length()}, stored in an array.
     */
    public String[] asArray() {
        ArrayList<String> subset = new ArrayList<>();

        for (int i = offset; i < this.absoluteLength(); ++i) {
            subset.add(finalArgs.get(i));
        }

        return subset.toArray(new String[this.length()]);
    }


    /**
     * Parses the arguments.
     *
     * @param args An array of arguments split by spaces.
     * @return     An {@link java.util.ArrayList} containing the parsed
     *             arguments.
     */
    private ArrayList<String> processArgs(String... args) {

        // If there are no arguments, return an empty list.
        if (args.length == 0) {
            return new ArrayList<>();
        }

        /**
         * To simplify both the code and the actual process itself,
         * the task of processing the arguments is split into three
         * steps:
         *
         * 1. We join multi-word arguments together.
         * 2. We join subsequent flags together.
         * 3. We parse flags.
         */
        ArrayList<String> stageOne = new ArrayList<>();
        ArrayList<String> stageTwo = new ArrayList<>();
        ArrayList<String> resultant = new ArrayList<>();

        /**
         * Firstly, we need to account for the fact that we support
         * backslash-escaped spaces in arguments. For example, one might use an
         * argument "this\ has\ spaces".
         * ----------------------------------------------------------
         * REGULAR EXPRESSION EXPLANATION
         * "(?<!
         (?<!\\)\\) "
         *
         * (?<!(?<!\\)\\): Zero-length negative look-behind; ensures that the
         *                 character before the space is not a backslash. It
         *                 also ensures that the backslash is not preceded by
         *                 another backslash.
         *            " ": Split by spaces.
         */
        args = Helper.join(" ", args).split("(?<!(?<!\\\\)\\\\) ");


        // Next, we remove the extraneous backslashes from the arguments.
        for (int i = 0; i < args.length; ++i) {
            /**
             * REGULAR EXPRESSION EXPLANATION
             * (?<!\\)\\
             *
             * (?<!\\): Zero-length negative look-behind; ensures that the
             *          character before the backslash is not also a backslash.
             *      \\: A backslash.
             */
            args[i] = args[i].replaceAll("(?<!\\\\)\\\\", "");
        }

        /**
         * Now we can glue together arguments that are joined by quotes (e.g.,
         * "this is one argument"). Both quotes and apostrophes are supported.
         */
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];

            /**
             * If the argument is just a blank space, then it should be
             * ignored. This prevents commands from breaking if a user
             * accidentally inputs something like "/registry arg1  arg2" instead
             * of "/registry arg1 arg2".
             */
            if (arg.length() == 0) {
                continue; // (for loop)
            }

            /**
             * Otherwise, check if there is a special input feature, such as a
             * quote or a backslash.
             */
            switch (arg.charAt(0)) {
                // Either an apostrophe or a quotation mark.
                case '\'': case '"':
                    /**
                     * endReached: tracks if we've found an end to the quoted
                     *             argument.
                     *     closer: the character that will end the group.
                     *          j: a for-loop index; used later.
                     *     result: the final argument (that will be
                     *             multi-word).
                     */
                    boolean endReached = false;
                    char closer = arg.charAt(0);
                    int j;

                    StringBuilder result = new StringBuilder(arg.substring(1));

                    // This loop looks for the end of the quote.
                    for (j = i; j < args.length; ++j) {
                        String quoted = args[j];
                        int len = quoted.length();

                        // Blank spaces should be included, not discarded.
                        if (len == 0) {
                            result.append(' ');
                            continue;
                        }

                        /**
                         * A backslash can be used to escape the quotation
                         * character (apostrophe or quotation mark).
                         */
                        if (quoted.startsWith("\\" + closer)) {
                            quoted = quoted.substring(1);
                            --len;
                        }

                        /**
                         * If the final character is a quote mark, one of two
                         * things can happen:
                         *
                         * 1. If the quote is preceded by a backslash (\), then
                         *    the quote mark will not be interpreted as the end
                         *    of the multi-word argument.
                         * 2. Otherwise, it will.
                         */
                        if (quoted.charAt(len - 1) == closer) {
                            // Quote mark preceded by backslash.
                            if (quoted.charAt(Math.max(len - 2, 0)) == '\\') {
                                quoted = quoted.substring(0, Math.max(len - 3, 0))
                                        + closer;
                            }

                            // Quote mark not preceded by backslash.
                            else {
                                result.append(' ')
                                        .append(quoted.substring(0, len - 1));
                                endReached = true;
                                break; // (for loop)
                            }
                        }

                        result.append(' ').append(quoted);
                    } // for (j = i; j < args.length; ++j)

                    /**
                     * endReached = true when one of the arguments has a quote
                     * mark at the end of the argument and when that quote mark
                     * isn't preceded by a backslash.
                     *
                     * Now we set i = j, because we don't want to accidentally
                     * revisit arguments that are actually part of a multi-word
                     * argument.
                     */
                    if (endReached) {
                        arg = result.toString();
                        i = j;
                    }

                    break; // (switch: case '\'', '"')

                /**
                 * Special characters (particularly apostrophes or quotation
                 * marks) can be escaped by using a backslash. Backslashes can
                 * also be escaped with the backslash (i.e., "\\text" becomes
                 * "\text").
                 */
                case '\\':
                    if (arg.length() > 1) {
                        arg = arg.substring(1);
                    }
            } // switch (arg.charAt(0))

            stageOne.add(arg);
        } // for (int i = 0; i < args.length; ++i)

        /**
         * Stage two involves the processing of sequential flags.
         * (See org.orbital.registry.command.Flag)
         */
        for (int i = 0; i < stageOne.size(); ++i) {
            String arg = stageOne.get(i);

            /**
             * Flag-parsing terminator. If two dashes are provided, no flags beyond
             * this point should be parsed.
             */
            if (arg.equals("--")) {
                for (int j = i; j < stageOne.size(); ++i) {
                    stageTwo.add(stageOne.get(i));
                }

                break; // (for loop)
            }

            /**
             * The below processing only happens for single-character flags.
             */
            if (!arg.matches("-[a-zA-Z~!@#$%^&*_+?<>:|]+")) {
                stageTwo.add(arg);
                continue; // (for loop)
            }

            /**
             *      concat: A String containing a series of subsequent
             *              flags put together.
             *     waiting: A list of arguments that are skipped over when
             *              concatenating valued flags.
             *      valued: The number of valued flags currently being
             *              concatenated. We use max(1, valuedIn()) because we
             *              want to look for subsequent flags as well (e.g.,
             *              "-a -b -c"). Therefore, only a regular argument can
             *              stop the while-loop.
             * indexOffset: The amount by which the for-loop index (i) should
             *              be offset when fetching an argument from the list
             *              (stageOne).
             *   terminate: Whether or not the for loop should be terminated
             *              once the while loop ends.
             */
            StringBuilder concat = new StringBuilder(arg);
            ArrayList<String> waiting = new ArrayList<>();
            int valued = Math.max(1, this.valuedIn(arg)), indexOffset = 1;
            boolean terminate = false;

            /**
             * This while-loop is crucial to processing odd, uncommon input,
             * such as "-ab valueA -f -c valueB valueC", properly.
             *
             * Simply put: this loop expects that all valued flags will receive
             * the input they deserve. Thus it makes the process of processing
             * flags later much easier.
             *
             * The loop continues until one of two conditions is met:
             * 1. there are no more valued flags seeking a value
             * 2. there are no more arguments available to check
             *
             * Let's take a look at that example:
             * "-ab valueA -f -c valueB valueC"
             *
             * Here, -a, -b, and -c are valued flags, and -f is not valued.
             * valueA, valueB, and valueC correspond respectively to -a, -b,
             * and -c.
             *
             * Breaking it down into steps:
             * 1. "-ab": This is a command, so we check how many valued flags are
             *           contained. There are two, so valued = 2.
             * 2. "valueA": Not a command, so valued is lowered by 1. valued = 1.
             * 3. "-f": A command, so we check how many valued flags are
             *          contained. None, so valued remains 1.
             * 4. "-c": A command, so we check how many valued flags are
             *          contained. One, so valued = 2.
             * 5. "valueB": Not a command, so valued is lowered by 1.
             * 6. "valueC": Not a command, so valued is lowered by 1.
             *
             * valued now equals 0, so the loop ends. The result is thus
             * "-abfc valueA valueB valueC".
             */
            while (valued > 0 && i + indexOffset < stageOne.size()) {
                String check = stageOne.get(i + indexOffset);

                // Flag-parsing terminator
                if (check.equals("--")) {
                    waiting.add("--");
                    terminate = true;
                    break; // (while loop)
                }

                // We have a command!
                if (check.matches("-[a-zA-Z~!@#$%^&*_+?<>:|]+")) {
                    /**
                     * We append the flags in this argument to the final
                     * command argument (concat). Using substring(1) removes the
                     * preceding hyphen.
                     */
                    concat.append(check.substring(1));

                    /**
                     * If this command argument has more valued flags,
                     * then we should increase the value of `valued`
                     * accordingly.
                     */
                    valued += this.valuedIn(check);
                }

                /**
                 * It isn't a command, so it must be a normal argument.
                 *
                 * Because this argument needs to be parsed, we will
                 * add it to the wait-list of arguments.
                 *
                 * We assume that this argument is the value for some
                 * valued command; therefore, the number of valued flags
                 * expecting a value decreases by 1.
                 */
                else {
                    waiting.add(check);
                    --valued;
                }

                // Move to the next argument.
                ++indexOffset;
            } // while (valued > 0 && i + indexOffset < stageOne.size())

            /**
             * First we add the concatenated command argument, then we
             * add all of the regular arguments that were skipped.
             */
            stageTwo.add(concat.toString());
            stageTwo.addAll(waiting);

            /**
             * We don't want to revisit any arguments that have been
             * handled already.
             */
            i += indexOffset - 1;

            // Flag-parsing terminator was reached
            if (terminate) {
                break; // (for loop)
            }
        } // for (int i = 0; i < stageOne.size(); ++i)

        /**
         * Now that the flags have been collected, let's actually parse them.
         */
        for (int i = 0; i < stageTwo.size(); ++i) {
            /**
             * arg: the argument at index i
             *   j: the number of arguments that should be skipped (if multiple
             *      valued flags are used in one command string)
             */
            String arg = stageTwo.get(i);
            int j = 1;

            /**
             * If it isn't a command, just add it to the list of normal arguments.
             */
            if (!arg.matches(FLAG_REGEX)) {
                resultant.add(arg);
                continue; // (for loop)
            }
            
            /**
             * Again, if we hit "--", stop parsing any more flags and just add the
             * remaining arguments as normal arguments.
             */
            if (arg.equals("--")) {
                for (int k = i + 1; k < stageTwo.size(); ++k) {
                    resultant.add(stageTwo.get(k));
                }

                break; // (for loop)
            }

            /**
             * Handle multi-character flags (e.g., "--option")
             */
            if (arg.length() > 2 && arg.charAt(1) == '-') {
                String flag = arg.substring(2);

                FlagType type = this.flags.getType(flag);

                flags.validateUsage(flag);

                switch (type) {
                    case BOOLEAN:
                        this.flags.flagUsed(flag);
                        break;

                    case VALUED:
                        /**
                         * Throw an exception if there isn't a follow-up argument after
                         * a command which wants one.
                         */
                        if (i + 1 >= stageTwo.size()) {
                            throw new RuntimeException("Invalid command input: option " +
                                    "--" + flag + " must be followed by a value");
                        }

                        /**
                         * Multi-character flags don't do any of the combining that
                         * single-character flags do, so a valid single-character command
                         * sequence such as
                         *
                         *    -a -b valueA valueB
                         *
                         * is not possible with multi-character flags:
                         *
                         *    --optionA --optionB valueA valueB
                         *
                         * in such a situation, the exception below will be thrown.
                         */
                        if (stageTwo.get(i + 1).matches(FLAG_REGEX)) {
                            throw new RuntimeException(RED + "Invalid command input: the "
                                    + "command parser cannot jump over the option '"
                                    + stageTwo.get(i + 1)
                                    + "' when trying to find the value for the option "
                                    + "'--" + flag + "'; please put the latter's value "
                                    + "immediately after it (e.g., '--" + flag
                                    + " <value> " + stageTwo.get(i + 1) + "')" + RESET);
                        }

                        this.flags.flagUsed(flag, stageTwo.get(++i));
                }


                continue; // (for loop)
            }


            /**
             * Now we parse single-character flags. To do this, we should create an
             * array of all of the flags (i.e., every character in the string excluding
             * the very first one, which is the hyphen).
             */
            char[] flags = arg.substring(1).toCharArray();

            /**
             * Iterate through all of the flags.
             */
            for (int k = 0; k < flags.length; ++k) {
                String flag = flags[k] + "";
                FlagType type = this.flags.getType(flag);

                this.flags.validateUsage(flag);

                /**
                 * Here, we will handle the three different types of flags:
                 *      BOOLEAN: Boolean flags have only two states, on and
                 *               off. When a boolean command is entered, it is
                 *               switched from the off state to the on state.
                 *       STRING: String flags accept a value
                 * UNREGISTERED: An unregistered command is one that is unknown to
                 *               this registry.
                 */
                switch (type) {
                    case BOOLEAN:
                        this.flags.flagUsed(flag);
                        break; // switch (type)

                    case VALUED:
                        // Are there insufficient arguments for this command?
                        if (i + j >= stageTwo.size()) {
                            throw new RuntimeException("Invalid command input: option " +
                                    "-" + flag + " must be followed by a value");
                        }

                        /**
                         * We can't jump over multi-character flags when parsing
                         * single-character flags. So, for example, if the sequence
                         * below is given:
                         *
                         *     -ab --optionC valueA valueB valueC
                         *
                         * an exception will be thrown.
                         */
                        if (stageTwo.get(i + j).startsWith("--")) {
                            throw new RuntimeException("Invalid command input: parser " +
                                    "cannot jump over option " + stageTwo.get(i + j) +
                                    " when trying to parse value for -" + flag);
                        }

                        this.flags.flagUsed(flag, stageTwo.get(i + j));
                        ++j;
                } // switch (FlagType type)
            }

            /**
             * We should skip as many arguments as command values we've parsed. So, for
             * example, given the input
             *
             *     -abc valueA valueB valueC d
             *
             * we want to skip past valueC straight to "d".
             */
            i += j - 1;
        } // for (int i = 0; i < stageTwo.size(); ++i)

        /**
         * All done at last!
         */
        return resultant;
    }


    /**
     * Returns the number of valued flags in an argument that contains multiple
     * single-character flags (e.g., -abc).
     *
     * @param flags (String) the argument to check.
     *
     * @return (int) the number of valued single-character flags in {@code flags}.
     */
    private int valuedIn(String flags) {
        int count = 0;
        for (char ch : flags.toCharArray()) {
            if (this.flags.getType(ch + "") == FlagType.VALUED) {
                ++count;
            }
        }

        return count;
    }

}