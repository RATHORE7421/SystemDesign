package src.hldConcepts.Multithreading;

class Pen {

    public synchronized void writeWithPenAndPaper(Paper paper) {
        System.out.println("" + Thread.currentThread().getName() + " Trying to get Paper class");
        paper.toErase("I want to erase");
    }

    public synchronized void toWrite(String brand) {
        System.out.println(brand);
    }
}

class Paper {

    public synchronized void writeWithPenAndPaper(Pen pen) {
        System.out.println("" + Thread.currentThread().getName() + " Trying to get pen class");
        pen.toWrite("I want to write");
    }

    public synchronized void toErase(String content) {
        System.out.println(content);
    }
}

class Task1 implements Runnable {
    private Pen pen ;
    private Paper paper;

    public Task1(Pen pen, Paper paper) {
        this.pen = pen;
        this.paper = paper;
    }

    @Override
    public void run() {
        pen.writeWithPenAndPaper(paper);
    }
}

class Task2 implements Runnable {
    private Pen pen ;
    private Paper paper;

    public Task2(Pen pen, Paper paper) {
        this.pen = pen;
        this.paper = paper;
    }

    @Override
    public void run() {
        synchronized(pen)
            {paper.writeWithPenAndPaper(pen);}
    }
}

public class DeadlockEmample {
    public static void main(String[] args) {
        Pen pen = new Pen();
        Paper paper = new Paper();
        Thread t1 = new Thread(new Task1(pen, paper));
        Thread t2 = new Thread(new Task2(pen, paper));
        t1.start();
        t2.start();
    }
}
