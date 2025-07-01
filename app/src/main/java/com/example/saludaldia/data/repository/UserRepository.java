package com.example.saludaldia.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.saludaldia.data.model.User;

public class UserRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public interface UserCallback {
        void onSuccess(User user);

        void onFailure(Exception e);
    }
    public void getUserById(@NonNull String userId, @NonNull UserCallback callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("Usuario no encontrado"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
    public void createUser(@NonNull User user, @NonNull UserCallback callback) {
        db.collection("users")
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                .addOnFailureListener(callback::onFailure);
    }
    public void updateUser(@NonNull User user, @NonNull UserCallback callback) {
        db.collection("users")
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                .addOnFailureListener(callback::onFailure);
    }
    public void getCurrentUser(@NonNull UserCallback callback) {
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            getUserById(uid, callback);
        } else {
            callback.onFailure(new Exception("No hay usuario logueado"));
        }
    }
}
