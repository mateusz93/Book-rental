package project.bookrental.models;

/**
 * @author Mateusz Wieczorek
 */
public class UserModel {

    private String uid;
    private String email;
    private String displayName;
    private String phoneNumber;

    public UserModel(String uId, String email, String displayName, String phoneNumber) {
        this.uid = uId;
        this.email = email;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
    }

    public String getUId() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
