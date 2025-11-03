package com.assignment.contactsservice.repository;

import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.assignment.contactsservice.model.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {

	/**
	 * Retrieves all contacts as a stream. This is essential for handling millions
	 * of rows efficiently without OutOfMemoryError(OOM) errors. The stream must be
	 * processed within a transaction.
	 */
	@Query("SELECT c FROM Contact c")
	Stream<Contact> streamAllContacts();

}
