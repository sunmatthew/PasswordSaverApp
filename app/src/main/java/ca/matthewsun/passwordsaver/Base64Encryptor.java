package ca.matthewsun.passwordsaver;


import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


public class Base64Encryptor {


    public Base64Encryptor() {

    }

    /**
     * Finds the command in a given decrypted string
     * @param s the given decrypted line
     * @return the corresponding number to the command
     */
    public int findCommand (String s) {
        // Given string is for a category
        if (s.indexOf("[%c]") != -1)
            return 1;
        // Given string is for a username
        else if (s.indexOf("[%u]") != -1)
            return 2;
        // Given string is for a password
        else if (s.indexOf("[%p]") != -1)
            return 3;
        // Given string is for a note
        else if (s.indexOf("[%n]") != -1)
            return 4;
        // Given string is for the name of the item
        else
            return 5;
    }

    /**
     * Removes the content of the given string leaving only the commands
     * @param line the line to change
     * @return only the command of the given line
     */
    public String removeContent (String line) {
        int index;
        // Category
        if (findCommand(line) == 1) {
            index = line.indexOf("]");
            return line.substring(0, index + 1);
        }
        // Username
        else if (findCommand(line) == 2) {
            index = line.indexOf("[%u]");
            return line.substring(0, index);
        }
        // Password
        else if (findCommand(line) == 3) {
            index = line.indexOf("[%p]");
            return line.substring(0, index);
        }
        // Note
        else if (findCommand(line) == 4) {
            index = line.indexOf("[%n]");
            return line.substring(0, index);
        }
        // Item name
        else {
            index = line.indexOf("]");
            return line.substring(0, index + 1);
        }
    }

    /**
     * Checks if the given line is of the given item name
     * @param line the given line to check
     * @param itemName the item name to compare it to
     * @return whether the line is of the item name
     */
    public boolean checkItemName (String line, String itemName) {
        // Item
        if (findCommand(line) == 5) {
            if (removeCommand(line).equals(itemName))
                return true;
        }
        // Not an item
        else {
            if (removeContent(line).indexOf(itemName) != -1)
                return true;
        }
        return false;
    }

    /**
     * Changes a given line to have the item name given
     * @param line the given line to change
     * @param itemName the item name to change to
     * @return the new line
     */
    public String changeItemName (String line, String itemName) {
        // Already of the same item name
        if (checkCategory(line, itemName))
            return line;
        else if (findCommand(line) == 5){
            return line.substring(0, line.indexOf("]") + 1) + itemName;
        }
        else {
            return line.substring(0, line.indexOf("]") + 1) + "[" + itemName + "]["
                    + line.substring(line.indexOf("%", line.indexOf("%") + 1));
        }
    }

    /**
     * Checks if the given decrypted line is of the given category
     * @param line the given decrypted line
     * @param category the given category
     * @return whether or not they are of the same category
     */
    public boolean checkCategory (String line, String category) {
        int index = line.indexOf("]");
        if (line.substring(2, index).equals(category))
            return true;
        else if (findCommand(line) == 1 && line.substring(index + 1).equals(category))
            return true;
        return false;
    }

    /**
     * Changes the category of a given decrypted line
     * @param line the given decrypted line
     * @param category the given category to change to
     * @return the new line
     */
    public String changeCategory (String line, String category) {
        String newLine;
        // Given line is a category
        if (findCommand(line) == 1) {
            newLine = "%[%c]" + category;
        }
        else {
            newLine = "%[" + category + "]" + line.substring(line.indexOf("]") + 1);
        }
        return newLine;
    }

    /**
     * Checks if the given decrypted line is of the given category and item
     * @param line the given decrypted line
     * @param category the given category
     * @param itemName the given iten
     * @return whether or not they are of the same category and item
     */
    public boolean checkCategoryAndItem (String line, String category, String itemName) {
        // Line is of a matching category
        if (checkCategory(line, category)) {
            // Individual Item
            if (findCommand((line)) == 5 && line.contains(itemName)) {
                return true;
            }
            // Category
            else if (findCommand(line) == 1)
                return false;
            // Either a password, username, or note
            else {
//                // Remove the category from the line
//                line = line.substring(line.indexOf("]") + 1);
//                // Check if the item name matches
//                if (findCommand(line) > 1 && (line.substring(0, line.indexOf("]"))).contains(itemName))
//                    return true;

                if (removeContent(line).indexOf(itemName) != -1)
                    return true;
            }
        }
        return false;
    }


    /**
     * Removes the command from a given already decrypted line
     * @param line the given decrypted line
     * @return a decrypted string with no command
     */
    public String removeCommand (String line) {
        String string = "";
        int index;
        // Category
        if (findCommand(line) == 1) {
            return line.substring(5);
        }
        // Username
        else if (findCommand(line) == 2) {
            index = line.indexOf("[%u]");
            return line.substring(index + 4);
        }
        // Password
        else if (findCommand(line) == 3) {
            index = line.indexOf("[%p]");
            return line.substring(index + 4);
        }
        // Note
        else if (findCommand(line) == 4) {
            index = line.indexOf("[%n]");
            return line.substring(index + 4);
        }
        // Item name
        else if (findCommand(line) == 5) {
            index = line.indexOf("]");
            return line.substring(index + 1);
        }
        return string;
    }

    public String encrypt(String str) {
        try {
            byte[] encryptedBytes = str.getBytes("UTF-8");
            return Base64.encodeBytes(encryptedBytes, Base64.ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt(String str) {
        try {
            byte[] decryptedBytes = Base64.decode(str, Base64.DECODE);
            return new String(decryptedBytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the category of a given line
     * @param line the given line
     * @return the category
     */
    public String getCategory (String line) {
        if (findCommand(line) == 1) {
            return removeCommand(line);
        }
        else {
            return line.substring(2, line.indexOf("]"));
        }
    }

    /**
     * Gets the item name of a given line
     * @param line the given line
     * @return the item name
     */
    public String getItemName (String line) {
        if (findCommand(line) == 5)
            return removeCommand(line);
        else if (findCommand(line) == 1) {
            return null;
        }
        else {
            line = line.substring(line.indexOf("]") + 2);
            return line.substring(0, line.indexOf("]"));
        }
    }
}
