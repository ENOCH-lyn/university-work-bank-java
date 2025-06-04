package com.example.banking.service;
/**
 * BankingService.java
 * 
 * 该类负责处理银行业务逻辑，包括银行、客户和账户的管理。
 * 主要功能包括：
 * <ul>
 *   <li>初始化数据</li>
 *   <li>加载和保存数据到文件</li>
 *   <li>创建银行、客户和账户</li>
 *   <li>转账、充值、提现</li>
 *   <li>账户利率和余额管理</li>
 * </ul>
 */
import com.example.banking.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BankingService {
    private static final Logger logger = LoggerFactory.getLogger(BankingService.class);
    private List<Bank> banks = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<Account> accounts = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private int customerIdCounter = 1;
    private int accountNumberCounter = 1000;

    /**
     * 初始化服务，加载数据并创建默认银行和管理员（若无）
     */
    @PostConstruct
    public void init() {
        loadData();
        if (banks.isEmpty()) {
            logger.info("No banks found, creating default bank.");
            banks.add(new Bank(UUID.randomUUID().toString(), "defaultBank"));
        }

        boolean adminExists = customers.stream()
                                     .anyMatch(c -> "admin".equals(c.getUsername()));
        if (!adminExists) {
            logger.info("Admin user 'admin' not found, creating default admin user.");
            String adminId = "C" + String.format("%03d", customerIdCounter++);
            String adminPasswordHash = BCrypt.hashpw("admin", BCrypt.gensalt());
            Customer adminUser = new Customer(adminId, "Administrator", "admin", adminPasswordHash, UUID.randomUUID().toString().substring(0, 8));
            adminUser.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));

            if (!banks.isEmpty()) {
                adminUser.getBankIds().add(banks.get(0).getId());
            } else {
                Bank adminBank = new Bank(UUID.randomUUID().toString(), "AdminDefaultBank");
                banks.add(adminBank);
                adminUser.getBankIds().add(adminBank.getId());
                logger.info("Created AdminDefaultBank for admin user.");
            }
            customers.add(adminUser);
            logger.info("Default admin user 'admin' created with password 'admin'.");
        }
        saveData();
    }

    /**
     * 从 data.json 文件加载银行、客户和账户数据。
     */
    private void loadData() {
        try {
            File file = new File("data.json");
            if (file.exists() && file.length() > 0) {
                Map<String, Object> data = objectMapper.readValue(file, Map.class);
                banks = ((List<Map<String, String>>) data.getOrDefault("banks", new ArrayList<>())).stream()
                        .map(m -> new Bank(m.get("id"), m.get("name")))
                        .collect(Collectors.toList());
                customers = ((List<Map<String, Object>>) data.getOrDefault("customers", new ArrayList<>())).stream()
                        .map(m -> {
                            Customer c = new Customer();
                            c.setId((String) m.get("id"));
                            c.setName((String) m.get("name"));
                            c.setUsername((String) m.get("username"));
                            c.setPasswordHash((String) m.get("passwordHash"));
                            c.setCardNumber((String) m.get("cardNumber"));
                            c.setBankIds((List<String>) m.getOrDefault("bankIds", new ArrayList<>()));
                            List<String> roles = (List<String>) m.get("roles");
                            if (roles == null || roles.isEmpty()) {
                                c.setRoles(new ArrayList<>(List.of("ROLE_USER")));
                            } else {
                                c.setRoles(new ArrayList<>(roles));
                            }
                            return c;
                        }).collect(Collectors.toList());
                accounts = ((List<Map<String, Object>>) data.getOrDefault("accounts", new ArrayList<>())).stream()
                        .map(m -> {
                            Account a = new Account();
                            a.setId((String) m.get("id"));
                            a.setBalance((Double) m.getOrDefault("balance", 0.0));
                            a.setInterestRate((Double) m.getOrDefault("interestRate", 0.01));
                            a.setBankId((String) m.get("bankId"));
                            a.setCustomerId((String) m.get("customerId"));
                            return a;
                        }).collect(Collectors.toList());
                updateCounters();
            } else {
                initializeEmptyData();
            }
        } catch (IOException e) {
            logger.error("Error loading data from data.json", e);
            initializeEmptyData();
        }
    }
    
    /**
     * 根据现有数据更新客户和账户的自增ID计数器。
     */
    private void updateCounters() {
        for (Customer customer : customers) {
            try {
                int idNum = Integer.parseInt(customer.getId().substring(1));
                if (idNum >= customerIdCounter) customerIdCounter = idNum + 1;
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                logger.warn("Could not parse customer ID for counter: {}", customer.getId());
            }
        }
        for (Account account : accounts) {
             try {
                int accNum = Integer.parseInt(account.getId());
                if (accNum >= accountNumberCounter) accountNumberCounter = accNum + 1;
            } catch (NumberFormatException e) {
                logger.warn("Could not parse account ID for counter: {}", account.getId());
            }
        }
    }

    /**
     * 初始化空的数据存储。
     */
    private void initializeEmptyData() {
        logger.info("Initializing with empty data store.");
        banks = new ArrayList<>();
        customers = new ArrayList<>();
        accounts = new ArrayList<>();

    }


    /**
     * 将当前银行、客户和账户数据保存到 data.json 文件。
     */
    public void saveData() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("banks", banks);
            data.put("customers", customers);
            data.put("accounts", accounts);
            objectMapper.writeValue(new File("data.json"), data);
            logger.debug("Data saved successfully to data.json");
        } catch (IOException e) {
            logger.error("Error saving data to data.json", e);
        }
    }

    /**
     * 创建一个新银行。
     * @param name 银行名称
     */
    public void createBank(String name) {
        banks.add(new Bank(UUID.randomUUID().toString(), name));
        saveData();
    }

    /**
     * 创建一个新客户，并为其在指定银行创建账户。
     * @param bank 银行对象
     * @param name 客户姓名
     * @param username 用户名
     * @param password 密码
     * @return 创建成功返回 Customer 对象，用户名已存在返回 null
     */
    public Customer createCustomer(Bank bank, String name, String username, String password) {
        if (isUsernameTaken(username)) {
            logger.warn("Username {} is already taken.", username);
            return null;
        }
        String id = "C" + String.format("%03d", customerIdCounter++);
        String cardNumber = UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        Customer customer = new Customer(id, name, username, passwordHash, cardNumber);
        customer.getBankIds().add(bank.getId());
        customers.add(customer);
        createAccount(customer, bank.getId());
        saveData();
        logger.info("Customer {} created successfully with username {}.", name, username);
        return customer;
    }

    /**
     * 为指定客户在指定银行创建新账户。
     * @param customer 客户对象
     * @param bankId 银行ID
     */
    public void createAccount(Customer customer, String bankId) {
        String accountId = String.valueOf(accountNumberCounter++);
        Account account = new Account(accountId, customer.getId(), bankId);
        accounts.add(account);
        saveData();
    }

    /**
     * 转账操作。
     * @param fromAccountId 转出账户ID
     * @param toAccountId 转入账户ID
     * @param amount 转账金额
     * @return 转账成功返回 true，否则返回 false
     */
    public boolean transfer(String fromAccountId, String toAccountId, double amount) {
        Account from = accounts.stream().filter(a -> a.getId().equals(fromAccountId)).findFirst().orElse(null);
        Account to = accounts.stream().filter(a -> a.getId().equals(toAccountId)).findFirst().orElse(null);
        if (from != null && to != null && from.getBalance() >= amount && amount > 0) {
            from.setBalance(from.getBalance() - amount);
            to.setBalance(to.getBalance() + amount);
            saveData();
            logger.info("Transfer successful: {} from account {} to account {}", amount, fromAccountId, toAccountId);
            return true;
        }
        logger.warn("Transfer failed: amount {}, from account {}, to account {}. Conditions not met or accounts not found.", amount, fromAccountId, toAccountId);
        return false;
    }

    /**
     * 充值操作。
     * @param accountId 账户ID
     * @param amount 充值金额
     * @return 充值成功返回 true，否则返回 false
     */
    public boolean recharge(String accountId, double amount) {
        Optional<Account> accountOpt = accounts.stream().filter(a -> a.getId().equals(accountId)).findFirst();
        if (accountOpt.isPresent() && amount > 0) {
            Account account = accountOpt.get();
            account.setBalance(account.getBalance() + amount);
            saveData();
            logger.info("Recharge successful for account {}: amount {}", accountId, amount);
            return true;
        }
        logger.warn("Recharge failed for account {}: amount {}. Account not found or amount not positive.", accountId, amount);
        return false;
    }

    /**
     * 取款操作。
     * @param accountId 账户ID
     * @param amount 取款金额
     * @return 取款成功返回 true，否则返回 false
     */
    public boolean withdraw(String accountId, double amount) {
        Account account = getAccountById(accountId);
        if (account != null && amount > 0 && account.getBalance() >= amount) {
            account.setBalance(account.getBalance() - amount);
            saveData();
            logger.info("Withdraw successful for account {}: amount {}", accountId, amount);
            return true;
        }
        logger.warn("Withdraw failed for account {}: amount {}. Account not found or insufficient balance.", accountId, amount);
        return false;
    }

    /**
     * 检查用户名是否已被占用。
     * @param username 用户名
     * @return 已存在返回 true，否则返回 false
     */
    public boolean isUsernameTaken(String username) {
        return customers.stream().anyMatch(c -> c.getUsername().equals(username));
    }

    /**
     * 用户认证。
     * @param username 用户名
     * @param password 密码
     * @return 认证成功返回 Customer 对象，否则返回 null
     */
    public Customer authenticate(String username, String password) {
        return customers.stream()
                .filter(c -> c.getUsername().equals(username) && BCrypt.checkpw(password, c.getPasswordHash()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有银行列表。
     * @return 银行列表
     */
    public List<Bank> getBanks() { return banks; }

    /**
     * 获取所有客户列表。
     * @return 客户列表
     */
    public List<Customer> getCustomers() { return customers; }

    /**
     * 获取所有账户列表。
     * @return 账户列表
     */
    public List<Account> getAccounts() { return accounts; }

    /**
     * 根据用户名获取该用户所有账户。
     * @param username 用户名
     * @return 账户列表
     */
    public List<Account> getAccounts(String username) {
        String customerId = customers.stream()
                .filter(c -> c.getUsername().equals(username))
                .findFirst().map(Customer::getId).orElse(null);
        if (customerId == null) return Collections.emptyList();
        return accounts.stream()
                .filter(a -> customerId.equals(a.getCustomerId()))
                .collect(Collectors.toList());
    }

    /**
     * 根据用户名获取客户对象。
     * @param username 用户名
     * @return Customer 对象
     */
    public Customer getCustomer(String username) {
        return customers.stream().filter(c -> c.getUsername().equals(username)).findFirst().orElse(null);
    }

    /**
     * 根据客户ID获取客户对象。
     * @param customerId 客户ID
     * @return Customer 对象
     */
    public Customer getCustomerById(String customerId) {
        return customers.stream().filter(c -> c.getId().equals(customerId)).findFirst().orElse(null);
    }

    /**
     * 根据账户ID获取账户对象。
     * @param accountId 账户ID
     * @return Account 对象
     */
    public Account getAccountById(String accountId) {
        return accounts.stream().filter(a -> a.getId().equals(accountId)).findFirst().orElse(null);
    }

    /**
     * 根据银行ID获取银行名称。
     * @param bankId 银行ID
     * @return 银行名称
     */
    public String getBankNameById(String bankId) {
        return banks.stream()
                .filter(b -> b.getId().equals(bankId))
                .findFirst()
                .map(Bank::getName)
                .orElse("Unknown Bank");
    }

    /**
     * 更新指定客户ID的客户姓名。
     * @param customerId 客户ID
     * @param newName 新姓名
     * @return 更新成功返回 true，否则返回 false
     *
     * 如果客户存在，则更新其姓名并保存数据。
     * 如果客户不存在，则返回 false 并记录警告日志。
     */
    public boolean updateCustomerName(String customerId, String newName) {
        Customer customer = getCustomerById(customerId);
        if (customer != null) {
            customer.setName(newName);
            saveData();
            logger.info("Updated name for customer {} to {}", customerId, newName);
            return true;
        }
        logger.warn("Failed to update name. Customer {} not found.", customerId);
        return false;
    }

    /**
     * 重置指定客户ID的密码。
     * @param customerId 客户ID
     * @param newPassword 新密码
     * @return 重置成功返回 true，否则返回 false
     */
    public boolean resetCustomerPassword(String customerId, String newPassword) {
        Customer customer = getCustomerById(customerId);
        if (customer != null) {
            customer.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            saveData();
            logger.info("Reset password for customer {}", customerId);
            return true;
        }
        logger.warn("Failed to reset password. Customer {} not found.", customerId);
        return false;
    }

    /**
     * 更新指定账户ID的余额。
     * @param accountId 账户ID
     * @param newBalance 新余额
     * @return 更新成功返回 true，否则返回 false
     */
    public boolean updateAccountBalance(String accountId, double newBalance) {

        Account account = getAccountById(accountId);
        if (account != null) {
            if (newBalance < 0) {
                logger.warn("Cannot set negative balance for account {}", accountId);
                return false;
            }
            account.setBalance(newBalance);
            saveData();
            logger.info("Updated balance for account {} to {}", accountId, newBalance);
            return true;
        }
        logger.warn("Failed to update balance. Account {} not found.", accountId);
        return false;
    }

    /**
     * 更新指定账户ID的利率。
     * @param accountId 账户ID
     * @param newInterestRate 新利率
     * @return 更新成功返回 true，否则返回 false
     */
    public boolean updateAccountInterestRate(String accountId, double newInterestRate) {

        Account account = getAccountById(accountId);
        if (account != null) {
            if (newInterestRate < 0) {
                logger.warn("Cannot set negative interest rate for account {}", accountId);
                return false;
            }
            account.setInterestRate(newInterestRate);
            saveData();
            logger.info("Updated interest rate for account {} to {}", accountId, newInterestRate);
            return true;
        }
        logger.warn("Failed to update interest rate. Account {} not found.", accountId);
        return false;
    }
}