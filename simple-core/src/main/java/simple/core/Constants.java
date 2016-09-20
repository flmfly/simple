package simple.core;

public interface Constants {

	public static final String SYNC_API_PREFIX = "/sync/api";
	public static final String FUNCTION_API_PREFIX = "/func/api";
	public static final String REST_API_PREFIX = "/rest/api";
	public static final String WEBSERVICE_API_PREFIX = "/ws/api";
	public static final String DOMAIN_VARIABLE_NAME = "domain";
	public static final String DOMAIN_PLACEHOLDER = "{" + DOMAIN_VARIABLE_NAME
			+ "}";
	public static final String SDOMAIN_VARIABLE_NAME = "sdomain";
	public static final String SDOMAIN_PLACEHOLDER = "{"
			+ SDOMAIN_VARIABLE_NAME + "}";
	public static final String DOMAIN_ROOT_DESCRIPTOR = "/"
			+ DOMAIN_PLACEHOLDER;
	public static final String ID_VARIABLE_NAME = "id";
	public static final String ID_PLACEHOLDER = "{" + ID_VARIABLE_NAME + "}";
	public static final String SID_VARIABLE_NAME = "sid";
	public static final String SID_PLACEHOLDER = "{" + SID_VARIABLE_NAME + "}";

	public static final String DOMAIN_SPECIFIC_DESCRIPTOR = DOMAIN_ROOT_DESCRIPTOR
			+ "/" + ID_PLACEHOLDER;

	public static final String PAGE_DESCRIPTOR = "page";
	public static final String DOMAIN_PAGE_DESCRIPTOR = DOMAIN_ROOT_DESCRIPTOR
			+ "/" + PAGE_DESCRIPTOR;

	public static final String DESC_DESCRIPTOR = "desc";
	public static final String REFERENCE_DESCRIPTOR = "ref";

	public static final String REFERENCE_PLACEHOLDER = "{"
			+ REFERENCE_DESCRIPTOR + "}";

	public static final String DOMAIN_DESCRIPTOR = "domain";
	public static final String FORM_DESCRIPTOR = "form";
	public static final String TABLE_DESCRIPTOR = "table";
	public static final String VALIDATION_DESCRIPTOR = "validation";
	public static final String VALIDATE_DESCRIPTOR = "validate";
	public static final String SEARCH_DESCRIPTOR = "search";
	public static final String TREE_DESCRIPTOR = "tree";

	public static final String DOMAIN_DESC_DESCRIPTOR = DOMAIN_ROOT_DESCRIPTOR
			+ "/" + DESC_DESCRIPTOR + "/" + DOMAIN_DESCRIPTOR;
	public static final String DOMAIN_FORM_DESCRIPTOR = DOMAIN_ROOT_DESCRIPTOR
			+ "/" + DESC_DESCRIPTOR + "/" + FORM_DESCRIPTOR;
	public static final String DOMAIN_TABLE_DESCRIPTOR = DOMAIN_ROOT_DESCRIPTOR
			+ "/" + DESC_DESCRIPTOR + "/" + TABLE_DESCRIPTOR;
	public static final String DOMAIN_VALIDATION_DESCRIPTOR = DOMAIN_ROOT_DESCRIPTOR
			+ "/" + DESC_DESCRIPTOR + "/" + VALIDATION_DESCRIPTOR;
	public static final String DOMAIN_VALIDATE_DESCRIPTOR = DOMAIN_ROOT_DESCRIPTOR
			+ "/" + VALIDATE_DESCRIPTOR;
	public static final String DOMAIN_SEARCH_DESCRIPTOR = DOMAIN_ROOT_DESCRIPTOR
			+ "/" + DESC_DESCRIPTOR + "/" + SEARCH_DESCRIPTOR;
	public static final String DOMAIN_REFERENCE_DESCRIPTOR = DOMAIN_ROOT_DESCRIPTOR
			+ "/" + REFERENCE_DESCRIPTOR + "/" + REFERENCE_PLACEHOLDER;

	public static final String DOMAIN_TREE_DESCRIPTOR = DOMAIN_ROOT_DESCRIPTOR
			+ "/" + TREE_DESCRIPTOR;

	public static final String SDOMAIN_DESCRIPTOR = DOMAIN_SPECIFIC_DESCRIPTOR
			+ "/" + SDOMAIN_PLACEHOLDER;

	public static final String ACCOUNT_SESSION_KEY = "_ACCOUNT_SESSION_KEY_";
	
}
