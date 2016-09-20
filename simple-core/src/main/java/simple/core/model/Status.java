package simple.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 200 OK - Response to a successful GET, PUT, PATCH or DELETE. Can also be used
 * for a POST that doesn't result in a creation.<br>
 * 201 Created - Response to a POST that results in a creation. Should be
 * combined with a Location header pointing to the location of the new resource<br>
 * 204 No Content - Response to a successful request that won't be returning a
 * body (like a DELETE request)<br>
 * 304 Not Modified - Used when HTTP caching headers are in play<br>
 * 400 Bad Request - The request is malformed, such as if the body does not
 * parse<br>
 * 401 Unauthorized - When no or invalid authentication details are provided.
 * Also useful to trigger an auth popup if the API is used from a browser<br>
 * 403 Forbidden - When authentication succeeded but authenticated user doesn't
 * have access to the resource<br>
 * 404 Not Found - When a non-existent resource is requested<br>
 * 405 Method Not Allowed - When an HTTP method is being requested that isn't
 * allowed for the authenticated user<br>
 * 410 Gone - Indicates that the resource at this end point is no longer
 * available. Useful as a blanket response for old API versions<br>
 * 415 Unsupported Media Type - If incorrect content type was provided as part
 * of the request<br>
 * 422 Unprocessable Entity - Used for validation errors<br>
 * 429 Too Many Requests - When a request is rejected due to rate limiting<br>
 * 
 * @author Jeffrey
 *
 */
public class Status {

	public static final Status STATUS_200 = new Status(200, Status.STATUS_OK);

	public static final Status STATUS_100 = new Status(100, "验证未通过");

	public static final Status STATUS_ENTITY_NOT_FOUND = new Status(404,
			"entity not found");

	public static final String STATUS_OK = "OK";

	private int code;
	private List<String> messages;

	public Status(int code, String message) {
		super();
		this.code = code;
		this.messages = new ArrayList<String>();
		this.messages.add(message);
	}

	public Status(int code, List<String> messages) {
		super();
		this.code = code;
		this.messages = messages;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

}
