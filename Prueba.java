package com.company;

import java.util.ArrayList;
import java.util.Arrays;

public class Prueba {

    public static void main (String[] args) {
        ArrayList<Integer> nums = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        for (Integer num: nums) {
            System.out.println(num);
            nums.remove(num);
        }
    }
}
