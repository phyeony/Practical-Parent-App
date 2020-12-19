package ca.cmpt276.charcoal.practicalparent.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *  Allows for saving and reading data for Records activity from SharedPreferences
 */
public class RecordsConfig {
    private static final String CHILD_KEY = "list_key1";
    private static final String DATE_KEY = "list_key2";
    private static final String IMG_KEY = "list_key3";
    private static final String CHOICE_KEY = "list_key4";

    public static void writeChildInPref(Context context, List<Child> list) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(CHILD_KEY, jsonString);
        editor.apply();
    }

    public static List<Child> readChildFromPref(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = pref.getString(CHILD_KEY, "");
        if (jsonString != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Child>>() {}.getType();
            return gson.fromJson(jsonString, type);
        } else {
            return null;
        }
    }

    public static void writeDateInPref(Context context, List<String> list) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(DATE_KEY, jsonString);
        editor.apply();
    }

    public static List<String> readDateFromPref(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = pref.getString(DATE_KEY, "");

        if (jsonString != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            return gson.fromJson(jsonString, type);
        } else {
            return null;
        }
    }

    public static void writeImageInPref(Context context, List<Integer> list) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(IMG_KEY, jsonString);
        editor.apply();
    }

    public static List<Integer> readImageFromPref(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = pref.getString(IMG_KEY, "");

        if (jsonString != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
            return gson.fromJson(jsonString, type);
        } else {
            return null;
        }
    }

    public static void writeChoiceInPref(Context context, List<String> list) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(CHOICE_KEY, jsonString);
        editor.apply();
    }

    public static List<String> readChoiceFromPref(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = pref.getString(CHOICE_KEY, "");
        if (jsonString != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            return gson.fromJson(jsonString, type);
        } else {
            return null;
        }
    }
}
