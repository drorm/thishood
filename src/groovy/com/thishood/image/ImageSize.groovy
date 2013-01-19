package com.thishood.image

public enum ImageSize {
	THUMB(30, null),
	TINY(50, null),
	SMALL(130, null),
	ORIGINAL(null, null)

	private Integer width
	private Integer height

	ImageSize(Integer width, Integer height) {
		this.width = width
		this.height = height
	}

	Integer getWidth() {
		width
	}

	Integer getHeight() {
		height
	}
}