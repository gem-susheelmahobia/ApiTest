package org.example;

public class Main {
    public static void main(String[] args) {


        UserAPI api = new UserAPI("https://reqres.in/api");

        try {
            // Get a list of users
            String response = api.getUsers();
            System.out.println("Get Users Response: " + response);

            // Get a user by ID
            response = api.getUserById(2);
            System.out.println("Get User by ID Response: " + response);

            // Create a new user
            String newUserJson = "{\"name\": \"John Doe\", \"job\": \"Software Engineer\"}";
            response = api.createUser(newUserJson);
            System.out.println("Create User Response: " + response);

            // Update an existing user
            String updateUserJson = "{\"name\": \"John Doe\", \"job\": \"Senior Software Engineer\"}";
            response = api.updateUser(2, updateUserJson);
            System.out.println("Update User Response: " + response);

            // Delete a user
            response = api.deleteUser(2);
            System.out.println("Delete User Response: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}