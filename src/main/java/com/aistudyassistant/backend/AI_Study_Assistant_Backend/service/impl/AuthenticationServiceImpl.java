package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.constants.ApplicationConstants;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.requests.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.GeneralAPIResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterVerifyResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.UserProfile;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.Username;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.enums.Role;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.AuthenticationService;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.EmailService;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.JwtService;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.OtpService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final CacheManager cacheManager;
    private final AuthenticationManager authenticationManager;

    @Override
    public ResponseEntity<RegisterResponse> registerUser(RegisterRequest registerRequest) {
        try {
            Optional<User> existingUserOpt = userRepository.findByEmail(registerRequest.getEmail().trim().toLowerCase());
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                if (existingUser.getIsVerified()) {
                    return new ResponseEntity<>(RegisterResponse.builder()
                            .message("User already exists")
                            .build(), HttpStatus.BAD_REQUEST);
                } else {
                    updateUserDetails(existingUser, registerRequest);
                    String otpToBeMailed = otpService.getOtpForEmail(registerRequest.getEmail());
                    CompletableFuture<Integer> emailResponse = emailService.sendEmailWithRetry(registerRequest.getEmail(), otpToBeMailed);
                    if (emailResponse.get() == -1) {
                        return new ResponseEntity<>(RegisterResponse.builder()
                                .message("Failed to send OTP email. Please try again later.")
                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    userRepository.save(existingUser);
                    return new ResponseEntity<>(RegisterResponse.builder()
                            .message("An email with OTP has been sent to your email address. Kindly verify.")
                            .build(), HttpStatus.CREATED);
                }
            } else {
                User newUser = createUser(registerRequest);
                String otpToBeMailed = otpService.getOtpForEmail(registerRequest.getEmail());
                CompletableFuture<Integer> emailResponse = emailService.sendEmailWithRetry(registerRequest.getEmail(),otpToBeMailed);
                if (emailResponse.get() == -1) {
                    return new ResponseEntity<>(RegisterResponse.builder()
                            .message("Failed to send OTP email. Please try again later.")
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                userRepository.save(newUser);
                return new ResponseEntity<>(RegisterResponse.builder()
                        .message("An email with OTP has been sent to your email address. Kindly verify.")
                        .build(), HttpStatus.CREATED);
            }
        } catch (MessagingException | UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RegisterResponse.builder()
                    .message("Failed to send OTP email. Please try again later.")
                    .build());
        }catch(DataIntegrityViolationException ex) {
            return new ResponseEntity<>(RegisterResponse.builder()
                    .message("User already exists with this phone number. Please try again with a different phone number.")
                    .build(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RegisterResponse.builder()
                    .message("Failed to register user. Please try again later.")
                    .build());
        }
    }

    private void updateUserDetails(User user, RegisterRequest registerRequest) {
        DUPLICATE_CODE(registerRequest, user);
    }

    private User createUser(RegisterRequest registerRequest) {
        User user = new User();
        DUPLICATE_CODE(registerRequest, user);
        return user;
    }

    private void DUPLICATE_CODE(RegisterRequest registerRequest, User user) {
        if (registerRequest.getGender().name().equals("FEMALE")) {
            user.setProfilePicture(ApplicationConstants.femaleProfilePicture);
        } else {
            user.setProfilePicture(ApplicationConstants.maleProfilePicture);
        }
        user.setName(new Username(registerRequest.getFirstName().trim(), registerRequest.getLastName().trim()));
        user.setEmail(registerRequest.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setGender(registerRequest.getGender());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setIsVerified(false);
        user.setRole(Role.USER);
    }

    @Override
    public ResponseEntity<?> verifyUserRegistration(RegisterVerifyRequest registerVerifyRequest) {
        String emailEntered = registerVerifyRequest.getEmail().trim().toLowerCase();
        String otpEntered = registerVerifyRequest.getOtp().trim();

        User user = userRepository.findByEmail(emailEntered).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email " + emailEntered)
        );

        String cachedOtp = cacheManager.getCache("user").get(emailEntered, String.class);
        if (cachedOtp == null) {
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Otp has been expired for user " + emailEntered);
        } else if (!otpEntered.equals(cachedOtp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect otp has been entered");
        } else {
            user.setIsVerified(true);
            userRepository.save(user);
            RegisterVerifyResponse jwtToken = jwtService.generateJwtToken(user);
            return new ResponseEntity<>(jwtToken, HttpStatus.CREATED);
        }
    }

    @Override
    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();
        String password = loginRequest.getPassword();
        try {
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email " + email)
            );
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            if (!user.getIsVerified()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not verified");
            }

            RegisterVerifyResponse jwtToken = jwtService.generateJwtToken(user);
            return new ResponseEntity<>(jwtToken, HttpStatus.OK);

        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this email does not exist");
        }
        catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials");
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials");
        }
    }

    @Override
    public ResponseEntity<?> resendOtp(ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail().trim().toLowerCase();
        try {
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email " + email)
            );
            if (cacheManager.getCache("user").get(email, String.class) != null) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Kindly retry after 1 minute");
            }
            String otpToBeSend = otpService.getOtpForEmail(email);
            CompletableFuture<Integer> emailResponse= emailService.sendEmailWithRetry(email,otpToBeSend);
            if (emailResponse.get() == -1) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP email. Please try again later.");
            }
            return new ResponseEntity<>(GeneralAPIResponse.builder().message("An email with OTP has been sent to your email address. Kindly verify.").build(), HttpStatus.OK);

        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP email. Please try again later.");
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with email not found in database");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to resend OTP. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<?> verifyOtp(RegisterVerifyRequest registerVerifyRequest) {
        String email = registerVerifyRequest.getEmail().trim().toLowerCase();
        String otp = registerVerifyRequest.getOtp().trim();
        try {
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email " + email)
            );
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this email does not exist");
        }
        String cachedOtp = cacheManager.getCache("user").get(email, String.class);
        if (cachedOtp == null) {
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Otp has been expired for user " + email);
        } else if (!otp.equals(cachedOtp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect otp has been entered");
        } else {
            return new ResponseEntity<>(GeneralAPIResponse.builder().message("otp verified successfully, now you can change the password").build(), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<?> resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String email = resetPasswordRequest.getEmail().trim().toLowerCase();
        String newPassword = resetPasswordRequest.getPassword();
        String confirmPassword = resetPasswordRequest.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password and confirm password do not match");
        }
        try {
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email " + email)
            );
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return new ResponseEntity<>(GeneralAPIResponse.builder().message("Password has been reset successfully").build(), HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user does not exist with this email");
        }
    }

    @Override
    public ResponseEntity<?> myProfile(ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail().trim().toLowerCase();
        try {
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email " + email)
            );
            return new ResponseEntity<>(UserProfile.builder()
                    .id(user.getId())
                    .firstName(user.getName().getFirstName())
                    .lastName(user.getName().getLastName())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .gender(user.getGender())
                    .profilePicture(user.getProfilePicture())
                    .isOfficiallyEnabled(user.getIsVerified())
                    .build(), HttpStatus.OK);

        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user does not exist with this email");
        }
    }
}