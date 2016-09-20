package simple.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import simple.core.Constants;
import simple.core.model.DomainData;
import simple.core.model.Status;
import simple.core.model.SyncRequest;
import simple.core.service.BaseService;
import simple.core.service.SyncService;

/**
 * The Class SyncController.
 * 
 * @author Jeffrey
 */
@RestController
@RequestMapping(Constants.SYNC_API_PREFIX)
public class SyncController {

	@Autowired
	protected SyncService syncService;

	@Autowired
	protected BaseService baseService;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
	public String sync(@RequestBody String json) {
		DomainData domainData = new DomainData();
		try {
			SyncRequest syncRequest = this.baseService.getObjectFromJson(json,
					SyncRequest.class);
			domainData.setData(syncService.sync(syncRequest.getFunc(),
					syncRequest));
			domainData.setStatus(Status.STATUS_200);
		} catch (Exception e) {
			e.printStackTrace();
			domainData.setStatus(new Status(500, e.getMessage()));
		}
		return this.baseService.toJsonStr(domainData).replaceAll("id",
				"remoteId");
	}
}