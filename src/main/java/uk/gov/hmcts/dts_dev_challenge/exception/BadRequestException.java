package uk.gov.hmcts.dts_dev_challenge.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
