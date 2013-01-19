package com.thishood

class ContentAction {
	enum Group {
		/**
		 * @see Post#CREATE
		 */
		POST,
		EDIT,
		DELETE,
		SET_ADMIN
	}
	enum Post {
		/**
		 * @see Group#POST
		 */
		CREATE,
		EDIT,
		DELETE,
		/**
		 * @see Comment#CREATE
		 */
		COMMENT,
		DISABLE_COMMENTS,
		ENABLE_COMMENTS,
		/**
		 * @see Chat#CREATE
		 */
		SEND_MESSAGE
	}
	enum Comment {
		/**
		 * @see Post#COMMENT
		 */
		CREATE,
		EDIT,
		DELETE,
		/**
		 * @see Chat#CREATE
		 */
		SEND_MESSAGE
	}
	enum Chat {
		/**
		 * @see Post#SEND_MESSAGE
		 */
		CREATE,
		VIEW,
		UPDATE_TOPIC
	}
}
