package src.hldConcepts.Multithreading;

class OrderStatus {
    public boolean hasOrder = false;

    public synchronized void produce() {
        while(hasOrder){
            System.out.println("Food is getting prepared");
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hasOrder = true;
        System.out.println("Food is ready");
        notify();
    }

    public synchronized void deliver() {
        while(!hasOrder){
            System.out.println("Waiting for the order");
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hasOrder = false;
        System.out.println("Food is delivered");
        notify();
    }
}

class Kitchen extends Thread {
    private OrderStatus status;

    Kitchen(OrderStatus status) {this.status = status;}

    @Override
    public void run() {
        status.produce();
    }
}

class Delivery extends Thread {
    private OrderStatus status;

    Delivery(OrderStatus status) {this.status = status;}

    @Override
    public void run() {
        status.deliver();
    }
}

public class ThreadCommunication {
    public static void main(String[] args) throws InterruptedException {
        OrderStatus order = new OrderStatus();

        Kitchen k = new Kitchen(order);
        Delivery d = new Delivery(order);
        
        d.start();
        k.start();
    }
}
