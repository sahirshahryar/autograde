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
import grader.backend.ManualGradingError;
import grader.backend.Script;
import grader.backend.Student;
import grader.flag.FlagParser;
import grader.flag.FlagSet;
import grader.util.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static grader.frontend.Color.*;

/**
 * This class handles all of the commands that
 *
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   Thursday, April 26, 2018
 * @version 1.0.0
 */
public class CommandHandler {

    /**
     *
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
     *
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
     * s * | so n | v -g
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
                            .withAliases("n", "s")
                            .describeAs("Removes source names from the feedback "
                                        + "(useful for copying and pasting feedback)")
                        .disallowTogether("grades-only", "no-source-names")
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
                        .accepts("reverse")
                            .withAliases("r")
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
     *
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
     *
     */
    private static StudentSelection currentSelection = null;
    
    private static EditorPreference preferredEditor = null;


    /**
     *
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

                
                for (int i = 0; i < piped.length; ++i) {
                    if (i + 1 == piped.length) {

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

                        case "save":     save(args);
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
                // ConsoleOutput.unmute();

                if (e instanceof CommandUsageException) {
                    CommandUsageException ex = (CommandUsageException) e;

                    if (ex.hasDescription()) {
                        System.out.println(RED + ex.getDescription() + RESET);
                    }

                    String command = ex.getCommand();
                    help(new FlagParser(new FlagSet(), command));
                } else {
                    System.out.println(RED + e.getMessage() + RESET);
                }
            }
        }
    }


    /**
     *
     *
     * @param input
     * @return
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
     * autograde $ delete [--grade-too] [--confirm-all]
     *
     * @param args
     */
    private static void delete(FlagParser args) {

    }


    /**
     * autograde $ deselect <filter|*> [--invert]
     *
     * @param args
     */
    private static void deselect(FlagParser args) {

    }


    /**
     * autograde $ exit [--discard]
     *
     * @param args
     */
    private static void exit(FlagParser args) {
        // TODO: Flesh out
        AutoGrade.close();
    }


    /**
     * autograde $ export [<file>|<dir>] [--separate-files]
     *
     * @param args
     */
    private static void export(FlagParser args) {

    }

    private static void file(FlagParser args) {

    }


    /**
     * autograde $ help [<command>]
     *
     * @param args
     */
    public static void help(FlagParser args) {
        if (args.length() == 0) {
            System.out.println(GREEN + "Command list:" + RESET);
            System.out.println(Helper.elegantPrintMap(COMMAND_DESCRIPTIONS));

            if (args.hasFlag("no-fundamentals")) {
                return;
            }

            System.out.println();
            System.out.println(GREEN + "Fundamentals" + RESET);
            System.out.println("To work with auto-graded submissions, you select " +
                               "students using the " + CYAN + "select" + RESET +
                               " command. After that, you can use the commands " +
                               "listed above to perform actions on the submissions of " +
                               "the students you've selected.");
        }

        else {
            String command = mapAliasToCommand(args.get(0));

            switch (command) {
                case "delete":
                    System.out.println(GREEN + "delete" + RESET);
                    System.out.println("Deletes a student's submission file.");
                    System.out.println();

                    System.out.println(CYAN + "  Aliases:");
                    System.out.println(YELLOW + "    d" + RESET + ", " + YELLOW + "del");

                    System.out.println(CYAN + "  Syntax:");
                    System.out.println(RESET + "    $ " + YELLOW +
                            "delete <student> [--grade-too] [--confirm-all]");
                    System.out.println();

                    System.out.println(CYAN + "Details:");
                    System.out.println(YELLOW + "<student>" + "");

                case "deselect":
                case "errors":
                case "exit":
                case "export":
                case "help":

                    break;

                case "inspect":

                    break;

                case "list":
                case "save":
                case "sort":
                case "view":

                default:
                    System.out.println("Unknown command '" + command + "'; " +
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

         if (args.length() > 0) {
             throw new CommandUsageException("list", "The list command does not accept "
                                                     + "any arguments.");
         }

         ArrayList<String> list = new ArrayList<>();
         for (Student student : AutoGrade.accessStudents().values()) {
             list.add(GREEN + student.getName() + RESET);
         }

        System.out.println(list.size()
                            + (list.size() == 1 ? " student: " : " students: ")
                            + Helper.elegantPrintList(list));
    }


    public static void run(FlagParser args) {
        Student student = null;

        if (args.length() == 0) {
            switch (currentSelection.selectedStudents.size()) {
                case 0:
                    throw new RuntimeException("No students selected!");

                case 1:
                    student = AutoGrade.accessStudents()
                              .get(currentSelection.selectedStudents.get(0));
                    break;

                default:
                    throw new RuntimeException("Ambiguous selection: 'run' can only be "
                            + "used on one student! Use 'run <name>' to specify a "
                            + "student, or change your selection to only one student.");
            }
        } else {
            String filter = Helper.join(" ", args.asArray());

            for (Student s : AutoGrade.accessStudents().values()) {
                if (parseFilter(filter, s)) {
                    student = s;
                    break;
                }
            }

            if (student == null) {
                throw new RuntimeException("No students match the given filter '" +
                        filter + "'!");
            }
        }

        if (args.hasFlag("manual")) {

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
        for (String studentName : AutoGrade.accessStudents().keySet()) {
            Student student = AutoGrade.accessStudents().get(studentName);

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
                currentSelection.addStudent(studentName);
                ++studentsAdded;
            }
        }

        ArrayList<String> selectedStudents = currentSelection.getNames();
        int size = selectedStudents.size();

        if (studentsAdded == 0 || size == 0) {
            System.out.println("No students match the filter '" + filter + "'!");
            if (size > 0) {
                System.out.println("Selection remains as follows:");
            } else {
                currentSelection = null;
                return;
            }
        }

        System.out.println(size + (size == 1 ? " student" : " students") + " selected: "
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
     * autograde $ sort [name|grade] [--descending]
     *
     * @param args
     */
    private static void sort(FlagParser args) {
        
    }


    private static void view(FlagParser args) {
        if (currentSelection == null) {
            throw new RuntimeException("No students selected!");
        }

        for (String studentName : currentSelection) {
            Student student = AutoGrade.accessStudents().get(studentName);

            if (student.getFeedback() == null) {
                System.out.println("Error scoring assignment from " + student.getName());
            }

            else {
                if (args.hasFlag("grades-only")) {
                    System.out.println(student.getFeedback().getGrade() + "\t\t"
                                       + student.getName());
                } else {
                    System.out.println("Score for student " + student.getName() + ": "
                            + student.getFeedback().getGrade());

                    ArrayList<String> sources = student.getFeedback().getAllSources();
                    String itemIndentation = sources.size() == 1 ? "    " : "       ";

                    for (String source : student.getFeedback().getAllSources()) {
                        ArrayList<String> notes = student.getFeedback().getNotes(source);
                        if (notes.isEmpty()) {
                            continue;
                        }

                        if (sources.size() > 1) {
                            System.out.println("    From source " + source + ":");
                        }

                        for (String note : student.getFeedback().getNotes(source)) {
                            System.out.println(itemIndentation + note);
                        }
                    }
                }
            }
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
            try {
                double score = Double.parseDouble(token.substring(1));

                if (student.getFeedback() == null) {
                    return false;
                }

                return negate != (student.getFeedback().getGrade() > score);
            }

            catch (final NumberFormatException e) {
                throw new RuntimeException("Filter specifier >... requires a number " +
                        "immediately thereafter; was given " + token
                        + " instead (valid example: '>75.0')");
            }
        }

        if (token.startsWith("<")) {
            try {
                double score = Double.parseDouble(token.substring(1));

                if (student.getFeedback() == null) {
                    return false;
                }

                return negate != (student.getFeedback().getGrade() < score);
            }

            catch (final NumberFormatException e) {
                throw new RuntimeException("Filter specifier <... requires a number " +
                        "immediately thereafter; was given " + token
                        + " instead (valid example: '<75.0')");
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


    private static class StudentSelection implements Iterable<String> {

        private ArrayList<String> selectedStudents;

        public StudentSelection() {
            this.selectedStudents = new ArrayList<>();
        }

        public void addStudent(String name) {
            if (!selectedStudents.contains(name)) {
                selectedStudents.add(name);
            }
        }

        public boolean hasStudent(String name) {
            return this.selectedStudents.contains(name);
        }

        public ArrayList<String> getNames() {
            ArrayList<String> properNames = new ArrayList<>();

            for (String name : this.selectedStudents) {
                properNames.add(GREEN +
                        AutoGrade.accessStudents().get(name).getName() + RESET);
            }

            return properNames;
        }


        @Override
        public Iterator<String> iterator() {
            return this.selectedStudents.iterator();
        }
    }


}
