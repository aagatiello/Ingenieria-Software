package com.hiBud.app.DAO;

import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hiBud.app.Constants;
import com.hiBud.app.Firebase.User;
import com.hiBud.app.Logic.UserLogic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class UserDAO {

    private static UserDAO userDAO;
    private DatabaseReference userRef;
    private StorageReference profilePicRef;

    private UserDAO() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        userRef = database.getReference(Constants.USERS_NODE);
        profilePicRef = storage.getReference("Fotos/FotoPerfil/" + getUserKey());
    }

    public static UserDAO getInstance() {
        if (userDAO == null) userDAO = new UserDAO();
        return userDAO;
    }

    public String getUserKey() {
        return FirebaseAuth.getInstance().getUid();
    }

    public void getUserKeyInfo(final String key, final IDevolverUsuario iDevolverUsuario) {
        userRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                UserLogic userLogic = new UserLogic(key, user);
                iDevolverUsuario.returnUser(userLogic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                iDevolverUsuario.returnError(databaseError.getMessage());
            }
        });

    }

    public void uploadPicUri(Uri uri, final IDevolverUrlFoto iDevolverUrlFoto) {
        String picName = "";
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("SSS.ss-mm-hh-dd-MM-yyyy", Locale.getDefault());
        picName = simpleDateFormat.format(date);
        final StorageReference picRef = profilePicRef.child(picName);
        picRef.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
                    assert uri != null;
                    iDevolverUrlFoto.devolerUrlString(uri.toString());
                }
            }
        });
    }

    public interface IDevolverUsuario {
        public void returnUser(UserLogic userLogic);

        public void returnError(String error);
    }

    public interface IDevolverUrlFoto {
        public void devolerUrlString(String url);
    }

}