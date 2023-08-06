package org.tud.oas.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Range implements Iterable<Integer> {
    private int start, end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new RangeIterator();
    }

    // Inner class example
    private class RangeIterator implements
            Iterator<Integer> {
        private int cursor;

        public RangeIterator() {
            this.cursor = Range.this.start;
        }

        @Override
        public boolean hasNext() {
            return this.cursor < Range.this.end;
        }

        @Override
        public Integer next() {
            if (this.hasNext()) {
                int current = cursor;
                cursor++;
                return current;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
