package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

//TODO Add, Delete and Edit tags
public class EditRecipeActivity extends AppCompatActivity {
    private final static int REQUEST_PICK_IMAGE = 0;
    private final static int REQUEST_EDIT_STEPS = 1;
    private final static int REQUEST_PICK_CATEGORY = 2;
    private int recipeID;
    private ArrayList<UriStep> uriSteps;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<Integer> tags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_recipe);
        recipeID = getIntent().getIntExtra("RecipeID", 0);
        ingredients = new ArrayList<>();
        uriSteps = new ArrayList<>();
        tags = new ArrayList<>();
        if (recipeID != 0) {
            EditText name = (EditText) findViewById(R.id.edit_recipe_header_name);
            EditText description = (EditText) findViewById(R.id.edit_recipe_body_description);
            CookBookStorage cookbook = CookBookStorage.getInstance(this);
            Recipe recipe = cookbook.getRecipe(recipeID);
            ingredients = cookbook.getRecipeIngredients(recipeID);
            for (Step s : cookbook.getRecipeSteps(recipeID)) {
                uriSteps.add(new UriStep(s));
            }
            tags = cookbook.getRecipeCategories(recipeID);
            name.setText(recipe.getName());
            description.setText(recipe.getDescription());
            showRecipeData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int errorCode, Intent resultContainer) {
        if (errorCode == RESULT_OK) {
            switch (requestCode) {
                /*case REQUEST_PICK_IMAGE:
                    Uri newImage = resultContainer.getData();
                    ImageView recipeImage = (ImageView) findViewById(R.id.edit_recipe_header_photo);
                    try {
                        recipeImage.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), newImage));
                    }
                    catch (IOException e) {
                        //Cant upload image;
                    }
                    break;*/
                case REQUEST_EDIT_STEPS:
                    uriSteps = (ArrayList<UriStep>) resultContainer.getSerializableExtra("UriSteps");
                    break;
                case REQUEST_PICK_CATEGORY:
                    final Integer category = resultContainer.getIntExtra("Result", 0);
                    tags.add(category);
                    addTagToView(category);
                    break;
            }
        }
    }

    public void onSaveClick(View v) {
        EditText name = (EditText) findViewById(R.id.edit_recipe_header_name);
        EditText description = (EditText) findViewById(R.id.edit_recipe_body_description);
        RecipeToChange result = new RecipeToChange(recipeID, description.getText().toString(), name.getText().toString());
        result.setCategoryID(tags);
        result.setIngredients(ingredients);
        //INISteps
        ArrayList<Step> steps = new ArrayList<>();
        //TODO Another Thread;
        for (UriStep uriStep : uriSteps) {
            try {
                String descr = uriStep.getDescription();
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                        uriStep.getImageUri());
                steps.add(new Step(descr, image));
            }
            catch (IOException e) {
                //Cant upload image;
            }
        }
        result.setSteps(steps);
        //TODO Another Thread
        boolean complete = false;
        while (!complete) {
            try {
                if (recipeID == 0) {
                    CookBookStorage.getInstance(this).addRecipeToDatabase(result);
                } else {
                    CookBookStorage.getInstance(this).changeRecipe(result);

                }
                complete = true;
            }
            catch (Exception e) {
                //Repeat
            }
        }
        finish();
    }

    public void onStepsClick(View v) {
        Intent task = new Intent(this, EditStepActivity.class);
        task.putExtra("UriSteps", uriSteps);
        try {
            ObjectOutputStream test = new ObjectOutputStream(new ByteArrayOutputStream());
            test.writeObject(uriSteps);
            Log.d("STEP CLICK", test.toString());
        }
        catch (Exception e) {
            Log.d("STEP BOOM", e.toString());
        }
        startActivityForResult(task, REQUEST_EDIT_STEPS);
    }

    //Finished
    public void onAddIngredientClick(View v) {
        final Ingredient newIngredient = new Ingredient("", Measure.gr, 0.0);
        ingredients.add(newIngredient);
        addIngredientToView(newIngredient);
    }

    //We have no separated pic of recipe yet
    /*public void onRecipeImageClick(View v) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE);
    }*/

    public void onAddTagClick(View view) {
        Intent task = new Intent(this, CookBookActivity.class);
        task.putExtra("Target", CookBookActivity.TARGET_CATEGORY);
        startActivityForResult(task, REQUEST_PICK_CATEGORY);
    }

    public static class UriStep implements Serializable {
        private String description;
        private String imageUri;

        public UriStep() {}
        public UriStep(Step s) {
            description = s.getDescription();
            imageUri = s.getImageLink();
        }
        public Uri getImageUri() {
            return Uri.parse(imageUri);
        }
        public void setImageUri(Uri uri) {
            imageUri = uri.toString();
        }

        public String getDescription() {
            return description;
        }
        public void setDescription(String s) {
            description = s;
        }
    }

    public void showRecipeData() {
        for (Integer i : tags) {
            addTagToView(i);
        }
        for (Ingredient i : ingredients) {
            addIngredientToView(i);
        }
    }

    private void addTagToView(final Integer category) {
        final LinearLayout tagsLayout = (LinearLayout) findViewById(R.id.edit_recipe_header_tags);
        final View newTag = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.edit_recipe_tag, null);
        TextView newTagName = (TextView) newTag.findViewById(R.id.edit_recipe_tag_name);
        ImageButton newTagDelete = (ImageButton) newTag.findViewById(R.id.edit_recipe_delete_tag);
        Category newCategory = null;
        while (newCategory == null) {
            newCategory = CookBookStorage.getInstance(this).getCategoryByID(category);
        }
        newTagName.setText(newCategory.getDescription());
        newTagDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagsLayout.removeView(newTag);
                tags.remove(category);
            }
        });
        tagsLayout.addView(newTag);
    }

    private void addIngredientToView(final Ingredient newIngredient) {
        final LinearLayout ingredientsLayout = (LinearLayout) findViewById(R.id.edit_recipe_body_ingredients);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Create ingredient view
        final View view = inflater.inflate(R.layout.edit_recipe_ingredient, null);
        EditText ingredientNameView = (EditText) view.findViewById(R.id.edit_recipe_ingredient_name);
        EditText ingredientCountView = (EditText) view.findViewById(R.id.edit_recipe_ingredient_count);
        Spinner ingredientMeasureView = (Spinner) view.findViewById(R.id.edit_recipe_ingredient_measure);
        ImageButton delete = (ImageButton) view.findViewById(R.id.edit_recipe_ingredient_delete);
        //Change name
        ingredientNameView.setText(newIngredient.getName());
        ingredientNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                newIngredient.setName(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        //Change count
        ingredientCountView.setText(new Double(newIngredient.getQuantity()).toString());
        ingredientCountView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    newIngredient.setQuantity(Double.parseDouble(charSequence.toString()));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        //Change measurement
        ArrayList<String> measurementNames = new ArrayList<>();
        for (Measure m : Measure.values()) {
            measurementNames.add(Ingredient.getMeasureName(m));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, measurementNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ingredientMeasureView.setAdapter(adapter);
        ingredientMeasureView.setSelection(newIngredient.getMeasure().ordinal());
        ingredientMeasureView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                newIngredient.setMeasure(Measure.values()[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        //remove ingredient
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredientsLayout.removeView(view);
                ingredients.remove(newIngredient);
            }
        });
        ingredientsLayout.addView(view);
    }
}
