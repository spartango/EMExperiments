package edu.harvard.mcb.leschziner.event;

public class ErrorEvent extends ProcessingEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -4984431049174703339L;
    protected final String    message;
    // Source information
    protected final int       lineNumber;
    protected final String    exceptionClassName;

    public ErrorEvent(String className, String message, int lineNumber) {
        this(className, message, lineNumber, "");
    }

    public ErrorEvent(String className,
                      String message,
                      int lineNumber,
                      String exceptionClassName) {
        super(className);
        this.message = message;
        this.lineNumber = lineNumber;
        this.exceptionClassName = exceptionClassName;
    }

    public String getMessage() {
        return message;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

}
