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
package grader.backend;

import grader.AutoGrade;

import java.io.File;
import java.util.ArrayList;

/**
 * Represents a student whose submission is being graded.
 *
 * @author  Sahir Shahryar
 * @since   Tuesday, April 24, 2018
 * @version 1.0.0
 */
public class Student implements Comparable<Student> {

    /**
     * The student's name.
     */
    private String name;

    /**
     * The list of files associated with this student.
     */
    private ArrayList<ELCSubmission> entries;


    /**
     * The Feedback (score + notes) associated with this student's submission.
     */
    private Feedback score;


    /**
     * If the script throws a ManualGradingError when grading this student's submission,
     * it will be stored in this variable for further analysis.
     */
    private Throwable error;


    /**
     * Determines whether the student's submission files will be saved on exit. This
     * value is automatically set to true if the student's submission fails to compile
     * or runs into a ManualGradingError, and can be set by the user by using the 'save'
     * command.
     */
    private boolean save;


    /**
     * Keeps track of whether the 'save' field was manually or automatically set. This
     * helps us respect the user's decision in case there is a scenario where the program
     * tries to automatically set 'save' after it has already been decided by the user.
     */
    private boolean saveManuallySet;


    /**
     * Initializes a new Student with the given file as their first linked file. The
     * student's name is determined automatically if possible.
     *
     * @param entry (File) a file that belongs to this student.
     */
    public Student(ELCSubmission entry) {
        this.name = entry.getStudentName();
        

        this.entries = new ArrayList<>();
        this.entries.add(entry);

        this.score = null;
        this.error = null;

        this.save = false;
        this.saveManuallySet = false;
    }


    /**
     * Returns the student's name, unless {@link AutoGrade#CENSOR} is enabled; in which
     * case, a string of hashes will be returned.
     *
     * @return (String) the student's name, or hashes if censorship is enabled.
     */
    public String getName() {
        if (!AutoGrade.CENSOR) {
            return this.name;
        }

        StringBuilder substitute = new StringBuilder();
        for (int i = 0; i < this.name.length(); ++i) {
            substitute.append(this.name.charAt(i) == ' ' ? ' ' : '#');
        }

        return substitute.toString();
    }


    /**
     * Returns the student's name regardless of whether it has been censored for
     * demonstration purposes.
     *
     * @return (String) the actual value of {@link #name}.
     */
    public String getTrueName() {
        return this.name;
    }


    /**
     * Attempts to extract the last name of a student from their name. This may not
     * always work, especially if the student has a last name like "de la Cruz", but
     * it's fine for 99% of names.
     *
     * @return (String) the last word in the student's name.
     */
    public String getLastName() {
        String[] split = this.name.split(" ");
        return split[split.length - 1];
    }


    /**
     * Sets the student's name, if we ever need to.
     *
     * @param name (String) the student's new name.
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Fetches a file that is associated with this student.
     *
     * @param name (String) the name of the file. Only relevant if the student has
     *             multiple files to their name; if there's only one file, the only
     *             file they have is returned.
     *
     * @return (File) the file associated with this student that contains `name` in
     *         its name.
     *
     * @throws ManualGradingError thrown if the student did not have a submission whose
     *         name contained `name`.
     */
    public File getSubmission(String name) throws ManualGradingError {
        if (entries.size() == 1) {
            return this.entries.get(0).getFile();
        }

        for (ELCSubmission submission : this.entries) {
            if (submission.getFileName().equalsIgnoreCase(name)) {
                return submission.getFile();
            }
        }

        throw new ManualGradingError("Student " + this.getName()
                + " did not have a file whose name contained '" + name + "'");
    }


    public ArrayList<ELCSubmission> getSubmissions() {
        return this.entries;
    }


    /**
     * Adds a file to the student's "portfolio."
     *
     * @param submission (ELCSubmission) the file to add.
     */
    public void addFile(ELCSubmission submission) {
        this.entries.add(submission);
    }


    /**
     * Returns the Feedback object associated with this student. This may be null if
     * there was an error grading the student's submission.
     *
     * @return (Feedback) the student's grade.
     */
    public Feedback getFeedback() {
        return this.score;
    }


    /**
     * Sets the student's score to the Feedback object given. Typically, this Feedback
     * object is generated by the script being run.
     *
     * @param score (Feedback) the Feedback object to set the student's score to.
     */
    public void setScore(Feedback score) {
        this.score = score;
    }


    /**
     * If there was an error grading the student's submission, this method can be
     * called to set the exception they had for future reference.
     *
     * @param e (ManualGradingError) the error that occurred.
     */
    public void appendException(Throwable e) {
        this.error = e;

        if (!this.saveManuallySet) {
            this.save = (e != null);
        }
    }


    /**
     * Ensures that this student's submissions will NOT be deleted on exit.
     */
    public void saveSubmissions() {
        this.saveManuallySet = true;
        this.save = true;
    }


    /**
     * Ensures that this student's submissions WILL be deleted on exit.
     */
    public void unsaveSubmissions() {
        this.saveManuallySet = true;
        this.save = false;
    }


    public ArrayList<String> deleteSubmissions() {
        if (this.save) {
            return new ArrayList<>();
        }

        ArrayList<String> failedDeletions = new ArrayList<>();

        for (ELCSubmission submission : this.entries) {
            if (!submission.getFile().delete()) {
                failedDeletions.add(submission.getFile().getName());
            }
        }

        return failedDeletions;
    }


    public int cleanUpDuplicates() {
        ArrayList<ELCSubmission> submissionsToRemove = new ArrayList<>();
        for (ELCSubmission i : this.entries) {
            for (ELCSubmission j : this.entries) {
                if (i == j) {
                    continue;
                }

                if (i.getFileName().equalsIgnoreCase(j.getFileName())) {
                    submissionsToRemove.add(i.newerThan(j) ? j : i);
                }
            }
        }
        
        for (ELCSubmission submission : submissionsToRemove) {
            this.entries.remove(submission);
        }

        return submissionsToRemove.size();
    }


    /**
     * Determines if the student's submission was able to be automatically graded.
     *
     * @return (boolean) true if there was no ManualGradingError and if the student
     *         has a score assigned; false otherwise.
     */
    public boolean wasErrorFree() {
        return this.error == null && this.score != null;
    }




    /**
     * Determines if another Object is equivalent ot this one.
     *
     * @param obj (Object) the object to compare to this one.
     *
     * @return (boolean) true if `obj` is a String that is equivalent (case-insensitive)
     *         to this Student's name, or if `obj` is a Student whose name is equivalent
     *         (case-insensitive) to this Student's name; false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return this.name.equalsIgnoreCase(obj.toString());
        } else if (obj instanceof Student) {
            return this.name.equalsIgnoreCase(((Student) obj).getName());
        }

        return false;
    }


    @Override
    public int compareTo(Student other) {
        return this.getLastName().compareTo(other.getLastName());
    }

    /**
     * Determines the name of a student by scraping it out of eLC's auto-generated
     * download names. AutoGrade is optimized to grade eLC batch downloads.
     *
     * @param entry (File) the file being checked.
     *
     * @return (String) the student's name if the file is formatted in eLC's naming
     *         format; "unable to determine student name (<filename>)" otherwise.
     */
    public static String determineStudentName(File entry) {
        if (entry == null) {
            return "unable to determine student name (entry was null)";
        }

        String filename = entry.getName();

        /**
         * eLC does make some things pretty convenient.
         */
        if (!filename.matches("\\d+-\\d+ - .+ - .+ - .+")) {
            return "unable to determine student name (" + filename + ")";
        }

        return filename.split(" - ")[1];
    }

}
