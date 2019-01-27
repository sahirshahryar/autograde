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
package grader.flag;

import java.util.ArrayList;

/**
 * Represents a command. A command is an optional, single-character or multi-character modifier
 * for the behavior of any text-based command. Flags are prefixed by hyphens (-);
 * multi-character flags are prefixed by two hyphens (--). Single-character flags can be
 * combined into a single argument (e.g., -a -b -c can become -abc).
 *
 * Here are some examples of command usage in commands:
 *
 *     java AutoGrade StatScript.java submissions --verbose
 *     java AutoGrade StatScript.java submissions --timeout 5
 *
 * Flags allow for added flexibility in command parsing while still allowing the
 * programmer to set hard-and-fast rules about syntax. For example, the AutoGrade
 * main() method accepts exactly 1 or 2 arguments, but flags can be interlaced anywhere
 * in the actual call:
 *
 *     java AutoGrade --verbose --timeout 5 StatScript.java --no-color submissions
 *
 * The FlagParser class requires all flags to be registered prior to usage.
 *
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   Monday, June 29, 2015
 * @version 1.1.0
 */
public class Flag {

    /**
     * The key is the character or String that represents the command.
     *
     *     a-z, A-Z, ~, !, @, #, $, %, ^, &, *, _, +, ?, <, >, :, |
     *
     * @see #Flag(String) Initializer for boolean flags
     * @see #Flag(String, String) Initializer for valued flags
     * @see #getFlag() Associated getter
     */
    private final String key;

    private final ArrayList<String> aliases;


    private final FlagType type;


    /**
     * Represents the value associated with this command. For example, the --timeout command
     * accepts an integer following it. For simplicity, all outputs are stored as Strings
     * and must be manually converted later.
     */
    private String value;


    /**
     * A help message describing what this command does.
     *
     * @since 1.1.0
     */
    private String description;


    /**
     *
     */
    private String paramName = "<value>";


    /**
     * Initializes a new Flag object.
     *
     * @param key   (String) the key of the new command.
     * @param value (String) the value stored in the command.
     */
    public Flag(String key, FlagType type, String value, String description, ArrayList<String> aliases) {
        this.key = key;
        this.type = type;
        this.value = value;
        this.description = description;
        this.aliases = aliases;
    }


    public Flag(String key, String value) {
        this(key, FlagType.VALUED, value, "", new ArrayList<>());
    }


    /**
     * Initializes a new Flag object with no value (i.e., a boolean command).
     *
     * @param key (String) the key of the new command.
     */
    public Flag(String key) {
        this(key, FlagType.BOOLEAN, "", "", new ArrayList<>());
    }


    /**
     * Returns the key of the command.
     *
     * @return (String) the key of the command.
     */
    public String getFlag() {
        return this.key;
    }

    public void addAlias(String alias) {
        if (!this.aliases.contains(alias.toLowerCase())) {
            this.aliases.add(alias.toLowerCase());
        }
    }

    public ArrayList<String> allKeys() {
        ArrayList<String> result = new ArrayList<>(this.aliases);
        result.add(0, this.key);
        return result;
    }


    public boolean is(String key) {
        if (this.key.equalsIgnoreCase(key)) {
            return true;
        }

        for (String alias : this.aliases) {
            if (alias.equalsIgnoreCase(key)) {
                return true;
            }
        }

        return false;
    }


    public FlagType getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }


    /**
     *
     * @param paramName
     */
    public void setParamName(String paramName) {
        if (paramName != null) {
            this.paramName = paramName;
        }
    }


    /**
     * Returns the value of the command.
     *
     * @return (String) the value of the command, or an empty string if it has no value.
     */
    public String getValue() {
        return (this.value != null) ? this.value : "";
    }


    /**
     *
     * @return
     */
    public String getParamName() {
        return this.paramName;
    }


    /**
     * Sets the description of this command.
     * @param description
     */
    public void setDescription(String description) {
        this.description = (description != null) ? description : "";
    }

}