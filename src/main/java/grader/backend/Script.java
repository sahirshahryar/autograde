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

import java.io.File;

/**
 * Represents a grading script that can be used for AutoGrade.
 *
 * @author  Sahir Shahryar
 * @since   Monday, January 15, 2018
 * @version 1.0.0
 */
public interface Script {

    /**
     * A list of {@link AncillaryScript}s that should be run alongside this script.
     * Right now, the only usable ancillary script is
     * {@link grader.scripts.ancillary.StyleAnalysis}, but more may be added in the
     * future.
     *
     * @return (String[]) an array of class names.
     */
    default String[] listAncillaryScripts() {
        return new String[] {};
    }


    /**
     * Returns whether or not the given script can be automatically inspected (i.e.,
     * it uses ExecutionInspector). If the grading script prints its own output (as
     * may be the case with StatScript.java, among others), then this should return
     * false so that the 'inspect' command can make the necessary preparations before
     * rerunning the grading script. Generally, scripts 
     *
     * @return (boolean) true by default, but can be overridden in the implementation.
     */
    default boolean inspectsAutomatically() {
        return true;
    }


    /**
     * This method allows the script to specify whether or not a file belongs to a
     * student's submission (or is an auxiliary file used by the script). AutoGrade
     * will ask the script this question for each file in the directory it is working
     * in. If this method returns false for any file in the directory (other than
     * index.html), AutoGrade will issue a warning to the user.
     *
     * @param file (File) the file being checked.
     * @return (boolean) true if the given file is associated with grading in any way;
     *         false otherwise.
     */
    boolean fileBelongs(File file);


    /**
     * This method is a slightly more specific version of {@link #fileBelongs(File)}. It
     * checks if the given file belongs to a student's submission. If this method returns
     * false, the given file will not be associated with a student.
     *
     * @param file (File) the file being checked.
     * @return (boolean) true if the given file is part of a student's submission;
     *         false otherwise.
     */
    boolean fileBelongsToStudent(File file);


    /**
     * Grades the given student's assignment, generating a Feedback object in the
     * process.
     *
     * @param student (Student) the student whose submission is bein ggraded.
     * @return (Feedback) a Feedback object containing the student's suggested score and
     *         any notes that may have occurred.
     *
     * @throws ManualGradingError thrown if an error occurs that requires the grader to
     *         score the submission manually.
     */
    Feedback gradeSubmission(Student student) throws ManualGradingError;

}
