package src.hldConcepts.Multithreading;

import java.util.concurrent.Semaphore;

// import java.lang.*;

class Printer {
    private Semaphore sc;
    Printer(int val) {
        this.sc = new Semaphore(val);
    }
    public void access() {
        try {
            sc.acquire();
            System.out.println(Thread.currentThread().getName() + " Is trying to access the resource");
            Thread.sleep(1000);
            System.out.println(Thread.currentThread().getName() + " is done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            sc.release();
        }
    }
}

public class SemaphoreMain {
    public static void main(String[] args) {
        Printer obj = new Printer(2);
        for(int i = 0; i<5; i++) {
            Thread t = new Thread( 
                () -> {
                    obj.access();
                }, "Thread" + i
            );
            t.start();
        }
    }
}
