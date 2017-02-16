package ru.spbau.mit.foodmanager;

import java.util.ArrayList;
import java.util.Random;

/**
 * Generates number of resource for loader animations
 */

public class LoaderAnimationSelector {
    private static ArrayList<Integer> loaderResources;
    private static Random random = new Random();

    static {
        loaderResources = new ArrayList<>();
        loaderResources.add(R.drawable.loading_animation);
        loaderResources.add(R.drawable.loading_animation2);
        loaderResources.add(R.drawable.loading_animation3);
        loaderResources.add(R.drawable.loading_animation4);
        loaderResources.add(R.drawable.loading_animation7);
        loaderResources.add(R.drawable.loading_animation6);
    }

    /**
     * return random loader animation's resource ID
     */
    public static int getRandomLoaderResource() {
        int resID = loaderResources.get(Math.abs(random.nextInt()) % loaderResources.size());
        return resID;
    }
}
