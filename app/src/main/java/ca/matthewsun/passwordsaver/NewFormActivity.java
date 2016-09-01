package ca.matthewsun.passwordsaver;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class NewFormActivity extends AppCompatActivity {
    private String category;
    private Base64Encryptor encryptor;
    private File directory, file;
    private FileEditor fileEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("New Form");

        // Get the given category
        category = getIntent().getExtras().getString("new form");
        encryptor = new Base64Encryptor();
        directory = new File (Environment.getExternalStorageDirectory(), "Password Storage");
        file = new File (directory, "ctgf.txt");
        fileEditor = new FileEditor();

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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewFormActivity.this);
        builder.setMessage("Exit this form?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog a = builder.create();
        a.show();
    }

    /**
     * Displays confirmation message before leaving
     */
    public void cancelButton (View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewFormActivity.this);
        builder.setMessage("Exit this form?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog a = builder.create();
        a.show();
    }

    /**
     * Actions to handle when clicking the done button
     */
    public void doneButton (View view) {
        EditText titleText = (EditText) findViewById(R.id.form_title);
        EditText usernameText = (EditText) findViewById(R.id.form_username);
        EditText passwordText = (EditText) findViewById(R.id.form_password);
        EditText noteText = (EditText) findViewById(R.id.form_notes);

        String title = titleText.getText().toString();
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();
        String notes = noteText.getText().toString();

        // Missing field
        if (title.matches("") || username.matches("") || password.matches("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewFormActivity.this);
            builder.setTitle("Missing Fields");
            builder.setMessage("You are missing one or more of the required fields");

            AlertDialog a = builder.create();
            a.show();
        }
        // Invalid characters
        else if (title.contains("]") || title.contains("[") || title.contains("%")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewFormActivity.this);
            builder.setTitle("Invalid Characters");
            builder.setMessage("The account title contains one or more invalid characters");

            AlertDialog a = builder.create();
            a.show();
        }
        // Account name is taken
        else if (fileEditor.itemExists(title, category)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewFormActivity.this);
            builder.setTitle("Invalid Title");
            builder.setMessage("There is already an account in this category of the same title");

            AlertDialog a = builder.create();
            a.show();
        }
        // Write to file
        else {
            FileWriter writer = null;
            try {
                // Set up the writer
                writer = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(writer);
                PrintWriter pw = new PrintWriter(bw);

                pw.println(encryptor.encrypt("%[" + category + "]" + title));
                pw.println(encryptor.encrypt("%[" + category + "][" + title + "][%u]" + username));
                pw.println(encryptor.encrypt("%[" + category + "][" + title + "][%p]" + password));
                // If there is a note
                if (!notes.matches(""))
                    pw.println(encryptor.encrypt("%[" + category + "][" + title + "][%n]" + notes));
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        }
    }

}
