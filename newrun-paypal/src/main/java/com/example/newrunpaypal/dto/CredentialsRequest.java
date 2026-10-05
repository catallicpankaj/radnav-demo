package com.example.newrunpaypal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CredentialsRequest {
    @NotBlank
    private String client_id;
    @NotBlank
    private String client_secret;
}