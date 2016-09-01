package ca.matthewsun.passwordsaver;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.TintTypedArray;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class OldFormActivity extends AppCompatActivity {
    private String category, itemName;
    private Base64Encryptor encryptor;
    private File directory, file;
    private EditText titleText, usernameText, passwordText, noteText;
    private String title, username, password, note;
    private FileEditor fileEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the given category
        category = getIntent().getExtras().getString("category");
        itemName = getIntent().getExtras().getString("item name");
        encryptor = new Base64Encryptor();
        fileEditor = new FileEditor();
        directory = new File (Environment.getExternalStorageDirectory(), "Password Storage");
        file = new File (directory, "ctgf.txt");

        // Get the text fields
        note = "";
        titleText = (EditText) findViewById(R.id.old_form_title);
        usernameText = (EditText) findViewById(R.id.old_form_username);
        passwordText = (EditText) findViewById(R.id.old_form_password);
        noteText = (EditText) findViewById(R.id.old_form_notes);

        // Go through the file and get the form information
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentLine = br.readLine();

            while ((currentLine = br.readLine()) != null) {
                currentLine = encryptor.decrypt(currentLine);
                // We got the right category and item
                if (encryptor.checkCategoryAndItem(currentLine, category, itemName)) {
                    // Username
                    if (encryptor.findCommand(currentLine) == 2) {
                        currentLine = encryptor.removeCommand(currentLine);
                        username = currentLine;
                        usernameText.setText(currentLine);
                    }
                    // Password
                    else if (encryptor.findCommand(currentLine) == 3) {
                        currentLine = encryptor.removeCommand(currentLine);
                        password = currentLine;
                        passwordText.setText(currentLine);
                    }
                    // Note
                    else if (encryptor.findCommand(currentLine) == 4) {
                        currentLine = encryptor.removeCommand(currentLine);
                        note = currentLine;
                        noteText.setText(currentLine);
                    }
                    // Item title
                    else if (encryptor.findCommand(currentLine) == 5) {
                        currentLine = encryptor.removeCommand(currentLine);
                        title = currentLine;
                        titleText.setText(currentLine);
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setTitle(itemName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Up button actions
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Finds out if any text fields have been changed
     * @return whether a change has occured
     */
    private boolean changesMade () {
        if (!titleText.getText().toString().equals(title) || !usernameText.getText().toString().equals(username)
                || !passwordText.getText().toString().equals(password) || !noteText.getText().toString().equals(note))
            return true;
        return false;
    }

    /**
     * Finds out if a mandatory field is missing
     * @return whether a required field is blank
     */
    private boolean missingField () {
        if (titleText.getText().toString().matches("") || usernameText.getText().toString().matches("")
                || passwordText.getText().toString().matches(""))
            return true;
        return false;
    }

    @Override
    public void onBackPressed() {
        // A blank field
        if (missingField()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
            builder.setTitle("Missing Fields");
            builder.setMessage("You are missing one or more of the required fields");

            AlertDialog a = builder.create();
            a.show();
        }
        // A change has occurred
        else if (changesMade()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
            builder.setTitle("Exit Form");
            builder.setMessage("A change has been made to one or more fields. Save before exiting?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Invalid characters
                    if (title.contains("]") || title.contains("[") || title.contains("%")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
                        builder.setTitle("Invalid Characters");
                        builder.setMessage("The account title contains one or more invalid characters");

                        AlertDialog a = builder.create();
                        a.show();
                    }
                    // Account name is taken
                    else if (!title.equals(titleText.getText().toString()) && fileEditor.itemExists(title, category)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
                        builder.setTitle("Invalid Title");
                        builder.setMessage("There is already an account in this category of the same title");

                        AlertDialog a = builder.create();
                        a.show();
                    }
                    else {
                        saveChanges();
                        finish();
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });

            AlertDialog a = builder.create();
            a.show();
        }
        else {
            finish();
        }
    }

    /**
     * Finds and saves changes made to the text file
     */
    private void saveChanges () {
        boolean noteAdded = false;
        ArrayList<String> linesToReplace = new ArrayList<>();
        ArrayList<String> newLines = new ArrayList<>();
        // Title change
        if (!titleText.getText().toString().equals(title)) {
            linesToReplace.add(encryptor.encrypt("%[" + category + "]" + title));
            newLines.add(encryptor.encrypt("%[" + category + "]" + titleText.getText().toString()));
        }
        // Username change
        if (!usernameText.getText().toString().equals(username)) {
            linesToReplace.add(encryptor.encrypt("%[" + category + "][" + itemName + "][%u]" + username));
            newLines.add(encryptor.encrypt("%[" + category + "][" + itemName + "][%u]" + usernameText.getText().toString()));
        }
        // Password change
        if (!passwordText.getText().toString().equals(password)) {
            linesToReplace.add(encryptor.encrypt("%[" + category + "][" + itemName + "][%p]" + password));
            newLines.add(encryptor.encrypt("%[" + category + "][" + itemName + "][%p]" + passwordText.getText().toString()));
        }
        // Note change
        if (!noteText.getText().toString().equals(note)) {
            linesToReplace.add(encryptor.encrypt("%[" + category + "][" + itemName + "][%n]" + note));
            newLines.add(encryptor.encrypt("%[" + category + "][" + itemName + "][%n]" + noteText.getText().toString()));
            noteAdded = true;
        }
        fileEditor.replaceMultipleLines(linesToReplace, newLines);

        // If there was no note initially
        if (note.matches("") && noteAdded)
            fileEditor.addLine(encryptor.encrypt("%[" + category + "][" + itemName + "][%n]" + noteText.getText().toString()));

        // If we gotta change item names
        if (!titleText.getText().toString().equals(title)) {
            fileEditor.changeItemName(title, titleText.getText().toString());
        }
    }

    public void doneButton (View view) {
        // A blank field
        if (missingField()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
            builder.setTitle("Missing Fields");
            builder.setMessage("You are missing one or more of the required fields");

            AlertDialog a = builder.create();
            a.show();
        }
        // A change has occurred
        else if (changesMade()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
            builder.setTitle("Save Changes");
            builder.setMessage("Do you want to save changes before exiting?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Invalid characters
                    if (title.contains("]") || title.contains("[") || title.contains("%")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
                        builder.setTitle("Invalid Characters");
                        builder.setMessage("The account title contains one or more invalid characters");

                        AlertDialog a = builder.create();
                        a.show();
                    }
                    // Account name is taken
                    else if (!title.equals(titleText.getText().toString()) && fileEditor.itemExists(title, category)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
                        builder.setTitle("Invalid Title");
                        builder.setMessage("There is already an account in this category of the same title");

                        AlertDialog a = builder.create();
                        a.show();
                    }
                    else {
                        saveChanges();
                        finish();
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });

            AlertDialog a = builder.create();
            a.show();
        }
        else {
            finish();
        }
    }

    public void cancelButton (View view) {
        // A blank field
        if (missingField()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
            builder.setTitle("Missing Fields");
            builder.setMessage("You are missing one or more of the required fields");

            AlertDialog a = builder.create();
            a.show();
        }
        // A change has occurred
        else if (changesMade()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
            builder.setTitle("Exit Form");
            builder.setMessage("A change has been made to one or more fields. Save before exiting?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Invalid characters
                    if (title.contains("]") || title.contains("[") || title.contains("%")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
                        builder.setTitle("Invalid Characters");
                        builder.setMessage("The account title contains one or more invalid characters");

                        AlertDialog a = builder.create();
                        a.show();
                    }
                    // Account name is taken
                    else if (!title.equals(titleText.getText().toString()) && fileEditor.itemExists(title, category)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OldFormActivity.this);
                        builder.setTitle("Invalid Title");
                        builder.setMessage("There is already an account in this category of the same title");

                        AlertDialog a = builder.create();
                        a.show();
                    }
                    else {
                        saveChanges();
                        finish();
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });

            AlertDialog a = builder.create();
            a.show();
        }
        else {
            finish();
        }
    }
}
