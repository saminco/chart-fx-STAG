/*
 * Copyright (c) 2010, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package de.gsi.chart.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * A mock observer that tracks calls to its onChanged() method,
 * combined with utility methods to make assertions on the calls made.
 * @param <E> generic list argument
 *
 */
public class MockListObserver<E> implements ListChangeListener<E> {
    private boolean tooManyCalls;
    private final List<Call<E>> calls = new LinkedList<>();

    static class Call<E> {
        protected ObservableList<? extends E> list;
        protected List<? extends E> removed;
        protected int from;
        protected int to;
        private int[] permutation;
        private boolean update;
        @Override
        public String toString() {
            return "removed: " + removed + ", from: " + from + ", to: " + to + ", permutation: " + Arrays.toString(permutation);
        }
    }

    @Override
    public void onChanged(final Change<? extends E> change) {
        if (calls.isEmpty()) {
            while (change.next()) {
                Call<E> call = new Call<>();
                call.list = change.getList();
                call.removed = change.getRemoved();
                call.from = change.getFrom();
                call.to = change.getTo();
                if (change.wasPermutated()) {
                    call.permutation = new int[call.to - call.from];
                    for (int i = 0; i < call.permutation.length; ++i) {
                        call.permutation[i] = change.getPermutation(i + call.from);
                    }
                } else {
                    call.permutation = new int[0];
                }
                call.update = change.wasUpdated();
                calls.add(call);

                // Check generic change assertions
                assertFalse(change.wasPermutated() && change.wasUpdated());
                assertFalse((change.wasAdded() || change.wasRemoved()) && change.wasUpdated());
                assertFalse((change.wasAdded() || change.wasRemoved()) && change.wasPermutated());
            }
        } else {
            tooManyCalls = true;
        }
    }

    public void check0() {
        assertEquals(0, calls.size());
    }

    public void check1AddRemove(final ObservableList<E> list, final List<E> removed, final int from, final int to) {
        assertFalse(tooManyCalls);
        assertEquals(1, calls.size());
        checkAddRemove(0, list, removed, from, to);
    }

    public void checkAddRemove(final int idx, final ObservableList<E> list, final List<E> removed, final int from, final int to) {
        List<E> removedValidated = removed;
        if (removed == null) {
            removedValidated = Collections.emptyList();
        }
        assertFalse(tooManyCalls);
        Call<E> call = calls.get(idx);
        assertSame(list, call.list);
        assertEquals(removedValidated, call.removed);
        assertEquals(from, call.from);
        assertEquals(to, call.to);
        assertEquals(0, call.permutation.length);
    }

    public void check1Permutation(final ObservableList<E> list, final int[] perm) {
        assertFalse(tooManyCalls);
        assertEquals(1, calls.size());
        checkPermutation(0, list, 0, list.size(), perm);
    }

    public void check1Permutation(final ObservableList<E> list, final int from, final int to, final int[] perm) {
        assertFalse(tooManyCalls);
        assertEquals(1, calls.size());
        checkPermutation(0, list, from, to, perm);
    }

    public void checkPermutation(final int idx, final ObservableList<E> list, final int from, final int to, final int[] perm) {
        assertFalse(tooManyCalls);
        Call<E> call = calls.get(idx);
        assertEquals(list, call.list);
        assertEquals(Collections.EMPTY_LIST, call.removed);
        assertEquals(from, call.from);
        assertEquals(to, call.to);
        assertArrayEquals(perm, call.permutation);
    }

    public void check1Update(final ObservableList<E> list, final int from, final int to) {
        assertFalse(tooManyCalls);
        assertEquals(1, calls.size());
        checkUpdate(0, list, from, to);
    }

    public void checkUpdate(final int idx, final ObservableList<E> list, final int from, final int to) {
        assertFalse(tooManyCalls);
        Call<E> call = calls.get(idx);
        assertEquals(list, call.list);
        assertEquals(Collections.EMPTY_LIST, call.removed);
        assertArrayEquals(new int[0], call.permutation);
        assertTrue(call.update);
        assertEquals(from, call.from);
        assertEquals(to, call.to);
    }

    public void check1() {
        assertFalse(tooManyCalls);
        assertEquals(1, calls.size());
    }

    public void clear() {
        calls.clear();
        tooManyCalls = false;
    }
}
