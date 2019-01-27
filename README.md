# AutoGrade
**NOTE**: For the purposes of academic honesty, any publicly available version of this program does not include any of the grader scripts.

A lot of the stuff here doesn't quite work correctly yet (the `make` command is a bit wacky, a few of the commands still have to be implemented, and the help pages are not yet visible), but the core functionality of the program — running an entire folder of eLC submissions at once and seeing scores for them — works.


AutoGrade is a Java program that can be used to automatically grade submissions that are downloaded off of eLC. The primary goal is to speed up the grading of assignments which would score a 100 under manual review, while also providing basic feedback for common errors. Some of its features include:

* the ability to run an automatic grading script on an entire folder of eLC submissions at once
* the ability to use any of a number of scripts depending on the assignment (any .java file implementing `grader.backend.Script` will do)
* the ability to automatically generate feedback from inside a script
* immunity to common pitfalls, such as infinite loops (supports timeouts) and program crashes / exceptions (handled by catching `InvocationTargetException`)
* the ability to generate rudimentary "style scores" and provide feedback for a student's indentation, commenting frequency, variable names, and presence of the Academic Honesty Policy comment (see `grader.scripts.ancillary.StyleAnalysis`)
* a command-line interface to select, sort, and view graded submissions
* the ability to review (see program output of) individual submissions (in case you're suspicious of a particular result)
* the ability to export submissions to a text file for copying and pasting into eLC
* clean compilation — AutoGrade compiles submissions entirely in memory, leaving behind no stray .java or .class files
* a C++ executable (lets you run `./autograde [...]` instead of `java -jar AutoGrade.jar [...]`)


### Compilation
To compile:

* download a ZIP of the repository using the "Clone or download" button above
* unzip the file if necessary
* open Terminal or Command Prompt
* within the extracted folder, navigate to the directory `src/main/resources` using `cd` or `dir`
* run `make`
* drag `AutoGrade.jar` and `autograde` (the executable) into whichever folder you'd like to use


### Usage 
AutoGrade uses a command-line interface. Usage is fairly straightforward:

```
$ ./autograde <script> [submission-folder] [flags...] 
```

The value of `script` must be either a precompiled .class file or a .java file, and the
class must implement the `grader.backend.Script` interface. (If you provide a .java file, 
AutoGrade will automatically build your script against AutoGrade.jar.)

If you want, you can specify the location of submissions by providing a value for
`submission-folder`. (If it isn't provided, the current directory is used.) Specifying
a submission folder is highly recommended, and AutoGrade will warn you if you try to
run it in a folder that doesn't contain exclusively eLC submissions. **Note**: although
it isn't reckless, AutoGrade is *automatic*, as its name implies. **USE IT IN A FOLDER 
WHICH CONTAINS NO IMPORTANT FILES. IT PROBABLY WON'T DO ANYTHING IT ISN'T SUPPOSED TO,
 BUT BETTER SAFE THAN SORRY.** 

Flags are available to modify AutoGrade's behavior. For example, to disable the
3-second timeout guarding against infinite loops, enter the `--timeout` flag with a
value of –1:

```
$ ./autograde StatScript.java --timeout -1
```

Flags can be used in any order. Run `./autograde` for a list of flags you can use.

(P.S.: if you don't want to use the executable file to run AutoGrade, you can just use 
`java -jar AutoGrade.jar <script> [submission-folder] [flags...]`. `autograde` just 
redirects your input to that command.)

Once AutoGrade finishes grading submissions, you will be greeted with AutoGrade's
internal command line. A list of commands will be shown to you. These commands are
quite extensive, but in a nutshell, you use commands as follows:

1. Use the `select` command to select students. It will then tell you which students
are currently selected.
2. Use the other commands to manipulate the results for the selected students.
3. Use the `help` command to learn more about the commands (e.g., `help select` to
learn more about selection filters).
4. Use the `export` command to save the results to a text file.
5. Use the `exit` command to quit.


### Technical details

AutoGrade is quite technically "intense," making use of a number of advanced Java
features including reflection, multi-threading, overridden `ClassLoader`s, and more.
The use of reflection is perhaps most important here, as it gives AutoGrade quite a lot
of power. Using reflection lets scripts be compiled purely against AutoGrade (since 
method signatures don't have to be validated). Rather than calling a specific class's
main method,

```java
NetPay.main(new String[] {});
// Code to intercept program output...
```

we load the class entirely abstractly, and so the class doesn't need to have a reference
to NetPay at all:

```java
File submission = student.getSubmission("NetPay.java");
Class<?> clazz = InternalCompiler.compile(submission);

// Code to intercept program output...
```

That makes compiling a script much simpler.

Not only that, but the methods provided by `ReflectionAssistant` provide a full range
of useful features, such as support for timeouts, exception catching, and, in some
instances, allowing us to get around spelling errors in method names (by finding a
method by its parameters rather than its name).

Grading multiple programs with all the same file names — in one instance of the JVM,
mind you — creates a few problems of its own. Java's default class loader doesn't load
a class with the same name twice. That's obviously very good for efficiency, but it
creates a problem when dealing with, say, 20 different versions of a class titled 
`StarGraph`. For this reason, AutoGrade uses its own class loader. 

For a while, this custom class loader, called `RefreshingClassLoader`, simply checked 
if the class was in the `temp` package (deferring to `super.loadClass(...)` if it 
wasn't); however, I ran into some issues with *deleting* .class files that AutoGrade
itself had created by calling `javac`. So, the next challenge was to figure out how
to compile and load classes *entirely in memory*. Java does make it possible, but it's
a bit complex, requiring me to create representations of .java 
(`grader.reflect.InternalSource`) and .class (`grader.reflect.InternalBytecode`) 
files in the process. A neat side effect of this is that AutoGrade can execute very 
cleanly — it no longer leaves behind an annoying, nearly-empty 'temp' directory. Now all
it creates is the files created by the `export` command.

The next issue I ran into was students' code inexplicably "crashing" AutoGrade with no exceptions or errors. I wasn't sure of the cause of this issue, so I didn't make any changes to AutoGrade for some time. However, I eventually discovered that the cause of the issue was students calling `System.exit(0)`, which forcibly closes the JVM (and AutoGrade by extension). I added a `SecurityManager` to AutoGrade specifically to block students' code from doing this.