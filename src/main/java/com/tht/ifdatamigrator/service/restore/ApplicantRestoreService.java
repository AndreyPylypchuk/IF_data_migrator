package com.tht.ifdatamigrator.service.restore;

import com.tht.ifdatamigrator.dao.service.RestoreDaoService;
import com.tht.ifdatamigrator.dto.ApplicantDTO;
import com.tht.ifdatamigrator.dto.CompanyDTO.AssessmentData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@AllArgsConstructor
public class ApplicantRestoreService {

    private final RestoreDaoService service;

    @Transactional
    public void restore(ApplicantDTO app, Long cjId, Long cjsId, Long cjManagerId, Long assId, AssessmentData ass, Map<Integer, Long> qOrderId) {
        log.info("Restoring applicant {}", app.getId());

        Long userId = getUserId(app);

        Long cjcId = service.createCompanyJobpostingCandidate(app, userId, cjId, cjsId);
        service.createCompanyJobpostingCandidateEmail(cjcId, app.getEmail());
        service.createCompanyJobpostingCandidatePhone(cjcId, app.getPhone());

        Long cjcaId = service.createCompanyJobpostingCandidateAss(
                cjcId, assId, ass.getThtVersion(), app, cjManagerId
        );

        CountDownLatch countDownLatch = new CountDownLatch(app.getQuestionAnswer().size());
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        app.getQuestionAnswer()
                .forEach((key, value) -> {
                    executorService.submit(() -> {
                        log.info("Restoring question {}", key);
                        Long qId = qOrderId.get(key);
                        if (nonNull(qId))
                            createQuestionAnswer(cjcaId, userId, qId, value, app.getTestDate());
                        countDownLatch.countDown();
                    });
                });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Parallel exception", e);
        } finally {
            executorService.shutdown();
        }

        service.createIntegrityFirstResult(cjcaId, ass.getThtVersion(), app);
    }

    private void createQuestionAnswer(Long cjcaId, Long userId, Long qId, Integer answer, LocalDateTime date) {
        Long aId;
        if (isNull(answer)) aId = null;
        else aId = service.getAnswerId(qId, answer);

        service.creteQuestionAnswer(cjcaId, qId, aId, date, userId);
    }

    private Long getUserId(ApplicantDTO app) {
        Long userId = -1L;
        if (nonNull(app.getEmail())) {
            userId = service.getUserIdByEmail(app.getEmail());
            if (userId == null)
                userId = service.createUser(app);
        }
        return userId;
    }
}
