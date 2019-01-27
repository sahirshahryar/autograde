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

import grader.backend.ManualGradingError;
import grader.frontend.Channel;
import grader.reflect.ReflectionAssistant;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import static grader.frontend.Color.*;

/**
 * This class can be used when grading assignments that run through a main() method.
 *
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   Saturday, July 14, 2018
 * @version 1.0.0
 */
public class ExecutionInspector {

    private boolean inspecting;

    private Class<?> program;

    private ByteArrayInputStream spy;

    private CaptivePrintStream capture;

    public ExecutionInspector(Class<?> main, List<String> inputs) {
        this.inspecting = Channel.STEPTHROUGH_INTERACTION.canSpeak();
        this.program = main;

        String input = "";

        for (String line : inputs) {
            input += line + '\n';
        }

        this.spy = new ByteArrayInputStream(input.getBytes());
        this.capture = new CaptivePrintStream(System.out);
    }

    public CaptivePrintStream getOutputCapture() {
        return this.capture;
    }

    public void addDebugHeader(String message) {
        if (this.capture != null) {
            this.capture.prefix(CYAN + message + RESET);
        }
    }

    public void markPrematureExit() {
        this.capture.exitedEarly();
    }

    public void run() throws InvocationTargetException, TimeoutException,
                             ManualGradingError {
        final InputStream oldSystemIn = System.in;
        System.setIn(this.spy);

        final PrintStream oldSystemOut = System.out;
        System.setOut(this.capture);

        String[] empty = new String[] {};
        try {
            ReflectionAssistant.testMethod(this.program, null, void.class,
                    "main", (Object) empty);
        } catch (final InvocationTargetException e) {
            if (ReflectionAssistant.getTrueException(e)
                    instanceof NoSuchElementException) {
                throw new ManualGradingError("Program requested more input than the " +
                        "grader can provide automatically");
            } else {
                throw e;
            }
        }

        System.setIn(oldSystemIn);
        System.setOut(oldSystemOut);

        this.capture.stopCollecting(false);

        if (this.inspecting) {
            Channel.INTERACTION.say(this.capture.peekFully());
        }
    }


}
