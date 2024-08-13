package org.example;

import java.util.Map;

public class UserAPI extends BaseAPI {

    public UserAPI(String baseUrl) {
        super(baseUrl);
    }

    public String getUsers() throws Exception {
        return makeRequest("/users", "GET", null, null);
    }

    public String getUserById(int id) throws Exception {
        return makeRequest("/users/" + id, "GET", null, null);
    }

    public String createUser(String jsonBody) throws Exception {
        return makeRequest("/users", "POST", null, jsonBody);
    }

    public String updateUser(int id, String jsonBody) throws Exception {
        return makeRequest("/users/" + id, "PUT", null, jsonBody);
    }

    public String deleteUser(int id) throws Exception {
        return makeRequest("/users/" + id, "DELETE", null, null);
    }
}
