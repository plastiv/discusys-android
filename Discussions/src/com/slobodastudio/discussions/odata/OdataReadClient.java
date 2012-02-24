package com.slobodastudio.discussions.odata;

import com.slobodastudio.discussions.odata.DiscussionsTableShema.Person;
import com.slobodastudio.discussions.odata.DiscussionsTableShema.Tables;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OdataReadClient {

	private final ODataConsumer consumer;

	/** Create a new odata consumer pointing to the odata read-write service.
	 * 
	 * @param serviceRootUri
	 *            the service uri e.g. http://services.odata.org/Northwind/Northwind.svc/ */
	public OdataReadClient(final String serviceRootUri) {

		// FIXME: check if network is accessible
		consumer = ODataConsumer.create(serviceRootUri);
	}

	public ArrayList<Map<String, String>> getUsers() {

		ArrayList<Map<String, String>> users = new ArrayList<Map<String, String>>();
		Map<String, String> m;
		for (OEntity person : consumer.getEntities(Tables.PERSON).execute()) {
			m = new HashMap<String, String>();
			m.put(Person.NAME, (String) person.getProperty(Person.NAME).getValue());
			m.put(Person.EMAIL, (String) person.getProperty(Person.EMAIL).getValue());
			users.add(m);
		}
		return users;
	}
}
