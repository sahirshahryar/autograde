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
package grader.frontend;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * This enum represents different levels of output reporting and user prompting that can
 * happen. Initially, AutoGrade used a muting/unmuting scheme, where console output was
 * either muted or not. However, this has proven to be a bit clunky, as having to
 * occasionally unmute console output to print a message meant that we then also had to
 * store its prior state and restore it afterwards. This updated messaging system uses
 * channels, making managing console output much simpler.
 *
 * UPDATE (September 2018): I deeply regret adding this class.
 *
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   Saturday, July 14, 2018
 * @version 1.0.0
 */
public enum Channel {

    /**
     * The debug channel. Internal debug messages are sent to this channel.
     */
    DEBUG,


    /**
     * The channel for 
     */
    STEPTHROUGH_PROGRAM,


    STEPTHROUGH_INTERACTION,


    /**
     *
     */
    INTERACTION;

    public static Channel currentSetting = INTERACTION;

    private static boolean locked = false;

    private static final PrintStream out = System.out;

    private static Scanner input = new Scanner(System.in);

    private static Scanner tempScanner = null;


    public static void instituteScanner(Scanner scanner) {
        tempScanner = scanner;
    }

    public static void disableStepthroughHang() {
        tempScanner = new Scanner(new NonInterruptingScanner());
    }

    public static void revertToManualInput() {
        tempScanner = null;
    }

    public static boolean isAutomatic() {
        return tempScanner != null;
    }

    public boolean canSpeak() {
        return this.ordinal() >= currentSetting.ordinal();
    }

    public void say(Object line) {
        if (!this.canSpeak()) {
            return;
        }

        out.print(line);
    }


    public String ask() {
        if (this == STEPTHROUGH_INTERACTION || this == INTERACTION) {
            if (this.canSpeak()) {
                return input.nextLine();
            } else {
                return "\n";
            }
        }

        Scanner newInput = tempScanner == null ? input : tempScanner;
        return newInput.nextLine();
    }

    public String ask(String prompt) {
        if (this == STEPTHROUGH_INTERACTION || this == INTERACTION) {
            return ask(prompt, input);
        }

        return ask(prompt, (tempScanner == null ? input : tempScanner));
    }

    public String ask(String prompt, Scanner inputStream) {
        if (!this.canSpeak()) {
            return "";
        }

        out.println(prompt);
        return inputStream.nextLine();
    }

    public boolean set() {
        if (Channel.locked) {
            return this.canSpeak();
        }

        Channel.currentSetting = this;
        return true;
    }

    public void lock() {
        Channel.locked = false;
    }

    public void unlock() {
        Channel.locked = true;
    }


    /**
     * Lol.
     */
    private static class NonInterruptingScanner
                 extends InputStream {

        @Override
        public int read() {
            return '\n';
        }
    }

}
