package ca.matthewsun.passwordsaver;

import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Enumeration;

public class FileEditor {
    File directory, file;
    Base64Encryptor encryptor;

    public FileEditor () {
        directory = new File (Environment.getExternalStorageDirectory(), "Password Storage");
        file = new File (directory, "ctgf.txt");
        encryptor = new Base64Encryptor();
    }

    public void fileCheck () {
        if (!directory.exists())
            directory.mkdirs();

        // File doesn't exist so create a new file
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Add the preset default categories
            FileWriter writer = null;
            try {
                writer = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(writer);
                PrintWriter pw = new PrintWriter(bw);

                pw.println(encryptor.encrypt("%[%c]All"));
                pw.println(encryptor.encrypt("%[%c]Email"));
                pw.println(encryptor.encrypt("%[%c]Websites"));
                pw.println(encryptor.encrypt("%[%c]Other"));
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds a line to file (ASSUMES THAT LINE IS ALREADY ENCRYPTED)
     * @param line the line to add
     */
    public void addLine (String line) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);

            pw.println(line);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a number of items from a given category
     * @param category the given category
     * @param itemNames the item names to remove
     */
    public void removeItems (String category, ArrayList<String> itemNames) {
        ArrayList<String> linesToWrite = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();
            linesToWrite.add(currentLine);

            while ((currentLine = br.readLine()) != null) {
                currentLine = encryptor.decrypt(currentLine);
                if (!(encryptor.checkCategory(currentLine, category)
                        && itemNames.contains(encryptor.getItemName(currentLine)))) {
                    linesToWrite.add(encryptor.encrypt(currentLine));
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clear the file and write all lines except the one to remove
        try {
            PrintWriter fileClear = new PrintWriter(file);
            fileClear.print("");
            fileClear.close();

            // Write new contents
            FileWriter writer = null;
            writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);

            for (int n = 0 ; n < linesToWrite.size() ; n ++) {
                pw.println(linesToWrite.get(n));
            }
            pw.close();

        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Removes a given line (ASSUMES THAT LINE IS ALREADY ENCRYPTED)
     * @param line the given line to remove
     */
    public void removeLine (String line) {
        // Save all the lines to keep
        ArrayList<String> lines = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();
            lines.add(currentLine);

            while ((currentLine = br.readLine()) != null) {
                if (!currentLine.equals(line))
                    lines.add(currentLine);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clear the file and write all lines except the one to remove
        try {
            PrintWriter fileClear = new PrintWriter(file);
            fileClear.print("");
            fileClear.close();

            // Write new contents
            FileWriter writer = null;
            writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);

            for (int n = 0 ; n < lines.size() ; n ++) {
                pw.println(lines.get(n));
            }
            pw.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if there are any items attached to a given category
     * @param category the given category to check
     * @return whether there are any items or not
     */
    public boolean checkCategoryItems (String category) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();

            while ((currentLine = br.readLine()) != null) {
                currentLine = encryptor.decrypt(currentLine);
                if (encryptor.findCommand(currentLine) > 1 && encryptor.checkCategory(currentLine, category))
                    return true;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Changes all file contents that have the old item name
     * @param oldName the old name to change
     * @param newName the new name to change to
     */
    public void changeItemName (String oldName, String newName) {
        ArrayList<String> linesToWrite = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();
            linesToWrite.add(currentLine);

            while ((currentLine = br.readLine()) != null) {
                currentLine = encryptor.decrypt(currentLine);
                // Need to change to item name
                if (encryptor.checkItemName(currentLine, oldName)) {
                    currentLine = encryptor.changeItemName(currentLine, newName);
                }
                linesToWrite.add(encryptor.encrypt(currentLine));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clear the file and write all lines except the ones to remove
        try {
            PrintWriter fileClear = new PrintWriter(file);
            fileClear.print("");
            fileClear.close();

            // Write new contents
            FileWriter writer = null;
            writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);

            for (int n = 0 ; n < linesToWrite.size() ; n ++) {
                pw.println(linesToWrite.get(n));
            }
            pw.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds the number of accounts in a given category
     * @param category the given category
     */
    public int findNumAccounts (String category) {
        int accounts = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();

            while ((currentLine = br.readLine()) != null) {
                currentLine = encryptor.decrypt(currentLine);
                if (encryptor.checkCategory(currentLine, category) && encryptor.findCommand(currentLine) == 5)
                    accounts ++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    /**
     * Does an all search given a keyword
     * @param keyword the given keyword to look for
     * @return a list of account names
     */
    public SearchObject allSearch (String keyword) {
        ArrayList<String> itemNames = new ArrayList<>();
        ArrayList<String> categories = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();

            while ((currentLine = br.readLine()) != null) {
                currentLine = encryptor.decrypt(currentLine);

                if ((currentLine.toLowerCase()).contains(keyword.toLowerCase())
                        && encryptor.findCommand(currentLine) == 5) {
                    itemNames.add(encryptor.getItemName(currentLine));
                    categories.add(encryptor.getCategory(currentLine));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SearchObject(itemNames, categories);
    }


    public ArrayList<String> getCategories () {
        ArrayList<String> categories = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();

            while ((currentLine = br.readLine()) != null) {
                currentLine = encryptor.decrypt(currentLine);

                if (encryptor.findCommand(currentLine) == 1) {
                    categories.add(encryptor.removeCommand(currentLine));
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public SearchObject categorySearch (String keyword, String category) {
        ArrayList<String> itemNames = new ArrayList<>();
        ArrayList<String> categories = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();

            while ((currentLine = br.readLine()) != null) {
                currentLine = encryptor.decrypt(currentLine);

                if ((currentLine.toLowerCase()).contains(keyword.toLowerCase())
                        && encryptor.findCommand(currentLine) == 5 && encryptor.checkCategory(currentLine, category)) {
                    itemNames.add(encryptor.getItemName(currentLine));
                    categories.add(encryptor.getCategory(currentLine));
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SearchObject(itemNames, categories);
    }

    /**
     * Checks if a given item already exists in a given category
      * @param itemName the item name to check
     * @param category the category to check in
     * @return whether it exists or not
     */
    public boolean itemExists (String itemName, String category) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();

            while ((currentLine = br.readLine()) != null) {
                currentLine = encryptor.decrypt(currentLine);
                if (encryptor.findCommand(currentLine) > 1 && encryptor.checkCategory(currentLine, category)
                        && encryptor.getItemName(currentLine).equals(itemName)) {
                    return true;
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void changeCategory (ArrayList<String> itemsToChange, String newCategory, String oldCategory) {
        ArrayList<String> linesToWrite = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();
            linesToWrite.add(currentLine);

            while ((currentLine = br.readLine()) != null) {
                currentLine = encryptor.decrypt(currentLine);
                // Need to change to category of this line
                if (encryptor.checkCategory(currentLine, oldCategory)
                        && itemsToChange.contains(encryptor.getItemName(currentLine))) {
                    linesToWrite.add(encryptor.encrypt(encryptor.changeCategory(currentLine, newCategory)));
                }
                else {
                    linesToWrite.add(encryptor.encrypt(currentLine));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clear the file and write all lines except the ones to remove
        try {
            PrintWriter fileClear = new PrintWriter(file);
            fileClear.print("");
            fileClear.close();

            // Write new contents
            FileWriter writer = null;
            writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);

            for (int n = 0 ; n < linesToWrite.size() ; n ++) {
                pw.println(linesToWrite.get(n));
            }
            pw.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes all items in a category
     * @param category the given category
     */
    public void removeCategoryItems (String category) {
        ArrayList<String> linesToKeep = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();
            linesToKeep.add(currentLine);

            while ((currentLine = br.readLine()) != null) {
                // The category does not match
                if (!encryptor.checkCategory(encryptor.decrypt(currentLine), category)) {
                    linesToKeep.add(currentLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clear the file and write all lines except the ones to remove
        try {
            PrintWriter fileClear = new PrintWriter(file);
            fileClear.print("");
            fileClear.close();

            // Write new contents
            FileWriter writer = null;
            writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);

            for (int n = 0 ; n < linesToKeep.size() ; n ++) {
                pw.println(linesToKeep.get(n));
            }
            pw.close();

        } catch(IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Removes a list of lines from the file (ASSUMES THAT ALL LINES ARE ALREADY ENCRYPTED)
     * @param lines the lines to remove
     */
    public void removeMultipleLines (ArrayList<String> lines) {
        // Save all the lines to keep
        ArrayList<String> linesToKeep = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();
            linesToKeep.add(currentLine);
            boolean remove;

            while ((currentLine = br.readLine()) != null) {
                remove = false;

                // Check if the current line should be removed
                for (int n = 0 ; n < lines.size() ; n ++) {
                    // Should be removed
                    if (lines.get(n).equals(currentLine)) {
                        remove = true;
                        break;
                    }
                }

                if (!remove) {
                    linesToKeep.add(currentLine);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clear the file and write all lines except the one to remove
        try {
            PrintWriter fileClear = new PrintWriter(file);
            fileClear.print("");
            fileClear.close();

            // Write new contents
            FileWriter writer = null;
            writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);

            for (int n = 0 ; n < linesToKeep.size() ; n ++) {
                pw.println(linesToKeep.get(n));
            }
            pw.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replaces a given line with another (ASSUMES THAT ALL CONTENTS IN LIST IS ALREADY ENCRYPTED)
     * @param linesToReplace the lines to replace
     * @param newLines the new lines
     */
    public void replaceMultipleLines (ArrayList<String> linesToReplace, ArrayList<String> newLines) {
        ArrayList<String> fileContents = new ArrayList<>();
        // Set up the readers
        try {
            BufferedReader br = new BufferedReader (new FileReader(file));
            String currentLine = br.readLine();
            fileContents.add(currentLine);
            int index = 0;
            boolean edit;

            // Go through the file and get the contents of the new file
            while ((currentLine = br.readLine()) != null) {
                edit = false;
                // Go through all the lines to replace
                for (int n = 0 ; n < linesToReplace.size() ; n ++) {
                    if (currentLine.equals(linesToReplace.get(n))) {
                        index = n;
                        edit = true;
                        break;
                    }
                }

                // Need to replace a line
                if (edit) {
                    fileContents.add(newLines.get(index));
                    newLines.remove(index);
                    linesToReplace.remove(index);
                }
                else {
                    fileContents.add(currentLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set up the writers
        try {
            // Clear the file
            PrintWriter eraseContents = new PrintWriter(file);
            eraseContents.print("");
            eraseContents.close();

            // Write the new contents to the file
            FileWriter writer = null;
            writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);

            for (int n = 0 ; n < fileContents.size() ; n ++) {
                pw.println(fileContents.get(n));
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeSecurityCode (String newCode) {
        ArrayList<String> linesToWrite = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();
            linesToWrite.add(encryptor.encrypt(newCode));

            while ((currentLine = br.readLine()) != null) {
                linesToWrite.add(currentLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set up the writers
        try {
            // Clear the file
            PrintWriter eraseContents = new PrintWriter(file);
            eraseContents.print("");
            eraseContents.close();

            // Write the new contents to the file
            FileWriter writer = null;
            writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            PrintWriter pw = new PrintWriter(bw);

            for (int n = 0 ; n < linesToWrite.size() ; n ++) {
                pw.println(linesToWrite.get(n));
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
