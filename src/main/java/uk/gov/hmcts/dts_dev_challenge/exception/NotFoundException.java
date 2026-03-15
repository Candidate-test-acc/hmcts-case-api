package uk.gov.hmcts.dts_dev_challenge.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}