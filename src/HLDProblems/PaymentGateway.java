// package src.HLDProblems;
// // Functional Requirements:
// // Transactions from multiple bancks are allowed
// // Multiple payment options(CC, UPI, Netbanking)
// // Once PO is selcted , will redirect to particular back particular bank
// // Payment option is selected - Fill the details
// // Details could be valid or invalid 
// // Invalid - Msg: Invalid details 
// // Valid - Ask for PIN/ OTP
// // Deduct money from account
// // Msg for successful payment 


// // NFRs:
// // How many payments are getting done in a day : 


// // Estimates:
// // Actors involved:
// // User

// // Classes: 
// // - PaymentGateway - uuid, date, time, generateUUID()
// // - PaymentMode - UPI/Card/Net Banking = 

// // Code:


// // Payment system Conclusions:
// // Key requirements: Reliability , Fault-tolerance
// // Key solution tools: Redundancy, messaging queue, Idempotance, Strategies for retry(exponential backoff, jitter) & timeout, Pattern for payment guarantee

// import java.util.Scanner;
// import java.util.*;

// enum PaymentMode {
//     UPI, CARD, NETBanking, DEBIT_CARD
// }

// interface Bank {
//     String getname();
//     boolean processPayment(PaymentDetails details);
// }

// class RandomBank implements Bank {
//     String name;
//     RandomBank(String name){
//         this.name = name;
//     }
    
//     @Override
//     public String getName(){
//         return name;
//     }
// }

// class PaymentDetails {
//     PaymentMode mode;
//     Map<String, String>details;
//     void addDetails(PaymentMode mode, Map<String, String>details){
//         this.mode = mode;
//         this.details = details;
//     }
// }

// class PaymentGatewayMain {

// }

// public class PaymentGateway {
//     public static void main(String srgs[]) {
//         PaymentGatewayMain pg = new PaymentGatewayMain();

//         List<String> banks = new ArrayList<>();
//         Scanner sc = new Scanner(System.in);
//         int noOfBanks = sc.nextInt();
//         for(int i = 0; i<noOfBanks; i++){
//             String bank = sc.nextLine();
//             banks.add(bank);
//             Bank bakObj = new RandomBank();
//         }
//         Bank bank = new RandomBank();
//         // Client, Bank, PaymentDetails, BankRoute

//         sc.close();
//     }
// }