package com.example.banking.controller;
/*
 * WebController.java
 * 处理Web请求，包括注册、登录、查看账户、d登出等。
 * 其中account路由跟主页面不一样
 * 仅显示账户信息，不进行操作
*/
import com.example.banking.model.Customer;
import com.example.banking.service.BankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private BankingService bankingService;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String username,
                           @RequestParam String password, Model model) {
        logger.info("Register attempt for username: {}", username);
        if (bankingService.getBanks().isEmpty()) {
            logger.debug("No banks found, creating default bank");
            bankingService.createBank("默认银行");
        }
        Customer customer = bankingService.createCustomer(bankingService.getBanks().get(0), name, username, password);
        if (customer == null) {
            logger.warn("Registration failed for username: {}. Username already exists.", username);
            model.addAttribute("error", "用户名已存在");
            return "register";
        }
        logger.info("Registration successful for username: {}", username);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        logger.debug("Showing login form");
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, Model model) {
        logger.info("Login attempt for username: {}", username);
        Customer customer = bankingService.authenticate(username, password);
        if (customer == null) {
            logger.warn("Authentication failed for username: {}. Invalid username or password.", username);
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, customer.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        session.setAttribute("customerId", customer.getId());
        if (customer.getRoles().contains("ROLE_ADMIN")) {
            return "redirect:/admin";
        }
        return "redirect:/";
    }

    /**
     * 处理/account页面请求，显示当前登录用户的账户信息。
     *
     * @param session HttpSession对象，用于获取当前登录用户ID
     * @param model   Spring MVC的Model对象，用于向前端传递数据
     * @return 返回account页面模板名，未登录则重定向到登录页
     */
    @GetMapping("/account")
    public String showAccount(HttpSession session, Model model) {
        logger.debug("Accessing account page");
        String customerId = (String) session.getAttribute("customerId");
        if (customerId == null) {
            logger.warn("No customerId in session, redirecting to login");
            return "redirect:/login";
        }
        Customer customer = bankingService.getCustomers().stream()
                .filter(c -> c.getId().equals(customerId)).findFirst().orElse(null);
        if (customer == null) {
            logger.warn("Customer not found for customerId: {}, clearing session", customerId);
            session.removeAttribute("customerId");
            return "redirect:/login";
        }
        logger.debug("Showing account for customer: {}", customer.getUsername());
        model.addAttribute("customer", customer);
        model.addAttribute("accounts", bankingService.getAccounts(customer.getUsername()));
        model.addAttribute("banks", bankingService.getBanks());
        return "account";
    }

    /**
     * 处理/logout请求，清除session和安全上下文，实现安全登出。
     *
     * @param session HttpSession对象
     * @return 重定向到登录页
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        logger.info("Logging out, clearing session and security context");
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}