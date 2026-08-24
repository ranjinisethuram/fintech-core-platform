/**
 * HISTORICAL TRANSACTIONS BUILDING LOGIC
 *
 * This document explains how to build historical transactions for fraud evaluation.
 *
 * 1. BASIC USAGE - Using HistoricalTransactionBuilder
 * =====================================================
 *
 * HistoricalTransactionBuilder builder = new HistoricalTransactionBuilder();
 * builder.addTransaction(new BigDecimal("50000"), Instant.parse("2026-08-20T16:30:00Z"))
 *        .addTransaction(new BigDecimal("45000"), Instant.parse("2026-08-20T16:35:00Z"))
 *        .addTransaction(new BigDecimal("55000"), Instant.parse("2026-08-20T16:40:00Z"));
 * List<HistoricalTransaction> history = builder.build();
 *
 *
 * 2. QUERYING FROM DATABASE/LEDGER SERVICE
 * =========================================
 *
 * To build historical transactions, you typically:
 *
 * a) Query transactions from the transaction ledger for the customer/account
 * b) Filter by time window (e.g., last 30 days)
 * c) Map database records to HistoricalTransaction objects
 * d) Pass to FraudEvaluationRequest
 *
 * Example (pseudo-code):
 * ----------------------
 * List<TransactionRecord> dbTxns = transactionRepository.findByAccountIdAndCreatedAfter(
 *     accountId,
 *     Instant.now().minus(Duration.ofDays(30))
 * );
 *
 * List<HistoricalTransaction> history = dbTxns.stream()
 *     .map(txn -> new HistoricalTransaction(txn.getAmount(), txn.getCreatedAt()))
 *     .collect(Collectors.toList());
 *
 *
 * 3. TIME WINDOW FILTERING
 * ========================
 *
 * To filter transactions within a specific time window:
 *
 * List<HistoricalTransaction> last10Min = builder.filterByTimeWindow(10, Instant.now());
 * List<HistoricalTransaction> last1Hour = builder.filterByTimeWindow(60, Instant.now());
 * List<HistoricalTransaction> last24Hours = builder.filterByTimeWindow(1440, Instant.now());
 *
 *
 * 4. CALCULATING STATISTICS
 * ==========================
 *
 * Average Amount:
 *     BigDecimal avg = builder.calculateAverage();
 *
 * Transaction Count in Time Window:
 *     long count = builder.countInTimeWindow(10, Instant.now()); // last 10 minutes
 *
 *
 * 5. IN THE FRAUD EVALUATION FLOW
 * ================================
 *
 * When EvaluateFraudCommand is received:
 *
 * 1. Fetch all historical transactions for the account from database/ledger
 * 2. Build HistoricalTransactionBuilder
 * 3. Pass to FraudEvaluationRequest
 * 4. FraudService.evaluate() will:
 *    - Check HighAmountRule (current amount > 100,000)
 *    - Check HighVelocityRule (>5 txns in last 10 min, including current)
 *    - Check UnusualAmountRule (current amount > 3× average)
 *
 * Example in EvaluateFraudStep:
 * -----------------------------
 * // Fetch historical transactions for the account
 * List<Transaction> historicalTxns = transactionService.findByAccountIdSorted(accountId);
 * List<HistoricalTransaction> history = historicalTxns.stream()
 *     .map(txn -> new HistoricalTransaction(txn.getAmount(), txn.getCreatedAt()))
 *     .collect(Collectors.toList());
 *
 * EvaluateFraudCommand command = new EvaluateFraudCommand(
 *     transactionId,
 *     amount,
 *     Instant.now(),
 *     history
 * );
 *
 *
 * 6. NOTES
 * ========
 *
 * - Historical transactions should include transactions from at least the last 30 days
 *   for accurate average calculation
 * - The current transaction amount should NOT be included in the historical list
 *   (it's provided separately in the EvaluateFraudCommand.amount field)
 * - If insufficient data exists, still proceed with evaluation (UnusualAmountRule
 *   handles empty history gracefully)
 * - For HighVelocityRule, the current transaction is counted in the 10-min window
 *
 */
