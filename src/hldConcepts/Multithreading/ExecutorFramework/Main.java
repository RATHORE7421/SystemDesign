package src.hldConcepts.Multithreading.ExecutorFramework;

public class Main {
    public static void main(String[] args) {
        int[] arr = new int[5];
        for(int i: arr) {
            System.out.println(i);
        }
        // long st = System.currentTimeMillis();
        // for(int i = 1; i<=10; i++) {
        //     int fact = factorial(i);
        //     System.out.println(fact);
        // }

        // System.out.println("total time " + (System.currentTimeMillis() - st));
    }

    public static int factorial(int n) {
        try{Thread.sleep(100);} catch(Exception e) {}
        
        int ans = 1;
        for(int i= 1; i<=n; i++)
        {
            ans*=i;
        }
        return ans;
    }
}
