package com.aiassistant.ai.features.education.jee;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * QuadraticEquationSolver solves quadratic equations of the form ax² + bx + c = 0
 * and provides step-by-step solutions for JEE Advanced questions.
 */
public class QuadraticEquationSolver {
    private static final double EPSILON = 1e-10;
    
    /**
     * Solve a quadratic equation
     * @param a Coefficient of x²
     * @param b Coefficient of x
     * @param c Constant term
     * @return Array of solutions (may be empty, have one root, or two roots)
     */
    public static double[] solve(double a, double b, double c) {
        // Check if actually quadratic
        if (Math.abs(a) < EPSILON) {
            // Linear equation: bx + c = 0
            if (Math.abs(b) < EPSILON) {
                // No variable, just constant
                return new double[0];
            }
            // Linear solution: x = -c/b
            return new double[] { -c / b };
        }
        
        // Calculate discriminant
        double discriminant = b * b - 4 * a * c;
        
        if (Math.abs(discriminant) < EPSILON) {
            // One real root
            return new double[] { -b / (2 * a) };
        } else if (discriminant > 0) {
            // Two real roots
            double sqrtDiscriminant = Math.sqrt(discriminant);
            return new double[] {
                (-b + sqrtDiscriminant) / (2 * a),
                (-b - sqrtDiscriminant) / (2 * a)
            };
        } else {
            // No real roots
            return new double[0];
        }
    }
    
    /**
     * Generate a step-by-step solution explanation
     * @param a Coefficient of x²
     * @param b Coefficient of x
     * @param c Constant term
     * @return Detailed solution explanation
     */
    public static String generateSolution(double a, double b, double c) {
        StringBuilder solution = new StringBuilder();
        
        // Format the equation
        solution.append("Quadratic Equation: ");
        if (Math.abs(a - 1.0) < EPSILON) {
            solution.append("x² ");
        } else if (Math.abs(a + 1.0) < EPSILON) {
            solution.append("-x² ");
        } else {
            solution.append(a).append("x² ");
        }
        
        if (b > 0) {
            solution.append("+ ");
            if (Math.abs(b - 1.0) < EPSILON) {
                solution.append("x ");
            } else {
                solution.append(b).append("x ");
            }
        } else if (b < 0) {
            if (Math.abs(b + 1.0) < EPSILON) {
                solution.append("- x ");
            } else {
                solution.append("- ").append(Math.abs(b)).append("x ");
            }
        }
        
        if (c > 0) {
            solution.append("+ ").append(c);
        } else if (c < 0) {
            solution.append("- ").append(Math.abs(c));
        }
        
        solution.append(" = 0\n\n");
        
        solution.append("Step 1: Identify the coefficients\n");
        solution.append("a = ").append(a).append("\n");
        solution.append("b = ").append(b).append("\n");
        solution.append("c = ").append(c).append("\n\n");
        
        // Check if actually quadratic
        if (Math.abs(a) < EPSILON) {
            solution.append("Step 2: Since a ≈ 0, this is not a quadratic equation but a linear equation\n");
            
            if (Math.abs(b) < EPSILON) {
                solution.append("Step 3: Since b ≈ 0 as well, this equation reduces to: ").append(c).append(" = 0\n");
                if (Math.abs(c) < EPSILON) {
                    solution.append("Step 4: This is true for all values of x, so there are infinitely many solutions\n");
                } else {
                    solution.append("Step 4: This is a contradiction, so there are no solutions\n");
                }
            } else {
                solution.append("Step 3: This is a linear equation of the form bx + c = 0\n");
                solution.append("Step 4: Solve for x: x = -c/b = -(").append(c).append(")/").append(b).append(" = ").append(-c/b).append("\n");
            }
            
            return solution.toString();
        }
        
        // Calculate discriminant
        solution.append("Step 2: Calculate the discriminant Δ = b² - 4ac\n");
        double discriminant = b * b - 4 * a * c;
        solution.append("Δ = (").append(b).append(")² - 4(").append(a).append(")(").append(c).append(") = ")
                .append(b*b).append(" - ").append(4*a*c).append(" = ").append(discriminant).append("\n\n");
        
        if (Math.abs(discriminant) < EPSILON) {
            solution.append("Step 3: Since Δ ≈ 0, there is exactly one real solution (repeated root)\n");
            solution.append("Step 4: Calculate x = -b/(2a)\n");
            double root = -b / (2 * a);
            solution.append("x = -(").append(b).append(")/(2*").append(a).append(") = ")
                    .append(root).append("\n\n");
            solution.append("The solution is x = ").append(root);
        } else if (discriminant > 0) {
            solution.append("Step 3: Since Δ > 0, there are two distinct real solutions\n");
            solution.append("Step 4: Calculate x using the quadratic formula: x = (-b ± √Δ)/(2a)\n");
            
            double sqrtDiscriminant = Math.sqrt(discriminant);
            solution.append("x = (-").append(b).append(" ± √").append(discriminant).append(")/(2*").append(a).append(")\n");
            solution.append("x = (-").append(b).append(" ± ").append(sqrtDiscriminant).append(")/").append(2*a).append("\n\n");
            
            double root1 = (-b + sqrtDiscriminant) / (2 * a);
            double root2 = (-b - sqrtDiscriminant) / (2 * a);
            
            solution.append("x₁ = (-").append(b).append(" + ").append(sqrtDiscriminant).append(")/").append(2*a)
                    .append(" = ").append(root1).append("\n");
            solution.append("x₂ = (-").append(b).append(" - ").append(sqrtDiscriminant).append(")/").append(2*a)
                    .append(" = ").append(root2).append("\n\n");
            
            solution.append("The solutions are x = ").append(root1).append(" and x = ").append(root2);
        } else {
            solution.append("Step 3: Since Δ < 0, there are no real solutions\n");
            solution.append("Step 4: Calculate the complex solutions: x = (-b ± i√|Δ|)/(2a)\n");
            
            double sqrtAbsDiscriminant = Math.sqrt(Math.abs(discriminant));
            double realPart = -b / (2 * a);
            double imagPart = sqrtAbsDiscriminant / (2 * a);
            
            solution.append("x = (-").append(b).append(" ± i√|").append(discriminant).append("|)/(2*").append(a).append(")\n");
            solution.append("x = (-").append(b).append(" ± i").append(sqrtAbsDiscriminant).append(")/").append(2*a).append("\n");
            solution.append("x = ").append(realPart).append(" ± ").append(imagPart).append("i\n\n");
            
            solution.append("The solutions are x = ").append(realPart).append(" + ").append(imagPart).append("i and x = ")
                    .append(realPart).append(" - ").append(imagPart).append("i");
        }
        
        return solution.toString();
    }
    
    /**
     * Extract coefficients from an equation string
     * @param equation String representation of quadratic equation
     * @return Array of coefficients [a, b, c]
     */
    public static double[] extractCoefficients(String equation) {
        // Normalize equation: remove spaces, ensure it's in form "ax² + bx + c = 0"
        equation = equation.replaceAll("\\s+", "").toLowerCase();
        
        // If equation is in form "... = ..." move everything to LHS
        if (equation.contains("=")) {
            String[] sides = equation.split("=");
            if (sides.length == 2) {
                // Move right side to left with sign change
                equation = sides[0] + "-(" + sides[1] + ")";
            }
        } else {
            // If no equals sign, assume = 0 implicitly
            equation = equation + "=0";
        }
        
        // Replace x² with x^2 for easier parsing
        equation = equation.replace("x²", "x^2").replace("x2", "x^2");
        
        // Extract coefficients
        double a = 0, b = 0, c = 0;
        
        // Extract x^2 term
        Pattern patternA = Pattern.compile("([+-]?\\d*\\.?\\d*)x\\^2");
        Matcher matcherA = patternA.matcher(equation);
        while (matcherA.find()) {
            String coef = matcherA.group(1);
            if (coef.isEmpty() || coef.equals("+")) {
                a += 1;
            } else if (coef.equals("-")) {
                a -= 1;
            } else {
                a += Double.parseDouble(coef);
            }
        }
        
        // Extract x term
        Pattern patternB = Pattern.compile("([+-]?\\d*\\.?\\d*)x(?!\\^)");
        Matcher matcherB = patternB.matcher(equation);
        while (matcherB.find()) {
            String coef = matcherB.group(1);
            if (coef.isEmpty() || coef.equals("+")) {
                b += 1;
            } else if (coef.equals("-")) {
                b -= 1;
            } else {
                b += Double.parseDouble(coef);
            }
        }
        
        // Extract constant term
        Pattern patternC = Pattern.compile("([+-]?\\d+\\.?\\d*)(?![x\\d])");
        Matcher matcherC = patternC.matcher(equation);
        while (matcherC.find()) {
            c += Double.parseDouble(matcherC.group(1));
        }
        
        return new double[] {a, b, c};
    }
}
