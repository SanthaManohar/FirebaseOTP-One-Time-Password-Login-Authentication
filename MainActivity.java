package basicandroid.com.simpleotpgit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "PhoneAuth";

    private EditText phoneText;
    private EditText codeText;
    private Button verifyButton;
    private Button sendButton;
    private Button resendButtton;
    private Button signoutButton;
    private TextView statusText;
    String number;

    private String phoneverificationId;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;

    private PhoneAuthProvider.ForceResendingToken resendToken;

    private FirebaseAuth fbAuth;

    CountryCodePicker ccp;

    ProgressDialog progressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneText = (EditText)findViewById(R.id.phoneText);
        codeText = (EditText)findViewById(R.id.codeText);
        verifyButton = (Button)findViewById(R.id.verifyButton);
        sendButton = (Button)findViewById(R.id.sendButton);
        resendButtton = (Button)findViewById(R.id.resendButton);
        signoutButton = (Button)findViewById(R.id.signoutButton);
        statusText = (TextView)findViewById(R.id.statusText);

        ccp = (CountryCodePicker)findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);

        verifyButton.setEnabled(false);
        resendButtton.setEnabled(false);
        signoutButton.setEnabled(false);
        statusText.setText("Sign Out");

        fbAuth = FirebaseAuth.getInstance();


    }

    public void sendCode(View view){


        number = ccp.getFullNumberWithPlus();

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number, // Phone number to verify
                60, // Time Out duration
                TimeUnit.SECONDS, // Unit of TimeOut
                this, // Activity (for callback binding)
                verificationCallbacks);


    }

    private void setUpVerificationCallbacks() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying...");
        progressDialog.show();

        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                signoutButton.setEnabled(true);
                statusText.setText("Sign In");
                resendButtton.setEnabled(false);
                verifyButton.setEnabled(false);
                codeText.setText("");
                signInWithPhoneAuthCretendial(credential);


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                if(e instanceof FirebaseAuthInvalidCredentialsException){

                    // Invalid Request

                    Log.d(TAG,"Invalid Credential" +e.getLocalizedMessage());
                }else if(e instanceof FirebaseTooManyRequestsException){

                    // SMS quota exceeded

                    Log.d(TAG,"SMS Quota exceeded");
                }

            }




            public void onCodeSent(String verificationId,PhoneAuthProvider.ForceResendingToken token){

                phoneverificationId = verificationId;
                resendToken = token;
                verifyButton.setEnabled(true);
                sendButton.setEnabled(false);
                resendButtton.setEnabled(true);

            }

        };

    }



    public void verifyCode(View view){

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying...");
        progressDialog.show();

        String code = codeText.getText().toString();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneverificationId,code);
        signInWithPhoneAuthCretendial(credential);

        progressDialog.dismiss();

    }














    private  void signInWithPhoneAuthCretendial(PhoneAuthCredential credential){
        fbAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    signoutButton.setEnabled(true);
                    codeText.setText("");
                    statusText.setText("Signed In");
                    resendButtton.setEnabled(false);
                    verifyButton.setEnabled(false);
                    FirebaseUser user = task.getResult().getUser();
                    String phonenumber = user.getPhoneNumber();

                    progressDialog.dismiss();

                    Intent intent = new Intent(MainActivity.this,MessageActivity.class);
                    intent.putExtra("phone",phonenumber);
                    startActivity(intent);
                    finish();

                }else {
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException ){
                        //The varification code entered was invalid

                    }
                }

            }
        });
    }



    public void resendCode(View view){


        number = ccp.getFullNumberWithPlus();

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken

        );


    }




    public void signOut(View view){
        fbAuth.signOut();
        statusText.setText("Signed Out");
        signoutButton.setEnabled(false);
        sendButton.setEnabled(true);

    }

}
