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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Provides a useful interface for working with eLC's special way of naming files when
 * they're downloaded wholesale.
 *
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   Tuesday, September 4, 2018
 * @version 1.0.0
 */
public class ELCSubmission {

    private final File file;

    private final String identifier;

    private final String studentName;

    private final String date;

    private final String filename;


    public static final String ELC_DATE_FORMAT = "MMM DD, YYYY hhmm a";


    public ELCSubmission(File f) {
        this.file = f;

        String filename = f.getName();
        String[] split = filename.split(" - ");

        if (split.length != 4) {
            throw new IllegalArgumentException("Specified a file which does not match " +
                    "the eLC naming conventions");
        }

        this.identifier = split[0];
        this.studentName = split[1];
        this.date = split[2];
        this.filename = split[3];
    }


    public String getIdentifier() {
        return this.identifier;
    }


    public String getStudentName() {
        return this.studentName;
    }


    public String getDate() {
        return this.date;
    }


    public File getFile() {
        return this.file;
    }


    public Date getParsedDate() {
        SimpleDateFormat format = new SimpleDateFormat(ELCSubmission.ELC_DATE_FORMAT);
        try {
            return format.parse(this.date);
        } catch (final ParseException e) {
            throw new RuntimeException("Unable to parse date for " + studentName + "'s " +
                    "submission");
        }
    }


    public String getFileName() {
        return this.filename;
    }


    public boolean newerThan(ELCSubmission other) {
        return this.getParsedDate().after(other.getParsedDate());
    }

}
