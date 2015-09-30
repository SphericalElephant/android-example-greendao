package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class Generator {
	private static final String TARGET_FOLDER = "../app/src/main/java/";
	private static final int SCHEMA_VERSION = 1;

	public static final void main(String[] args) throws Throwable {
		Schema schema = new Schema(SCHEMA_VERSION, "com.sphericalelephant.example.greendao.generated");

		// defining the Person entity
		Entity person = schema.addEntity("Person");
		person.setTableName("PERSON"); // note that we can actually manually provide a table name!
		person.addIdProperty();
		person.addStringProperty("name").notNull().index(); // setting an index
		person.addDateProperty("birthday").notNull();
		person.addIntProperty("height");
		// this field is used to store the access card relation
		Property accessCardId = person.addLongProperty("accessCardId").notNull().getProperty();

		Entity accessCard = schema.addEntity("AccessCard");
		accessCard.addIdProperty();
		accessCard.addDateProperty("expirationDate");

		Entity emailAddress = schema.addEntity("EmailAddress");
		emailAddress.addIdProperty();
		emailAddress.addStringProperty("address").unique();
		// this field is used to refer to the person within the email address table
		Property personId = emailAddress.addLongProperty("personId").notNull().getProperty();

		// To-One relationship, a person has an accessCard, the id is stored in the subject that has one relation
		person.addToOne(accessCard, accessCardId);
		// To-Many relationship, a person has multiple email addresses, the id is stored in the target of the To-Many relation
		person.addToMany(emailAddress, personId);
		// the following line of code allows accessing the person from the email entity
		emailAddress.addToOne(person, personId);
		// n:m relationships are not directly supported, use a join table entity to emulate this special kind of relationship

		// we are generating the schema here
		new DaoGenerator().generateAll(schema, TARGET_FOLDER);
	}
}
