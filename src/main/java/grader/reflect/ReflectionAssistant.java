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
package grader.reflect;

import grader.AutoGrade;
import grader.backend.ManualGradingError;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Since reflection is generally messy, this class makes it a little easier to work
 * with.
 *
 * @author  Sahir Shahryar
 * @since   Tuesday, April 24, 2018
 * @version 1.0.0
 */
public final class ReflectionAssistant {


    /**
     * macOS seems to behave strangely, so let's get this going.
     */
    public static final char PATH_SEPARATOR
            = File.pathSeparatorChar == ':' ? '/'
                                            : File.pathSeparatorChar;

    /**
     * Compiles a class with no special javac arguments, then loads it.
     *
     * @param file (File) the file to compile. Should be a .java file.
     *
     * @see ReflectionAssistant#compileClass(File) aliased method
     *
     * @return (Class<?>) a class of unknown type.
     *
     * @throws ManualGradingError thrown if the class cannot be compiled or loaded.
     */
    public static Class<?> compileClass(File file) throws ManualGradingError {
        return compileClass(file, null, true);
    }


    /**
     * Compiles a class with the given javac arguments (and option of editing the package
     * to 'temp'), then loads it. NOTE: due to the way that InternalClassLoader works,
     * all packages must be
     *
     * @param file (File) the file to compile. Should be a .java file.
     * @param javac
     * @param editPackage
     * @return
     * @throws ManualGradingError
     */
    protected static Class<?> compileClass(File file, String javac, boolean editPackage)
            throws ManualGradingError {
        /**
         *
         */
        if (!file.getName().endsWith(".java")) {
            throw new RuntimeException("Attempted to compile a class that is not " +
                    "suffixed with .java");
        }


        /**
         *
         */
        String className = determineCorrectClassName(file);
        File runningFile;
        String runningFilePath = null;
        // TODO: Rewrite using SourceUtilities#getLines() method.
        if (editPackage) {
            try {
                BufferedReader stream = new BufferedReader(new FileReader(file));

                String fileContents = "";
                String line;
                boolean inComment = false, packageDeclarationFound = false;


                /**
                 * I
                 */
                while (true) {
                    line = stream.readLine();

                    if (line == null) {
                        break;
                    }

                    if (line.contains("/*") && !line.trim().startsWith("//")) {
                        inComment = true;
                    }

                    String oldLine = line;
                    if (line.contains("*/") && !line.trim().startsWith("//")) {
                        inComment = false;
                        line = line.substring(line.indexOf("*/"));
                    }


                    /**
                     * This is a horrible, terrible, very bad, no-good hack.
                     * Hopefully no one tries to be too clever.
                     */
                    if (line.trim().startsWith("package") && !inComment) {
                        fileContents += "package temp;\n";
                        packageDeclarationFound = true;
                    } else {
                        fileContents += oldLine + "\n";
                    }
                }


                /**
                 * If we never found a package declaration, try to stick it at the top
                 * of the file. This shouldn't really cause any problems; after all,
                 * javac will complain quite loudly if we did something wrong.
                 */
                // TODO: Use AutoGrade.TEMP_PACKAGE value instead of "temp" hardcoded
                if (!packageDeclarationFound) {
                    fileContents = "package temp;\n" + fileContents;
                }

                File tempDirectory = new File("temp");

                if (tempDirectory.exists()) {
                    if (!AutoGrade.DIRECTORY_WARNING_ACKNOWLEDGED
                            && !AutoGrade.TEMP_DIRECTORY_CREATED_BY_US) {
                        AutoGrade.showDirectoryWarning();
                    }
                } else {
                    AutoGrade.TEMP_DIRECTORY_CREATED_BY_US = true;
                    if (!tempDirectory.mkdirs()) {
                        throw new RuntimeException("Failed to create temp directory " +
                                "for javac. Rerun with command --no-package-edits to try " +
                                "again");
                    }
                }

                className = determineCorrectClassName(file);
                runningFile = new File("temp" + PATH_SEPARATOR + className + ".java");


                /**
                 * First ensure that there isn't an existing file in the file space we're
                 * trying to use. If there is, check if it was created by AutoGrade
                 * itself; if it wasn't (or if it was created by another instance of
                 * AutoGrade), give a prompt to delete it.
                 */
                if (runningFile.exists()) {
                    if (AutoGrade.isByproductFile(runningFile)) {
                        if (!runningFile.delete()) {
                            throw new RuntimeException("Unable to delete a file that " +
                                    "was created by this program. That's awkward!");
                        }
                    }

                    else {
                        AutoGrade.promptToDelete(runningFile);
                    }
                } else {
                    if (!runningFile.createNewFile()) {
                        throw new RuntimeException("Failed to create new file " +
                              runningFile.getName());
                    }
                }

                /**
                 * Finally, write the new file to the given filename.
                 */
                FileOutputStream output = new FileOutputStream(runningFile);
                output.write(fileContents.getBytes());
                output.flush();
                AutoGrade.addByproductFile(runningFile);
                runningFilePath = "temp" + PATH_SEPARATOR + className + ".java";
            }

            catch (final IOException ex) {
                // Should never occur.
            }
        }


        /**
         * We elected not to edit the package declaration in the file. In that case just
         * make sure that there isn't a preexisting file with the name we want, ask the
         * user for permission to delete it if necessary, and copy over the original file
         * to the given destination.
         */
        else {
            runningFile = new File(className + ".java");

            if (!file.equals(runningFile)) {
                if (runningFile.exists()) {
                    if (AutoGrade.isByproductFile(runningFile)) {
                        if (!runningFile.delete()) {
                            throw new RuntimeException("Unable to delete a file " +
                                    "that was created by this program. That's awkward!");
                        }
                    } else {
                        AutoGrade.promptToDelete(runningFile);
                    }
                }

                try {
                    Files.copy(file.toPath(), runningFile.toPath());
                    AutoGrade.addByproductFile(runningFile);
                } catch (final IOException ex) {
                    throw new RuntimeException("Unable to copy " + file.getName() +
                            " to " + runningFile.getName() + "; please rerun after " +
                            "fixing file permissions");
                }
            }

            runningFilePath = className + ".java";
        }

        if (runningFilePath == null) {
            throw new RuntimeException("Internal error: could not prepare the file " +
                    "to be compiled");
        }

        if (javac == null) {
            javac = "";
        }

        // ConsoleOutput.unmute();
        if (AutoGrade.executeCommand("javac " + javac + " " + runningFilePath) == 0) {
            AutoGrade.addByproductFile(new File(className + ".class"));
        } else {
            throw new ManualGradingError("javac failed on " + className + ".java;" +
                    "please rerun after fixing this issue");
        }

        String address = "";
        try {
            InternalClassLoader loader = new InternalClassLoader();

            address = runningFilePath.substring(0, runningFilePath.length() - 5)
                                     .replace(PATH_SEPARATOR, '.');

            return loader.loadClass(address);
        }

        catch (final ClassNotFoundException ex) {
            throw new ManualGradingError("Could not load class " + address);
        }
    }


    /**
     *
     * @param clazz
     * @param args
     * @param <C>
     * @return
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static <C> C constructObjectNoTimeout(Class<C> clazz, Object... args)
        throws IllegalArgumentException, InvocationTargetException {
        try {
            return constructObject(-1, clazz, args);
        }

        catch (final TimeoutException e) {
            return null;
        }
    }


    /**
     *
     * @param clazz
     * @param args
     * @param <C>
     * @return
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws TimeoutException
     */
    public static <C> C constructObject(Class<C> clazz, Object... args)
        throws IllegalArgumentException, InvocationTargetException, TimeoutException {
        return constructObject(AutoGrade.TIMEOUT_SECONDS, clazz, args);
    }


    /**
     *
     * @param timeout
     * @param clazz
     * @param args
     * @param <C>
     * @return
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws TimeoutException
     */
    private static <C> C constructObject(int timeout, Class<C> clazz, Object... args)
            throws IllegalArgumentException, InvocationTargetException, TimeoutException {

        Constructor<C> constructorRef = null;
        try {
            Class[] paramTypes = new Class[args.length];

            for (int i = 0; i < args.length; ++i) {
                paramTypes[i] = args[i].getClass();
            }

            final Constructor<C> constructor = clazz.getConstructor(paramTypes);
            constructorRef = constructor;

            if (!determineAccess(constructor.getModifiers()).equals("public")) {
                constructor.setAccessible(true);
            }

            if (timeout == -1) {
                @SuppressWarnings("unchecked")
                C object = constructor.newInstance(args);

                return object;
            } else {
                return executeReturningThread(new ReturningThread<C>() {
                    @SuppressWarnings("unchecked")
                    public void runInternal() throws Throwable {
                        value = constructor.newInstance(args);
                    }
                }, timeout);
            }
        }

        catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException("The constructor " +
                    generateMethodSignature(clazz, "<init>", false, args)
                    + " does not exist", e);
        }

        catch (final SecurityException | IllegalAccessException e) {
            throw new IllegalArgumentException("The method " +
                    generateMethodSignature(clazz, "<init>", false, args)
                    + "is " + determineAccess(constructorRef.getModifiers()), e);
        }

        catch (final InstantiationException e) {
            throw new IllegalArgumentException("The type " + clazz.getSimpleName() +
                    " cannot be instantiated", e);
        }

        catch (final InterruptedException e) {
            throw new TimeoutException("The construction of " + clazz.getSimpleName() +
                    " was interrupted internally");
        }

    }


    /**
     *
     * @param clazz
     * @param object
     * @param returnType
     * @param name
     * @param args
     * @param <C>
     * @param <R>
     * @return
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static <C, R> R testMethodNoTimeout(Class<C> clazz, Object object,
                                      Class<R> returnType, String name, Object... args)
            throws IllegalArgumentException, InvocationTargetException {
        try {
            return testMethod(-1, clazz, object, returnType, name, args);
        }

        catch (final TimeoutException e) {
            return null;
        }
    }


    /**
     *
     * @param clazz
     * @param object
     * @param returnType
     * @param name
     * @param args
     * @param <C>
     * @param <R>
     * @return
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws TimeoutException
     */
    public static <C, R> R testMethod(Class<C> clazz, Object object,
                                      Class<R> returnType, String name, Object... args)
            throws IllegalArgumentException, InvocationTargetException, TimeoutException {
        return testMethod(AutoGrade.TIMEOUT_SECONDS, clazz, object, returnType,
                          name, args);
    }


    /**
     *
     * @param timeout
     * @param clazz
     * @param object
     * @param returnType
     * @param name
     * @param args
     * @param <C>
     * @param <R>
     * @return
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws TimeoutException
     */
    private static <C, R> R testMethod(int timeout, Class<C> clazz, Object object,
                                      Class<R> returnType, String name, Object... args)
            throws IllegalArgumentException, InvocationTargetException, TimeoutException {

        Method methodRef = null;
        boolean isStatic = false;
        try {
            Class[] paramTypes = new Class[args.length];

            for (int i = 0; i < args.length; ++i) {
                paramTypes[i] = args[i].getClass();
            }

            final Method method = clazz.getMethod(name, paramTypes);
            methodRef = method;

            isStatic = Modifier.isStatic(method.getModifiers());

            if (!returnType.isAssignableFrom(method.getReturnType())) {
                throw new IllegalArgumentException("The method " +
                        generateMethodSignature(clazz, name, isStatic, args)
                        + " exists, but returns " + method.getReturnType().getSimpleName()
                        + " instead of " + returnType.getSimpleName());
            }

            if (!determineAccess(method.getModifiers()).equals("public")) {
                method.setAccessible(true);
            }

            if (timeout == -1) {
                @SuppressWarnings("unchecked")
                R returnValue = (R) method.invoke(object, args);

                return returnValue;
            } else {
                /**
                 * Unfortunately no lambda expression allowed here :(
                 */
                return executeReturningThread(new ReturningThread<R>() {
                    @SuppressWarnings("unchecked")
                    public void runInternal() throws Throwable {
                        value = (R) method.invoke(object, args);
                    }
                }, timeout);
            }
        }

        catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException("The method " +
                    generateMethodSignature(clazz, name, false, args)
                    + " does not exist");
        }

        catch (final SecurityException | IllegalAccessException e) {
            throw new IllegalArgumentException("The method " +
                    generateMethodSignature(clazz, name, isStatic, args)
                    + " is " + determineAccess(methodRef.getModifiers()));
        }

        catch (final InterruptedException e) {
            throw new TimeoutException("Execution of the method " +
                    generateMethodSignature(clazz, name, isStatic, args)
                    + " was interrupted internally");
        }
    }


    /**
     *
     * @param clazz
     * @param object
     * @param type
     * @param name
     * @param <C>
     * @param <T>
     * @return
     * @throws IllegalArgumentException
     */
    public static <C, T> T accessField(Class<C> clazz, Object object, Class<T> type,
                                       String name)
            throws IllegalArgumentException {
        Field field = null;
        boolean isStatic = false;
        try {
            field = clazz.getField(name);
            isStatic = Modifier.isStatic(field.getModifiers());

            if (!type.isAssignableFrom(field.getType())) {
                throw new IllegalArgumentException("The field " +
                        generateFieldName(clazz, isStatic, name) + "exists, but its " +
                        "type is " + field.getType().getSimpleName() + ", not " +
                        type.getSimpleName());
            }

            if (!determineAccess(field.getModifiers()).equals("public")) {
                field.setAccessible(true);
            }

            @SuppressWarnings("unchecked")
            T returnValue = (T) field.get(object);

            return returnValue;
        }

        catch (final NoSuchFieldException ex) {
            throw new IllegalArgumentException("The field " +
                    generateFieldName(clazz, false, name) + " does not exist");
        }

        catch (final SecurityException | IllegalAccessException ex) {
            throw new IllegalArgumentException("The field " +
                    generateFieldName(clazz, isStatic, name)
                    + "is " + determineAccess(field.getModifiers()));
        }
    }


    /**
     *
     * @param clazz
     * @param object
     * @param type
     * @param <C>
     * @param <T>
     * @return
     * @throws IllegalArgumentException
     */
    public static <C, T> T accessFieldByType(Class<C> clazz, Object object, Class<T> type)
            throws IllegalArgumentException {

        for (Field field : clazz.getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType())) {
                return accessField(clazz, object, type, field.getName());
            }
        }

        throw new IllegalArgumentException("No fields of type " + type.getSimpleName()
                                           + " were found");
    }


    public static <C, T> void setField(Class<C> clazz, Object instance,
                                       Class<T> fieldType, T newValue)
            throws IllegalAccessException, IllegalArgumentException {
        for (Field field : clazz.getDeclaredFields()) {
            if (fieldType.isAssignableFrom(field.getType())) {
                /**
                 * HACKERMAN
                 */
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                field.set(instance, newValue);
            }
        }

        throw new IllegalArgumentException("No fields of type " + fieldType.getSimpleName()
                + " were found");
    }


    /**
     *
     * @param runnable
     * @param timeout
     * @param <R>
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws InvocationTargetException
     */
    private static <R> R executeReturningThread(ReturningThread<R> runnable, int timeout)
            throws InterruptedException, TimeoutException, InvocationTargetException {
        Thread thread = new Thread(runnable);
        thread.start();
        thread.join(timeout * 1000L);

        if (thread.isAlive()) {
            thread.interrupt();
            throw new TimeoutException();
        }

        if (runnable.hadException()) {
            throw new InvocationTargetException(runnable.getException().getCause());
        }

        return runnable.getValue();
    }


    /**
     *
     * @param <R>
     */
    private static abstract class ReturningThread<R> implements Runnable {

        /**
         *
         */
        R value = null;

        /**
         *
         */
        Throwable exception = null;


        /**
         *
         */
        @Override
        public void run() {
            try {
                runInternal();
            } catch (final Throwable t) {
                exception = t;
            }
        }


        /**
         *
         * @throws Throwable
         */
        abstract void runInternal() throws Throwable;


        /**
         *
         * @return
         */
        public R getValue() {
            return value;
        }


        /**
         *
         * @return
         */
        public boolean hadException() {
            return this.exception != null;
        }


        /**
         *
         * @return
         */
        public Throwable getException() {
            return this.exception;
        }

    }


    /**
     *
     * @param modifiers
     * @return
     */
    private static String determineAccess(int modifiers) {
        return Modifier.isPublic(modifiers) ? "public"
             : Modifier.isPrivate(modifiers) ? "private"
             : Modifier.isProtected(modifiers) ? "protected"
             : "package-protected";
    }


    /**
     *
     * @param clazz
     * @param name
     * @param isStatic
     * @param args
     * @param <C>
     * @return
     */
    private static <C> String generateMethodSignature(Class<C> clazz, String name,
                                                      boolean isStatic, Object... args) {
        String params = "";
        for (Object o : args) {
            params += o.getClass().getSimpleName() + ", ";
        }

        params = (params.length() < 2) ? params
                                       : params.substring(0, params.length() - 2);

        return clazz.getSimpleName()
             + (isStatic ? "." : "#")
             + name + "(" + params + ")";
    }


    /**
     *
     * @param clazz
     * @param isStatic
     * @param name
     * @param <C>
     * @return
     */
    private static <C> String generateFieldName(Class<C> clazz, boolean isStatic,
                                                String name) {
        return clazz.getSimpleName()
             + (isStatic ? "." : "#")
             + name;
    }


    /**
     * 
     * @param e
     * @return
     */
    public static Throwable getTrueException(InvocationTargetException e) {
        Throwable result = e;

        while (result instanceof InvocationTargetException) {
            result = result.getCause();
        }

        return result;
    }


    public static String getExceptionName(InvocationTargetException e) {
        return getTrueException(e).getClass().getSimpleName();
    }


    /**
     *
     * @param file
     * @return
     * @throws ManualGradingError
     */
    public static String determineCorrectClassName(File file)
            throws ManualGradingError {

        ArrayList<String> source = SourceUtilities.getSource(file, true, true);

        for (String line : source) {
            if (line.trim().startsWith("public class ")) {
                String[] split = line.split(" ");

                for (int i = 0; i < split.length; ++i) {
                    if (split[i].equals("public")) {
                        if (i + 2 < split.length) {
                            String result = split[i + 2];

                            if (result.endsWith("{")) {
                                result = result.substring(0, result.length() - 1);
                            }

                            return result;
                        }
                    }
                }
            }
        }

        throw new ManualGradingError("Code was too complex to automatically rename;" +
                " please do so manually.");
    }

}
