import dev.m4nd3l.sonj.SonJ;
import dev.m4nd3l.sonj.SonJBuilder;
import dev.m4nd3l.sonj.annotations.*;
import dev.m4nd3l.sonj.exceptions.CircularReferenceException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Dataset {
    public static void main(String[] args) throws IOException, CircularReferenceException {
        // 1. CREATE THE DATASET
        Bank globalBank = new Bank("International Alpha Bank");

        // Create 5 Branches
        for (int b = 1; b <= 5; b++) {
            Branch branch = new Branch("BR-" + b, "City " + b);

            // Create 10 Customers per branch
            for (int c = 1; c <= 10; c++) {
                Customer customer = new Customer(
                        c * b,
                        "Customer " + c,
                        "cust" + c + "@bank.com",
                        "HIGH_RISK_INTERNAL",
                        branch
                );

                // Create 2 Accounts per customer
                for (int a = 1; a <= 2; a++) {
                    Account account = new Account("IBAN-" + UUID.randomUUID().toString().substring(0, 8), "Savings", 1000.0 * a);

                    // Create 5 Transactions per account
                    for (int t = 1; t <= 5; t++) {
                        account.addTransaction(new Transaction("TXN-" + t, 50.0 * t, "Monthly fee " + t));
                    }
                    customer.addAccount(account);
                }
                branch.addCustomer(customer);
            }
            globalBank.registerBranch(branch);
        }

        // 2. CONFIGURE THE SERIALIZER
        // This configuration handles the circular references and respects your annotations
        SonJ sonj = new SonJBuilder()
                .setPrettyStyle()                               // Readable output
                .setEuropeanDateFormat()                        // dd-MM-yyyy
                .makeNullIfCircularReference()                  // CRITICAL: Prevents stack overflow on Branch <-> Customer
                .create();

        // 3. EXECUTE
        File jsonOutput = new File("");
        for (int i = 0; i < 10000; i++) {
            long startTime = System.currentTimeMillis();
            jsonOutput = sonj.serialize(globalBank, new File("C:\\Users\\m4nd3l\\Downloads\\bankybank" + i + ".json"));
            long endTime = System.currentTimeMillis();

            System.out.println("Serialization took: " + (endTime - startTime) + "ms");
        }
        System.out.println(Files.readString(jsonOutput.toPath()));
    }
}

class Transaction {
    @SonJName("transaction_id")
    private String id;

    private java.util.Date timestamp;
    private double amount;
    private String description;

    public Transaction(String id, double amount, String description) {
        this.id = id;
        this.timestamp = new java.util.Date();
        this.amount = amount;
        this.description = description;
    }
}

class Account {
    @SonJName("iban_code")
    private String accountNumber;
    private String type; // e.g., "Savings", "Checking"
    private double balance;
    private List<Transaction> transactions = new ArrayList<>();

    public Account(String accountNumber, String type, double balance) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.balance = balance;
    }

    public void addTransaction(Transaction t) {
        this.transactions.add(t);
    }
}

class Customer {
    @Expose
    private int id;

    @Expose
    @SonJName(value = "full_name", alternative = {"name", "user_name"})
    private String name;

    private String email;

    @Hide
    private String internalRiskScore; // Should be hidden by default

    private List<Account> accounts = new ArrayList<>();

    // CIRCULAR REFERENCE: Customer -> Branch -> Customers -> Customer
    private Branch homeBranch;

    public Customer(int id, String name, String email, String risk, Branch branch) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.internalRiskScore = risk;
        this.homeBranch = branch;
    }

    public void addAccount(Account a) {
        this.accounts.add(a);
    }
}

class Branch {
    private String branchCode;
    private String city;
    private List<Customer> customers = new ArrayList<>();

    public Branch(String branchCode, String city) {
        this.branchCode = branchCode;
        this.city = city;
    }

    public void addCustomer(Customer c) {
        this.customers.add(c);
    }

    public String getBranchCode() {
        return branchCode;
    }
}

class Bank {
    @Expose
    @SonJName("legal_entity_name")
    private String bankName;

    // Map of Branch Code -> Branch Object
    @Expose
    private Map<String, Branch> branchRegistry = new HashMap<>();

    public Bank(String bankName) {
        this.bankName = bankName;
    }

    public void registerBranch(Branch b) {
        branchRegistry.put(b.getBranchCode(), b);
    }
}