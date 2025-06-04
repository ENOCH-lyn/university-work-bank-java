package com.example.banking.controller;
/*
 * AdminController.java
 * 监听路由 /admin
 * 处理管理员相关操作，如创建银行、编辑客户信息、编辑账户信息等。
 * * 需要管理员角色才能访问。
 * * 主要功能包括：
 * - 显示所有银行和客户信息
 * - 创建新银行
 * - 编辑客户名称
 * - 重置客户密码
 * - 编辑账户余额
 * - 编辑账户利率
 * * 通过 @PreAuthorize 注解限制访问权限，确保只有具有 ADMIN 角色的用户可以访问这些功能。
 * * @ENOCH
*/
import com.example.banking.model.Customer;
import com.example.banking.service.BankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private BankingService bankingService;

    /**
     * 显示管理员面板，包含所有银行、客户和账户信息。
     * 可选参数 selectedCustomerId 用于高亮显示某个客户及其账户。
     *
     * @param model Spring MVC的Model对象
     * @param selectedCustomerId 选中的客户ID（可选）
     * @return 返回admin页面
     */
    @GetMapping
    public String adminPanel(Model model, @RequestParam(required = false) String selectedCustomerId) {
        model.addAttribute("banks", bankingService.getBanks());
        List<Customer> customers = bankingService.getCustomers();
        model.addAttribute("customers", customers);
        model.addAttribute("allAccounts", bankingService.getAccounts());

        if (selectedCustomerId != null) {
            Customer selectedCustomer = bankingService.getCustomerById(selectedCustomerId);
            model.addAttribute("selectedCustomer", selectedCustomer);
            if (selectedCustomer != null) {
                model.addAttribute("customerAccounts", bankingService.getAccounts(selectedCustomer.getUsername()));
            }
        }
        return "admin";
    }

    /**
     * 管理员创建新银行。
     * @param bankName 银行名称
     * @param redirectAttributes 重定向属性
     * @return 重定向到admin页面
     */
    @PostMapping("/create-bank")
    public String createBank(@RequestParam String bankName, RedirectAttributes redirectAttributes) {
        if (bankName == null || bankName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "银行名称不能为空。");
            return "redirect:/admin";
        }
        bankingService.createBank(bankName);
        logger.info("Admin created new bank: {}", bankName);
        redirectAttributes.addFlashAttribute("message", "银行 '" + bankName + "' 创建成功。");
        return "redirect:/admin";
    }

    /**
     * 管理员修改客户姓名。
     * @param customerId 客户ID
     * @param newName 新姓名
     * @param redirectAttributes 重定向属性
     * @return 重定向到admin页面并高亮该客户
     */
    @PostMapping("/customer/edit-name")
    public String editCustomerName(@RequestParam String customerId, @RequestParam String newName, RedirectAttributes redirectAttributes) {
        boolean success = bankingService.updateCustomerName(customerId, newName);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "客户名称更新成功。");
        } else {
            redirectAttributes.addFlashAttribute("error", "客户名称更新失败。");
        }
        return "redirect:/admin?selectedCustomerId=" + customerId;
    }

    /**
     * 管理员重置客户密码。
     * @param customerId 客户ID
     * @param newPassword 新密码
     * @param redirectAttributes 重定向属性
     * @return 重定向到admin页面并高亮该客户
     */
    @PostMapping("/customer/reset-password")
    public String resetCustomerPassword(@RequestParam String customerId, @RequestParam String newPassword, RedirectAttributes redirectAttributes) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "新密码不能为空。");
            return "redirect:/admin?selectedCustomerId=" + customerId;
        }
        boolean success = bankingService.resetCustomerPassword(customerId, newPassword);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "客户密码重置成功。");
        } else {
            redirectAttributes.addFlashAttribute("error", "客户密码重置失败。");
        }
        return "redirect:/admin?selectedCustomerId=" + customerId;
    }

    /**
     * 管理员修改账户余额。
     * @param accountId 账户ID
     * @param newBalance 新余额
     * @param customerId 客户ID（用于重定向高亮）
     * @param redirectAttributes 重定向属性
     * @return 重定向到admin页面并高亮该客户
     */
    @PostMapping("/account/edit-balance")
    public String editAccountBalance(@RequestParam String accountId, @RequestParam double newBalance, @RequestParam String customerId, RedirectAttributes redirectAttributes) {
        boolean success = bankingService.updateAccountBalance(accountId, newBalance);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "账户余额更新成功。");
        } else {
            redirectAttributes.addFlashAttribute("error", "账户余额更新失败。请确保余额非负。");
        }
        return "redirect:/admin?selectedCustomerId=" + customerId;
    }

    /**
     * 管理员修改账户利率。
     * @param accountId 账户ID
     * @param newInterestRate 新利率（百分比形式，需除以100存储）
     * @param customerId 客户ID（用于重定向高亮）
     * @param redirectAttributes 重定向属性
     * @return 重定向到admin页面并高亮该客户
     */
    @PostMapping("/account/edit-interest-rate")
    public String editAccountInterestRate(@RequestParam String accountId, @RequestParam double newInterestRate, @RequestParam String customerId, RedirectAttributes redirectAttributes) {
        boolean success = bankingService.updateAccountInterestRate(accountId, newInterestRate / 100.0);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "账户利率更新成功。");
        } else {
            redirectAttributes.addFlashAttribute("error", "账户利率更新失败。请确保利率非负。");
        }
        return "redirect:/admin?selectedCustomerId=" + customerId;
    }
}