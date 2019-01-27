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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   Monday, January 15, 2018
 * @version 1.0.0
 */
public class Feedback {

    private double grade = 100.0;

    private HashMap<String, ArrayList<String>> notes;

    private String currentSource = null;

    public Feedback(String source) {
        this.notes = new HashMap<>();
        this.setSource(source);
    }

    public void setSource(String scriptName) {
        if (scriptName == null) {
            throw new IllegalArgumentException("Feedback cannot come from a source " +
                    "that has no name!");
        }

        this.currentSource = scriptName;

        if (!this.notes.containsKey(scriptName)) {
            this.notes.put(scriptName, new ArrayList<>());
        }
    }

    public String getSource() {
        return this.currentSource;
    }

    public ArrayList<String> getAllSources() {
        return new ArrayList<>(notes.keySet());
    }

    public void bonusPoints(double points) {
        this.grade += points;
    }

    public void deductPoints(double points) {
        this.grade -= points;
    }

    public void deductPoints(double points, String note) {
        this.grade -= points;

        /**
         * Looks can be deceiving: that hyphen is actually an en-dash.
         */
        char prefix = points < 0 ? '+' : '–';
        String formattedPoints;

        if (points == (int) points) {
            formattedPoints = "" + (int) Math.abs(points);
        } else {
            formattedPoints = "" + Math.abs(points);
        }

        if (formattedPoints.contains(".")) {
            int cutoff = Math.min(formattedPoints.lastIndexOf('.'),
                                  formattedPoints.length());

            formattedPoints = formattedPoints.substring(0, cutoff);
        }

        this.accessCurrentNotes().add(prefix + formattedPoints + ": " + note);
    }

    public void assignGrade(double grade) {
        this.grade = grade;
    }

    public double getGrade() {
        return grade;
    }

    public void addNote(String note) {
        this.accessCurrentNotes().add(note);
    }

    private ArrayList<String> accessCurrentNotes() {
        if (this.currentSource == null) {
            throw new IllegalArgumentException("Feedback cannot come from a source" +
                    "that has no name!");
        }

        return this.notes.get(this.currentSource);
    }

    public ArrayList<String> getNotes(String source) {
        return this.notes.get(source);
    }

}
