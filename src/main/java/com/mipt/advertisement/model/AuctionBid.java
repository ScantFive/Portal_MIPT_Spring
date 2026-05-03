package com.mipt.advertisement.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auction_bids")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionBid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "advertisement_id", nullable = false)
    private UUID advertisementId;

    @Column(name = "bidder_id", nullable = false)
    private UUID bidderId;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
