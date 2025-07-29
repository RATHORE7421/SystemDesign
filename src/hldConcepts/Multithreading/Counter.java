package src.hldConcepts.Multithreading;

public class Counter extends Thread {
    public static int count = 0;

    @Override
    public void run() {
        for(int i = 0; i < 100000; i++) 
        {getIncrement();}
    }

    static synchronized void getIncrement() {
        count++;
    }

    int getCount() {
        return count;
    }
}
