package org.example;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.testng.annotations.Test;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import javax.lang.model.element.Modifier;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TestCaseGeneratorNew {

    public static void main(String[] args) throws IOException {
        // URL to Swagger spec
        String swaggerUrl = "https://petstore.swagger.io/v2/swagger.json";

        // Parse the Swagger spec
        OpenAPI openAPI = new OpenAPIV3Parser().read(swaggerUrl);

        // Extract base URL from the Swagger spec
        String baseUrl = extractBaseUrl(openAPI);

        // Generate test cases based on the paths with the extracted base URL
        generateTestCases(openAPI, baseUrl);
    }

    private static String extractBaseUrl(OpenAPI openAPI) {
        // Get the servers list from the OpenAPI spec
        List<Server> servers = openAPI.getServers();

        // Return the first server's URL as the base URL (fallback to default if servers list is empty)
        if (servers != null && !servers.isEmpty()) {
            return servers.get(0).getUrl();
        } else {
            return "http://localhost";  // Default base URL if none is specified in the spec
        }
    }

    private static void generateTestCases(OpenAPI openAPI, String baseUrl) throws IOException {
        // Directory where test classes will be generated
        Path testJavaDir = Paths.get("src/test/java");
        String packageName = "org.example";  // Package name for the generated classes

        // Create directories if they don't exist
        if (!Files.exists(testJavaDir)) {
            Files.createDirectories(testJavaDir);
        }

        // Accumulate test methods for each HTTP method
        Map<String, StringBuilder> methodBuilders = new HashMap<>();

        for (String path : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(path);

            for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                String httpMethod = entry.getKey().name(); // Convert HttpMethod enum to string
                Operation operation = entry.getValue();

                if (!methodBuilders.containsKey(httpMethod)) {
                    methodBuilders.put(httpMethod, new StringBuilder());
                }
                generateTestMethod(methodBuilders.get(httpMethod), httpMethod, path, operation, baseUrl);
            }
        }

        // Write each HTTP method's accumulated test methods into their respective class files
        for (Map.Entry<String, StringBuilder> entry : methodBuilders.entrySet()) {
            String httpMethod = entry.getKey();
            StringBuilder methods = entry.getValue();
            writeTestClassIfNeeded(testJavaDir, packageName, httpMethod + "Tests", methods, baseUrl);
        }
    }

    private static void generateTestMethod(StringBuilder methodBuilder, String httpMethod, String path, Operation operation, String baseUrl) {
        String methodName = "test" + httpMethod + formatPathToMethodName(path);
        methodBuilder.append("    @Test\n");
        methodBuilder.append("    public void ").append(methodName).append("() {\n");

        if (httpMethod.equals("GET")) {
            methodBuilder.append("        Map<String, String> queryParams = new HashMap<>();\n");
            if (operation.getParameters() != null) {
                for (Parameter parameter : operation.getParameters()) {
                    if ("query".equals(parameter.getIn())) {
                        methodBuilder.append("        queryParams.put(\"").append(parameter.getName()).append("\", \"sampleValue\");\n");
                    }
                }
            }
            methodBuilder.append("        Response response = getRequest(\"").append(path).append("\", queryParams);\n");
        } else {
            methodBuilder.append("        Map<String, Object> bodyParams = new HashMap<>();\n");
            methodBuilder.append("        Response response = ").append(httpMethod.toLowerCase()).append("Request(\"").append(path).append("\", bodyParams);\n");
        }

        methodBuilder.append("        response.then().statusCode(200);\n");
        methodBuilder.append("    }\n\n");
    }

    private static void writeTestClassIfNeeded(Path testJavaDir, String packageName, String className, StringBuilder methods, String baseUrl) throws IOException {
        Path packageDir = testJavaDir.resolve(packageName.replace(".", "/"));
        Path classFilePath = packageDir.resolve(className + ".java");

        // Check if the class file already exists
        boolean classExists = Files.exists(classFilePath);
        if (classExists) {
            // Parse the existing class using JavaParser
            FileInputStream in = new FileInputStream(classFilePath.toFile());
            JavaParser javaParser = new JavaParser();
            CompilationUnit cu = javaParser.parse(in).getResult().orElseThrow(() -> new RuntimeException("Parse error"));
            in.close();

            // Find the class you want to modify
            ClassOrInterfaceDeclaration existingClass = cu.getClassByName(className)
                    .orElseThrow(() -> new RuntimeException("Class not found"));

            List<String> existingMethodNames = new ArrayList<>();
            for (MethodDeclaration method : existingClass.getMethods()) {
                existingMethodNames.add(method.getNameAsString());
            }

            // Check if the methods are already present in the class
            boolean newMethodsAdded = false;
            for (MethodSpec methodSpec : parseMethods(methods.toString())) {
                if (existingMethodNames.contains(methodSpec.name)) {
                    // Check if the V2 version of the method already exists
                    String v2MethodName = methodSpec.name + "V2";
                    if (existingMethodNames.contains(v2MethodName)) {
                        // V2 version already exists, skip adding and no prompt
                        System.out.println("Method " + v2MethodName + " already exists. Skipping...");
                        continue;
                    } else {
                        // If V2 version does not exist, prompt the user
                        System.out.println("Method " + methodSpec.name + " already exists in class " + className + ". Do you want to add it again as a new version? (yes/no)");
                        Scanner scanner = new Scanner(System.in);
                        String userInput = scanner.nextLine().trim().toLowerCase();

                        if ("yes".equals(userInput)) {
                            // Add new version of the method
                            MethodSpec newMethodSpec = methodSpec.toBuilder().setName(v2MethodName).build();
                            String newMethodString = newMethodSpec.toString();
                            MethodDeclaration newMethodDeclaration = (MethodDeclaration) javaParser.parseBodyDeclaration(newMethodString)
                                    .getResult()
                                    .orElseThrow(() -> new RuntimeException("Failed to parse method"));
                            existingClass.addMember(newMethodDeclaration);
                            newMethodsAdded = true;
                        }
                    }
                } else {
                    // Add the method if it doesn't exist
                    String newMethodString = methodSpec.toString();
                    MethodDeclaration newMethodDeclaration = (MethodDeclaration) javaParser.parseBodyDeclaration(newMethodString)
                            .getResult()
                            .orElseThrow(() -> new RuntimeException("Failed to parse method"));
                    existingClass.addMember(newMethodDeclaration);
                    newMethodsAdded = true;
                }
            }

            // Write the updated class back to the file
            if (newMethodsAdded) {
                FileWriter writer = new FileWriter(classFilePath.toFile());
                writer.write(cu.toString());
                writer.close();
            }

        } else {
            // Create the class if it does not exist
            createTestClass(testJavaDir, packageName, className, methods, baseUrl);
        }
    }

    private static void createTestClass(Path testJavaDir, String packageName, String className, StringBuilder methods, String baseUrl) throws IOException {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .superclass(BaseAPI.class) // Assuming BaseAPI exists
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("super($S)", baseUrl)
                        .build());

        for (MethodSpec methodSpec : parseMethods(methods.toString())) {
            classBuilder.addMethod(methodSpec);
        }

        TypeSpec testClass = classBuilder.build();
        JavaFile javaFile = JavaFile.builder(packageName, testClass)
                .build();
        // Write the generated code to a file
        javaFile.writeTo(testJavaDir);
    }

    private static Iterable<MethodSpec> parseMethods(String methodsStr) {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        // Split the methods string into individual methods
        String[] methodBlocks = methodsStr.split("(?=public void )");  // Split on method declarations

        for (String methodBlock : methodBlocks) {
            methodBlock = methodBlock.trim();
            if (methodBlock.isEmpty()) {
                continue;
            }

            // Extract method signature
            String methodName = extractMethodName(methodBlock);
            if (methodName == null) {
                continue;
            }

            // Extract method body
            String methodBody = extractMethodBody(methodBlock);
            if (methodBody == null) {
                continue;
            }

            // Build MethodSpec
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addAnnotation(Test.class) // Ensure correct Test annotation import
                    .addCode(methodBody);  // Add the method body code

            methodSpecs.add(methodBuilder.build());
        }

        return methodSpecs;
    }

    private static String extractMethodName(String methodBlock) {
        // Extract method signature (e.g., "public void testGetSomething()")
        String signature = methodBlock.split("\\{")[0].trim();
        String[] parts = signature.split("\\s+");
        if (parts.length < 3) {
            return null;
        }

        return parts[2].replaceAll("[^a-zA-Z0-9_]", ""); // Clean method name
    }

    private static String extractMethodBody(String methodBlock) {
        // Extract method body (everything between the curly braces)
        int start = methodBlock.indexOf('{') + 1;
        int end = methodBlock.lastIndexOf('}');
        if (start < 0 || end < 0 || start >= end) {
            return null;
        }

        return methodBlock.substring(start, end).trim();
    }

    private static String formatPathToMethodName(String path) {
        // Convert path to method name
        return path.replaceAll("[^a-zA-Z0-9]", "_");
    }
}