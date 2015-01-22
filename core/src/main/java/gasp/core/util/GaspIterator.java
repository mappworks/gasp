package gasp.core.util;

import com.google.common.collect.AbstractIterator;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Iterator wrapper that allows for registering a callback
 * when the iterator is exhausted.
 */
public class GaspIterator<T> extends AbstractIterator<T> {

    /** the underlying iterator */
    java.util.Iterator<T> it;

    /** callack to notify when iterator exhaused */
    Optional<Consumer<Void>> onFinish = Optional.empty();

    public GaspIterator(Iterator<T> it) {
        this.it = it;
    }

    @Override
    protected T computeNext() {
        if (it.hasNext()) {
            return it.next();
        }

        return finish();
    }

    T finish() {
        endOfData();
        onFinish.ifPresent((callback) -> callback.accept(null));
        return null;
    }

    public GaspIterator<T> onFinish(Consumer<Void> callback) {
        onFinish = Optional.ofNullable(callback);
        return this;
    }

}
