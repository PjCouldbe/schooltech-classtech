package ru.pjcouldbe.classtech.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IntPair {
    private int first;
    private int second;
    
    public IntPair() {
        this.first = 0;
        this.second = 0;
    }
    
    public void incFirst() {
        addFirst(1);
    }
    
    public void incSecond() {
        addSecond(1);
    }
    
    public void addFirst(int v) {
        first += v;
    }
    
    public void addSecond(int v) {
        second += v;
    }
    
    public IntPair plus(IntPair p) {
        return new IntPair(first + p.first, second + p.second);
    }
}
