package com.imgprocesadondk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.data;
import static android.R.attr.width;

public class ImgProcesadoNDK extends AppCompatActivity {
    private String tag = "ImgProcesadoNDK";
    private Bitmap bitmapOriginal = null;
    private Bitmap bitmapGrises = null;
    private Bitmap bitmapSepia = null;
    private Bitmap bitmapMarco_1 = null;
    private ImageView ivDisplay = null;
    // Propuesta Doc Oficial
    private String mCurrentPhotoPath;
    // doc Internet
    private static int TAKE_PICTURE = 1;
    private static int SELECT_PICTURE = 2;



    static {
        System.loadLibrary("imgprocesadondk");
    }

    // funciones de Código nativo
    public native void convertirGrises(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void convertirSepia(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void creaMarco(Bitmap bitmapIn, Bitmap bitmapOut);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(tag, "Imagen antes de modificar");
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        setColor(true);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void setColor(Boolean imgOrigen)
    {
        Log.i(tag, "Resetear Imagen");
        if (imgOrigen) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            // Asegurar que la imagen tiene 24 bits de color
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmapOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.sampleimage, options);
            if (bitmapOriginal != null) ivDisplay.setImageBitmap(bitmapOriginal);
        }
        ivDisplay.setImageBitmap(bitmapOriginal);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case  R.id.action_reset:
                  setColor(true);
            break;
            case R.id.color:
                  setColor(false);
                break;
            case  R.id.gray:
                Log.i(tag, "Conversion a escala de grises");
                bitmapGrises = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
                convertirGrises(bitmapOriginal, bitmapGrises);
                ivDisplay.setImageBitmap(bitmapGrises);
            break;
            case R.id.sepia:
                Log.i(tag, "Conversion a escala a Sepia");
                bitmapSepia = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
                convertirSepia(bitmapOriginal, bitmapSepia);
                ivDisplay.setImageBitmap(bitmapSepia);
            break;
            case R.id.marco_1:
                Log.i(tag, "Conversion a escala a Sepia");
                bitmapMarco_1 = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
                creaMarco(bitmapOriginal, bitmapMarco_1);
                ivDisplay.setImageBitmap(bitmapMarco_1);
                break;
            case R.id.galery:
              Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
              int code = SELECT_PICTURE;
              startActivityForResult(intent, code);
            break;
        }

        return super.onOptionsItemSelected(item);
    }



    public void onTakePhoto(View v) {
        //tomarFoto();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        int code = TAKE_PICTURE;
        try {
            File fic =  createImageFile();
            Uri output = Uri.fromFile(fic);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, output);
            startActivityForResult(takePictureIntent, code);
//        } else if (rbtnGallery.isChecked()){
//            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//            code = SELECT_PICTURE;
//        }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"No se ha podido capturar la fotografía de forma correcta " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


    //Crea un fichero que su nombre para hacer único
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    // Al tomar fotografía, comprobamos si resultado correcto
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            if (data == null) {
                escalaImagen();
            }
        }
        else if (requestCode == SELECT_PICTURE){
            Uri selectedImage = data.getData();
            InputStream is;
            try {
                is = getContentResolver().openInputStream(selectedImage);
                BufferedInputStream bis = new BufferedInputStream(is);
                Bitmap bitmap = BitmapFactory.decodeStream(bis);
         //       ImageView iv = (ImageView)findViewById(R.id.imgView);
                bitmapOriginal = bitmap;
                setColor(false);
            } catch (FileNotFoundException e) {}
        }
    }

    // Escalamos la fotografía, y la mostramos
    private void escalaImagen()
    {
        int targetW = ivDisplay.getWidth();
        int targetH = ivDisplay.getHeight();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath), targetW, targetH, true);
        bitmapOriginal = resizedBitmap;
        ivDisplay.setImageBitmap(bitmapOriginal);
    }
}


