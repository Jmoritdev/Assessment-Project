package eu.iunxi.apps.assessment.model;

import eu.iunxi.apps.assessment.util.PasswordDigester;
import eu.iunxi.apps.assessment.util.Permission;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 *
 * @author joey
 */
@Entity
public class User implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NonVisual
    private int id;
    
    @Property
    @Validate("required")
    private String name;
    
    @Property
    @Validate("required")
    private String email;
    
    @Property
    @Validate("required")
    private String passwordHash;
    
    @Column(nullable = false, columnDefinition = "BIT", length = 1)
    private boolean deleted;
    
    @ElementCollection
    @Cascade(CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "User_permission", joinColumns = @JoinColumn(name = "User_id"))
    private Set<Permission> permissions;
    
    /**
     * true if the password is temporary and must be changed
     */
    @Column(nullable = false, columnDefinition = "BIT", length = 1)
    private boolean tempPassword;
    

    public User() {
        this.permissions = new HashSet<Permission>();
    }
    
    public User(String name, String email, String plainPassword) throws NoSuchAlgorithmException{
        this.name = name;
        this.email = email;
        this.passwordHash = PasswordDigester.getDigest(plainPassword);
        this.permissions = new HashSet<Permission>();
    }
    
    public User(String name, String email, String plainPassword, Set<Permission> permissions) throws NoSuchAlgorithmException{
        this.name = name;
        this.email = email;
        this.passwordHash = PasswordDigester.getDigest(plainPassword);
        this.permissions = new HashSet<Permission>();
        this.permissions.addAll(permissions);
    }
    

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }


    public void setPassword(String plaintext) throws NoSuchAlgorithmException {
        this.passwordHash = PasswordDigester.getDigest(plaintext);
    }
    
    public String getPassword(){
        return passwordHash;
    }
    
    public boolean checkPassword(String plaintext) throws NoSuchAlgorithmException {
        if (plaintext == null) return false;
        return PasswordDigester.getDigest(plaintext).equals(this.passwordHash);
    }

    /**
     * @return the deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * soft deletes the user and clears all permissions
     * @param deleted 
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        this.permissions.clear();
    }
    
    public User addPermission(Permission permission){
        permissions.add(permission);
        return this;
    }
    
    public User removePermission(Permission permission){
        if (permissions.contains(permission)) permissions.remove(permission);
        return this;
    }
    
    public void setPermissions(Set<Permission> permissions){
        this.permissions.clear();
        this.permissions = permissions;
    }
    
    public boolean hasPermissionTo(Permission permission){
        return permissions.contains(permission);
    }

    /**
     * @return the tempPassword
     */
    public boolean hasTempPassword() {
        return tempPassword;
    }

    /**
     * @param tempPassword the tempPassword to set
     */
    public void setTempPassword(boolean tempPassword) {
        this.tempPassword = tempPassword;
    }
    
}
