-- Auction support for advertisements

ALTER TABLE advertisements
  ADD COLUMN IF NOT EXISTS is_auction      BOOLEAN   NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS auction_ends_at  TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS auction_closed_at TIMESTAMPTZ;

-- price column is reused as starting_bid when is_auction = TRUE

CREATE TABLE IF NOT EXISTS auction_bids (
  id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  advertisement_id UUID        NOT NULL REFERENCES advertisements(id) ON DELETE CASCADE,
  bidder_id        UUID        NOT NULL,
  amount           BIGINT      NOT NULL,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_auction_bid_positive CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_auction_bids_advertisement_id ON auction_bids(advertisement_id);
CREATE INDEX IF NOT EXISTS idx_auction_bids_bidder_id        ON auction_bids(bidder_id);
CREATE INDEX IF NOT EXISTS idx_auction_bids_amount           ON auction_bids(advertisement_id, amount DESC);
