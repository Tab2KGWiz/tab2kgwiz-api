package cat.udl.eps.softarch.demo.controller;


import cat.udl.eps.softarch.demo.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            if (jwtUtil.validateJwtToken(token)) {
                return ResponseEntity.ok().body("Token is valid");
            } else {
                return ResponseEntity.badRequest().body("Token is invalid");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Token is invalid");
        }
    }
}
