var ChatModule = (function (my) {
	var chatId = null;
	var author = null;
	var userId = null;
	var userPhotoUrl = null;
	var tooltip = null;
	var unreadMessages = [];
	var unreadMessagesCount = 0;
	var messageCountContainer = $('#inboxMessagesCount');
	var messageCount = 0;
	
	var my = function(params) {
		chatId = params.chatId;
		author = params.author;
		userId = params.userId;
		userPhotoUrl = params.userPhotoUrl;
		
		$('#send-message').click(function() {
			my.sendReplyMessage();
		});
		
		$('#chat_message_topic_placeholder').editable(baseUrl + '/chat/setTopic', {
			submitdata : {"chatId" : chatId},
			tooltip : 'Click to change topic',
			width: 200
		});
		
		$('#chat_message_topic_placeholder').click(function(){
			$('#chat_message_topic_switch_container').hide();
		});
		
		my.loadMessages();
		my.parseNumberOfMessages();
	};
	
	my.loadMessages = function() {
		if (!chatId) {
			return;
		}
		jQuery.ajax({
			type: 'POST',
			url: baseUrl + "/chat/getMessages",
			data: "chatId="+chatId,
			success: function(data, textStatus){
				if (data.error) {
					jAlert(data.error);
					return;
				}
				if (!data.messages) {
					return;
				}
				$.each(data.messages, function (index) {
					if (this.dateUpdated <= data.lastReadAt) {
						this.rowClass = 'chat_message_read';
					} else {
						this.rowClass = 'chat_message_unread';
					}
					this.userId = userId;
					this.relativeTime = ThishoodStream.relativeTime(this.dateCreated);
					this.rowIndex = index;
					$("#chatMessageTemplate").tmpl(this).appendTo($('#previousMessagesTable').find('tbody'));
					unreadMessages.push('#messageRow'+index);
				});
				unreadMessagesCount = unreadMessages.length; 
				setTimeout(my.markNextAsRead, 2000);
			},
			error: function(XMLHttpRequest, textStatus, errorThrown){
				jAlert('Error occured');
			}
		});
		return false;
	};
	
	my.sendReplyMessage = function() {
		if ($("#form-send-message").validate({
			'errorPlacement' : function (error, element) {
				element.before(error);
			}
		}).form()) {
			$("#form-send-message").ajaxSubmit({
				'success' : function (responseText, statusText) {
					if (responseText.errors) {
						GroupSettingsModule.processErrors(responseText.errors);
					} else {
						var newMsg = {
								content: $("#form-send-message #message").val(),
								relativeTime: ThishoodStream.relativeTime(responseText.message.lastUpdated),
								rowClass : 'chat_message_read',
								authorId : userId,
								userId : userId,
								authorPhotoUrl : userPhotoUrl,
								authorName : author
						};
						$("#chatMessageTemplate").tmpl(newMsg).appendTo($('#previousMessagesTable').find('tbody'));
						$("#form-send-message #message").val('');
						$.each($('.chat_message_unread'), function() {
							$(this).removeClass('chat_message_unread').addClass('chat_message_read');
						});
						my.decreaseNumberOfMessages(unreadMessagesCount);
					}
				},
				'failure' : function (responseText, statusText) {
					jAlert('Failed! Try again.');
				}
			});
		};
	};
	
	my.showAuthorTooltip = function(userId) {
		if (!userId) {
			return;
		}
		if (!tooltip) {
			tooltip = new ThishoodTooltip();
		}
		tooltip.showUrl(this.id, baseUrl + '/stream/getUser?userId='+encodeURIComponent(userId));
	};
	
	my.markNextAsRead = function() {
		if (unreadMessages && unreadMessages.length > 0) {
			var nextRow = unreadMessages.shift();
			$(nextRow).removeClass('chat_message_unread').addClass('chat_message_read');
			my.decreaseNumberOfMessages();
			setTimeout(my.markNextAsRead, 2000);
		}
	};
	
	my.parseNumberOfMessages = function () {
		if (messageCountContainer) {
			var currentCount = (/\((\d*)\)/g).exec(messageCountContainer.html());
			if (currentCount != null && currentCount[1]) {
				messageCount = currentCount[1];
			}
		}
	};
	
	my.decreaseNumberOfMessages = function(amount) {
		my.parseNumberOfMessages();
		if (!amount) {
			amount = 1;
		}
		messageCount = messageCount - amount;
		unreadMessagesCount = unreadMessagesCount - amount; 
		$(messageCountContainer).html('('+messageCount+')')
	};
	
	return my;
}(ChatModule || {}));