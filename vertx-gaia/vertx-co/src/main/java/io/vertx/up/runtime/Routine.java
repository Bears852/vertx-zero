package io.vertx.up.runtime;

/**
 * Routine execute map
 *
 * @param <T>
 */
public interface Routine<T> extends Runnable {
    /**
     * Executed after thread finished.
     *
     * @return Generic type will be run in thread
     */
    T get();

    /**
     * Get executed key of this thread.
     *
     * @return Get current thread name to be identified by the system.
     */
    String getKey();
}
