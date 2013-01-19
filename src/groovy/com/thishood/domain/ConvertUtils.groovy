package com.thishood.domain

class ConvertUtils {
    static Long toLong(def value) {
        def result
        try {
            result = value ? value.toLong() : null
        } catch (NumberFormatException ignore) {
        }
        result
    }
}
