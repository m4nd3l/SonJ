import dev.m4nd3l.sonj.SonJ;
import dev.m4nd3l.sonj.SonJBuilder;
import dev.m4nd3l.sonj.exceptions.CircularReferenceException;

public class Main {
    public static void main(String[] args) throws CircularReferenceException {
        Bank myBank = generateBigDataset();

        // Configure SonJ based on your provided Builder logic
        SonJ sonj = new SonJBuilder()
                .setPrettyStyle()
                .makeNullIfCircularReference()
                .serializeNullFields() // This ensures fields appear even if they are null
                .create();

        // Serialize the massive object
        String jsonOutput = sonj.serialize(myBank);
        System.out.println(jsonOutput);
    }

    public static Bank generateBigDataset() {
        Bank bank = new Bank();
        bank.exchangeRates.put("EUR", 0.92);
        bank.exchangeRates.put("GBP", 0.79);

        // Branch 1
        Branch mainBranch = new Branch();
        mainBranch.branchId = "BR-001";
        mainBranch.location = new Address();
        mainBranch.location.street = "123 Wall St";
        mainBranch.location.city = "New York";
        mainBranch.location.zipCode = "10005";

        // Create Manager (Circular Reference)
        Employee manager = new Employee();
        manager.id = 1;
        manager.name = "Alice Smith";
        manager.salary = 150000.00;
        manager.managedBranch = mainBranch; // Loop back to branch
        mainBranch.manager = manager;

        // Add Departments
        Department it = new Department();
        it.name = "IT";
        it.floor = 4;
        mainBranch.departments.add(it);

        // Add a Client with complex data
        Client client = new Client();
        client.contact = new ContactInfo();
        client.contact.email = "john.doe@example.com";
        client.contact.socialMedia.put("linkedin", "linkedin.com/in/johndoe");

        // Accounts
        SavingsAccount sa = new SavingsAccount();
        sa.accountNumber = "SA-9988";
        sa.balance = 12500.50;

        Transaction t1 = new Transaction();
        t1.id = 5001L;
        t1.amount = 200.0;
        t1.description = "ATM Deposit";
        sa.transactionHistory.add(t1);

        CheckingAccount ca = new CheckingAccount();
        ca.accountNumber = "CA-1122";
        ca.balance = 450.0;

        CreditCard cc = new CreditCard();
        cc.holderName = "John Doe";
        cc.creditLimit = 5000.0;
        ca.linkedCard = cc;

        client.accounts.add(sa);
        client.accounts.add(ca);

        // Security
        client.security = new SecuritySettings();
        client.security.multiFactorEnabled = true;
        AuditRecord ar = new AuditRecord();
        ar.action = "LOGIN";
        ar.success = true;
        client.security.loginHistory.add(ar);

        mainBranch.regularClients.add(client);
        bank.branches.add(mainBranch);

        return bank;
    }
}