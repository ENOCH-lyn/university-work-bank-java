package com.example.banking.controller;

/**
 * HomeController
 * 
 * 监听根路由 "/"，检测用户是否已登录。
 * 如果未登录，则重定向到登录页面。
 * 如果已登录，则返回 home.html 并传递账户、银行等信息。
 */
import com.example.banking.model.Account;
import com.example.banking.model.Bank;
import com.example.banking.service.BankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    @Autowired
    private BankingService bankingService;

    /**
     * 处理根路径请求，判断用户是否登录，并加载主页所需数据。
     * 
     * @param model Spring MVC的Model对象
     * @param auth  Spring Security的认证对象
     * @return 已登录返回home页面，未登录重定向到登录页
     */
    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        String username = auth.getName();
        model.addAttribute("banks", bankingService.getBanks());
        List<Account> userAccounts = bankingService.getAccounts(username);
        model.addAttribute("accounts", userAccounts);
        model.addAttribute("customer", bankingService.getCustomer(username));
        Map<String, String> bankNames = bankingService.getBanks().stream()
                .collect(Collectors.toMap(Bank::getId, Bank::getName));
        model.addAttribute("bankNames", bankNames);
        return "home";
    }
}