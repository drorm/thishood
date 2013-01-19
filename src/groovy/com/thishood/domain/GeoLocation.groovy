package com.thishood.domain

class GeoLocation {
    BigDecimal latitude
    BigDecimal longitude

    static constrains = {
        latitude range: -90..+90, scale: 6
        longitude range: -90..+90, scale: 6
    }
}
