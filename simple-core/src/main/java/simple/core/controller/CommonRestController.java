package simple.core.controller;

//import java.util.Set;
//
//import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import simple.config.annotation.support.OperationHandler.OperationException;
import simple.core.Constants;
import simple.core.model.DomainData;
import simple.core.model.ResponseObject;
import simple.core.model.Status;
import simple.core.service.BaseService;
import simple.core.service.HttpSessionService;
import simple.core.service.MessageService;

/**
 * The Class CommonRestController.
 * 
 * @author Jeffrey
 */
@RestController
@RequestMapping(Constants.REST_API_PREFIX)
public class CommonRestController implements Constants {

	/** The base service. */
	@Autowired
	protected BaseService baseService;

	@Autowired
	private HttpSessionService httpSessionService;

	@Autowired
	private MessageService messageService;

	/** The Constant logger. */
	static final Logger logger = Logger.getLogger(CommonRestController.class);

	@RequestMapping(value = Constants.DOMAIN_ROOT_DESCRIPTOR, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String addDomain(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@RequestBody String json, HttpServletRequest request) {
		DomainData domainData = new DomainData();
		try {
			this.httpSessionService.setSession(request.getSession());
			Object domain = this.baseService.getDomainFromPostJosn(json,
					domainName);
			Collection<String> errorList = this.baseService.insertDomain(
					domainName, domain, request);

			if (!errorList.isEmpty()) {
				domainData.setInfo(new ArrayList<String>(errorList));
				domainData.setStatus(Status.STATUS_100);
			} else {
				domainData.setData(domain);
				domainData.setStatus(Status.STATUS_200);
			}
		} catch (Exception e) {
			e.printStackTrace();
			domainData.setStatus(new Status(500, e.getMessage()));
		}
		return this.baseService.toJsonStr(domainData);
	}

	@RequestMapping(value = Constants.DOMAIN_SPECIFIC_DESCRIPTOR, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String updateDomain(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@PathVariable("id") long id, @RequestBody String json,
			HttpServletRequest request) {
		this.httpSessionService.setSession(request.getSession());
		DomainData domainData = new DomainData();
		try {
			Object domain = this.baseService.getDomainFromPostJosn(json,
					domainName);
			baseService.autoFillDomain(domainName, domain, request);

			List<String> errorList = this.baseService.validate(domainName,
					domain);

			if (!errorList.isEmpty()) {
				domainData.setInfo(errorList);
				domainData.setStatus(Status.STATUS_100);
			} else {
				this.baseService.update(domain, true);
				domainData.setData(domain);
				domainData.setStatus(Status.STATUS_200);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			domainData.setStatus(new Status(500, e.getMessage()));
		}

		return this.baseService.toJsonStr(domainData);
	}

	@RequestMapping(value = Constants.DOMAIN_SPECIFIC_DESCRIPTOR, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getDomain(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@PathVariable("id") long id) {
		DomainData data = new DomainData();

		Class<?> clazz = this.baseService.getDomainClass(domainName);
		if (null == clazz) {
			data.setStatus(Status.STATUS_ENTITY_NOT_FOUND);
		} else {
			data.setData(this.baseService.get(clazz, id));
			data.setStatus(Status.STATUS_200);
		}
		return this.baseService.toJsonStr(data);
	}

	@RequestMapping(value = Constants.DOMAIN_SPECIFIC_DESCRIPTOR, method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
	public String deleteDomain(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@PathVariable("id") long id) {
		ResponseObject ro = new ResponseObject();
		try {
			this.baseService.delete(
					this.baseService.getDomainClass(domainName), id);
			ro.setStatus(Status.STATUS_200);
		} catch (IllegalArgumentException e) {
			ro.setStatus(new Status(0, "对象不存在"));
			ro.addInfo(e.getMessage());
		} catch (Exception e) {
			Throwable ex = e;
			String msg = null;
			while (ex.getCause() != null && !ex.getCause().equals(ex)) {
				if (ex instanceof ConstraintViolationException) {
					msg = "数据存在引用约束，不能删除！";
				}
				ex = ex.getCause();
			}
			if (StringUtils.isEmpty(msg)) {
				msg = e.getMessage();
			}
			ro.setStatus(new Status(0, msg));
			ro.addInfo(msg);
		}
		return this.baseService.toJsonStr(ro);
	}

	@RequestMapping(value = Constants.DOMAIN_ROOT_DESCRIPTOR + "/delete", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String deleteDomain(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@RequestBody String json) {
		try {
			this.baseService.deleteAll(
					this.baseService.getDomainClass(domainName), json);
			return this.baseService.toJsonStr(Status.STATUS_200);
		} catch (IllegalArgumentException e) {
			return this.baseService.toJsonStr(new Status(0, "对象不存在"));
		} catch (Exception e) {
			return this.baseService.toJsonStr(new Status(0, e.getMessage()));
		}
	}

	@RequestMapping(value = Constants.DOMAIN_ROOT_DESCRIPTOR
			+ "/operation/{operationCode}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String operateDomain(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@PathVariable("operationCode") String operationCode,
			@RequestBody String json, HttpServletRequest request) {
		this.httpSessionService.setSession(request.getSession());
		try {
			List<String> messages = this.baseService.operate(domainName,
					operationCode, json);

			if (messages.isEmpty()) {
				return this.baseService.toJsonStr(Status.STATUS_200);
			} else {
				return this.baseService.toJsonStr(new Status(0, messages));
			}
		} catch (OperationException e) {
			return this.baseService.toJsonStr(new Status(0, e.getMessages()));
		} catch (Exception e) {
			return this.baseService.toJsonStr(new Status(0, e.getMessage()));
		}
	}

	@RequestMapping(value = Constants.DOMAIN_PAGE_DESCRIPTOR, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String getPage(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@RequestParam int pageNumber, @RequestParam int pageSize,
			@RequestParam String sort, @RequestBody String json,
			HttpServletRequest request) {
		this.httpSessionService.setSession(request.getSession());
		return this.baseService.toJsonStr(this.baseService.getPage(domainName,
				pageNumber, pageSize, sort, json,
				this.httpSessionService.getLoginUser()));
	}

	@RequestMapping(value = Constants.DOMAIN_FORM_DESCRIPTOR, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getFormDescription(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			HttpServletRequest request) {
		return this.baseService.toJsonStr(this.baseService.getDomainFormDesc(
				domainName, request));
	}

	@RequestMapping(value = Constants.DOMAIN_DESC_DESCRIPTOR, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getDomainDescription(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			HttpServletRequest request) {
		return this.baseService.toJsonStr(this.baseService
				.getDomainDesc(domainName));
	}

	@RequestMapping(value = Constants.DOMAIN_SEARCH_DESCRIPTOR, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getSearchDescription(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName) {
		return this.baseService.toJsonStr(this.baseService
				.getDomainSearchDesc(domainName));
	}

	@RequestMapping(value = Constants.DOMAIN_TABLE_DESCRIPTOR, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getTableDescription(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName) {
		return this.baseService.toJsonStr(this.baseService
				.getDomainTableDesc(domainName));
	}

	@RequestMapping(value = Constants.DOMAIN_VALIDATION_DESCRIPTOR, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getValidationDescription(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName) {
		return this.baseService.toJsonStr(this.baseService
				.getDomainFormDesc(domainName));
	}

	@RequestMapping(value = Constants.DOMAIN_REFERENCE_DESCRIPTOR, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getReference(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@PathVariable(Constants.REFERENCE_DESCRIPTOR) String ref,
			HttpServletRequest request) {
		this.httpSessionService.setSession(request.getSession());
		return this.baseService.toJsonStr(this.baseService.getReference(
				domainName, ref));
	}

	@RequestMapping(value = Constants.DOMAIN_TREE_DESCRIPTOR, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getTree(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName) {
		return this.baseService.toJsonStr(this.baseService.getTree(domainName,
				null, null));
	}

	@RequestMapping(value = Constants.DOMAIN_TREE_DESCRIPTOR
			+ "/{propertyName}/{id}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getSubTree(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@PathVariable("propertyName") String propertyName,
			@PathVariable("id") String pid, HttpServletRequest request) {
		this.httpSessionService.setSession(request.getSession());
		return this.baseService.toJsonStr(this.baseService.getTree(domainName,
				propertyName, pid));
	}

	@RequestMapping(value = Constants.DOMAIN_SPECIFIC_DESCRIPTOR
			+ "/{propertyName}/tree", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getSelectTree(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@PathVariable("propertyName") String propertyName,
			@PathVariable("id") long id) {
		return this.baseService.toJsonStr(this.baseService.getSelectTree(
				domainName, id, propertyName));
	}

	@RequestMapping(value = Constants.DOMAIN_SPECIFIC_DESCRIPTOR + "/{refName}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public String getSubList(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@PathVariable("refName") String refName, @PathVariable("id") long id) {
		return this.baseService.toJsonStr(this.baseService.getSubList(
				domainName, id, refName));
	}

	@RequestMapping(value = Constants.DOMAIN_VALIDATE_DESCRIPTOR, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String validate(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@RequestBody String json) {
		DomainData domainData = new DomainData();
		try {
			Object domain = this.baseService.getDomainFromPostJosn(json,
					domainName);

			List<String> errorList = this.baseService.validate(domainName,
					domain);

			if (!errorList.isEmpty()) {
				domainData.setInfo(errorList);
				domainData.setStatus(Status.STATUS_100);
			} else {
				domainData.setStatus(Status.STATUS_200);
			}
		} catch (Exception e) {
			e.printStackTrace();
			domainData.setStatus(new Status(500, e.getMessage()));
		}
		return this.baseService.toJsonStr(domainData);
	}

	@RequestMapping(value = Constants.DOMAIN_ROOT_DESCRIPTOR
			+ "/changed/{fieldName}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String fieldChanged(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@PathVariable("fieldName") String fieldName,
			@RequestBody String json, HttpServletRequest request) {
		DomainData domainData = new DomainData();
		this.httpSessionService.setSession(request.getSession());
		try {
			Object domain = this.baseService.getDomainFromPostJosn(json,
					domainName);
			this.baseService.processFieldValueChanged(domainName, fieldName,
					domain);
			domainData.setData(domain);
			domainData.setStatus(Status.STATUS_200);
		} catch (Exception e) {
			domainData.setStatus(new Status(500, e.getMessage()));
		}
		return this.baseService.toJsonStr(domainData);
	}

	@RequestMapping(value = Constants.DOMAIN_ROOT_DESCRIPTOR + "/spec", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String special(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@RequestBody String json) {
		DomainData domainData = new DomainData();
		try {
			domainData.setData(this.baseService
					.processSpecial(domainName, json));
			domainData.setStatus(Status.STATUS_200);
		} catch (Exception e) {
			domainData.setStatus(new Status(500, e.getMessage()));
		}
		return this.baseService.toJsonStr(domainData);
	}

	@RequestMapping(value = Constants.DOMAIN_ROOT_DESCRIPTOR + "/getmsg", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String message(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@RequestBody String json) {
		DomainData domainData = new DomainData();
		try {
			domainData.setData(this.baseService
					.operateMessage(domainName, json));
			domainData.setStatus(Status.STATUS_200);
		} catch (Exception e) {
			e.printStackTrace();
			domainData.setStatus(new Status(500, e.getMessage()));
		}
		return this.baseService.toJsonStr(domainData);
	}

	public String supportURI() {
		return Constants.REST_API_PREFIX + Constants.DOMAIN_ROOT_DESCRIPTOR
				+ "\n" + Constants.REST_API_PREFIX
				+ Constants.DOMAIN_SPECIFIC_DESCRIPTOR + "\n"
				+ Constants.REST_API_PREFIX + Constants.DOMAIN_PAGE_DESCRIPTOR
				+ "\n" + Constants.REST_API_PREFIX
				+ Constants.DOMAIN_FORM_DESCRIPTOR + "\n"
				+ Constants.REST_API_PREFIX + Constants.DOMAIN_TABLE_DESCRIPTOR
				+ "\n" + Constants.REST_API_PREFIX
				+ Constants.DOMAIN_VALIDATION_DESCRIPTOR + "\n"
				+ Constants.REST_API_PREFIX
				+ Constants.DOMAIN_SEARCH_DESCRIPTOR + "\n";
	}

	@RequestMapping(value = Constants.DOMAIN_ROOT_DESCRIPTOR + "/msg", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public @ResponseBody String sendMessage(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@RequestBody String json) {
		DomainData domainData = new DomainData();
		try {
			domainData.setData(this.messageService.operateSMS(domainName,
					this.baseService.getObjectFromJson(json,
							MessageService.Request.class)));
			domainData.setStatus(Status.STATUS_200);
		} catch (Exception e) {
			domainData.setStatus(new Status(500, e.getMessage()));
		}
		return this.baseService.toJsonStr(domainData);
	}

	public static void main(String[] args) {
		System.out.println(new CommonRestController().supportURI());
	}

	// private void cleanCache(HttpServletResponse response) {
	// response.addHeader("Cache-Control",
	// "no-cache, no-store, must-revalidate");
	// response.addHeader("Pragma", "no-cache");
	// response.addHeader("Expires", "0");
	// }

}