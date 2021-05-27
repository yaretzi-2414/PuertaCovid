package com.example.puertacovid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {
    private EditText emailEt, passwordEt1, passwordEt2;
    private Button SignUpButton;
    private TextView SignInTv;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    boolean valid = true;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    CheckBox esAdministradorBox,esUsuarioBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);
        firebaseAuth = FirebaseAuth.getInstance();
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        emailEt = findViewById(R.id.email);
        passwordEt1 = findViewById(R.id.password1);
        passwordEt2 = findViewById(R.id.password2);
        SignUpButton = findViewById(R.id.register);
        esAdministradorBox = findViewById(R.id.administrador);
        esUsuarioBox = findViewById(R.id.usuario);
        progressDialog = new ProgressDialog(this);
        SignInTv = findViewById(R.id.signInTv);
        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });
        SignInTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        esUsuarioBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    esAdministradorBox.setChecked(false);
                }
            }
        });
        esAdministradorBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    esUsuarioBox.setChecked(false);
                }
            }
        });
    }

    private void Register() {
        String email = emailEt.getText().toString();
        String password1 = passwordEt1.getText().toString();
        String password2 = passwordEt2.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEt.setError("Ingresa tu email");
            return;
        } else if (TextUtils.isEmpty(password1)) {
            passwordEt1.setError("Ingresa tu contraseña");
            return;
        } else if (TextUtils.isEmpty(password2)) {
            passwordEt2.setError("Confirma tu contraseña");
            return;
        } else if (!password1.equals(password2)) {
            passwordEt2.setError("Diferente contraseña");
            return;
        } else if (password1.length() < 4) {
            passwordEt1.setError("La contraseña debe contener mas de 4 caracteres");
            return;
        } else if (!isVallidEmail(email)) {
            emailEt.setError("Correo invalido");
            return;
        } else if (!(esAdministradorBox.isChecked() || esUsuarioBox.isChecked())){
            Toast.makeText(RegistroActivity.this, "Selecciona el tipo de cuenta", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Porfavor, espere");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth.createUserWithEmailAndPassword(emailEt.getText().toString(), passwordEt1.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegistroActivity.this, "Registro Completo, por favor, verifica tu email", Toast.LENGTH_LONG).show();
                    FirebaseUser email = firebaseAuth.getCurrentUser();
                    FirebaseUser user = fAuth.getCurrentUser();
                    email.sendEmailVerification();
                    DocumentReference df = fStore.collection("Usuarios").document(user.getUid());
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("Email", emailEt.getText().toString());

                    if(esAdministradorBox.isChecked()){
                        userInfo.put("esAdministrador", "1");
                    }
                    if(esUsuarioBox.isChecked()){
                        userInfo.put("esUsuario", "1");
                    }

                    df.set(userInfo);

                } else {
                    Toast.makeText(RegistroActivity.this, "Registro Fallido", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    private Boolean isVallidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
