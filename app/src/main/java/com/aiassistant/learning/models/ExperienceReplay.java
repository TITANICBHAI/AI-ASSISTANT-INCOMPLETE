package com.aiassistant.learning.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Experience replay buffer for storing and sampling transitions.
 */
public class ExperienceReplay {
    private final int capacity;
    private final List<Transition> buffer;
    private final Random random = new Random();
    
    /**
     * Constructor
     * @param capacity Maximum capacity of the buffer
     */
    public ExperienceReplay(int capacity) {
        this.capacity = capacity;
        this.buffer = new ArrayList<>(capacity);
    }
    
    /**
     * Add a transition to the buffer
     * @param state Current state
     * @param action Action taken
     * @param reward Reward received
     * @param nextState Next state
     * @param done Whether the episode is done
     */
    public void add(float[] state, int action, float reward, float[] nextState, boolean done) {
        // Create transition
        Transition transition = new Transition(state, action, reward, nextState, done);
        
        // Add to buffer
        if (buffer.size() >= capacity) {
            // Replace oldest entry if at capacity
            buffer.remove(0);
        }
        buffer.add(transition);
    }
    
    /**
     * Sample a batch of transitions
     * @param batchSize Size of batch to sample
     * @return Batch of transitions
     */
    public Batch sample(int batchSize) {
        // Ensure we have enough samples
        int actualBatchSize = Math.min(batchSize, buffer.size());
        
        // Batch arrays
        float[][] states = new float[actualBatchSize][];
        int[] actions = new int[actualBatchSize];
        float[] rewards = new float[actualBatchSize];
        float[][] nextStates = new float[actualBatchSize][];
        boolean[] dones = new boolean[actualBatchSize];
        
        // Randomly sample transitions
        for (int i = 0; i < actualBatchSize; i++) {
            int index = random.nextInt(buffer.size());
            Transition transition = buffer.get(index);
            
            states[i] = transition.state;
            actions[i] = transition.action;
            rewards[i] = transition.reward;
            nextStates[i] = transition.nextState;
            dones[i] = transition.done;
        }
        
        return new Batch(states, actions, rewards, nextStates, dones, actualBatchSize);
    }
    
    /**
     * Get the current size of the buffer
     * @return Buffer size
     */
    public int size() {
        return buffer.size();
    }
    
    /**
     * Clear the buffer
     */
    public void clear() {
        buffer.clear();
    }
    
    /**
     * Get the capacity of the buffer
     * @return Buffer capacity
     */
    public int getCapacity() {
        return capacity;
    }
    
    /**
     * Class representing a single transition
     */
    public static class Transition {
        public final float[] state;
        public final int action;
        public final float reward;
        public final float[] nextState;
        public final boolean done;
        
        /**
         * Constructor
         * @param state Current state
         * @param action Action taken
         * @param reward Reward received
         * @param nextState Next state
         * @param done Whether the episode is done
         */
        public Transition(float[] state, int action, float reward, float[] nextState, boolean done) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.nextState = nextState;
            this.done = done;
        }
    }
    
    /**
     * Class representing a batch of transitions
     */
    public static class Batch {
        public final float[][] states;
        public final int[] actions;
        public final float[] rewards;
        public final float[][] nextStates;
        public final boolean[] dones;
        public final int size;
        
        /**
         * Constructor
         * @param states Array of states
         * @param actions Array of actions
         * @param rewards Array of rewards
         * @param nextStates Array of next states
         * @param dones Array of done flags
         * @param size Batch size
         */
        public Batch(float[][] states, int[] actions, float[] rewards, float[][] nextStates, boolean[] dones, int size) {
            this.states = states;
            this.actions = actions;
            this.rewards = rewards;
            this.nextStates = nextStates;
            this.dones = dones;
            this.size = size;
        }
    }
}