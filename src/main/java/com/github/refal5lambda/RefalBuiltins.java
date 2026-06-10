package com.github.refal5lambda;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Names offered by completion (directives + common Refal-5 built-ins) and their short docs. */
public final class RefalBuiltins {
    private RefalBuiltins() {}

    public static final String[] DIRECTIVES = {
            "$ENTRY", "$EXTERN", "$EXTRN", "$EXTERNAL", "$EASTEREGG",
            "$ENUM", "$EENUM", "$SWAP", "$ESWAP", "$SCOPEID",
            "$DRIVE", "$INLINE", "$SPEC", "$INCLUDE"
    };

    // Short descriptions of well-known Refal-5 / Library built-ins (brief; exact behaviour can vary
    // slightly between dialects and library versions).
    private static final Map<String, String> DESCRIPTIONS = new LinkedHashMap<>();
    static {
        DESCRIPTIONS.put("Add", "Arithmetic addition of two numbers.");
        DESCRIPTIONS.put("Sub", "Arithmetic subtraction of two numbers.");
        DESCRIPTIONS.put("Mul", "Arithmetic multiplication of two numbers.");
        DESCRIPTIONS.put("Div", "Integer division of two numbers.");
        DESCRIPTIONS.put("Mod", "Remainder of integer division.");
        DESCRIPTIONS.put("Divmod", "Integer division returning both quotient and remainder.");
        DESCRIPTIONS.put("Compare", "Compares two numbers, yielding '-', '0' or '+'.");
        DESCRIPTIONS.put("Numb", "Converts a string of decimal digits into a number.");
        DESCRIPTIONS.put("Symb", "Converts a number into its string of decimal digits.");
        DESCRIPTIONS.put("Chr", "Converts numeric codes into characters.");
        DESCRIPTIONS.put("Ord", "Converts characters into their numeric codes.");
        DESCRIPTIONS.put("Type", "Classifies the first term (letter, digit, bracket, ...).");
        DESCRIPTIONS.put("Explode", "Splits a compound symbol/identifier into its characters.");
        DESCRIPTIONS.put("Implode", "Builds an identifier from a sequence of characters.");
        DESCRIPTIONS.put("First", "Splits off the first N terms of an expression.");
        DESCRIPTIONS.put("Last", "Splits off the last N terms of an expression.");
        DESCRIPTIONS.put("Lenw", "Returns the length (number of terms) of an expression.");
        DESCRIPTIONS.put("Card", "Reads one line from standard input.");
        DESCRIPTIONS.put("Open", "Opens a file and associates it with a channel.");
        DESCRIPTIONS.put("Get", "Reads one line from an open channel.");
        DESCRIPTIONS.put("Put", "Writes an expression to an open channel.");
        DESCRIPTIONS.put("Putout", "Writes to a channel and also echoes to standard output.");
        DESCRIPTIONS.put("Print", "Prints an expression to standard output, returning it unchanged.");
        DESCRIPTIONS.put("Prout", "Prints an expression to standard output (discarding it).");
        DESCRIPTIONS.put("Close", "Closes an open channel.");
        DESCRIPTIONS.put("Mu", "Metacall: calls a function given its name.");
        DESCRIPTIONS.put("Br", "Buries (stores) a value under a key in the associative storage.");
        DESCRIPTIONS.put("Dg", "Digs up (retrieves and removes) a buried value by key.");
        DESCRIPTIONS.put("Cp", "Copies a buried value by key without removing it.");
        DESCRIPTIONS.put("Rp", "Replaces a buried value for a key.");
        DESCRIPTIONS.put("Arg", "Returns a command-line argument by index.");
        DESCRIPTIONS.put("GetEnv", "Returns the value of an environment variable.");
        DESCRIPTIONS.put("System", "Runs a command through the operating-system shell.");
        DESCRIPTIONS.put("Time", "Returns the current date/time.");
        DESCRIPTIONS.put("Upper", "Converts letters to upper case.");
        DESCRIPTIONS.put("Lower", "Converts letters to lower case.");
    }

    /** Built-in function names (for completion). */
    public static final String[] FUNCTIONS = DESCRIPTIONS.keySet().toArray(new String[0]);

    /**
     * Public functions of the Refal-5λ standard library (the {@code $ENTRY} names of the library
     * sources). Used so that the instant "Unresolved function" check does not flag legitimate
     * library calls. Captured from bmstu-iu9/simple-refal-distrib (lib/src/*.ref).
     */
    private static final String[] LIBRARY = {
            "Add", "AppendBytes", "AppendFile", "Apply", "Arg", "ArgList", "Br", "Card", "Chr",
            "Close", "Compare", "Cp", "DeSysfun", "Dec", "DelAccumulator", "Dg", "Dgall",
            "DirectorySeparator", "Div", "Divmod", "ExistFile", "Exit", "Explode", "Explode_Ext",
            "FSeek", "FTell", "Fetch", "First", "Get", "GetCurrentDirectory", "GetEnv", "GetOpt",
            "GetPID", "GetPPID", "HashLittle2-Chars", "Implode", "Implode_Ext", "Inc",
            "IsDirectorySeparator", "Last", "Lenw", "ListOfBuiltin", "LoadBytes", "LoadFile",
            "Lower", "Map", "MapAccum", "Max", "Min", "Mod", "Module-FunctionPtr", "Module-Load",
            "Module-LookupFunction", "Module-Mu", "Module-Unload", "Mul", "Numb", "OneOf", "Open",
            "Open-Auto", "Ord", "PathSeparator", "Pipe", "Platform", "Print", "Proud", "Prout",
            "Put", "Putout", "Random", "RandomDigit", "ReadBytes", "Reduce", "RemoveFile",
            "RenameFile", "Rp", "SaveBytes", "SaveFile", "SizeOf", "Sort", "Step", "Sub", "Symb",
            "SymbCompare", "Sysfun", "System", "TermCompare", "TermCompare-T", "Time", "TimeElapsed",
            "Trim", "Trim-L", "Trim-R", "Trout", "Type", "UnBracket", "Unique", "Upper", "Write",
            "WriteBytes", "Mu", "Up", "Dn"
    };

    private static final Set<String> KNOWN = new HashSet<>();
    static {
        KNOWN.addAll(DESCRIPTIONS.keySet());
        KNOWN.addAll(Arrays.asList(LIBRARY));
    }

    /** @return true if {@code name} is a known built-in or standard-library function. */
    public static boolean isKnownFunction(String name) {
        return KNOWN.contains(name);
    }

    /** @return a short description for a built-in name, or {@code null} if it is not a known built-in. */
    public static @Nullable String describe(String name) {
        return DESCRIPTIONS.get(name);
    }
}
