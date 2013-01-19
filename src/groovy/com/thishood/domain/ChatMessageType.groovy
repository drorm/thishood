package com.thishood.domain

/**
 * @see Chat
 */
enum ChatMessageType {
	SYSTEM,
	COMMON

    static list() {
        [SYSTEM, COMMON]
    }

}
