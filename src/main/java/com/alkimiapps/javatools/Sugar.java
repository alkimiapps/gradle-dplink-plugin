package com.alkimiapps.javatools;

/**
 * Provide some useful syntactic sugar.
 */
public class Sugar {
    /**
     * Guard against a fatal condition. If the condition is false then the provided Runnable is executed followed
     * by calling System.exit.
     *
     * NOTE: not recommended for use in any application where it is not acceptable to exit the jvm.
     *
     * @param condition the condition against which to guard i.e. if it resolves to false then the function is executed
     * @param function function to execute if the condition fails
     */
    public static void fatalGuard(boolean condition, Runnable function) {
        if (!condition) {
            function.run();
            System.exit(-1);
        }
    }

    /**
     * Guard against a fatal condition. If the condition is false then a RuntimeException is thrown with the
     * provided message. This is quite similar to the assert function except assert is only meant for development
     * whereas fatalGuard is for runtime fatal exceptions.
     *
     * NOTE: this method will throw a RuntimeException if the specifed condition resolves to false
     *
     * @param condition the condition against which to guard i.e. if it resolves to false then a RuntimeException is thrown
     * @param message String to use for the RuntimeException message in the event that the condition resolves to false
     */
    public static void fatalGuard(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException(message);
        }
    }
}
