package com.systelm.service;

import com.systelm.dto.CreateCustomerCommand;
import com.systelm.dto.UpdateCustomerCommand;
import com.systelm.entity.Customer;
import com.systelm.entity.SalesOrder;
import com.systelm.repository.CustomerRepository;
import com.systelm.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CustomerService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;

    public CustomerService(CustomerRepository customerRepository, SalesOrderRepository salesOrderRepository) {
        this.customerRepository = customerRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("客户不存在"));
    }

    @Transactional
    public Customer createCustomer(CreateCustomerCommand cmd) {
        Customer customer = new Customer();
        customer.setName(cmd.name());
        customer.setContact(cmd.contact());
        customer.setPhone(cmd.phone());
        customer.setAddress(cmd.address());
        customer.setCreatedAt(LocalDateTime.now().format(DATE_TIME_FORMAT));
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(Long id, UpdateCustomerCommand cmd) {
        Customer customer = getCustomer(id);
        if (cmd.name() != null) {
            customer.setName(cmd.name());
        }
        if (cmd.contact() != null) {
            customer.setContact(cmd.contact());
        }
        if (cmd.phone() != null) {
            customer.setPhone(cmd.phone());
        }
        if (cmd.address() != null) {
            customer.setAddress(cmd.address());
        }
        return customerRepository.save(customer);
    }

    public List<SalesOrder> listCustomerOrders(Long customerId) {
        getCustomer(customerId);
        return salesOrderRepository.findByCustomerIdOrderByIdDesc(customerId);
    }
}
