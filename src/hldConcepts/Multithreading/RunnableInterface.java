package src.hldConcepts.Multithreading;

public class RunnableInterface {
    public static void main(String[] args) throws InterruptedException {
        LockEx sbi = new LockEx();
        Runnable tInterface = new Runnable() {
            @Override
            public void run() {
                sbi.withdrawAmt(600);
            }
        };
    
        Thread t1 = new Thread(tInterface, "Priya");
        Thread t2 = new Thread(tInterface, "Krishna");
    
        t1.start();
        t2.start();
    
        t1.join();
        t2.join();
    
        sbi.checkBalance();
    }
};
