-- -----------------------------------------------------------------------------
-- Clean existing seed data (idempotent)
-- -----------------------------------------------------------------------------
DELETE FROM portfolio_asset;

INSERT INTO portfolio_asset (id, symbol, name, quantity, purchasePrice, currency) values
(1, 'AAPL', 'Apple Inc.', 100, 15000, 'USD'), -- Portfolio 1: 100 shares of Apple
(2, 'MSFT', 'Microsoft Corporation', 50, 20000, 'USD'),  -- Portfolio 1: 50 shares of Microsoft
(3, 'AAPL', 'Apple Inc.', 200, 15000, 'USD'), -- Portfolio 2: 200 shares of Apple
(4, 'AMZN', 'Amazon.com Inc.', 150, 25000, 'USD'); -- Portfolio 2: 150 shares of Amazon