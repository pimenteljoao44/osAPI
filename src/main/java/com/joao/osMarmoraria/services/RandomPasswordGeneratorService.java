package com.joao.osMarmoraria.services;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
@Service
public class RandomPasswordGeneratorService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";

    private static final SecureRandom random = new SecureRandom();

    private static final int PASSWORD_LENGTH = 12;

    public String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();
    }
}
