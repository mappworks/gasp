package gasp.core.util;

/**
 * Functional interface that allows function to throw exception.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface UnsafeFunction<T, R> {

    R apply(T input) throws Exception;
}
