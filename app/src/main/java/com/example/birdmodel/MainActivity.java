package com.example.birdmodel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.birdmodel.ml.AutoModel3;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

        TextView result,google;
          Button btn;
         private ImageView image;
         ActivityResultLauncher<String> mgetcontent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        google = findViewById(R.id.google);
        result = findViewById(R.id.output);
        btn = findViewById(R.id.button);
        image = findViewById(R.id.image);


        mgetcontent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri o) {
                Bitmap imagebitmap = null;
                try {
                    imagebitmap = UriToBitmap(o);
                }catch (IOException e){
                    e.printStackTrace();
                }
                image.setImageBitmap(imagebitmap);
                OutputGenerate(imagebitmap);
            }


        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q="+result.getText().toString()));
                startActivity(i);
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgetcontent.launch("image/*");
            }
        });
    }

    private void OutputGenerate(Bitmap imagebitmap) {
        try {
            AutoModel3 model = AutoModel3.newInstance(MainActivity.this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(imagebitmap);

            // Runs model inference and gets result.
            AutoModel3.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

            // Releases model resources if no longer used.


            int index =0;
            float max = probability.get(0).getScore();
             for (int i =0;i<probability.size();i++){
                 if (max<probability.get(i).getScore()){
                     max = probability.get(i).getScore();
                     index =i;
                 }
             }

          Category output = probability.get(index);
          result.setText(output.getLabel());
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    private Bitmap UriToBitmap(Uri o) throws IOException {
        return MediaStore.Images.Media.getBitmap(this.getContentResolver(),o);
    }
}