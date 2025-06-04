package com.example.banking.model;

/**
 * Account
 * 
 * 账户模型类，关于银行账户的基本信息。
 * 包括账户ID、余额、利率、银行ID和客户ID。
 * 账户默认余额为0.0，利率为0.01。
 */
public class Account {
    private String id;
    private double balance;
    private double interestRate;
    private String bankId;
    private String customerId;

    /**
     * 默认构造函数。
     */
    public Account() {}

    /**
     * 带参数构造函数，初始化账户ID、客户ID和银行ID，余额为0，利率为0.01。
     * @param id 账户ID
     * @param customerId 客户ID
     * @param bankId 银行ID
     */
    public Account(String id, String customerId, String bankId) {
        this.id = id;
        this.customerId = customerId;
        this.bankId = bankId;
        this.balance = 0.0;
        this.interestRate = 0.01;
    }
    
    //getters and setters

    /**
     * 获取账户ID。
     * @return 账户ID
     */
    public String getId() { return id; }

    /**
     * 设置账户ID。
     * @param id 账户ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * 获取账户余额。
     * @return 余额
     */
    public double getBalance() { return balance; }

    /**
     * 设置账户余额。
     * @param balance 余额
     */
    public void setBalance(double balance) { this.balance = balance; }

    /**
     * 获取账户利率。
     * @return 利率
     */
    public double getInterestRate() { return interestRate; }

    /**
     * 设置账户利率。
     * @param interestRate 利率
     */
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    /**
     * 获取银行ID。
     * @return 银行ID
     */
    public String getBankId() { return bankId; }

    /**
     * 设置银行ID。
     * @param bankId 银行ID
     */
    public void setBankId(String bankId) { this.bankId = bankId; }

    /**
     * 获取客户ID。
     * @return 客户ID
     */
    public String getCustomerId() { return customerId; }

    /**
     * 设置客户ID。
     * @param customerId 客户ID
     */
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}
