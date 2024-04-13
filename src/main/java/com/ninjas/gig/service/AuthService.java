package com.ninjas.gig.service;

import com.ninjas.gig.dto.AuthResponseDTO;
import com.ninjas.gig.dto.UserLoginDTO;
import com.ninjas.gig.dto.UserRegisterDTO;
import com.ninjas.gig.entity.UserAccount;
import com.ninjas.gig.entity.UserType;
import com.ninjas.gig.repository.UserRepository;
import com.ninjas.gig.security.JWTGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;
    JWTGenerator jwtGenerator;


    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JWTGenerator jwtGenerator){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
    }

    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            return new ResponseEntity<>("Username is taken!", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(registerDTO.getEmail())){
            return new ResponseEntity<>("Email is taken!", HttpStatus.BAD_REQUEST);
        }
        UserAccount newUser = new UserAccount();
        newUser.setName(registerDTO.getName());
        newUser.setEmail(registerDTO.getEmail());
        newUser.setUsername(registerDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        if (registerDTO.getUserType() == null){
            newUser.setUserType(UserType.CUSTOMER);
        } else {
            newUser.setUserType(registerDTO.getUserType());
        }
        newUser.setPhoto("https://static.vecteezy.com/system/resources/thumbnails/009/292/244/small/default-avatar-icon-of-social-media-user-vector.jpg");
        userRepository.save(newUser);
        var token = jwtGenerator.generateToken(newUser);
        return new ResponseEntity<>(new AuthResponseDTO(token), HttpStatus.OK);
    }

    public ResponseEntity<?> loginUser(@RequestBody UserLoginDTO loginDTO){
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(),
                        loginDTO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            return new ResponseEntity<>(new AuthResponseDTO(token), HttpStatus.OK);
        } catch (AuthenticationException authEx) {
            authEx.printStackTrace();
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
        }
    }
}
