package simple.core.controller;

//import java.util.Set;
//
//import javax.validation.ConstraintViolation;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import simple.config.annotation.util.CalendarUtils;
import simple.core.Constants;
import simple.core.model.DomainDesc;
import simple.core.model.FileMeta;
import simple.core.service.BaseService;
import simple.core.service.BaseService.Callback;
import simple.core.service.FileService;
import simple.core.service.HttpSessionService;
import simple.core.support.ImportHandler;
import eu.bitwalker.useragentutils.Browser;

/**
 * The Class CommonRestController.
 * 
 * @author Jeffrey
 */
@Controller
@RequestMapping(Constants.FUNCTION_API_PREFIX)
public class FunctionController {

	@Autowired
	protected FileService fileService;

	@Autowired
	protected ImportHandler importHandler;

	@Autowired
	protected BaseService baseService;

	@Autowired
	private HttpSessionService httpSessionService;

	/** The Constant logger. */
	static final Logger logger = Logger.getLogger(FunctionController.class);

	@RequestMapping(value = "/{domainName}/file", method = RequestMethod.GET)
	public void file(@RequestParam("path") String path,
			HttpServletRequest request, HttpServletResponse resp,
			@RequestParam("fileName") String fileName,
			@RequestParam("category") String category) throws IOException {
		// File file = new File(path);
		File file = fileService.getFile(path, category);
		resp.reset();

		MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

		String lowerPath = path.toLowerCase();
		boolean isImg = false;
		if (lowerPath.endsWith(".png")) {
			mediaType = MediaType.IMAGE_PNG;
			isImg = true;
		} else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
			mediaType = MediaType.IMAGE_JPEG;
			isImg = true;
		} else if (lowerPath.endsWith(".gif")) {
			mediaType = MediaType.IMAGE_GIF;
			isImg = true;
		}	

		resp.setContentType(mediaType.toString());
		resp.setContentLength((int) file.length());
		if (null != fileName) {
			resp.setHeader("Content-disposition", "attachment; filename=\""
					+ fileName + "\"");
		}
		if (isImg && file.exists()) {// 图片显示添加浏览器端的缓存
			String if_none_match = request.getHeader("if-none-match");
			if (StringUtils.equals(if_none_match, file.lastModified() + "")) {
				resp.setStatus(304);
				return;
			}
			resp.addHeader("Etag", file.lastModified() + "");
		}
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				file));
		FileCopyUtils.copy(in, resp.getOutputStream());
		resp.flushBuffer();
	}

	@RequestMapping(value = "/htmleditor/file", method = RequestMethod.GET)
	public void htmlEditorFile(@RequestParam("path") String path,
			HttpServletResponse resp, @RequestParam("category") String category)
			throws IOException {
		// File file = new File(path);
		File file = fileService.getFile(path, category);
		resp.reset();

		MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

		String lowerPath = path.toLowerCase();

		if (lowerPath.endsWith(".png")) {
			mediaType = MediaType.IMAGE_PNG;
		} else if (lowerPath.endsWith(".jpg")) {
			mediaType = MediaType.IMAGE_JPEG;
		} else if (lowerPath.endsWith(".gif")) {
			mediaType = MediaType.IMAGE_GIF;
		}

		resp.setContentType(mediaType.toString());
		resp.setContentLength((int) file.length());
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				file));

		// final HttpHeaders headers = new HttpHeaders();
		// headers.setContentType(mediaType);

		FileCopyUtils.copy(in, resp.getOutputStream());

		resp.flushBuffer();
		// }

		// return ResponseEntity.ok().contentLength(file.length())
		// .contentType(mediaType)
		// .body(new InputStreamResource(new FileInputStream(file)));
	}

	@RequestMapping(value = "/htmleditor/upload", method = RequestMethod.POST)
	public @ResponseBody
	String htmlEditorUpload(MultipartHttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("category") String category,
			@RequestParam("CKEditorFuncNum") String callback) {
		FileMeta fileMeta = null;
		Iterator<String> itr = request.getFileNames();
		MultipartFile mpf = null;

		String message = null;

		if (itr.hasNext()) {
			mpf = request.getFile(itr.next());
			fileMeta = new FileMeta();
			fileMeta.setFileName(mpf.getOriginalFilename());
			fileMeta.setSize(mpf.getSize());
			fileMeta.setType(mpf.getContentType());

			try {
				String fileName = mpf.getOriginalFilename();
				String ext = "";
				if (fileName.lastIndexOf(".") > -1) {
					ext = fileName.substring(fileName.lastIndexOf("."),
							fileName.length());
				}

				String subFolder = SDF.format(CalendarUtils.getCurrentDate());

				String savedFileName = (System.currentTimeMillis() + Thread
						.currentThread().getName()).hashCode() + ext;

				this.fileService.saveFile(mpf.getInputStream(), subFolder,
						savedFileName, category);
				fileMeta.setUrl(subFolder + File.separator + savedFileName);
			} catch (IOException e) {
				e.printStackTrace();

				message = e.getMessage();

			}
		}

		String rtnInfo = "";
		if (null == message && null != fileMeta) {
			rtnInfo = request.getContextPath()
					+ "/func/api/htmleditor/file?category=htmlEditor&path="
					+ fileMeta.getUrl();
		} else {
			rtnInfo = message;
		}

		StringBuffer sb = new StringBuffer();

		sb.append("<script type=\"text/javascript\">");
		sb.append("window.parent.CKEDITOR.tools.callFunction(" + callback
				+ ",'" + rtnInfo + "','')");
		sb.append("</script>");

		return sb.toString();
	}

	@RequestMapping(value = "/{domainName}/upload", method = RequestMethod.POST)
	public @ResponseBody
	LinkedList<FileMeta> upload(MultipartHttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("category") String category) {
		LinkedList<FileMeta> files = new LinkedList<FileMeta>();
		FileMeta fileMeta = null;
		// 1. build an iterator
		Iterator<String> itr = request.getFileNames();
		MultipartFile mpf = null;

		// 2. get each file
		while (itr.hasNext()) {

			// 2.1 get next MultipartFile
			mpf = request.getFile(itr.next());

			// 2.2 if files > 10 remove the first from the list
			if (files.size() >= 10)
				files.pop();

			// 2.3 create new fileMeta
			fileMeta = new FileMeta();
			fileMeta.setFileName(mpf.getOriginalFilename());
			fileMeta.setSize(mpf.getSize());
			fileMeta.setType(mpf.getContentType());

			try {

				String fileName = mpf.getOriginalFilename();

				String ext = "";
				if (fileName.lastIndexOf(".") > -1) {
					ext = fileName.substring(fileName.lastIndexOf("."),
							fileName.length());
				}

				String subFolder = SDF.format(CalendarUtils.getCurrentDate());

				String savedFileName = (System.currentTimeMillis() + Thread
						.currentThread().getName()).hashCode() + ext;

				String realPath = this.fileService.saveFile(
						mpf.getInputStream(), subFolder, savedFileName,
						category);
				fileMeta.setUrl(subFolder + File.separator + savedFileName);

				if (mpf.getContentType().equals(MediaType.IMAGE_JPEG_VALUE)
						|| mpf.getContentType().equals(
								MediaType.IMAGE_PNG_VALUE)
						|| mpf.getContentType().equals(
								MediaType.IMAGE_GIF_VALUE)) {

					BufferedImage bimg = ImageIO.read(new File(realPath));
					fileMeta.setHeight(bimg.getHeight());
					fileMeta.setWidth(bimg.getWidth());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			// 2.4 add to files
			files.add(fileMeta);
		}
		// result will be like this
		// [{"fileName":"app_engine-85x77.png","fileSize":"8 Kb","fileType":"image/png"},...]
		return files;

	}

	@RequestMapping(value = "/{domainName}/import/file/{fileName}", method = RequestMethod.GET)
	public void importFile(@PathVariable("domainName") String domainName,
			@PathVariable("fileName") String fileName,
			HttpServletResponse response, HttpServletRequest request)
			throws IOException {
		response.reset();
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		response.setHeader(
				"Content-Disposition",
				"attachment;filename="
						+ this.getAttachmentFileName(request,
								this.importHandler.getDomainLabel(domainName)
										+ "_导入错误.xls"));
		ServletOutputStream out = response.getOutputStream();
		File file = this.importHandler.getImportFile(fileName);
		try {
			IOUtils.copy(new FileInputStream(file), out);
		} catch (Exception e) {
			// ignore
		} finally {
			// if (!file.delete()) {
			// logger.warn("file:" + file.getAbsolutePath() + "delete failed!");
			// }
		}
	}

	@RequestMapping(value = "/{domainName}/template", method = RequestMethod.GET)
	public void template(@PathVariable("domainName") String domainName,
			HttpServletResponse response, HttpServletRequest request)
			throws IOException {
		response.reset();
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		response.setHeader(
				"Content-Disposition",
				"attachment;filename="
						+ this.getAttachmentFileName(request,
								this.importHandler.getDomainLabel(domainName)
										+ "_导入模版.xls"));
		ServletOutputStream out = response.getOutputStream();
		InputStream in = null;
		try {
			IOUtils.copy(
					(in = this.importHandler.getTemplateInputStream(domainName)),
					out);
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	private String getAttachmentFileName(HttpServletRequest request,
			String originName) throws UnsupportedEncodingException {
		Browser browser = Browser.parseUserAgentString(
				request.getHeader("User-Agent")).getGroup();
		if (browser == Browser.FIREFOX || browser == Browser.SAFARI) {
			return new String(originName.getBytes("utf-8"), "iso-8859-1");
		}
		return java.net.URLEncoder.encode(originName, "utf-8");
	}

	@RequestMapping(value = "/{domainName}/import", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody
	String importFile(@RequestParam("fileUpload") final MultipartFile file,
			@RequestParam("update") final boolean update,
			@PathVariable("domainName") final String domainName,
			final HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		DomainDesc desc = this.baseService.getDomainDesc(domainName);
		httpSessionService.setSession(request.getSession());
		try {
			if (desc.isBatch()) {
				return baseService.doNewTransaction(new Callback<String>() {
					@Override
					public String doAction() {
						// TODO Auto-generated method stub
						try {
							if (update) {
								return importHandler.importFile(
										file.getInputStream(), domainName,
										request, false, true);
							} else {
								return importHandler.importFile(
										file.getInputStream(), domainName,
										request, true, true);
							}
						} catch (Exception e) {
							// TODO: handle exception
							throw new RuntimeException(e.getMessage(), e);
						}

					}
				});

			} else {
				if (update) {
					return importHandler.importFile(file.getInputStream(),
							domainName, request, false);
				} else {

					return importHandler.importFile(file.getInputStream(),
							domainName, request, true);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			return e.getMessage();
		}
	}

	@RequestMapping(value = "/{domain}/export", method = RequestMethod.POST)
	public void export(
			@PathVariable(Constants.DOMAIN_VARIABLE_NAME) String domainName,
			@RequestParam String query, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.reset();
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		response.setHeader(
				"Content-Disposition",
				"attachment;filename="
						+ this.getAttachmentFileName(request,
								this.importHandler.getDomainLabel(domainName)
										+ "_导出数据.xls"));
		ServletOutputStream out = response.getOutputStream();
		try {
			this.httpSessionService.setSession(request.getSession());
			IOUtils.copy(this.importHandler.export(
					domainName,
					this.baseService.getPage(domainName, -1, -1, "", query,
							this.httpSessionService.getLoginUser()).getList()),
					out);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO
		}

	}

	private static final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd");
}
