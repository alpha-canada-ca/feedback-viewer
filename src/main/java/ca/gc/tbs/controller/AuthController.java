package ca.gc.tbs.controller;

import ca.gc.tbs.domain.Role;
import ca.gc.tbs.domain.User;
import ca.gc.tbs.security.JWTUtil;
import ca.gc.tbs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashSet;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/authenticate")
    public ResponseEntity<String> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Check if the user is an admin
            if (!userService.isAdmin(userService.findUserByEmail(userDetails.getUsername())) && !userService.isAPI(userService.findUserByEmail(userDetails.getUsername()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Only API users & Admins can generate tokens.");
            }

            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/createApiUser")
    public ResponseEntity<String> createApiUser(@RequestBody CreateUserRequest request) {
        // Check if user already exists
        User existingUser = userService.findUserByEmail(request.getUsername());
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists.");
        }

        User user = new User();
        user.setEmail(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEnabled(true);

        Role apiRole = userService.findRoleByName("API");
        if (apiRole == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("API role does not exist.");
        }
        user.setRoles(new HashSet<>(Arrays.asList(apiRole)));

        userService.saveApiUser(user);
        return ResponseEntity.ok("API user created successfully.");
    }

    static class CreateUserRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // Request body for authentication
    static class AuthRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
