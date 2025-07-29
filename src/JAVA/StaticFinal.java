package src.JAVA;

import java.util.*;

class Parent {
    public static void see(){
        System.out.println("Parent static method see");
    }

    public static void walk(){
        System.out.println("Parent static method walk");
    }
}

class Child extends Parent {
    public static void see(){
        System.out.println("Child static method see");
    }
}

public class StaticFinal {
    public static void main(String[] args) {
        Child child = new Child();
        child.see();

        String s = "Hello";
        String y = "Hello";

        s= s.replace('H', 'h');

        Map<Integer, Integer> map = new HashMap<>();
        map.put(null, null);

        System.out.println(map.get(null));

        System.out.println(s + " " + y);
    }
}
