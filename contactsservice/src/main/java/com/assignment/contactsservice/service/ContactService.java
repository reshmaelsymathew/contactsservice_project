package com.assignment.contactsservice.service;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import com.assignment.contactsservice.model.Contact;

public interface ContactService {

	/**
	 * Creates a new contact in the database and publishes an event.
	 *
	 * @param contact contact to be created
	 * @return saved contact
	 */
	Contact createContact(Contact contact);

	/**
	 * Returns all contacts that do NOT match the given regular expression. Uses
	 * streaming to handle large datasets.
	 *
	 * @param nameFilter regex pattern to exclude
	 * @return list of filtered contacts
	 */
	List<Contact> getContactsExcludingRegex(String nameFilter) throws PatternSyntaxException;

}
