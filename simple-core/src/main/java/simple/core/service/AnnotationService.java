package simple.core.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.persistence.Enumerated;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import simple.config.annotation.AssociateTableColumn;
import simple.config.annotation.Attachment;
import simple.config.annotation.AutoFill;
import simple.config.annotation.BooleanValue;
import simple.config.annotation.BusinessKey;
import simple.config.annotation.DataFilter;
import simple.config.annotation.DefaultValue;
import simple.config.annotation.DictField;
import simple.config.annotation.Domain;
import simple.config.annotation.ExtentionPoint;
import simple.config.annotation.FormGroup;
import simple.config.annotation.ImageGalleryTableColumn;
import simple.config.annotation.ImportIgnore;
import simple.config.annotation.Operation;
import simple.config.annotation.OperationParameter;
import simple.config.annotation.Reference;
import simple.config.annotation.ReferenceValue;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.RepresentationLayout;
import simple.config.annotation.SMSSupport;
import simple.config.annotation.SearchField;
import simple.config.annotation.Serializer;
import simple.config.annotation.Special;
import simple.config.annotation.StandardOperation;
import simple.config.annotation.SubImport;
import simple.config.annotation.Synchronization;
import simple.config.annotation.TabView;
import simple.config.annotation.TabViewType;
import simple.config.annotation.TableColumn;
import simple.config.annotation.Title;
import simple.config.annotation.UpdateImport;
import simple.config.annotation.map.Latitude;
import simple.config.annotation.map.Longitude;
import simple.config.annotation.map.MapAddress;
import simple.config.annotation.map.MapCity;
import simple.config.annotation.map.MapLabel;
import simple.core.annotation.DataFilterAnnoatationHandler;
import simple.core.annotation.OperationAnnoatationHandler;
import simple.core.annotation.StandarOperationAnnoatationHandler;
import simple.core.model.AttachmentDesc;
import simple.core.model.DomainDesc;
import simple.core.model.FieldValidation;
import simple.core.model.FormField;
import simple.core.model.FormGroupDesc;
import simple.core.model.ImageGalleryTableColumnDesc;
import simple.core.model.OneToMany;
import simple.core.model.Option;
import simple.core.model.RepresentationType;
import simple.core.model.TableColumnDesc;
import simple.core.model.UpdateImportDesc;
import simple.core.model.annotation.OperationDesc;
import simple.core.model.annotation.SearchFieldDesc;
import simple.core.model.annotation.StandarOperationDesc;
import simple.core.util.GenericUtils;
import simple.core.util.GsonBuilderUtil;
import simple.core.util.ReflectUtils;

import com.google.gson.Gson;

class AnnotationService {

	private String dictClassName = "simple.base.model.BaseDictItem";

	private static Map<String, Map<String, DefaultValue>> defaultValueMap = new HashMap<String, Map<String, DefaultValue>>();

	private static Map<String, List<FormField>> formMap = new HashMap<String, List<FormField>>();

	// private static Map<String, List<FormField>> searchMap = new
	// HashMap<String, List<FormField>>();

	private static Map<String, List<SearchFieldDesc>> searchMap = new HashMap<String, List<SearchFieldDesc>>();

	private static Map<String, List<TableColumnDesc>> tableMap = new HashMap<String, List<TableColumnDesc>>();

	private static Map<String, Class<?>> clazzMap = new HashMap<String, Class<?>>();

	private static Map<String, String> clazzNameMap = new HashMap<String, String>();

	private static Map<String, Map<Field, AutoFill>> autoFillMap = new HashMap<String, Map<Field, AutoFill>>();

	private static Map<String, Map<String, Reference>> referenceMap = new HashMap<String, Map<String, Reference>>();

	private static Map<String, RepresentationLayout> representationLayoutMap = new HashMap<String, RepresentationLayout>();

	private static Map<String, List<OneToMany>> oneToManyMap = new HashMap<String, List<OneToMany>>();

	private static Map<String, ExtentionPoint> extentionPointMap = new HashMap<String, ExtentionPoint>();

	private static Map<String, String> serializerMap = new HashMap<String, String>();

	private static Map<String, DomainDesc> domainDescMap = new HashMap<String, DomainDesc>();

	private static Map<String, String> tagsInsertMap = new HashMap<String, String>();

	private static Map<String, List<String>> businessKeyMap = new HashMap<String, List<String>>();

	private static Map<String, String> domainFilterHandlerMap = new HashMap<String, String>();

	private static Map<String, Special> domainSpecialMap = new HashMap<String, Special>();

	private static Map<String, SMSSupport> domainSMSSupportMap = new HashMap<String, SMSSupport>();

	private static Map<String, Map<String, String>> changedListenerMap = new HashMap<String, Map<String, String>>();

	private static Map<String, List<Synchronization>> synchronizationMap = new HashMap<String, List<Synchronization>>();

	public List<Synchronization> getSynchronizationMap(String domainName) {
		return synchronizationMap.get(domainName);
	}

	public List<String> getBusinessKey(String domainName) {
		return businessKeyMap.get(domainName);
	}

	public String getTagsInsert(String domainName) {
		return tagsInsertMap.get(domainName);
	}

	Collection<List<FormField>> getAllFormFieldSet() {
		return formMap.values();
	}

	Map<String, List<FormField>> getFormMap() {
		return formMap;
	}

	public boolean hasCustomOperation(String doaminName) {
		return null != domainDescMap.get(doaminName).getOperation()
				&& domainDescMap.get(doaminName).getOperation().size() > 0;
	}

	public ExtentionPoint getExtentionPoint(String domainName) {
		return extentionPointMap.get(domainName);
	}

	public boolean hasDomain(String domainName) {
		return domainDescMap.containsKey(domainName);
	}

	public Map<String, DefaultValue> getDefaultValues(String domainName) {
		return defaultValueMap.get(domainName);
	}

	public void setDictClassName(String dictClassName) {
		this.dictClassName = dictClassName;
	}

	public List<OneToMany> getOneToMany(String domainName) {
		return oneToManyMap.get(domainName);
	}

	public List<FormField> getDomainFormDesc(String domainName) {
		// List<FormField> fieldList = formMap.get(domainName);
		// List<FormField> clonedFieldList = new ArrayList<FormField>(
		// fieldList.size());
		//
		// for (FormField formField : fieldList) {
		// clonedFieldList.add(formField.cloneMe());
		// }
		//
		// return clonedFieldList;
		return formMap.get(domainName);
	}

	public List<SearchFieldDesc> getDomainSearchDesc(String domainName) {
		List<SearchFieldDesc> fieldList = searchMap.get(domainName);
		if (null == fieldList) {
			return Collections.emptyList();
		}
		return fieldList;
	}

	public List<TableColumnDesc> getDomainTableDesc(String domainName) {
		return tableMap.get(domainName);
	}

	public Map<Field, AutoFill> getAutoFill(String domainName) {
		return autoFillMap.get(domainName);
	}

	public Class<?> getDomainClass(String domainName) {
		return clazzMap.get(domainName);
	}

	public String getDomainNameByClass(Class<?> clazz) {
		return clazzNameMap.get(clazz.getName());
	}

	public Reference getReferences(String domainName, String refName) {
		if (null != referenceMap.get(domainName)) {
			return referenceMap.get(domainName).get(refName);
		}
		return null;
	}

	public RepresentationLayout getRepresentationLayout(String domainName) {
		return representationLayoutMap.get(domainName);
	}

	public Class<?> getDictClass() {
		try {
			return Class.forName(this.dictClassName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getDomainFilterHandler(String domainName) {
		return domainFilterHandlerMap.get(domainName);
	}

	public Special getSpecial(String domainName) {
		return domainSpecialMap.get(domainName);
	}

	public SMSSupport getSMSSupport(String domainName) {
		return domainSMSSupportMap.get(domainName);
	}

	public String getOnChangedListenerHandler(String domainName,
			String fieldName) {
		if (null != changedListenerMap.get(domainName)) {
			return changedListenerMap.get(domainName).get(fieldName);
		}
		return null;
	}

	public DomainDesc getDomainDesc(String domainName) {
		return domainDescMap.get(domainName);
	}

	public AnnotationService(String packageName) throws ClassNotFoundException {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
				false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(Domain.class));
		StandarOperationAnnoatationHandler soahandler = new StandarOperationAnnoatationHandler();
		OperationAnnoatationHandler oah = new OperationAnnoatationHandler();
		DataFilterAnnoatationHandler dfah = new DataFilterAnnoatationHandler();

		StringTokenizer st1 = new StringTokenizer(packageName, ",");
		while (st1.hasMoreTokens()) {
			for (BeanDefinition bd : scanner.findCandidateComponents(st1
					.nextToken())) {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				String domainName = clazz.getSimpleName().toLowerCase();
				Domain domain = clazz.getAnnotation(Domain.class);
				clazzMap.put(domainName, clazz);
				clazzNameMap.put(clazz.getName(), domainName);
				DomainDesc dd = new DomainDesc();
				dd.setLabel(domain.value());
				dd.setBatch(domain.batch());
				if (!"".equals(domain.defaultSort())) {
					dd.setDefaultSort(domain.defaultSort());
				}
				if (!"".equals(domain.defaultFilterHandler())) {
					domainFilterHandlerMap.put(domainName,
							domain.defaultFilterHandler());
				}
				UpdateImport ui = clazz.getAnnotation(UpdateImport.class);
				if (null != ui) {
					StringTokenizer st = new StringTokenizer(ui.by(), ",");
					List<UpdateImportDesc> updateImport = new ArrayList<UpdateImportDesc>();
					while (st.hasMoreTokens()) {
						UpdateImportDesc uid = new UpdateImportDesc();
						uid.setBy(st.nextToken());
						updateImport.add(uid);
					}
					dd.setUpdateImport(updateImport);
				}

				Special special = clazz.getAnnotation(Special.class);
				if (null != special) {
					domainSpecialMap.put(domainName, special);
				}

				SMSSupport smsSupport = clazz.getAnnotation(SMSSupport.class);
				if (null != smsSupport) {
					domainSMSSupportMap.put(domainName, smsSupport);
				}

				StandarOperationDesc sod = new StandarOperationDesc();

				StandardOperation so = clazz
						.getAnnotation(StandardOperation.class);
				if (null != so) {
					sod = soahandler.handle(so);
				}

				dd.setStandarOperation(sod);

				// operations
				List<OperationDesc> list = new ArrayList<OperationDesc>();
				Operation operation = clazz.getAnnotation(Operation.class);
				if (null != operation) {
					list.add(oah.handle(operation));
				}

				Operation.List operationList = clazz
						.getAnnotation(Operation.List.class);
				if (null != operationList) {
					for (Operation o : operationList.value()) {
						list.add(oah.handle(o));
					}
				}
				dd.setOperation(list);

				// sync
				List<Synchronization> syncList = new ArrayList<Synchronization>();
				Synchronization synchronization = clazz
						.getAnnotation(Synchronization.class);
				if (null != synchronization) {
					syncList.add(synchronization);
				}

				Synchronization.List synchronizationList = clazz
						.getAnnotation(Synchronization.List.class);
				if (null != synchronizationList) {
					for (Synchronization o : synchronizationList.value()) {
						syncList.add(o);
					}
				}
				if (!syncList.isEmpty()) {
					synchronizationMap.put(domainName, syncList);
				}

				// data filter
				DataFilter df = clazz.getAnnotation(DataFilter.class);
				if (null != df) {
					dd.setDataFilter(dfah.handle(df));
				}

				domainDescMap.put(domainName, dd);

				ExtentionPoint ep = clazz.getAnnotation(ExtentionPoint.class);
				extentionPointMap.put(domainName, ep);

				Serializer serializer = clazz.getAnnotation(Serializer.class);
				if (null != serializer && !"".equals(serializer.value())) {
					serializerMap.put(domainName, serializer.value());
				}

				BusinessKey bk = clazz.getAnnotation(BusinessKey.class);
				if (null != bk) {
					List<String> fieldList = new ArrayList<String>();
					StringTokenizer st = new StringTokenizer(bk.value(), ",");
					while (st.hasMoreTokens()) {
						fieldList.add(st.nextToken());
					}

					businessKeyMap.put(domainName, fieldList);
				}

				List<Field> fields = getAllFields(clazz);
				if (fields.isEmpty()) {
					continue;
				}
				this.processRepresentationField(domainName, fields);
				this.processTableColumn(domainName, fields);
				this.processAutoFill(domainName, fields);
				this.processReference(domainName, fields);
				this.processRepresentationLayout(domainName, clazz);
				this.processOneToMany(domainName, fields);
			}
		}

		this.processReferenceField();

		this.processUpdateImportBy();

		this.processOperationParameters();

		this.processExtraSearchField();
	}

	private void processExtraSearchField() {
		for (String domainName : searchMap.keySet()) {
			List<SearchFieldDesc> sfs = searchMap.get(domainName);

			for (SearchFieldDesc searchFieldDesc : sfs) {
				if (searchFieldDesc.getName().indexOf(".") != -1) {
					FormField ff = this.getFormFieldWithPropertyName(
							domainName, searchFieldDesc.getName());
					BeanUtils.copyProperties(ff, searchFieldDesc, "name",
							"title");
				}
			}
		}
	}

	private void processOperationParameters() {
		for (String domainName : domainDescMap.keySet()) {
			DomainDesc dd = domainDescMap.get(domainName);
			List<OperationDesc> operations = dd.getOperation();
			if (null != operations) {

				for (OperationDesc od : operations) {
					List<OperationParameter> parameters = od
							.getParameterAnnotations();
					if (null != parameters) {
						for (OperationParameter operationParameter : parameters) {
							FormField ff = null;
							switch (operationParameter.type()) {
							case INPUT:
								ff = new FormField();
								ff.setName(operationParameter.code());
								ff.setTitle(operationParameter.title());
								RepresentationType rt = new RepresentationType();
								rt.setView(RepresentationType.INPUT);
								ff.setType(rt);
								break;
							case SELECT:
								ff = new FormField();
								ff.setName(operationParameter.code());
								ff.setTitle(operationParameter.title());
								RepresentationType rts = new RepresentationType();
								rts.setView(RepresentationType.SELECT);

								List<Option> options = new ArrayList<Option>();
								StringTokenizer st = new StringTokenizer(
										operationParameter.value(), ",");
								while (st.hasMoreTokens()) {
									String token = st.nextToken();

									String[] splitedVal = token.split("=");
									if (splitedVal.length == 2) {
										Option option = new Option(
												splitedVal[0], splitedVal[1]);
										options.add(option);
									}
								}

								rts.setOptions(options);
								ff.setType(rts);
								break;
							case FIELD:
								// StringTokenizer fst = new StringTokenizer(
								// operationParameter.code(), ".");
								ff = this.getFormFieldWithPropertyName(
										domainName, operationParameter.code());
								break;
							default:
								break;
							}
							ff.setVisable(true);
							od.addParameter(ff);
						}
					}
				}
			}
		}
	}

	private void processUpdateImportBy() {
		for (String domainName : domainDescMap.keySet()) {
			DomainDesc dd = domainDescMap.get(domainName);
			if (null != dd.getUpdateImport()) {
				List<TableColumnDesc> tds = tableMap.get(domainName);
				for (UpdateImportDesc uid : dd.getUpdateImport()) {
					for (TableColumnDesc tableColumnDesc : tds) {
						if (tableColumnDesc.getName().equals(
								uid.getBy().replaceAll("\\.", "_"))) {
							uid.setLabel(tableColumnDesc.getTitle());
							break;
						}
					}
				}
			}
		}
	}

	private void processReferenceField() {
		for (String domainName : formMap.keySet()) {
			List<FormField> fields = formMap.get(domainName);
			for (FormField formField : fields) {
				Reference ref = this.getReferences(domainName,
						formField.getName());
				if (null != ref) {
					String refFields = ref.viewFields();
					if (!"".equals(refFields)) {
						String refDomainName = formField.getType()
								.getRefDomainName();
						// Map<String, FormField> fieldMap = new HashMap<String,
						// FormField>();
						// for (FormField ff : formMap.get(refDomainName)) {
						// fieldMap.put(ff.getName(), ff);
						// }
						for (String field : refFields.split(",")) {
							String refTitle = null;
							int p = field.indexOf(":");
							if (p != -1) {
								refTitle = field.substring(p + 1);
								field = field.substring(0, p);
							}
							addRefField(domainName, formField, field,
									refDomainName, refTitle);
						}
					}
				}
			}
		}
	}

	private void addRefField(String domainName, FormField formField,
			String field, String refDomainName, String refTitle) {

		String tempRefDomainName = refDomainName;
		StringTokenizer st = new StringTokenizer(field, ".");

		FormField tmpff = null;

		String title = null;

		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			tmpff = this.getFormFieldWithPropertyName(tempRefDomainName, token);
			if (null != tmpff) {
				// FormField ff = tmpff.cloneMe();
				// ff.setName(field);
				// formField.getType().addRefField(ff);
				//
				tempRefDomainName = tmpff.getType().getRefDomainName();
				if (null != tempRefDomainName) {
					title = tmpff.getTitle();
				}
			} else {
				System.out.println("Domain:" + domainName + ",field:"
						+ formField.getName() + ",refField:" + field
						+ " can not be fonud!");
				break;
			}
		}

		if (null != tmpff) {
			FormField clonedFormField = new FormField();
			BeanUtils.copyProperties(tmpff, clonedFormField);
			if (null != title) {
				clonedFormField.setTitle(title);
				clonedFormField.setName(field);
			}
			if (null != refTitle) {
				clonedFormField.setTitle(refTitle);
			}
			formField.getType().addRefField(clonedFormField);
		}
	}

	private FormField getFormFieldWithPropertyName(String domainName,
			String propertyName) {
		String currDomainName = domainName;
		FormField rtn = null;
		StringTokenizer st = new StringTokenizer(propertyName, ".");
		while (st.hasMoreTokens()) {
			String tmpPropertyName = st.nextToken();
			if (formMap.containsKey(currDomainName)) {
				List<FormField> fields = formMap.get(currDomainName);
				int i = 0;
				for (; i < fields.size(); i++) {
					FormField ff = fields.get(i);
					if (ff.getName().equals(tmpPropertyName)) {
						rtn = ff;
						break;
					}
				}
				if (i == fields.size()) {
					rtn = null;
					break;
				} else {
					currDomainName = rtn.getType().getRefDomainName();
				}
			} else {
				rtn = null;
				break;
			}
		}

		return rtn;
	}

	private void processOneToMany(String domainName, List<Field> fields) {
		List<OneToMany> list = new ArrayList<OneToMany>();
		for (Field field : fields) {
			javax.persistence.OneToMany annotation = field
					.getAnnotation(javax.persistence.OneToMany.class);
			javax.persistence.ManyToMany annotation1 = field
					.getAnnotation(javax.persistence.ManyToMany.class);
			if (null != annotation || null != annotation1) {
				OneToMany oneToMany = new OneToMany();
				Class<?> subClass = GenericUtils
						.getCollectionElementGeneric(field);
				oneToMany.setType(subClass);
				oneToMany.setFieldName(field.getName());

				for (Field f : ReflectUtils.getDeclaredFields(subClass)) {
					if (f.getType().getName()
							.equals(clazzMap.get(domainName).getName())) {
						oneToMany.setSubFieldName(f.getName());
						break;
					}
				}
				list.add(oneToMany);
			}
		}
		oneToManyMap.put(domainName, list);
	}

	private void processRepresentationLayout(String domainName, Class<?> clazz) {
		RepresentationLayout layout = clazz
				.getAnnotation(RepresentationLayout.class);
		if (null != layout) {
			representationLayoutMap.put(domainName, layout);
		}
	}

	private void processAutoFill(String domainName, List<Field> fields) {
		Map<Field, AutoFill> autoFills = new HashMap<Field, AutoFill>();
		for (Field field : fields) {
			AutoFill autoFill = field.getAnnotation(AutoFill.class);
			if (null != autoFill) {
				autoFills.put(field, autoFill);
			}
		}
		autoFillMap.put(domainName, autoFills);
	}

	private void processReference(String domainName, List<Field> fields) {
		Map<String, Reference> refMap = new HashMap<String, Reference>();
		for (Field field : fields) {
			Reference reference = field.getAnnotation(Reference.class);
			if (null != reference) {
				refMap.put(field.getName(), reference);
			}
		}
		referenceMap.put(domainName, refMap);
	}

	private void processTableColumn(String domainName, List<Field> fields) {
		List<TableColumnDesc> tableColumns = new ArrayList<TableColumnDesc>();
		for (Field field : fields) {
			TableColumn tableColumn = field.getAnnotation(TableColumn.class);
			ImageGalleryTableColumn igtc = field
					.getAnnotation(ImageGalleryTableColumn.class);
			ImageGalleryTableColumnDesc imageGallery = null;
			if (null != igtc) {
				imageGallery = new ImageGalleryTableColumnDesc();
				imageGallery.setUrl(igtc.url());

				if (Collection.class.isAssignableFrom(field.getType())) {
					imageGallery.setIsArray(true);
					imageGallery.setFieldName(field.getName());
				} else {
					imageGallery.setIsArray(false);
				}
				imageGallery.setIsFileStyle(igtc.isFileStyle());

				String fileNameProperty = igtc.fileNameProperty();
				if (!"".equals(fileNameProperty)) {
					imageGallery.setFileNameProperty(fileNameProperty);
				}
			}
			if (null != tableColumn) {
				TableColumnDesc column = new TableColumnDesc();
				String title = tableColumn.title();
				if ("".equals(title)) {
					title = field.getName();
				}

				Reference reference = field.getAnnotation(Reference.class);
				if (null != reference) {
					column.setName(field.getName() + "_"
							+ reference.label().replaceAll("\\.", "_"));
				} else {
					column.setName(field.getName());
				}

				if (StringUtils.isNotBlank(tableColumn.handler())) {
					column.setSortable(false);
				}

				column.setTitle(title);
				column.setShow(tableColumn.show());
				column.setSort(tableColumn.sort());
				column.setHandler(tableColumn.handler());
				column.setType(tableColumn.type());
				column.setImageGallery(imageGallery);
				tableColumns.add(column);
				BooleanValue bv = field.getAnnotation(BooleanValue.class);
				if (null != bv) {
					column.addBooleanValue(bv.value()[0]);
					column.addBooleanValue(bv.value()[1]);
				}
			} else {
				AssociateTableColumn associateTableColumn = field
						.getAnnotation(AssociateTableColumn.class);
				if (null != associateTableColumn) {
					Class<?> clazz = field.getType();
					String[] titles = associateTableColumn.titles().split(",");
					String[] columns = associateTableColumn.columns()
							.split(",");
					String[] sortArry = null;
					String sorts = associateTableColumn.sorts();
					if (!"".equals(sorts)) {
						sortArry = sorts.split(",");
					}
					String[] handlerArry = null;
					String handlers = associateTableColumn.handlers();
					if (!"".equals(handlers)) {
						handlerArry = handlers.split(",");
					}

					String[] shrowArry = null;
					String shows = associateTableColumn.shows();
					if (!"".equals(shows)) {
						shrowArry = shows.split(",");
					}
					for (int i = 0; i < columns.length; i++) {
						TableColumnDesc column = new TableColumnDesc();
						String title = titles[i];
						if ("".equals(title)) {
							title = field.getName();
						}
						if (null != igtc && columns[i].equals(igtc.field())) {
							column.setImageGallery(imageGallery);
						}

						column.setName(field.getName() + "_"
								+ columns[i].replaceAll("\\.", "_"));

						column.setTitle(title);
						// column.setShow(tableColumn.show());
						if (null != sortArry) {
							column.setSort(Integer.parseInt(sortArry[i]));
						}
						if (null != handlerArry) {
							column.setHandler(handlerArry[i]);
						}
						if (null != shrowArry) {
							if (shrowArry.length > i) {
								column.setShow(!StringUtils.equals("false",
										shrowArry[i]));
							}
						}
						// column.setType(tableColumn.type());
						String[] cols = columns[i].split("\\.");
						String last = cols[cols.length - 1];

						BooleanValue bv = null;
						List<Field> fs = null;
						if (cols.length > 1) {
							Class fClass = clazz;
							for (int j = 0; j < cols.length - 1; j++) {
								fs = getAllFields(fClass);
								Class t = null;
								for (Field f : fs) {
									if (f.getName().equals(cols[j])) {
										t = f.getType();
										break;
									}
								}
								fClass = t;
								if (fClass == null) {
									break;
								}
							}
							if (fClass != null) {
								fs = getAllFields(fClass);
							}
						} else {
							fs = getAllFields(clazz);
						}

						if (fs != null) {
							for (Field f : fs) {
								if (f.getName().equals(last)) {
									bv = f.getAnnotation(BooleanValue.class);
									break;
								}
							}
						}

						if (bv != null) {
							column.addBooleanValue(bv.value()[0]);
							column.addBooleanValue(bv.value()[1]);
						}
						tableColumns.add(column);
					}
				}
			}
		}
		if (!tableColumns.isEmpty()) {
			Collections.sort(tableColumns);
			tableMap.put(domainName, tableColumns);
		}
	}

	private void setValidation(Field field, FormField formField) {
		if (field.getAnnotation(NotNull.class) != null
				|| field.getAnnotation(NotBlank.class) != null
				|| field.getAnnotation(NotEmpty.class) != null) {
			formField.setRequired(true);
		}
		FieldValidation fv = new FieldValidation();
		Length l = field.getAnnotation(Length.class);
		if (null != l) {
			fv.setLength(l.max());
		}

		if (String.class.isAssignableFrom(field.getType())) {
			fv.setType(FieldValidation.TYPE_STRING);
			Pattern p = field.getAnnotation(Pattern.class);
			if (null != p) {
				fv.setPattern(p.regexp());
			}
		} else if (Long.class.isAssignableFrom(field.getType())) {
			fv.setType(FieldValidation.TYPE_LONG);

			DecimalMax dm = field.getAnnotation(DecimalMax.class);
			if (null != dm) {
				fv.setLength(dm.value().length());
				fv.setPattern(dm.value());
			}
		} else if (Double.class.isAssignableFrom(field.getType())) {
			fv.setType(FieldValidation.TYPE_DOUBLE);
			DecimalMax dm = field.getAnnotation(DecimalMax.class);
			if (null != dm) {
				fv.setLength(dm.value().length());
				fv.setPattern(dm.value());
			}
		}

		formField.setValidation(fv);
	}

	private void processRepresentationField(String domainName,
			List<Field> fields) {
		List<FormField> formFields = new ArrayList<FormField>();
		List<SearchFieldDesc> searchFields = new ArrayList<SearchFieldDesc>();
		Map<String, DefaultValue> defaultValues = new HashMap<String, DefaultValue>();
		String tagsInsert = null;
		for (Field field : fields) {
			RepresentationField representationField = field
					.getAnnotation(RepresentationField.class);
			DefaultValue dv = field.getAnnotation(DefaultValue.class);
			if (null != dv) {
				defaultValues.put(field.getName(), dv);
			}
			if (null != representationField) {
				FormField formField = new FormField();
				this.setValidation(field, formField);

				FormGroup group = representationField.group();
				FormGroupDesc fgd = new FormGroupDesc();
				fgd.setCode(group.code());
				fgd.setTitle(group.title());
				formField.setGroup(fgd);

				String title = representationField.title();
				if ("".equals(title)) {
					Title titleAnnotation = field.getAnnotation(Title.class);

					if (null != titleAnnotation) {
						title = titleAnnotation.value();
					} else {
						title = field.getName();
					}
				}

				formField.setName(field.getName());
				formField.setTitle(title);
				formField.setSort(representationField.sort());
				formField.setVisable(representationField.visable());
				ReferenceValue rv = field.getAnnotation(ReferenceValue.class);
				if (null != rv) {
					formField.setRefField(rv.field());
					formField.setRefPath(rv.path());
				}

				ImportIgnore importIgnore = field
						.getAnnotation(ImportIgnore.class);
				if (null != importIgnore) {
					formField.setImportIgnore(true);
				}

				MapLabel mapInfo = field.getAnnotation(MapLabel.class);
				if (null != mapInfo) {
					formField.setIsMapInfo(true);
					formField.setMapInfoSort(mapInfo.sort());
					if (mapInfo.title()) {
						formField.setIsMapInfoTitle(true);
					}
				}

				Longitude longitude = field.getAnnotation(Longitude.class);
				if (null != longitude) {
					formField.setIsLongitude(true);
				}

				Latitude latitude = field.getAnnotation(Latitude.class);
				if (null != latitude) {
					formField.setIsLatitude(true);
				}

				MapCity mapCity = field.getAnnotation(MapCity.class);
				if (null != mapCity) {
					formField.setMapCityProperty(mapCity.value());
				}

				MapAddress mapAddress = field.getAnnotation(MapAddress.class);
				if (null != mapAddress) {
					formField.setMapAddress(mapAddress.value());
				}

				if (!"".equals(representationField.onChangedListener())) {
					Map<String, String> tmpMap = changedListenerMap
							.get(domainName);
					if (null == tmpMap) {
						tmpMap = new HashMap<String, String>();
						changedListenerMap.put(domainName, tmpMap);
					}
					tmpMap.put(formField.getName(),
							representationField.onChangedListener());
					formField.setHasChangedListener(true);
				}

				RepresentationFieldType reType = representationField.view();
				RepresentationType type = new RepresentationType();
				type.setDisabled(representationField.disable());
				DictField df = field.getAnnotation(DictField.class);
				if (null != df) {
					type.setDict(df.value());
				}
				switch (reType) {
				case TAGS:
					type.setView(RepresentationType.TAGS);
					tagsInsert = formField.getName();
					break;
				case INPUT:
					type.setView(RepresentationType.INPUT);
					break;
				case TEXTAREA:
					type.setView(RepresentationType.TEXTAREA);
					break;
				case HIDDEN:
					type.setView(RepresentationType.HIDDEN);
					formField.setSort(0);
					break;
				case REFERENCE:
					type.setView(RepresentationType.REFERENCE);
					type.setRefName(field.getName());
					type.setEditable(false);
					Reference reference = field.getAnnotation(Reference.class);
					if (null != reference) {
						type.setEditable(reference.editable());
						type.setRefId(reference.id());
						type.setRefLabel(reference.label());
						if (!"".equals(reference.depend())) {
							type.setDepend(reference.depend());
							type.setDependAssociateField(reference
									.dependAssociateField());
						}
						type.setRefDomainName(field.getType().getSimpleName()
								.toLowerCase());

						type.setRefType(reference.type().name());
					}
					break;
				case DATE:
					type.setView(RepresentationType.DATE);
					type.setEditable(false);
					break;
				case TIME:
					type.setView(RepresentationType.TIME);
					type.setEditable(false);
					break;
				case DATETIME:
					type.setView(RepresentationType.DATETIME);
					type.setEditable(false);
					break;
				case TAB:
					type.setView(RepresentationType.TAB);
					TabView tabView = field.getAnnotation(TabView.class);
					TabViewType tabViewType = TabViewType.TABLE;
					if (null != tabView) {
						tabViewType = tabView.value();
					}
					type.setTabType(tabViewType.name().toLowerCase());
					Class<?> subClass = GenericUtils
							.getCollectionElementGeneric(field);

					for (Field subField : ReflectUtils
							.getDeclaredFields(subClass)) {
						if (subField.getType() == field.getDeclaringClass()) {
							type.setDependAssociateField(subField.getName());
						}
					}

					type.setRefDomainName(subClass.getSimpleName()
							.toLowerCase());

					SubImport subImport = field.getAnnotation(SubImport.class);
					if (null != subImport) {
						formField.setSubImport(true);
					}
					break;

				case ATTACHMENT:
					type.setView(RepresentationFieldType.ATTACHMENT.name());
					Class<?> subClass1 = GenericUtils
							.getCollectionElementGeneric(field);

					Attachment at = field.getAnnotation(Attachment.class);

					AttachmentDesc atd = new AttachmentDesc();
					atd.setUrl(at.url());
					atd.setFileName(at.fileName());
					atd.setSize(at.size());
					atd.setMaxSize(at.maxSize());
					atd.setType(at.type());
					atd.setDesc(at.desc());
					if (at.height() > 0) {
						atd.setHeight(at.height());
					}
					if (at.width() > 0) {
						atd.setWidth(at.width());
					}
					type.setAttachment(atd);

					for (Field subField : ReflectUtils
							.getDeclaredFields(subClass1)) {
						if (subField.getType() == field.getDeclaringClass()) {
							type.setDependAssociateField(subField.getName());
						}
					}

					type.setRefDomainName(subClass1.getSimpleName()
							.toLowerCase());
					break;
				case SELECT:
					type.setView(RepresentationType.SELECT);
					type.setRefName(field.getName());
					this.setReferenceInfo(field, type);
					break;
				case RADIO:
					type.setView(RepresentationType.RADIO);
					type.setRefName(field.getName());
					this.setReferenceInfo(field, type);
					break;
				case BOOLEAN:
					type.setView(RepresentationType.BOOLEAN);
					BooleanValue bv = field.getAnnotation(BooleanValue.class);
					if (null != bv) {
						type.addOption(new Option("true", bv.value()[0]));
						type.addOption(new Option("false", bv.value()[1]));
					}
					break;
				case HTML_EDITOR:
					type.setView(RepresentationType.HTML_EDITOR);
					break;
				case QRCODE:
					type.setView(RepresentationType.QRCODE);
					break;
				default:
					type.setView(reType.name().toLowerCase());
					break;
				}

				Enumerated enumerated = field.getAnnotation(Enumerated.class);
				if (null != enumerated) {
					type.setView(RepresentationType.ENUM);
					Object[] possibleValues = field.getType()
							.getEnumConstants();
					for (Object object : possibleValues) {
						String value = object.toString();
						type.addOption(new Option(value, value));
					}
				}

				String placeholder = representationField.placeholder();
				if (!"".equals(placeholder)) {
					type.setPlaceholder(placeholder);
				}

				formField.setType(type);
				formFields.add(formField);
				if (!"".equals(representationField.defaultVal())) {
					formField.setDefaultVal(representationField.defaultVal());
				}

				List<SearchField> allSearchFields = new ArrayList<SearchField>();

				SearchField sf = field.getAnnotation(SearchField.class);
				if (null != sf) {
					allSearchFields.add(sf);
				}

				SearchField.List sfList = field
						.getAnnotation(SearchField.List.class);
				if (null != sfList) {
					for (SearchField s : sfList.value()) {
						allSearchFields.add(s);
					}
				}

				if (allSearchFields.size() == 0
						&& representationField.isSearchField()) {
					SearchFieldDesc sfd = new SearchFieldDesc();
					BeanUtils.copyProperties(formField, sfd);
					searchFields.add(sfd);
				}

				for (SearchField searchField : allSearchFields) {
					SearchFieldDesc sfd = new SearchFieldDesc();
					BeanUtils.copyProperties(formField, sfd);
					sfd.setIsRange(searchField.isRange());
					sfd.setCanFuzzy(searchField.canFuzzy());
					// sfd.getType().setDisabled(false);
					if (!"".equals(searchField.path())) {
						sfd.setName(searchField.path());
					}
					if (!"".equals(searchField.title())) {
						sfd.setTitle(searchField.title());
					}
					searchFields.add(sfd);
				}
			}
		}
		if (!defaultValues.isEmpty()) {
			defaultValueMap.put(domainName, defaultValues);
		}
		if (!formFields.isEmpty()) {
			Collections.sort(formFields);
			formMap.put(domainName, formFields);
		}
		if (!searchFields.isEmpty()) {
			Collections.sort(searchFields);
			searchMap.put(domainName, searchFields);
		}

		tagsInsertMap.put(domainName, tagsInsert);
	}

	private void setReferenceInfo(Field field, RepresentationType type) {
		Reference reference = field.getAnnotation(Reference.class);
		if (null != reference) {
			type.setRefId(reference.id());
			type.setRefLabel(reference.label());
			if (!"".equals(reference.depend())) {
				type.setDepend(reference.depend());
				type.setDependAssociateField(reference.dependAssociateField());
			}
			type.setRefDomainName(field.getType().getSimpleName().toLowerCase());
		}
	}

	private List<Field> getAllFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();
		if (type != null) {
			fields.addAll(Arrays.asList(type.getDeclaredFields()));
			fields.addAll(getAllFields(type.getSuperclass()));
		}
		return fields;
	}

	public String getSerializer(String domainName) {
		return serializerMap.get(domainName);
	}
}
