package id.dkakunsi.lab.jlogging.attributeloader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.dkakunsi.lab.jlogging.layout.CustomJsonLayout;

/**
 * Loader for Exception stacktrace.
 * 
 * @author dkakunsi
 */
public class StacktraceAttributeLoader extends EventAttributeLoader {

    private static final String STACKTRACE = "stacktrace";

    public StacktraceAttributeLoader(CustomJsonLayout layout) {
        super(layout);
    }

    @Override
    public Object load(String key) {
        if (!contains(key)) {
            return null;
        }
        return layout.getEvent().getThrown() != null
                ? generateStackTrace(this.layout.getEvent().getThrown(), this.layout.isRecursiveStackTrace())
                : null;
    }

    @Override
    public boolean contains(String key) {
        return STACKTRACE.equals(key);
    }

    /**
     * <p>
     * Generate exception stack trace. The result is list of exception in custom
     * format.
     * </p>
     * The example is:
     * 
     * <pre>
     * {
     *      "exception": "java.lang.Exception",
     *      "message": "Exception message",
     *      "stack": "[LIST OF CALLING STACKTRACE]"
     * }
     * </pre>
     * 
     * To generate the stack element, please see
     * {@link #generateStackTrace(StackTraceElement[])}.
     * 
     * @param thrown    the exception
     * @param recursive whether to print the exception recursively. {@code false}
     *                  will just print the most inner exception.
     * @return list of custom-formatted exception data.
     */
    public static List<Object> generateStackTrace(Throwable thrown, boolean recursive) {
        List<Object> stack = new ArrayList<>();

        if (recursive) {
            if (thrown.getCause() != null) {
                stack.addAll(generateStackTrace(thrown.getCause(), recursive));
            }
        } else {
            thrown = getInnerException(thrown);
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("exception", thrown.getClass().getName());
        map.put("message", thrown.getMessage());
        map.put("stack", generateStackTrace(thrown.getStackTrace()));

        stack.add(map);

        return stack;
    }

    /**
     * Get the most inner exception.
     * 
     * @param thrown exception
     * @return the most inner exception
     */
    private static Throwable getInnerException(Throwable thrown) {
        while (thrown.getCause() != null) {
            thrown = thrown.getCause();
        }
        return thrown;
    }

    /**
     * <p>
     * Generate calling stacktrace of an exception. The result of list of calling
     * stacktrace in custom format.
     * </p>
     * The format is:
     * 
     * <pre>
     * {
     *      "file": "App.java",
     *      "method": "main",
     *      "line": 20
     * }
     * </pre>
     * 
     * @param stacktraces stacktrace element
     * @return list of custom-formatted stacktrace
     */
    private static List<Object> generateStackTrace(StackTraceElement[] stacktraces) {
        List<Object> stack = new ArrayList<>();

        Map<String, Object> map;
        for (StackTraceElement stacktrace : stacktraces) {
            map = new LinkedHashMap<>();
            map.put("file", stacktrace.getFileName());
            map.put("method", stacktrace.getMethodName());
            map.put("line", stacktrace.getLineNumber());

            stack.add(map);
        }

        return stack;
    }
}
