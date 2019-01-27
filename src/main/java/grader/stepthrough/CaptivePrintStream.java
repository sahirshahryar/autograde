/*
 * This file is part of AutoGrade, licensed under the MIT License (MIT).
 *
 * Copyright (c) Sahir Shahryar <https://github.com/sahirshahryar>
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
package grader.stepthrough;

import grader.frontend.Channel;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import static grader.frontend.Color.*;

/**
 * @author  Sahir Shahryar
 * @since   Sunday, July 15, 2018
 * @version 1.0.0
 */
public class CaptivePrintStream
     extends PrintStream {

    private boolean collect = true;

    private String prefix = "";

    private String storedContent = "";

    private boolean exitedEarly = false;

    public CaptivePrintStream(OutputStream stream) {
        super(stream, true);
    }

    public void prefix(String message) {
        this.prefix = message;
    }

    public void exitedEarly() {
        this.exitedEarly = true;
    }

    public void startCollecting() {
        collect = true;
        // storedContent = "";
    }

    public void stopCollecting(boolean dump) {
        if (dump) {
            dump();
        }

        collect = false;
    }

    public void dump() {
        Channel.INTERACTION.say(storedContent);
        storedContent = "";

        if (exitedEarly) {
            Channel.INTERACTION.say(RED + "Grading script ended before running " +
                    "all test cases" + RESET);
        }
    }


    protected String peekFully() {
        return prefix + "\n" + storedContent;
    }

    public String peek() {
        return this.storedContent;
    }


    /**
     *
     * @param s
     */
    @Override
    public void print(String s) {
        if (collect) {
            storedContent += s;
        } else {
            super.print(s);
        }
    }


    /**
     *
     * @param s
     */
    @Override
    public void println(String s) {
        print(s);
        println();
    }


    /**
     *
     */
    public void println() {
        print("\n");
    }


    /**
     * WHY
     */
    @Override
    public void print(int i) {
        print(String.valueOf(i));
    }


    /**
     * ARE
     */
    @Override
    public void println(int i) {
        print(String.valueOf(i));
    }


    /**
     * THERE
     */
    @Override
    public void print(long l) {
        print(String.valueOf(l));
    }


    /**
     * SO
     */
    @Override
    public void println(long l) {
        println(String.valueOf(l));
    }


    /**
     * MANY
     */
    @Override
    public void print(char c) {
        print(String.valueOf(c));
    }


    /**
     * OF
     */
    @Override
    public void println(char c) {
        println(String.valueOf(c));
    }


    /**
     * THESE
     */
    @Override
    public void print(double d) {
        print(String.valueOf(d));
    }


    /**
     * METHODS
     */
    @Override
    public void println(double d) {
        print(String.valueOf(d));
    }


    /**
     * NEVER
     */
    @Override
    public void print(float f) {
        print(String.valueOf(f));
    }


    /**
     * ENDING
     */
    @Override
    public void println(float f) {
        println(String.valueOf(f));
    }


    /**
     * FOREVER
     */
    @Override
    public void print(char[] s) {
        print(String.valueOf(s));
    }


    /**
     * UNRELENTING
     */
    @Override
    public void println(char[] s) {
        println(String.valueOf(s));
    }


    /**
     * PLEASE STOP
     */
    @Override
    public void print(boolean b) {
        print(String.valueOf(b));
    }


    /**
     * YOU GET A METHOD,
     */
    @Override
    public void println(boolean b) {
        println(String.valueOf(b));
    }


    /**
     * *YOU* GET A METHOD!
     */
    @Override
    public void print(Object obj) {
        print(String.valueOf(obj));
    }


    /**
     * EVERYBODY GETS A METHOD!
     */
    @Override
    public void println(Object obj) {
        println(String.valueOf(obj));
    }


    /**
     * Okay, that's it, I'm done ranting.
     */
    @Override
    public PrintStream printf(String format, Object... args) {
        print(String.format(format, args));
        return this;
    }


    /**
     * See you later.
     */
    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        print(String.format(l, format, args));
        return this;
    }
    
}
