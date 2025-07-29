package src.hldConcepts.Multithreading;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class LockEx {
    public static int totalAmt = 1000; 
    private final Lock lock = new ReentrantLock();

    void withdrawAmt(int amt) {
        try {
            if(lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
                if(totalAmt >= amt) {
                    System.out.println("Withdrawl amt " + amt);
                    totalAmt-=amt;
                    Thread.sleep(3000);
                    System.out.println("Amount remaining " + totalAmt);
                    lock.unlock();
                } else {
                    System.out.println("Your bank balance is Empty");
                }
            } else {
                System.out.println(Thread.currentThread().getName() + " Could not acquire the lock, will try later");
            }
        }
        catch (Exception e){

        }
        System.out.println("I am here after my termination");
    }

    int checkBalance() {
        System.out.println("Your bank balance is " + totalAmt);
        return totalAmt;
    }
} 
