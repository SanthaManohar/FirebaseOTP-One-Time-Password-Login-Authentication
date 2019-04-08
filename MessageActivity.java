package basicandroid.com.simpleotpgit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MessageActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent intent = getIntent();
        String phoneNumber = intent.getExtras().getString("phone");

        textView = (TextView)findViewById(R.id.mytextView);

        textView.setText("This is the Authenticated PhoneNumber" + phoneNumber);

    }
}

