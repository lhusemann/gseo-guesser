package com.gse23.lhusemann;

public class CorruptedExifDataException extends Exception {
    public CorruptedExifDataException() {
    }
    public CorruptedExifDataException(final String message) {
        super(message);
    }
    public CorruptedExifDataException(final String message, final Throwable cause) {
        super(message, cause);
    }
    public CorruptedExifDataException(final Throwable cause) {
        super(cause);
    }
}
