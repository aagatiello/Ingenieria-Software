package com.hiBud.app.Activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hiBud.app.Constants;
import com.hiBud.app.DAO.UserDAO;
import com.hiBud.app.Firebase.User;
import com.hiBud.app.R;
import com.kbeanie.multipicker.api.CacheLocation;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignInActivity extends AppCompatActivity {

    private CircleImageView fotoPerfil;
    private EditText txtNombre;
    private EditText txtCorreo;
    private EditText txtPassword;
    private EditText txtPasswordRepetida;
    private EditText txtFechaDeNacimiento;
    private RadioButton rdHombre;
    private RadioButton rdMujer;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    private ImagePicker imagePicker;
    private CameraImagePicker cameraPicker;

    private String pickerPath;
    private Uri fotoPerfilUri;
    private long fechaDeNacimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        fotoPerfil = findViewById(R.id.fotoPerfil);
        txtNombre = findViewById(R.id.idRegistroNombre);
        txtCorreo = findViewById(R.id.idRegistroCorreo);
        txtPassword = findViewById(R.id.idRegistroContraseña);
        txtPasswordRepetida = findViewById(R.id.idRegistroContraseñaRepetida);
        txtFechaDeNacimiento = findViewById(R.id.txtFechaDeNacimiento);
        rdHombre = findViewById(R.id.rdHombre);
        rdMujer = findViewById(R.id.rdMujer);
        Button btnRegistrar = findViewById(R.id.idRegistroRegistrar);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        imagePicker = new ImagePicker(this);
        cameraPicker = new CameraImagePicker(this);

        cameraPicker.setCacheLocation(CacheLocation.EXTERNAL_STORAGE_APP_DIR);

        imagePicker.setImagePickerCallback(new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> list) {
                if (!list.isEmpty()) {
                    String path = list.get(0).getOriginalPath();
                    fotoPerfilUri = Uri.parse(path);
                    fotoPerfil.setImageURI(fotoPerfilUri);
                }
            }

            @Override
            public void onError(String s) {
                Toast.makeText(SignInActivity.this, "Error: " + s, Toast.LENGTH_SHORT).show();
            }
        });

        cameraPicker.setImagePickerCallback(new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> list) {
                String path = list.get(0).getOriginalPath();
                fotoPerfilUri = Uri.fromFile(new File(path));
                fotoPerfil.setImageURI(fotoPerfilUri);
            }

            @Override
            public void onError(String s) {
                Toast.makeText(SignInActivity.this, "Error: " + s, Toast.LENGTH_SHORT).show();
            }
        });

        fotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SignInActivity.this);
                dialog.setTitle("Foto de perfil");

                String[] items = {"Galeria", "Camara"};

                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                imagePicker.pickImage();
                                break;
                            case 1:
                                pickerPath = cameraPicker.pickImage();
                                break;
                        }
                    }
                });

                AlertDialog dialogBuild = dialog.create();
                dialogBuild.show();

            }
        });

        txtFechaDeNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(SignInActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int mes, int dia) {
                        Calendar calendarResult = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, mes);
                        calendar.set(Calendar.DAY_OF_MONTH, dia);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date date = calendarResult.getTime();
                        String birthDate = simpleDateFormat.format(date);
                        fechaDeNacimiento = date.getTime();
                        txtFechaDeNacimiento.setText(birthDate);
                    }
                }, calendar.get(Calendar.YEAR) , calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = txtCorreo.getText().toString();
                final String name = txtNombre.getText().toString();
                if (validateEmail(email) && validatePassword() && validateName(name)) {
                    String password = txtPassword.getText().toString();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        final String gender;

                                        if (rdHombre.isChecked()) {
                                            gender = "Hombre";
                                        } else {
                                            gender = "Mujer";
                                        }

                                        if (fotoPerfilUri != null) {

                                            UserDAO.getInstance().uploadPicUri(fotoPerfilUri, new UserDAO.IDevolverUrlFoto() {
                                                @Override
                                                public void devolerUrlString(String url) {
                                                    Toast.makeText(SignInActivity.this, "Gracias por regitrarte!", Toast.LENGTH_SHORT).show();
                                                    User user = new User();
                                                    user.setCorreo(email);
                                                    user.setNombre(name);
                                                    user.setFechaDeNacimiento(fechaDeNacimiento);
                                                    user.setGenero(gender);
                                                    user.setFotoPerfilURL(url);
                                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                                    DatabaseReference reference = database.getReference("Usuarios/" + currentUser.getUid());
                                                    reference.setValue(user);
                                                    sendEmailVerification();
                                                    nextActivity();
                                                }
                                            });

                                        } else {
                                            Toast.makeText(SignInActivity.this, "Gracias por regitrarte!", Toast.LENGTH_SHORT).show();
                                            User user = new User();
                                            user.setCorreo(email);
                                            user.setNombre(name);
                                            user.setFechaDeNacimiento(fechaDeNacimiento);
                                            user.setGenero(gender);
                                            user.setFotoPerfilURL(Constants.DEFAULT_PICTURE);
                                            FirebaseUser currentUser = mAuth.getCurrentUser();
                                            DatabaseReference reference = database.getReference("Usuarios/" + currentUser.getUid());
                                            reference.setValue(user);
                                            sendEmailVerification();
                                            nextActivity();
                                        }

                                    } else {
                                        Toast.makeText(SignInActivity.this, "Error al registrarse", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(SignInActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Glide.with(this).load(Constants.DEFAULT_PICTURE).into(fotoPerfil);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Picker.PICK_IMAGE_DEVICE && resultCode == RESULT_OK) {
            imagePicker.submit(data);
        } else if (requestCode == Picker.PICK_IMAGE_CAMERA && resultCode == RESULT_OK) {
            cameraPicker.reinitialize(pickerPath);
            cameraPicker.submit(data);
        }
    }

    private boolean validateEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public boolean validatePassword() {
        String password, repeatPswd;
        password = txtPassword.getText().toString();
        repeatPswd = txtPasswordRepetida.getText().toString();
        if (password.equals(repeatPswd)) {
            if (password.length() >= 6 && password.length() <= 15) {
                return true;
            } else return false;
        } else return false;
    }

    public boolean validateName(String name) {
        return !name.isEmpty();
    }

    public void sendEmailVerification() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    private static final String TAG = "CORREO";

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("picker_path", pickerPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState.containsKey("picker_path")) {
            pickerPath = savedInstanceState.getString("picker_path");
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void nextActivity() {
        startActivity(new Intent(SignInActivity.this, LogInActivity.class));
        finish();
    }

}
