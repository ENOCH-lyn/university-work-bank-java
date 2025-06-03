package com.example.banking.controller;
/*
 * UserController.java
 * 处理用户相关操作，如转账、充值、提现、添加账户等。
*/
import com.example.banking.model.Customer;
import com.example.banking.service.BankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private BankingService bankingService;

    @PostMapping("/transfer")
    public String transfer(@RequestParam String fromAccountId, @RequestParam String toAccountId,
                           @RequestParam double amount, Model model) {
        logger.info("Transfer attempt from {} to {} amount {}", fromAccountId, toAccountId, amount);
        boolean success = bankingService.transfer(fromAccountId, toAccountId, amount);
        if (!success) {
            model.addAttribute("error", "转账失败，请检查账户ID或余额。");
            logger.warn("Transfer failed from {} to {} amount {}", fromAccountId, toAccountId, amount);
        }
        return "redirect:/";
    }

    /**
     * 处理用户充值请求。
     * @param accountId 充值账户ID
     * @param amount 充值金额
     * @return 重定向到主页
     */
    @PostMapping("/recharge")
    public String recharge(@RequestParam String accountId, @RequestParam double amount) {
        logger.info("Recharge attempt for account {} amount {}", accountId, amount);
        bankingService.recharge(accountId, amount);
        return "redirect:/";
    }

    /**
     * 处理添加新账户请求。
     * @param bankId 银行ID
     * @param model Spring MVC的Model对象
     * @return 重定向到主页或登录页
     */
    @PostMapping("/add-account")
    public String addAccount(@RequestParam String bankId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("User not authenticated, cannot add account.");
            return "redirect:/login";
        }
        String username = authentication.getName();
        Customer customer = bankingService.getCustomer(username);

        if (customer == null) {
            logger.error("Authenticated user {} not found in banking service.", username);
            model.addAttribute("error", "无法找到当前用户信息，请重新登录。");
            return "redirect:/login";
        }
        logger.info("Adding new account for customer {} (ID: {}) in bank {}", customer.getUsername(), customer.getId(), bankId);
        bankingService.createAccount(customer, bankId);
        return "redirect:/";
    }

    /**
     * 处理取款请求。
     * @param accountId 取款账户ID
     * @param amount 取款金额
     * @param redirectAttributes 重定向属性
     * @param session HttpSession对象
     * @return 重定向到主页
     */
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String accountId, @RequestParam double amount, RedirectAttributes redirectAttributes, HttpSession session) {
        if (amount <= 0) {
            redirectAttributes.addFlashAttribute("error", "取款金额必须大于0。");
            return "redirect:/";
        }
        boolean success = bankingService.withdraw(accountId, amount);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "取款成功！");
        } else {
            redirectAttributes.addFlashAttribute("error", "取款失败，余额不足或账户不存在。");
        }
        return "redirect:/";
    }
}