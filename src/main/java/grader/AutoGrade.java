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
package grader;

import grader.articles.ArticleManager;
import grader.backend.*;
import grader.flag.FlagParser;
import grader.flag.FlagSet;
import grader.frontend.Channel;
import grader.frontend.CommandHandler;
import grader.frontend.SortOrder;
import grader.reflect.InternalCompiler;
import grader.reflect.ReflectionAssistant;
import grader.reflect.SourceUtilities;
import grader.stability.ExitBlocker;
import grader.stability.ExitException;
import grader.util.Helper;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import static grader.frontend.Color.*;

/**
 * "Meanwhile @Sahir Shahryar literally programming himself out of a job"
 * - Austin
 *
 * @author  Sahir Shahryar
 * @since   Monday, January 15, 2018
 * @version 1.0.0
 */
public class AutoGrade {

    /**
     * The runtime is used to execute system commands, such as (but not necessarily
     * limited to) javac.
     */
    private static final Runtime runtime = Runtime.getRuntime();

    /**
     * This is a debug statement which toggles whether stack traces are printed alongside
     * external error messages.
     */
    public static final boolean SHOW_STACK_TRACES = true;

    // THE FIELDS BELOW ARE SETTINGS DETERMINED BY FLAGS PASSED TO ./autograde.

    /**
     * This value represents the number of seconds that AutoGrade will allow each
     * method call via {@link ReflectionAssistant} to run. It can be set to -1 to
     * disable the timeout. This value is controlled by the {@code --timeout} command.
     */
    public static int TIMEOUT_SECONDS = 3;


    /**
     * These are the arguments passed to 'javac' when compiling the grading script and
     * any ancillary scripts. This value is controlled by the {@code --javac} command.
     */
    public static String JAVAC_ARGS = null;


    /**
     * This is the location where submissions are moved to be worked with.
     */
    public static final String TEMP_PACKAGE = "temp";


    /**
     * Determines whether or not the output from each individual submission is muted
     * while grading. This value is controlled by the {@code --verbose} command.
     */
    public static boolean VERBOSE = false;


    /**
     * Determines whether or not AutoGrade asks prior to deleting a file it didn't
     * create. This value is controlled by the {@code --auto-delete} command.
     */
    public static boolean PROMPT_AUTO_DELETIONS = true;


    /**
     * Determines whether the program immediately exports and quits after it finishes
     * grading. This value is controlled by the {@code --auto-quit} command.
     *
     *
     * TODO: Implement this functionality.
     */
    public static boolean EXPORT_AND_QUIT_AUTOMATICALLY = true;


    /**
     * Determines whether the program silences all output and simply pipes all generated
     * feedback to the console (for piping).
     */
    public static boolean PIPE = false;


    /**
     * Determines whether the program shows colors or not. It may be useful to disable
     * this if the terminal doesn't support colors. This value is controlled by the
     * {@code --no-color} command.
     */
    public static boolean SHOW_COLORS = true;


    /**
     * Determines whether or not a list of AutoGrade commands should be shown after it
     * finishes grading. This value is controlled by the {@code --no-help} command.
     */
    public static boolean SHOW_HELP = true;


    /**
     * Determines whether or not student's names should be censored. This may be useful
     * for demonstrating AutoGrade's functionality to non-faculty. This value is
     * controlled by the {@code --censor} command.
     */
    public static boolean CENSOR = false;


    /**
     * Determines whether or not AutoGrade attempts to load ancillary scripts (such as
     * StyleAnalysis). This value is controlled by the {@code --no-ancillary} command.
     */
    public static boolean ENABLE_ANCILLARY_SCRIPTS = true;


    /**
     * Determines where AutoGrade finds student submissions to grade. By default, it's
     * the current directory. This value is controlled by the optional second argument
     * to {@code ./autograde}, {@code [submission-folder]}.
     */
    public static File WORKING_DIRECTORY = null;


    /**
     * This value is set to {@code false} if the user has not acknowledged the risk of
     * running AutoGrade in a folder that doesn't contain exclusively eLC submissions.
     * It's set to {@code true} once the user types "yes" in response to a prompt
     * asking them about the aforementioned problem, or it can be set to true at the
     * start by including the {@code --no-warnings} command.
     */
    public static boolean DIRECTORY_WARNING_ACKNOWLEDGED = false;


    /**
     * This value determines if the 'temp' directory was created by AutoGrade in the
     * current run. If it was, AutoGrade can clean it up automatically.
     *
     * TODO: Find workaround for the inability to delete compiled .class files.
     */
    public static boolean TEMP_DIRECTORY_CREATED_BY_US = false;


    public static Script GRADING_SCRIPT = null;


    /**
     * This map contains a list of students mapped by their names. The names are kept
     * in lowercase; their respective {@link Student} objects keep the actual capitalized
     * names.
     */
    private static HashMap<String, Student> students = new HashMap<>();


    /**
     * Order in which results will be exported to a text file.
     */
    private static SortOrder exportSortOrder = SortOrder.LAST_NAME_ASC;


    /**
     * This
     */
    private static ArrayList<File> unassociatedFiles = new ArrayList<>();


    /**
     *
     */
    private static ArrayList<File> byproductFiles = new ArrayList<>();

    private static ArticleManager articles = null;


    /**
     *
     */
    public static final FlagSet COMMAND_OPTIONS = new FlagSet()
                    .accepts("timeout", "3")
                        .withAliases("t")
                        .withParamName("<timeout>")
                        .describeAs("specifies maximum length a method can run, in " +
                                "seconds. -1 to disable")
                    .accepts("verbose")
                        .withAliases("v")
                        .describeAs("show ALL program output. (WARNING: it's a lot!)")
                    .accepts("auto-delete")
                        .withAliases("d")
                        .describeAs("automatically delete redundant files without a " +
                                   "prompt")
                    .accepts("auto-quit")
                        .withAliases("q", "eq")
                        .describeAs("export results and quit immediately")
                    .accepts("no-color")
                        .describeAs("don't show colors")
                    .accepts("no-help")
                        .withAliases("h")
                        .describeAs("don't show command help prior to accepting " +
                                    "commands")
                    .accepts("no-warnings")
                        .withAliases("w")
                        .describeAs("don't warn me about running the program in a " +
                                    "folder that has other files in it")
                    .accepts("no-ancillary")
                        .withAliases("an")
                        .describeAs("don't attempt to compile or run any ancillary " +
                                    "scripts (e.g., StyleAnalysis)")
                    .accepts("attendance")
                        .withAliases("a")
                        .describeAs("adds the given week's attendance to each " +
                                "student's feedback")
                    .accepts("censor")
                        .withAliases("c")
                        .describeAs("censor student names (good for demonstrations)")
                    .accepts("cat")
                        .withAliases("pipe", "p")
                        .describeAs("prints all generated feedback to console (good for "
                                    + "piping)")
                    .accepts("javac", "")
                        .withAliases("j")
                        .withParamName("\"<args...>\"")
                        .describeAs("special arguments to give javac when compiling " +
                                    "the grading script. Surround with quotes if you're "
                                    + "providing more than one argument");


    public static void main(String[] args) {
        PrintStream out = System.out;
        try {
            run(args);
        } catch (final Throwable t) {
            System.setOut(out);
            System.out.println("Program crashed, oops");
            t.printStackTrace(out);
        }
    }

    /**
     * This is the 
     * @param array
     */
    @SuppressWarnings("unchecked")
    public static void run(String[] array) {
        /**
         * First, try to parse the command arguments for flags.
         */
        FlagParser args;
        try {
            args = new FlagParser(COMMAND_OPTIONS, array);

            articles = new ArticleManager();
            InputStream helpFile = AutoGrade.class.getResourceAsStream("/help.txt");
            articles.addFile(new InputStreamReader(helpFile), false);
        } catch (final RuntimeException e) {
            System.out.println(e.getMessage());
            return;
        }

        /**
         * If the command is not formatted correctly, then show command usage and exit.
         */
        if (args.length() != 1 && args.length() != 2) {
            AutoGrade.printUsage();
            return;
        }

        /**
         * Set configurable values based on the values of flags that the user passed.
         */
        VERBOSE                        =  args.hasFlag("verbose");
        PROMPT_AUTO_DELETIONS          = !args.hasFlag("auto-delete");
        EXPORT_AND_QUIT_AUTOMATICALLY  =  args.hasFlag("auto-quit");
        SHOW_COLORS                    = !args.hasFlag("no-color");
        SHOW_HELP                      = !args.hasFlag("no-help");
        DIRECTORY_WARNING_ACKNOWLEDGED =  args.hasFlag("no-warnings");
        ENABLE_ANCILLARY_SCRIPTS       = !args.hasFlag("no-ancillary");
        CENSOR                         =  args.hasFlag("censor");
        PIPE                           =  args.hasFlag("cat");
        JAVAC_ARGS                     =  args.hasFlag("javac") ? args.getValue("javac")
                                                                : null;


        /**
         * 
         */
        if (args.length() == 1) {
            WORKING_DIRECTORY = new File("");
        } else {
            WORKING_DIRECTORY = new File(args.get(1));
        }

        if (args.hasFlag("timeout")) {
            try {
                TIMEOUT_SECONDS = Integer.parseInt(args.getValue("timeout"));
            } catch (final NumberFormatException e) {
                System.out.println("Invalid input for option --timeout (must be an " +
                        "integer)");
                return;
            }
        }

        /**
         *
         */
        try {
            Class gradingClass;
            String scriptFilename = args.get(0);

            File scriptFile = new File(scriptFilename);
            
            if (scriptFilename.toLowerCase().endsWith(".java")) {
                if (!scriptFile.exists()) {
                    loadingError(scriptFilename, "the specified file does not exist");
                    return;
                }

                gradingClass = InternalCompiler.compile(scriptFile,
                        (JAVAC_ARGS != null) ? JAVAC_ARGS : "-cp .:AutoGrade.jar");
            } else {
                ClassLoader loader = AutoGrade.class.getClassLoader();
                gradingClass = loader.loadClass(args.get(0));
            }

            if (gradingClass == null) {
                throw new ManualGradingError("The grader Class<?> is somehow null");
            }

            if (!Script.class.isAssignableFrom(gradingClass)) {
                loadingError(scriptFilename, gradingClass.getSimpleName()
                        + " does not implement grader.backend.Script interface");
                return;
            }

            GRADING_SCRIPT =
                    (Script) ReflectionAssistant.constructObjectNoTimeout(gradingClass);
        } catch (final ClassNotFoundException ex) {
            loadingError(args.get(0), "The specified class could not be found");
            return;
        } catch (final ManualGradingError e) {
            System.out.println("Error while attempting to compile grading script: ");
            System.out.println("\t" + e.getMessage());

            if (SHOW_STACK_TRACES) {
                e.printStackTrace();
            }

            return;
        } catch (final IllegalArgumentException | InvocationTargetException ex) {
            System.out.println("Error while constructing the grading script: ");
            System.out.println("\t" + ex.getMessage());

            if (SHOW_STACK_TRACES) {
                ex.printStackTrace();
            }

            return;
        }


        if (GRADING_SCRIPT == null) {
            System.out.println("The grading script could not be created");
            return;
        } else {
            System.out.println("Main script '" + GRADING_SCRIPT.getClass().getSimpleName()
                    + "' loaded!");
        }

        if (AutoGrade.WORKING_DIRECTORY == null) {
            System.out.println("The working directory is somehow null");
            return;
        }


        /**
         * The ExitBlocker prevents students' code from "crashing" AutoGrade by using
         * System.exit(0).
         */
        System.setSecurityManager(new ExitBlocker());

        /**
         * Load any extra scripts (like the StyleAnalysis script).
         */
        ArrayList<AncillaryScript> ancillaryScripts = new ArrayList<>();
        if (ENABLE_ANCILLARY_SCRIPTS) {
            String[] scriptsToLoad = GRADING_SCRIPT.listAncillaryScripts();
            for (String scriptName : scriptsToLoad) {
                try {
                    File scriptFile = new File(scriptName + ".java");
                    AncillaryScript newScript;
                    Class newScriptClass;

                    if (scriptFile.exists()) {
                        // TODO: Adjust
                        newScriptClass = InternalCompiler.compile(scriptFile,
                                (JAVAC_ARGS != null) ? JAVAC_ARGS : "-cp .:AutoGrade.jar");
                    } else {
                        scriptFile = new File(scriptName + ".class");

                        if (!scriptFile.exists()) {
                            System.out.println("Could not locate the ancillary script " +
                                    "'" + scriptName + "'. Either a .java or a .class file " +
                                    "with that name must exist within the directory " +
                                    "AutoGrade is being run from. Rerun with the option " +
                                    "--no-ancillary to run without the bonus features in " +
                                    "'" + scriptName + "'.");
                            return;
                        }

                        newScriptClass = AutoGrade.class.getClassLoader()
                                .loadClass(scriptName);
                    }

                    if (newScriptClass == null) {
                        throw new ManualGradingError("Ancillary script Class<?> is " +
                                "somehow null");
                    }

                    if (!AncillaryScript.class.isAssignableFrom(newScriptClass)) {
                        throw new ManualGradingError("Ancillary script '" + scriptName +
                                " does not implement AncillaryScript");
                    }

                    newScript =
                            (AncillaryScript) ReflectionAssistant
                                    .constructObjectNoTimeout(newScriptClass);

                    ancillaryScripts.add(newScript);
                    System.out.println("Ancillary script '" + scriptName + "' loaded!");
                }

                catch (final ManualGradingError e) {
                    System.out.println("Error compiling ancillary script '" +
                            scriptName + "':");
                    System.out.println(e.getMessage());
                    System.out.println("Rerun with the option --no-ancillary to " +
                            "run without the bonus features in that script.");

                    return;
                }

                catch (final ClassNotFoundException e) {
                    System.out.println("Could not load class '" + scriptName + "'. It " +
                            "may be compiled to be in the wrong package. If possible, " +
                            "consider using the .java version of the script to " +
                            "automatically resolve this issue, or rerun with the option " +
                            "--no-ancillary to run without the bonus features in that " +
                            "script.");

                    return;
                }

                catch (final IllegalArgumentException | InvocationTargetException e) {
                    System.out.println("Unexpected error while compiling or " +
                            "instantiating ancillary script '" + scriptName + "':");
                    if (SHOW_STACK_TRACES) {
                        e.printStackTrace();
                    }
                    System.out.println("Rerun with the option --no-ancillary to run " +
                            "without the bonus features in that script.");
                    return;
                }
            }
        }


        /**
         *
         */
        if (!AutoGrade.WORKING_DIRECTORY.isDirectory()) {
            System.out.println("Error: the submissions \"folder\" specified isn't " +
                    "actually a directory!");
            return;
        }


        /**
         *
         */
        for (File file : AutoGrade.WORKING_DIRECTORY.listFiles()) {
            if (!GRADING_SCRIPT.fileBelongs(file)
                    && !file.getName().equals("index.html")) {
                if (!AutoGrade.DIRECTORY_WARNING_ACKNOWLEDGED) {
                    showDirectoryWarning();
                }

                continue;
            }

            if (!GRADING_SCRIPT.fileBelongsToStudent(file)) {
                continue;
            }

            String studentName = Student.determineStudentName(file);
            if (studentName.contains("unable to determine student name")) {
                unassociatedFiles.add(file);
                continue;
            }

            ELCSubmission submission = new ELCSubmission(file);

            if (!students.containsKey(studentName.toLowerCase())) {
                Student newStudent = new Student(submission);
                students.put(studentName.toLowerCase(), newStudent);
            } else {
                students.get(studentName.toLowerCase()).addFile(submission);
            }
        }


        /**
         *
         */
        if (students.isEmpty()) {
            System.out.println("Could not identify any student submissions " +
                    "automatically! Please make sure that their files' names are " +
                    "formatted correctly by downloading them directly from eLC.");
            return;
        }


        /**
         *
         */
        PrintStream oldOut = System.out;
        int validGrades = 0, invalidGrades = 0, duplicatesRemoved = 0;
        for (String studentName : students.keySet()) {
            Student student = students.get(studentName);

            /**
             *
             */
            System.out.print(getProgressBar(student.getName(),
                    (validGrades + invalidGrades + 1), students.keySet().size()));

            /**
             *
             */
            try {
                duplicatesRemoved += student.cleanUpDuplicates();
                student.setScore(GRADING_SCRIPT.gradeSubmission(student));


                for (AncillaryScript bonusScript : ancillaryScripts) {
                    student.getFeedback()
                           .setSource(bonusScript.getClass().getSimpleName());

                    student.setScore(bonusScript.addAdditionalFeedback(student));
                }

                ++validGrades;
            }

            /**
             *
             */
            catch (final ManualGradingError | ExitException e) {
                System.setOut(oldOut);
                student.appendException(e);
                ++invalidGrades;
            }
            
            catch (final Throwable t) {
                System.setOut(oldOut);
                t.printStackTrace();
                break;
            }
        }

        /**
         *
         */
        System.out.print(getProgressBar(null, students.keySet().size() + 1,
                students.keySet().size()));

        System.out.println();

        if (duplicatesRemoved > 0) {
            System.out.println("Removed " + duplicatesRemoved + " duplicate "
                + (duplicatesRemoved == 1 ? "submission" : "submissions")
                + " from consideration.");
        }

        /**
         *
         */
        if (validGrades == 0) {
            System.out.println("None of the students' submissions could be graded " +
                    "automatically! Exiting...");
            return;
        }

        /**
         *
         */
        if (invalidGrades == 0) {
            System.out.println("All " + validGrades + " submissions were automatically gradable!");
        } else {
            int total = validGrades + invalidGrades;
            System.out.println("Successfully graded " + validGrades
                    + (validGrades == 1 ? " student's assignment"
                      : " students' assignments")
                    + (total != 1 ? " (out of " + total + ")." : "."));
        }

        /**
         *
         */
        if (EXPORT_AND_QUIT_AUTOMATICALLY) {
            // TODO

            return;
        }

        try {
            CommandHandler.startAcceptingCommands();
        }

        catch (final Throwable t) {
            System.out.println(RED + "Fatal error: " + t.getMessage() + RESET);
            close();
        }
    }


    /**
     *
     */
    public static void close() {
        ArrayList<String> unsuccessfulDeletions = new ArrayList<>();

        for (File f : byproductFiles) {
            if (!f.delete()) {
                unsuccessfulDeletions.add(f.getName());
            }
        }

        if (!unsuccessfulDeletions.isEmpty()) {
            System.out.println("Could not delete " +
                    Helper.elegantPrintList(unsuccessfulDeletions));
        }

        /**
         * Funnily enough, setting a SecurityManager to block System.exit() calls ended
         * up blocking OUR System.exit() call, too!
         */
        SecurityManager sm = System.getSecurityManager();

        if (sm instanceof ExitBlocker) {
            ((ExitBlocker) sm).permitExit();
        }

        System.exit(0);
    }


    /**
     *
     * @param file
     */
    public static void addByproductFile(File file) {
        if (!byproductFiles.contains(file)) {
            byproductFiles.add(file);
        }
    }


    /**
     *
     * @param file
     * @return
     */
    public static boolean isByproductFile(File file) {
        return byproductFiles.contains(file);
    }


    /**
     *
     * @param name
     * @param error
     */
    private static void loadingError(String name, String error) {
        System.out.println("Error loading grading script '" + name + "': " + error);
    }


    /**
     *
     * @param file
     * @throws ManualGradingError
     */
    public static void promptToDelete(File file) throws ManualGradingError {
        if (!PROMPT_AUTO_DELETIONS ||
                promptBoolean("Delete file " + file.getName() + "?")) {
            if (!file.delete()) {
                throw new ManualGradingError("Could not delete preexisting " +
                        file.getName() + " to write over; please rerun after " +
                        "fixing this issue");
            }
        }

        else {
            throw new RuntimeException("Not allowed to delete " + file.getName());
        }
    }


    /**
     *
     * @return
     */
    public static boolean promptBoolean(String originalQuestion) {
        boolean repeat = false;
        String[] yeses = { "yes", "y", "true", "t" }, nos = { "no", "n", "false", "f" };

        while (true) {
            originalQuestion += " (yes/no)";
            String repeatQuestion = "Please input either 'yes' or 'no':";

            String answer = Channel.INTERACTION.ask(repeat ? repeatQuestion
                                                           : originalQuestion);

            for (String yes : yeses) {
                if (answer.equalsIgnoreCase(yes)) {
                    return true;
                }
            }

            for (String no : nos) {
                if (answer.equalsIgnoreCase(no)) {
                    return false;
                }
            }

            repeat = true;
        }
    }


    /**
     *
     */
    public static void showDirectoryWarning() {
        AutoGrade.DIRECTORY_WARNING_ACKNOWLEDGED = true;
        String warning = RED + "WARNING" + RESET + ": AutoGrade is highly automated " +
                "and deletes submission files automatically after grading them. It is " +
                "highly recommended that you run AutoGrade in the context of a folder " +
                "containing exclusively eLC submissions. Are you SURE you want to " +
                "proceed?";

        if (promptBoolean(warning)) {
            Channel.INTERACTION.say("Proceeding...");
        } else {
            Channel.INTERACTION.say("Exiting...");
            AutoGrade.close();
        }
    }


    /**
     *
     * @return
     */
    public static String readCommand() {
        String line;

        do {
            Channel.INTERACTION.say(RESET + "autograde $ " + CYAN);
            line = Channel.INTERACTION.ask();
        } while (line.trim().isEmpty());

        Channel.INTERACTION.say(RESET);

        return line;
    }

    public static SortOrder getExportSortOrder() {
        return exportSortOrder;
    }


    public static void setExportSortOrder(SortOrder newSortOrder) {
        exportSortOrder = newSortOrder;
    }

    public static ArticleManager getArticles() {
        return articles;
    }


    /**
     *
     * @return
     */
    public static HashMap<String, Student> accessStudents() {
        return students;
    }


    /**
     *
     * @param student
     * @param current
     * @param max
     * @return
     */
    private static String getProgressBar(String student, int current, int max) {
        String result = "Progress: "
                      + rightAlign(Math.min(max, current) + "",
                                   (int) Math.max(1, Math.log10(max) + 1))
                      + " / " + max + " [";

        final int PROGRESS_BAR_WIDTH = 20;

        int hashes = (int) (((float) current / max) * PROGRESS_BAR_WIDTH);
        result += fillWidth(Math.min(hashes, PROGRESS_BAR_WIDTH), '#');
        result += fillWidth(PROGRESS_BAR_WIDTH - hashes, ' ');

        result += "]";

        if (current > max || CENSOR) {
            return result + "                                    \r";
        } else {
            return result + " (" + student + ")                 \r";
        }
    }


    /**
     *
     * @param value
     * @param width
     * @return
     */
    private static String rightAlign(String value, int width) {
        return fillWidth(width - value.length(), ' ') + value;
    }


    /**
     *
     * @param width
     * @param filler
     * @return
     */
    private static String fillWidth(int width, char filler) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < width; ++i) {
            result.append(filler);
        }

        return result.toString();
    }


    /**
     *
     * @param command
     * @return
     */
    public static int executeCommand(String command) {
        try {
            int ret = runtime.exec(command).waitFor();
            System.out.println("Exit code for '" + command + "' = " + ret);
            return ret;
        }

        catch (final InterruptedException | IOException e) {
            System.out.println("Warning: unexpected " + e.getClass().getSimpleName()
                               + " when trying to run the command '" + command + "'");
            if (SHOW_STACK_TRACES) {
                e.printStackTrace();
            }

            return 1;
        }
    }


    public static ArrayList<File> mostRecentFilesOnly(File directory) {
        if (!directory.isDirectory()) {
            return null;
            // This needs to have been verified beforehand.
        }

        ArrayList<File> result = new ArrayList<>();
        HashMap<String, ArrayList<String>> studentFileNames;

        for (File f : directory.listFiles()) {
            if (!GRADING_SCRIPT.fileBelongsToStudent(f)) {
                continue;
            }

            
        }





        return result;
    }


    public static boolean validatePackage(String packageName) {
        if (!packageName
                .matches("[$_a-zA-Z][$_a-zA-Z0-9]*(\\.[$_a-zA-Z][$_a-zA-Z0-9]*)*")) {
            return false;
        }

        String[] split = packageName.split("\\.");

        for (String portion : split) {
            for (String keyword : SourceUtilities.JAVA_KEYWORDS) {
                if (portion.equals(keyword)) {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     *
     */
    public static void printUsage() {
        boolean aliasExists = new File("autograde").exists();

        String syntaxStart = aliasExists ? "./autograde" : "java -jar AutoGrade.jar";


        System.out.println(GREEN + "Syntax: " + RESET);
        System.out.println(YELLOW + "  " + syntaxStart
                + " <script> [submission-folder] [flags...]");
        System.out.println(BLUE + "  (<mandatory>, [optional])");
        System.out.println();
        System.out.println(GREEN + "Flags:");
        System.out.println(Helper.elegantPrintMap(COMMAND_OPTIONS.flagDescriptions()));
    }

}
