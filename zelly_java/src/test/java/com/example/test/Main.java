package com.example.test;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int t;

        t = sc.nextInt();
        int a;

        int[] numArr = new int[t];
        for (int i = 0; i < t; i++) {
            a = sc.nextInt();
            numArr[i] = a;
        }
        System.out.print(Arrays.stream(numArr).min().getAsInt());
        System.out.println(" "+Arrays.stream(numArr).max().getAsInt());
    }
}
