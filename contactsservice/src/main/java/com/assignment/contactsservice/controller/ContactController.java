package com.assignment.contactsservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assignment.contactsservice.model.Contact;
import com.assignment.contactsservice.service.ContactService;

import jakarta.validation.Valid;

/**
 * REST controller for managing contact-related operations.
 * Handles incoming HTTP requests for creating and retrieving contacts.
 */
@RestController
@RequestMapping("/hello")
public class ContactController {

	private static final Logger log = LoggerFactory.getLogger(ContactController.class);

	private final ContactService contactService;

	public ContactController(ContactService contactService) {
		this.contactService = contactService;
	}

	/**
	 * Endpoint to create a new contact. Validates input using Bean Validation
	 * before saving and publishing event.
	 * 
	 * @param contact       The Contact object from the request body.
	 * @param bindingResult Captures validation errors triggered by @Valid.
	 * @return ResponseEntity with created contact (201 Created) or a detailed error
	 *         response (400 Bad Request).
	 */
	@PostMapping("/contacts")
	public ResponseEntity<?> createContact(@Valid @RequestBody Contact contact, // @Valid triggers model validation
			BindingResult bindingResult) {
		log.info("REQUEST: POST /hello/contacts received for contact name: {}", contact.getName());// Log Entry Point
		// Validation Error Check
		if (bindingResult.hasErrors()) {
			// Get the specific validation error message
			FieldError fieldError = bindingResult.getFieldError();
			String validationMessage = fieldError != null ? fieldError.getDefaultMessage()
					: "Invalid contact data provided.";
			log.warn("VALIDATION FAILED for contact creation: {}", validationMessage); // Log Validation Failure
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));
			errorResponse.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
			errorResponse.put("error", "Bad Request");
			errorResponse.put("message", validationMessage);
			errorResponse.put("path", "/hello/contacts");

			// Return HTTP 400 Bad Request
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}

		// If validation passes, proceed to save and publish event.
		Contact createdContact = contactService.createContact(contact);
		log.info("SUCCESS: Contact created and event published. ID: {}", createdContact.getId()); // Log Success
		// Return 201 Created
		return new ResponseEntity<>(createdContact, HttpStatus.CREATED);
	}

	/**
	 * Handles GET requests to /hello/contacts. Retrieves contacts, excluding those
	 * whose name matches the provided regex filter. The filtering logic is
	 * performed in Java code (not SQL) using a streaming approach to handle
	 * potentially millions of records efficiently
	 * 
	 * @param nameFilter The regular expression pattern to exclude from contact
	 *                   names
	 * @return ResponseEntity containing: 200 OK and a JSON body { "contacts":
	 *         [...]} on success 400 Bad Request and a custom error message if
	 *         nameFilter is missing or invalid
	 */

	@GetMapping("/contacts")
	public ResponseEntity<?> getContacts(@RequestParam String nameFilter) {
		log.info("REQUEST: GET /hello/contacts received with nameFilter: {}", nameFilter); // Log Entry Point
		// Mandatory Parameter 'nameFilter' Check
		if (nameFilter == null || nameFilter.isBlank()) {
			log.warn("VALIDATION FAILED for GET /contacts: 'nameFilter' parameter is empty."); // Log Mandatory Check
																								// Failure
			Map<String, String> errorResponse = new HashMap<>();
			// Populate standard error response fields
			errorResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));
			errorResponse.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
			errorResponse.put("error", "Bad Request");
			errorResponse.put("message", "The 'nameFilter' parameter is mandatory and cannot be empty.");
			errorResponse.put("path", "/hello/contacts");
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

		}
		try {
			List<Contact> contacts = contactService.getContactsExcludingRegex(nameFilter);
			log.info("SUCCESS: Regex filtering complete. Returning {} contacts.", contacts.size()); // Log Success
			Map<String, Object> response = new HashMap<>();
			// Success Response Formatting
			response.put("contacts", contacts);
			return ResponseEntity.ok(response);
		} catch (PatternSyntaxException e) {
			// Invalid Regex Pattern Check (Custom JSON Error)
			// Catches the exception thrown by Pattern.compile() in the service layer.
			log.error("REGEX ERROR: Invalid pattern '{}' provided by user.", nameFilter, e); // Log Error with stack
																								// trace
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));
			errorResponse.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
			errorResponse.put("error", "Bad Request");
			errorResponse.put("message", "The provided nameFilter is an invalid regular expression: " + e.getMessage());
			errorResponse.put("path", "/hello/contacts");

			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}
	}
}
