package formation.exemple.helloedittext;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** écouteur */
        Button buttonOk=(Button) findViewById(R.id.ok);
        buttonOk.setOnClickListener(this);
        Button buttonCancel=(Button) findViewById(R.id.cancel);
        buttonCancel.setOnClickListener(this);
    }

    public void onClick(View v) {
        EditText myText=(EditText)findViewById(R.id.mytext);
        TextView myAffiche=(TextView)findViewById(R.id.affiche);
        switch (v.getId())
        {	case R.id.ok :
            myAffiche.setText(myText.getText());
            break;
            case R.id.cancel :
                myAffiche.setText("");
                myText.setText("");
                break;
        }

    }
}