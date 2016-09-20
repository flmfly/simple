package simple.core.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileService {

	@Value("${file.save.root.path}")
	private String rootPath;

	public String saveFile(InputStream in, String subFolder, String fileName,
			String category) throws IOException {
		String folder = rootPath
				+ (StringUtils.isBlank(category) ? ""
						: (File.separator + category)) + File.separator
				+ subFolder;
		checkFolderExist(folder);

		String realPath = folder + File.separator + fileName;

		FileUtils.copyInputStreamToFile(in, new File(realPath));
		return realPath;
	}

	private void checkFolderExist(String path) {
		File folder = new File(path);
		if (!folder.exists() || !folder.isDirectory()) {
			folder.mkdirs();
		}
	}

	public File getFile(String path, String category) {
		if (StringUtils.isNotBlank(category)) {
			return new File(rootPath + File.separator + category
					+ File.separator + path);
		}
		return new File(rootPath + File.separator + path);
	}

}
