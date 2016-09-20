package simple.core.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import simple.core.Constants;
import simple.core.model.Status;
import simple.core.model.webservice.Response;
import simple.core.service.FileService;
import simple.core.service.HttpSessionService;
import simple.core.service.WebserviceService;

/**
 * The Class MobileController.
 * 
 * @author Jeffrey
 */
@RestController
@RequestMapping(Constants.WEBSERVICE_API_PREFIX)
public class WebserviceController implements Constants {

	@Autowired
	private HttpSessionService httpSessionService;

	/** The menu service. */
	@Autowired
	private WebserviceService webserviceService;

	@Autowired
	private FileService fileService;

	/** The Constant logger. */
	static final Logger logger = Logger.getLogger(WebserviceController.class);

	@RequestMapping(value = "/write", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String write(@RequestBody String json, HttpServletRequest request) {
		Response response = new Response(
				this.webserviceService.getRequest(json));
		try {
			httpSessionService.setSession(request.getSession());
			response = this.webserviceService.updateWriteRequest(response,
					request);
			if (null == response.getValidation()) {
				response.setStatus(Status.STATUS_200);
			} else {
				response.setStatus(Status.STATUS_100);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(new Status(500, e.getMessage()));
		}
		return this.webserviceService.toJson(response.getFunc(), response);
	}

	/*
	 * 查询协议说明
	 * {
	 * 	func:"domainName：不能为空，要查询的domain名称",
	 * 	query:{},//件见查询条说明
	 * 	page:{num:"页码：不能为空，-1为不分页",size:"每页显示条数：不能为空",sort:"排序字段：可以为空，-xxx为按照xxx字段降序，多个排序用逗号隔开"}
	 * }
	 * 
	 * ****
	 * 查询条件
	 * v1.0 
	 * 按照domain的层次结构传递查询条件，只支持=的精准查询条件
	 * 如：
	 * 查询用户名称等于“test”的用户信息
	 * {func:"baseuser",
	 * 	query:{name:"test"},
	 * 	page:{num:-1,size:10}
	 * }
	 * 
	 * 
	 * 查询员工名称为“test”用户信息
	 * {func:"baseuser",
	 * 	query:{employee:{name:"test"}},
	 * 	page:{num:-1,size:10}
	 * }
	 * 
	 * 查询职位为“操作司机”，类型为“承运商”的所有用户信息
	 * {func:"baseuser",
	 * 	query:{employee:{position:{name:"操作司机"}},type:{name:"承运商"}},
	 * 	page:{num:-1,size:10}
	 * }
	 * 
	 * ****
	 * v2.0
	 * 兼容v1.0的查询格式 ，同时v2.0对查询进行了扩展，支持多种查询条件同时查询条件的格式有所不同,但默认为and连接，设置 or=true时按照的or连接查询
	 * 
	 * 条件的基本格式为
	 * {"字段(支持点的多级属性，不能为空)":{"查询的方式(不能为空)":"值","fuzzy(是否模糊查询，缺省为false，只对字符串生效)":"true/false","or(是否为or连接，缺省为false)":"true/false"}}
	 * 
	 * 查询方式						 操作符
	 * val							  =
	 * neval						  ！=
	 * lval							  >
	 * rval							  <
	 * inval						  in
	 * notinval						  not in
	 * isnullval(true/false)		is null/is not null
	 * ----
	 * fuzzy(true)				 可以和val,neval组合进行模糊查询
	 * fuzzy(true)+val				like '%value%'
	 * fuzzy(true)+neval			not	like '%value%'
	 * ----
	 * or(false)						按照and连接查询（默认查询方式）
	 * or(true)							按照or连接查询
	 * 如：
	 * 查询用户名称等于“test”的用户信息
	 * {func:"baseuser",
	 * 	query:{name:{val:"test"}},
	 * 	page:{num:-1,size:10}
	 * }
	 * 
	 * 查询用户名称含有“test”的所有用户信息
	 * {func:"baseuser",
	 * 	query:{name:{val:"test",fuzzy:true}},
	 * 	page:{num:-1,size:10}
	 * }
	 * 
	 * 
	 * 查询员工名称为“test”用户信息
	 * {func:"baseuser",
	 * 	query:{"employee.name":{val:"test"}},
	 * 	page:{num:-1,size:10}
	 * }
	 * 
	 *查询员工名称为“test”和"test1"用户信息
	 * {func:"baseuser",
	 * 	query:{"employee.name":{inval:["test","test1"]}},
	 * 	page:{num:-1,size:10}
	 * }
	 * 
	 * 查询职位为“操作司机”，类型为“承运商”的所有用户信息
	 * {func:"baseuser",
	 * 	query:{"employee.position.name":{val:"操作司机"},"type.name":{val:"承运商"}},
	 * 	page:{num:-1,size:10}
	 * } 
	 */
	@RequestMapping(value = "/read", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String read(@RequestBody String json, HttpServletRequest request) {
		httpSessionService.setSession(request.getSession());
		Response response = new Response(
				this.webserviceService.getRequest(json));
		try {
			response = this.webserviceService.processReadRequest(response);
			response.setStatus(Status.STATUS_200);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(new Status(500, e.getMessage()));
		}
		return this.webserviceService.toJson(response.getFunc(), response);
	}

}