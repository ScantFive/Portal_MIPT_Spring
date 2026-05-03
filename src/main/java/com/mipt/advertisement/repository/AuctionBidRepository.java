package com.mipt.advertisement.repository;

import com.mipt.advertisement.model.AuctionBid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionBidRepository extends JpaRepository<AuctionBid, UUID> {
    List<AuctionBid> findByAdvertisementIdOrderByAmountDesc(UUID advertisementId);
    Optional<AuctionBid> findTopByAdvertisementIdOrderByAmountDesc(UUID advertisementId);
}
