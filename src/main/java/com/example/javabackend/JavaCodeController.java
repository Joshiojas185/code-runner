// package com.example.javabackend;

// import org.springframework.web.bind.annotation.*;
// import java.io.*;
// import java.net.URL;
// import java.net.URLClassLoader;
// import java.nio.charset.StandardCharsets;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.concurrent.TimeUnit;
// import java.util.stream.Collectors;
// import javax.tools.JavaCompiler;
// import javax.tools.ToolProvider;
// import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
// import org.junit.platform.launcher.Launcher;
// import org.junit.platform.launcher.core.LauncherFactory;
// import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
// import org.junit.platform.launcher.listeners.TestExecutionSummary;

// @RestController
// @RequestMapping("/api")
// public class JavaCodeController {

//     private static final String CLASS_NAME = "Main";
//     private static final String TEST_CLASS_NAME = "MainTest";

//     // API endpoint for Java code with test cases
//     @PostMapping("/run-java")
//     public Map<String, Object> runTestCases(@RequestBody Map<String, Object> requestBody) {
//         String javaCode = (String) requestBody.get("code");
//         java.util.List<Map<String, Object>> testCases = (java.util.List<Map<String, Object>>) requestBody.get("testCases");

//         File javaFile = new File(CLASS_NAME + ".java");
//         File testFile = new File(TEST_CLASS_NAME + ".java");
//         Map<String, Object> response = new HashMap<>();

//         try {
//             // Write user's code
//             try (FileWriter writer = new FileWriter(javaFile)) {
//                 writer.write(javaCode);
//             }
            
//             // Generate and write test code
//             String testCode = generateTestClass(testCases);
//             try (FileWriter writer = new FileWriter(testFile)) {
//                 writer.write(testCode);
//             }

//             // Compile both files
//             JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//             if (compiler == null) {
//                 return Map.of("error", "JDK is not properly configured. Cannot find system Java compiler.");
//             }

//             int compilationResult = compiler.run(null, null, null, javaFile.getPath(), testFile.getPath());
//             if (compilationResult != 0) {
//                 return Map.of("error", "Compilation failed.");
//             }

//             // Run the tests
//             URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(".").toURI().toURL()});
//             Class<?> testClass = classLoader.loadClass(TEST_CLASS_NAME);

//             Launcher launcher = LauncherFactory.create();
//             SummaryGeneratingListener listener = new SummaryGeneratingListener();
//             launcher.registerTestExecutionListeners(listener);
            
//             launcher.execute(LauncherDiscoveryRequestBuilder.request().selectors(
//                 org.junit.platform.engine.discovery.DiscoverySelectors.selectClass(testClass)
//             ).build());

//             TestExecutionSummary summary = listener.getSummary();
//             response.put("passed", summary.getTestsSucceededCount());
//             response.put("failed", summary.getTestsFailedCount());
//             response.put("total", summary.getTestsFoundCount());
//             response.put("success", summary.getTestsFailedCount() == 0);
            
//             if (summary.getTestsFailedCount() > 0) {
//                 java.util.List<String> failures = new java.util.ArrayList<>();
//                 for (TestExecutionSummary.Failure failure : summary.getFailures()) {
//                     failures.add(failure.getTestIdentifier().getDisplayName() + ": " + failure.getException().getMessage());
//                 }
//                 response.put("failures", failures);
//             }
            
//         } catch (Exception e) {
//             response.put("error", "Server error during execution: " + e.getMessage());
//         } finally {
//             // Clean up files
//             javaFile.delete();
//             testFile.delete();
//             new File(CLASS_NAME + ".class").delete();
//             new File(TEST_CLASS_NAME + ".class").delete();
//         }

//         return response;
//     }

//     private String generateTestClass(java.util.List<Map<String, Object>> testCases) {
//         StringBuilder sb = new StringBuilder();
//         sb.append("import org.junit.jupiter.api.Test;\n");
//         sb.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
//         sb.append("public class MainTest {\n");

//         for (int i = 0; i < testCases.size(); i++) {
//             Map<String, Object> testCase = testCases.get(i);
//             String input = (String) testCase.get("input");
//             String expected = (String) testCase.get("expected");
//             String methodName = "testCase" + (i + 1);

//             sb.append("    @Test\n");
//             sb.append("    void " + methodName + "() {\n");
//             sb.append("        Main main = new Main();\n");
//             sb.append("        int[] expectedArray = new int[]{" + expected + "};\n");
//             sb.append("        int[] actualArray = main.run(new int[]{" + input + "});\n");
//             sb.append("        assertArrayEquals(expectedArray, actualArray, \"Test Case " + (i + 1) + " failed\");\n");
//             sb.append("    }\n\n");
//         }

//         sb.append("}\n");
//         return sb.toString();
//     }

//     // Helper method for interpreted languages (Python, JS)
//     private String runInterpreterCommand(String fileName, String code, String... command) {
//         try {
//             try (FileWriter writer = new FileWriter(fileName)) {
//                 writer.write(code);
//             }
//             ProcessBuilder processBuilder = new ProcessBuilder(command);
//             Process process = processBuilder.start();

//             if (!process.waitFor(60, TimeUnit.SECONDS)) {
//                 process.destroyForcibly();
//                 return "Error: Execution timed out.";
//             }

//             String output = readInputStream(process.getInputStream());
//             String error = readInputStream(process.getErrorStream());

//             if (!error.isEmpty()) {
//                 return "Execution error:\n" + error;
//             }
//             return output;
//         } catch (IOException | InterruptedException e) {
//             return "Server error: " + e.getMessage();
//         } finally {
//             new File(fileName).delete();
//         }
//     }

//     // Helper method for compiled languages (C, C++)
//     private String runCompileAndExecuteCommand(String sourceFile, String code, String compileCommand, String executeCommand) {
//         String output = "";
//         try {
//             // Write the source code to a file
//             try (FileWriter writer = new FileWriter(sourceFile)) {
//                 writer.write(code);
//             }

//             // 1. Run the compilation command
//             Process compileProcess = Runtime.getRuntime().exec(compileCommand);
//             if (!compileProcess.waitFor(60, TimeUnit.SECONDS) || compileProcess.exitValue() != 0) {
//                 String error = readInputStream(compileProcess.getErrorStream());
//                 return "Compilation error:\n" + error;
//             }

//             // 2. Run the executable
//             Process executeProcess = Runtime.getRuntime().exec(executeCommand);
//             if (!executeProcess.waitFor(60, TimeUnit.SECONDS)) {
//                 executeProcess.destroyForcibly();
//                 return "Error: Execution timed out.";
//             }
            
//             output = readInputStream(executeProcess.getInputStream());
//             String error = readInputStream(executeProcess.getErrorStream());

//             if (!error.isEmpty()) {
//                 return "Execution error:\n" + error;
//             }
//             return output;

//         } catch (IOException | InterruptedException e) {
//             return "Server error: " + e.getMessage();
//         } finally {
//             // Clean up files
//             new File(sourceFile).delete();
//             new File(executeCommand).delete(); // Delete the compiled executable
//         }
//     }

//     private String readInputStream(InputStream is) throws IOException {
//         return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
//                 .lines().collect(Collectors.joining("\n"));
//     }

//     // New API endpoint for C code
//     @PostMapping("/run-c")
//     public String runC(@RequestBody String code) {
//         String sourceFile = "main.c";
//         String executableName = "./a.out";
//         String compileCmd = String.format("gcc %s -o %s", sourceFile, executableName);
//         return runCompileAndExecuteCommand(sourceFile, code, compileCmd, executableName);
//     }

//     // New API endpoint for C++ code
//     @PostMapping("/run-cpp")
//     public String runCpp(@RequestBody String code) {
//         String sourceFile = "main.cpp";
//         String executableName = "./a.out";
//         String compileCmd = String.format("g++ %s -o %s", sourceFile, executableName);
//         return runCompileAndExecuteCommand(sourceFile, code, compileCmd, executableName);
//     }

//     // New API endpoint for Python code
//     @PostMapping("/run-python")
//     public String runPython(@RequestBody String code) {
//         String fileName = "main.py";
//         return runInterpreterCommand(fileName, code, "python3", fileName);
//     }

//     // New API endpoint for JavaScript code
//     @PostMapping("/run-js")
//     public String runJavaScript(@RequestBody String code) {
//         String fileName = "main.js";
//         return runInterpreterCommand(fileName, code, "node", fileName);
//     }
// }















package com.example.javabackend;

import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.jupiter.api.Assertions;

@RestController
@RequestMapping("/api")
public class JavaCodeController {

    private static final String CLASS_NAME = "Main";
    private static final String TEST_CLASS_NAME = "MainTest";

    // Helper method for interpreted languages (Python, JS)
    private String runInterpreter(String fileName, String code, String... command) {
        try {
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(code);
            }
            Process process = new ProcessBuilder(command).start();
            
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return "Error: Execution timed out.";
            }

            String output = readInputStream(process.getInputStream());
            String error = readInputStream(process.getErrorStream());

            if (!error.isEmpty()) {
                return "Execution error:\n" + error;
            }
            return output;
        } catch (IOException | InterruptedException e) {
            return "Server error: " + e.getMessage();
        } finally {
            new File(fileName).delete();
        }
    }

    // Helper method for compiled languages (C, C++)
    private String runCompiledCode(String sourceFile, String code, String compileCmd, String execCmd) {
        try {
            // Write the source code to a file
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(code);
            }

            // 1. Run the compilation command
            Process compileProcess = new ProcessBuilder(compileCmd.split(" ")).start();
            if (!compileProcess.waitFor(60, TimeUnit.SECONDS) || compileProcess.exitValue() != 0) {
                String error = readInputStream(compileProcess.getErrorStream());
                return "Compilation error:\n" + error;
            }

            // 2. Run the executable
            Process execProcess = new ProcessBuilder(execCmd).start();
            if (!execProcess.waitFor(60, TimeUnit.SECONDS)) {
                execProcess.destroyForcibly();
                return "Error: Execution timed out.";
            }
            
            String output = readInputStream(execProcess.getInputStream());
            String error = readInputStream(execProcess.getErrorStream());

            if (!error.isEmpty()) {
                return "Execution error:\n" + error;
            }
            return output;

        } catch (IOException | InterruptedException e) {
            return "Server error: " + e.getMessage();
        } finally {
            new File(sourceFile).delete();
            new File(execCmd).delete(); 
        }
    }

    private String readInputStream(InputStream is) throws IOException {
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
    }
    
    // API endpoint for Java code with test cases
    // @PostMapping("/run-java")

    // Inside JavaCodeController.java

// API endpoint for Java code with test cases
@PostMapping("/run-java")
public Map<String, Object> runTestCases(@RequestBody Map<String, Object> requestBody) {
    String javaCode = (String) requestBody.get("code");
    java.util.List<Map<String, Object>> testCases = (java.util.List<Map<String, Object>>) requestBody.get("testCases");

    File javaFile = new File(CLASS_NAME + ".java");
    File testFile = new File(TEST_CLASS_NAME + ".java");
    Map<String, Object> response = new HashMap<>();

    try {
        // Write user's code
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(javaCode);
        }
        
        // Generate and write test code
        String testCode = generateTestClass(testCases);
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(testCode);
        }

        // --- Corrected Compilation Step ---
        String classpath = "target/classes" + 
                           ":/root/.m2/repository/org/junit/platform/junit-platform-suite-api/1.10.3/junit-platform-suite-api-1.10.3.jar" + 
                           ":/root/.m2/repository/org/junit/jupiter/junit-jupiter-engine/5.10.3/junit-jupiter-engine-5.10.3.jar" +
                           ":/root/.m2/repository/org/junit/platform/junit-platform-launcher/1.10.3/junit-platform-launcher-1.10.3.jar" +
                           ":/root/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.10.3/junit-jupiter-api-5.10.3.jar" +
                           ":/root/.m2/repository/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.jar" +
                           ":/root/.m2/repository/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar";
        
        Process compileProcess = new ProcessBuilder("javac", "-cp", classpath, javaFile.getPath(), testFile.getPath()).start();
        if (!compileProcess.waitFor(60, TimeUnit.SECONDS) || compileProcess.exitValue() != 0) {
            String error = readInputStream(compileProcess.getErrorStream());
            return Map.of("error", "Compilation failed:\n" + error);
        }
        // --- End of Corrected Compilation Step ---

        // Run the tests
        URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(".").toURI().toURL()});
        Class<?> testClass = classLoader.loadClass(TEST_CLASS_NAME);

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        
        launcher.execute(LauncherDiscoveryRequestBuilder.request().selectors(
            org.junit.platform.engine.discovery.DiscoverySelectors.selectClass(testClass)
        ).build());

        TestExecutionSummary summary = listener.getSummary();
        response.put("passed", summary.getTestsSucceededCount());
        response.put("failed", summary.getTestsFailedCount());
        response.put("total", summary.getTestsFoundCount());
        response.put("success", summary.getTestsFailedCount() == 0);
        
        if (summary.getTestsFailedCount() > 0) {
            java.util.List<String> failures = new java.util.ArrayList<>();
            for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                failures.add(failure.getTestIdentifier().getDisplayName() + ": " + failure.getException().getMessage());
            }
            response.put("failures", failures);
        }
        
    } catch (Exception e) {
        response.put("error", "Server error during execution: " + e.getMessage());
    } finally {
        javaFile.delete();
        testFile.delete();
        new File(CLASS_NAME + ".class").delete();
        new File(TEST_CLASS_NAME + ".class").delete();
    }

    return response;
}
    
    // public Map<String, Object> runTestCases(@RequestBody Map<String, Object> requestBody) 
    // {
    //     String javaCode = (String) requestBody.get("code");
    //     java.util.List<Map<String, Object>> testCases = (java.util.List<Map<String, Object>>) requestBody.get("testCases");

    //     File javaFile = new File(CLASS_NAME + ".java");
    //     File testFile = new File(TEST_CLASS_NAME + ".java");
    //     Map<String, Object> response = new HashMap<>();

    //     try {
    //         // Write user's code
    //         try (FileWriter writer = new FileWriter(javaFile)) {
    //             writer.write(javaCode);
    //         }
            
    //         // Generate and write test code
    //         String testCode = generateTestClass(testCases);
    //         try (FileWriter writer = new FileWriter(testFile)) {
    //             writer.write(testCode);
    //         }

    //         // Compile both files
    //         JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    //         if (compiler == null) {
    //             return Map.of("error", "JDK is not properly configured. Cannot find system Java compiler.");
    //         }

    //         int compilationResult = compiler.run(null, null, null, javaFile.getPath(), testFile.getPath());
    //         if (compilationResult != 0) {
    //             return Map.of("error", "Compilation failed.");
    //         }

    //         // Run the tests
    //         URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(".").toURI().toURL()});
    //         Class<?> testClass = classLoader.loadClass(TEST_CLASS_NAME);

    //         Launcher launcher = LauncherFactory.create();
    //         SummaryGeneratingListener listener = new SummaryGeneratingListener();
    //         launcher.registerTestExecutionListeners(listener);
            
    //         launcher.execute(LauncherDiscoveryRequestBuilder.request().selectors(
    //             org.junit.platform.engine.discovery.DiscoverySelectors.selectClass(testClass)
    //         ).build());

    //         TestExecutionSummary summary = listener.getSummary();
    //         response.put("passed", summary.getTestsSucceededCount());
    //         response.put("failed", summary.getTestsFailedCount());
    //         response.put("total", summary.getTestsFoundCount());
    //         response.put("success", summary.getTestsFailedCount() == 0);
            
    //         if (summary.getTestsFailedCount() > 0) {
    //             java.util.List<String> failures = new java.util.ArrayList<>();
    //             for (TestExecutionSummary.Failure failure : summary.getFailures()) {
    //                 failures.add(failure.getTestIdentifier().getDisplayName() + ": " + failure.getException().getMessage());
    //             }
    //             response.put("failures", failures);
    //         }
            
    //     } catch (Exception e) {
    //         response.put("error", "Server error during execution: " + e.getMessage());
    //     } finally {
    //         javaFile.delete();
    //         testFile.delete();
    //         new File(CLASS_NAME + ".class").delete();
    //         new File(TEST_CLASS_NAME + ".class").delete();
    //     }

    //     return response;
    // }

    private String generateTestClass(java.util.List<Map<String, Object>> testCases) {
        StringBuilder sb = new StringBuilder();
        sb.append("import org.junit.jupiter.api.Test;\n");
        sb.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
        sb.append("public class MainTest {\n");

        for (int i = 0; i < testCases.size(); i++) {
            Map<String, Object> testCase = testCases.get(i);
            String input = (String) testCase.get("input");
            String expected = (String) testCase.get("expected");
            String methodName = "testCase" + (i + 1);

            sb.append("    @Test\n");
            sb.append("    void " + methodName + "() {\n");
            sb.append("        Main main = new Main();\n");
            sb.append("        int[] expectedArray = new int[]{" + expected + "};\n");
            sb.append("        int[] actualArray = main.run(new int[]{" + input + "});\n");
            sb.append("        assertArrayEquals(expectedArray, actualArray, \"Test Case " + (i + 1) + " failed\");\n");
            sb.append("    }\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }
    
    @PostMapping("/run-c")
    public String runC(@RequestBody String code) {
        String sourceFile = "main.c";
        String execFile = "a.out";
        String compileCmd = "gcc " + sourceFile + " -o " + execFile;
        return runCompiledCode(sourceFile, code, compileCmd, "./" + execFile);
    }

    @PostMapping("/run-cpp")
    public String runCpp(@RequestBody String code) {
        String sourceFile = "main.cpp";
        String execFile = "a.out";
        String compileCmd = "g++ " + sourceFile + " -o " + execFile;
        return runCompiledCode(sourceFile, code, compileCmd, "./" + execFile);
    }

    @PostMapping("/run-python")
    public String runPython(@RequestBody String code) {
        String fileName = "main.py";
        return runInterpreter(fileName, code, "python3", fileName);
    }

    @PostMapping("/run-js")
    public String runJavaScript(@RequestBody String code) {
        String fileName = "main.js";
        return runInterpreter(fileName, code, "node", fileName);
    }
}