package ca.matthewsun.passwordsaver;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CategoryItemActivity extends AppCompatActivity {
    private String category;
    private ArrayList<String> items, allCategories;
    private File directory, file;
    private LinearLayout parentLayout;
    private Base64Encryptor encryptor;
    private FileEditor fileEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        parentLayout = (LinearLayout)findViewById(R.id.content_category_layout);
        encryptor = new Base64Encryptor();
        fileEditor = new FileEditor();
        allCategories = new ArrayList<>();
        items = new ArrayList<>();
        directory = new File (Environment.getExternalStorageDirectory(), "Password Storage");
        file = new File (directory, "ctgf.txt");

        // Get the given category
        category = getIntent().getExtras().getString("keyname");
        setTitle(category);

        // Add new item
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CategoryItemActivity.this, NewFormActivity.class);
                intent.putExtra("new form", category);
                startActivityForResult(intent, 1);

            }
        });

        refreshItems();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!category.equals("All"))
            getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Add a category
        if (id == R.id.action_add_account) {
            Intent intent = new Intent(CategoryItemActivity.this, NewFormActivity.class);
            intent.putExtra("new form", category);
            startActivityForResult(intent, 1);
            return true;
        }
        else if (id == R.id.action_remove_account) {
            removeAccounts();
            return true;
        }
        else if (id == R.id.action_move_account) {
            moveAccounts();
            return true;
        }
        else if (id == R.id.action_sort_account) {
            sortAccounts();
            return true;
        }
        else if (id == R.id.action_search_category) {
            searchAccounts();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 || requestCode == 2) {
            refreshItems();
        }
    }

    private void moveAccounts () {
        final String[] accounts = new String[items.size()];
        for (int n = 0 ; n < items.size() ; n ++) {
            accounts[n] = items.get(n);
        }

        final ArrayList<String> selectedAccounts = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryItemActivity.this);
        builder.setTitle("Choose Accounts");
        builder.setMultiChoiceItems(accounts, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked)
                    selectedAccounts.add(accounts[which]);
                else
                    selectedAccounts.remove(accounts[which]);
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                AlertDialog.Builder categoryBuilder = new AlertDialog.Builder(CategoryItemActivity.this);
                categoryBuilder.setTitle("Move to...");

                ArrayList<String> categories = fileEditor.getCategories();
                final String[] categoryArray = new String[categories.size()];
                for (int n = 0 ; n < categories.size() ; n ++)
                    categoryArray[n] = categories.get(n);
                categoryBuilder.setItems(categoryArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newCategory = categoryArray[which];
                        fileEditor.changeCategory(selectedAccounts, newCategory, category);
                        refreshItems();
                    }
                });

                AlertDialog alertDialog = categoryBuilder.create();
                alertDialog.show();
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

    private void searchAccounts () {
        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryItemActivity.this);
        builder.setTitle("Search in Category");

        final EditText editText = new EditText(CategoryItemActivity.this);
        editText.setHint("Search...");
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editText.setGravity(Gravity.CENTER_HORIZONTAL);
        // Left, Top, Right, Bottom
        builder.setView(editText, 75, 50, 75, 50);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SearchObject results = fileEditor.categorySearch(editText.getText().toString(), category);
                // No results found
                if (results.isEmpty()) {
                    dialog.cancel();
                    AlertDialog.Builder empty = new AlertDialog.Builder(CategoryItemActivity.this);
                    empty.setMessage("No results found");
                    AlertDialog alertDialog = empty.create();
                    alertDialog.show();
                }
                else {
                    ArrayList<String> items = results.getItemNames();

                    Intent intent = new Intent(CategoryItemActivity.this, SearchResults.class);
                    intent.putExtra("search", results);
                    intent.putExtra("search term", editText.getText().toString() + " (" + category + ")");
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

    private void sortAccounts () {
        String[] sortOptions = {"A to Z", "Z to A"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryItemActivity.this);
        builder.setTitle("Sort");
        builder.setItems(sortOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Sort A to Z
                if (which == 0) {
                    for (int n = 1 ; n < items.size() ; n ++) {
                        int currentIndex = n;

                        while(currentIndex > 0 && (items.get(currentIndex).compareTo(items.get(currentIndex - 1)) < 0)) {
                            String placeholder = items.get(currentIndex);
                            items.set(currentIndex, items.get(currentIndex - 1));
                            items.set(currentIndex - 1, placeholder);

                            currentIndex --;
                        }
                    }
                }
                // Sort Z to A
                else if (which == 1) {
                    for (int n = 1 ; n < items.size() ; n ++) {
                        int currentIndex = n;

                        while(currentIndex > 0 && (items.get(currentIndex).compareTo(items.get(currentIndex - 1)) > 0)) {
                            String placeholder = items.get(currentIndex);
                            items.set(currentIndex, items.get(currentIndex - 1));
                            items.set(currentIndex - 1, placeholder);

                            currentIndex --;
                        }
                    }
                }
                refreshExistingItems();
            }
        });

        AlertDialog a = builder.create();
        a.show();
    }

    private void removeAccounts () {
        final ArrayList<String> itemsToRemove = new ArrayList<>();
        final String[] itemNames = new String[items.size()];
        final ArrayList<String> categoryNames = new ArrayList<>();
        for (int n = 0 ; n < items.size() ; n ++) {
            itemNames[n] = items.get(n);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryItemActivity.this);
        builder.setTitle("Remove");
        builder.setMultiChoiceItems(itemNames, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // When selected, add it to the array of selected items
                if (isChecked) {
                    itemsToRemove.add(itemNames[which]);
                }
                // Deselected
                else if (itemsToRemove.contains(itemNames[which])) {
                    itemsToRemove.remove(itemNames[which]);
                }
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileEditor.removeItems(category, itemsToRemove);
                refreshItems();
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

    private void refreshExistingItems () {
        parentLayout.removeAllViews();

        if (!category.equals("All")) {
            // Go through all items
            for (int n = 0; n < items.size(); n++) {
                Button b = new Button(CategoryItemActivity.this);
                final String itemName = items.get(n);
                b.setText(itemName);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(CategoryItemActivity.this, OldFormActivity.class);
                        intent.putExtra("item name", itemName);
                        intent.putExtra("category", category);
                        startActivityForResult(intent, 2);
                    }
                });
                parentLayout.addView(b);
            }
        }
        else {
            // Go through all items
            for (int n = 0; n < items.size(); n++) {
                Button b = new Button(CategoryItemActivity.this);
                final String itemName = items.get(n);
                final String itemCategory = allCategories.get(n);
                b.setText(itemName + " (" + itemCategory + ")");
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(CategoryItemActivity.this, OldFormActivity.class);
                        intent.putExtra("item name", itemName);
                        intent.putExtra("category", itemCategory);
                        startActivityForResult(intent, 2);
                    }
                });
                parentLayout.addView(b);
            }
        }
    }

    /**
     * Refreshes all items on the screen based on the current list
     */
    private void refreshItems () {
        parentLayout.removeAllViews();
        items.clear();

        if (!category.equals("All")) {
            // Read items in the file
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String currentLine = br.readLine();

                while ((currentLine = br.readLine()) != null) {
                    // Decrypt the line
                    currentLine = encryptor.decrypt(currentLine);
                    // The current line is an item
                    if (encryptor.findCommand(currentLine) == 5 && encryptor.checkCategory(currentLine, category)) {
                        items.add(encryptor.removeCommand(currentLine));
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Go through all items
            for (int n = 0; n < items.size(); n++) {
                Button b = new Button(CategoryItemActivity.this);
                final String itemName = items.get(n);
                b.setText(itemName);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(CategoryItemActivity.this, OldFormActivity.class);
                        intent.putExtra("item name", itemName);
                        intent.putExtra("category", category);
                        startActivityForResult(intent, 2);
                    }
                });
                parentLayout.addView(b);
            }
        }
        else {
            // Read items in the file
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String currentLine = br.readLine();

                while ((currentLine = br.readLine()) != null) {
                    // Decrypt the line
                    currentLine = encryptor.decrypt(currentLine);
                    // The current line is an item
                    if (encryptor.findCommand(currentLine) == 5) {
                        items.add(encryptor.removeCommand(currentLine));
                        allCategories.add(encryptor.getCategory(currentLine));
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Go through all items
            for (int n = 0; n < items.size(); n++) {
                Button b = new Button(CategoryItemActivity.this);
                final String itemName = items.get(n);
                final String itemCategory = allCategories.get(n);
                b.setText(itemName + " (" + itemCategory + ")");
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(CategoryItemActivity.this, OldFormActivity.class);
                        intent.putExtra("item name", itemName);
                        intent.putExtra("category", itemCategory);
                        startActivityForResult(intent, 2);
                    }
                });
                parentLayout.addView(b);
            }
        }
    }
}
