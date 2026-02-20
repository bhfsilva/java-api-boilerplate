package br.com.boilerplate.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class RandomCodeService {
    private final int amountCharacter;

    private RandomCodeService(){
        this.amountCharacter = 6;
    }

    public String generate(){
        Random random = new Random();

        char[] characters =  "0123456789".toCharArray();

        char[] generatePassword = new char[this.amountCharacter];

        for(int i = 0; i < amountCharacter; i++){
            generatePassword[i] = characters[random.nextInt(characters.length)];
        }

        return new String(generatePassword);
    }
}
