
package com.sudayagiri.ledgerrecon.web;

import com.sudayagiri.ledgerrecon.model.ReconResult;
import com.sudayagiri.ledgerrecon.model.ReconStatus;
import com.sudayagiri.ledgerrecon.repo.ReconResultRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recon")
public class ReconController {

    private final ReconResultRepository repo;
    private final JobLauncher jobLauncher;
    private final Job reconciliationJob;

    public ReconController(
            ReconResultRepository repo,
            JobLauncher jobLauncher,
            Job reconciliationJob) {

        this.repo = repo;
        this.jobLauncher = jobLauncher;
        this.reconciliationJob = reconciliationJob;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {

        Map<String, Object> response = new LinkedHashMap<>();

        long matched = repo.countByStatus(ReconStatus.MATCHED);
        long mismatch = repo.countByStatus(ReconStatus.MISMATCH);
        long missingBank = repo.countByStatus(ReconStatus.MISSING_IN_BANK);
        long missingLedger = repo.countByStatus(ReconStatus.MISSING_IN_LEDGER);
        long duplicates = repo.countByStatus(ReconStatus.DUPLICATE_IN_BANK);

        long total = matched + mismatch + missingBank
                + missingLedger + duplicates;

        long exceptions = mismatch + missingBank
                + missingLedger + duplicates;

        Map<String, Long> breakdown = new LinkedHashMap<>();

        for (ReconStatus status : ReconStatus.values()) {
            breakdown.put(status.name(), repo.countByStatus(status));
        }

        response.put("message", "Reconciliation summary retrieved successfully");
        response.put("totalTransactions", total);
        response.put("matchedTransactions", matched);
        response.put("exceptionCount", exceptions);
        response.put("statusBreakdown", breakdown);

        return response;
    }

    @GetMapping("/exceptions")
    public Map<String, Object> exceptions() {

        List<ReconResult> results =
                repo.findByStatusNot(ReconStatus.MATCHED);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", "Exception records retrieved successfully");
        response.put("totalExceptions", results.size());
        response.put("exceptions", results);

        return response;
    }

    @PostMapping("/run")
    public Map<String, Object> run() throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();

        var execution = jobLauncher.run(reconciliationJob, params);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", "Reconciliation job executed");
        response.put("jobId", execution.getJobId());
        response.put("status", execution.getStatus().toString());

        return response;
    }
}
