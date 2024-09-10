package org.example;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.servers.Server;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
public class TestCaseGenerator {

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
        List<Server> servers = openAPI.getServers();
        if (servers != null && !servers.isEmpty()) {
            return servers.get(0).getUrl();
        } else {
            return "http://localhost";  // Default base URL
        }
    }

    private static void generateTestCases(OpenAPI openAPI, String baseUrl) throws IOException {
        String testJavaDir = "src/test/java/org/example/tests";

        // Create directories for each request type if they don't exist
        createDirectoryIfNotExists(testJavaDir + "/get");
        createDirectoryIfNotExists(testJavaDir + "/post");
        createDirectoryIfNotExists(testJavaDir + "/put");
        createDirectoryIfNotExists(testJavaDir + "/delete");

        for (String path : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(path);

            if (pathItem.getGet() != null) {
                generateTestCaseForGet(testJavaDir + "/get", "org.example.tests.get", path, pathItem.getGet(), baseUrl);
            }
            if (pathItem.getPost() != null) {
                generateTestCaseForPost(testJavaDir + "/post", "org.example.tests.post", path, pathItem.getPost(), baseUrl);
            }
            if (pathItem.getPut() != null) {
                generateTestCaseForPut(testJavaDir + "/put", "org.example.tests.put", path, pathItem.getPut(), baseUrl);
            }
            if (pathItem.getDelete() != null) {
                generateTestCaseForDelete(testJavaDir + "/delete", "org.example.tests.delete", path, pathItem.getDelete(), baseUrl);
            }
        }
    }

    private static void createDirectoryIfNotExists(String dirPath) throws IOException {
        Path pathToCreate = Paths.get(dirPath);
        if (!Files.exists(pathToCreate)) {
            Files.createDirectories(pathToCreate);
        }
    }

    private static void generateTestCaseForGet(String dir, String packageName, String path, Operation operation, String baseUrl) throws IOException {
        String className = "TestGet" + path.replace("/", "").replace("{", "").replace("}", "");
        String filePath = dir + "/" + className + ".java";

        if (checkIfClassExists(filePath)) {
            System.out.println(className + " already exists. Skipping generation.");
            return;
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writeTestCaseHeader(writer, className, packageName, baseUrl);

            writer.write("    @Test\n");
            writer.write("    public void testGetRequest() {\n");
            writer.write("        Map<String, String> queryParams = new HashMap<>();\n");

            if (operation.getParameters() != null) {
                for (Parameter parameter : operation.getParameters()) {
                    if ("query".equals(parameter.getIn())) {
                        writer.write("        queryParams.put(\"" + parameter.getName() + "\", \"sampleValue\");\n");
                    }
                }
            }

            writer.write("        Response response = getRequest(\"" + path + "\", queryParams);\n");
            writer.write("        response.then().statusCode(200);\n");
            writer.write("    }\n");

            writer.write("}\n");
        }
    }

    private static void generateTestCaseForPost(String dir, String packageName, String path, Operation operation, String baseUrl) throws IOException {
        String className = "TestPost" + path.replace("/", "").replace("{", "").replace("}", "");
        String filePath = dir + "/" + className + ".java";

        if (checkIfClassExists(filePath)) {
            System.out.println(className + " already exists. Skipping generation.");
            return;
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writeTestCaseHeader(writer, className, packageName, baseUrl);

            writer.write("    @Test\n");
            writer.write("    public void testPostRequest() {\n");
            writer.write("        Map<String, Object> bodyParams = new HashMap<>();\n");

            // Add logic to generate sample body parameters, if available

            writer.write("        Response response = postRequest(\"" + path + "\", bodyParams);\n");
            writer.write("        response.then().statusCode(200);\n");
            writer.write("    }\n");

            writer.write("}\n");
        }
    }

    private static void generateTestCaseForPut(String dir, String packageName, String path, Operation operation, String baseUrl) throws IOException {
        String className = "TestPut" + path.replace("/", "").replace("{", "").replace("}", "");
        String filePath = dir + "/" + className + ".java";

        if (checkIfClassExists(filePath)) {
            System.out.println(className + " already exists. Skipping generation.");
            return;
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writeTestCaseHeader(writer, className, packageName, baseUrl);

            writer.write("    @Test\n");
            writer.write("    public void testPutRequest() {\n");
            writer.write("        Map<String, Object> bodyParams = new HashMap<>();\n");

            // Add logic to generate sample body parameters, if available

            writer.write("        Response response = putRequest(\"" + path + "\", bodyParams);\n");
            writer.write("        response.then().statusCode(200);\n");
            writer.write("    }\n");

            writer.write("}\n");
        }
    }

    private static void generateTestCaseForDelete(String dir, String packageName, String path, Operation operation, String baseUrl) throws IOException {
        String className = "TestDelete" + path.replace("/", "").replace("{", "").replace("}", "");
        String filePath = dir + "/" + className + ".java";

        if (checkIfClassExists(filePath)) {
            System.out.println(className + " already exists. Skipping generation.");
            return;
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writeTestCaseHeader(writer, className, packageName, baseUrl);

            writer.write("    @Test\n");
            writer.write("    public void testDeleteRequest() {\n");

            writer.write("        Response response = deleteRequest(\"" + path + "\");\n");
            writer.write("        response.then().statusCode(200);\n");
            writer.write("    }\n");

            writer.write("}\n");
        }
    }

    private static boolean checkIfClassExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    private static void writeTestCaseHeader(FileWriter writer, String className, String packageName, String baseUrl) throws IOException {
        writer.write("package " + packageName + ";\n\n");
        writer.write("import io.restassured.response.Response;\n");
        writer.write("import org.testng.annotations.Test;\n");
        writer.write("import java.util.HashMap;\n");
        writer.write("import java.util.Map;\n");
        writer.write("public class " + className + " extends BaseAPI {\n");
        writer.write("    public " + className + "() {\n");
        writer.write("        super(\"" + baseUrl + "\");\n");
        writer.write("    }\n");
    }
}
