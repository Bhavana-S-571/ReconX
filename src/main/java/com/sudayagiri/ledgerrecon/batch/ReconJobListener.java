
package com.sudayagiri.ledgerrecon.batch;

import com.sudayagiri.ledgerrecon.model.ReconResult;
import com.sudayagiri.ledgerrecon.model.ReconStatus;
import com.sudayagiri.ledgerrecon.repo.ReconResultRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class ReconJobListener implements JobExecutionListener {

    private static final Logger log =
            LoggerFactory.getLogger(ReconJobListener.class);

    private final BankStatementCache bank;
    private final ReconResultRepository repo;

    public ReconJobListener(
            BankStatementCache bank,
            ReconResultRepository repo) {

        this.bank = bank;
        this.repo = repo;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {

        repo.deleteAllInBatch();
        bank.reset();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        if (jobExecution.getStatus().isUnsuccessful()) {
            log.error("Reconciliation job failed. Skipping summary.");
            return;
        }

        // Save bank transactions missing from ledger
        for (BankStatementCache.BankTxn txn : bank.unmatched()) {

            repo.save(new ReconResult(
                    txn.txnId(),
                    ReconStatus.MISSING_IN_LEDGER,
                    null,
                    txn.amount(),
                    null,
                    txn.counterparty()
            ));
        }

        log.info(
                "DUPLICATES RECEIVED BY LISTENER: {}",
                bank.duplicateTransactions().size()
        );

        // Save duplicate bank transactions
        for (BankStatementCache.BankTxn txn :
                bank.duplicateTransactions()) {

            repo.save(new ReconResult(
                    txn.txnId(),
                    ReconStatus.DUPLICATE_IN_BANK,
                    null,
                    txn.amount(),
                    null,
                    txn.counterparty()
            ));
        }

        long matched = repo.countByStatus(ReconStatus.MATCHED);
        long mismatch = repo.countByStatus(ReconStatus.MISMATCH);
        long missingBank = repo.countByStatus(ReconStatus.MISSING_IN_BANK);
        long missingLedger = repo.countByStatus(ReconStatus.MISSING_IN_LEDGER);
        long duplicates = repo.countByStatus(ReconStatus.DUPLICATE_IN_BANK);

        log.info("================ RECONCILIATION SUMMARY ================");
        log.info("Matched             : {}", matched);
        log.info("Mismatched          : {}", mismatch);
        log.info("Missing in bank     : {}", missingBank);
        log.info("Missing in ledger   : {}", missingLedger);
        log.info("Duplicate in bank   : {}", duplicates);
        log.info("Total exceptions    : {}",
                mismatch + missingBank + missingLedger + duplicates);
        log.info("========================================================");
    }
}
