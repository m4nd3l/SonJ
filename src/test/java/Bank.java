import dev.m4nd3l.sonj.annotations.*;
import dev.m4nd3l.sonj.SonJ;
import dev.m4nd3l.sonj.SonJBuilder;

import java.util.*;

// ==========================================
// 1. Core Bank Structure
// ==========================================

class Bank {
    @SonJName("bank_legal_name")
    public String name = "Global Finance Corp";

    @Expose
    public List<Branch> branches = new ArrayList<>();

    @Hide
    private String internalVaultCode = "X-99-SECRET";

    public Map<String, Double> exchangeRates = new HashMap<>();
}

class Branch {
    public String branchId;
    public Address location;

    // Circular Reference: Branch -> Manager -> Branch
    @Expose
    public Employee manager;

    public List<Department> departments = new ArrayList<>();
    public List<Client> regularClients = new ArrayList<>();
}

class Address {
    public String street;
    public String city;
    @SonJName(value = "zip", alternative = {"postal_code", "zip_code"})
    public String zipCode;
    public double[] coordinates = {41.8781, -87.6298};
}

// ==========================================
// 2. Staff & Hierarchy
// ==========================================

class Employee {
    public int id;
    public String name;
    public String position;

    @Hide(serialize = false) // Hide salary from JSON output
    public double salary;

    // Circular link back to the branch they manage
    @Expose
    public Branch managedBranch;
}

class Department {
    public String name;
    public int floor;
    public List<String> permissions = Arrays.asList("READ", "WRITE", "EXECUTE");
}

// ==========================================
// 3. Client & Security
// ==========================================

class Client {
    @SonJName("client_uuid")
    public UUID id = UUID.randomUUID();

    public ContactInfo contact;
    public List<Account> accounts = new ArrayList<>();

    @Hide
    public String plainTextPassword = "password123";

    public SecuritySettings security;
}

class ContactInfo {
    public String email;
    public String phone;
    @Expose
    public Map<String, String> socialMedia = new HashMap<>();
}

class SecuritySettings {
    public boolean multiFactorEnabled;
    public String lastLoginIp;
    public List<AuditRecord> loginHistory = new ArrayList<>();
}

class AuditRecord {
    public Date timestamp = new Date();
    public String action;
    public boolean success;
}

// ==========================================
// 4. Accounts & Products (Inheritance simulation)
// ==========================================

abstract class Account {
    public String accountNumber;
    public double balance;
    public String currency = "USD";
    public List<Transaction> transactionHistory = new ArrayList<>();
}

class SavingsAccount extends Account {
    public double interestRate = 0.05;
    @Expose
    public boolean allowsWithdrawal = true;
}

class CheckingAccount extends Account {
    public double overdraftLimit = 500.0;
    public Card linkedCard;
}

// ==========================================
// 5. Transactions & Cards
// ==========================================

class Transaction {
    @SonJName("tx_id")
    public long id;
    public double amount;
    public String description;
    public Date date = new Date();
}

class Card {
    @SonJName("card_number_masked")
    public String cardNumber = "****-****-****-1234";
    public String holderName;
    @Hide
    public int cvv = 999;
    public Date expiryDate = new Date();
}

class CreditCard extends Card {
    public double creditLimit;
    public double currentDebt;
}

class DebitCard extends Card {
    public String linkedCheckingAccountId;
}

// ==========================================
// 6. Investments & Assets
// ==========================================

class InvestmentPortfolio {
    public List<Asset> assets = new ArrayList<>();
    public double totalValue;
    public Map<String, Double> sectorAllocation = new HashMap<>();
}

class Asset {
    public String ticker;
    public int quantity;
    public double purchasePrice;
    @Expose
    public AssetType type = AssetType.STOCK;
}

enum AssetType { STOCK, BOND, CRYPTO, COMMODITY }

class Loan {
    public double principal;
    public double remainingBalance;
    public int termMonths;
    public InsurancePolicy insurance;
}

class InsurancePolicy {
    public String policyNumber;
    public double coverageAmount;
    public String provider = "SafeGuard Inc.";
}