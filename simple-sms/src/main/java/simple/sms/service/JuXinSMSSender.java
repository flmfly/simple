package simple.sms.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import simple.core.exception.SMSSendFailedException;
import simple.core.service.BaseService;
import simple.core.service.SMSSender;
import simple.core.util.MD5Utils;
import simple.sms.model.MessageSmsLog;

@Component
public class JuXinSMSSender implements SMSSender {

	private static final String POST_URL = "http://api.app2e.com/smsBigSend.api.php";

	private static final String USER_NAME = "bjysckj";

	private static final String PASSWORD = "SMVlvl9L";

	private static final String MD5_PASSWORD = MD5Utils.digest(PASSWORD);

	@Autowired
	protected BaseService baseService;

	public String send(String number, String message)
			throws SMSSendFailedException {
		URL url = null;
		HttpURLConnection http = null;
		MessageSmsLog log = new MessageSmsLog();
		log.mobile = number;
		log.message = message;
		try {
			String result = "";
			url = new URL(POST_URL);
			http = (HttpURLConnection) url.openConnection();
			http.setDoInput(true);
			http.setDoOutput(true);
			http.setUseCaches(false);
			http.setConnectTimeout(50000);// 设置连接超时
			// 如果在建立连接之前超时期满，则会引发一个
			// java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
			http.setReadTimeout(50000);// 设置读取超时
			// 如果在数据可读取之前超时期满，则会引发一个
			// java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
			http.setRequestMethod("POST");
			// http.setRequestProperty("Content-Type","text/xml; charset=UTF-8");
			http.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			http.connect();

			// pwd=a0286f4e5f157df5d16284b47a574d34&username=bjysckj&p=13717756822&msg=%E6%B5%8B%E8%AF%95%E9%AA%8C%E8%AF%81%E7%A0%81+112abc&charSetStr=utf

			String param = "pwd=" + MD5_PASSWORD + "&username=" + USER_NAME
					+ "&p=" + number + "&isUrlEncode=no&charSetStr=utf&msg="
					+ message;

			OutputStreamWriter osw = new OutputStreamWriter(
					http.getOutputStream(), "utf-8");
			osw.write(param);
			osw.flush();
			osw.close();

			if (http.getResponseCode() == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						http.getInputStream(), "utf-8"));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					result += inputLine;
				}
				in.close();
				// result = "["+result+"]";
			}

			System.out.println(result);
			log.sendState = true;
			return "发送成功！";
		} catch (Exception e) {
			log.sendState = false;
			return "发送失败！";
		} finally {
			if (http != null)
				http.disconnect();
			log.sendTime = new Date();
			// TODO mid
			this.baseService.save(log);
		}
	}

	public String bulkSend(Set<String> numberSet, String message)
			throws SMSSendFailedException {
		// TODO Auto-generated method stub
		return "";
	}
}
