package com.example.banking.model;

/**
 * Bank
 * 
 * 银行模型类，包含银行的基本信息（银行ID和银行名称）。
 */
public class Bank {
    private String id;
    private String name;

    /**
     * 默认构造函数。
     */
    public Bank() {}

    /**
     * 带参数构造函数，初始化银行ID和名称。
     * @param id 银行ID
     * @param name 银行名称
     */
    public Bank(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters

    /**
     * 获取银行ID。
     * @return 银行ID
     */
    public String getId() { return id; }

    /**
     * 设置银行ID。
     * @param id 银行ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * 获取银行名称。
     * @return 银行名称
     */
    public String getName() { return name; }

    /**
     * 设置银行名称。
     * @param name 银行名称
     */
    public void setName(String name) { this.name = name; }
}
