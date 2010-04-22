package sf.pnr.alg;

import java.lang.reflect.Array;

/**
 */
public final class ReinitStack <T extends Reinitialiseable> {

    private final T[] elements;
    private final Class<T> clazz;
    private int size;
    private int allocated;

    public ReinitStack(final Class<T> clazz, final int capacity) {
        elements = (T[]) Array.newInstance(clazz, capacity);
        allocated = 0;
        this.clazz = clazz;
        size = 0;
        allocate(20);
    }

    private void allocate(final int count) {
        try {
            for (int i = allocated + count - 1; i >= allocated; i--) {
                elements[i] = clazz.newInstance();
            }
            allocated += count;
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public void push() {
        if (allocated > size) {
            elements[size].reinitialise();
        } else {
            allocate(5);
        }
        size++;
    }

    public void pop() {
        size--;
    }

    public T peek() {
        return elements[size - 1];
    }

    public int size() {
        return size;
    }
}