package simple.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import simple.config.annotation.Reference;
import simple.core.dao.HibernateBaseDAO;
import simple.core.model.FormField;
import simple.core.model.Option;
import simple.core.model.RepresentationType;

@Service
final class DataInitializeService {
	@Autowired
	private HibernateBaseDAO hibernateBaseDAO;

	@Autowired
	private AnnotationService annotionService;

	@Autowired
	@Qualifier("transactionManager")
	private PlatformTransactionManager txManager;
	private Map<String, Map<String, Set<FormField>>> dictFieldMap = new HashMap<String, Map<String, Set<FormField>>>();

	@PostConstruct
	void fillDictOptions() {
		TransactionTemplate tmpl = new TransactionTemplate(txManager);
		tmpl.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				Map<String, Map<String, List<Option>>> dictOptionMap = new HashMap<String, Map<String, List<Option>>>();
				List<String> propertyList = new ArrayList<String>();
				propertyList.add("id");
				propertyList.add("name");

				Map<String, List<FormField>> formMap = annotionService
						.getFormMap();

				for (Iterator<String> iterator = formMap.keySet().iterator(); iterator
						.hasNext();) {
					String domainName = iterator.next();
					List<FormField> fields = formMap.get(domainName);

					for (FormField field : fields) {
						String dictCode = field.getType().getDict();
						if (null != dictCode
								&& (RepresentationType.SELECT.equals(field
										.getType().getView()) || RepresentationType.RADIO
										.equals(field.getType().getView()))) {
							String filter = "";
							Reference ref = annotionService.getReferences(
									domainName, field.getName());
							if (null != ref) {
								filter = ref.filter();
							}
							if (!StringUtils.isBlank(filter)) {
								filter = " and " + filter;
							}

							if (dictOptionMap.containsKey(dictCode)
									&& dictOptionMap.get(dictCode).containsKey(
											filter)) {
								field.getType()
										.setOptions(
												dictOptionMap.get(dictCode)
														.get(filter));
							} else {

								Map<String, List<Option>> optionsMap = dictOptionMap
										.get(dictCode);

								if (optionsMap == null) {
									optionsMap = new LinkedHashMap<String, List<Option>>();
									dictOptionMap.put(dictCode, optionsMap);
								}
								@SuppressWarnings("unchecked")
								List<Object[]> entityList = (List<Object[]>) hibernateBaseDAO
										.findByProperties(
												annotionService.getDictClass(),
												propertyList, "dict.code='"
														+ dictCode + "' "
														+ filter);
								for (Object[] entity : entityList) {
									Option option = new Option(entity[0],
											String.valueOf(entity[1]));
									field.getType().addOption(option);
								}
								optionsMap.put(filter, field.getType()
										.getOptions());
							}

							Map<String, Set<FormField>> fieldsMap = dictFieldMap
									.get(dictCode);
							if (null == fieldsMap) {
								fieldsMap = new LinkedHashMap<String, Set<FormField>>();
								dictFieldMap.put(dictCode, fieldsMap);
							}
							Set<FormField> fieldList = fieldsMap.get(filter);
							if (null == fieldList) {
								fieldList = new HashSet<FormField>();
								fieldsMap.put(filter, fieldList);
							}
							fieldList.add(field);
						}
					}
				}
			}
		});
	}

	void applyDictChange(String dictCode) {
		Map<String, Set<FormField>> fieldsMap = dictFieldMap.get(dictCode);
		if(fieldsMap == null){
			return;
		}
		for (Entry<String, Set<FormField>> e : fieldsMap.entrySet()) {
			String filter = e.getKey();
			Set<FormField> fieldList = e.getValue();
			if (null != fieldList) {
				List<String> propertyList = new ArrayList<String>();
				propertyList.add("id");
				propertyList.add("name");
				@SuppressWarnings("unchecked")
				List<Object[]> entityList = (List<Object[]>) this.hibernateBaseDAO
						.findByProperties(this.annotionService.getDictClass(),
								propertyList, "dict.code='" + dictCode + "' "
										+ filter);
				List<Option> optionList = new ArrayList<Option>();
				for (Object[] entity : entityList) {
					Option option = new Option(entity[0],
							String.valueOf(entity[1]));
					optionList.add(option);
				}
				for (FormField formField : fieldList) {
					formField.getType().setOptions(optionList);
				}
			}
		}
	}
}
