/**
 * Gets called from aggregate functions to calculate totals for one or several
 * columns.
 *
 * @public
 * @param {object} href that contains the URL
 * @el {Element} DOM Element to load the page into.
 */
function loadPage(obj, el) {
	var url = obj.href;
	// first display the busy indicator
	$("#" + el).html("<img class='th_centered' src='/thishood/images/spinner.gif' alt='Loading...' />");
	//and then load the page
	$("#" + el).load(url); //TODO handle existing params in here
	return (false);
}

function processLocationHash() {
	if (!location || !location.hash) {
		return;
	}
	
	if (location.hash.indexOf('#!') == 0) {
		var shortcut = location.hash.substring(2);
	}
	
	if (shortcut === 'settings') {
		changePage('/settings/');
	} else if (shortcut === 'messages' || shortcut === 'chat') {
		changePage('/chat/');
	}
};

/**
 * Diplay busy indicator
 * @public
 * @el {Element} DOM Element to put the busy indicator
 */
function displayBusy(el) {
	$("#" + el).html("<img class='th_centered' src='/thishood/images/spinner.gif' alt='Loading...' />");
}

var GroupSettingsModule = (function () {
	var Module = {};
	
	var createGroupTemplate = null;
	
	Module.init = function(){
		createGroupTemplate = $("#createGroupTemplate").template("createGroup");

		this.initButtons();		
		
		$("#form-create-group").submit(function(){
			if ($(this).validate().form()) {
				$(this).ajaxSubmit({
					success: function(objResponse) {
						if (objResponse.errors) {
							Module.processErrors(objResponse.errors);
						} else {
							Module.successHandler();
						}
					},
					failure: Module.failureHandler
				});
			}
			return false;
		});
		
		$('#joinGroupButton').click(Module.joinGroupDialog);
		
		$('#createGroupButton').click(function(){
			Module.hideHeaders();
			$('#createGroupFormContainer').show();
		});
	};
	
	Module.initButtons = function() {
		$("#create-group").button();
		$("#joinGroupButton").button();
		$("#createGroupButton").button();
		$("#create-group-cancel").button();
		$("#group-news-cancel").button();
		$("#name").focus();
	};
	
	Module.hideHeaders = function() {
		$('#groupButtons').hide();
		$('#groupsTable').hide();
		$('.headerContainer').hide();
	};
	
	Module.showHeaders = function() {
		$('#groupButtons').show();
		$('#groupsTable').show();
		$('.headerContainer').show();
	};
	
	
	Module.joinGroupDialog = function() {
		jQuery.ajax({
			type: 'POST',
			url: baseUrl + "/userGroup/join",
			success: function(data, textStatus){
				jQuery('#dialogContainer').html(data);
			},
			error: function(XMLHttpRequest, textStatus, errorThrown){
				jAlert('Error occured');
			}
		});
		return false;
	};
	
	Module.administrateGroup = function(groupId, discriminator){
		Module.hideHeaders();
		var pathContext;
		if (discriminator == 'GROUP') {
			pathContext = "Group";
		} else if (discriminator == 'HOOD') {
			pathContext = "Hood";
		} else {
			alert("Unknown discriminator " + discriminator);
		}

		$.ajax({
			'url': baseUrl + '/userGroup/edit' + pathContext + '/' + groupId,
			'success': function(responseText){
				$('#groupsTable').hide();
				$('#editGroupContainer').show();
				$('#editGroupContainer').html(responseText);
				$('#update-group-button').button();
				$('#delete-group-button').button();
				$('#update-group-button-cancel').button();
				$("#updateGroupForm").submit(function(){
					if ($(this).validate().form()) {
						$(this).ajaxSubmit({
							success: function(objResponse) {
								if (objResponse.errors) {
									Module.processErrors(objResponse.errors);
								} else {
									Module.successHandler();
								}
							},
							failure: Module.failureHandler
						});
					}
					return false;
				});
			},
			'failure': Module.failureHandler
		});
	};

	Module.setAdministrator = function(groupId){
		Module.hideHeaders();
		var pathContext;

		$.ajax({
			'url': baseUrl + '/userGroup/nonAdmins/' + groupId,
			'success': function(responseText){
				$('#groupsTable').hide();
				$('#editGroupContainer').show();
				$('#editGroupContainer').html(responseText);
				
				$('#set-admin-button').button();
				$('#set-admin-button-cancel').button();
				
				$("#setAdminGroupForm").submit(function(){
					if ($(this).validate().form()) {
						$(this).ajaxSubmit({
							success: function(objResponse) {
								if (objResponse.errors) {
									Module.processErrors(objResponse.errors);
								} else {
									Module.successHandler();
								}
							},
							failure: Module.failureHandler
						});
					}
					return false;
				});
			},
			'failure': Module.failureHandler
		});
	};

	Module.deleteGroup = function(groupId){
		jConfirm('Are you sure you want to delete this group?', 'Please confirm', function(r){
			if (r) {
				$.ajax({
					type : "POST",
					url: baseUrl + '/userGroup/delete',
					data: "group="+groupId,
					'success': function(responseText){
						Module.successHandler();
						$('#groupsTable').hide();
						$('#editGroupContainer').show();
						$('#update-group-button').button();
						$('#delete-group-button').button();
						$('#update-group-button-cancel').button();
					},
					'failure': Module.failureHandler
				});
			}
		});
	};
	
	Module.cancelCreateGroup = function(){
		Module.reloadTab();
		Module.showHeaders();
		$('#editGroupContainer').hide();
		$('#createGroupFormContainer').hide();
	};
	
	Module.showGroupNewsSettings = function(groupId){
		$.ajax({
			'url': baseUrl + '/groupNews/list/' + groupId,
			'success': function(responseText){
				$('#groupButtons').hide();
				$('#groupsTable').hide();
				$('#crudNewsContainer').show();
				$('#crudNewsContainer #crudNewsTable').html(responseText);
			},
			'failure': Module.failureHandler
		});
	};
	
	Module.leaveGroup = function(id, name){
		jConfirm("Leave the group \"" + name + "\"?", "Please confirm", function (answer) {
			if (answer) {
				url = baseUrl+"/userGroup/leaveGroup?id=" + id;
				$.ajax({
					url: url,
					success: function(){
						Module.reloadTab();
					},
					error: Module.failureHandler
				});
			}
		});
	};
	
	Module.reloadTab = function (groupId) {
		if (typeof settingsTabs != 'undefined') {
			settingsTabs.tabs("load", 4);
		} else {
			if (typeof groupId == 'undefined') {
				document.location = baseUrl;
			} else {
				document.location = baseUrl + '?group='+groupId;
			}
		}
	};
	Module.processErrors = function (errors) {
		var messages = [];
		if (typeof errors != "object") {
			//one error occured, as global problem
			jAlert(errors, "Error");
		} else {
			// list of validation errors
			$.each(errors, function (index, value) {
				var message = '';
				if (value.field) {
					message = value.field + ' ';
				}
				message = message + value.message;
				messages.push(message);
			});
			if (messages.length > 0) {
				jAlert(messages.join(), messages.length + ' error(s).');
			}
		}
	};
	Module.successHandler = function(responseText, statusText){
		Module.reloadTab();
		$('#groupsTable').show();
	};

	Module.failureHandler = function(responseText, statusText){
		jAlert('Failed!');
	};

	return Module;
})();

var HomeModule = (function () {
	var Module = {
		groupsMenuShown : false
	};
	var getMessageCountPeriodMs = 30000;
	var getMessageCountTimeout = null; 
	
	Module.init = function() {
		attachHandlers();
		getMessagesCount();
		scheduleMessagesCount();
	};
	
	var attachHandlers = function () {
/*
		$('#groupsContextMenuTrigger').hover(function(event){
			$(this).attr('src', 'images/menu/dropdown-bigger-active.png');
		},function(event){
			$(this).attr('src', 'images/menu/dropdown-bigger-inactive.png');
		});
		$('#groupsContextMenuTrigger').click(function(event){
				if (!Module.groupsMenuShown) {
					$('DIV.myGroupsContextMenu').show();
					Module.groupsMenuShown = true;
				} else {
					$('DIV.myGroupsContextMenu').hide();
					Module.groupsMenuShown = false;
				}
				event.preventDefault();
		});
		$('#groupListContainer LI IMG').click(function(event){
			event.preventDefault();
		});
		$('DIV.myGroupsContextMenu A').click(function(event){
					$('DIV.myGroupsContextMenu').hide();
					Module.groupsMenuShown = false;
		});
		$('DIV.myGroupsContextMenu').mouseleave(function(){
					$('DIV.myGroupsContextMenu').hide();
					Module.groupsMenuShown = false;
		});
		$('#grouplist').hover(function(){
			$('#groupsContextMenuTrigger').show();
		},function(){
			$('#groupsContextMenuTrigger').hide();
			$('DIV.myGroupsContextMenu').hide();
			Module.groupsMenuShown = false;
		});
*/

		/*
		$('#groupListContainer LI IMG').hover(function(event) {
				$(this).attr('src', 'images/menu/dropdown-smaller-active.png');
			}, function(event){
				$(this).attr('src', 'images/menu/dropdown-smaller-inactive.png');
			});
		
		$('#groupListContainer LI').hover(function(event) {
				$(this).find('IMG').show();
			}, function(event){
				$(this).find('IMG').hide();
			});
			*/
	};
	
	Module.initialWizard = function() {
		jQuery.ajax({
			type: 'POST',
			url: baseUrl +'/user/initialWizard',
			success: function(data, textStatus){
				jQuery('#dialogContainer').html(data);
			},
			error: function(XMLHttpRequest, textStatus, errorThrown){
			}
		});
	};
	
	Module.openNewMessageDialog = function (neighborId) {
		jQuery.ajax({
			type: 'POST',
			url: baseUrl + '/chat/createChat',
			data: {'recipientId' : neighborId },
			success: function(data, textStatus){
				jQuery('#dialogContainer').html(data);
			},
			error: function(XMLHttpRequest, textStatus, errorThrown){
				jAlert('Failed to open a new message dialog.');
			}
		});
		return false;
	};
	
	Module.openReplyMessageDialog = function (chatId) {
		if (chatId == null) {
			return false;
		}
		jQuery.ajax({
			type: 'POST',
			url: baseUrl + '/chat/chatMessages',
			data: {'chatId' : chatId},
			success: function(data, textStatus){
				jQuery('#chatDetailsContainer').html(data);
				jQuery('#messageListContainer').hide();
				jQuery('#chatDetailsContainer').show();
			},
			error: function(XMLHttpRequest, textStatus, errorThrown){
				jAlert('Failed to open a new message dialog.');
			}
		});
		return false;
	};
	
	Module.backToMyChats = function(){
		jQuery('#chatDetailsContainer').hide();
		jQuery('#messageListContainer').show();
		jQuery('#chatDetailsContainer').empty();
		return false;
	};
	
	var scheduleMessagesCount = function() {
		if (!getMessageCountTimeout) {
			getMessageCountTimeout = setTimeout(getMessagesCount, getMessageCountPeriodMs);
		}
	};
	var suspendMessageCount = function() {
		if (getMessageCountTimeout) {
			clearTimeout(getMessageCountTimeout);
			getMessageCountTimeout = null;
		}
	};
	var getMessagesCount = function() {
		suspendMessageCount();
		$.ajax({
			url: baseUrl + "/chat/getMessagesCount?r=" + Math.random(),
			success: onMessagesCountReceived,
			dataType: "json"
		});
		scheduleMessagesCount();
	};
	var onMessagesCountReceived = function (count) {
		if (count > 0) {
			$('#inboxMessagesCount').html('('+count+')');
			$('#inboxMessagesCount').fadeIn();
		} else {
			$('#inboxMessagesCount').fadeOut();
		}
	};
	
	return Module;
})();

function showHelp() {
var dialog = $.FrameDialog.create({
			url: "/home/apphelp",
			width: 700,
			height: 500,
			title: "ThisHood Help",
			buttons: {}
});
}

function initGroupTree(initiallySelect) {
	$(function () {
		$("#groupTree").jstree({
			themes: {
				theme: "apple",
				dots: false,
				icons: false
			},
			ui: {
				initially_select: [initiallySelect]
			},
			cookies: {
				save_selected: false
			},
			plugins: ["html_data", "themes", "ui", "cookies"]
		}).bind("select_node.jstree", function(e, data) {
			if (initiallySelect) {
				initiallySelect = null;
				return;
			}
			var node = data.rslt.obj;
			var link = node.children("a");
			var href = link.attr("href");
			if (href.substring(0, 1) == "/" || href.substring(0, 4) == "http") {
				document.location = href;
			}  
		});
	});
}

function initHints() {
	$("input,textarea,select,label").each(function(index) {
		var el = $(this);
		var hint = el.attr("hint");
		if (hint) {
			if (!el.attr("id")) {
				el.attr("id", "hint" + (Math.random() + "").substring(2));
			}
			el.bind("mouseenter", function() {
				hintTooltip.show($(this).attr("id"), {hint: hint});
			});
			el = null;
		}
	});
}
