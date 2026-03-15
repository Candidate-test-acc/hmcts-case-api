package uk.gov.hmcts.dts_dev_challenge.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.hmcts.dts_dev_challenge.dto.*;
import uk.gov.hmcts.dts_dev_challenge.service.CaseService;
import uk.gov.hmcts.dts_dev_challenge.dto.PagedResponse;

import java.net.URI;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService service;

    public CaseController(CaseService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CaseResponse> create(@Valid @RequestBody CreateCaseRequest req) {
        CaseResponse created = service.create(req);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public CaseResponse get(
            @PathVariable long id,
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        return service.get(id, includeDeleted);
    }

    @GetMapping
    public PagedResponse<CaseResponse> list(
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) String reference,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(status, reference, includeDeleted, page, size);
    }

    @PutMapping("/{id}")
    public CaseResponse update(@PathVariable long id, @Valid @RequestBody UpdateCaseRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}")
    public CaseResponse patch(@PathVariable long id, @Valid @RequestBody PatchCaseRequest req) {
        return service.patch(id, req);
    }

    // Soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id, @Valid @RequestBody DeleteCaseRequest req) {
        service.softDelete(id, req.reason());
        return ResponseEntity.noContent().build();
    }

    // Restore (support/dev action)
    @PostMapping("/{id}/restore")
    public CaseResponse restore(@PathVariable long id, @RequestParam(defaultValue = "Restored by support") String reason) {
        return service.restore(id, reason);
    }

    // Close (business action)
    @PostMapping("/{id}/close")
    public CaseResponse close(@PathVariable long id, @RequestParam(defaultValue = "Case closed") String reason) {
        return service.close(id, reason);
    }
}