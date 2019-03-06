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
package grader.stability;

import java.security.Permission;

/**
 * Custom SecurityManager to block student code from exiting the program.
 *
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   11/28/18
 * @version 1.0.0
 */
public class ExitBlocker
     extends SecurityManager {

    /**
     * This value determines whether ExitBlocker forbids System.exit() calls. This is
     * necessary since the 'quit' command indirectly calls System.exit() itself.
     */
    private boolean permitExit = false;

    @Override
    public void checkPermission(Permission perm) {
        if (!permitExit && perm.getName().startsWith("exitVM")) {
            throw new ExitException();
        }

        // This is probably not a good idea
        // backend.checkPermission(perm);
    }

    /**
     * Allows AutoGrade itself to exit when the user asks for it.
     */
    public void permitExit() {
        this.permitExit = true;
    }

}
