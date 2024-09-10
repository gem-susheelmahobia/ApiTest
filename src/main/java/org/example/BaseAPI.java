package org.example;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class BaseAPI {

    private final String baseUrl;

    public BaseAPI(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Method to make a request for any HTTP method
     *
     * @param method      The HTTP method (e.g., GET, POST, PUT, DELETE, etc.)
     * @param path        The API path
     * @param queryParams Query parameters for the request (optional)
     * @return Response object
     */
    protected Response makeRequest(Method method, String path, Map<String, String> queryParams) {
        RequestSpecification request = RestAssured.given().baseUri(baseUrl);

        // Add query parameters if available
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }

        // Make the request based on the provided HTTP method
        return request.request(method, path);
    }

    /**
     * Method to make a GET request
     *
     * @param path        The API path
     * @param queryParams Query parameters for the request
     * @return Response object
     */
    protected Response getRequest(String path, Map<String, String> queryParams) {
        return makeRequest(Method.GET, path, queryParams);
    }

    /**
     * Method to make a POST request
     *
     * @param path       The API path
     * @param bodyParams Body parameters for the request
     * @return Response object
     */
    protected Response postRequest(String path, Map<String, Object> bodyParams) {
        return makeRequest(Method.POST, path, null);
    }

    /**
     * Method to make a PUT request
     *
     * @param path       The API path
     * @param bodyParams Body parameters for the request
     * @return Response object
     */
    protected Response putRequest(String path, Map<String, Object> bodyParams) {
        return makeRequest(Method.PUT, path, null);
    }

    /**
     * Method to make a DELETE request
     *
     * @param path       The API path
     * @param bodyParams
     * @return Response object
     */
    protected Response deleteRequest(String path, Map<String, Object> bodyParams) {
        return makeRequest(Method.DELETE, path, null);
    }

    /**
     * Method to make a PATCH request
     *
     * @param path       The API path
     * @param bodyParams Body parameters for the request
     * @return Response object
     */
    protected Response patchRequest(String path, Map<String, String> bodyParams) {
        return makeRequest(Method.PATCH, path, null);
    }

    /**
     * Method to make an OPTIONS request
     *
     * @param path The API path
     * @return Response object
     */
    protected Response optionsRequest(String path) {
        return makeRequest(Method.OPTIONS, path, null);
    }

    /**
     * Method to make a HEAD request
     *
     * @param path The API path
     * @return Response object
     */
    protected Response headRequest(String path) {
        return makeRequest(Method.HEAD, path, null);
    }
}
