package com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.impl;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.GeneralAPIResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RefreshTokenResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.dtos.responses.RegisterVerifyResponse;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.entities.User;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.exceptions.ResourceNotFoundException;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.repository.UserRepository;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.security.JwtHelper;
import com.aistudyassistant.backend.AI_Study_Assistant_Backend.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtHelper jwtHelper;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    public RegisterVerifyResponse generateJwtToken(User user) {
        String access = jwtHelper.generateAccessToken(user);
        String refresh = jwtHelper.generateRefreshToken(user);
        return RegisterVerifyResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .firstName(user.getName().getFirstName())
                .lastName(user.getName().getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .isVerified(user.getIsVerified())
                .build();
    }

    @Override
    public ResponseEntity<?> generateAccessTokenFromRefreshToken(String refreshToken) {
        if(refreshToken != null)
        {
            try
            {
                String username = jwtHelper.extractUsername(refreshToken);
                if(username.startsWith("#refresh"))
                {
                    String finalUserName = username.substring(8);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(finalUserName);
                    User user = userRepository.findByEmail(finalUserName).orElseThrow(
                            ()-> new ResourceNotFoundException("User not found with email "+finalUserName)
                    );
                    if(jwtHelper.isRefreshTokenValid(refreshToken, userDetails))
                    {
                        String accessToken = jwtHelper.generateAccessToken(user);
                        return new ResponseEntity<>(RefreshTokenResponse.builder()
                                .accessToken(accessToken)
                                .firstName(user.getName().getFirstName())
                                .lastName(user.getName().getLastName())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .build() , HttpStatus.OK);
                    }
                    else
                    {
                        return new ResponseEntity<>(GeneralAPIResponse.builder().message("Refresh token is expired").build() , HttpStatus.BAD_REQUEST);
                    }
                }
                else
                {
                    return new ResponseEntity<>(GeneralAPIResponse.builder().message("Invalid refresh token").build() , HttpStatus.BAD_REQUEST);
                }
            }
            catch(IllegalArgumentException | MalformedJwtException e)
            {
                return new ResponseEntity<>(GeneralAPIResponse.builder().message("Invalid refresh token").build() , HttpStatus.BAD_REQUEST);
            }
            catch(ResourceNotFoundException e)
            {
                return new ResponseEntity<>(GeneralAPIResponse.builder().message("User not found").build() , HttpStatus.NOT_FOUND);
            }
            catch(ExpiredJwtException e)
            {
                return new ResponseEntity<>(GeneralAPIResponse.builder().message("Refresh token is expired").build() , HttpStatus.BAD_REQUEST);
            }

        }
        else
        {
            return new ResponseEntity<>(GeneralAPIResponse.builder().message("Refresh token is null").build() , HttpStatus.BAD_REQUEST);
        }

    }
}
