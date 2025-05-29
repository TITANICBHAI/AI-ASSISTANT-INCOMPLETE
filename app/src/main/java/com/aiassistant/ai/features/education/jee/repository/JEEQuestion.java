package com.aiassistant.ai.features.education.jee.repository;

/**
 * Represents a JEE Advanced question with its solution
 */
public class JEEQuestion {
    private String question;
    private String solution;
    private String subject;
    private String topic;
    private long timeAdded;
    
    /**
     * Constructor for creating a new JEE question
     * @param question The question text
     * @param solution The solution text
     * @param subject The subject (physics, math, chemistry)
     * @param topic The specific topic within the subject
     */
    public JEEQuestion(String question, String solution, String subject, String topic) {
        this.question = question;
        this.solution = solution;
        this.subject = subject.toLowerCase();
        this.topic = topic;
        this.timeAdded = System.currentTimeMillis();
    }
    
    /**
     * Get the question text
     * @return Question text
     */
    public String getQuestion() {
        return question;
    }
    
    /**
     * Get the solution text
     * @return Solution text
     */
    public String getSolution() {
        return solution;
    }
    
    /**
     * Get the subject of the question
     * @return Subject
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * Get the topic within the subject
     * @return Topic
     */
    public String getTopic() {
        return topic;
    }
    
    /**
     * Get time when question was added
     * @return Time in milliseconds
     */
    public long getTimeAdded() {
        return timeAdded;
    }
    
    /**
     * Set the solution for this question
     * @param solution Updated solution
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }
    
    /**
     * Update the topic
     * @param topic Updated topic
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }
}