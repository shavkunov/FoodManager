package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.view.menu.MenuView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Adapter for MenuViewActivity ListView
 */

public class MenuViewAdapter extends BaseAdapter {
    private Context context;
    private CookBookStorage cookbook;
    private LayoutInflater inflater;
    private ArrayList<Pair<Day, DayMenu>> menus;
    static private String[] DAY_NAMES = {
            "Понедельник",
            "Вторник",
            "Среда",
            "Четверг",
            "Пятница",
            "Суббота",
            "Воскресенье"};

    public MenuViewAdapter(Context context, HashMap<Day, DayMenu> menusByDays) {
        this.context = context;
        cookbook = CookBookStorage.getInstance(context);
        menus = new ArrayList<>();
        for (Day d : Day.values()) {
            if (menusByDays.get(d) != null) {
                menus.add(new Pair<>(d, menusByDays.get(d)));
            }
        }
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return menus.size();
    }

    @Override
    public Object getItem(int i) {
        return menus.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.menu_view_list_element, null);
        Pair<Day, DayMenu> menu = menus.get(i);
        TextView dayName = (TextView) view.findViewById(R.id.menu_day_name);
        LinearLayout dayMealtimes = (LinearLayout) view.findViewById(R.id.menu_day_mealtimes);
        dayName.setText(DAY_NAMES[menu.first.ordinal()]);
        for (DayMenu.Mealtime mealtime : menu.second.getMealtimes()) {
            LinearLayout mealtimeElement = (LinearLayout) inflater.inflate(
                    R.layout.menu_view_day_menu_list_element, null);
            TextView mealtimeName = (TextView) mealtimeElement.findViewById(
                    R.id.menu_day_mealtime_name);
            LinearLayout recipeList = (LinearLayout) mealtimeElement.findViewById(
                    R.id.menu_day_mealtime_dishes);

            mealtimeName.setText(mealtime.getName());
            for (Integer recipeID : mealtime.getRecipes()) {
                TextView recipeName = new TextView(context);
                recipeName.setText(cookbook.getRecipe(recipeID).getName());
                //TODO add onClickListener
                recipeList.addView(recipeName);
            }
            dayMealtimes.addView(mealtimeElement);
        }
        return view;
    }
}
