package simple.config.annotation.util;

import java.util.Calendar;
import java.util.Date;

public final class CalendarUtils {
	private CalendarUtils() {

	}

	public static Date getCurrentDateTime() {
		return Calendar.getInstance().getTime();
	}

	public static Date getCurrentDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}
}
