package com.mipt.wallet.controller;

import com.mipt.wallet.controller.dto.OperationRequest;
import com.mipt.wallet.model.Operation;
import com.mipt.wallet.model.OperationType;
import com.mipt.wallet.model.Wallet;
import com.mipt.wallet.service.OperationDataService;
import com.mipt.wallet.service.WalletDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletDataService walletDataService;
    private final OperationDataService operationDataService;

    @GetMapping("/{userId}")
    @Transactional
    public ResponseEntity<Wallet> getWallet(@PathVariable UUID userId) {
        log.debug("GET /wallets/{}", userId);
        if (!walletDataService.existsById(userId)) {
            Wallet wallet = walletDataService.createWithUserId(userId);
            return ResponseEntity.ok(wallet);
        }
        return ResponseEntity.ok(walletDataService.findById(userId));
    }

    @PostMapping("/{userId}/topup")
    @Transactional
    public ResponseEntity<Wallet> topUp(@PathVariable UUID userId, @RequestParam long amount) {
        log.debug("POST /wallets/{}/topup amount={}", userId, amount);
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Сумма пополнения должна быть положительной");
        }
        if (!walletDataService.existsById(userId)) {
            walletDataService.createWithUserId(userId);
        }
        Wallet wallet = walletDataService.findById(userId);
        wallet.setAvailableTokens(wallet.getAvailableTokens() + amount);
        walletDataService.update(wallet);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{userId}/operations")
    public ResponseEntity<List<Operation>> getOperations(@PathVariable UUID userId) {
        log.debug("GET /wallets/{}/operations", userId);
        return ResponseEntity.ok(operationDataService.findByUserId(userId));
    }

    @PostMapping("/operations/reserve")
    @Transactional
    public ResponseEntity<Operation> reserve(@RequestBody OperationRequest req) {
        log.debug("POST /wallets/operations/reserve client={} performer={} amount={}", req.getClientId(), req.getPerformerId(), req.getAmount());
        validate(req);
        Wallet clientWallet = getWalletOrThrow(req.getClientId());
        if (clientWallet.getAvailableTokens() < req.getAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно токенов для резервирования");
        }
        clientWallet.setAvailableTokens(clientWallet.getAvailableTokens() - req.getAmount());
        clientWallet.setReservedTokens(clientWallet.getReservedTokens() + req.getAmount());
        walletDataService.update(clientWallet);
        Operation op = Operation.builder()
                .clientId(req.getClientId())
                .performerId(req.getPerformerId())
                .amount(req.getAmount())
                .type(OperationType.RESERVE)
                .title(req.getTitle())
                .build();
        return ResponseEntity.ok(operationDataService.create(op));
    }

    @PostMapping("/operations/pay")
    @Transactional
    public ResponseEntity<Operation> pay(@RequestBody OperationRequest req) {
        log.debug("POST /wallets/operations/pay client={} performer={} amount={}", req.getClientId(), req.getPerformerId(), req.getAmount());
        validate(req);
        Wallet clientWallet = getWalletOrThrow(req.getClientId());
        Wallet performerWallet = getWalletOrThrow(req.getPerformerId());
        if (clientWallet.getReservedTokens() < req.getAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно зарезервированных токенов");
        }
        clientWallet.setReservedTokens(clientWallet.getReservedTokens() - req.getAmount());
        performerWallet.setAvailableTokens(performerWallet.getAvailableTokens() + req.getAmount());
        walletDataService.update(clientWallet);
        walletDataService.update(performerWallet);
        Operation op = Operation.builder()
                .clientId(req.getClientId())
                .performerId(req.getPerformerId())
                .amount(req.getAmount())
                .type(OperationType.PAY)
                .title(req.getTitle())
                .build();
        return ResponseEntity.ok(operationDataService.create(op));
    }

    @PostMapping("/operations/cancel")
    @Transactional
    public ResponseEntity<Operation> cancel(@RequestBody OperationRequest req) {
        log.debug("POST /wallets/operations/cancel client={} performer={} amount={}", req.getClientId(), req.getPerformerId(), req.getAmount());
        validate(req);
        Wallet clientWallet = getWalletOrThrow(req.getClientId());
        if (clientWallet.getReservedTokens() < req.getAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно зарезервированных токенов для отмены");
        }
        clientWallet.setReservedTokens(clientWallet.getReservedTokens() - req.getAmount());
        clientWallet.setAvailableTokens(clientWallet.getAvailableTokens() + req.getAmount());
        walletDataService.update(clientWallet);
        Operation op = Operation.builder()
                .clientId(req.getClientId())
                .performerId(req.getPerformerId())
                .amount(req.getAmount())
                .type(OperationType.CANCEL)
                .title(req.getTitle())
                .build();
        return ResponseEntity.ok(operationDataService.create(op));
    }

    @PostMapping("/operations/refund")
    @Transactional
    public ResponseEntity<Operation> refund(@RequestBody OperationRequest req) {
        log.debug("POST /wallets/operations/refund client={} performer={} amount={}", req.getClientId(), req.getPerformerId(), req.getAmount());
        validate(req);
        Wallet performerWallet = getWalletOrThrow(req.getPerformerId());
        Wallet clientWallet = getWalletOrThrow(req.getClientId());
        if (performerWallet.getAvailableTokens() < req.getAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно токенов у продавца для возврата");
        }
        performerWallet.setAvailableTokens(performerWallet.getAvailableTokens() - req.getAmount());
        clientWallet.setAvailableTokens(clientWallet.getAvailableTokens() + req.getAmount());
        walletDataService.update(performerWallet);
        walletDataService.update(clientWallet);
        Operation op = Operation.builder()
                .clientId(req.getClientId())
                .performerId(req.getPerformerId())
                .amount(req.getAmount())
                .type(OperationType.REFUND)
                .title(req.getTitle())
                .build();
        return ResponseEntity.ok(operationDataService.create(op));
    }

    private Wallet getWalletOrThrow(UUID userId) {
        if (!walletDataService.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Кошелёк не найден для пользователя: " + userId);
        }
        return walletDataService.findById(userId);
    }

    private void validate(OperationRequest req) {
        if (req.getClientId() == null || req.getPerformerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId и performerId обязательны");
        }
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Сумма должна быть положительной");
        }
    }
}
