package gasp.core.util;

/**
 * Function interface.
 * <p>
 * Equivalent to {@link java.util.function.Function} except that it allows
 * the function to throw an exception.
 * </p>
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface Function<T, R> {

    /**
     * Invokes the function.
     */
    R apply(T input) throws Exception;
}
