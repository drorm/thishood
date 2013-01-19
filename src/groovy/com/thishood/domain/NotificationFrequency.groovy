package com.thishood.domain

import com.thishood.domain.StringValuedEnum

/**
 * How often notifications might be sent
 */
enum NotificationFrequency implements StringValuedEnum {
    RIGHT_AWAY("RIGHT_AWAY"),
    //once per hour
    HOURLY("HOURLY"),
    //three time a day == once per 8 hours
    THREE_PER_DAY("THREE_PER_DAY"),
    //once a day
    DAILY("DAILY"),
    //every other day
    ONCE_PER_2DAYS("ONCE_PER_2DAYS"),
    //once a week
    WEEKLY("WEEKLY");

    final String value

    NotificationFrequency(String value) {
        this.value = value
    }

    static list() {
        [RIGHT_AWAY, HOURLY, THREE_PER_DAY, DAILY, ONCE_PER_2DAYS, WEEKLY]
    }

}
