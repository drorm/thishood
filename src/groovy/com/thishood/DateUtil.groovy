package com.thishood

class DateUtil {
	static String currentDate() {
		new Date().format("yyMMdd")
	}

	static def getLowerAndUpperByDate(Date date = new Date()) {
		Date lowerDate = date.clone()
		lowerDate.clearTime()

		Date upperDate = date.clone() + 1
		upperDate.clearTime()

		return [lowerDate, upperDate]
	}
}
