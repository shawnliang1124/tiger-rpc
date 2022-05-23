package com.shawnliang.tiger.core.utils;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/5/23
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> {

    private Map<E, Boolean> MAP;

    public ConcurrentHashSet() {
        super();
        MAP = new ConcurrentHashMap<>();
    }

    @Override
    public Iterator<E> iterator() {
        return MAP.keySet().iterator();
    }

    @Override
    public int size() {
        return MAP.size();
    }


    @Override
    public boolean add(E e) {
       return MAP.putIfAbsent(e, true) == null;
    }

    @Override
    public boolean remove(Object o) {
        return MAP.remove(o) != null;
    }

    @Override
    public boolean contains(Object o) {
        return MAP.containsKey(o);
    }
}
