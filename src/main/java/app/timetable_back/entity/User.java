package app.timetable_back.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "First_name")
    @NotBlank
    @Size(max = 100)
    private String firstName;

    @Column(name = "Last_name")
    @NotBlank
    @Size(max = 100)
    private String lastName;

    @Column(name = "Email")
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @Column(name = "Password_hash")
    @NotBlank
    @Size(min = 4)
    private String passwordHash;

    @Column(name = "Role")
    @NotBlank
    @Size(max = 50)
    private String role;

    @Column(name = "Phone")
    @Size(max = 20)
    private String phone;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}