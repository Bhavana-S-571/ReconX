
package com.sudayagiri.ledgerrecon.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class BankStatementCache {

    private static final Logger log =
            LoggerFactory.getLogger(BankStatementCache.class);

    public record BankTxn(
            String txnId,
            BigDecimal amount,
            String counterparty
    ) {}

    private final Map<String, BankTxn> byId =
            new ConcurrentHashMap<>();

    private final Set<String> matched =
            ConcurrentHashMap.newKeySet();

    private final List<BankTxn> duplicates =
            new CopyOnWriteArrayList<>();

    public synchronized void reset() {
        byId.clear();
        matched.clear();
        duplicates.clear();

        load();

        log.info("BANK CACHE LOADED: {} unique transactions", byId.size());
        log.info("BANK DUPLICATES DETECTED: {}", duplicates.size());
    }

    private void load() {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("data/bank.csv").getInputStream(),
                        StandardCharsets.UTF_8))) {

            String header = reader.readLine();

            if (header == null) {
                throw new IllegalStateException("Bank CSV is empty");
            }

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);

                if (fields.length < 4) {
                    throw new IllegalArgumentException(
                            "Invalid bank CSV row: " + line);
                }

                BankTxn txn = new BankTxn(
                        fields[0].trim(),
                        new BigDecimal(fields[2].trim()),
                        fields[3].trim()
                );

                BankTxn existing =
                        byId.putIfAbsent(txn.txnId(), txn);

                if (existing != null) {
                    duplicates.add(txn);

                    log.warn(
                            "DUPLICATE BANK TRANSACTION FOUND: {}",
                            txn.txnId()
                    );
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to load bank statement", e);
        }
    }

    public BankTxn find(String txnId) {
        return byId.get(txnId);
    }

    public void markMatched(String txnId) {
        matched.add(txnId);
    }

    public List<BankTxn> unmatched() {

        List<BankTxn> result = new ArrayList<>();

        for (BankTxn txn : byId.values()) {
            if (!matched.contains(txn.txnId())) {
                result.add(txn);
            }
        }

        return result;
    }

    public List<BankTxn> duplicateTransactions() {
        return List.copyOf(duplicates);
    }
}
