package ca.matthewsun.passwordsaver;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchObject implements Serializable {
    private ArrayList<String> itemNames;
    private ArrayList<String> categories;
    private int d;


    public SearchObject (ArrayList<String> items, ArrayList<String> categ) {
        itemNames = items;
        categories = categ;
    }

    public ArrayList<String> getCategories () {
        return categories;
    }

    public ArrayList<String> getItemNames () {
        return itemNames;
    }

    public boolean isEmpty () {
        if (itemNames.isEmpty() && categories.isEmpty())
            return true;
        return false;
    }
}
