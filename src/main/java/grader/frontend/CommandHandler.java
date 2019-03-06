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
package grader.frontend;

import grader.*;
import grader.articles.ArticleManager;
import grader.backend.ManualGradingError;
import grader.backend.Script;
import grader.backend.Student;
import grader.flag.FlagParser;
import grader.flag.FlagSet;
import grader.util.Helper;

import java.io.File;
import java.util.*;

import static grader.frontend.Color.*;
import static grader.frontend.SortOrder.*;

/**
 * This class handles the command-line interface presented to the user after grading
 * is complete.
 *
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   Thursday, April 26, 2018
 * @version 1.0.0
 */
public class CommandHandler {

    /**
     * This map contains a list of all valid aliases for each command.
     */
    public static final HashMap<String, String[]> COMMAND_ALIASES
            = new HashMap<String, String[]>() {{
                put("delete",   new String[] { "d", "del" });
                put("deselect", new String[] { "r", "desel" });
                put("exit",     new String[] { "q", "quit" });
                put("export",   new String[] { "x", "exp" });
                put("file",     new String[] { "f" });
                put("help",     new String[] { "h", "man", "commands", "?" });
                put("inspect",  new String[] { "i", "run", "ins" });
                put("list",     new String[] { "l", "ls" });
                put("select",   new String[] { "s", "sel" });
                put("save",     new String[] { "w" });
                put("sort",     new String[] { "so" });
                put("view",     new String[] { "v", "show" });
    }};


    /**
     * This map contains a short description of each of the commands.
     */
    private static final HashMap<String, String> COMMAND_DESCRIPTIONS
            = new HashMap<String, String>() {{
                put("delete", "Deletes the submission files of the selected students");
                put("deselect", "Removes students from the current selection");
                put("exit", "Exits the program");
                put("export", "Exports the selected results to a file");
                put("file", "Prints the contents of a submission file");
                put("help", "Shows all available commands");
                put("list", "Shows a list of students");
                put("run", "Reruns a submission for the purposes of inspection");
                put("select", "Changes the students that are selected for editing");
                put("save", "Saves the submissions of the given students");
                put("sort", "Sorts results by the given criterion (grade or name)");
                put("view", "Shows details for the selected students");
    }};


    /**
     * This map contains the list of usable flags for each command.
     */
    public static final HashMap<String, FlagSet> COMMAND_FLAGSETS
            = new HashMap<String, FlagSet>() {{
                put("delete",
                        new FlagSet()
                                .accepts("grade-too")
                                    .withAliases("g")
                                    .describeAs("Deletes the student's grade on top of "
                                                 + "deleting their submission files")
                                .accepts("confirm")
                                    .withAliases("c")
                                    .describeAs("Bypasses the confirmation prompt")
                );

                put("deselect",
                        new FlagSet()
                               .accepts("invert")
                                   .withAliases("i")
                                   .describeAs("Deselects all students NOT matching the "
                                               + "given filter")
                );

                put("exit",
                        new FlagSet()
                                .accepts("discard")
                                    .withAliases("d")
                                    .describeAs("Confirms discarding of results if you "
                                                + "haven't exported them already")

                );
                
                put("export", new FlagSet()
                        .accepts("overwrite")
                            .withAliases("o", "w")
                            .describeAs("Overwrites the file / directory at the given " +
                                    "location, if permitted")
                        .accepts("grades-only")
                            .withAliases("g")
                            .describeAs("Exports only the grades of the selected " +
                                        "students (not the feedback they received)")
                        .accepts("no-source-names")
                            .withAliases("n")
                            .describeAs("Removes source names from the feedback "
                                        + "(useful for copying and pasting feedback)")
                        .accepts("separate-files")
                            .withAliases("s")
                            .describeAs("Exports each student's feedback into its own "
                                        + "file. Cannot be used in conjunction with " +
                                    "*--csv*")
                        .accepts("csv")
                            .withAliases("c")
                            .describeAs("Exports the results to a CSV file. Cannot be " +
                                    "used in conjunction with *--separate-files*")
                        .disallowTogether("grades-only", "no-source-names")
                        .disallowTogether("csv", "separate-files")
                );

                put("file", new FlagSet()
                        .accepts("emacs")
                            .withAliases("e")
                            .describeAs("Opens the file in emacs")
                        .accepts("vi")
                            .withAliases("v")
                            .describeAs("Opens the file in vim")
                        .accepts("console")
                            .withAliases("c")
                            .describeAs("Prints the file out to the console")
                        .disallowTogether("emacs", "vi")
                        .disallowTogether("emacs", "console")
                        .disallowTogether("vi", "console")
                );

                put("run", new FlagSet()
                        .accepts("manual")
                            .withAliases("m")
                            .describeAs("Runs program entirely manually")
                        .accepts("stepped")
                            .withAliases("s")
                            .describeAs("Runs through the grading script one step at " +
                                    "a time, like a debugger")
                );

                put("select", new FlagSet()
                        .accepts("invert")
                            .withAliases("i")
                            .describeAs("Selects students who do NOT match the filter")
                        .accepts("append")
                            .withAliases("a")
                            .describeAs("Appends matching students to the existing "
                                        + "selection")
                        .accepts("sort-clear")
                            .withAliases("s", "c")
                            .describeAs("Clears the preferred sorting order.")
                );

                put("sort", new FlagSet()
                        .accepts("descending")
                            .withAliases("d", "r", "reverse")
                            .describeAs("Reverses the order in which entries are shown")
                );

                put("view", new FlagSet()
                        .accepts("grades-only")
                            .withAliases("g")
                            .describeAs("Shows only the grades of the selected students "
                                        + "(not the feedback they received)")
                        .accepts("no-source-names")
                            .withAliases("n", "s")
                            .describeAs("Removes source names from the feedback "
                                        + "(useful for copying and pasting feedback)")
                        .disallowTogether("grades-only", "no-source-names")
                );
    }};


    /**
     * This map contains the list of commands that can be used even if the user has no
     * students selected. All other commands require the student to select at least one
     * student first.
     */
    private static final ArrayList<String> USABLE_WITHOUT_SELECTION
            = new ArrayList<String>() {{
                add("exit");
                add("file");
                add("help");
                add("list");
                add("select");
    }};


    /**
     * Represents the list of selected students.
     */
    private static StudentSelection currentSelection = null;


    /**
     * Handles the command-line interface (CLI) loop for AutoGrade.
     */
    public static void startAcceptingCommands() {
        if (AutoGrade.SHOW_HELP) {
            help(new FlagParser(new FlagSet().accepts("no-fundamentals"),
                                "--no-fundamentals"));
        }

        while (true) {
            String commandInput = AutoGrade.readCommand();

            try {
                String[] piped
                        = commandInput.contains("|") ? commandInput.split(" *\\| *")
                                                     : new String[] { commandInput };

                Channel.muteAll();
                for (int i = 0; i < piped.length; ++i) {
                    if (i + 1 == piped.length) {
                        Channel.unmuteAll();
                    }

                    String pipedCommand = piped[i];
                    String[] split = pipedCommand.split(" ");

                    String command = mapAliasToCommand(split[0]);

                    FlagParser args;
                    if (COMMAND_FLAGSETS.containsKey(command)) {
                        args = new FlagParser(COMMAND_FLAGSETS.get(command), split);
                    } else {
                        args = new FlagParser(split);
                    }

                    args.setOffset(1);

                    if (currentSelection == null &&
                            !USABLE_WITHOUT_SELECTION.contains(command) &&
                            COMMAND_DESCRIPTIONS.containsKey(command)) {
                        throw new RuntimeException("No students have been selected yet!");
                    }

                    switch (command) {
                        case "delete":   delete(args);
                                         break;

                        case "deselect": deselect(args);
                                         break;

                        case "exit":     exit(args);
                                         break;

                        case "export":   export(args);
                                         break;

                        case "file":     file(args);
                                         break;

                        case "help":     help(args);
                                         break;

                        case "inspect":  run(args);
                                         break;

                        case "list":     list(args);
                                         break;

                        case "select":   select(args);
                                         break;

                        case "sort":     sort(args);
                                         break;

                        case "view":     view(args);
                                         break;

                        default:
                            throw new RuntimeException("Unknown command '" + command
                                    + "'; use the 'help' command for a list of valid "
                                    + "commands.");
                    }
                }
            }

            catch (final RuntimeException e) {
                Channel.unmuteAll();

                if (e instanceof CommandUsageException) {
                    CommandUsageException ex = (CommandUsageException) e;

                    if (ex.hasDescription()) {
                        Channel.INTERACTION.say(RED + ex.getDescription() + RESET);
                    }

                    String command = ex.getCommand();
                    help(new FlagParser(new FlagSet(), command));
                } else {
                    Channel.INTERACTION.say(RED + e.getMessage() + RESET);
                }

                if (AutoGrade.SHOW_STACK_TRACES) {
                    e.printStackTrace();
                }
            }

            for (FlagSet f : COMMAND_FLAGSETS.values()) {
                f.clear();
            }
        }
    }


    /**
     * Maps the given command input to an actual command. The string given to this
     * method would be the first 'word' inputted by the user; for instance, if the
     * user types in
     *
     * $ s *
     *
     * this method would receive "s" and would return "select".
     *
     * @param input (String) the command the user typed in
     * @return (String) the primary name of this command, if it exists
     */
    private static String mapAliasToCommand(String input) {
        for (String command : COMMAND_ALIASES.keySet()) {
            if (input.equalsIgnoreCase(command)) {
                return command;
            }

            for (String alias : COMMAND_ALIASES.get(command)) {
                if (input.equalsIgnoreCase(alias)) {
                    return command;
                }
            }
        }

        return input;
    }


    /**
     * Deletes the submissions of the selected students.
     *
     * autograde $ delete [--grade-too] [--confirm-all]
     *
     * @param args (FlagParser) the arguments given by the user.
     */
    private static void delete(FlagParser args) {

    }


    /**
     * Deselects the given students.
     *
     * autograde $ deselect <filter|*> [--invert]
     *
     * @param args (FlagParser) the arguments given by the user.
     */
    private static void deselect(FlagParser args) {

    }


    /**
     * Exits AutoGrade.
     *
     * autograde $ exit [--discard]
     *
     * @param args (FlagParser) the arguments given by the user.
     */
    private static void exit(FlagParser args) {
        // TODO: Flesh out
        AutoGrade.close();
    }


    /**
     * Exports AutoGrade's findings to a text file or CSV.
     *
     * autograde $ export [<file>|<dir>] [--separate-files|--csv]
     *             [--overwrite] [--grades-only|--no-source-names]
     *
     * @param args
     */
    private static void export(FlagParser args) {

    }

    private static void file(FlagParser args) {
        boolean preferenceChanged = true;
        if (args.hasFlag("vi")) {
            EditorPreference.PREFERENCE = EditorPreference.VI;
        } else if (args.hasFlag("emacs")) {
            EditorPreference.PREFERENCE = EditorPreference.EMACS;
        } else if (args.hasFlag("console")) {
            EditorPreference.PREFERENCE = EditorPreference.NONE;
        } else {
            preferenceChanged = false;
        }

        if (preferenceChanged) {
            String pref = (EditorPreference.PREFERENCE == EditorPreference.NONE ?
                             "the console"
                           : EditorPreference.PREFERENCE.name().toLowerCase());

            Channel.INTERACTION.say("Text editor set to " + pref + " for this session.");
        }

        Student student = getSingularStudent(args);

        int submissions = student.getSubmissions().size();
        switch (submissions) {
            case 0:
                throw new RuntimeException(student.getName() + " has no submissions!");

            case 1:
                File file = student.getSubmissions().get(0).getFile();
                String address = file.getAbsolutePath();
                EditorPreference.PREFERENCE.openEditor(address);
                break;

            default:
                Channel.INTERACTION.say("Student has " + submissions
                        + " files to choose from. Please specify the number you want,"
                        + " or 'cancel' to cancel:");

                for (int i = 0; i < submissions; ++i) {
                    Channel.INTERACTION.say("  " + CYAN.toString() + (i + 1) + RESET + ". "
                            + student.getSubmissions().get(i).getFileName());
                }

                int selectionIndex;

                do {
                    Channel.INTERACTION.sayNoNewline(RESET + "autograde:file $ " + PURPLE);
                    String input = Channel.INTERACTION.ask().trim();

                    if (input.isEmpty()) {
                        continue;
                    }

                    if (input.matches("(?i)(e(xit)?|c(ancel)?|q(uit)?|x)")) {
                        return;
                    }

                    if (!input.matches("-?\\d+")) {
                        if (input.matches("-?\\d*\\.\\d+")) {
                            Channel.INTERACTION.say(RED + "Please enter whole numbers only!"
                                    + RESET);
                        } else {
                            Channel.INTERACTION.say(RED + "Please input a number!" + RESET);
                        }

                        continue;
                    }

                    selectionIndex = Integer.parseInt(input);
                    if (selectionIndex <= 0 || selectionIndex > submissions) {
                        Channel.INTERACTION.say(RED + "Please input a number between 1 and "
                            + submissions + "!");
                        continue;
                    }

                    break;
                } while (true);

                file = student.getSubmissions().get(selectionIndex - 1).getFile();
                address = file.getAbsolutePath();
                EditorPreference.PREFERENCE.openEditor(address);
        }
    }


    /**
     * autograde $ help [<command>]
     *
     * @param args
     */
    public static void help(FlagParser args) {
        if (args.length() == 0) {
            Channel.INTERACTION.say(GREEN + "Command list:" + RESET);
            Channel.INTERACTION.say(Helper.elegantPrintMap(COMMAND_DESCRIPTIONS));

            Channel.INTERACTION.say("Type " + GREEN + "help <command>" + RESET +
                    " for more information about a specific command.");

            if (args.hasFlag("no-fundamentals")) {
                return;
            }

            Channel.INTERACTION.say();
            Channel.INTERACTION.say(GREEN + "Fundamentals" + RESET);
            Channel.INTERACTION.say("To work with auto-graded submissions, you select " +
                               "students using the " + CYAN + "select" + RESET +
                               " command. After that, you can use the commands " +
                               "listed above to perform actions on the submissions of " +
                               "the students you've selected.");
        }

        else {
            String command = mapAliasToCommand(args.get(0));
            ArticleManager am = AutoGrade.getArticles();

            switch (command) {
                case "delete":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:delete"));
                    break;

                case "deselect":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:deselect"));
                    break;

                case "exit":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:exit"));
                    break;
                    
                case "export":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:export"));
                    break;

                case "file":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:file"));
                    break;

                case "help":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:help"));
                    break;

                case "list":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:list"));
                    break;

                case "run":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:run"));
                    break;

                case "save":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:save"));
                    break;

                case "select":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:select"));
                    break;

                case "sort":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:sort"));
                    break;

                case "view":
                    Channel.INTERACTION.say(am.getElement("ARTICLE:view"));
                    break;

                case "about":
                    Channel.INTERACTION.say(GREEN + "AutoGrade 0.5.0(b)");
                    Channel.INTERACTION.say(CYAN + "by Sahir Shahryar "
                        + "<sahirshahryar@uga.edu>");
                    break;

                default:
                    Channel.INTERACTION.say("Unknown command '" + command + "'; " +
                            "use the 'help' command for a list of commands.");
            }
        }
    }


    /**
     * autograde $ list
     */
    private static void list(FlagParser args) {
         if (AutoGrade.CENSOR) {
             throw new RuntimeException("The list command is unavailable when student "
                     + "names are censored.");
         }

         if (args != null && args.length() > 0) {
             throw new CommandUsageException("list", "The list command does not accept "
                                                     + "any arguments.");
         }

         StudentSelection sel = new StudentSelection(AutoGrade.accessStudents().values());
         sel.sort(AutoGrade.getExportSortOrder());

         ArrayList<String> names = sel.getNames();

        Channel.INTERACTION.say(names.size()
                            + (names.size() == 1 ? " student: " : " students: ")
                            + Helper.elegantPrintList(names));
    }


    public static void run(FlagParser args) {
        Student student = getSingularStudent(args);

        if (args.hasFlag("manual")) {
            // TODO: stepthrough execution
        } else {
            if (args.hasFlag("java")) {
                throw new RuntimeException("The --java option can only be used with " +
                        "manual execution!");
            }

            boolean automatic = !args.hasFlag("stepped");

            Script gradingScript = AutoGrade.GRADING_SCRIPT;
            Channel currentChannel = Channel.currentSetting;

            if (automatic) {
                Channel.disableStepthroughHang();
            }

            if (!Channel.STEPTHROUGH_PROGRAM.set()) {
                throw new RuntimeException("Unable to use the stepthrough mode " +
                        "because channel settings are locked to a lower verbosity " +
                        "setting than is required for step-through. If you used a " +
                        "flag limiting verbosity, please re-run with that flag " +
                        "omitted.");
            }

            try {
                gradingScript.gradeSubmission(student);
            } catch (final ManualGradingError e) {
                throw new RuntimeException("Encountered a grading error requiring "
                        + "manual grading: " + e.getMessage()
                        + "\nExiting submission...");
            }

            currentChannel.set();
            if (automatic) {
                Channel.revertToManualInput();
            }
        }
    }


    /**
     * autograde $ select <filter|*> [--invert] [--append]
     *
     * @param args
     */
    private static void select(FlagParser args)  {
        if (args.length() == 0) {
            throw new CommandUsageException("select", "No filter provided!");
        }

        String filter = Helper.join(" ", args.asArray());

        if (!args.hasFlag("append") || currentSelection == null) {
            currentSelection = new StudentSelection();
        }

        boolean invert = args.hasFlag("invert");

        int studentsAdded = 0;
        for (Student student : AutoGrade.accessStudents().values()) {
            boolean match = parseFilter(filter, student);

            /**
             * For two booleans a and b, a != b is a simplified way of saying
             *
             * a XOR b
             *
             * So if invert is true but match is false, or if invert is false and
             * match is true, then the student should be added.
             */
            if (invert != match) {
                currentSelection.addStudent(student);
                ++studentsAdded;
            }
        }
        
        SortOrder sorting = AutoGrade.getExportSortOrder();
        currentSelection.sort(sorting);

        ArrayList<String> selectedStudents = currentSelection.getNames();
        int size = selectedStudents.size();

        if (studentsAdded == 0 || size == 0) {
            Channel.INTERACTION.say(RED + "No students match the filter '"
                    + filter + "'!" + RESET);
            if (size > 0) {
                Channel.INTERACTION.say("Selection remains as follows:");
            } else {
                currentSelection = null;
                return;
            }
        }

        Channel.INTERACTION.say(size + (size == 1 ? " student" : " students") + " selected: "
                           + Helper.elegantPrintList(selectedStudents));
    }


    /**
     * autograde $ save [<file>]
     *
     * @param args
     */
    private static void save(FlagParser args) {

    }


    /**
     * autograde $ sort [name|firstname|grade] [--descending]
     *
     * @param args
     */
    private static void sort(FlagParser args) {
        SortOrder mode = AutoGrade.getExportSortOrder();

        if (args.hasFlag("reverse")) {
            mode = mode.reverse();
        }

        if (args.length() == 1) {
            String modeInput = args.get(0);
            boolean descending = args.hasFlag("reverse");

            if (modeInput.matches("(?i)(la?s?t?)?na?m?e?")) {
                mode = descending ? LAST_NAME_DESC : LAST_NAME_ASC;
            }

            else if (modeInput.matches("(?i)fi?r?s?t?n?a?m?e?")) {
                mode = descending ? FIRST_NAME_DESC : FIRST_NAME_ASC;
            }

            else if (modeInput.matches("(?i)gr?a?d?e?")) {
                mode = descending ? GRADE_DESC : GRADE_ASC;
            }

            else {
                throw new CommandUsageException("sort", "Unknown sorting mode '"
                        + modeInput + "'");
            }
        } else if (args.length() > 1) {
            throw new CommandUsageException("The 'sort' command accepts exactly " +
                    "one argument");
        }

        if (mode == AutoGrade.getExportSortOrder()) {
            Channel.INTERACTION.say("Students were already sorted " + mode + ".");
            if (currentSelection != null) {
                ArrayList<String> names = currentSelection.getNames();
                Channel.INTERACTION.say(names.size()
                        + (names.size() == 1 ? " student " : " students ")
                        + "selected: " + Helper.elegantPrintList(names));
            }
        } else {
            AutoGrade.setExportSortOrder(mode);
            if (currentSelection == null) {
                Channel.INTERACTION.say("Exported results will now be sorted " + mode + ".");
            } else {
                currentSelection.sort(mode);
                ArrayList<String> names = currentSelection.getNames();
                Channel.INTERACTION.say(names.size()
                        + (names.size() == 1 ? " student " : " students ")
                        + "selected: " + Helper.elegantPrintList(names));
            }
        }
    }


    private static void view(FlagParser args) {
        if (currentSelection == null) {
            throw new RuntimeException("No students selected!");
        }

        for (Student student : currentSelection) {
            // Student student = AutoGrade.accessStudents().get(studentName);

            if (student.getFeedback() == null) {
                Channel.INTERACTION.say("Error scoring assignment from " + student.getName());
            }

            else {
                if (args.hasFlag("grades-only")) {
                    Channel.INTERACTION.say(student.getFeedback().getGrade() + "\t\t"
                                       + student.getName());
                } else {
                    Channel.INTERACTION.say(YELLOW + "Score for student "
                            + student.getName() + RESET + ": "
                            + student.getFeedback().getGrade());

                    ArrayList<String> sources = student.getFeedback().getAllSources();
                    String itemIndentation = sources.size() == 1 ? "    " : "       ";

                    for (String source : student.getFeedback().getAllSources()) {
                        ArrayList<String> notes = student.getFeedback().getNotes(source);
                        if (notes.isEmpty()) {
                            continue;
                        }

                        if (sources.size() > 1) {
                            Channel.INTERACTION.say("    From source " + source + ":");
                        }

                        for (String note : student.getFeedback().getNotes(source)) {
                            Channel.INTERACTION.say(itemIndentation + note);
                        }
                    }
                }
            }
        }
    }

    private static Student getSingularStudent(FlagParser args) {
        if (args.length() == 0) {
            if (currentSelection == null) {
                throw new RuntimeException("No students selected!");
            }

            switch (currentSelection.selectedStudents.size()) {
                case 1:
                    return currentSelection.selectedStudents.get(0);

                default:
                    throw new RuntimeException("Ambiguous selection: 'run' can only be "
                            + "used on one student! Use 'run <name>' to specify a "
                            + "student, or change your selection to only one student.");
            }
        } else {
            String filter = Helper.join(" ", args.asArray());

            for (Student s : AutoGrade.accessStudents().values()) {
                if (parseFilter(filter, s)) {
                    return s;
                }
            }

            throw new RuntimeException("No students match the given filter '" +
                        filter + "'!");
        }
    }

    private static boolean parseFilter(String filter, Student student) {
        /**
         * Logical OR
         */
        if (filter.contains(",")) {
            String[] subfilters = filter.split(",");

            for (String subfilter : subfilters) {
                if (parseFilter(subfilter, student)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Logical AND
         */
        if (filter.contains(";")) {
            String[] subfilters = filter.split(";");

            for (String subfilter : subfilters) {
                if (!parseFilter(subfilter, student)) {
                    return false;
                }
            }

            return true;
        }

        return studentQualifies(filter, student);
    }


    private static boolean studentQualifies(String token, Student student) {
        token = token.trim();

        boolean negate = token.startsWith("!");
        if (negate) {
            token = token.substring(1);
        }

        if (token.equals("*")) {
            return !negate;
        }

        if (token.equalsIgnoreCase("@incomplete")) {
            return negate == (student.wasErrorFree() && student.getFeedback() != null);
        }

        if (token.equalsIgnoreCase("@complete")) {
            return negate != (student.wasErrorFree() && student.getFeedback() != null);
        }

        if (token.startsWith(">")) {
            boolean orEqual = token.substring(1).startsWith("=");
            try {
                double score = Double.parseDouble(token.substring(orEqual ? 2 : 1));

                if (student.getFeedback() == null) {
                    return false;
                }

                if (orEqual) {
                    return negate != (student.getFeedback().getGrade() >= score);
                } else {
                    return negate != (student.getFeedback().getGrade() > score);
                }
            }

            catch (final NumberFormatException e) {
                String symbol = (orEqual ? ">=" : ">");
                throw new RuntimeException("Filter specifier " + symbol
                        + "... requires a number immediately afterward; "
                        + "was given " + token + " instead (valid example: '"
                        + symbol + "75.0')");
            }
        }

        if (token.startsWith("<")) {
            boolean orEqual = token.substring(1).startsWith("=");
            try {
                double score = Double.parseDouble(token.substring(orEqual ? 2 : 1));

                if (student.getFeedback() == null) {
                    return false;
                }

                if (orEqual) {
                    return negate != (student.getFeedback().getGrade() <= score);
                } else {
                    return negate != (student.getFeedback().getGrade() < score);
                }
            }

            catch (final NumberFormatException e) {
                String symbol = (orEqual ? ">=" : ">");
                throw new RuntimeException("Filter specifier " + symbol
                        + "... requires a number immediately afterward; "
                        + "was given " + token + " instead (valid example: '"
                        + symbol + "75.0')");
            }
        }

        if (token.startsWith("=")) {
            try {
                double score = Double.parseDouble(token.substring(1));

                if (student.getFeedback() == null) {
                    return false;
                }

                return negate != (Helper.roughlyEqual(student.getFeedback().getGrade(),
                                                      score));
            }

            catch (final NumberFormatException e) {
                throw new RuntimeException("Filter specifier =... requires a number " +
                        "immediately thereafter; was given " + token
                        + " instead (valid example: '=75.0')");
            }
        }

        return negate != student.getTrueName()
                                .toLowerCase().contains(token.toLowerCase());
    }


    private static class StudentSelection implements Iterable<Student> {

        private ArrayList<Student> selectedStudents;

        public StudentSelection() {
            this.selectedStudents = new ArrayList<>();
        }

        public StudentSelection(Collection<Student> students) {
            this.selectedStudents = new ArrayList<>(students);
        }

        public void addStudent(Student student) {
            if (!selectedStudents.contains(student)) {
                selectedStudents.add(student);
            }
        }

        public boolean hasStudent(String name) {
            return this.selectedStudents.contains(name);
        }

        public void sort(SortOrder mode) {
            this.selectedStudents = mode.sort(this.selectedStudents);
        }

        public ArrayList<String> getNames() {
            ArrayList<String> properNames = new ArrayList<>();

            for (Student student : this.selectedStudents) {
                properNames.add(GREEN + student.getName() + RESET);
            }

            return properNames;
        }


        @Override
        public Iterator<Student> iterator() {
            return this.selectedStudents.iterator();
        }
    }


}
