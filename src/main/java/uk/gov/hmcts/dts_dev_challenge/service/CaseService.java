package uk.gov.hmcts.dts_dev_challenge.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dts_dev_challenge.dto.*;
import uk.gov.hmcts.dts_dev_challenge.entity.CaseEntity;
import uk.gov.hmcts.dts_dev_challenge.exception.BadRequestException;
import uk.gov.hmcts.dts_dev_challenge.exception.NotFoundException;
import uk.gov.hmcts.dts_dev_challenge.repository.CaseRepository;

import java.time.Instant;

@Service
public class CaseService {

    private final CaseRepository repo;

    public CaseService(CaseRepository repo) {
        this.repo = repo;
    }

    private String generateReference() {
        int year = java.time.Year.now().getValue();
        String suffix = java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();

        return "HMCTS-" + year + "-" + suffix;
    }

    public CaseResponse create(CreateCaseRequest req) {
        CaseEntity e = new CaseEntity();
        e.setTitle(req.title());
        e.setDescription(req.description());
        e.setStatus(CaseStatus.OPEN.name());
        e.setOutcome(CaseOutcome.NONE.name());
        e.setLastChangeReason("Created case");
        e.setReference(generateReference());

        e = repo.save(e);
        return toResponse(e);
    }

    public CaseResponse get(long id, boolean includeDeleted) {
        CaseEntity e = repo.findById(id).orElseThrow(() -> new NotFoundException("case not found"));
        if (!includeDeleted && e.isDeleted()) throw new NotFoundException("case not found");
        return toResponse(e);
    }

    public PagedResponse<CaseResponse> list(
            CaseStatus status,
            String referenceQuery,
            boolean includeDeleted,
            int page,
            int size
    ) {
        if (page < 0) throw new BadRequestException("page must be >= 0");
        if (size < 1 || size > 100) throw new BadRequestException("size must be between 1 and 100");

        Pageable pageable = PageRequest.of(page, size);
        String q = (referenceQuery == null) ? null : referenceQuery.trim();

        Page<CaseEntity> result;

        if (includeDeleted) {
            if (status != null && q != null && !q.isEmpty())
                result = repo.findByStatusAndReferenceContainingIgnoreCase(status.name(), q, pageable);
            else if (status != null)
                result = repo.findByStatus(status.name(), pageable);
            else if (q != null && !q.isEmpty())
                result = repo.findByReferenceContainingIgnoreCase(q, pageable);
            else
                result = repo.findAll(pageable);
        } else {
            if (status != null && q != null && !q.isEmpty())
                result = repo.findByDeletedAtIsNullAndStatusAndReferenceContainingIgnoreCase(status.name(), q, pageable);
            else if (status != null)
                result = repo.findByDeletedAtIsNullAndStatus(status.name(), pageable);
            else if (q != null && !q.isEmpty())
                result = repo.findByDeletedAtIsNullAndReferenceContainingIgnoreCase(q, pageable);
            else
                result = repo.findByDeletedAtIsNull(pageable);
        }

        return new PagedResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                page,
                size,
                result.getTotalElements()
        );
    }

    public CaseResponse update(long id, UpdateCaseRequest req) {
        CaseEntity e = findActive(id);

        if (CaseStatus.valueOf(e.getStatus()) == CaseStatus.CLOSED)
            throw new BadRequestException("cannot update a closed case");

        e.setTitle(req.title());
        e.setDescription(req.description());
        if (req.status() != null) e.setStatus(req.status().name());
        if (req.outcome() != null) e.setOutcome(req.outcome().name());
        e.setLastChangeReason(req.changeReason());

        enforceOutcomeRules(e, req.outcome(), req.changeReason());

        return toResponse(repo.save(e));
    }

    public CaseResponse patch(long id, PatchCaseRequest req) {
        CaseEntity e = findActive(id);

        if (CaseStatus.valueOf(e.getStatus()) == CaseStatus.CLOSED)
            throw new BadRequestException("cannot update a closed case");

        if (req.title() != null) e.setTitle(req.title());
        if (req.description() != null) e.setDescription(req.description());
        if (req.status() != null) e.setStatus(req.status().name());
        if (req.outcome() != null) e.setOutcome(req.outcome().name());

        e.setLastChangeReason(req.changeReason());

        enforceOutcomeRules(e, req.outcome(), req.changeReason());

        return toResponse(repo.save(e));
    }

    public void softDelete(long id, String reason) {
        CaseEntity e = findActive(id);
        e.setDeletedAt(Instant.now());
        e.setLastChangeReason("Soft-deleted: " + reason);
        repo.save(e);
    }

    public CaseResponse restore(long id, String reason) {
        CaseEntity e = repo.findById(id).orElseThrow(() -> new NotFoundException("case not found"));
        if (!e.isDeleted()) throw new BadRequestException("case is not deleted");

        e.setDeletedAt(null);
        e.setLastChangeReason("Restored: " + reason);
        return toResponse(repo.save(e));
    }

    public CaseResponse close(long id, String reason) {
        CaseEntity e = findActive(id);
        e.setStatus(CaseStatus.CLOSED.name());
        e.setLastChangeReason(reason);
        return toResponse(repo.save(e));
    }

    private CaseEntity findActive(long id) {
        CaseEntity e = repo.findById(id).orElseThrow(() -> new NotFoundException("case not found"));
        if (e.isDeleted()) throw new NotFoundException("case not found");
        return e;
    }

    private void enforceOutcomeRules(CaseEntity e, CaseOutcome outcome, String reason) {
        if (outcome == null) return;

        boolean finalOutcome = outcome == CaseOutcome.REJECTED
                || outcome == CaseOutcome.APPROVED
                || outcome == CaseOutcome.WITHDRAWN;

        if (finalOutcome && CaseStatus.valueOf(e.getStatus()) != CaseStatus.CLOSED) {
            e.setStatus(CaseStatus.CLOSED.name());
            e.setLastChangeReason("Auto-closed due to outcome: " + reason);
        }
    }

    private CaseResponse toResponse(CaseEntity e) {
        return new CaseResponse(
                e.getId(),
                e.getReference(),
                e.getTitle(),
                e.getDescription(),
                CaseStatus.valueOf(e.getStatus()),
                CaseOutcome.valueOf(e.getOutcome()),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getLastChangeReason(),
                e.getDeletedAt()
        );
    }
}