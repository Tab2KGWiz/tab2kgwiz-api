package cat.udl.eps.softarch.demo.dto;

import lombok.Data;

@Data
public class SignInRequest {
    private String username;
    private String password;
}