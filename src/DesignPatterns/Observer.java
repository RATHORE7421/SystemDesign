package src.DesignPatterns;

import java.util.*;

interface Observers {
    public void notifyToUsers(String s);
}

class EmailObserver implements Observers {
    public void notifyToUsers(String s) {
        System.out.println("Message received via Email " + s);
    }
}

class MobileObserver implements Observers {
    public void notifyToUsers(String s) {
        System.out.println("Message received via Mobile " + s);
    }
}

class WebDashboardObserver implements Observers {
    public void notifyToUsers(String s) {
        System.out.println("Message received via Web Dashboard " + s);
    }
}

class NewsPublisher {
    List<Observers> observers = new ArrayList<>();

    public void addObserver(Observers ob) {
        observers.add(ob);
    }

    public void removeObserver(Observers ob) {
       System.out.println("I am here");
        observers.remove(ob);
    }

    public void setMessage(String s) {
        notifyAllObservers(s);
    }

    public void notifyAllObservers(String s) {
        for(Observers o: observers) {
            o.notifyToUsers(s);
        }
    }
}

public class Observer {
    public static void main(String[] args) {
        NewsPublisher np = new NewsPublisher();
        
        np.addObserver(new EmailObserver());
        np.addObserver(new MobileObserver());
        np.addObserver(new WebDashboardObserver());

        np.removeObserver(new EmailObserver());
        np.notifyAllObservers("What's up baby");
        np.notifyAllObservers("I am good sexy");
    }
}
