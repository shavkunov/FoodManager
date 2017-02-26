package ru.spbau.mit.foodmanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class EditStepActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_IMAGE = 0;
    private ArrayList<EditRecipeActivity.UriStep> uriSteps;
    private Integer stepPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_step);

        Intent task = getIntent();
        uriSteps = (ArrayList<EditRecipeActivity.UriStep>) task.getSerializableExtra("UriSteps");
        if (uriSteps == null) {
            uriSteps = new ArrayList<>();
        }
        stepPosition = 0;
        showStep(stepPosition);
        //Initialize edit text
        EditText stepDescription = (EditText) findViewById(R.id.edit_step_step_text);
        stepDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (uriSteps.size() > stepPosition) {
                    uriSteps.get(stepPosition).setDescription(charSequence.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int errorCode, Intent resultContainer) {
        if (errorCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    try {
                        Uri imageUri = resultContainer.getData();
                        ImageView image = (ImageView) findViewById(R.id.edit_step_step_image);
                        image.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri));
                        uriSteps.get(stepPosition).setImageUri(imageUri);
                    }
                    catch (IOException e) {
                        //Failed load image
                    }
                    break;
            }
        }
    }

    public void onAddClick(View v) {
        EditRecipeActivity.UriStep newStep = new EditRecipeActivity.UriStep();
        uriSteps.add(stepPosition, newStep);
        showStep(stepPosition);
    }

    public void onDeleteClick(View v) {
        if (stepPosition > 0) {
            uriSteps.remove((int)stepPosition);
            stepPosition--;
        }
        showStep(stepPosition);
    }

    public void onImageClick(View v) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MainActivity.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE);
    }

    public void onSaveClick(View v) {
        Boolean checkingOK = true;
        for (EditRecipeActivity.UriStep step : uriSteps) {
            if (step.getDescription().equals("") || step.getImageUri() == null) {
                checkingOK = false;
                //TODO: Show alarm
            }
        }
        if (checkingOK) {
            Intent result = new Intent();
            result.putExtra("UriSteps", uriSteps);
            setResult(RESULT_OK, result);
            //TODO: Show result ok
            finish();
        }
    }

    public void onPrevBtnClick(View view) {
        if (stepPosition > 0) {
            stepPosition--;
            showStep(stepPosition);
        }
    }

    public void onNextBtnClick(View view) {
        if (stepPosition < uriSteps.size() - 1) {
            stepPosition++;
            showStep(stepPosition);
        }
    }

    private void showStep(int id) {
        //TODO If there are no step, hide edit field and image field
        if (uriSteps.size() > 0) {
            EditText description = (EditText) findViewById(R.id.edit_step_step_text);
            if (uriSteps.get(id).getDescription() != null) {
                description.setText(uriSteps.get(id).getDescription());
            } else {
                description.setText("");
            }
            ImageView image = (ImageView) findViewById(R.id.edit_step_step_image);
            if (uriSteps.get(id).getImageUri() != null) {
                try {
                    image.setImageBitmap(
                            MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriSteps.get(id).getImageUri()));
                } catch (IOException e) {
                    Step s = new Step(
                            uriSteps.get(id).getDescription(),
                            uriSteps.get(id).getImageUri().toString());
                    CookBookStorage.getInstance(this).downloadStepImage(s);
                    image.setImageBitmap(s.getImage());
                }
            } else {
                //TODO upload image
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.add_image));
            }
            TextView counter = (TextView) findViewById(R.id.edit_step_position);
            counter.setText(((Integer) (id + 1)).toString());
            TextView count = (TextView) findViewById(R.id.edit_step_step_count);
            count.setText(((Integer) uriSteps.size()).toString());

        }
    }
}
