package com.mipt.advertisement.controller;

import com.mipt.advertisement.controller.dto.AuctionBidRequest;
import com.mipt.advertisement.controller.dto.AuctionBidResponse;
import com.mipt.advertisement.service.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/advertisements/{id}/auction")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping("/bids")
    public ResponseEntity<List<AuctionBidResponse>> getBids(@PathVariable UUID id) {
        log.info("GET /v1/advertisements/{}/auction/bids", id);
        return ResponseEntity.ok(auctionService.getBids(id));
    }

    @PostMapping("/bids")
    public ResponseEntity<AuctionBidResponse> placeBid(
            @PathVariable UUID id,
            @Valid @RequestBody AuctionBidRequest request) {
        log.info("POST /v1/advertisements/{}/auction/bids bidder={} amount={}", id, request.getBidderId(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(auctionService.placeBid(id, request));
    }

    @PostMapping("/close")
    public ResponseEntity<Void> closeAuction(
            @PathVariable UUID id,
            @RequestParam UUID requesterId) {
        log.info("POST /v1/advertisements/{}/auction/close requester={}", id, requesterId);
        auctionService.closeAuction(id, requesterId);
        return ResponseEntity.noContent().build();
    }
}
