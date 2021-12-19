package me.friwi.jcefmavenbot.issues;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;

public class HttpURLConnectionPatch {
    /* valid HTTP methods */
    private static final String[] methods = {
            "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE", "PATCH"
    };

    public static void allowPatchMethod() throws NoSuchFieldException, IllegalAccessException {
        Field f = HttpURLConnection.class.getDeclaredField("methods");
        f.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

        f.set(null, methods);
    }
}
