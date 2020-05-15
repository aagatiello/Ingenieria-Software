package com.hiBud.app.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hiBud.app.Adapter.MessageAdapter;
import com.hiBud.app.DAO.UserDAO;
import com.hiBud.app.Firebase.Message;
import com.hiBud.app.Firebase.User;
import com.hiBud.app.Logic.MessageLogic;
import com.hiBud.app.Logic.UserLogic;
import com.hiBud.app.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final int PHOTO_SEND = 1;
    private static final int PROFILE_PIC = 2;
    private TextView nombre;
    private RecyclerView rvMensajes;
    private EditText txtMensaje;
    private Button btnEnviar;
    private MessageAdapter adapter;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;

    private FirebaseAuth mAuth;
    private String USER_NAME;

    public static boolean verifyStoragePermissions(Activity activity) {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int REQUEST_EXTERNAL_STORAGE = 1;
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        CircleImageView fotoPerfil = (CircleImageView) findViewById(R.id.fotoPerfil);
        nombre = findViewById(R.id.nombre);
        rvMensajes = findViewById(R.id.rvMensajes);
        txtMensaje = findViewById(R.id.txtMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
        ImageButton btnEnviarFoto = findViewById(R.id.btnEnviarFoto);
        Button cerrarSesion = findViewById(R.id.cerrarSesion);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("chat");      // Chat Version
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        adapter = new MessageAdapter(this);
        LinearLayoutManager l = new LinearLayoutManager(this);
        rvMensajes.setLayoutManager(l);
        rvMensajes.setAdapter(adapter);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendMessage = txtMensaje.getText().toString();
                if (!sendMessage.isEmpty()) {
                    Message message = new Message();
                    message.setMensaje(sendMessage);
                    message.setContieneFoto(false);
                    message.setKeyEmisor(UserDAO.getInstance().getUserKey());
                    databaseReference.push().setValue(message);
                    txtMensaje.setText("");
                }
            }
        });

        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                returnLogin();
            }
        });

        btnEnviarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/jpeg");
                i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(i, "Selecciona una foto"), PHOTO_SEND);
            }
        });

        fotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/jpeg");
                i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(i, "Selecciona una foto"), PROFILE_PIC);
            }
        });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                setScrollbar();
            }
        });

        databaseReference.addChildEventListener(new ChildEventListener() {
            Map<String, UserLogic> temporaryUser = new HashMap<>();

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Message message = dataSnapshot.getValue(Message.class);
                final MessageLogic messageLogic = new MessageLogic(dataSnapshot.getKey(), message);
                final int position = adapter.addMessage(messageLogic);

                if (temporaryUser.get(message.getKeyEmisor()) != null) {
                    messageLogic.setUserLogic(temporaryUser.get(message.getKeyEmisor()));
                    adapter.updateMessage(position, messageLogic);
                } else {
                    UserDAO.getInstance().getUserKeyInfo(message.getKeyEmisor(), new UserDAO.IDevolverUsuario() {
                        @Override
                        public void returnUser(UserLogic userLogic) {
                            temporaryUser.put(message.getKeyEmisor(), userLogic);
                            messageLogic.setUserLogic(userLogic);
                            adapter.updateMessage(position, messageLogic);
                        }

                        @Override
                        public void returnError(String error) {
                            Toast.makeText(ChatActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        verifyStoragePermissions(this);

    }

    private void setScrollbar() {
        rvMensajes.scrollToPosition(adapter.getItemCount() - 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_SEND && resultCode == RESULT_OK) {
            Uri u = data.getData();
            StorageReference storageReference = storage.getReference("imagenes_chat");
            assert u != null;
            final StorageReference picRef = storageReference.child(Objects.requireNonNull(u.getLastPathSegment()));
            picRef.putFile(u).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return picRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        Message message = new Message();
                        assert uri != null;
                        message.setUrlFoto(uri.toString());
                        message.setContieneFoto(true);
                        message.setKeyEmisor(UserDAO.getInstance().getUserKey());
                        databaseReference.push().setValue(message);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            btnEnviar.setEnabled(false);
            DatabaseReference reference = database.getReference("Usuarios/" + currentUser.getUid());
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    USER_NAME = user.getNombre();
                    nombre.setText(USER_NAME);
                    btnEnviar.setEnabled(true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            returnLogin();
        }
    }

    private void returnLogin() {
        startActivity(new Intent(ChatActivity.this, LogInActivity.class));
        finish();
    }
}
