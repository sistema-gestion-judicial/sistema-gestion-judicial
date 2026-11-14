package mx.edu.utez.judicial.audiencias.model.usuario;

public class User {

    private String professionalLicense;
    private Role role;
    private Boolean status;
    private String email;
    private String fullName;
    private String matter;

    private String phone;

    public User() {
    }

    public User(String professionalLicense, Role role, Boolean status, String email, String fullName) {
        this.professionalLicense = professionalLicense;
        this.role = role;
        this.status = status;
        this.email = email;
        this.fullName = fullName;
    }

    public User(String email, String fullName, Role role, Boolean status) {
        this.role = role;
        this.status = status;
        this.email = email;
        this.fullName = fullName;
    }

    public User(String fullName, String professionalLicense, boolean status, String matter) {
        this.professionalLicense = professionalLicense;
        this.fullName = fullName;
        this.status = status;
        this.matter = matter;
    }

    public String getProfessionalLicense() {
        return professionalLicense;
    }

    public void setProfessionalLicense(String professionalLicense) {
        this.professionalLicense = professionalLicense;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMatter() {
        return matter;
    }

    public void setMatter(String matter) {
        this.matter = matter;
    }
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}

