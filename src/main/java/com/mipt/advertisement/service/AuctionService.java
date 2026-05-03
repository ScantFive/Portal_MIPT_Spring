package com.mipt.advertisement.service;

import com.mipt.advertisement.controller.dto.AuctionBidRequest;
import com.mipt.advertisement.controller.dto.AuctionBidResponse;
import com.mipt.advertisement.exception.AdvertisementNotFoundException;
import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.AuctionBid;
import com.mipt.advertisement.repository.AdvertisementRepository;
import com.mipt.advertisement.repository.AuctionBidRepository;
import com.mipt.wallet.model.Operation;
import com.mipt.wallet.model.OperationType;
import com.mipt.wallet.model.Wallet;
import com.mipt.wallet.service.OperationDataService;
import com.mipt.wallet.service.WalletDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionBidRepository bidRepository;
    private final AdvertisementRepository advertisementRepository;
    private final WalletDataService walletDataService;
    private final OperationDataService operationDataService;

    @Transactional(readOnly = true)
    public List<AuctionBidResponse> getBids(UUID adId) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new AdvertisementNotFoundException(adId));
        if (!ad.isAuction()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Объявление не является аукционом");
        }
        return bidRepository.findByAdvertisementIdOrderByAmountDesc(adId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public AuctionBidResponse placeBid(UUID adId, AuctionBidRequest request) {
        // Pessimistic lock to serialize concurrent bids on the same auction
        Advertisement ad = advertisementRepository.findByIdWithLock(adId)
                .orElseThrow(() -> new AdvertisementNotFoundException(adId));

        if (!ad.isAuction()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Объявление не является аукционом");
        }
        if (ad.getStatus() != AdvertisementStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Аукцион не активен");
        }
        if (ad.getAuctionClosedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Аукцион уже завершён");
        }
        if (ad.getAuctionEndsAt() != null && !ad.getAuctionEndsAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Время аукциона истекло");
        }
        if (request.getBidderId().equals(ad.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Автор не может участвовать в собственном аукционе");
        }

        Optional<AuctionBid> currentTop = bidRepository.findTopByAdvertisementIdOrderByAmountDesc(adId);
        long minRequired = currentTop
                .map(b -> b.getAmount() + 1)
                .orElse(ad.getPrice() != null ? ad.getPrice() : 1L);

        if (request.getAmount() < minRequired) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ставка должна быть не менее " + minRequired + " токенов");
        }

        String title = buildTitle(ad);

        // Cancel current leader's reservation (whether same bidder raising or different bidder outbidding)
        if (currentTop.isPresent()) {
            AuctionBid leader = currentTop.get();
            cancelReservation(leader.getBidderId(), ad.getAuthorId(), leader.getAmount(), title);
        }

        // Reserve tokens for new highest bidder
        reserveTokens(request.getBidderId(), ad.getAuthorId(), request.getAmount(), title);

        AuctionBid bid = AuctionBid.builder()
                .advertisementId(adId)
                .bidderId(request.getBidderId())
                .amount(request.getAmount())
                .build();

        log.info("Bid placed on auction {}: bidder={}, amount={}", adId, request.getBidderId(), request.getAmount());
        return toResponse(bidRepository.save(bid));
    }

    @Transactional
    public void closeAuction(UUID adId, UUID requesterId) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new AdvertisementNotFoundException(adId));

        if (!ad.isAuction()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Объявление не является аукционом");
        }
        if (!ad.getAuthorId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только автор может завершить аукцион");
        }
        if (ad.getAuctionClosedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Аукцион уже завершён");
        }

        ad.setAuctionClosedAt(Instant.now());
        advertisementRepository.save(ad);
        log.info("Auction {} closed by {}", adId, requesterId);
    }

    private void cancelReservation(UUID clientId, UUID performerId, Long amount, String title) {
        if (!walletDataService.existsById(clientId)) return;
        Wallet wallet = walletDataService.findById(clientId);
        if (wallet.getReservedTokens() < amount) {
            log.warn("Cannot cancel reservation for bidder {}: reserved={}, requested={}", clientId, wallet.getReservedTokens(), amount);
            return;
        }

        wallet.setReservedTokens(wallet.getReservedTokens() - amount);
        wallet.setAvailableTokens(wallet.getAvailableTokens() + amount);
        walletDataService.update(wallet);

        operationDataService.create(Operation.builder()
                .clientId(clientId)
                .performerId(performerId)
                .amount(amount)
                .type(OperationType.CANCEL)
                .title(title)
                .build());
    }

    private void reserveTokens(UUID clientId, UUID performerId, Long amount, String title) {
        if (!walletDataService.existsById(clientId)) {
            walletDataService.createWithUserId(clientId);
        }
        Wallet wallet = walletDataService.findById(clientId);
        if (wallet.getAvailableTokens() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недостаточно токенов для ставки (нужно " + amount + ", доступно " + wallet.getAvailableTokens() + ")");
        }
        wallet.setAvailableTokens(wallet.getAvailableTokens() - amount);
        wallet.setReservedTokens(wallet.getReservedTokens() + amount);
        walletDataService.update(wallet);

        operationDataService.create(Operation.builder()
                .clientId(clientId)
                .performerId(performerId)
                .amount(amount)
                .type(OperationType.RESERVE)
                .title(title)
                .build());
    }

    private String buildTitle(Advertisement ad) {
        return "[" + ad.getId() + "] " + ad.getName();
    }

    private AuctionBidResponse toResponse(AuctionBid bid) {
        return AuctionBidResponse.builder()
                .id(bid.getId())
                .advertisementId(bid.getAdvertisementId())
                .bidderId(bid.getBidderId())
                .amount(bid.getAmount())
                .createdAt(bid.getCreatedAt())
                .build();
    }
}
