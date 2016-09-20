package simple.core.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.rmi.NoSuchObjectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import jxl.Cell;
import jxl.CellType;
import jxl.CellView;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Range;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import simple.config.annotation.AutoFill;
import simple.config.annotation.BooleanValue;
import simple.config.annotation.BusinessKey;
import simple.config.annotation.DefaultValue;
import simple.config.annotation.ImportNotFindCheck.CheckType;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.UpdateImport;
import simple.config.annotation.support.DefaultValueHandler;
import simple.config.annotation.support.ImportExtentionCheck.CheckResult;
import simple.core.model.FormField;
import simple.core.model.OneToMany;
import simple.core.model.Option;
import simple.core.model.RepresentationType;
import simple.core.model.TableColumnDesc;
import simple.core.service.BaseService;
import simple.core.service.HandlerService;
import simple.core.service.HttpSessionService;
import simple.core.service.ImportExtentionCheckService;
import simple.core.service.ImportService;
import simple.core.util.GsonBuilderUtil;
import simple.core.util.ReflectUtils;
import simple.core.validation.annotation.UniqueKey;

import com.google.gson.Gson;

@Service
public class ImportHandler extends HandlerService {

	@Autowired
	ImportService importService;

	@Autowired
	BusinessKeyAutoFillHandler businessKeyAutoFillHandler;

	@Autowired
	protected BaseService baseService;

	@Autowired
	protected HttpSessionService httpSessionService;

	@Autowired
	protected ImportExtentionCheckService importExtentionCheckService;

	private SimpleDateFormat timestamp = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");

	private SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");

	private static final String TEMPLATE_CACHE_SUFFIX = ".template.cache";

	private static final String TEMPLATE_FILE_CACHE_PATH;

	private static final String FILE_IMPORT_TEMP_PATH;
	static {
		TEMPLATE_FILE_CACHE_PATH = System.getProperty("java.io.tmpdir")
				+ "simple" + File.separator + "cache" + File.separator
				+ "template";
		FILE_IMPORT_TEMP_PATH = System.getProperty("java.io.tmpdir") + "simple"
				+ File.separator + "import";

		File cacheFoler = new File(TEMPLATE_FILE_CACHE_PATH);
		if (cacheFoler.exists() && cacheFoler.isDirectory()) {
			try {
				FileUtils.deleteDirectory(cacheFoler);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cacheFoler.mkdirs();

		File importFoler = new File(FILE_IMPORT_TEMP_PATH);
		if (importFoler.exists() && importFoler.isDirectory()) {
			try {
				FileUtils.deleteDirectory(importFoler);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		importFoler.mkdirs();
	}

	private Map<String, List<FormField>> getAllField(String domainName) {
		Map<String, List<FormField>> fieldNameMap = new LinkedHashMap<String, List<FormField>>();
		List<FormField> fieldList = new ArrayList<FormField>();
		fieldNameMap.put(domainName, fieldList);
		List<FormField> allFields = this.baseService
				.getDomainFormDesc(domainName);
		for (FormField formField : allFields) {
			fieldList.add(formField);
			if (formField.isSubImport()) {
				String subDomainName = formField.getType().getRefDomainName();
				fieldNameMap.putAll(getAllField(subDomainName));
				if (fieldNameMap.containsKey(subDomainName)) {
					List<FormField> subFieldList = fieldNameMap
							.get(subDomainName);
					for (FormField subFormField : subFieldList) {
						if (StringUtils.equals(subFormField.getType()
								.getRefDomainName(), domainName)) {
							subFieldList.remove(subFormField);
							if (subFieldList.isEmpty()) {
								fieldNameMap.remove(subDomainName);
							}
							break;
						}
					}
				}
			}
		}

		if (fieldList.isEmpty()) {
			fieldNameMap.remove(fieldNameMap);
		}
		return fieldNameMap;
	}

	public InputStream export(String domainName, List<Object> dataList)
			throws RowsExceededException, WriteException, IOException {
		String tempFileName = (Thread.currentThread().getName() + System
				.currentTimeMillis()).hashCode() + "";
		String tempFilePath = FILE_IMPORT_TEMP_PATH + File.separator
				+ tempFileName;
		String domainLabel = this.getDomainLabel(domainName);
		File tempFile = new File(tempFilePath);
		WritableWorkbook workbook = Workbook.createWorkbook(tempFile);
		WritableSheet sheet = workbook.createSheet(domainLabel, 0);
		CellView cellView = new CellView();
		// cellView.setAutosize(true); // 设置自动大小
		WritableFont normal = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK);
		WritableCellFormat normalCellFormat = new WritableCellFormat(normal);
		// normalCellFormat.setBackground(Colour.);
		normalCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		normalCellFormat.setShrinkToFit(true);

		List<TableColumnDesc> tableColumnDescs = this.baseService
				.getDomainTableDesc(domainName);

		List<TableColumnDesc> fieldList = new ArrayList<TableColumnDesc>();

		for (int i = 0, j = 0; i < tableColumnDescs.size(); i++) {
			TableColumnDesc tableColumnDesc = tableColumnDescs.get(i);
			if (!tableColumnDesc.isShow()) {
				continue;
			}

			Label label = new Label(j, 0, tableColumnDesc.getTitle(),
					normalCellFormat);
			sheet.setColumnView(j, cellView);
			sheet.setColumnView(j,
					tableColumnDesc.getTitle().getBytes().length + 2);
			sheet.addCell(label);
			fieldList.add(tableColumnDesc);
			j++;
		}

		for (int l = 0; l < dataList.size(); l++) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) dataList.get(l);
			for (int i = 0; i < fieldList.size(); i++) {
				TableColumnDesc tableColumnDesc = fieldList.get(i);
				Object value = map.get(tableColumnDesc.getName());
				String valStr = "";
				if (null != value) {
					if (value instanceof Date) {
						valStr = this.timestamp.format(value);
						sheet.addCell(new Label(i, l + 1, valStr));
						// DateTime d=new DateTime(i, l + 1, (Date) value);
						// sheet.addCell(d);
					} else if (value instanceof Number) {
						Number number = (Number) value;
						sheet.addCell(new jxl.write.Number(i, l + 1, number
								.doubleValue()));
					} else {
						valStr = String.valueOf(value);
						if (tableColumnDesc.getBooleanValue() != null
								&& !tableColumnDesc.getBooleanValue().isEmpty()) {
							if ("true".equals(valStr)
									&& tableColumnDesc.getBooleanValue().size() >= 1) {
								sheet.addCell(new Label(i, l + 1,
										tableColumnDesc.getBooleanValue()
												.get(0)));
							} else if ("false".equals(valStr)
									&& tableColumnDesc.getBooleanValue().size() >= 2) {
								sheet.addCell(new Label(i, l + 1,
										tableColumnDesc.getBooleanValue()
												.get(1)));
							} else {
								sheet.addCell(new Label(i, l + 1, valStr));
							}
						} else {
							sheet.addCell(new Label(i, l + 1, valStr));
						}
					}
					// sheet.addCell(new Label(i, l + 1, valStr));
				}

			}
		}

		workbook.write();
		workbook.close();

		System.out.println(tempFile.getAbsolutePath());

		return new FileInputStream(tempFile);
	}

	public InputStream getTemplateInputStream(String domainName)
			throws RowsExceededException, WriteException, IOException {
		return getTemplateInputStream(domainName, true);
	}

	public InputStream getTemplateInputStream(String domainName,
			boolean isSingleSheet) throws IOException, RowsExceededException,
			WriteException {
		String templateFilePath = TEMPLATE_FILE_CACHE_PATH + File.separator
				+ domainName + TEMPLATE_CACHE_SUFFIX;
		File templateFile = new File(templateFilePath);
		if (!templateFile.exists()) {
			WritableWorkbook workbook = Workbook.createWorkbook(templateFile);
			try {
				CellConfig cellConfig = new CellConfig();
				Map<String, List<FormField>> sheets = getAllField(domainName);
				List<FormField> formFields = sheets.get(domainName);

				int sheetNun = 0;
				int startCol = 0;

				String domainLabel = this.getDomainLabel(domainName);
				WritableSheet sheet = workbook.createSheet(domainLabel,
						sheetNun);
				sheet.setColumnView(startCol, cellConfig.cellView);
				sheet.addCell(new Label(startCol, 0, domainLabel,
						cellConfig.normalCellFormat));

				List<FormField> subFields = fillCell(domainName, formFields,
						sheet, startCol, cellConfig);
				startCol += formFields.size();

				if (startCol > 1) {
					sheet.mergeCells(0, 0, startCol - 1, 0);
				}

				for (FormField subField : subFields) {
					if (!isSingleSheet) {
						startCol = 0;
						sheetNun++;
						sheetNun = fillCellBySubfield(subField, sheets,
								workbook, sheetNun, isSingleSheet, startCol,
								cellConfig);
					} else {
						startCol = fillCellBySubfield(subField, sheets,
								workbook, sheetNun, isSingleSheet, startCol,
								cellConfig);
					}
				}
				workbook.write();

			} catch (Exception e) {
				// TODO: handle exception
				workbook.close();
				templateFile.delete();
				e.printStackTrace();
			} finally {
				workbook.close();
			}
		}
		return new FileInputStream(templateFile);
	}

	private List<FormField> fillCell(String domainName,
			List<FormField> formFields, WritableSheet sheet, int startCol,
			CellConfig cellConfig) throws RowsExceededException, WriteException {
		List<FormField> subFields = new ArrayList<FormField>();
		List<FormField> ignoreField = new ArrayList<FormField>();
		for (FormField formField : formFields) {
			if (formField.isSubImport()) {
				subFields.add(formField);
			}
			if (isIgnoreField(formField)) {
				ignoreField.add(formField);
				continue;
			}
			WritableCellFormat format;
			if (formField.isRequired()) {
				format = cellConfig.requiredCellFormat;
			} else {
				format = cellConfig.normalCellFormat;
			}
			// CellView cellView = new CellView();
			// cellView.setAutosize(true);
			sheet.setColumnView(startCol, cellConfig.cellView);
			sheet.setColumnView(startCol, formField.getTitle().trim()
					.getBytes().length + 2);
			Label label = new Label(startCol, 1, formField.getTitle().trim(),
					format);
			sheet.addCell(label);
			String dictCode = formField.getType().getDict();
			List<String> text = new ArrayList<String>();
			List<Option> options = formField.getType().getOptions();
			if (options != null && !options.isEmpty()) {
				for (Option option : options) {
					text.add(option.getName());
				}
			} else if (StringUtils.isNotEmpty(dictCode)) {
				List<String> propertyList = new ArrayList<String>();
				propertyList.add(formField.getType().getRefLabel());
				String filter = "dict.code='" + dictCode + "' ";
				Reference ref = baseService.getReferences(domainName,
						formField.getName());
				if (null != ref && !StringUtils.isBlank(ref.filter())) {
					filter += "and " + filter;
				}
				Class clazz = baseService.getDomainClass(formField.getType()
						.getRefDomainName());
				List<Object> entityList = importService.findDictItemProperties(
						clazz, propertyList, filter);
				for (Object entity : entityList) {
					text.add(String.valueOf(entity));
				}
			}
			if (!text.isEmpty()) {
				WritableCellFeatures cellFeature = label
						.getWritableCellFeatures();
				if (cellFeature == null) {
					cellFeature = new WritableCellFeatures();
				}
				String comment = StringUtils.join(text, ",");
				double h = 4;
				double w = 3;
				int l = 50;
				if (comment.getBytes().length > l) {
					double p = comment.getBytes().length / l - 1;
					p = 1 + p / 4;
					h *= p;
					w *= p;
				}
				cellFeature.setComment("[" + comment + "]", w, h);
				label.setCellFeatures(cellFeature);
			}
			startCol++;
		}
		formFields.removeAll(ignoreField);
		return subFields;
	}

	private int fillCellBySubfield(FormField field,
			Map<String, List<FormField>> sheets, WritableWorkbook workbook,
			int sheetNun, boolean isSingleSheet, int startCol,
			CellConfig cellConfig) throws RowsExceededException, WriteException {
		String domainName = field.getType().getRefDomainName();
		List<FormField> formFields = sheets.get(domainName);
		WritableSheet sheet;
		String domainLabel = this.getDomainLabel(domainName);
		if (workbook.getSheets().length > sheetNun) {
			sheet = workbook.getSheet(sheetNun);
		} else {
			sheet = workbook.createSheet(domainLabel, sheetNun);
		}
		sheet.setColumnView(startCol, cellConfig.cellView);
		sheet.addCell(new Label(startCol, 0, domainLabel,
				cellConfig.normalCellFormat));
		List<FormField> subFields = fillCell(domainName, formFields, sheet,
				startCol, cellConfig);
		int sumCol = startCol + formFields.size();
		if ((sumCol - startCol) > 1) {
			sheet.mergeCells(startCol, 0, sumCol - 1, 0);
		}

		// for (FormField subField : subFields) {
		// if (!isSingleSheet) {
		// startCol = 0;
		// sheetNun = fillCellBySubfield(subField, sheets, workbook,
		// sheetNun, isSingleSheet, startCol, cellConfig);
		// sheetNun++;
		// } else {
		// sumCol += fillCellBySubfield(subField, sheets, workbook,
		// sheetNun, isSingleSheet, startCol, cellConfig);
		// }
		// }
		return isSingleSheet ? sumCol : sheetNun;
	}

	private boolean isIgnoreField(FormField formField) {
		return formField.isHidden() || formField.getImportIgnore()
				|| formField.getType().getView().equals(RepresentationType.TAB)
				|| null != formField.getType().getAttachment();
	}

	public String getDomainLabel(String domainName) {
		String domainLabel = this.baseService.getDomainDesc(domainName)
				.getLabel();
		if (StringUtils.isBlank(domainLabel)) {
			domainLabel = domainName;
		}
		return domainLabel;
	}

	public String importFile(InputStream file, String domainName,
			HttpServletRequest request, boolean onlyInsert) {
		return importFile(file, domainName, request, onlyInsert, false);
	}

	public String importFile(InputStream file, String domainName,
			HttpServletRequest request, boolean onlyInsert, boolean batch) {
		ImportResult ir = new ImportResult();
		String tempFileName = (Thread.currentThread().getName() + System
				.currentTimeMillis()).hashCode() + "";
		String tempFilePath = FILE_IMPORT_TEMP_PATH + File.separator
				+ tempFileName;
		// String errFilePath = FILE_IMPORT_TEMP_PATH + File.separator
		// + tempFileName+".err";
		boolean hasError = false;
		WritableWorkbook copy = null;
		int bathcSize = 50;
		int maxErrorSize = 500;
		int threadSize = bathcSize / 10;
		if (threadSize <= 1) {
			threadSize = 5;
		} else if (threadSize > 50) {
			threadSize = 50;
		}
		ExecutorService service = Executors.newScheduledThreadPool(threadSize);
		try {
			Workbook workbook = Workbook.getWorkbook(file);
			// WritableWorkbook errbook=Workbook.createWorkbook(new
			// File(errFilePath));
			// WritableSheet errSheet = workbook.createSheet(domainLabel,
			// sheetNun);
			copy = Workbook.createWorkbook(new File(tempFilePath), workbook);
			WritableSheet sheet = copy
					.getSheet(this.getDomainLabel(domainName));

			if (sheet == null) {
				throw new Exception("错误的模版格式");
			}
			List<MergedHeader> headers = this.getHeader(sheet, domainName);
			// 校验文件格式
			if (headers.isEmpty()) {
				throw new Exception("错误的模版格式");
			}

			CellConfig cellConfig = new CellConfig();
			// int lastColNun = sheet.getColumns();
			int lastColNun = headers.get(headers.size() - 1).endCol + 1;
			Label warningLabel = new Label(lastColNun, 0, "错误信息");
			warningLabel.setCellFormat(cellConfig.warningCellFormat);
			sheet.addCell(warningLabel);
			DataIterator iterator = iterator(sheet, headers);
			long start = System.currentTimeMillis();
			List<ImportData> errors = new ArrayList<ImportData>();
			List<ImportData> rows = new ArrayList<ImportData>();
			int emptyNun = 0;
			while (iterator.hasNext()) {
				int startRow = iterator.getIndex();
				Map<String, Object> dataMap = new HashMap<String, Object>();
				try {
					dataMap = iterator.next();
				} catch (Exception e) {
					// TODO: handle exception
					ImportData r = new ImportData(startRow, startRow + 1, null);
					r.msg = e.getMessage();
					errors.add(r);
					continue;
				}

				if (dataMap.isEmpty()) {
					ImportData r = new ImportData(startRow, startRow + 1, null);
					r.msg = "空行！";
					// errors.add(r);
					emptyNun++;
					if (emptyNun > 5) {
						break;
					}
					continue;
				}
				emptyNun = 0;
				int endRow = iterator.getIndex();
				Object domain = this.baseService.getDomainFromPostJosn(
						this.baseService.toJsonStr(dataMap), domainName);

				rows.add(new ImportData(startRow, endRow, domain));

				if (rows.size() >= bathcSize) {
					System.err.println(System.currentTimeMillis());
					initImporDatas(domainName, request, service, onlyInsert,
							rows);
					// if (!(batch && hasError)) {
					saveDatas(rows, batch ? null : service);
					// }
					for (ImportData r : rows) {
						if (!r.success) {
							r.data = null;
							errors.add(r);
						}
						if (r.checkResult != null
								&& r.checkResult.getCheckType() == CheckType.warn) {
							r.data = null;
							errors.add(r);
						}
					}
					if (!hasError) {
						hasError = !errors.isEmpty();
					}
					rows.clear();

				}
			}
			if (!rows.isEmpty()) {
				initImporDatas(domainName, request, service, onlyInsert, rows);
				// if (!(batch && hasError)) {
				saveDatas(rows, batch ? null : service);
				// }
				for (ImportData r : rows) {
					if (!r.success) {
						r.data = null;
						errors.add(r);
					}
					if (r.checkResult != null
							&& r.checkResult.getCheckType() == CheckType.warn) {
						r.data = null;
						errors.add(r);
					}
				}
				if (!hasError) {
					hasError = !errors.isEmpty();
				}
				rows.clear();
			}
			System.err.println("import time:"
					+ (System.currentTimeMillis() - start));
			if (!errors.isEmpty()) {
				hasError = true;
				writeError(sheet, lastColNun, errors);
				errors.clear();
			}
			copy.write();
		} catch (BiffException e) {
			hasError = true;
			ir.setResultInfo("文件格式错误！");
		} catch (Exception e) {
			e.printStackTrace();
			hasError = true;
			ir.setResultInfo(e.getMessage());
		} finally {
			service.shutdown();
			if (null != copy) {
				try {
					copy.close();
				} catch (WriteException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		ir.setSuccess(!hasError);

		if (hasError) {
			ir.setErrorFileName(tempFileName);
			throw new RuntimeException(this.baseService.toJsonStr(ir));
		} else {
			return this.baseService.toJsonStr(ir);
		}

	}

	private void initImporDatas(final String domainName,
			final HttpServletRequest request, ExecutorService service,
			final boolean onlyInsert, List<ImportData> rows) {
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		final HttpSession session = httpSessionService.getSession();

		for (final ImportData row : rows) {
			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					// TODO Auto-generated method stub
					httpSessionService.setSession(session);
					try {
						Object domain = row.data;
						Object saveData;
						if (!onlyInsert) {
							saveData = initUpdateData(domainName, domain);
						} else {
							saveData = domain.getClass().newInstance();
							copyProperties(domain.getClass(), domain, saveData);
						}

						initImporData(domainName, request, saveData);

						if (onlyInsert) {
							autoFillByDefaultValueHandler(saveData, request);
							//解决新增导入时，修改时间比创建时间早的问题
							baseService.autoFillDomain(domainName, saveData,request);
						}

						row.data = saveData;
						row.success = true;
						List<String> errorList = ImportHandler.this.baseService
								.validateWithPropertyPath(domainName, saveData);
						if (!errorList.isEmpty()) {
							row.success = false;
							row.msg = StringUtils.join(errorList, "");
						} else {
							row.checkResult = importExtentionCheckService
									.handle(domain, saveData);
							if (row.checkResult != null
									&& row.checkResult.getCheckType() != CheckType.none) {
								row.success = row.checkResult.getCheckType() != CheckType.err;
								row.msg = StringUtils.join(
										row.checkResult.getMsg(), ";");
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
						row.success = false;
						row.msg = e.getMessage();
					}
					return null;
				}
			});
		}
		if (service != null) {
			try {
				service.invokeAll(tasks);
			} catch (Exception e) {
				for (ImportData row : rows) {
					if (row.success) {
						row.success = false;
						row.msg = e.getMessage();
					}
				}
			}
		} else {
			for (Callable<Void> call : tasks) {
				try {
					call.call();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

		validateWithUniqueKey(domainName, rows);
	}

	private void initHibernateObject(Object obj) throws Exception {
		Class clazz = obj.getClass();
		Field[] fields = ReflectUtils.getDeclaredFields(clazz);
		for (Field field : fields) {
			field.setAccessible(true);
			Object o = field.get(obj);
			if (null != o) {
				if (o instanceof HibernateProxy) {
					HibernateProxy proxy = (HibernateProxy) o;
					LazyInitializer initializer = proxy
							.getHibernateLazyInitializer();
					o = initializer.getSession().immediateLoad(
							initializer.getEntityName(),
							initializer.getIdentifier());
					field.set(obj, o);
					initHibernateObject(o);
				} else if (o instanceof PersistentSet) {
					Set set = new HashSet((PersistentSet) o);
					field.set(obj, set);
					for (Object s : set) {
						initHibernateObject(s);
					}
				}
			}
		}
	}

	private Object initUpdateData(String domainName, final Object domain)
			throws NoSuchFieldException, IllegalAccessException,
			NoSuchObjectException {
		Class clazz = domain.getClass();
		final List<String> bkList = getBusinessKey(domainName, clazz);
		Object saveData;
		if (null != bkList) {
			Field idField = ReflectUtils.getField(domain.getClass(), "id");
			idField.setAccessible(true);
			Object id = idField.get(domain);
			if (null == id) {
				Object obj = baseService
						.doNewTransaction(new BaseService.Callback<Object>() {
							@Override
							public Object doAction() {
								// TODO Auto-generated method stub
								try {
									Object rt = importService.getByBusinessKey(
											domain, bkList);
									if (rt == null) {
										return null;
									}
									initHibernateObject(rt);
									return rt;
								} catch (Exception e) {
									// TODO: handle exception
									return null;
								}
							}
						});
				if (obj != null) {
					saveData = obj;
					copyProperties(obj.getClass(), domain, saveData);
				} else {
					throw new RuntimeException("更新的数据不存在！");
				}
			} else {
				saveData = domain;
			}
		} else {
			saveData = domain;
		}
		List<OneToMany> oneToManies = baseService.getOneToMany(domainName);
		if (oneToManies == null) {
			oneToManies = new ArrayList<OneToMany>();
		}
		for (OneToMany oneToMany : oneToManies) {
			// get property filed
			Field subField = ReflectUtils.getField(clazz,
					oneToMany.getFieldName());
			subField.setAccessible(true);
			boolean isManyToMany = (null == oneToMany.getSubFieldName());

			if (!isManyToMany) {
				List<String> subBkList = baseService.getBusinessKey(oneToMany
						.getType().getSimpleName().toLowerCase());
				if (null != subBkList) {
					Set<?> subSet = (Set<?>) subField.get(domain);
					if (subSet == null) {
						subSet = new HashSet(0);
					}
					Set set = new HashSet();
					for (Object sub : subSet) {
						Object obj = importService.getByBusinessKey(sub,
								subBkList);
						if (obj != null) {
							copyProperties(obj.getClass(), sub, obj);
							set.add(obj);
						} else {
							set.add(sub);
						}
					}
					subField.set(saveData, set);
				}
			}
		}
		return saveData;
	}

	private void writeError(WritableSheet sheet, int lastColNun,
			List<ImportData> errors) throws WriteException,
			RowsExceededException {
		long start = System.currentTimeMillis();
		for (ImportData r : errors) {
			for (int i = r.startRow; i < r.endRow; i++) {
				Label label = new Label(lastColNun, i, r.msg);
				sheet.addCell(label);
			}
		}
		System.err.println("write error size:" + errors.size() + " time:"
				+ (System.currentTimeMillis() - start));
	}

	private void initImporData(String domainName, HttpServletRequest request,
			Object domain) throws Exception, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		this.autoFill(domain, null, request);
		this.baseService.autoFillDomain(domainName, domain, request);
	}

	private void validateWithUniqueKey(String domainName, List<ImportData> rows) {
		Class clazz = this.baseService.getDomainClass(domainName);
		if (clazz == null) {
			return;
		}
		UniqueKey uniqueKey = (UniqueKey) clazz.getAnnotation(UniqueKey.class);
		List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();
		if (uniqueKey != null) {
			uniqueKeys.add(uniqueKey);
		}
		UniqueKey.List list = (UniqueKey.List) clazz
				.getAnnotation(UniqueKey.List.class);
		if (list != null && list.value() != null) {
			uniqueKeys.addAll(Arrays.asList(list.value()));
		}
		if (uniqueKeys.isEmpty()) {
			return;
		}
		Set<String> cache = new HashSet<String>();
		Gson gson = GsonBuilderUtil.getDefaultGsonBuilder().create();
		for (final ImportData r : rows) {
			if (!r.success) {
				continue;
			}
			for (UniqueKey u : uniqueKeys) {
				String[] columnNames = u.columnNames();
				Map<String, Object> data = new HashMap<String, Object>();
				for (String name : columnNames) {
					Object value = getValueByProperty(clazz, r.data,
							name.split("\\."));
					data.put(name, value == null ? "" : value);
				}

				String key = gson.toJson(data);
				if (cache.contains(key)) {
					r.success = false;
					r.msg = u.message();
					break;
				} else {
					cache.add(key);
				}
			}
		}

	}

	private Object getValueByProperty(Class clazz, Object data,
			String[] property) {
		if (data == null) {
			return null;
		}
		try {
			Class cls = clazz;
			Object obj = data;
			for (String p : property) {
				if (obj == null) {
					return null;
				}
				Field f = ReflectUtils.getField(cls, p);
				f.setAccessible(true);
				obj = f.get(obj);
				cls = f.getType();
			}
			return obj;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	private long saveDatas(List<ImportData> rows, ExecutorService service)
			throws Exception {
		long start = System.currentTimeMillis();
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		final HttpSession session = httpSessionService.getSession();

		for (final ImportData r : rows) {
			if (!r.success) {
				continue;
			}
			tasks.add(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					// TODO Auto-generated method stub
					httpSessionService.setSession(session);
					try {
						baseService.update(r.data, true);
						r.success = true;
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						r.msg = e.getMessage();
						r.success = false;
					}
					return null;
				}
			});
		}
		if (!tasks.isEmpty()) {
			if (service != null) {
				service.invokeAll(tasks);
			} else {
				for (Callable<Void> call : tasks) {
					call.call();
				}
			}
		}
		// System.err.println("imporData:" + rows.size() + ",time:"
		// + (System.currentTimeMillis() - start));
		return System.currentTimeMillis() - start;
	}

	private class ImportData {
		public final int startRow;
		public final int endRow;
		public Object data;
		public String msg;
		public boolean success = false;
		public CheckResult checkResult = null;

		public ImportData(int startRow, int endRow, Object data) {
			this.startRow = startRow;
			this.endRow = endRow;
			this.data = data;
		}
	}

	private void autoFillByDefaultValueHandler(Object domain,
			HttpServletRequest request) {
		if (domain == null) {
			return;
		}
		Class clazz = domain.getClass();
		if (baseService.getDomainDesc(clazz.getSimpleName().toLowerCase()) == null) {
			return;
		}
		Field[] fields = ReflectUtils.getDeclaredFields(clazz);
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				Object obj = field.get(domain);
				if (obj == null) {
					DefaultValue defaultValue = field
							.getAnnotation(DefaultValue.class);
					if (defaultValue != null) {
						DefaultValueHandler dvHanlder = (DefaultValueHandler) super
								.fetchHandler(defaultValue.handler());
						field.set(domain, dvHanlder.handle(domain, request));
					}
					if (field.getType().equals(Boolean.class)
							|| field.getType().equals(boolean.class)) {
						RepresentationField representationField = field
								.getAnnotation(RepresentationField.class);
						if (representationField != null
								&& StringUtils.isNotBlank(representationField
										.defaultVal())) {
							field.set(domain,
									BooleanUtils.toBoolean(representationField
											.defaultVal()));
						}
					}
					continue;
				} else {//
					RepresentationField representationField = field
							.getAnnotation(RepresentationField.class);
					if (representationField != null
							&& StringUtils.isNotBlank(representationField
									.onChangedListener())) {
						this.baseService.processFieldValueChanged(clazz
								.getSimpleName().toLowerCase(),
								field.getName(), domain);
					}
				}
				if (field.getAnnotation(ManyToMany.class) != null) {
					ManyToMany many = field.getAnnotation(ManyToMany.class);
					String mappedBy = many.mappedBy();
					field.setAccessible(true);
					Collection objs = (Collection) obj;
					if (objs.isEmpty()) {
						continue;
					}
					for (Object o : objs) {
						Field subField = ReflectUtils.getField(o.getClass(),
								mappedBy);
						subField.setAccessible(true);
						subField.set(o, domain);
						this.autoFillByDefaultValueHandler(o, request);
						subField.set(o, null);
					}
				} else if (field
						.getAnnotation(javax.persistence.OneToMany.class) != null) {
					javax.persistence.OneToMany many = field
							.getAnnotation(javax.persistence.OneToMany.class);
					String mappedBy = many.mappedBy();
					field.setAccessible(true);
					Collection objs = (Collection) obj;
					if (objs.isEmpty()) {
						continue;
					}
					for (Object o : objs) {
						Field subField = ReflectUtils.getField(o.getClass(),
								mappedBy);
						subField.setAccessible(true);
						subField.set(o, domain);
						this.autoFillByDefaultValueHandler(o, request);
						subField.set(o, null);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	// 对onetomany和manytoone的并且没有设置设置AutoFill和BusinessKey的属性进行自动填充。
	private void autoFill(Object domain, String mappedBy,
			HttpServletRequest request) throws Exception {
		// TODO Auto-generated method stub
		if (domain == null) {
			return;
		}
		Class clazz = domain.getClass();
		if (baseService.getDomainDesc(clazz.getSimpleName().toLowerCase()) == null) {
			return;
		}
		Field[] fields = ReflectUtils.getDeclaredFields(clazz);
		for (Field field : fields) {
			if (StringUtils.equals(field.getName(), mappedBy)) {
				continue;
			}
			field.setAccessible(true);
			Object obj = field.get(domain);
			if (obj == null) {
				continue;
			}
			if (field.getAnnotation(AutoFill.class) != null) {
				// this.autoFill(obj,null, request);
				continue;
			}

			if (field.getAnnotation(ManyToOne.class) != null
					|| field.getAnnotation(OneToOne.class) != null) {
				businessKeyAutoFillHandler.handle(field, domain, request);
				// this.autoFill(obj, null, request);
			}

			if (field.getAnnotation(javax.persistence.OneToMany.class) != null
					|| field.getAnnotation(ManyToMany.class) != null) {
				field.setAccessible(true);
				Collection objs = (Collection) field.get(domain);
				if (objs == null || objs.isEmpty()) {
					continue;
				}
				String subMappedBy;
				if (field.getAnnotation(javax.persistence.OneToMany.class) != null) {
					javax.persistence.OneToMany many = field
							.getAnnotation(javax.persistence.OneToMany.class);
					subMappedBy = many.mappedBy();
				} else {
					ManyToMany many = field.getAnnotation(ManyToMany.class);
					subMappedBy = many.mappedBy();
				}
				field.set(domain, null);
				for (Object o : objs) {
					if (o == null) {
						continue;
					}
					if (o.getClass().getAnnotation(BusinessKey.class) != null) {
						break;
					}
					Field mappedByField = ReflectUtils.getField(o.getClass(),
							subMappedBy);
					mappedByField.setAccessible(true);
					mappedByField.set(o, domain);
					this.autoFill(o, subMappedBy, request);
					mappedByField.set(o, null);
				}
				field.set(domain, objs);
			}

		}

	}

	public File getImportFile(String fileName) {
		return new File(FILE_IMPORT_TEMP_PATH + File.separator + fileName);
	}

	private void copyProperties(Class<?> clazz, Object source, Object target)
			throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = ReflectUtils.getDeclaredFields(clazz);
		for (Field field : fields) {
			if ("id".equals(field.getName())
					|| Modifier.toString(field.getModifiers())
							.indexOf("static") != -1) {
				continue;
			}
			field.setAccessible(true);
			Object obj = field.get(source);
			if (null != obj) {
				if (obj instanceof Set) {
					Set set = (Set) obj;
					if (!set.isEmpty()) {
						field.set(target, obj);
					}
				} else {
					field.set(target, obj);
				}
			}
		}
	}

	private void fillFieldMapping(Map<String, String> map, String domainName,
			FormField formField) {
		if (isIgnoreField(formField)) {
			return;
		}
		Reference ref = baseService.getReferences(domainName,
				formField.getName());
		if (ref != null) {
			map.put(formField.getTitle().trim(), formField.getName() + "."
					+ ref.label());
		} else {
			map.put(formField.getTitle().trim(), formField.getName());
		}
	}

	private MergedHeader createHeader(Sheet sheet, int startCol, int endCol,
			Map<String, Map<String, String>> feildMapping,
			Map<String, String[]> labelMapping) {
		Cell cell = sheet.getCell(startCol, 0);
		String label = getCellValue(cell);
		if (StringUtils.isEmpty(label)) {
			return null;
		}

		String firstLabel = labelMapping.isEmpty() ? "" : labelMapping.keySet()
				.iterator().next();
		String[] t = labelMapping.get(label);
		if (t == null) {
			return null;
		}
		String domainName = t[0];
		String fiedName = t.length > 1 ? t[1] : t[0];
		MergedHeader header = new MergedHeader(domainName,
				labelMapping.containsKey(label) ? label : "",
				StringUtils.equals(firstLabel, label) ? "" : "$" + fiedName,
				startCol, endCol);
		if (!labelMapping.containsKey(label)) {
			return header;
		}
		Class clazz = baseService.getDomainClass(domainName);
		for (int i = startCol; i <= endCol; i++) {
			Cell subCell = sheet.getCell(i, 1);
			String subCelllabel = getCellValue(subCell);
			if (!feildMapping.get(label).containsKey(subCelllabel)) {
				continue;
			}
			String p = feildMapping.get(label).get(subCelllabel);
			try {
				String[] tmp = p.split("\\.");
				Field f = ReflectUtils.getField(clazz, tmp[0]);
				RepresentationField representationField = f
						.getAnnotation(RepresentationField.class);
				Header h;
				if (representationField != null) {
					if (representationField.view() == RepresentationFieldType.DATE) {
						h = new DateHeader(date, subCelllabel, p, i);
					} else if (representationField.view() == RepresentationFieldType.DATETIME) {
						h = new DateHeader(timestamp, subCelllabel, p, i);
					} else if (representationField.view() == RepresentationFieldType.TIME) {
						h = new DateHeader(time, subCelllabel, p, i);
					} else if (representationField.view() == RepresentationFieldType.BOOLEAN
							&& f.getAnnotation(BooleanValue.class) != null) {
						h = new BooleanHeader(
								f.getAnnotation(BooleanValue.class),
								subCelllabel, p, i);
					} else {
						h = new Header(subCelllabel, p, i);
					}
				} else {
					h = new Header(subCelllabel, p, i);
				}

				if (f.getAnnotation(NotNull.class) != null) {
					h.isNotNull = true;
				}
				header.addSub(h);
			} catch (Exception e) {
				// TODO: handle exception
				header.addSub(new Header(subCelllabel, p, i));
			}

		}
		return header;
	}

	private List<MergedHeader> getHeader(Sheet sheet, String domainName) {
		Map<String, List<FormField>> formFieldMap = getAllField(domainName);
		String domainLabel = this.getDomainLabel(domainName);
		List<FormField> formFields = formFieldMap.get(domainName);
		Map<String, Map<String, String>> feildMapping = new LinkedHashMap<String, Map<String, String>>();
		Map<String, String[]> labelMapping = new LinkedHashMap<String, String[]>();
		Map<String, String> map = new HashMap<String, String>();
		feildMapping.put(domainLabel, map);
		labelMapping.put(domainLabel, new String[] { domainName });
		for (FormField formField : formFields) {
			if (formField.isSubImport()) {
				String subDomainName = formField.getType().getRefDomainName();
				String subDomainLabel = this.getDomainLabel(subDomainName);
				labelMapping.put(subDomainLabel, new String[] { subDomainName,
						formField.getName() });
				Map<String, String> subMap = new HashMap<String, String>();
				feildMapping.put(subDomainLabel, subMap);
				for (FormField f : formFieldMap.get(subDomainName)) {
					fillFieldMapping(subMap, subDomainName, f);
				}
			}
			fillFieldMapping(map, domainName, formField);
		}

		List<MergedHeader> headers = new ArrayList<MergedHeader>();
		Range[] ranges = sheet.getMergedCells();
		for (Range range : ranges) {
			Cell topLeft = range.getTopLeft();
			Cell bottomright = range.getBottomRight();
			if (topLeft.getRow() != 0 || bottomright.getRow() != 0) {
				continue;
			}
			headers.add(createHeader(sheet, topLeft.getColumn(),
					bottomright.getColumn(), feildMapping, labelMapping));
		}
		Collections.sort(headers);
		int s = -1;
		if (!headers.isEmpty() && headers.get(0).col > 0) {
			s = headers.get(0).col;
		}
		for (int i = s; i >= 0; i--) {
			headers.add(0,
					createHeader(sheet, i, i, feildMapping, labelMapping));
		}
		s = 0;
		if (!headers.isEmpty()) {
			Header last = headers.get(headers.size() - 1);
			if (last instanceof MergedHeader) {
				s = ((MergedHeader) last).endCol + 1;
			} else {
				s = last.col + 1;
			}
		}
		for (int i = s; i < sheet.getColumns(); i++) {
			MergedHeader header = createHeader(sheet, i, i, feildMapping,
					labelMapping);
			if (header == null) {
				continue;
			}
			headers.add(header);
		}
		if (!headers.isEmpty()) {
			Iterator<Header> it = new ArrayList(headers).iterator();
			Header current = it.next();
			while (it.hasNext()) {
				Header next = it.next();
				int start = current.col;
				if (current instanceof MergedHeader) {
					start = ((MergedHeader) current).endCol + 1;
				}
				int end = next.col;
				current = next;
				if (start >= end) {
					continue;
				}
				for (int i = start; i < end; i++) {
					headers.add(createHeader(sheet, i, i, feildMapping,
							labelMapping));
				}
			}
		}
		MergedHeader mainHeader = null;
		for (int i = 0; i < headers.size(); i++) {
			MergedHeader h = headers.get(i);
			if (StringUtils.isEmpty(h.label)) {
				headers.remove(i);
				i--;
			} else if (domainLabel.equals(h.label)) {
				mainHeader = h;
			}
		}
		Collections.sort(headers);
		if (mainHeader != null) {
			if (headers.size() > 1) {
				List<String> groups = new ArrayList<String>();
				for (Header h : mainHeader.subHeaders) {
					groups.add(h.property);
				}
				mainHeader.groups = groups.toArray(new String[0]);
			}
		} else {
			headers = new ArrayList<MergedHeader>();
		}

		return headers;
	}

	private static final SimpleDateFormat GMT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	static {
		GMT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private String getCellValue(Cell cell) {
		if (cell == null) {
			return null;
		}
		if (cell.getType().equals(CellType.DATE)) {
			DateCell dc = (DateCell) cell;
			Date d = dc.getDate();
			return GMT.format(d);
		} else if (cell.getType().equals(CellType.NUMBER)) {
			NumberCell mc = (NumberCell) cell;
			return new BigDecimal(mc.getValue()).toString().replaceAll(
					"\\.0+$", "");
		} else {
			return StringUtils.isBlank(cell.getContents()) ? null : cell
					.getContents().trim();
		}
	}

	private DataIterator iterator(final WritableSheet sheet,
			final List<MergedHeader> headers) {
		final List<String> subProperies = new ArrayList<String>();
		MergedHeader mainHeader = null;
		for (MergedHeader header : headers) {
			if (StringUtils.isEmpty(header.property)) {
				mainHeader = header;
				continue;
			}
			if (header.property.startsWith("$")) {
				subProperies.add(header.property.substring(1));
			}
		}

		final String[] groups;
		if (headers.size() > 1) {
			if (mainHeader != null) {
				groups = mainHeader.groups == null ? new String[0]
						: mainHeader.groups;
			} else {
				groups = new String[0];
			}
		} else {
			groups = new String[0];
		}

		final String[] busKeys;
		if (mainHeader != null) {
			Class clazz = baseService.getDomainClass(mainHeader.domainName);
			List<String> bkList = getBusinessKey(mainHeader.domainName, clazz);
			if (null != bkList) {
				busKeys = bkList.toArray(new String[0]);
			} else {
				busKeys = new String[0];
			}
		} else {
			busKeys = new String[0];
		}
		return new DataIterator() {
			private int index = 2;
			private Set<String> keySet = new HashSet<String>();

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return sheet.getRows() > index;
			}

			@Override
			public Map<String, Object> next() {
				// TODO Auto-generated method stub
				Map<String, Object> map = toMap();
				index++;
				if (map.isEmpty()) {
					return map;
				}
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				list.add(map);
				if (busKeys.length > 0) {
					String busKey = getBusKey(map);
					if (hasNext()) {
						while (hasNext()) {
							Map<String, Object> t = toMap();
							if (busKey.equals(getBusKey(t))) {
								list.add(t);
								index++;
							} else {
								break;
							}
						}
					}
					if (keySet.contains(busKey)) {
						List<Object> err = new ArrayList<Object>();
						for (Object k : busKeys) {
							err.add(map.get(k));
						}
						throw new RuntimeException(StringUtils.join(err, ",")
								+ "已经存在！");
					}
					keySet.add(busKey);
				}

				for (MergedHeader header : headers) {
					for (Header h : header.subHeaders) {
						if (!h.isNotNull) {
							continue;
						}
						for (Map m : list) {
							if (getMap(m, h.getProperties()) == null) {
								if (h instanceof BooleanHeader) {
									throw new RuntimeException(
											StringUtils.join(h.getLables(),
													"->")
													+ "只能为["
													+ StringUtils
															.join(((BooleanHeader) h).booleanValue
																	.value(),
																	",") + "]！");
								} else if (h instanceof DateHeader) {
									SimpleDateFormat format = ((DateHeader) h).format;
									throw new RuntimeException(
											StringUtils.join(h.getLables(),
													"->")
													+ "格式为["
													+ format.toPattern() + "]");
								} else {
									throw new RuntimeException(
											StringUtils.join(h.getLables(),
													"->") + "不能为空！");
								}
							}

						}

					}
				}

				if (groups.length > 0 && list.size() > 1) {
					Map first = list.get(0);
					String groupKey = getGroupKey(first);

					for (Map<String, Object> m : list) {
						if (!groupKey.equals(getGroupKey(m))) {
							m.keySet().removeAll(subProperies);
							StringBuffer err = new StringBuffer();
							for (String k : groups) {
								String[] keys = k.split("\\.");
								String v1 = getMap(first, keys);
								String v2 = getMap(m, keys);
								if (!StringUtils.equals(v1, v2)) {
									err.append(v1).append("与").append(v2)
											.append("值不相等;");
								}
							}
							throw new RuntimeException(err.toString());
						}
					}
				}

				map = new HashMap<String, Object>(list.get(list.size() - 1));
				map.keySet().removeAll(subProperies);
				for (Map m : list) {
					for (String s : subProperies) {
						List subList;
						if (map.containsKey(s)) {
							subList = (List) map.get(s);
						} else {
							subList = new ArrayList();
							map.put(s, subList);
						}
						if (m.containsKey(s)) {
							subList.add(m.get(s));
						}
					}
				}
				return map;
			}

			private String getBusKey(Map map) {
				List<String> values = new ArrayList<String>();
				for (String s : busKeys) {
					Object obj = getMap(map, s.split("\\."));
					values.add(obj == null ? "" : obj.toString());
				}
				return StringUtils.join(values, "#");
			}

			private String getGroupKey(Map map) {
				List<String> values = new ArrayList<String>();
				for (String s : groups) {
					Object obj = getMap(map, s.split("\\."));
					values.add(obj == null ? "" : obj.toString());
				}
				return StringUtils.join(values, "#");
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException();
			}

			private Map<String, Object> toMap() {
				Map<String, Object> map = new HashMap<String, Object>();
				for (MergedHeader header : headers) {
					for (Header h : header.subHeaders) {
						setMap(map, h,
								getCellValue(sheet.getCell(h.col, index)));
					}
				}
				return map;
			}

			private String getMap(Map map, String[] keys) {
				String value = null;
				int end = keys.length - 1;
				Map m = map;
				for (int i = 0; i < end; i++) {
					String key = keys[i];
					if (key.startsWith("$")) {
						key = key.substring(1);
					}
					if (m.containsKey(key)) {
						m = (Map) m.get(key);
					} else {
						return null;
					}
				}
				try {
					return (String) m.get(keys[end]);
				} catch (Exception e) {
					// TODO: handle exception
					return null;
				}

			}

			private void setMap(Map map, Header h, String value) {
				if (StringUtils.isEmpty(value)) {
					return;
				}
				String[] keys = h.getProperties();
				BooleanValue booleanValue = null;
				SimpleDateFormat format = null;
				if (h instanceof BooleanHeader) {
					booleanValue = ((BooleanHeader) h).booleanValue;
				} else if (h instanceof DateHeader) {
					format = ((DateHeader) h).format;
				}
				int end = keys.length - 1;
				Map m = map;
				for (int i = 0; i < end; i++) {
					String key = keys[i];
					if (key.startsWith("$")) {
						key = key.substring(1);
					}
					if (m.containsKey(key)) {
						m = (Map) m.get(key);
					} else {
						Map t = new HashMap();
						m.put(key, t);
						m = t;
					}
				}
				if (booleanValue != null) {
					if (StringUtils.equals(value, booleanValue.value()[0])) {
						value = "true";
					} else if (StringUtils.equals(value,
							booleanValue.value()[1])) {
						value = "false";
					} else {
						value = null;
					}
				} else if (format != null) {
					Date d = null;
					try {
						d = timestamp.parse(value);
					} catch (Exception e) {
						// TODO: handle exception
						try {
							d = format.parse(value);
						} catch (Exception e2) {
							// TODO: handle exception
						}
					}
					if (d == null) {
						value = null;
					} else {
						value = timestamp.format(d);
					}
				}
				m.put(keys[end], value);
			}

			@Override
			public int getIndex() {
				// TODO Auto-generated method stub
				return index;
			}
		};
	}

	private List<String> getBusinessKey(String domainName, Class clazz) {
		UpdateImport updateImport = (UpdateImport) clazz
				.getAnnotation(UpdateImport.class);
		List<String> bkList = null;
		if (updateImport != null && StringUtils.isNotEmpty(updateImport.by())) {
			bkList = Arrays.asList(updateImport.by().split(","));
		} else {
			bkList = baseService.getBusinessKey(domainName);
		}
		return bkList;
	}
}

interface DataIterator extends Iterator<Map<String, Object>> {
	int getIndex();
}

class CellConfig {
	public final WritableCellFormat normalCellFormat;
	public final WritableCellFormat requiredCellFormat;
	public final WritableCellFormat parentCellFormat;
	public final WritableCellFormat warningCellFormat;
	public final CellView cellView = new CellView();

	public CellConfig() throws Exception {
		// TODO Auto-generated method stub
		// cellView.setAutosize(true); // 设置自动大小

		WritableFont normalFont = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK);
		normalCellFormat = new WritableCellFormat(normalFont);
		normalCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		normalCellFormat.setShrinkToFit(true);
		normalCellFormat.setAlignment(Alignment.CENTRE);

		WritableFont requiredFont = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.RED);
		requiredCellFormat = new WritableCellFormat(requiredFont);
		requiredCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		requiredCellFormat.setShrinkToFit(true);
		requiredCellFormat.setAlignment(Alignment.CENTRE);

		WritableFont parentFont = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.DARK_YELLOW);
		parentCellFormat = new WritableCellFormat(parentFont);
		parentCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		parentCellFormat.setShrinkToFit(true);
		parentCellFormat.setAlignment(Alignment.CENTRE);

		// 定义错误信息单元格样式
		WritableFont warning = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.DARK_RED);
		warningCellFormat = new WritableCellFormat(warning);
		warningCellFormat.setBackground(Colour.GRAY_25);
		warningCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		warningCellFormat.setShrinkToFit(true);
	}
}

class Header implements Comparable<Header> {
	final String label;
	final String property;
	final int col;
	boolean isNotNull = false;
	Header parent;

	public Header(String label, String property, int col) {
		this.label = label;
		this.property = property;
		this.col = col;
	}

	private String[] labels = null;

	public String[] getLables() {
		if (this.labels == null) {
			if (parent != null) {
				String[] keys = parent.getLables();
				this.labels = new String[keys.length + 1];
				this.labels[keys.length] = label;
				System.arraycopy(keys, 0, this.labels, 0, keys.length);
			} else if (StringUtils.isEmpty(label)) {
				this.labels = new String[0];
			} else {
				this.labels = new String[] { label };
			}
		}
		return this.labels;
	}

	private String[] properties = null;

	public String[] getProperties() {
		if (this.properties == null) {
			if (parent != null) {
				String[] properties = parent.getProperties();
				String[] arr = property.split("\\.");
				this.properties = new String[properties.length + arr.length];
				System.arraycopy(properties, 0, this.properties, 0,
						properties.length);
				System.arraycopy(arr, 0, this.properties, properties.length,
						arr.length);
			} else if (StringUtils.isEmpty(property)) {
				this.properties = new String[0];
			} else {
				this.properties = new String[] { property };
			}
		}
		return this.properties;
	}

	@Override
	public int compareTo(Header o) {
		// TODO Auto-generated method stub
		return this.col - o.col;
	}
}

class BooleanHeader extends Header {
	final BooleanValue booleanValue;

	public BooleanHeader(BooleanValue booleanValue, String label,
			String property, int col) {
		super(label, property, col);
		this.booleanValue = booleanValue;
	}

}

class DateHeader extends Header {
	final SimpleDateFormat format;

	public DateHeader(SimpleDateFormat format, String label, String property,
			int col) {
		super(label, property, col);
		this.format = format;
	}

}

class MergedHeader extends Header {
	String[] groups;
	String[] buzKeys;
	List<Header> subHeaders = new ArrayList<Header>();
	final int endCol;
	final String domainName;

	public MergedHeader(String domainName, String label, String property,
			int startCol, int endCol) {
		super(label, property, startCol);
		this.endCol = endCol;
		this.domainName = domainName;
	}

	public void addSub(Header header) {
		if (header == null) {
			System.err.println("header is null!");
			return;
		}
		if (this.col <= header.col && this.endCol >= header.col) {
			header.parent = this;
			subHeaders.add(header);
		} else {
			System.err.println("header out of bounds parent!");
		}
	}
}

class ImportResult {
	private Boolean success;

	private String errorFileName;

	private String resultInfo;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getErrorFileName() {
		return errorFileName;
	}

	public void setErrorFileName(String errorFileName) {
		this.errorFileName = errorFileName;
	}

	public String getResultInfo() {
		return resultInfo;
	}

	public void setResultInfo(String resultInfo) {
		this.resultInfo = resultInfo;
	}

}
