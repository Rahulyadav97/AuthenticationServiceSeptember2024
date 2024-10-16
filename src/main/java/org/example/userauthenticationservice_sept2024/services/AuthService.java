package org.example.userauthenticationservice_sept2024.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.example.userauthenticationservice_sept2024.models.Session;
import org.example.userauthenticationservice_sept2024.models.SessionState;
import org.example.userauthenticationservice_sept2024.models.User;
import org.example.userauthenticationservice_sept2024.repos.SessionRepo;
import org.example.userauthenticationservice_sept2024.repos.UserRepository;
import org.example.userauthenticationservice_sept2024.exceptions.UserAlreadyExistsException;
import org.example.userauthenticationservice_sept2024.exceptions.UserNotFoundException;
import org.example.userauthenticationservice_sept2024.exceptions.WrongPasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    SessionRepo sessionRepository;

    @Autowired
    private BCryptPasswordEncoder bcryptPasswordEncoder;

    @Autowired

    private SecretKey secretKey;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public boolean signUp(String email, String password) {
        User user = new User();
        user.setEmail(email);
        String hashedPassword = bcryptPasswordEncoder.encode(password);
        //user.setPassword(password);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        return true;
    }

    public Pair<Boolean,String>  login(String email, String password) throws UserNotFoundException, WrongPasswordException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User with email: " + email + " not found.");
        }
        //boolean matches = password.equals(userOptional.get().getPassword());
        boolean matches = bcryptPasswordEncoder.matches(password,userOptional.get().getPassword());
        System.out.println("matches: "+matches);
        MacAlgorithm algorithm = Jwts.SIG.HS256;
        SecretKey secretKey = algorithm.key().build();
        // byte[] content = message.getBytes(StandardCharsets.UTF_8);

        Map<String,Object> claims  = new HashMap<>();
        Long currentTimeInMillis = System.currentTimeMillis();
        claims.put("iat",currentTimeInMillis);
        claims.put("exp",currentTimeInMillis+864000);
        claims.put("user_id",userOptional.get().getId());
        claims.put("issuer","scaler");

        String token  = Jwts.builder().claims(claims).signWith(secretKey).compact();
        Session session = new Session();
        session.setToken(token);
//        session.setUser(userOptional.get());
        session.setSessionState(SessionState.ACTIVE);
        sessionRepository.save(session);
        if (matches) {
            return new Pair<Boolean,String>(true,token);
        } else {
            throw new WrongPasswordException("Wrong password.");
        }
    }

    public Boolean validateToken(Long userId, String token) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return false;
        }

        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        Long ExpiryTime = (Long) claims.get("exp");
        Long currentTimeInMillis = System.currentTimeMillis();

        if (ExpiryTime < currentTimeInMillis) {
            return false;
        }
        if (!claims.get("user_id").equals(userId)) {
            return false;
        }
        if (!claims.get("issuer").equals("scaler")) {
            return false;
        }
        return true;
    }
}
