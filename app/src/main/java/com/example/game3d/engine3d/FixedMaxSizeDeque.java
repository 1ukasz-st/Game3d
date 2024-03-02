package com.example.game3d.engine3d;

public class FixedMaxSizeDeque<T> {
    private int front = -1, curr_size = 0;

    private final T[] elements;
    private final int max_size;

    public FixedMaxSizeDeque(int max_size) {
        this.max_size = max_size;
        this.elements = (T[]) (new Object[max_size]);
    }

    public int getMaxSize() {
        return max_size;
    }

    public T getFirst() {
        if (curr_size == 0) {
            throw new IllegalStateException("Deque is empty");
        }
        int ind = front + 1;
        return ind < max_size ? elements[ind] : elements[ind - max_size];
    }

    public T getLast() {
        if (curr_size == 0) {
            throw new IllegalStateException("Deque is empty");
        }
        int ind = front + curr_size;
        return ind < max_size ? elements[ind] : elements[ind - max_size];
    }

    public void removeFirst() {
        if (curr_size == 0) {
            throw new IllegalStateException("Deque is empty");
        }
        --curr_size;
        front = front < max_size - 1 ? front + 1 : 0;
      // elements[front] = null;
    }

    public void removeLast() {
        if (curr_size == 0) {
            throw new IllegalStateException("Deque is empty");
        }
        --curr_size;
    }

    public int size() {
        return curr_size;
    }

    public boolean isEmpty(){
        return size()==0;
    }

    public void clear(){
        while(size()>0){
            removeFirst();
        }
    }

    public void pushBack(T val) {
        if (curr_size == max_size) {
            throw new IllegalStateException("Size exceeded");
        }
        ++curr_size;
        int ind = front + curr_size;
        if (ind >= max_size) {
            ind -= max_size;
        }
        elements[ind] = val;
    }

    public T get(int ind) {
        if (ind < 0 || ind >= max_size) {
            throw new IllegalStateException("Index "+ind+"Out of bounds");
        }
        ind = front + ind + 1;
        if (ind >= max_size) {
            ind -= max_size;
        }
        return elements[ind];
    }

}