package ca.matthewsun.passwordsaver;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private LinearLayout parentLayout;
    private ArrayList<String> categories;
    private File mainDirectory, categoryFile, codeFile;
    private FileEditor fileEditor;
    private Base64Encryptor encryptor;
    private int passwordLength, numChecked;
    private boolean upperStatus, lowerStatus, numberStatus, symbolStatus;
    private String secCode;
//    private SeekBar seekBar;
//    private TextView seekBarProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Home");

        parentLayout = (LinearLayout) findViewById(R.id.parent_layout);
        categories = new ArrayList<>();
        encryptor = new Base64Encryptor();
        fileEditor = new FileEditor();



        mainDirectory = new File (Environment.getExternalStorageDirectory(), "Password Storage");
        if (!mainDirectory.exists())
            mainDirectory.mkdirs();

        categoryFile = new File (mainDirectory, "ctgf.txt");
        // File doesn't exist so display a dialog to set a security code and create a new file
        if (!categoryFile.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Create Security Code");

            final EditText editText = new EditText(MainActivity.this);
            editText.setHint("Security Code");
            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);

            editText.setGravity(Gravity.CENTER_HORIZONTAL);
            // Left, Top, Right, Bottom
            builder.setView(editText, 75, 50, 75, 50);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String code = editText.getText().toString();

                    AlertDialog.Builder confirm = new AlertDialog.Builder(MainActivity.this);
                    confirm.setTitle("Confirm Security Code");

                    final EditText confirmEdit = new EditText(MainActivity.this);
                    confirmEdit.setHint("Re-enter Security Code");
                    confirmEdit.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    confirmEdit.setGravity(Gravity.CENTER_HORIZONTAL);
                    // Left, Top, Right, Bottom
                    confirm.setView(confirmEdit, 75, 50, 75, 50);
                    confirm.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (confirmEdit.getText().toString().equals(code)) {
                                try {
                                    categoryFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // Add the preset default categories
                                FileWriter writer = null;
                                try {
                                    writer = new FileWriter(categoryFile, true);
                                    BufferedWriter bw = new BufferedWriter(writer);
                                    PrintWriter pw = new PrintWriter(bw);

                                    pw.println(encryptor.encrypt(code));
                                    pw.println(encryptor.encrypt("%[%c]All"));
                                    pw.println(encryptor.encrypt("%[%c]Email"));
                                    pw.println(encryptor.encrypt("%[%c]Websites"));
                                    pw.println(encryptor.encrypt("%[%c]Other"));
                                    pw.close();


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    BufferedReader br = new BufferedReader(new FileReader(categoryFile));
                                    String currentLine = br.readLine();

                                    while ((currentLine = br.readLine()) != null) {
                                        currentLine = encryptor.decrypt(currentLine);
                                        // Is a category
                                        if (encryptor.findCommand(currentLine) == 1) {
                                            categories.add(encryptor.removeCommand(currentLine));
                                        }
                                    }
                                    br.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                refreshCategories();
                            }
                            else {
                                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                                b.setMessage("Input does not match");
                                AlertDialog b1 = b.create();
                                b1.show();
                            }
                        }
                    });
                    confirm.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            finish();
                        }
                    });
                    confirm.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.cancel();
                            finish();
                        }
                    });
                    AlertDialog alertDialog = confirm.create();
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    alertDialog.show();
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.cancel();
                    finish();
                }
            });

            AlertDialog a = builder.create();
            a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            a.show();
        }
        // File exists
        else {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(categoryFile));
                secCode = encryptor.decrypt(bufferedReader.readLine());
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Enter Security Code");

            final EditText editText = new EditText(MainActivity.this);
            editText.setHint("Security Code");
            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setGravity(Gravity.CENTER_HORIZONTAL);
            // Left, Top, Right, Bottom
            builder.setView(editText, 75, 50, 75, 50);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (editText.getText().toString().equals(secCode)) {
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(categoryFile));
                            String currentLine = br.readLine();

                            while ((currentLine = br.readLine()) != null) {
                                currentLine = encryptor.decrypt(currentLine);
                                // Is a category
                                if (encryptor.findCommand(currentLine) == 1) {
                                    categories.add(encryptor.removeCommand(currentLine));
                                }
                            }
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        refreshCategories();
                    }
                    else {
                        dialog.cancel();
                        finish();
                    }
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.cancel();
                    finish();
                }
            });

            AlertDialog a = builder.create();
            a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            a.show();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Add a category
        if (id == R.id.action_add_category) {
            addCategory();
            return true;
        }
        // Remove a category
        else if (id == R.id.action_remove_category) {
            removeCategory();
            return true;
        }
        // Generate random password
        else if (id == R.id.action_generate_password) {
            generatePassword();
            return true;
        }
        // Sort Categories
        else if (id == R.id.action_sort_categories) {
            sortCategories();
            return true;
        }
        // Search accounts
        else if (id == R.id.action_search_accounts) {
            search();
            return true;
        }
        // Change security code
        else if (id == R.id.action_security_code) {
            changeCode();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void changeCode () {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Enter Current Code");

        final EditText editText = new EditText(MainActivity.this);
        editText.setHint("Security Code");
        editText.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);

        editText.setGravity(Gravity.CENTER_HORIZONTAL);
        // Left, Top, Right, Bottom
        builder.setView(editText, 75, 50, 75, 50);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editText.getText().toString().equals(secCode)) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setTitle("Enter New Code");

                    final EditText newPass = new EditText(MainActivity.this);
                    newPass.setHint("New Security Code");
                    newPass.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    newPass.setGravity(Gravity.CENTER_HORIZONTAL);
                    // Left, Top, Right, Bottom
                    builder1.setView(newPass, 75, 50, 75, 50);

                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            secCode = newPass.getText().toString();
                            fileEditor.changeSecurityCode(secCode);
                        }
                    });
                    builder1.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder1.create();
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    alertDialog.show();
                }
                else {
                    AlertDialog.Builder error = new AlertDialog.Builder(MainActivity.this);
                    error.setTitle("Error");
                    error.setMessage("The code entered is incorrect");
                    AlertDialog alertDialog = error.create();
                    alertDialog.show();
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog a = builder.create();
        a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        a.show();

    }

    private void search () {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Search Accounts");

        final EditText input = new EditText(MainActivity.this);
        input.setHint("Search...");
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        // Left, Top, Right, Bottom
        builder.setView(input, 75, 50, 75, 50);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SearchObject results = fileEditor.allSearch(input.getText().toString());
                // No results found
                if (results.isEmpty()) {
                    dialog.cancel();
                    AlertDialog.Builder empty = new AlertDialog.Builder(MainActivity.this);
                    empty.setMessage("No results found");
                    AlertDialog alertDialog = empty.create();
                    alertDialog.show();
                }
                else {
                    ArrayList<String> items = results.getItemNames();

                    Intent intent = new Intent(MainActivity.this, SearchResults.class);
                    intent.putExtra("search", results);
                    intent.putExtra("search term", input.getText().toString());
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog a = builder.create();
        a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        a.show();
    }

    private void sortCategories () {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Sort");
        String[] sortMethods = {"A to Z", "Z to A", "Most Accounts", "Least Accounts"};
        builder.setItems(sortMethods, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Sort A to Z
                if (which == 0) {
                    for (int n = 1 ; n < categories.size() ; n ++) {
                        int currentIndex = n;

                        while(currentIndex > 0 && (categories.get(currentIndex).compareTo(categories.get(currentIndex - 1)) < 0)) {
                            String placeholder = categories.get(currentIndex);
                            categories.set(currentIndex, categories.get(currentIndex - 1));
                            categories.set(currentIndex - 1, placeholder);

                            currentIndex --;
                        }
                    }
                }
                // Sort Z to A
                else if (which == 1) {
                    for (int n = 1 ; n < categories.size() ; n ++) {
                        int currentIndex = n;

                        while(currentIndex > 0 && (categories.get(currentIndex).compareTo(categories.get(currentIndex - 1)) > 0)) {
                            String placeholder = categories.get(currentIndex);
                            categories.set(currentIndex, categories.get(currentIndex - 1));
                            categories.set(currentIndex - 1, placeholder);

                            currentIndex --;
                        }
                    }
                }
                // Sort Most Accounts
                else if (which == 2) {
                    for (int n = 1 ; n < categories.size() ; n ++) {
                        int currentIndex = n;

                        while (currentIndex > 0 && fileEditor.findNumAccounts(categories.get(currentIndex)) >
                                fileEditor.findNumAccounts(categories.get(currentIndex - 1))) {
                            String placeholder = categories.get(currentIndex);
                            categories.set(currentIndex, categories.get(currentIndex - 1));
                            categories.set(currentIndex - 1, placeholder);

                            currentIndex --;
                        }
                    }
                }
                // Sort Least Accounts
                else if (which == 3) {
                    for (int n = 1 ; n < categories.size() ; n ++) {
                        int currentIndex = n;

                        while (currentIndex > 0 && fileEditor.findNumAccounts(categories.get(currentIndex)) <
                                fileEditor.findNumAccounts(categories.get(currentIndex - 1))) {
                            String placeholder = categories.get(currentIndex);
                            categories.set(currentIndex, categories.get(currentIndex - 1));
                            categories.set(currentIndex - 1, placeholder);

                            currentIndex --;
                        }
                    }
                }
                refreshCategories();
            }
        });

        AlertDialog a = builder.create();
        a.show();
    }

    private void generatePassword () {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Generate Password");
        LayoutInflater layoutInflater = MainActivity.this.getLayoutInflater();
        View layout  = layoutInflater.inflate(R.layout.dialog_password, (ViewGroup) findViewById(R.id.dialog_layout));

        builder.setView(layout);
        passwordLength = 0;
        AlertDialog a = builder.create();
        a.show();

        SeekBar seekBar = (SeekBar)layout.findViewById(R.id.password_seekbar);
        final TextView seekBarProgress = (TextView) layout.findViewById(R.id.password_seekbar_progress);
        CheckBox uppercase = (CheckBox) layout.findViewById(R.id.checkbox_uppercase);
        CheckBox lowercase = (CheckBox) layout.findViewById(R.id.checkbox_lowercase);
        CheckBox symbols = (CheckBox) layout.findViewById(R.id.checkbox_symbols);
        CheckBox numbers = (CheckBox) layout.findViewById(R.id.checkbox_numbers);

        uppercase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    upperStatus = true;
                    numChecked ++;
                }
                else {
                    upperStatus = false;
                    numChecked --;
                }
            }
        });
        lowercase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    lowerStatus = true;
                    numChecked ++;
                }
                else {
                    lowerStatus = false;
                    numChecked --;
                }
            }
        });
        numbers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    numberStatus = true;
                    numChecked ++;
                }
                else {
                    numberStatus = false;
                    numChecked --;
                }
            }
        });
        symbols.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    symbolStatus = true;
                    numChecked ++;
                }
                else {
                    symbolStatus = false;
                    numChecked --;
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                passwordLength = progress;
                seekBarProgress.setText("Length: " + passwordLength);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


    }

    /**
     * Handles the adding of a new category
     */
    private void addCategory () {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Category");

        final EditText input = new EditText(MainActivity.this);
        input.setHint("Enter text...");
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        // Left, Top, Right, Bottom
        builder.setView(input, 75, 50, 75, 50);


        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Invalid characters
                if (input.getText().toString().contains("]") ||
                        input.getText().toString().contains("[") || input.getText().toString().contains("%")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Invalid Characters");
                    builder.setMessage("The category name contains one or more invalid characters");

                    AlertDialog a = builder.create();
                    a.show();
                }
                else if (!categories.contains(input.getText().toString())) {
                    categories.add(input.getText().toString());

                    fileEditor.addLine(encryptor.encrypt("%[%c]" + input.getText().toString()));
                    // Refresh the views
                    refreshCategories();
                }
                else {
                    Toast.makeText(MainActivity.this, "Category already exists!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog a = builder.create();
        a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        a.show();
    }

    private void removeCategory () {
        final ArrayList<Integer> selectedItems = new ArrayList<>();
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Remove");

        String [] choices = new String [categories.size()];
        for (int n = 0 ; n < categories.size() ; n ++)
            choices[n] = categories.get(n);

        builder.setMultiChoiceItems(choices, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // When selected, add it to the array of selected items
                if (isChecked)
                    selectedItems.add(which);
                // Deselected
                else if (selectedItems.contains(which))
                    selectedItems.remove(Integer.valueOf(which));
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean removeError = false;
                // Check if there any items on the chosen category
                for (int n = 0 ; n < selectedItems.size() ; n ++) {
                    // There are items on the category
                    if (fileEditor.checkCategoryItems(categories.get(selectedItems.get(n)))) {
                       removeError = true;
                        break;
                    }
                }

                // There is a problem with the removal process
                if (removeError) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setTitle("Removing Error");
                    builder1.setMessage("One or more of the chosen categories contain accounts.");
                    builder1.setPositiveButton("DELETE ALL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayList<String> linesToRemove = new ArrayList<String>();
                            for (int n = 0; n < selectedItems.size(); n++) {
                                linesToRemove.add(encryptor.encrypt("%[%c]" + categories.get(selectedItems.get(n))));
                                fileEditor.removeCategoryItems(categories.get(selectedItems.get(n)));
                                categories.remove(categories.get(selectedItems.get(n)));
                            }
                            fileEditor.removeMultipleLines(linesToRemove);
                            refreshCategories();
                        }
                    });
                    builder1.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder1.create();
                    alertDialog.show();
                }
                else {
                    ArrayList<String> linesToRemove = new ArrayList<String>();
                    for (int n = 0; n < selectedItems.size(); n++) {
                        linesToRemove.add(encryptor.encrypt("%[%c]" + categories.get(selectedItems.get(n))));
                        categories.remove(categories.get(selectedItems.get(n)));
                    }
                    fileEditor.removeMultipleLines(linesToRemove);
                    refreshCategories();
                }

            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog a = builder.create();
        a.show();
    }

    /**
     * Refreshes all category buttons based on the current list
     */
    private void refreshCategories () {
        parentLayout.removeAllViews();

        for (int n = 0 ; n < categories.size() ; n ++) {
            Button b = new Button (MainActivity.this);
            final String category = categories.get(n);
            b.setText(category);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, CategoryItemActivity.class);
                    intent.putExtra("keyname", category);
                    startActivity(intent);
                }
            });
            parentLayout.addView(b);
        }
    }

    public void generatePassword (View view) {
        // Invalid input
        if (numChecked > passwordLength || passwordLength == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Error");
            builder.setMessage("Invalid password length.");
            AlertDialog a = builder.create();
            a.show();
        }
        else if (numChecked == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Error");
            builder.setMessage("No parameters selected.");
            AlertDialog a = builder.create();
            a.show();
        }
        else {
            PasswordGenerator p = new PasswordGenerator();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("" + p.generatePassword(lowerStatus, upperStatus,
                    numberStatus, symbolStatus, passwordLength));
            AlertDialog a = builder.create();
            a.show();
        }
    }
}
