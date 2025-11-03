package com.assignment.contactsservice.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.assignment.contactsservice.model.Contact;
import com.assignment.contactsservice.service.ContactService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for the ContactController class. Uses @WebMvcTest to load only the
 * controller slice and MockMvc to simulate HTTP requests.
 * 
 * @MockBean is used to replace the ContactService dependency with a Mockito
 *           mock.
 */
@WebMvcTest(ContactController.class)
class ContactControllerTest {

	private static final Logger log = LoggerFactory.getLogger(ContactControllerTest.class);

	@Autowired
	private MockMvc mockMvc;

	@MockBean // Mocks the ContactService dependency, allowing us to control its behavior
	private ContactService contactService;

	@Autowired
	private ObjectMapper objectMapper; // Helper for converting Java objects to JSON strings

	private final String BASE_URL = "/hello/contacts";

	// POST /hello/contacts Tests (Creation Logic)

	/**
	 * Test case for successful contact creation. Verifies HTTP 201 Created and that
	 * the service's returned object is in the response body.
	 */
	@Test
	void testCreateContactSuccess() throws Exception {
		log.info("Running test: testCreateContactSuccess");
		// Arrange
		Contact requestContact = new Contact("New Contact"); // Input contact without ID
		Contact savedContact = new Contact(100L, "New Contact"); // Mock service result with ID

		// Mock the service call to simulate saving and returning the ID
		when(contactService.createContact(any(Contact.class))).thenReturn(savedContact);

		// Act & Assert
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestContact))) // Convert request object to JSON
				.andExpect(status().isCreated()) // Expect HTTP 201 CREATED
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id", is(100))) // Verify
																													// the
																													// returned
																													// ID
				.andExpect(jsonPath("$.name", is("New Contact")));

		// Ensure the service method was called once
		verify(contactService, times(1)).createContact(any(Contact.class));
		log.info("testCreateContactSuccess passed");
	}

	/**
	 * Test case for Bean Validation failure (e.g., @NotBlank violation on name
	 * field). Verifies HTTP 400 Bad Request and the custom validation error message
	 * from BindingResult.
	 */
	@Test
	void createContact_ValidationFailure() throws Exception {
		log.info("Running test: createContact_ValidationFailure");
		// Arrange
		Contact invalidContact = new Contact(null, ""); // Invalid: name is blank

		// Act & Assert
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidContact))).andExpect(status().isBadRequest()) // Expect
																												// HTTP
																												// 400
																												// Bad
																												// Request
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error", is("Bad Request")))
				// Verify the specific validation message (assumes message="Contact name cannot
				// be empty" in model)
				.andExpect(jsonPath("$.message", containsString("Contact name cannot be empty")))
				.andExpect(jsonPath("$.path", is("/hello/contacts")));

		// Service should never be called when validation fails
		verify(contactService, never()).createContact(any());
		log.info("createContact_ValidationFailure passed");
	}

	// GET /hello/contacts Tests (Filter Logic)

	/**
	 * Test case for successful contact retrieval and filtering. Verifies HTTP 200
	 * OK, the correct JSON content type, and the size/content of the list.
	 */
	@Test
	void testGetContactsSuccess() throws Exception {
		log.info("Running test: testGetContactsSuccess");
		// Arrange
		List<Contact> filteredList = Arrays.asList(new Contact(1L, "Jane"), new Contact(2L, "Jack"));
		String testFilter = "^J.*";

		// Mock the service call to return the expected list
		when(contactService.getContactsExcludingRegex(testFilter)).thenReturn(filteredList);

		// Act & Assert
		mockMvc.perform(get(BASE_URL).param("nameFilter", testFilter)).andExpect(status().isOk()) // Expect HTTP 200 OK
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.contacts", hasSize(2))) // Verify the top-level 'contacts' array size
				.andExpect(jsonPath("$.contacts[0].name", is("Jane"))); // Verify content of the first element

		// Ensure the service method was called exactly once with the correct parameter
		verify(contactService, times(1)).getContactsExcludingRegex(testFilter);
		log.info(" testGetContactsSuccess passed");
	}

	
	/**
	 * Test case for handling a PatternSyntaxException thrown by the service layer.
	 * Verifies HTTP 400 Bad Request and the custom JSON error format with the regex
	 * message.
	 */
	@Test
	void testGetContactsInvalidRegex() throws Exception {
		log.info("Running test: testGetContactsInvalidRegex");
		// Arrange
		String invalidRegex = "[";
		String exceptionMessage = "Dangling meta character '['";

		// Mock the service to throw the expected exception
		when(contactService.getContactsExcludingRegex(anyString()))
				.thenThrow(new PatternSyntaxException(exceptionMessage, invalidRegex, 0));

		// Act & Assert
		mockMvc.perform(get(BASE_URL).param("nameFilter", invalidRegex)).andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error", is("Bad Request"))).andExpect(jsonPath("$.status", is("400")))
				// Verify the custom error message contains the specific regex issue
				.andExpect(jsonPath("$.message", containsString("invalid regular expression: " + exceptionMessage)))
				.andExpect(jsonPath("$.path", is("/hello/contacts")));

		log.info(" testGetContactsInvalidRegex passed");
	}

	/**
     *  GET request with blank nameFilter should return HTTP 400
     * Ensures controller validation for missing mandatory parameter.
     */
    @Test
    void testGetContactsMissingNameFilter() throws Exception {
        log.info("Running test: testGetContactsMissingNameFilter");

        mockMvc.perform(get("/hello/contacts")
                .param("nameFilter", " ")) // blank filter
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("The 'nameFilter' parameter is mandatory and cannot be empty."));

        log.info(" testGetContactsMissingNameFilter passed");
    }

}
