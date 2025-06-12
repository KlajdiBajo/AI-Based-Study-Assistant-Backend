package com.aistudyassistant.backend.AI_Study_Assistant_Backend.controller;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.requests.*;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.GeneralAPIResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RefreshTokenResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterVerifyResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.AuthenticationService;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "This API registers a new user if the user is not already present in the records, and sends an email to the user for verification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User has been saved in records, but still needs to be verified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = """
                Bad Request:
                - User already exists with this email and is verified.
                - Validation errors:
                  - First name can't be blank
                  - Last name can't be blank
                  - Invalid email entered
                  - Password must contain at least 8 characters, one uppercase, one lowercase, one special character and one number
                  - Please choose your gender
                  - Invalid phone number, please enter in the format +(code)XXXXXXXXXX
                - User already exists with this phone number. Please try again with a different phone number.
            """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Failed to send OTP email. Please try again later.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Error.class)))
    })
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Register request received for email: {}", registerRequest.getEmail());
        return authenticationService.registerUser(registerRequest);
    }

    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verify user registration", description = "This API verifies user registration using the provided OTP.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registration verified successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegisterVerifyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request: Email or OTP is incorrect",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "408", description = "Request Timeout: OTP has expired",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: User with the specified email not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Failed to verify user registration",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class)))
    })
    public ResponseEntity<?> verifyRegistration(@Valid @RequestBody RegisterVerifyRequest registerVerifyRequest) {
        log.info("Registration verification request received for email {}", registerVerifyRequest.getEmail());
        return authenticationService.verifyUserRegistration(registerVerifyRequest);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Login user", description = "Authenticate and log in a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegisterVerifyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid credentials or user not verified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: User with the specified email not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class)))
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for email {}", loginRequest.getEmail());
        return authenticationService.loginUser(loginRequest);
    }

    @PostMapping(value = "/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Forgot password", description = "Send OTP to user's email for resetting password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: User with the specified email not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too Many Requests: OTP already sent recently, wait for 2 minutes before trying again",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Failed to send OTP email",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class)))
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        log.info("Forgot password request received for email {}", forgotPasswordRequest.getEmail());
        return authenticationService.resendOtp(forgotPasswordRequest);
    }

    @PostMapping(value = "/verify-otp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verify OTP", description = "Verify the OTP provided by the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified successfully, now you can change the password",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request: Incorrect OTP entered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: User with the specified email not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "408", description = "Request Timeout: OTP has expired",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class)))
    })
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody RegisterVerifyRequest registerVerifyRequest) {
        log.info("OTP verification request received for email {}", registerVerifyRequest.getEmail());
        return authenticationService.verifyOtp(registerVerifyRequest);
    }

    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Reset Password", description = "Reset the password for the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password has been reset successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request: Password and confirm password do not match",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: User with the specified email not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class)))
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        log.info("Password reset request received for email {}", resetPasswordRequest.getEmail());
        return authenticationService.resetPassword(resetPasswordRequest);
    }

    @GetMapping("/getRefreshToken")
    @Operation(summary = "Refresh Token", description = "Generate a new access token from a refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK: Access token generated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid refresh token or refresh token expired",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralAPIResponse.class)))
    })
    public ResponseEntity<?> refreshToken(@RequestParam(name = "refreshToken") String refreshToken) {
        log.info("Refresh token request received");
        return jwtService.generateAccessTokenFromRefreshToken(refreshToken);
    }

    @PostMapping("/hello")
    @Operation(summary = "Health Check", description = "Keep-alive endpoint for deployment platforms")
    public ResponseEntity<GeneralAPIResponse> hello() {
        log.info("Hello request received");
        return new ResponseEntity<>(
                GeneralAPIResponse.builder()
                        .message("This API is automated, for doing cronJob so that render does not get turned off")
                        .build(),
                HttpStatus.OK
        );
    }
}