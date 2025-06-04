package com.example.banking.model;

/**
 * Customer
 *
 * 客户模型类，包含客户的基本信息，如ID、姓名、用户名、密码哈希、银行卡号、所属银行ID列表和角色列表。
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class Customer {
    private String id;
    private String name;
    private String username;
    private String passwordHash;
    private String cardNumber;
    private List<String> bankIds = new ArrayList<>();
    private List<String> roles = new ArrayList<>();

    /**
     * 默认构造函数，初始化角色为ROLE_USER。
     */
    public Customer() {
        this.roles.add("ROLE_USER");
    }

    /**
     * 带参数构造函数，初始化客户基本信息。
     * @param id 客户ID
     * @param name 客户姓名
     * @param username 用户名
     * @param passwordHash 密码哈希
     * @param cardNumber 银行卡号
     */
    public Customer(String id, String name, String username, String passwordHash, String cardNumber) {
        this();// 初始化角色为ROLE_USER
        this.id = id;
        this.name = name;
        this.username = username;
        this.passwordHash = passwordHash;
        this.cardNumber = cardNumber;
    }

    // Getters and Setters

    /**
     * 获取客户ID。
     * @return 客户ID
     */
    public String getId() { return id; }

    /**
     * 设置客户ID。
     * @param id 客户ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * 获取客户姓名。
     * @return 客户姓名
     */
    public String getName() { return name; }

    /**
     * 设置客户姓名。
     * @param name 客户姓名
     */
    public void setName(String name) { this.name = name; }

    /**
     * 获取用户名。
     * @return 用户名
     */
    public String getUsername() { return username; }

    /**
     * 设置用户名。
     * @param username 用户名
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * 获取密码哈希。
     * @return 密码哈希
     */
    public String getPasswordHash() { return passwordHash; }

    /**
     * 设置密码哈希。
     * @param passwordHash 密码哈希
     */
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /**
     * 获取银行卡号。
     * @return 银行卡号
     */
    public String getCardNumber() { return cardNumber; }

    /**
     * 设置银行卡号。
     * @param cardNumber 银行卡号
     */
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    /**
     * 获取所属银行ID列表。
     * @return 银行ID列表
     */
    public List<String> getBankIds() { return bankIds; }

    /**
     * 设置所属银行ID列表。
     * @param bankIds 银行ID列表
     */
    public void setBankIds(List<String> bankIds) { this.bankIds = bankIds; }

    /**
     * 获取角色列表。
     * @return 角色列表
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * 设置角色列表。
     * @param roles 角色列表
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * 获取Spring Security权限集合。
     * @return 权限集合
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles == null || this.roles.isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER")); 
        }
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}