package com.sphericalelephant.example.greendao;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sphericalelephant.example.greendao.generated.AccessCard;
import com.sphericalelephant.example.greendao.generated.AccessCardDao;
import com.sphericalelephant.example.greendao.generated.DaoMaster;
import com.sphericalelephant.example.greendao.generated.DaoSession;
import com.sphericalelephant.example.greendao.generated.EmailAddress;
import com.sphericalelephant.example.greendao.generated.EmailAddressDao;
import com.sphericalelephant.example.greendao.generated.Person;
import com.sphericalelephant.example.greendao.generated.PersonDao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by siyb on 29/09/15.
 */
public class MainActivity extends AppCompatActivity {
	private static final String DATABASE_NAME = "greendao-demo";

	// a bunch of views used to bind data to
	private TextView userName;
	private TextView birthDay;
	private TextView height;
	private TextView expires;
	private TextView email;

	// database connection / session related
	private SQLiteDatabase databaseConnection;
	private DaoMaster daoMaster;
	private DaoSession daoSession;

	// data access objects for our entities
	private PersonDao personDao;
	private AccessCardDao accessCardDao;
	private EmailAddressDao emailAddressDao;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mainactivity);

		// view bindings, nothing special happens here
		userName = (TextView) findViewById(R.id.activity_mainactivity_tv_username);
		birthDay = (TextView) findViewById(R.id.activity_mainactivity_tv_birthday);
		height = (TextView) findViewById(R.id.activity_mainactivity_tv_height);
		expires = (TextView) findViewById(R.id.activity_mainactivity_tv_expires);
		email = (TextView) findViewById(R.id.activity_mainactivity_tv_email);

		findViewById(R.id.activity_mainactivity_b_createuser).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Person p = getPerson();
				if (p == null) {
					p = createPerson();
				} else {
					Toast.makeText(MainActivity.this, R.string.activity_mainactivity_personexists, Toast.LENGTH_SHORT).show();
				}
				userName.setText(p.getName());
				birthDay.setText(p.getBirthday().toString());
				height.setText(String.valueOf(p.getHeight())); // int needs to be parsed to string, otherwise it will be used understood as a string id
				expires.setText(p.getAccessCard().getExpirationDate().toString());
				email.setText(p.getEmailAddressList().get(0).getAddress());
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		databaseConnection = new DaoMaster.DevOpenHelper(this, DATABASE_NAME, null)
				.getWritableDatabase();
		daoMaster = new DaoMaster(databaseConnection);
		daoSession = daoMaster.newSession(); // we can instantiate multiple sessions as well, sessions share the connection owned by the DaoMaster!
		personDao = daoSession.getPersonDao();
		accessCardDao = daoSession.getAccessCardDao();
		emailAddressDao = daoSession.getEmailAddressDao();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (databaseConnection != null && databaseConnection.isOpen()) {
			databaseConnection.close(); // close db connection if it's open
		}
	}

	private List<Person> getPersons() {
		return personDao.queryBuilder().listLazy();
	}

	private Person getPerson() {
		// using a very simple query to get the person. QueryBuilder also supports more advanced stuff like joins
		Person p = personDao.queryBuilder()
				.where(PersonDao.Properties.Name.eq("ExampleName"))
				.unique();
		return p;
	}

	private EmailAddress createEmail(Person p) {
		EmailAddress e = new EmailAddress();
		e.setAddress("foo@bar.com");
		e.setPerson(p);
		emailAddressDao.insert(e);
		return e;
	}

	private AccessCard createAccessCard() {
		Calendar cal = Calendar.getInstance();
		AccessCard accessCard = new AccessCard();
		cal.set(2050, 11, 31);
		accessCard.setExpirationDate(new Date(cal.getTimeInMillis()));

		accessCardDao.insert(accessCard);

		return accessCard;
	}

	private Person createPerson() {
		Calendar cal = Calendar.getInstance();

		Person person = new Person();
		person.setAccessCard(createAccessCard());

		cal.set(1983, 10, 11);
		person.setBirthday(new Date(cal.getTimeInMillis()));
		person.setHeight(175);
		person.setName("ExampleName");

		// we need to insert the person before we can create and add the email, because we require a user id
		personDao.insert(person);

		EmailAddress email = createEmail(person);
		person.getEmailAddressList().add(email);
		person.update();
		return person;
	}
}
