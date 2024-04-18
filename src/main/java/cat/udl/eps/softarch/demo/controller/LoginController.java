package cat.udl.eps.softarch.demo.controller;

import cat.udl.eps.softarch.demo.config.AuthenticationConfig;
import cat.udl.eps.softarch.demo.config.BasicUserDetailsImpl;
import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.dto.JwtResponse;
import cat.udl.eps.softarch.demo.dto.SignInRequest;
import cat.udl.eps.softarch.demo.dto.SignUpRequest;
import cat.udl.eps.softarch.demo.repository.SupplierRepository;
import cat.udl.eps.softarch.demo.utils.JwtUtil;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class LoginController {
    private SupplierRepository supplierRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;


    public LoginController(SupplierRepository supplierRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.supplierRepository = supplierRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody SignInRequest signInRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                signInRequest.getUsername(), signInRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtil.generateJwtToken(authentication);

        BasicUserDetailsImpl userDetails = (BasicUserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        JwtResponse res = new JwtResponse();
        res.setToken(jwt);
        res.setId(userDetails.getId());
        res.setUsername(userDetails.getUsername());
        res.setRoles(roles);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpRequest signUpRequest) {
        if (supplierRepository.existsById(signUpRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("username is already taken");
        }
        if (supplierRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("email is already taken");
        }
        String hashedPassword = passwordEncoder.encode(signUpRequest.getPassword());

        Supplier supplier = new Supplier();
        supplier.setUsername(signUpRequest.getUsername());
        supplier.setEmail(signUpRequest.getEmail());
        supplier.setPassword(hashedPassword);
        supplierRepository.save(supplier);
        return ResponseEntity.ok("Supplier registered success");
    }
}
