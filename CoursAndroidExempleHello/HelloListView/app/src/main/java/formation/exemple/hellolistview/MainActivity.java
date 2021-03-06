package formation.exemple.hellolistview;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
                          implements AdapterView.OnItemClickListener {

    private ListView lv;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //lier la liste des pays au layout 'list' à l'aide d'un adaptateur (tableau)
        String[] countries = getCountries();

        ArrayAdapter<String> arrayAdapterCountries = new ArrayAdapter<String>(this,
                R.layout.list_item, countries);

        //l'adaptateur Sert de source de données au ListView
        lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(arrayAdapterCountries );
        lv.setOnItemClickListener(this);

        arrayAdapterCountries.notifyDataSetChanged();


    }

    @Override
    public void onItemClick(AdapterView<?> ad, View v, int pos, long id) {
        // When clicked, show a toast with the TextView text
        String  itemValue = (String) ad.getItemAtPosition(pos);

        Toast toast = Toast.makeText(getApplicationContext(),
                                    "Position : " + pos
                                     +" Item : "+ itemValue,
                                     Toast.LENGTH_LONG);
        toast.show();
    }

    private String[] getCountries()
    { return  new String[] {
            "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
            "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina",
            "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan",
            "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",
            "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia",
            "Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil",
            "British Indian Ocean   Territory",
            "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
            "Cote d'Ivoire", "Cambodia", "Cameroon", "Canada", "Cape Verde",
            "Cayman Islands", "Central African Republic", "Chad", "Chile", "China",
            "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo",
            "Cook Islands", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic",
            "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic",
            "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea",
            "Estonia", "Ethiopia", "Faeroe Islands", "Falkland Islands", "Fiji", "Finland",
            "Former Yugoslav Republic of Macedonia", "France"};

    }

}