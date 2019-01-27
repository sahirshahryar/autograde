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

import grader.util.Helper;
import grader.util.Tuple;

import java.util.*;

/**
 *
 *
 * @author  Sahir Shahryar <sahirshahryar@uga.edu>
 * @since   Monday, June 29, 2015
 * @version 1.0.0
 */
public class FlagSet
  implements Iterable<Flag> {

    /**
     * This <code>HashMap&lt;Character, Tuple&lt;...&gt;&gt;</code>
     * is used  as a sort of database. Since each registry's flags
     * vary, this map keeps track of the flags a registry has, as well
     * as the type of each command and the command's default value.
     */
    private final HashMap<String, Flag> validFlags = new HashMap<>();


    /**
     * This <code>HashSet&lt;Flag&gt;</code> retains any command input
     * acquired by parsing the registry's arguments.
     */
    private final HashMap<String, Flag> flagData = new HashMap<>();


    private final ArrayList<Tuple<Flag, Flag>> disallowedCombos = new ArrayList<>();


    private transient String lastAppendedFlag = null;


    /**
     * This method initializes an empty FlagMap.
     */
    public FlagSet() {
        // This code block is intentionally left blank.
    }

    public FlagSet accepts(String flag) {
        if (flag.length() > 1) {
            flag = flag.toLowerCase();
        }

        if (!validFlags.containsKey(flag)) {
            this.validFlags.put(flag, new Flag(flag));
        }

        this.lastAppendedFlag = flag;
        return this;
    }


    public FlagSet accepts(String flag, String defValue) {
        if (flag.length() > 1) {
            flag = flag.toLowerCase();
        }

        if (!validFlags.containsKey(flag)) {
            this.validFlags.put(flag, new Flag(flag, defValue));
        }

        this.lastAppendedFlag = flag;
        return this;
    }


    public FlagSet describeAs(String desc) {
        if (this.lastAppendedFlag == null) {
            throw new IllegalArgumentException("Cannot describe a command before any " +
                    "have been added to the FlagSet!");
        }

        Flag f = this.validFlags.get(this.lastAppendedFlag);

        if (f == null) {
            throw new RuntimeException("the command '" + this.lastAppendedFlag + "' was " +
                    "apparently not registered");
        }

        f.setDescription(desc);

        return this;
    }

    public FlagSet withParamName(String paramName) {
        if (this.lastAppendedFlag == null) {
            throw new IllegalArgumentException("Cannot describe a command before any " +
                    "have been added to the FlagSet!");
        }

        Flag f = this.validFlags.get(this.lastAppendedFlag);

        if (f == null) {
            throw new RuntimeException("the command '" + this.lastAppendedFlag + "' was " +
                    "apparently not registered");
        }

        f.setParamName(paramName);
        return this;
    }


    public FlagSet withAliases(String... aliases) {
        if (this.lastAppendedFlag == null) {
            throw new IllegalArgumentException("Cannot describe a command before any " +
                    "have been added to the FlagSet!");
        }

        Flag f = this.validFlags.get(this.lastAppendedFlag);

        if (f == null) {
            throw new RuntimeException("the command '" + this.lastAppendedFlag + "' was " +
                    "apparently not registered");
        }

        for (String alias : aliases) {
            f.addAlias(alias);
        }

        return this;
    }


    public FlagSet disallowTogether(String flag1, String flag2) {
        Flag f1 = this.resolve(flag1), f2 = this.resolve(flag2);

        if (f1 == null || f2 == null) {
            // This probably shouldn't fail silently.
            return this;
        }

        this.disallowedCombos.add(new Tuple<>(f1, f2));
        return this;
    }


    protected Flag resolve(String flag) {
        for (Flag f : this.validFlags.values()) {
            if (f.is(flag)) {
                return f;
            }
        }

        return null;
    }


    /**
     *
     *
     * @param flag
     * @return
     */
    public FlagSet flagUsed(String flag) {
        if (flag.length() > 1) {
            flag = flag.toLowerCase();
        }

        Flag f = this.resolve(flag);

        if (f == null) {
            throw new RuntimeException("flagUsed() called before validation (?????)");
        }

        switch (f.getType()) {
            case BOOLEAN:
                if (!this.hasFlag(flag)) {
                    flagData.put(f.getFlag(), f);
                }

                else {
                    return this.flagCanceled(flag);
                }

                break;

            default: // String with no given value
                flagData.put(f.getFlag(), f);
        }

        return this;
    }

    public void validateUsage(String flag) {
        Flag f = this.resolve(flag);

        if (f == null) {
            throw new RuntimeException("Invalid command input: unknown option "
                                       + makeFlagText(flag));
        }

        for (Tuple<Flag, Flag> tuple : this.disallowedCombos) {
            if (tuple.has(f)) {
                throw new RuntimeException("The options " + makeFlagText(flag) + " and "
                                           + makeFlagText(tuple.other(f).toString())
                                           + " cannot be used in conjunction");
            }
        }
    }

    public FlagSet flagUsed(String flag, String value) {
        Flag f = this.resolve(flag);
        if (f == null) {
            throw new RuntimeException("flagUsed() called before validation (?????)");
        }
        
        flagData.put(f.getFlag(), new Flag(f.getFlag(), value));

        return this;
    }

    public FlagSet flagCanceled(String flag) {
        if (this.hasFlag(flag)) {
            flagData.remove(flag);
        }

        return this;
    }

    public String makeFlagText(String flag) {
        return (flag.length() == 1 ? "-" : "--") + flag;
    }

    public String getDefaultValue(String flag) {
        return this.validFlags.get(flag).getValue();
    }

    public FlagType getType(String flag) {
        Flag f = this.resolve(flag);
        return f == null ? FlagType.UNREGISTERED : f.getType();
    }

    public boolean hasFlag(String flag) {
        Flag f = this.resolve(flag);

        if (f == null) {
            return false;
        }

        return this.flagData.containsKey(f.getFlag());
    }


    public Flag getFlag(String flag) {
        for (Flag f : this.flagData.values()) {
            if (f.is(flag)) {
                return f;
            }
        }

        return null;
    }

    public HashMap<String, String> flagDescriptions() {
        HashMap<String, String> result = new HashMap<>();

        for (String key : this.validFlags.keySet()) {
            Flag flag = this.validFlags.get(key);

            ArrayList<String> names = new ArrayList<>();

            for (String flagAlias : flag.allKeys()) {
                names.add((flagAlias.length() > 1 ? "--" : "-") + flagAlias);
            }

            String term = Helper.elegantPrintList(names, true);
            if (flag.getType() == FlagType.VALUED) {
                term += " " + flag.getParamName();
            }

            result.put(term, flag.getDescription());
        }

        return result;
    }

    @Override
    public Iterator<Flag> iterator() {
        return this.flagData.values().iterator();
    }
}