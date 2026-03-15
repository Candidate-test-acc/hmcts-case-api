package uk.gov.hmcts.dts_dev_challenge.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.dts_dev_challenge.dto.CaseOutcome;
import uk.gov.hmcts.dts_dev_challenge.dto.CaseStatus;
import uk.gov.hmcts.dts_dev_challenge.dto.CreateCaseRequest;
import uk.gov.hmcts.dts_dev_challenge.dto.PatchCaseRequest;
import uk.gov.hmcts.dts_dev_challenge.entity.CaseEntity;
import uk.gov.hmcts.dts_dev_challenge.exception.NotFoundException;
import uk.gov.hmcts.dts_dev_challenge.repository.CaseRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    CaseRepository repo;

    @InjectMocks
    CaseService service;

    @Test
    void create_generates_reference_and_defaults_status_open() {
        when(repo.save(any(CaseEntity.class))).thenAnswer(invocation -> {
            CaseEntity e = invocation.getArgument(0);
            if (e.getId() == null) {
                setIdForTest(e, 1001L);
            }
            return e;
        });

        var created = service.create(new CreateCaseRequest("Test case", "Desc"));

        assertTrue(created.reference().startsWith("HMCTS-"));
        assertEquals(CaseStatus.OPEN, created.status());
        assertEquals(CaseOutcome.NONE, created.outcome());

        verify(repo).save(any(CaseEntity.class));
    }

    @Test
    void patch_with_rejected_outcome_auto_closes_case() {
        CaseEntity existing = new CaseEntity();
        setIdForTest(existing, 1L);
        existing.setReference("HMCTS-2026-000001");
        existing.setTitle("Case");
        existing.setDescription("Desc");
        existing.setStatus(CaseStatus.OPEN.name());
        existing.setOutcome(CaseOutcome.NONE.name());
        existing.setDeletedAt(null);

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(CaseEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var patched = service.patch(1L, new PatchCaseRequest(
                null, null, null, CaseOutcome.REJECTED, "Rejected by tribunal"
        ));

        assertEquals(CaseOutcome.REJECTED, patched.outcome());
        assertEquals(CaseStatus.CLOSED, patched.status());
        verify(repo).save(any(CaseEntity.class));
    }

    @Test
    void soft_delete_hides_case_from_get() {
        CaseEntity existing = new CaseEntity();
        setIdForTest(existing, 2L);
        existing.setReference("HMCTS-2026-000002");
        existing.setTitle("Case");
        existing.setDescription("Desc");
        existing.setStatus(CaseStatus.OPEN.name());
        existing.setOutcome(CaseOutcome.NONE.name());
        existing.setDeletedAt(null);

        when(repo.findById(2L)).thenReturn(Optional.of(existing));
        when(repo.save(any(CaseEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.softDelete(2L, "Duplicate");

        assertNotNull(existing.getDeletedAt());
        assertThrows(NotFoundException.class, () -> service.get(2L, false));
        assertDoesNotThrow(() -> service.get(2L, true));
    }

    private static void setIdForTest(CaseEntity e, long id) {
        try {
            var f = CaseEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(e, id);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}