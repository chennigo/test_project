package com.systelm.controller;

import com.systelm.dto.TransferCommand;
import com.systelm.entity.Transfer;
import com.systelm.service.CurrentUserService;
import com.systelm.service.TransferService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;
    private final CurrentUserService currentUserService;

    public TransferController(TransferService transferService, CurrentUserService currentUserService) {
        this.transferService = transferService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public Transfer create(@RequestBody TransferCommand cmd) {
        return transferService.confirm(currentUserService.requireCurrentUser(), cmd);
    }
}
