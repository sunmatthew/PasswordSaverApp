package ca.matthewsun.passwordsaver;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class SearchResults extends AppCompatActivity {
    private ArrayList<String> items;
    private ArrayList<String> categories;
    private SearchObject searchResults;
    private LinearLayout parentLayout;
    private String searchTerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchResults = (SearchObject)getIntent().getSerializableExtra("search");
        searchTerm = getIntent().getStringExtra("search term");

        setTitle("Search Results: " + searchTerm);

        items = searchResults.getItemNames();
        categories = searchResults.getCategories();

        parentLayout = (LinearLayout) findViewById(R.id.search_layout);
        for (int n = 0 ; n < items.size() ; n ++) {
            final String itemName = items.get(n);
            final String categoryName = categories.get(n);
            Button b = new Button(SearchResults.this);
            b.setText(itemName + " (" + categoryName + ")");
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick (View view) {
                    Intent intent = new Intent(SearchResults.this, OldFormActivity.class);
                    intent.putExtra("item name", itemName);
                    intent.putExtra("category", categoryName);
                    startActivityForResult(intent, 1);
                }
            });
            parentLayout.addView(b);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            refresh();
        }
    }

    public void refresh () {
        parentLayout.removeAllViews();
        FileEditor fileEditor = new FileEditor();

        SearchObject searchObject = fileEditor.allSearch(searchTerm);
        items = searchObject.getItemNames();
        categories = searchObject.getCategories();

        for (int n = 0 ; n < items.size() ; n ++) {
            final String itemName = items.get(n);
            final String categoryName = categories.get(n);
            Button b = new Button(SearchResults.this);
            b.setText(itemName + " (" + categoryName + ")");
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick (View view) {
                    Intent intent = new Intent(SearchResults.this, OldFormActivity.class);
                    intent.putExtra("item name", itemName);
                    intent.putExtra("category", categoryName);
                    startActivityForResult(intent, 1);
                }
            });
            parentLayout.addView(b);
        }
    }

}
