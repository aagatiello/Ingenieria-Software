package com.hiBud.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hiBud.app.R;

public class LogInActivity extends AppCompatActivity {


    private EditText txtEmail;
    private EditText txtPassword;
    private FirebaseAuth mAuth;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtEmail = findViewById(R.id.idCorreoLogin);
        txtPassword = findViewById(R.id.idContraseñaLogin);
        Button btnLogin = findViewById(R.id.idLoginLogin);
        Button btnRegistro = findViewById(R.id.idRegistroLogin);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmail.getText().toString();
                if (validateEmail(email) && validatePassword()) {
                    String password = txtPassword.getText().toString();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LogInActivity.this, "Inicio sesión correcto", Toast.LENGTH_SHORT).show();
                                        flag = true;
                                        nextActivity();
                                    } else {
                                        Toast.makeText(LogInActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                } else {
                    Toast.makeText(LogInActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogInActivity.this, SignInActivity.class));
            }
        });
    }

    private boolean validateEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public boolean validatePassword() {
        String password;
        password = txtPassword.getText().toString();
        return password.length() >= 6 && password.length() <= 15;
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if ((currentUser != null) && flag) {
            nextActivity();
        }
    }

    private void nextActivity() {
        startActivity(new Intent(LogInActivity.this, ChatActivity.class));
        finish();
    }

}
