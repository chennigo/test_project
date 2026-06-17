package com.systelm.controller;

import com.systelm.dto.CreateCustomerCommand;
import com.systelm.dto.UpdateCustomerCommand;
import com.systelm.entity.Customer;
import com.systelm.entity.SalesOrder;
import com.systelm.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> listCustomers() {
        return customerService.listCustomers();
    }

    @PostMapping
    public Customer createCustomer(@RequestBody CreateCustomerCommand cmd) {
        return customerService.createCustomer(cmd);
    }

    @PutMapping("/{id}")
    public Customer updateCustomer(@PathVariable Long id, @RequestBody UpdateCustomerCommand cmd) {
        return customerService.updateCustomer(id, cmd);
    }

    @GetMapping("/{id}/orders")
    public List<SalesOrder> listCustomerOrders(@PathVariable Long id) {
        return customerService.listCustomerOrders(id);
    }
}
