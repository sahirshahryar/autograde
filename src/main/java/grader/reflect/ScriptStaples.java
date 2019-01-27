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
package grader.reflect;

import grader.backend.Feedback;
import grader.util.Helper;
import grader.backend.ManualGradingError;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;

/**
 * Some common things (staples) that may need to be done when running a grading script.
 *
 * @author  Sahir Shahryar
 * @since   Sunday, May 6, 2018
 * @version 1.0.0
 */
public class ScriptStaples {

    /**
     *
     * @param feedback
     * @param penalty
     * @param constructorDescription
     * @param clazz
     * @param params
     * @param <C>
     * @return
     * @throws ManualGradingError
     */
    public static <C> C safeConstruct(Feedback feedback, float penalty,
                                      String constructorDescription, Class<C> clazz,
                                      Object... params)
            throws ManualGradingError {
        try {
            return ReflectionAssistant.constructObject(clazz, params);
        }

        catch (final InvocationTargetException e) {
            feedback.deductPoints(penalty, "your program throws a(n) " +
                ReflectionAssistant.getTrueException(e) + " when using the "
                + constructorDescription + ".");
        }

        catch (final TimeoutException e) {
            feedback.deductPoints(penalty, "your program loops infinitely when using " +
                    "the " + constructorDescription + ".");
        }

        catch (final IllegalArgumentException e) {
            throw new ManualGradingError("Error while calling ScriptStaples#" +
                    "safeConstruct(): " + e.getMessage());
        }

        return null;
    }


    /**
     *
     * @param feedback
     * @param penalty
     * @param expectedReturnValue
     * @param clazz
     * @param objectInstance
     * @param method
     * @param params
     * @param <C>
     * @param <R>
     * @return
     * @throws ManualGradingError
     */
    @SuppressWarnings("unchecked")
    public static <C, R> void compareValues(Feedback feedback, float penalty,
                                            String feedbackMessage,
                                            R expectedReturnValue,
                                            Class<C> clazz, Object objectInstance,
                                            String method, Object... params)
            throws ManualGradingError {
        try {
            R actualReturnValue = ReflectionAssistant.testMethod(
                    clazz, objectInstance, (Class<R>) expectedReturnValue.getClass(),
                    method, params);

            if (expectedReturnValue instanceof Double) {
                if (!Helper.roughlyEqual((Double) expectedReturnValue,
                        (Double) actualReturnValue)) {
                    feedback.deductPoints(penalty, feedbackMessage);
                }

                return;
            }

            else if (expectedReturnValue.getClass().isArray()) {
                R[] expectedArray = (R[]) expectedReturnValue,
                    actualArray = (R[]) actualReturnValue;

                if (expectedArray.length != actualArray.length) {
                    feedbackMessage = feedbackMessage.replace("%ERROR%",
                            "expected length = " + expectedArray.length + ", " +
                            "actual length = " + actualArray.length);

                    feedback.deductPoints(penalty, feedbackMessage);
                    return;
                }

                for (int i = 0; i < expectedArray.length; ++i) {
                    if (!expectedArray[i].equals(actualArray[i])) {
                        feedbackMessage = feedbackMessage.replace("%ERROR%",
                                "expected answer = " +
                                Helper.arrayToString(expectedArray) +
                                ", actual answer = " +
                                Helper.arrayToString(actualArray));

                        feedback.deductPoints(penalty, feedbackMessage);
                        return;
                    }
                }
            }

            else if (Collection.class.isAssignableFrom(expectedReturnValue.getClass())) {
                Collection<?> expectedCollection = (Collection<?>) expectedReturnValue,
                              actualCollection   = (Collection<?>) actualReturnValue;

                if (expectedCollection.size() != actualCollection.size()) {
                    feedbackMessage = feedbackMessage.replace("%ERROR%",
                            "expected length = " + expectedCollection.size() + ", " +
                            "actual length = " + actualCollection.size());

                    feedback.deductPoints(penalty, feedbackMessage);
                    return;
                }

                Iterator<?> expectedIterator = expectedCollection.iterator(),
                            actualIterator   = actualCollection.iterator();

                int i = 0;
                while (expectedIterator.hasNext()) {
                    Object expectedNext = expectedIterator.next(),
                           actualNext   = actualIterator.next();

                    if (!expectedNext.equals(actualNext)) {
                        boolean needsQuotes = !expectedNext.getClass().isPrimitive();

                        String expectedText = expectedNext.toString(),
                               actualText   = actualNext.toString();

                        if (needsQuotes) {
                            expectedText = "\"" + expectedText + "\"";
                            actualText   = "\"" + actualText + "\"";
                        }

                        feedbackMessage = feedbackMessage.replace("%ERROR%",
                                "at index " + i + ", expected = " + expectedText +
                                ", actual = " + actualText);

                        feedback.deductPoints(penalty, feedbackMessage);
                    }

                    i++;
                }
            }

            else {

            }
        }

        catch (final InvocationTargetException e) {
            feedback.deductPoints(penalty, "your program throws a(n) " +
                ReflectionAssistant.getTrueException(e) + " when running the " +
                method + "() method.");
        }

        catch (final TimeoutException e) {
            feedback.deductPoints(penalty, "your program loops infinitely when " +
                    "running the " + method + "() method.");
        }

        catch (final IllegalArgumentException | NullPointerException
                | ClassCastException e) {
            throw new ManualGradingError("Error while calling ScriptStaples#" +
                    "valueIncorrect(): " + e.getMessage());
        }

        // return new Tuple<>(true, null);
    }



}
