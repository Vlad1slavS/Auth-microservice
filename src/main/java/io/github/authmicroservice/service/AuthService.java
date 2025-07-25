package io.github.authmicroservice.service;

import io.github.authmicroservice.model.dto.JwtResponse;
import io.github.authmicroservice.model.dto.SigninRequest;
import io.github.authmicroservice.model.dto.SignupRequest;

public interface AuthService {

    void signup(SignupRequest request);

    JwtResponse signin(SigninRequest request);

}
