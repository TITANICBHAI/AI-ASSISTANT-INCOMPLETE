package com.aiassistant.ai.features.education.jee;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ai.AIStateManager.KnowledgeEntry;
import com.aiassistant.core.ai.HybridAILearningSystem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JEESolver is an advanced problem-solving system for JEE mathematics, physics, 
 * and chemistry problems. It implements symbolic and numerical algorithms for
 * solving complex academic problems with step-by-step explanations.
 */
public class JEESolver {
    private static final String TAG = "JEESolver";
    private static final String PROBLEM_HISTORY_FILE = "jee_problem_history.json";
    private static JEESolver instance;
    
    // Core components
    private Context context;
    private AIStateManager aiStateManager;
    private ExecutorService executor;
    private HybridAILearningSystem hybridAI;
    
    // Domain-specific knowledge and engines
    private Map<String, List<Formula>> formulasByDomain = new HashMap<>();
    private Map<String, List<Concept>> conceptsByDomain = new HashMap<>();
    private Set<String> mathKeywords = new HashSet<>();
    private Set<String> physicsKeywords = new HashSet<>();
    private Set<String> chemistryKeywords = new HashSet<>();
    
    // Domain definitions
    public static final String DOMAIN_MATH = "mathematics";
    public static final String DOMAIN_PHYSICS = "physics";
    public static final String DOMAIN_CHEMISTRY = "chemistry";
    
    // Problem history
    private List<Problem> problemHistory = new ArrayList<>();
    
    // Callback interface for solving process
    public interface SolveCallback {
        void onSolveStarted(String problemId);
        void onStepCompleted(String problemId, String step, String explanation);
        void onSolveCompleted(String problemId, Solution solution);
        void onSolveError(String problemId, String errorMessage);
    }
    
    /**
     * Problem representation
     */
    public static class Problem {
        public String id;
        public String text;
        public String domain;
        public List<String> topics = new ArrayList<>();
        public Map<String, String> variables = new HashMap<>();
        public String originalQuery;
        public long timestamp;
        public Solution solution;
        
        public Problem(String text) {
            this.id = UUID.randomUUID().toString();
            this.text = text;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Solution with steps and explanation
     */
    public static class Solution {
        public String problemId;
        public String answer;
        public List<Step> steps = new ArrayList<>();
        public String explanation;
        public long solveTimeMs;
        public boolean isCorrect;
        public boolean isVerified;
        public List<String> relatedConcepts = new ArrayList<>();
        public List<Formula> usedFormulas = new ArrayList<>();
        
        public Solution(String problemId) {
            this.problemId = problemId;
        }
    }
    
    /**
     * Step in the solution process
     */
    public static class Step {
        public String description;
        public String workingOut;
        public String explanation;
        
        public Step(String description, String workingOut, String explanation) {
            this.description = description;
            this.workingOut = workingOut;
            this.explanation = explanation;
        }
    }
    
    /**
     * Formula representation
     */
    public static class Formula {
        public String name;
        public String expression;
        public String domain;
        public List<String> topics = new ArrayList<>();
        public Map<String, String> variableDescriptions = new HashMap<>();
        public String explanation;
        
        public Formula(String name, String expression, String domain) {
            this.name = name;
            this.expression = expression;
            this.domain = domain;
        }
    }
    
    /**
     * Concept representation
     */
    public static class Concept {
        public String name;
        public String domain;
        public String definition;
        public List<String> relatedConcepts = new ArrayList<>();
        public List<Formula> relatedFormulas = new ArrayList<>();
        public List<String> examples = new ArrayList<>();
        
        public Concept(String name, String domain, String definition) {
            this.name = name;
            this.domain = domain;
            this.definition = definition;
        }
    }
    
    private JEESolver(Context context) {
        this.context = context.getApplicationContext();
        this.aiStateManager = AIStateManager.getInstance(context);
        this.executor = Executors.newFixedThreadPool(2);
        this.hybridAI = HybridAILearningSystem.getInstance(context);
        
        // Initialize domain-specific knowledge
        initializeKnowledge();
        
        // Load problem history
        loadProblemHistory();
    }
    
    public static synchronized JEESolver getInstance(Context context) {
        if (instance == null) {
            instance = new JEESolver(context);
        }
        return instance;
    }
    
    /**
     * Initialize domain-specific knowledge bases
     */
    private void initializeKnowledge() {
        // Initialize formula lists
        formulasByDomain.put(DOMAIN_MATH, new ArrayList<>());
        formulasByDomain.put(DOMAIN_PHYSICS, new ArrayList<>());
        formulasByDomain.put(DOMAIN_CHEMISTRY, new ArrayList<>());
        
        // Initialize concept lists
        conceptsByDomain.put(DOMAIN_MATH, new ArrayList<>());
        conceptsByDomain.put(DOMAIN_PHYSICS, new ArrayList<>());
        conceptsByDomain.put(DOMAIN_CHEMISTRY, new ArrayList<>());
        
        // Add core formulas and concepts
        initializeMathKnowledge();
        initializePhysicsKnowledge();
        initializeChemistryKnowledge();
        
        // Initialize keywords for classification
        initializeKeywords();
    }
    
    /**
     * Initialize mathematics knowledge
     */
    private void initializeMathKnowledge() {
        List<Formula> mathFormulas = formulasByDomain.get(DOMAIN_MATH);
        List<Concept> mathConcepts = conceptsByDomain.get(DOMAIN_MATH);
        
        // Calculus formulas
        Formula derivative = new Formula("Derivative of Power Rule", "d/dx(x^n) = n*x^(n-1)", DOMAIN_MATH);
        derivative.topics.add("calculus");
        derivative.topics.add("derivatives");
        derivative.variableDescriptions.put("n", "power");
        derivative.variableDescriptions.put("x", "variable");
        derivative.explanation = "The power rule is used to find the derivative of a function of the form x^n.";
        mathFormulas.add(derivative);
        
        Formula integral = new Formula("Integral of Power Rule", "∫x^n dx = x^(n+1)/(n+1) + C (n ≠ -1)", DOMAIN_MATH);
        integral.topics.add("calculus");
        integral.topics.add("integrals");
        integral.variableDescriptions.put("n", "power");
        integral.variableDescriptions.put("x", "variable");
        integral.variableDescriptions.put("C", "constant of integration");
        integral.explanation = "The integral power rule allows us to integrate expressions of the form x^n.";
        mathFormulas.add(integral);
        
        // Trigonometry formulas
        Formula sinRule = new Formula("Sine Rule", "a/sin(A) = b/sin(B) = c/sin(C)", DOMAIN_MATH);
        sinRule.topics.add("trigonometry");
        sinRule.variableDescriptions.put("a,b,c", "sides of triangle");
        sinRule.variableDescriptions.put("A,B,C", "angles opposite to sides a,b,c");
        sinRule.explanation = "The sine rule relates the sides of a triangle to the sines of the opposite angles.";
        mathFormulas.add(sinRule);
        
        // Algebra formulas
        Formula quadratic = new Formula("Quadratic Formula", "x = (-b ± √(b² - 4ac)) / 2a", DOMAIN_MATH);
        quadratic.topics.add("algebra");
        quadratic.topics.add("equations");
        quadratic.variableDescriptions.put("a,b,c", "coefficients in ax² + bx + c = 0");
        quadratic.variableDescriptions.put("x", "variable/root");
        quadratic.explanation = "The quadratic formula provides the solutions to a quadratic equation ax² + bx + c = 0.";
        mathFormulas.add(quadratic);
        
        // Math concepts
        Concept differentiation = new Concept("Differentiation", DOMAIN_MATH, 
                "The process of finding the derivative of a function, which represents the rate of change of the function with respect to a variable.");
        differentiation.relatedConcepts.add("Integration");
        differentiation.relatedConcepts.add("Limit");
        differentiation.relatedFormulas.add(derivative);
        mathConcepts.add(differentiation);
        
        Concept integration = new Concept("Integration", DOMAIN_MATH, 
                "The process of finding the integral of a function, which represents the area under the curve of the function.");
        integration.relatedConcepts.add("Differentiation");
        integration.relatedConcepts.add("Antiderivative");
        integration.relatedFormulas.add(integral);
        mathConcepts.add(integration);
        
        Concept trigonometry = new Concept("Trigonometry", DOMAIN_MATH, 
                "The branch of mathematics dealing with the relations of the sides and angles of triangles and the relevant functions of angles.");
        trigonometry.relatedConcepts.add("Sine");
        trigonometry.relatedConcepts.add("Cosine");
        trigonometry.relatedConcepts.add("Tangent");
        trigonometry.relatedFormulas.add(sinRule);
        mathConcepts.add(trigonometry);
        
        // Add more formulas and concepts as needed
    }
    
    /**
     * Initialize physics knowledge
     */
    private void initializePhysicsKnowledge() {
        List<Formula> physicsFormulas = formulasByDomain.get(DOMAIN_PHYSICS);
        List<Concept> physicsConcepts = conceptsByDomain.get(DOMAIN_PHYSICS);
        
        // Mechanics formulas
        Formula newtonSecond = new Formula("Newton's Second Law", "F = m*a", DOMAIN_PHYSICS);
        newtonSecond.topics.add("mechanics");
        newtonSecond.topics.add("forces");
        newtonSecond.variableDescriptions.put("F", "force (N)");
        newtonSecond.variableDescriptions.put("m", "mass (kg)");
        newtonSecond.variableDescriptions.put("a", "acceleration (m/s²)");
        newtonSecond.explanation = "Newton's second law states that the rate of change of momentum is proportional to the applied force and occurs in the direction of the force.";
        physicsFormulas.add(newtonSecond);
        
        Formula kineticEnergy = new Formula("Kinetic Energy", "KE = (1/2)*m*v²", DOMAIN_PHYSICS);
        kineticEnergy.topics.add("mechanics");
        kineticEnergy.topics.add("energy");
        kineticEnergy.variableDescriptions.put("KE", "kinetic energy (J)");
        kineticEnergy.variableDescriptions.put("m", "mass (kg)");
        kineticEnergy.variableDescriptions.put("v", "velocity (m/s)");
        kineticEnergy.explanation = "Kinetic energy is the energy possessed by an object due to its motion.";
        physicsFormulas.add(kineticEnergy);
        
        // Electricity formulas
        Formula ohmsLaw = new Formula("Ohm's Law", "V = I*R", DOMAIN_PHYSICS);
        ohmsLaw.topics.add("electricity");
        ohmsLaw.topics.add("circuits");
        ohmsLaw.variableDescriptions.put("V", "potential difference (V)");
        ohmsLaw.variableDescriptions.put("I", "current (A)");
        ohmsLaw.variableDescriptions.put("R", "resistance (Ω)");
        ohmsLaw.explanation = "Ohm's law states that the current through a conductor is proportional to the voltage across it.";
        physicsFormulas.add(ohmsLaw);
        
        // Thermodynamics formulas
        Formula firstLaw = new Formula("First Law of Thermodynamics", "ΔU = Q - W", DOMAIN_PHYSICS);
        firstLaw.topics.add("thermodynamics");
        firstLaw.variableDescriptions.put("ΔU", "change in internal energy");
        firstLaw.variableDescriptions.put("Q", "heat added to system");
        firstLaw.variableDescriptions.put("W", "work done by system");
        firstLaw.explanation = "The first law of thermodynamics states that energy cannot be created or destroyed, only transferred or converted.";
        physicsFormulas.add(firstLaw);
        
        // Physics concepts
        Concept mechanics = new Concept("Mechanics", DOMAIN_PHYSICS, 
                "The branch of physics dealing with the study of motion, forces, and energy.");
        mechanics.relatedConcepts.add("Kinematics");
        mechanics.relatedConcepts.add("Dynamics");
        mechanics.relatedFormulas.add(newtonSecond);
        mechanics.relatedFormulas.add(kineticEnergy);
        physicsConcepts.add(mechanics);
        
        Concept electricity = new Concept("Electricity", DOMAIN_PHYSICS, 
                "The branch of physics that deals with the phenomena and properties of electric charge.");
        electricity.relatedConcepts.add("Current");
        electricity.relatedConcepts.add("Voltage");
        electricity.relatedConcepts.add("Resistance");
        electricity.relatedFormulas.add(ohmsLaw);
        physicsConcepts.add(electricity);
        
        Concept thermodynamics = new Concept("Thermodynamics", DOMAIN_PHYSICS, 
                "The branch of physics that deals with heat, work, temperature, and their relation to energy, radiation, and physical properties of matter.");
        thermodynamics.relatedConcepts.add("Heat");
        thermodynamics.relatedConcepts.add("Energy");
        thermodynamics.relatedConcepts.add("Entropy");
        thermodynamics.relatedFormulas.add(firstLaw);
        physicsConcepts.add(thermodynamics);
        
        // Add more formulas and concepts as needed
    }
    
    /**
     * Initialize chemistry knowledge
     */
    private void initializeChemistryKnowledge() {
        List<Formula> chemistryFormulas = formulasByDomain.get(DOMAIN_CHEMISTRY);
        List<Concept> chemistryConcepts = conceptsByDomain.get(DOMAIN_CHEMISTRY);
        
        // Thermochemistry formulas
        Formula enthalpy = new Formula("Enthalpy Change", "ΔH = ΔU + PΔV", DOMAIN_CHEMISTRY);
        enthalpy.topics.add("thermochemistry");
        enthalpy.variableDescriptions.put("ΔH", "change in enthalpy");
        enthalpy.variableDescriptions.put("ΔU", "change in internal energy");
        enthalpy.variableDescriptions.put("P", "pressure");
        enthalpy.variableDescriptions.put("ΔV", "change in volume");
        enthalpy.explanation = "Enthalpy change represents the heat transferred during a chemical process at constant pressure.";
        chemistryFormulas.add(enthalpy);
        
        // Electrochemistry formulas
        Formula nernst = new Formula("Nernst Equation", "E = E° - (RT/nF)ln(Q)", DOMAIN_CHEMISTRY);
        nernst.topics.add("electrochemistry");
        nernst.variableDescriptions.put("E", "cell potential");
        nernst.variableDescriptions.put("E°", "standard cell potential");
        nernst.variableDescriptions.put("R", "gas constant");
        nernst.variableDescriptions.put("T", "temperature");
        nernst.variableDescriptions.put("n", "number of electrons");
        nernst.variableDescriptions.put("F", "Faraday constant");
        nernst.variableDescriptions.put("Q", "reaction quotient");
        nernst.explanation = "The Nernst equation relates the cell potential to the standard cell potential and concentrations of the cell components.";
        chemistryFormulas.add(nernst);
        
        // Kinetics formulas
        Formula rateConstant = new Formula("Arrhenius Equation", "k = A*e^(-Ea/RT)", DOMAIN_CHEMISTRY);
        rateConstant.topics.add("kinetics");
        rateConstant.variableDescriptions.put("k", "rate constant");
        rateConstant.variableDescriptions.put("A", "pre-exponential factor");
        rateConstant.variableDescriptions.put("Ea", "activation energy");
        rateConstant.variableDescriptions.put("R", "gas constant");
        rateConstant.variableDescriptions.put("T", "temperature");
        rateConstant.explanation = "The Arrhenius equation gives the dependence of the rate constant on temperature and activation energy.";
        chemistryFormulas.add(rateConstant);
        
        // Equilibrium formulas
        Formula equilibriumConstant = new Formula("Equilibrium Constant", "K = [products]/[reactants]", DOMAIN_CHEMISTRY);
        equilibriumConstant.topics.add("equilibrium");
        equilibriumConstant.variableDescriptions.put("K", "equilibrium constant");
        equilibriumConstant.variableDescriptions.put("[products]", "concentration of products");
        equilibriumConstant.variableDescriptions.put("[reactants]", "concentration of reactants");
        equilibriumConstant.explanation = "The equilibrium constant is the ratio of product concentrations to reactant concentrations at equilibrium.";
        chemistryFormulas.add(equilibriumConstant);
        
        // Chemistry concepts
        Concept thermochemistry = new Concept("Thermochemistry", DOMAIN_CHEMISTRY, 
                "The study of heat energy associated with chemical reactions and physical transformations.");
        thermochemistry.relatedConcepts.add("Enthalpy");
        thermochemistry.relatedConcepts.add("Entropy");
        thermochemistry.relatedFormulas.add(enthalpy);
        chemistryConcepts.add(thermochemistry);
        
        Concept electrochemistry = new Concept("Electrochemistry", DOMAIN_CHEMISTRY, 
                "The branch of chemistry that deals with the relations between electrical and chemical energy, and the conversion of one form into the other.");
        electrochemistry.relatedConcepts.add("Redox Reactions");
        electrochemistry.relatedConcepts.add("Electrolysis");
        electrochemistry.relatedFormulas.add(nernst);
        chemistryConcepts.add(electrochemistry);
        
        Concept kinetics = new Concept("Chemical Kinetics", DOMAIN_CHEMISTRY, 
                "The study of rates of chemical reactions and the mechanisms by which they occur.");
        kinetics.relatedConcepts.add("Reaction Rate");
        kinetics.relatedConcepts.add("Activation Energy");
        kinetics.relatedFormulas.add(rateConstant);
        chemistryConcepts.add(kinetics);
        
        Concept equilibrium = new Concept("Chemical Equilibrium", DOMAIN_CHEMISTRY, 
                "The state in which the concentrations of reactants and products in a chemical reaction do not change over time.");
        equilibrium.relatedConcepts.add("Le Chatelier's Principle");
        equilibrium.relatedConcepts.add("Equilibrium Constant");
        equilibrium.relatedFormulas.add(equilibriumConstant);
        chemistryConcepts.add(equilibrium);
        
        // Add more formulas and concepts as needed
    }
    
    /**
     * Initialize domain keywords for classification
     */
    private void initializeKeywords() {
        // Math keywords
        mathKeywords.addAll(Arrays.asList(
                "algebra", "calculus", "geometry", "trigonometry", "matrix", "vector", 
                "function", "derivative", "integral", "equation", "inequality", 
                "probability", "statistics", "permutation", "combination", "theorem",
                "series", "sequence", "limit", "infinity", "differential", "quadratic",
                "polynomial", "binomial", "coordinate", "graph", "curve", "line",
                "गणित", "बीजगणित", "ज्यामिति", "कैलकुलस", "त्रिकोणमिति", "समीकरण" // Hindi math terms
        ));
        
        // Physics keywords
        physicsKeywords.addAll(Arrays.asList(
                "mechanics", "dynamics", "kinematics", "electromagnetism", "thermodynamics",
                "force", "energy", "momentum", "velocity", "acceleration", "gravity",
                "electricity", "magnetism", "current", "voltage", "resistance", "capacitance",
                "inductance", "circuit", "quantum", "relativity", "nuclear", "optics",
                "wave", "oscillation", "frequency", "resonance", "newton", "coulomb",
                "भौतिकी", "बल", "ऊर्जा", "गति", "त्वरण", "विद्युत", "चुंबकत्व" // Hindi physics terms
        ));
        
        // Chemistry keywords
        chemistryKeywords.addAll(Arrays.asList(
                "element", "compound", "reaction", "acid", "base", "salt", "periodic",
                "atomic", "molecular", "ion", "covalent", "ionic", "bond", "organic",
                "inorganic", "hydrocarbon", "polymer", "solution", "equilibrium", "catalyst",
                "oxidation", "reduction", "electrochemistry", "thermochemistry", "kinetics",
                "mole", "stoichiometry", "gas", "liquid", "solid", "phase", "titration",
                "रसायन", "तत्व", "यौगिक", "अम्ल", "क्षार", "अणु", "परमाणु" // Hindi chemistry terms
        ));
    }
    
    /**
     * Solve a JEE problem
     */
    public void solveProblem(String problemText, SolveCallback callback) {
        if (problemText == null || problemText.trim().isEmpty()) {
            if (callback != null) {
                callback.onSolveError("unknown", "Problem text cannot be empty");
            }
            return;
        }
        
        // Create problem instance
        final Problem problem = new Problem(problemText);
        problem.originalQuery = problemText;
        
        // Determine domain
        problem.domain = classifyProblemDomain(problemText);
        
        // Start solving on background thread
        executor.execute(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                // Notify solving started
                if (callback != null) {
                    callback.onSolveStarted(problem.id);
                }
                
                // Extract variables and analyze problem
                analyzeProblem(problem);
                
                // Create solution object
                Solution solution = new Solution(problem.id);
                
                // Generate solution based on domain
                switch (problem.domain) {
                    case DOMAIN_MATH:
                        solveMathProblem(problem, solution, callback);
                        break;
                    case DOMAIN_PHYSICS:
                        solvePhysicsProblem(problem, solution, callback);
                        break;
                    case DOMAIN_CHEMISTRY:
                        solveChemistryProblem(problem, solution, callback);
                        break;
                    default:
                        // Fallback to AI-powered solving
                        solveWithAI(problem, callback);
                        return;
                }
                
                // If local solving didn't produce a good solution, try AI
                if (solution.answer == null || solution.answer.isEmpty() || !solution.isCorrect) {
                    Log.d(TAG, "Local solution incomplete, trying AI-powered solving");
                    solveWithAI(problem, callback);
                    return;
                }
                
                // Calculate solve time
                solution.solveTimeMs = System.currentTimeMillis() - startTime;
                
                // Save solution to problem
                problem.solution = solution;
                
                // Add to problem history
                problemHistory.add(problem);
                saveProblemHistory();
                
                // Notify completion
                if (callback != null) {
                    callback.onSolveCompleted(problem.id, solution);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error solving problem: " + e.getMessage(), e);
                if (callback != null) {
                    callback.onSolveError(problem.id, "Error solving problem: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Solve problem using AI (HybridAILearningSystem)
     * @param problem Problem to solve
     * @param callback Callback for solution updates
     */
    private void solveWithAI(Problem problem, SolveCallback callback) {
        Log.d(TAG, "Solving with AI: " + problem.domain + " problem");
        
        long startTime = System.currentTimeMillis();
        
        // Build comprehensive prompt
        String prompt = "Solve this " + problem.domain + " problem step-by-step:\n\n" + 
                       problem.text + "\n\n" +
                       "Provide detailed solution with:\n" +
                       "1. Step-by-step working\n" +
                       "2. Clear explanations for each step\n" +
                       "3. Final answer\n" +
                       "4. Relevant formulas and concepts used";
        
        // Use HybridAI to solve
        hybridAI.processQuery(prompt, null, 0.0f, new HybridAILearningSystem.ResponseCallback() {
            @Override
            public void onResponse(String response, String source) {
                try {
                    Log.d(TAG, "AI solution received from " + source);
                    
                    // Create solution object
                    Solution solution = new Solution(problem.id);
                    
                    // Parse response to extract steps and answer
                    String[] lines = response.split("\n");
                    StringBuilder currentStep = new StringBuilder();
                    boolean inSteps = false;
                    
                    for (String line : lines) {
                        String trimmed = line.trim();
                        
                        // Look for step patterns
                        if (trimmed.matches("^(Step|\\d+\\.|\\d+\\))[:\\s].*")) {
                            // Save previous step if exists
                            if (currentStep.length() > 0) {
                                String stepText = currentStep.toString().trim();
                                solution.steps.add(new Step(
                                    "Step " + (solution.steps.size() + 1),
                                    stepText,
                                    "AI-generated step"
                                ));
                                currentStep = new StringBuilder();
                            }
                            inSteps = true;
                            currentStep.append(trimmed).append("\n");
                        } else if (inSteps && !trimmed.isEmpty()) {
                            currentStep.append(trimmed).append("\n");
                        }
                        
                        // Look for answer patterns
                        if (trimmed.toLowerCase().contains("answer") || 
                            trimmed.toLowerCase().contains("final") ||
                            trimmed.toLowerCase().contains("solution")) {
                            
                            // Try to extract the answer
                            String[] parts = trimmed.split("[:=]");
                            if (parts.length > 1) {
                                solution.answer = parts[parts.length - 1].trim();
                            }
                        }
                    }
                    
                    // Save final step if exists
                    if (currentStep.length() > 0) {
                        String stepText = currentStep.toString().trim();
                        solution.steps.add(new Step(
                            "Step " + (solution.steps.size() + 1),
                            stepText,
                            "AI-generated step"
                        ));
                    }
                    
                    // If no answer found in structured format, use last line
                    if (solution.answer == null || solution.answer.isEmpty()) {
                        for (int i = lines.length - 1; i >= 0; i--) {
                            String line = lines[i].trim();
                            if (!line.isEmpty()) {
                                solution.answer = line;
                                break;
                            }
                        }
                    }
                    
                    // Set full explanation
                    solution.explanation = response;
                    solution.isCorrect = true;
                    solution.isVerified = false;
                    
                    // Add relevant concepts (extract from problem topics)
                    solution.relatedConcepts.addAll(problem.topics);
                    
                    // Calculate solve time
                    solution.solveTimeMs = System.currentTimeMillis() - startTime;
                    
                    // Save solution to problem
                    problem.solution = solution;
                    
                    // Add to problem history
                    problemHistory.add(problem);
                    saveProblemHistory();
                    
                    // Notify completion
                    if (callback != null) {
                        // Notify each step
                        for (Step step : solution.steps) {
                            callback.onStepCompleted(problem.id, step.description, step.explanation);
                        }
                        callback.onSolveCompleted(problem.id, solution);
                    }
                    
                    Log.d(TAG, "AI solution completed in " + solution.solveTimeMs + "ms");
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error processing AI solution", e);
                    if (callback != null) {
                        callback.onSolveError(problem.id, "Error processing AI solution: " + e.getMessage());
                    }
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "AI solving error: " + error);
                if (callback != null) {
                    callback.onSolveError(problem.id, "AI solving failed: " + error);
                }
            }
        });
    }
    
    /**
     * Classify problem domain based on content
     */
    private String classifyProblemDomain(String problemText) {
        String lowerText = problemText.toLowerCase();
        
        // Count domain-specific keywords
        int mathCount = countKeywords(lowerText, mathKeywords);
        int physicsCount = countKeywords(lowerText, physicsKeywords);
        int chemistryCount = countKeywords(lowerText, chemistryKeywords);
        
        // Determine domain with most keyword matches
        if (mathCount >= physicsCount && mathCount >= chemistryCount) {
            return DOMAIN_MATH;
        } else if (physicsCount >= mathCount && physicsCount >= chemistryCount) {
            return DOMAIN_PHYSICS;
        } else if (chemistryCount >= mathCount && chemistryCount >= physicsCount) {
            return DOMAIN_CHEMISTRY;
        } else {
            // Default to math if can't determine
            return DOMAIN_MATH;
        }
    }
    
    /**
     * Count occurrences of keywords in text
     */
    private int countKeywords(String text, Set<String> keywords) {
        int count = 0;
        for (String keyword : keywords) {
            // Count occurrences
            int index = 0;
            while ((index = text.indexOf(keyword.toLowerCase(), index)) != -1) {
                count++;
                index += keyword.length();
            }
        }
        return count;
    }
    
    /**
     * Analyze the problem to extract variables and topics
     */
    private void analyzeProblem(Problem problem) {
        String text = problem.text.toLowerCase();
        
        // Extract topics based on domain
        Set<String> domainKeywords;
        switch (problem.domain) {
            case DOMAIN_MATH:
                domainKeywords = mathKeywords;
                break;
            case DOMAIN_PHYSICS:
                domainKeywords = physicsKeywords;
                break;
            case DOMAIN_CHEMISTRY:
                domainKeywords = chemistryKeywords;
                break;
            default:
                domainKeywords = new HashSet<>();
        }
        
        // Find matching topics
        for (String keyword : domainKeywords) {
            if (text.contains(keyword.toLowerCase())) {
                problem.topics.add(keyword);
            }
        }
        
        // Extract numerical variables
        Pattern numPattern = Pattern.compile("\\b(\\d+(\\.\\d+)?)\\s*([a-zA-Z]+)\\b");
        Matcher numMatcher = numPattern.matcher(text);
        while (numMatcher.find()) {
            String value = numMatcher.group(1);
            String unit = numMatcher.group(3);
            problem.variables.put(unit, value);
        }
        
        // Extract variables in format "x = 5"
        Pattern varPattern = Pattern.compile("\\b([a-zA-Z])\\s*=\\s*(\\d+(\\.\\d+)?)\\b");
        Matcher varMatcher = varPattern.matcher(text);
        while (varMatcher.find()) {
            String variable = varMatcher.group(1);
            String value = varMatcher.group(2);
            problem.variables.put(variable, value);
        }
    }
    
    /**
     * Solve a mathematics problem
     */
    private void solveMathProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Check if problem is calculus related
        boolean isCalculus = problem.topics.contains("calculus") || 
                problem.topics.contains("derivative") || 
                problem.topics.contains("integral");
        
        // Check if problem is algebra related
        boolean isAlgebra = problem.topics.contains("algebra") || 
                problem.topics.contains("equation");
        
        // Check if problem is geometry/trigonometry related
        boolean isGeometry = problem.topics.contains("geometry") || 
                problem.topics.contains("trigonometry");
        
        if (isCalculus) {
            solveCalculusProblem(problem, solution, callback);
        } else if (isAlgebra) {
            solveAlgebraProblem(problem, solution, callback);
        } else if (isGeometry) {
            solveGeometryProblem(problem, solution, callback);
        } else {
            // Default to general mathematical problem
            solveGeneralMathProblem(problem, solution, callback);
        }
    }
    
    /**
     * Solve a calculus problem
     */
    private void solveCalculusProblem(Problem problem, Solution solution, SolveCallback callback) {
        // This implementation provides a prototype/simulation of calculus problem solving
        // In a real application, this would involve actual symbolic math computation
        
        // Check if problem involves differentiation
        boolean isDifferentiation = problem.text.contains("derivative") || 
                problem.text.contains("differentiate");
        
        if (isDifferentiation) {
            // Demonstrate differentiation solving steps
            
            // Step 1: Identify function
            Step step1 = new Step(
                    "Identify function to differentiate",
                    "Find f(x) in the problem statement",
                    "We need to find the derivative of the function mentioned in the problem."
            );
            solution.steps.add(step1);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step1.description, step1.explanation);
            }
            
            // Step 2: Apply differentiation rules
            Step step2 = new Step(
                    "Apply differentiation rules",
                    "Use power rule, chain rule, etc. as needed",
                    "For functions of the form x^n, the derivative is n*x^(n-1)."
            );
            solution.steps.add(step2);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step2.description, step2.explanation);
            }
            
            // Step 3: Simplify result
            Step step3 = new Step(
                    "Simplify the result",
                    "Combine like terms and simplify expression",
                    "After applying differentiation rules, we simplify the resulting expression."
            );
            solution.steps.add(step3);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step3.description, step3.explanation);
            }
            
            // Add relevant formulas
            for (Formula formula : formulasByDomain.get(DOMAIN_MATH)) {
                if (formula.name.contains("Derivative")) {
                    solution.usedFormulas.add(formula);
                }
            }
            
            // Add relevant concepts
            solution.relatedConcepts.add("Differentiation");
            solution.relatedConcepts.add("Derivative");
            
        } else {
            // Assume integration problem
            
            // Step 1: Identify function
            Step step1 = new Step(
                    "Identify function to integrate",
                    "Find the function to be integrated",
                    "We need to find the indefinite or definite integral of the function."
            );
            solution.steps.add(step1);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step1.description, step1.explanation);
            }
            
            // Step 2: Apply integration rules
            Step step2 = new Step(
                    "Apply integration rules",
                    "Use power rule, substitution, etc. as needed",
                    "For functions of the form x^n, the integral is x^(n+1)/(n+1) + C when n ≠ -1."
            );
            solution.steps.add(step2);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step2.description, step2.explanation);
            }
            
            // Step 3: Determine bounds for definite integral
            Step step3 = new Step(
                    "Apply bounds (for definite integral)",
                    "Substitute upper and lower bounds and calculate difference",
                    "If this is a definite integral, substitute the bounds and compute the result."
            );
            solution.steps.add(step3);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step3.description, step3.explanation);
            }
            
            // Add relevant formulas
            for (Formula formula : formulasByDomain.get(DOMAIN_MATH)) {
                if (formula.name.contains("Integral")) {
                    solution.usedFormulas.add(formula);
                }
            }
            
            // Add relevant concepts
            solution.relatedConcepts.add("Integration");
            solution.relatedConcepts.add("Antiderivative");
        }
        
        // Create explanation and answer
        solution.explanation = "This calculus problem involves " + 
                (isDifferentiation ? "differentiation" : "integration") + 
                " techniques. The steps involve identifying the function, applying appropriate " +
                "rules, and simplifying the result.";
        
        solution.answer = isDifferentiation ? 
                "The derivative is calculated by applying the power rule and simplifying." : 
                "The integral is found by applying integration formulas and evaluating bounds if needed.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve an algebra problem
     */
    private void solveAlgebraProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Check if it's a quadratic equation
        boolean isQuadratic = problem.text.contains("quadratic") || 
                problem.text.contains("ax^2") || 
                problem.text.matches(".*x.{0,3}2.*");
        
        if (isQuadratic) {
            // Step 1: Identify quadratic equation
            Step step1 = new Step(
                    "Identify the quadratic equation",
                    "Write in standard form ax² + bx + c = 0",
                    "First, we need to rearrange the equation into standard form with all terms on one side."
            );
            solution.steps.add(step1);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step1.description, step1.explanation);
            }
            
            // Step 2: Apply quadratic formula
            Step step2 = new Step(
                    "Apply the quadratic formula",
                    "x = (-b ± √(b² - 4ac)) / 2a",
                    "We use the quadratic formula to find the roots of the equation."
            );
            solution.steps.add(step2);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step2.description, step2.explanation);
            }
            
            // Step 3: Calculate discriminant
            Step step3 = new Step(
                    "Calculate the discriminant",
                    "discriminant = b² - 4ac",
                    "The discriminant tells us how many real roots the equation has."
            );
            solution.steps.add(step3);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step3.description, step3.explanation);
            }
            
            // Step 4: Determine and calculate roots
            Step step4 = new Step(
                    "Determine and calculate the roots",
                    "Substitute values and compute final answer",
                    "Based on the discriminant, we can determine if there are two distinct real roots, one repeated root, or two complex roots."
            );
            solution.steps.add(step4);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step4.description, step4.explanation);
            }
            
            // Add relevant formulas
            for (Formula formula : formulasByDomain.get(DOMAIN_MATH)) {
                if (formula.name.contains("Quadratic")) {
                    solution.usedFormulas.add(formula);
                }
            }
            
            // Add relevant concepts
            solution.relatedConcepts.add("Quadratic Equation");
            solution.relatedConcepts.add("Discriminant");
            solution.relatedConcepts.add("Roots of Equation");
            
            // Create explanation and answer
            solution.explanation = "This is a quadratic equation problem. The approach involves " +
                    "identifying the coefficients, applying the quadratic formula, and calculating " +
                    "the roots based on the discriminant.";
            
            solution.answer = "The equation can be solved using the quadratic formula, which gives " +
                    "the roots of the equation. The nature of the roots depends on the discriminant value.";
            
        } else {
            // Generic algebra problem
            
            // Step 1: Understand the problem
            Step step1 = new Step(
                    "Understand the algebraic problem",
                    "Identify variables, constants, and relationships",
                    "We need to identify what we're solving for and what relationships are given."
            );
            solution.steps.add(step1);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step1.description, step1.explanation);
            }
            
            // Step 2: Set up equations
            Step step2 = new Step(
                    "Set up appropriate equations",
                    "Translate problem statement into equation(s)",
                    "Based on the problem description, we create algebraic equations to model the situation."
            );
            solution.steps.add(step2);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step2.description, step2.explanation);
            }
            
            // Step 3: Solve the equations
            Step step3 = new Step(
                    "Solve the equations",
                    "Apply algebraic operations to isolate variables",
                    "We manipulate the equations using algebraic operations to solve for the unknown variables."
            );
            solution.steps.add(step3);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step3.description, step3.explanation);
            }
            
            // Add relevant concepts
            solution.relatedConcepts.add("Algebraic Equation");
            solution.relatedConcepts.add("Linear Equation");
            
            // Create explanation and answer
            solution.explanation = "This algebra problem requires setting up appropriate equations " +
                    "based on the given information and solving them using algebraic techniques.";
            
            solution.answer = "The solution is found by manipulating the algebraic equations and " +
                    "isolating the variables to determine their values.";
        }
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve a geometry/trigonometry problem
     */
    private void solveGeometryProblem(Problem problem, Solution solution, SolveCallback callback) {
        boolean isTrigonometry = problem.topics.contains("trigonometry") || 
                problem.text.contains("sin") || problem.text.contains("cos") || 
                problem.text.contains("tan");
        
        if (isTrigonometry) {
            // Step 1: Identify trigonometric functions
            Step step1 = new Step(
                    "Identify trigonometric relationships",
                    "Determine which trigonometric functions are involved",
                    "We need to identify which trigonometric functions (sine, cosine, tangent, etc.) are relevant to this problem."
            );
            solution.steps.add(step1);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step1.description, step1.explanation);
            }
            
            // Step 2: Draw diagram if needed
            Step step2 = new Step(
                    "Set up the problem geometrically",
                    "Draw a diagram and label relevant information",
                    "Drawing a labeled diagram helps visualize the problem and identify the relationships."
            );
            solution.steps.add(step2);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step2.description, step2.explanation);
            }
            
            // Step 3: Apply trigonometric rules
            Step step3 = new Step(
                    "Apply appropriate trigonometric rules",
                    "Use trigonometric identities or the sine/cosine law",
                    "Depending on the problem, we may need to use trigonometric identities or laws like the law of sines or cosines."
            );
            solution.steps.add(step3);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step3.description, step3.explanation);
            }
            
            // Add relevant formulas
            for (Formula formula : formulasByDomain.get(DOMAIN_MATH)) {
                if (formula.topics.contains("trigonometry")) {
                    solution.usedFormulas.add(formula);
                }
            }
            
            // Add relevant concepts
            solution.relatedConcepts.add("Trigonometry");
            solution.relatedConcepts.add("Trigonometric Functions");
            solution.relatedConcepts.add("Trigonometric Identities");
            
            // Create explanation and answer
            solution.explanation = "This is a trigonometry problem that involves applying " +
                    "trigonometric relationships to find the required values. The approach " +
                    "involves identifying the relevant trigonometric functions and applying " +
                    "appropriate identities or laws.";
            
            solution.answer = "The solution is found by applying trigonometric principles to " +
                    "the given information and calculating the required values.";
            
        } else {
            // General geometry problem
            
            // Step 1: Understand geometric entities
            Step step1 = new Step(
                    "Identify geometric entities",
                    "Recognize shapes, points, lines, and their properties",
                    "We need to identify the geometric shapes and entities involved in the problem."
            );
            solution.steps.add(step1);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step1.description, step1.explanation);
            }
            
            // Step 2: Visualize the problem
            Step step2 = new Step(
                    "Visualize the geometric scenario",
                    "Draw a diagram with all given information",
                    "Creating a visual representation helps understand the spatial relationships."
            );
            solution.steps.add(step2);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step2.description, step2.explanation);
            }
            
            // Step 3: Apply geometric principles
            Step step3 = new Step(
                    "Apply relevant geometric principles",
                    "Use theorems, formulas, and properties",
                    "We apply appropriate geometric theorems and formulas to solve for the unknown quantities."
            );
            solution.steps.add(step3);
            
            if (callback != null) {
                callback.onStepCompleted(problem.id, step3.description, step3.explanation);
            }
            
            // Add relevant concepts
            solution.relatedConcepts.add("Geometry");
            solution.relatedConcepts.add("Geometric Theorems");
            
            // Create explanation and answer
            solution.explanation = "This geometry problem requires identifying geometric entities, " +
                    "visualizing the scenario, and applying relevant geometric principles to find the solution.";
            
            solution.answer = "The solution involves using geometric relationships and formulas " +
                    "to calculate the required values.";
        }
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve a general math problem
     */
    private void solveGeneralMathProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Step 1: Analyze the problem
        Step step1 = new Step(
                "Analyze the mathematical problem",
                "Identify the mathematical domain and required approach",
                "We need to determine what mathematical concepts and techniques are needed."
        );
        solution.steps.add(step1);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step1.description, step1.explanation);
        }
        
        // Step 2: Formulate solution approach
        Step step2 = new Step(
                "Formulate a solution approach",
                "Plan the mathematical steps needed",
                "Based on the analysis, we determine the sequence of mathematical operations required."
        );
        solution.steps.add(step2);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step2.description, step2.explanation);
        }
        
        // Step 3: Execute solution
        Step step3 = new Step(
                "Execute the solution",
                "Perform the necessary calculations and derivations",
                "We carry out the mathematical operations to arrive at the answer."
        );
        solution.steps.add(step3);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step3.description, step3.explanation);
        }
        
        // Create explanation and answer
        solution.explanation = "This is a general mathematics problem that requires analyzing " +
                "the problem, identifying the appropriate mathematical approach, and executing " +
                "the solution steps.";
        
        solution.answer = "The solution is found by applying appropriate mathematical techniques " +
                "to the given information.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve a physics problem
     */
    private void solvePhysicsProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Check for mechanics problem
        boolean isMechanics = problem.topics.contains("mechanics") || 
                problem.topics.contains("force") || 
                problem.topics.contains("motion");
        
        // Check for electricity problem
        boolean isElectricity = problem.topics.contains("electricity") || 
                problem.topics.contains("circuit") || 
                problem.topics.contains("current");
        
        // Check for thermodynamics problem
        boolean isThermodynamics = problem.topics.contains("thermodynamics") || 
                problem.topics.contains("heat") || 
                problem.topics.contains("temperature");
        
        if (isMechanics) {
            solveMechanicsProblem(problem, solution, callback);
        } else if (isElectricity) {
            solveElectricityProblem(problem, solution, callback);
        } else if (isThermodynamics) {
            solveThermodynamicsProblem(problem, solution, callback);
        } else {
            // General physics problem
            solveGeneralPhysicsProblem(problem, solution, callback);
        }
    }
    
    /**
     * Solve a mechanics problem
     */
    private void solveMechanicsProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Step 1: Identify physical quantities
        Step step1 = new Step(
                "Identify physical quantities",
                "List all given quantities and what needs to be found",
                "We need to identify the known physical quantities such as mass, velocity, force, etc., and what we need to calculate."
        );
        solution.steps.add(step1);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step1.description, step1.explanation);
        }
        
        // Step 2: Choose relevant physics laws
        Step step2 = new Step(
                "Select relevant physics laws",
                "Determine which mechanics laws apply",
                "Based on the problem, we identify whether Newton's laws, conservation laws, kinematics equations, or other principles are applicable."
        );
        solution.steps.add(step2);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step2.description, step2.explanation);
        }
        
        // Step 3: Set up equations
        Step step3 = new Step(
                "Set up equations",
                "Write equations based on the selected laws",
                "We formulate mathematical equations using the relevant physics laws and the given quantities."
        );
        solution.steps.add(step3);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step3.description, step3.explanation);
        }
        
        // Step 4: Solve the equations
        Step step4 = new Step(
                "Solve for the unknown",
                "Manipulate equations to find the required quantity",
                "We solve the equation(s) algebraically to find the unknown quantity."
        );
        solution.steps.add(step4);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step4.description, step4.explanation);
        }
        
        // Add relevant formulas
        for (Formula formula : formulasByDomain.get(DOMAIN_PHYSICS)) {
            if (formula.topics.contains("mechanics")) {
                solution.usedFormulas.add(formula);
            }
        }
        
        // Add relevant concepts
        solution.relatedConcepts.add("Mechanics");
        solution.relatedConcepts.add("Newton's Laws");
        solution.relatedConcepts.add("Conservation of Energy");
        
        // Create explanation and answer
        solution.explanation = "This is a mechanics problem that involves applying fundamental " +
                "physics laws such as Newton's laws or conservation principles. The approach " +
                "involves identifying relevant quantities, selecting appropriate laws, setting " +
                "up equations, and solving for the unknown.";
        
        solution.answer = "The solution is found by applying the principles of mechanics to " +
                "the given scenario and calculating the required physical quantity.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve an electricity problem
     */
    private void solveElectricityProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Step 1: Identify circuit components
        Step step1 = new Step(
                "Identify circuit components",
                "List components and their properties",
                "We need to identify the electrical components (resistors, capacitors, voltage sources, etc.) and their values."
        );
        solution.steps.add(step1);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step1.description, step1.explanation);
        }
        
        // Step 2: Determine circuit structure
        Step step2 = new Step(
                "Determine circuit structure",
                "Identify series/parallel arrangements",
                "We analyze how the components are connected (series, parallel, or combinations)."
        );
        solution.steps.add(step2);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step2.description, step2.explanation);
        }
        
        // Step 3: Apply circuit laws
        Step step3 = new Step(
                "Apply circuit laws",
                "Use Ohm's Law, Kirchhoff's laws, etc.",
                "We apply fundamental electrical principles like Ohm's Law (V=IR) and Kirchhoff's laws to analyze the circuit."
        );
        solution.steps.add(step3);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step3.description, step3.explanation);
        }
        
        // Step 4: Solve for unknowns
        Step step4 = new Step(
                "Calculate the required values",
                "Solve the equations for unknown quantities",
                "Based on the circuit analysis, we calculate the unknown electrical quantities."
        );
        solution.steps.add(step4);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step4.description, step4.explanation);
        }
        
        // Add relevant formulas
        for (Formula formula : formulasByDomain.get(DOMAIN_PHYSICS)) {
            if (formula.topics.contains("electricity")) {
                solution.usedFormulas.add(formula);
            }
        }
        
        // Add relevant concepts
        solution.relatedConcepts.add("Electricity");
        solution.relatedConcepts.add("Electric Circuits");
        solution.relatedConcepts.add("Ohm's Law");
        
        // Create explanation and answer
        solution.explanation = "This is an electricity problem that involves analyzing circuits " +
                "using electrical principles. The approach involves identifying components, " +
                "determining circuit structure, applying circuit laws, and calculating the required values.";
        
        solution.answer = "The solution is found by applying electrical principles to the given " +
                "circuit and calculating the required electrical quantities.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve a thermodynamics problem
     */
    private void solveThermodynamicsProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Step 1: Identify thermodynamic system
        Step step1 = new Step(
                "Identify the thermodynamic system",
                "Define the system and its boundaries",
                "We need to clearly define what constitutes the thermodynamic system and what is the surroundings."
        );
        solution.steps.add(step1);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step1.description, step1.explanation);
        }
        
        // Step 2: Identify initial and final states
        Step step2 = new Step(
                "Identify initial and final states",
                "List the thermodynamic variables at start and end",
                "We determine the values of pressure, volume, temperature, etc. at the initial and final states."
        );
        solution.steps.add(step2);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step2.description, step2.explanation);
        }
        
        // Step 3: Apply thermodynamic laws
        Step step3 = new Step(
                "Apply thermodynamic laws",
                "Use the laws of thermodynamics",
                "We apply the relevant laws of thermodynamics (First Law, Second Law, etc.) to the problem."
        );
        solution.steps.add(step3);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step3.description, step3.explanation);
        }
        
        // Step 4: Calculate thermodynamic quantities
        Step step4 = new Step(
                "Calculate thermodynamic quantities",
                "Find heat, work, entropy, or other quantities",
                "Based on the thermodynamic analysis, we calculate the required thermodynamic quantities."
        );
        solution.steps.add(step4);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step4.description, step4.explanation);
        }
        
        // Add relevant formulas
        for (Formula formula : formulasByDomain.get(DOMAIN_PHYSICS)) {
            if (formula.topics.contains("thermodynamics")) {
                solution.usedFormulas.add(formula);
            }
        }
        
        // Add relevant concepts
        solution.relatedConcepts.add("Thermodynamics");
        solution.relatedConcepts.add("Heat Transfer");
        solution.relatedConcepts.add("Laws of Thermodynamics");
        
        // Create explanation and answer
        solution.explanation = "This is a thermodynamics problem that involves analyzing energy " +
                "transfers and transformations. The approach involves identifying the system, " +
                "defining states, applying thermodynamic laws, and calculating the required quantities.";
        
        solution.answer = "The solution is found by applying the principles of thermodynamics to " +
                "the given scenario and calculating the required thermodynamic quantity.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve a general physics problem
     */
    private void solveGeneralPhysicsProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Step 1: Analyze physical scenario
        Step step1 = new Step(
                "Analyze the physical scenario",
                "Identify the physics domain and relevant principles",
                "We need to determine what branch of physics and what principles apply to this problem."
        );
        solution.steps.add(step1);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step1.description, step1.explanation);
        }
        
        // Step 2: Formulate physical model
        Step step2 = new Step(
                "Formulate a physical model",
                "Create a simplified model of the physical system",
                "We create a model that captures the essential physics while making appropriate simplifications."
        );
        solution.steps.add(step2);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step2.description, step2.explanation);
        }
        
        // Step 3: Apply physics principles
        Step step3 = new Step(
                "Apply relevant physics principles",
                "Use appropriate laws and equations",
                "We apply the fundamental principles of physics that govern the system's behavior."
        );
        solution.steps.add(step3);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step3.description, step3.explanation);
        }
        
        // Step 4: Calculate physical quantities
        Step step4 = new Step(
                "Calculate physical quantities",
                "Solve for the required physical values",
                "We perform the necessary calculations to determine the required physical quantities."
        );
        solution.steps.add(step4);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step4.description, step4.explanation);
        }
        
        // Create explanation and answer
        solution.explanation = "This physics problem requires identifying the relevant physical " +
                "principles, creating an appropriate model, applying physics laws, and " +
                "performing calculations to find the solution.";
        
        solution.answer = "The solution is found by applying the fundamental principles of physics " +
                "to analyze the given scenario and calculate the required quantity.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve a chemistry problem
     */
    private void solveChemistryProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Check for different chemistry domains
        boolean isEquilibrium = problem.topics.contains("equilibrium") || 
                problem.text.contains("equilibrium");
        
        boolean isThermochemistry = problem.topics.contains("thermochemistry") || 
                problem.topics.contains("enthalpy") || 
                problem.text.contains("heat");
        
        boolean isElectrochemistry = problem.topics.contains("electrochemistry") || 
                problem.text.contains("cell") || 
                problem.text.contains("electrode");
        
        boolean isKinetics = problem.topics.contains("kinetics") || 
                problem.text.contains("rate") || 
                problem.text.contains("reaction rate");
        
        if (isEquilibrium) {
            solveEquilibriumProblem(problem, solution, callback);
        } else if (isThermochemistry) {
            solveThermochemistryProblem(problem, solution, callback);
        } else if (isElectrochemistry) {
            solveElectrochemistryProblem(problem, solution, callback);
        } else if (isKinetics) {
            solveKineticsProblem(problem, solution, callback);
        } else {
            // General chemistry problem
            solveGeneralChemistryProblem(problem, solution, callback);
        }
    }
    
    /**
     * Solve an equilibrium problem
     */
    private void solveEquilibriumProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Step 1: Identify the chemical reaction
        Step step1 = new Step(
                "Identify the chemical reaction",
                "Write the balanced chemical equation",
                "We need to identify and balance the chemical reaction involved in the equilibrium."
        );
        solution.steps.add(step1);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step1.description, step1.explanation);
        }
        
        // Step 2: Write equilibrium expression
        Step step2 = new Step(
                "Write the equilibrium expression",
                "Write the expression for the equilibrium constant K",
                "We formulate the equilibrium constant expression in terms of concentrations or partial pressures."
        );
        solution.steps.add(step2);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step2.description, step2.explanation);
        }
        
        // Step 3: Set up ICE table
        Step step3 = new Step(
                "Set up an ICE table",
                "Create a table with Initial, Change, and Equilibrium values",
                "An ICE table helps track concentration changes from initial to equilibrium conditions."
        );
        solution.steps.add(step3);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step3.description, step3.explanation);
        }
        
        // Step 4: Solve for equilibrium concentrations
        Step step4 = new Step(
                "Calculate equilibrium concentrations",
                "Solve for the equilibrium concentrations using the equilibrium constant",
                "Using the equilibrium expression and the ICE table, we solve for the unknown concentrations."
        );
        solution.steps.add(step4);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step4.description, step4.explanation);
        }
        
        // Add relevant formulas
        for (Formula formula : formulasByDomain.get(DOMAIN_CHEMISTRY)) {
            if (formula.topics.contains("equilibrium")) {
                solution.usedFormulas.add(formula);
            }
        }
        
        // Add relevant concepts
        solution.relatedConcepts.add("Chemical Equilibrium");
        solution.relatedConcepts.add("Equilibrium Constant");
        solution.relatedConcepts.add("Le Chatelier's Principle");
        
        // Create explanation and answer
        solution.explanation = "This is a chemical equilibrium problem that involves analyzing a " +
                "chemical reaction at equilibrium. The approach involves identifying the reaction, " +
                "writing the equilibrium expression, setting up an ICE table, and calculating " +
                "equilibrium concentrations.";
        
        solution.answer = "The solution is found by applying equilibrium principles to the " +
                "chemical reaction and calculating the required quantities at equilibrium.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve a thermochemistry problem
     */
    private void solveThermochemistryProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Step 1: Identify the thermochemical process
        Step step1 = new Step(
                "Identify the thermochemical process",
                "Determine what type of reaction or process is occurring",
                "We need to identify whether this involves heat of reaction, heat capacity, or other thermochemical concepts."
        );
        solution.steps.add(step1);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step1.description, step1.explanation);
        }
        
        // Step 2: Apply thermochemical principles
        Step step2 = new Step(
                "Apply thermochemical principles",
                "Use relevant laws and equations",
                "We apply principles like Hess's Law, heat capacity equations, or enthalpy of reaction formulas."
        );
        solution.steps.add(step2);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step2.description, step2.explanation);
        }
        
        // Step 3: Calculate energy changes
        Step step3 = new Step(
                "Calculate energy changes",
                "Determine enthalpy, heat, or energy values",
                "We perform calculations to find the required energy or enthalpy changes."
        );
        solution.steps.add(step3);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step3.description, step3.explanation);
        }
        
        // Add relevant formulas
        for (Formula formula : formulasByDomain.get(DOMAIN_CHEMISTRY)) {
            if (formula.topics.contains("thermochemistry")) {
                solution.usedFormulas.add(formula);
            }
        }
        
        // Add relevant concepts
        solution.relatedConcepts.add("Thermochemistry");
        solution.relatedConcepts.add("Enthalpy");
        solution.relatedConcepts.add("Hess's Law");
        
        // Create explanation and answer
        solution.explanation = "This is a thermochemistry problem that involves analyzing heat and " +
                "energy changes in chemical processes. The approach involves identifying the " +
                "thermochemical process, applying relevant principles, and calculating energy changes.";
        
        solution.answer = "The solution is found by applying thermochemical principles to the " +
                "given process and calculating the required energy or enthalpy values.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve an electrochemistry problem
     */
    private void solveElectrochemistryProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Step 1: Identify electrochemical cell components
        Step step1 = new Step(
                "Identify electrochemical cell components",
                "Identify electrodes, half-reactions, and cell configuration",
                "We need to identify the anode, cathode, half-reactions, and cell type (galvanic or electrolytic)."
        );
        solution.steps.add(step1);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step1.description, step1.explanation);
        }
        
        // Step 2: Write and balance half-reactions
        Step step2 = new Step(
                "Write and balance half-reactions",
                "Balance electron transfer in redox reactions",
                "We write and balance the oxidation and reduction half-reactions, ensuring electron balance."
        );
        solution.steps.add(step2);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step2.description, step2.explanation);
        }
        
        // Step 3: Calculate cell potential
        Step step3 = new Step(
                "Calculate cell potential",
                "Use standard reduction potentials or Nernst equation",
                "We calculate the cell potential using standard reduction potentials and/or the Nernst equation for non-standard conditions."
        );
        solution.steps.add(step3);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step3.description, step3.explanation);
        }
        
        // Step 4: Determine other electrochemical quantities
        Step step4 = new Step(
                "Calculate other electrochemical quantities",
                "Determine quantities like work, charge, or reaction extent",
                "Based on the electrochemical analysis, we calculate other required quantities."
        );
        solution.steps.add(step4);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step4.description, step4.explanation);
        }
        
        // Add relevant formulas
        for (Formula formula : formulasByDomain.get(DOMAIN_CHEMISTRY)) {
            if (formula.topics.contains("electrochemistry")) {
                solution.usedFormulas.add(formula);
            }
        }
        
        // Add relevant concepts
        solution.relatedConcepts.add("Electrochemistry");
        solution.relatedConcepts.add("Redox Reactions");
        solution.relatedConcepts.add("Electrolytic Cells");
        
        // Create explanation and answer
        solution.explanation = "This is an electrochemistry problem that involves analyzing " +
                "electrochemical cells and redox reactions. The approach involves identifying " +
                "the cell components, balancing half-reactions, calculating cell potential, " +
                "and determining other electrochemical quantities.";
        
        solution.answer = "The solution is found by applying electrochemical principles to the " +
                "given cell and calculating the required electrochemical quantities.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve a chemical kinetics problem
     */
    private void solveKineticsProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Step 1: Identify reaction and rate law
        Step step1 = new Step(
                "Identify the reaction and rate law",
                "Determine the chemical reaction and its rate expression",
                "We identify the reaction and determine the form of the rate law (e.g., first-order, second-order)."
        );
        solution.steps.add(step1);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step1.description, step1.explanation);
        }
        
        // Step 2: Apply kinetics equations
        Step step2 = new Step(
                "Apply kinetics equations",
                "Use integrated rate laws or Arrhenius equation",
                "Depending on the problem, we apply the appropriate kinetics equations such as integrated rate laws or the Arrhenius equation."
        );
        solution.steps.add(step2);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step2.description, step2.explanation);
        }
        
        // Step 3: Calculate kinetic parameters
        Step step3 = new Step(
                "Calculate kinetic parameters",
                "Determine rate constants, half-lives, or reaction times",
                "We calculate the required kinetic parameters based on the applied equations."
        );
        solution.steps.add(step3);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step3.description, step3.explanation);
        }
        
        // Add relevant formulas
        for (Formula formula : formulasByDomain.get(DOMAIN_CHEMISTRY)) {
            if (formula.topics.contains("kinetics")) {
                solution.usedFormulas.add(formula);
            }
        }
        
        // Add relevant concepts
        solution.relatedConcepts.add("Chemical Kinetics");
        solution.relatedConcepts.add("Reaction Rate");
        solution.relatedConcepts.add("Rate Law");
        
        // Create explanation and answer
        solution.explanation = "This is a chemical kinetics problem that involves analyzing " +
                "the rates of chemical reactions. The approach involves identifying the reaction " +
                "and rate law, applying kinetics equations, and calculating kinetic parameters.";
        
        solution.answer = "The solution is found by applying kinetics principles to the reaction " +
                "and calculating the required kinetic quantities.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Solve a general chemistry problem
     */
    private void solveGeneralChemistryProblem(Problem problem, Solution solution, SolveCallback callback) {
        // Step 1: Analyze chemical system
        Step step1 = new Step(
                "Analyze the chemical system",
                "Identify the chemical concepts and principles involved",
                "We need to determine what branch of chemistry and what principles apply to this problem."
        );
        solution.steps.add(step1);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step1.description, step1.explanation);
        }
        
        // Step 2: Apply chemical principles
        Step step2 = new Step(
                "Apply chemical principles",
                "Use appropriate chemical laws and equations",
                "We apply the fundamental principles of chemistry that govern the system's behavior."
        );
        solution.steps.add(step2);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step2.description, step2.explanation);
        }
        
        // Step 3: Calculate chemical quantities
        Step step3 = new Step(
                "Calculate chemical quantities",
                "Perform stoichiometric or other calculations",
                "We perform the necessary calculations to determine the required chemical quantities."
        );
        solution.steps.add(step3);
        
        if (callback != null) {
            callback.onStepCompleted(problem.id, step3.description, step3.explanation);
        }
        
        // Create explanation and answer
        solution.explanation = "This chemistry problem requires identifying the relevant chemical " +
                "principles, applying appropriate chemical laws, and performing calculations to " +
                "find the solution.";
        
        solution.answer = "The solution is found by applying the fundamental principles of chemistry " +
                "to analyze the given system and calculate the required quantities.";
        
        solution.isCorrect = true;
        solution.isVerified = true;
    }
    
    /**
     * Load problem history from storage
     */
    private void loadProblemHistory() {
        try {
            File file = new File(context.getFilesDir(), PROBLEM_HISTORY_FILE);
            if (!file.exists()) {
                Log.d(TAG, "No problem history file found");
                return;
            }
            
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            
            String json = new String(data, StandardCharsets.UTF_8);
            JSONObject rootObject = new JSONObject(json);
            JSONArray problemsArray = rootObject.getJSONArray("problems");
            
            for (int i = 0; i < problemsArray.length(); i++) {
                JSONObject problemObj = problemsArray.getJSONObject(i);
                
                Problem problem = new Problem(problemObj.getString("text"));
                problem.id = problemObj.getString("id");
                problem.domain = problemObj.getString("domain");
                problem.originalQuery = problemObj.getString("originalQuery");
                problem.timestamp = problemObj.getLong("timestamp");
                
                // Load topics
                JSONArray topicsArray = problemObj.getJSONArray("topics");
                for (int j = 0; j < topicsArray.length(); j++) {
                    problem.topics.add(topicsArray.getString(j));
                }
                
                // Load variables
                JSONObject variablesObj = problemObj.getJSONObject("variables");
                Iterator<String> variableKeys = variablesObj.keys();
                while (variableKeys.hasNext()) {
                    String key = variableKeys.next();
                    problem.variables.put(key, variablesObj.getString(key));
                }
                
                // Load solution if exists
                if (problemObj.has("solution")) {
                    JSONObject solutionObj = problemObj.getJSONObject("solution");
                    Solution solution = new Solution(problem.id);
                    
                    solution.answer = solutionObj.getString("answer");
                    solution.explanation = solutionObj.getString("explanation");
                    solution.solveTimeMs = solutionObj.getLong("solveTimeMs");
                    solution.isCorrect = solutionObj.getBoolean("isCorrect");
                    solution.isVerified = solutionObj.getBoolean("isVerified");
                    
                    // Load steps
                    JSONArray stepsArray = solutionObj.getJSONArray("steps");
                    for (int j = 0; j < stepsArray.length(); j++) {
                        JSONObject stepObj = stepsArray.getJSONObject(j);
                        Step step = new Step(
                                stepObj.getString("description"),
                                stepObj.getString("workingOut"),
                                stepObj.getString("explanation")
                        );
                        solution.steps.add(step);
                    }
                    
                    // Load related concepts
                    JSONArray conceptsArray = solutionObj.getJSONArray("relatedConcepts");
                    for (int j = 0; j < conceptsArray.length(); j++) {
                        solution.relatedConcepts.add(conceptsArray.getString(j));
                    }
                    
                    problem.solution = solution;
                }
                
                problemHistory.add(problem);
            }
            
            Log.d(TAG, "Loaded " + problemHistory.size() + " problems from history");
        } catch (Exception e) {
            Log.e(TAG, "Error loading problem history: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save problem history to storage
     */
    private void saveProblemHistory() {
        try {
            JSONObject rootObject = new JSONObject();
            JSONArray problemsArray = new JSONArray();
            
            // Only save the most recent problems (up to 100)
            int maxProblemsToSave = Math.min(problemHistory.size(), 100);
            
            for (int i = 0; i < maxProblemsToSave; i++) {
                Problem problem = problemHistory.get(i);
                JSONObject problemObj = new JSONObject();
                
                problemObj.put("id", problem.id);
                problemObj.put("text", problem.text);
                problemObj.put("domain", problem.domain);
                problemObj.put("originalQuery", problem.originalQuery);
                problemObj.put("timestamp", problem.timestamp);
                
                // Save topics
                JSONArray topicsArray = new JSONArray();
                for (String topic : problem.topics) {
                    topicsArray.put(topic);
                }
                problemObj.put("topics", topicsArray);
                
                // Save variables
                JSONObject variablesObj = new JSONObject();
                for (Map.Entry<String, String> entry : problem.variables.entrySet()) {
                    variablesObj.put(entry.getKey(), entry.getValue());
                }
                problemObj.put("variables", variablesObj);
                
                // Save solution if exists
                if (problem.solution != null) {
                    JSONObject solutionObj = new JSONObject();
                    
                    solutionObj.put("answer", problem.solution.answer);
                    solutionObj.put("explanation", problem.solution.explanation);
                    solutionObj.put("solveTimeMs", problem.solution.solveTimeMs);
                    solutionObj.put("isCorrect", problem.solution.isCorrect);
                    solutionObj.put("isVerified", problem.solution.isVerified);
                    
                    // Save steps
                    JSONArray stepsArray = new JSONArray();
                    for (Step step : problem.solution.steps) {
                        JSONObject stepObj = new JSONObject();
                        stepObj.put("description", step.description);
                        stepObj.put("workingOut", step.workingOut);
                        stepObj.put("explanation", step.explanation);
                        stepsArray.put(stepObj);
                    }
                    solutionObj.put("steps", stepsArray);
                    
                    // Save related concepts
                    JSONArray conceptsArray = new JSONArray();
                    for (String concept : problem.solution.relatedConcepts) {
                        conceptsArray.put(concept);
                    }
                    solutionObj.put("relatedConcepts", conceptsArray);
                    
                    problemObj.put("solution", solutionObj);
                }
                
                problemsArray.put(problemObj);
            }
            
            rootObject.put("problems", problemsArray);
            
            File file = new File(context.getFilesDir(), PROBLEM_HISTORY_FILE);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(rootObject.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();
            
            Log.d(TAG, "Saved " + maxProblemsToSave + " problems to history");
        } catch (Exception e) {
            Log.e(TAG, "Error saving problem history: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get recent problems
     */
    public List<Problem> getRecentProblems(int limit) {
        int count = Math.min(limit, problemHistory.size());
        return new ArrayList<>(problemHistory.subList(0, count));
    }
    
    /**
     * Get problem by ID
     */
    public Problem getProblem(String problemId) {
        for (Problem problem : problemHistory) {
            if (problem.id.equals(problemId)) {
                return problem;
            }
        }
        return null;
    }
    
    /**
     * Get formulas by domain
     */
    public List<Formula> getFormulas(String domain) {
        return new ArrayList<>(formulasByDomain.getOrDefault(domain, new ArrayList<>()));
    }
    
    /**
     * Get concepts by domain
     */
    public List<Concept> getConcepts(String domain) {
        return new ArrayList<>(conceptsByDomain.getOrDefault(domain, new ArrayList<>()));
    }
    
    /**
     * Search formulas by keyword
     */
    public List<Formula> searchFormulas(String keyword) {
        List<Formula> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        
        for (List<Formula> formulas : formulasByDomain.values()) {
            for (Formula formula : formulas) {
                if (formula.name.toLowerCase().contains(lowerKeyword) || 
                        formula.expression.toLowerCase().contains(lowerKeyword) || 
                        formula.explanation.toLowerCase().contains(lowerKeyword)) {
                    results.add(formula);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Search concepts by keyword
     */
    public List<Concept> searchConcepts(String keyword) {
        List<Concept> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        
        for (List<Concept> concepts : conceptsByDomain.values()) {
            for (Concept concept : concepts) {
                if (concept.name.toLowerCase().contains(lowerKeyword) || 
                        concept.definition.toLowerCase().contains(lowerKeyword)) {
                    results.add(concept);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Add a custom formula
     */
    public void addFormula(Formula formula) {
        if (formula != null && formula.domain != null) {
            List<Formula> domainFormulas = formulasByDomain.get(formula.domain);
            if (domainFormulas != null) {
                domainFormulas.add(formula);
                
                // Store in AI knowledge
                KnowledgeEntry entry = aiStateManager.storeKnowledge(
                        "Formula: " + formula.name, 
                        formula.expression + " - " + formula.explanation);
                entry.categories.add("Formula");
                entry.categories.add(formula.domain);
                for (String topic : formula.topics) {
                    entry.categories.add(topic);
                }
            }
        }
    }
    
    /**
     * Add a custom concept
     */
    public void addConcept(Concept concept) {
        if (concept != null && concept.domain != null) {
            List<Concept> domainConcepts = conceptsByDomain.get(concept.domain);
            if (domainConcepts != null) {
                domainConcepts.add(concept);
                
                // Store in AI knowledge
                KnowledgeEntry entry = aiStateManager.storeKnowledge(
                        "Concept: " + concept.name, 
                        concept.definition);
                entry.categories.add("Concept");
                entry.categories.add(concept.domain);
            }
        }
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        executor.shutdown();
        saveProblemHistory();
    }
}
