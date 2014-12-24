package jasp.core.db;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A streamable result of a query.
 *
 * @param <T> Type of object returned from the stream.
 */
public class Result<T> {

    Consumer<Void> finish;
    Iterable<T> iterable;

    /**
     * Sets the iterable containing objects of the stream.
     *
     * @return This object.
     */
    public Result set(Iterable<T> iterable) {
        this.iterable = iterable;
        return this;
    }

    /**
     * The iterable containing objects of the stream.
     */
    public Iterable<T> get() {
        return iterable;
    }

    /**
     * Registers a callback to be invoked when the result stream has been exhausted.
     */
    public Result finish(Consumer<Void> finish) {
        this.finish = finish;
        return this;
    }

    /**
     * The optional callback to be invoked when the result stream is exhausted.
     */
    public Optional<Consumer<Void>> finish() {
        return Optional.ofNullable(finish);
    }
}
