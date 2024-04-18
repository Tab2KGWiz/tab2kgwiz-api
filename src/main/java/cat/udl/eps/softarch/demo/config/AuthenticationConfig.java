package cat.udl.eps.softarch.demo.config;

import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.filter.AuthTokenFilter;
import cat.udl.eps.softarch.demo.repository.SupplierRepository;
import cat.udl.eps.softarch.demo.domain.Supplier;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class AuthenticationConfig extends GlobalAuthenticationConfigurerAdapter {

    @Value("${default-password}")
    String defaultPassword;

    final BasicUserDetailsService basicUserDetailsService;

    final UserRepository userRepository;

    final SupplierRepository supplierRepository;

    public AuthenticationConfig(BasicUserDetailsService basicUserDetailsService, UserRepository userRepository,
                                SupplierRepository supplierRepository) {
        this.basicUserDetailsService = basicUserDetailsService;
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
    }

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(basicUserDetailsService)
                .passwordEncoder(User.passwordEncoder);


        //Sample User
        if (!userRepository.existsById("demo")) {
            User user = new User();
            user.setEmail("demo@sample.app");
            user.setUsername("demo");
            user.setPassword(defaultPassword);
            user.encodePassword();
            userRepository.save(user);
        }

        if (!supplierRepository.existsById("demoSupplier")) {
            Supplier supplier = new Supplier();
            supplier.setUsername("demoSupplier");
            supplier.setEmail("demoSupplier@sample.app");
            supplier.setPassword(defaultPassword);
            supplier.encodePassword();
            supplierRepository.save(supplier);
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    @Primary
    public AuthenticationManagerBuilder configureAuthenticationManagerBuilder(
            AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(basicUserDetailsService).passwordEncoder(bCryptPasswordEncoder());
        return authenticationManagerBuilder;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
