package src.hldConcepts.Multithreading;


public class MultithreadingTest {
    public static void main(String[] args) {
        Counter c1 = new Counter();
        c1.start();
        Counter c2 = new Counter();
        c2.start();

        try {
            c1.join();
            c2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Final count: " + c1.getCount());
    }
}
