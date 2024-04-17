package cat.udl.eps.softarch.demo.controller;

import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.repository.SupplierRepository;
import cat.udl.eps.softarch.demo.utils.JwtUtil;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@RestController
public class LoginController {
    private final SupplierRepository supplierRepository;
    private PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;


    public LoginController(SupplierRepository supplierRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.supplierRepository = supplierRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Supplier supplier) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Optional<Supplier> supplierExist = supplierRepository.findById(supplier.getUsername());

        if (supplierExist.isPresent() && passwordEncoder.matches(supplier.getPassword(), supplierExist.get().getPassword())) {
            Authentication authentication = authenticationManager.authenticate(new
                    UsernamePasswordAuthenticationToken(supplier.getUsername(), supplier.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

//            Cookie cookie = new Cookie("username", supplier.getUsername());
//            cookie.setHttpOnly(true);
//            cookie.setPath("/");
//            response.addCookie(cookie);

            //System.out.println("login" + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            return ResponseEntity.ok().body("Login successful");
        } else {
            return ResponseEntity.badRequest().body("Login failed");
        }
    }

    @RequestMapping("/userLoggedIn")
    public Object userLoggedIn() {
        //System.out.println("userLoggedIn" + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
