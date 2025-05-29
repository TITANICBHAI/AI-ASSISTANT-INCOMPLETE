package com.aiassistant.ai.features.education.jee.pdf;

/**
 * Class representing the status of a PDF processing task
 */
public class PDFProcessingStatus {
    
    /**
     * Processing status enum
     */
    public enum Status {
        QUEUED,
        PROCESSING,
        COMPLETED,
        FAILED
    }
    
    private String processId;
    private String title;
    private String subject;
    private Status status;
    private int progress;
    private long startTime;
    private long endTime;
    
    /**
     * Constructor
     * @param processId Process ID
     * @param title Material title
     * @param subject Subject category
     */
    public PDFProcessingStatus(String processId, String title, String subject) {
        this.processId = processId;
        this.title = title;
        this.subject = subject;
        this.status = Status.QUEUED;
        this.progress = 0;
        this.startTime = System.currentTimeMillis();
        this.endTime = 0;
    }
    
    /**
     * Get the process ID
     * @return Process ID
     */
    public String getProcessId() {
        return processId;
    }
    
    /**
     * Get the material title
     * @return Title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Get the subject category
     * @return Subject
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * Get the current status
     * @return Status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Set the current status
     * @param status New status
     */
    public void setStatus(Status status) {
        this.status = status;
        if (status == Status.COMPLETED || status == Status.FAILED) {
            this.endTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Get the progress percentage (0-100)
     * @return Progress
     */
    public int getProgress() {
        return progress;
    }
    
    /**
     * Set the progress percentage
     * @param progress Progress (0-100)
     */
    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }
    
    /**
     * Get the processing start time
     * @return Start time in milliseconds
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Get the processing end time
     * @return End time in milliseconds, or 0 if not completed
     */
    public long getEndTime() {
        return endTime;
    }
    
    /**
     * Get the processing duration
     * @return Duration in milliseconds
     */
    public long getDuration() {
        if (endTime > 0) {
            return endTime - startTime;
        } else {
            return System.currentTimeMillis() - startTime;
        }
    }
    
    /**
     * Check if the processing is complete
     * @return True if completed
     */
    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }
    
    /**
     * Check if the processing failed
     * @return True if failed
     */
    public boolean isFailed() {
        return status == Status.FAILED;
    }
    
    /**
     * Check if the processing is in progress
     * @return True if in progress
     */
    public boolean isInProgress() {
        return status == Status.QUEUED || status == Status.PROCESSING;
    }
    
    @Override
    public String toString() {
        return "PDFProcessingStatus{" +
               "title='" + title + '\'' +
               ", subject='" + subject + '\'' +
               ", status=" + status +
               ", progress=" + progress + "%" +
               '}';
    }
}
