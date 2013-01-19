var ThishoodStream = (function() {

	var Constructor = function(params) {

		var updateRelativeTimePeriodMs = 60000,
			getThreadCountPeriodMs = 120000;

		var pageTitle = document.title;

		var lastThreadDate = params.lastThreadDate || "";
		var currentGroupId = params.currentGroupId || "";
		var currentThreadId = params.currentThreadId || "";
		var baseUrl = params.baseUrl || "";

		var fromDate = "";
		var toDate = "";

		var that = this;

		function updateRelativeTimes() {
			var relTime = Constructor.relativeTime;
			var els = document.querySelectorAll(".isoDate");
			var len = els.length;
			var ie, el, el1;
			for (ie = 0; ie < len; ie++) {
				el = els[ie];
				el1 = document.getElementById(el.id.substring(3));
				el1.innerHTML = relTime(el.innerText);
			}
			setTimeout(updateRelativeTimes, updateRelativeTimePeriodMs);
		}

		function onAjaxError(xhr) {
			if (xhr.status == 401) {
				document.location = baseUrl + "/logout/index";
			}
		}

		function markLastNewThread(threadId) {
			$(".thStream .thread").removeClass("endNewThreads");
			$("#thread" + threadId).addClass("endNewThreads");
		}

		this.getThreads = function() {
			var stillWorking = true;
			setTimeout(function() {
				if (stillWorking) {
					var spinner = document.getElementById("streamSpinner");
					if (spinner) {
						spinner.style.display = "";
					}
				}
			}, 1000);
			$.ajax({
				url: baseUrl + "/stream/getThreads?groupId=" + currentGroupId + "&threadId=" + currentThreadId + "&toDate=" + toDate + "&r=" + Math.random(),
				success: onThreadsReceived,
				error: onAjaxError,
				complete: function() {
					stillWorking = null;
					var el = document.getElementById("streamSpinner");
					if (el) {
						el.style.display = "none";
					}
				},
				dataType: "json"
			});
		};

		function onThreadsReceived(threads) {
			var spinner = document.getElementById("streamSpinner");
			if (spinner) {
				spinner.style.display = "none";
			}
			var threadsLength = threads.length;
			if (threadsLength > 0) {
				var container = document.getElementById("threads");
				if (container) {
					$.tmpl("streamThread", threads).appendTo(container);
					var lastNewThread;
					if (!fromDate) {
						if (lastThreadDate) {
							var thread;
							for (var it in threads) {
								thread = threads[it];
								if (thread.dateUpdated <= lastThreadDate) {
									break;
								}
								$("#thread" + thread.threadId).addClass("new");
								lastNewThread = thread;
							}
							lastThreadDate = null;
						}
						fromDate = threads[0].dateUpdated;
					}
					toDate = threads[threadsLength - 1].dateUpdated;
					if (lastNewThread) {
						markLastNewThread(lastNewThread.threadId);
					}
				}
			}
			if (threadsLength < 20) {
				$("#moreButton").hide();
			}
		}

		this.getNewThreads = function() {
			suspendThreadCount();
			$(".thread.new").removeClass("new");
			$.ajax({
				url: baseUrl + "/stream/getThreads?groupId=" + currentGroupId + "&threadId=" + currentThreadId + "&fromDate=" + fromDate + "&r=" + Math.random(),
				success: onNewThreadsReceived,
				error: onAjaxError,
				dataType: "json"
			});
			scheduleThreadCount();
		};

		function onNewThreadsReceived(threads) {
			if (threads.length) {
				fromDate = threads[0].dateUpdated;
				var it, thread, replies, repliesLength;
				for (it = threads.length - 1; it >= 0; it--) {
					thread = threads[it];
					replies = thread.replies;
					repliesLength = replies.length;
					if (repliesLength) {
						// Check replies
						if ($("#reply" + replies[repliesLength - 1].replyId).length) {
							// Last reply already displayed => not new
							threads.splice(it, 1);
						} else {
							// Move whole thread to top
							$("#thread" + thread.threadId).remove();
						}
					} else if ($("#thread" + thread.threadId).length) {
						// Thread already displayed => not new
						threads.splice(it, 1);
					}
				}
				if (threads.length) {
					var container = document.getElementById("threads");
					if (container) {
						$.tmpl("streamThread", threads).prependTo(container);
						for (it in threads) {
							$("#thread" + threads[it].threadId).addClass("new");
						}
						markLastNewThread(threads[threads.length - 1].threadId);
					}
				}
			}
			updatedThreadCount = 0;
			$("#newMessagesButton").hide();
			document.title = pageTitle;
		}

		var getThreadCountTimeout, gettingThreadCount, updatedThreadCount;

		function scheduleThreadCount() {
			if (!getThreadCountTimeout) {
				getThreadCountTimeout = setTimeout(getThreadCount, getThreadCountPeriodMs);
			}
		}

		function suspendThreadCount() {
			if (getThreadCountTimeout) {
				clearTimeout(getThreadCountTimeout);
				getThreadCountTimeout = null;
			}
		}

		function getThreadCount() {
			suspendThreadCount();
			if (!gettingThreadCount) {
				gettingThreadCount = true;
				$.ajax({
					url: baseUrl + "/stream/getThreadCount?groupId=" + currentGroupId + "&threadId=" + currentThreadId + "&fromDate=" + fromDate + "&r=" + Math.random(),
					success: onThreadCountReceived,
					error: onAjaxError,
					complete: function() {
						gettingThreadCount = null;
					},
					dataType: "json"
				});
			}
			scheduleThreadCount();
		}

		function onThreadCountReceived(count) {
			updatedThreadCount = count;
			if (count > 0) {
				var title = "(" + count + ") " + pageTitle;
				if (document.title != title) {
					document.title = title;
					var newMessagesButton = $("#newMessagesButton");
					newMessagesButton.html("<span>Click to see " + count + " new message" + (count > 1 ? "s" : "") + "</span>");
					newMessagesButton.show();
				}
			}
		}

		this.getReplies = function(threadId) {
			$.ajax({
				url: baseUrl + "/stream/getReplies?threadId=" + threadId + "&r=" + Math.random(),
				success: onRepliesReceived,
				error: onAjaxError,
				dataType: "json"
			});
		};

		function onRepliesReceived(replies) {
			// replyTemplate should be compiled already
			if (replies.length) {
				var containerId = "#replies" + replies[0].threadId;
				$(containerId).html("");
				$.tmpl("streamReply", replies).appendTo(containerId);
			}
		}

		this.createThread = function(form) {
			var message = $(form.message);
			if (message.val().length) {
				document.getElementById("threadPostButton").disabled = true;
				var stillWorking = true;
				setTimeout(function() {
					if (stillWorking) {
						var spinner = document.getElementById("threadPostSpinner");
						if (spinner) {
							spinner.style.display = "";
						}
					}
				}, 1000);
				var the = this;
				$.ajax({
					url: baseUrl + "/stream/createThread",
					global: false,
					type: "POST",
					data: $(form).serialize(),
					dataType: "json",
					success: function(threads) {
						if (threads && !threads.errors) {
							var container = document.getElementById("threads");
							if (container) {
								$(form.message).val("");
								Constructor.hideForm(form.id);
								form = null;
								$.tmpl("streamThread", threads).prependTo(container);
								var it, el;
								for (it in threads) {
									el = $("#thread" + threads[it].threadId);
									el.hide();
									el.addClass("highlighted");
								}
								el = null;
								for (it in threads) {
									$("#thread" + threads[it].threadId + ":hidden").fadeIn("slow", function() {
										$(this).removeClass("highlighted");
									});
								}
								the.getNewThreads();
								the = null;
							}
						} else {
							jAlert(threads.errors, 'Error on creating thread');
						}
					},
					error: onAjaxError,
					complete: function() {
						stillWorking = null;
						var el = document.getElementById("threadPostSpinner");
						if (el) {
							el.style.display = "none";
						}
						el = document.getElementById("threadPostButton");
						if (el) {
							el.disabled = false;
						}
						el = null;
					}
				});
				message.val("");
			}
			message = null;
		};

		this.deleteThread = function(threadId) {
			jConfirm("The thread will be deleted permanently. Are you sure?", "Please confirm", function (answer) {
				if (answer) {
					deleteThread(threadId);
				}
			});
		};

		function deleteThread(threadId) {
			$.ajax({
				url: baseUrl + "/stream/deleteThread",
				global: false,
				type: "POST",
				data: "threadId=" + threadId,
				dataType: "json",
				success: function(response) {
					if (response && response.ok) {
						var el = $("#thread" + threadId);
						if (el.length) {
							el.addClass("deleted");
							el.hide("slow", function() {
								el.remove();
								el = null;
							});
						}
					} else {
						jAlert(response.errors, 'Error on deleting thread');
					}
				},
				error: onAjaxError
			});
		}

		this.disableComments = function(threadId) {
			$.ajax({
				url: baseUrl + "/stream/disableComments",
				global: false,
				type: "POST",
				data: "threadId=" + threadId,
				dataType: "json",
				success: function(response) {
					if (response && response.ok) {
						//todo re-render?
						jAlert("Need to reload page (F5) because re-rendering is not implemented", "Comments are disabled", function(){})
					}
				},
				error: onAjaxError
			});
		};

		this.enableComments = function(threadId) {
			$.ajax({
				url: baseUrl + "/stream/enableComments",
				global: false,
				type: "POST",
				data: "threadId=" + threadId,
				dataType: "json",
				success: function(response) {
					if (response && response.ok) {
						//todo re-render?
						jAlert("Need to reload page (F5) because re-rendering is not implemented", "Comments are enabled",function(){})
					}
				},
				error: onAjaxError
			});
		};

		this.createReply = function(form) {
			var reply = $(form.reply);
			if (reply.val().length) {
				var threadId = form.threadId.value;
				var postButtonId = "replyForm" + threadId + "Button";
				var postSpinnerId = "replyForm" + threadId + "Spinner";
				document.getElementById(postButtonId).disabled = true;
				var stillWorking = true;
				setTimeout(function() {
					if (stillWorking) {
						var spinner = document.getElementById(postSpinnerId);
						if (spinner) {
							spinner.style.display = "";
						}
					}
				}, 1000);
				var the = this;
				$.ajax({
					url: baseUrl + "/stream/createReply",
					global: false,
					type: "POST",
					data: "fromDate=" + fromDate + "&" + $(form).serialize(),
					dataType: "json",
					success: function(replies) {
						if (replies && !replies.errors) {
							$(form.reply).val("");
							Constructor.hideForm(form.id);
							form = null;
							var containerId = "#replies" + replies[0].threadId;
							var ir, reply, el;
							for (ir in replies) {
								reply = replies[ir];
								if (!$("#reply" + reply.replyId).length) {
									$.tmpl("streamReply", reply).appendTo(containerId);
									el = $("#reply" + reply.replyId);
									el.hide();
									el.addClass("highlighted");
								}
							}
							el = null;
							for (ir in replies) {
								$("#reply" + replies[ir].replyId + ":hidden").fadeIn("slow", function() {
									$(this).removeClass("highlighted");
								});
							}
							the.getNewThreads();
							the = null;
						} else {
							jAlert(replies.errors,"Error on create reply")
						}
					},
					error: onAjaxError,
					complete: function() {
						stillWorking = null;
						var el = document.getElementById(postSpinnerId);
						if (el) {
							el.style.display = "none";
						}
						el = document.getElementById(postButtonId);
						if (el) {
							el.disabled = false;
						}
						el = null;
					}
				});
			}
			reply = null;
		};

		this.deleteReply = function(replyId) {
			jConfirm("The reply will be deleted permanently. Are you sure?", "Please confirm", function (answer) {
				if (answer) {
					deleteReply(replyId);
				}
			});
		};

		function deleteReply(replyId) {
			$.ajax({
				url: baseUrl + "/stream/deleteReply",
				global: false,
				type: "POST",
				data: "replyId=" + replyId,
				dataType: "json",
				success: function(response) {
					if (response && response.ok) {
						var el = $("#reply" + replyId);
						if (el.length) {
							el.addClass("deleted");
							el.hide("slow", function() {
								el.remove();
								el = null;
							});
						}
					} else {
						jAlert(response.errors, "Error on removing reply")
					}
				},
				error: onAjaxError
			});
		}

/*
		function updateUsersOnline() {
			$.ajax({
				url: baseUrl + "/stream/getUsersOnline?groupId=" + currentGroupId + "&r=" + Math.random(),
				success: onUsersOnlineReceived,
				error: onAjaxError,
				dataType: "json"
			});
			setTimeout(updateUsersOnline, 60000);
		}

		function onUsersOnlineReceived(users) {
			$("#usersOnlineCount").html(users.length);
			$("#usersOnlineList").html("");
			$.tmpl("streamUserOnline", users).appendTo("#usersOnlineList");
		}
*/

		// Init
		(function() {
			$(document.getElementById("threadTemplate")).template("streamThread");
			$(document.getElementById("replyTemplate")).template("streamReply");
			$(document.getElementById("userOnlineTemplate")).template("streamUserOnline");
			that.getThreads();
			setTimeout(updateRelativeTimes, updateRelativeTimePeriodMs);
			scheduleThreadCount();
//			updateUsersOnline();
		})();

	};

	Constructor.ampRegexp = /&/g;
	Constructor.gtRegexp = />/g;
	Constructor.ltRegexp = /</g;

	Constructor.escapeHtml = function(text) {
		if (typeof text == "string") {
			var the = Constructor;
			return text.replace(the.ampRegexp, "&amp;").replace(the.gtRegexp, "&gt;").replace(the.ltRegexp, "&lt;");
		}
		return "";
	};

	Constructor.nlRegexp = /\r\n|\r|\n/g;

	Constructor.nl2br = function(text) {
		if (typeof text == "string") {
			return text.replace(Constructor.nlRegexp, "<br/>");	
		}
		return "";
	};

	Constructor.urlPatternRegexp = /((?:https?:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(?:(?:[^\s()<>]+|(?:\(?:[^\s()<>]+\)))*\))+(?:\(?:(?:[^\s()<>]+|(?:\(?:[^\s()<>]+\)))*\)|[^&=\s`!()\[\]{};:'".,<>?������]))/gi;
	Constructor.emailPatternRegexp = /(([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+)/gi;

	Constructor.parseMessage = function(msg) {
		var the = Constructor;
		msg = the.nl2br(the.escapeHtml(msg));
		msg = msg.replace(the.urlPatternRegexp, the.urlReplacer);
		msg = msg.replace(the.emailPatternRegexp, the.emailReplacer);
		return msg;
	};

	Constructor.urlSchemeRegexp = /^([\w]+:)?\/\//;

	Constructor.urlReplacer = function(str) {
		var url = Constructor.urlSchemeRegexp.test(str) ? str : "http://" + str;
		if (str.length > 60) {
			str = str.substring(0, 57) + "...";
		}
		return '<a href="' + url + '" title="' + url + '" target="_blank" ref="nofollow">' + str + '</a>';
	};

	Constructor.emailReplacer = function(str) {
		return '<a href="mailto:' + str + '">' + str + '</a>';
	};

	Constructor.isoDateRegexp = /([0-9]{4})(-([0-9]{2})(-([0-9]{2})(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(\.([0-9]+))?)?(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?/;

	Constructor.isoDateToDate = function(isoDate) {
		isoDate = new String(isoDate);
		var d = isoDate.match(Constructor.isoDateRegexp);
		var offset = 0;
		var date = new Date(d[1], 0, 1);
		if (d[3]) {date.setMonth(d[3] - 1);}
		if (d[5]) {date.setDate(d[5]);}
		if (d[7]) {date.setHours(d[7]);}
		if (d[8]) {date.setMinutes(d[8]);}
		if (d[10]) {date.setSeconds(d[10]);}
		if (d[12]) {date.setMilliseconds(Number("0." + d[12]) * 1000);}
		if (d[14]) {
			offset = Number(d[16]) * 60 + Number(d[17]);
			offset *= ((d[15] == '-') ? 1 : -1);
		}
		offset -= date.getTimezoneOffset();
		return new Date().setTime(Number(date) + offset * 60 * 1000);
	};

	Constructor.niceValue = function(val, unit) {
		val = new String(val);
		var niceVal = val;
		niceVal += " ";
		niceVal += unit;
		if (val.substring(val.length - 1) == "1") {
			if (val.substring(val.length - 2) == "11") {
				niceVal += "s";
			}
		} else {
			niceVal += "s";
		}
		return niceVal;
	};

	Constructor.relativeTime = function(isoDate) {
		var date;
		if (typeof isoDate == 'number') {
			date = isoDate;
		} else {
			date = Constructor.isoDateToDate(isoDate);
		}
		var delta = parseInt((new Date().getTime() - date) / 1000);
		if (delta < 60) {
			return "less than a minute ago";
		} else if (delta < 120) {
			return "about a minute ago";
		} else if (delta < 2700) {
			return Constructor.niceValue(Math.round(delta / 60), "minute") + " ago";
		} else if (delta < 5400) {
			return "about an hour ago";
		} else if (delta < 86400) {
			return "about " + Constructor.niceValue(Math.round(delta / 3600), "hour") + " ago";
		} else {
			return Constructor.niceValue(Math.round(delta / 86400), "day") + " ago";
		}
	};

	Constructor.showForm = function(formId) {
		var container = $("#" + formId + "Container");
		if (container.length) {
			container.show();
			container = null;
		}
		$("#" + formId + "Placeholder").hide();
		$("#" + formId).show();
		var textarea = $("#" + formId + "Textarea");
		if (!textarea.attr("autoResizable")) {
			textarea.autoResize();
			textarea.attr("autoResizable", "1");
		}
		textarea.trigger("change.dynSiz");
		textarea.focus();
		textarea = null;
	};

	Constructor.hideForm = function(formId) {
		$("#" + formId).hide();
		$("#" + formId + "Placeholder").show();
	};

	return Constructor;

})();