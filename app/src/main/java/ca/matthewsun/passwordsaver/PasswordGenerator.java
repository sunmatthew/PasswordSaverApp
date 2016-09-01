package ca.matthewsun.passwordsaver;


import java.util.Random;

public class PasswordGenerator {
    // Index 0-25
    private char[] lowercase = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    // Index 0-25
    private char[] uppercase = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    // Index 0-9
    private char[] numbers = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    // Index 0-12
    private char[] symbols = {'!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '<', '>', '?'};

    public PasswordGenerator () {

    }

    public String generatePassword (boolean hasLowercase, boolean hasUppercase,
                                    boolean hasNumbers, boolean hasSymbols, int length) {
        char[] passwordChar = new char[length];
        String password = "";
        Random rand = new Random();
        for (int n = 0 ; n < length ; n ++) {
            // Fill up half of the password with lower and upper case (if required)
            if (n < (length / 2)) {
                // If no letters are needed
                if (!hasLowercase && !hasUppercase) {
                    // Fill up with numbers
                    if (hasNumbers) {
                        password += "" + Integer.valueOf(rand.nextInt(10));
                    }
                    // Fill up with symbols
                    else {
                        password += symbols[rand.nextInt(13)];
                    }
                }
                // If both upper and lowercase letters are needed
                else if (hasLowercase && hasUppercase) {
                    // Fill up with uppercase half of the time
                    if (n % 2 == 0) {
                        password += uppercase[rand.nextInt(26)];
                    }
                    // Fill up with lowercase the other half
                    else {
                        password += lowercase[rand.nextInt(26)];
                    }
                }
                // If only uppercase is needed
                else if (hasUppercase) {
                    password += uppercase[rand.nextInt(26)];
                }
                // If only lowercase is needed
                else {
                    password += lowercase[rand.nextInt(26)];
                }
            }
            // Fill up the second half of the password with numbers and symbols if required
            else {
                // If no numbers and symbols are required
                if (!hasNumbers && !hasSymbols) {
                    // Fill up with lowercase letters
                    if (hasLowercase) {
                        password += lowercase[rand.nextInt(26)];
                    }
                    // Fill up with uppercase letters
                    else {
                        password += uppercase[rand.nextInt(26)];
                    }
                }
                // If both numbers and symbols are needed
                else if (hasNumbers && hasSymbols) {
                    if (n % 2 == 0) {
                        password += symbols[rand.nextInt(13)];
                    }
                    else {
                        password += Integer.valueOf(rand.nextInt(10));
                    }
                }
                // If only numbers are needed
                else if (hasNumbers) {
                    password += Integer.valueOf(rand.nextInt(10));
                }
                // If only symbols are needed
                else if (hasSymbols) {
                    password += symbols[rand.nextInt(13)];
                }
            }
        }

        // Jumble up the characters in the password by performing swaps
        String scrambled = "";

        while (password.length() != 0)
        {
            int index = (int) Math.floor(Math.random() * password.length());
            char c = password.charAt(index);
            password = password.substring(0,index) + password.substring(index+1);
            scrambled += c;
        }

        return scrambled;

    }
}
