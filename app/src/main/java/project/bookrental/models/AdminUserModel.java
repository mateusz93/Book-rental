package project.bookrental.models;

/**
 * Created by Mateusz on 16.01.2018.
 */

public class AdminUserModel {

    private String email;

    public AdminUserModel(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
