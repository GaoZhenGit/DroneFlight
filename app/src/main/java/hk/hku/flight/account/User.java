package hk.hku.flight.account;

public class User {
    public String name;
    public String email;
    public String avatar;
    public String id;
    public String token;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", id='" + id + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
