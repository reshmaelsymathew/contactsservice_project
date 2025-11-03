package com.assignment.contactsservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a persistent entity in the 'contacts' database table. This class
 * is used for both retrieving contacts (GET) and creating new ones (POST).
 */
@Entity // to mark a Java class as a persistent entity.
@Table(name = "contacts") // Explicitly map to existing table 'contacts'
public class Contact {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Works with BIGSERIAL or auto-increment IDs
	@Column(name = "id") // Explicitly map to DB column
	private Long id;

	// Bean Validation: Ensures the name field is not null or whitespace when
	// receiving a POST request
	@NotBlank(message = "Contact name cannot be empty")
	@Column(name = "name", nullable = false) // Ensures name is not null
	private String name;

	/**
	 * Default constructor required by the JPA specification. It is set to
	 * 'protected' to enforce using parameterized constructors for creating new
	 * objects in application code, ensuring they are initialized correctly.
	 */
	protected Contact() {
	}

	/**
	 * Parameterized constructor used for creating a new Contact object *before* *
	 * it has been saved to the database (i.e., when handling a POST request).
	 * 
	 * @param name The name of the contact.
	 */
	public Contact(String name) {
		this.name = name;
	}

	/**
	 * Full parameterized constructor used for mapping results from the database or
	 * for creating initialized objects for mocking/testing purposes.
	 * 
	 * @param id   The database primary key.
	 * @param name The name of the contact.
	 */
	public Contact(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns a string representation of the Contact object for logging and
	 * debugging.
	 */
	@Override
	public String toString() {
		return "Contact [id=" + id + ", name=" + name + "]";
	}

}
