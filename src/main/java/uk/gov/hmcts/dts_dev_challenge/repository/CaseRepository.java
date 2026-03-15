package uk.gov.hmcts.dts_dev_challenge.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.dts_dev_challenge.entity.CaseEntity;

import java.time.Instant;
import java.util.Optional;

public interface CaseRepository extends JpaRepository<CaseEntity, Long> {

    Optional<CaseEntity> findByReferenceIgnoreCase(String reference);

    // active case
    Page<CaseEntity> findByDeletedAtIsNull(Pageable pageable);
    Page<CaseEntity> findByDeletedAtIsNullAndStatus(String status, Pageable pageable);
    Page<CaseEntity> findByDeletedAtIsNullAndReferenceContainingIgnoreCase(String reference, Pageable pageable);
    Page<CaseEntity> findByDeletedAtIsNullAndStatusAndReferenceContainingIgnoreCase(String status, String reference, Pageable pageable);

    // include deleted
    Page<CaseEntity> findByStatus(String status, Pageable pageable);
    Page<CaseEntity> findByReferenceContainingIgnoreCase(String reference, Pageable pageable);
    Page<CaseEntity> findByStatusAndReferenceContainingIgnoreCase(String status, String reference, Pageable pageable);

    // soft delete helper (optional future): find deleted since…
    Page<CaseEntity> findByDeletedAtAfter(Instant since, Pageable pageable);
}