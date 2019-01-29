package com.unfortunatelySober.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

/**
 * Created by Cody on 20/12/2018.
 *
 * Random access list, mainly overrides {@link #set(int, Object) set} method to not throw
 * IndexOutOfBoundsException in the event that the index is greater then the current size of the list
 * <P>
 * Instead the list will be resized such that the index given is the last index of the array
 * and sets it to the value given as usual, any empty slots created by this are left uninitialized
 * i.e. set to null
 * <P>
 * The RAList makes use of reflection to get the underlying size field in the {@link ArrayList ArrayList}
 * in order to grow the list without using any kinds of loops, this may not be the best method but it is
 * the quickest that I know of
 *
 * @param <E> type the collection will contain
 */
public class RAList<E> extends ArrayList<E> {

    //The size Field of the inherited ArrayList, used to quickly increase the Lists size
    private static Field sizeSetter;

    //Set up the sizeSetter field
    static {
        try {
            sizeSetter = ArrayList.class.getDeclaredField("size");
            sizeSetter.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Forces the supplied {@link ArrayList ArrayList} to the specified size,
     * unlike ensure capacity this not only allocates more space, but also grows
     * the {@link ArrayList ArrayList}, filling all the new slots with null
     *
     * @param list the list to grow
     * @param size the new size of the list
     * @param <T> The type of the elements stored in the supplied {@link ArrayList ArrayList}
     */
    public static <T> void forceArrayListSize(ArrayList<T> list, int size) {
        if (size < list.size()) return;
        list.ensureCapacity(size);
        try {
            sizeSetter.set(list, size);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public RAList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public RAList() {
        super();
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public RAList(Collection<? extends E> c) {
        super(c);
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element. If index is greater then the size of the list,
     * the list is resized in order to fit it
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position or null if the list is to small
     */
    @Override
    public E set(int index, E element) {
        if (index >= size()) {
            forceArrayListSize(this, index+1);
        }
        return super.set(index, element);
    }
}
