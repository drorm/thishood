var ThishoodUploads = (function() {

	var Constructor = function(params) {

		var baseUrl = params.baseUrl;
		var sessionId = params.sessionId;

		var currentUploads;
		var swfupload;
		var inProgress = false;

		this.destroy = function() {
			swfupload.cancelQueue();
			swfupload = null;
		};

		function onAjaxError(xhr) {
			if (xhr.status == 401) {
				document.location = baseUrl + "/logout/index";
			}
		}

		function getUploads() {
			$.ajax({
				url: baseUrl + "/uploads/getUploads?r=" + Math.random(),
				success: onUploadsReceived,
				error: onAjaxError,
				dataType: "json"
			});
		}

		function onUploadsReceived(uploads) {
			var jq = $;
			var doc = document;
			currentUploads = uploads;
			var uploadList = doc.getElementById("uploadList");
			if (jq(".thUpload").length > 0) {
				jq("#uploadQueue .thUploadSuccess").remove();
				var len = uploads.length,
					iu, upload, elId, el;
				for (iu = 0; iu < len; iu++) {
					upload = jq.extend({}, uploads[iu]);
					elId = "upload";
					elId += upload.uploadId;
					if (!doc.getElementById(elId)) {
						upload.hide = true;
						jq.tmpl("uploadsUpload", upload).appendTo(uploadList);
						el = jq(doc.getElementById(elId));
						el.addClass("thUploadNew");
						el.show();
					}
				}
				el = null;
			} else {
				jq.tmpl("uploadsUpload", uploads).appendTo(uploadList);
			}
			uploadList = null;
		}

		function getUploadById(uploadId) {
			var uploads = currentUploads,
				len = uploads.length,
				iu, upload;
			for (iu = 0; iu < len; iu++) {
				upload = uploads[iu];
				if (upload.uploadId == uploadId) {
					return upload;
				}
			}
			return null;
		}

		this.previewUpload = function(uploadId) {
			previewUpload(uploadId);
		};

		function previewUpload(upload) {
			var jq = $;
			var doc = document;
			jq("#uploadList .thUploadNew").removeClass("thUploadNew");
			jq("#uploadList .thUploadHighlighted").removeClass("thUploadHighlighted");
			var previewEl = doc.getElementById("uploadPreview");
			previewEl.innerHTML = "";
			if (typeof upload == "number") {
				upload = getUploadById(upload);
			}
			if (upload) {
				var elId = "upload";
				elId += upload.uploadId;
				jq(doc.getElementById(elId)).addClass("thUploadHighlighted");
				jq.tmpl("uploadsPreview", upload).appendTo(previewEl);
			} else {
				previewEl.innerHTML = "Select file from the list.";
			}
		}

		this.deleteUpload = function(uploadId) {
			jConfirm("The file will be deleted permanently. Are you sure?", "Please confirm", function (answer) {
				if (answer) {
					$.ajax({
						url: baseUrl + "/uploads/deleteUpload",
						global: false,
						type: "POST",
						data: "uploadId=" + uploadId,
						dataType: "json",
						success: function(response) {
							if (response && response.ok) {
								var el = $("#upload" + uploadId);
								if (el.length) {
									el.addClass("thUploadDeleted");
									el.hide("slow", function() {
										el.remove();
										el = null;
									});
									previewUpload();
								}
							}
						},
						error: onAjaxError
					});
				}
			});
		};

		function uploadStatus(params) {
			var jq = $;
			var file = params.file;
			var fileId = file.id;
			var item = jq("#queueItem" + fileId);
			if (!item.length) {
				var fileName = file.name;
				if (fileName.length > 20) {
					fileName = fileName.substring(0, 20) + "...";
				}
				jq.tmpl("uploadsQueueItem", {
					fileId: fileId,
					filename: fileName,
					length: file.size
				}).appendTo("#uploadQueue");
				item = jq("#queueItem" + fileId);
			}
			params.success ? item.addClass("thUploadSuccess") : item.removeClass("thUploadSuccess");
			params.error ? item.addClass("thUploadError") : item.removeClass("thUploadError");
			jq("#queueItemStatus" + fileId).html(params.status);
			jq("#queueItemProgress" + fileId).css("width", params.percent ? params.percent + "%" : 0);
		}

		this.cancelUpload = function(fileId) {
			swfupload.cancelUpload(fileId);
			var el = $("#queueItem" + fileId);
			if (el.length) {
				el.addClass("thUploadDeleted");
				el.hide("slow", function() {
					el.remove();
					el = null;
				});
			}
		};

		this.use = function(uploadId) {
			if (inProgress) {
				jAlert("Please wait while finishing upload or cancel pending uploads.");
			} else {
				parent.useUpload(getUploadById(uploadId));
			}
		};

		this.crop = function(uploadId) {
			var dialog = parent.$.FrameDialog.create({
				url: baseUrl + "/uploads/crop?uploadId=" + uploadId,
				width: 624,
				height: 460,
				title: "Crop Image",
				buttons: {}
			}).bind("dialogclose", function(event, ui) {
				var uploadId = event.result;
				if (!uploadId) {
					return;
				}
				var elId = "upload";
				elId += uploadId;
				var el = document.getElementById(elId);
				if (el) {
					var upload = getUploadById(uploadId);
					if (upload) {
						upload.force = true;
						var jq = $;
						jq(el).replaceWith(jq.tmpl("uploadsUpload", upload));
						previewUpload(upload);
						upload.force = null;
					}
				}
			});
			dialog.dialog("option", "resizable", false);
			return dialog;
		};

		// Init
		(function() {
			var base = baseUrl;
			var jq = $;
			var doc = document;
			jq(doc.getElementById("uploadTemplate")).template("uploadsUpload");
			jq(doc.getElementById("queueItemTemplate")).template("uploadsQueueItem");
			jq(doc.getElementById("previewTemplate")).template("uploadsPreview");
			previewUpload();
			getUploads();
			swfupload = new SWFUpload({
				flash_url: base + "/js/swfupload/swfupload.swf",
				flash9_url: base + "/js/swfupload/swfupload_fp9.swf",
				upload_url: base + "/uploads/upload;jsessionid=" + sessionId,
				file_size_limit: "10 MB",
				file_types: "*.*",
				file_types_description: "All Files",
				file_upload_limit: 100,
				file_queue_limit: 0,
				debug: false,
				button_image_url: base + "/js/thishood/uploads/images/upload-button.png",
				button_width: "220",
				button_height: "30",
				button_placeholder_id: "uploadButton",
				swfupload_preload_handler: function() {
					if (!this.support.loading) {
						jAlert("You need the Flash Player 9.028 or above to upload files.");
						return false;
					}
				},
				swfupload_load_failed_handler: function() {
					jAlert("Something went wrong while loading swfupload. Please close this window and try again. Please report this error if it persists.");
				},
				file_queued_handler: function(file) {
					uploadStatus({
						file: file,
						status: "Pending..."
					});
				},
				file_queue_error_handler: function(file, errorCode, message) {
					var status = "";
					switch (errorCode) {
					case SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT:
						status = "File is too big";
						break;
					case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE:
						status = "File is empty";
						break;
					case SWFUpload.QUEUE_ERROR.INVALID_FILETYPE:
						status = "Invalid file";
						break;
					default:
						if (file !== null) {
							status = "Error";
						}
					}
					uploadStatus({
						file: file,
						status: status,
						error: true
					});
				},
				file_dialog_complete_handler: function(numFilesSelected, numFilesQueued) {
					this.startUpload();
				},
				upload_start_handler: function(file) {
					inProgress = true;
					uploadStatus({
						file: file,
						status: "Uploading..."
					});
					return true;
				},
				upload_progress_handler: function(file, bytesLoaded, bytesTotal) {
					var percent = Math.ceil((bytesLoaded / bytesTotal) * 100);
					uploadStatus({
						file: file,
						status: percent + "%",
						percent: percent
					});
				},
				upload_error_handler: function(file, errorCode, message) {
					var status = "";
					switch (errorCode) {
					case SWFUpload.UPLOAD_ERROR.HTTP_ERROR:
						status = "HTTP error";
						break;
					case SWFUpload.UPLOAD_ERROR.UPLOAD_FAILED:
						status = "Failed";
						break;
					case SWFUpload.UPLOAD_ERROR.IO_ERROR:
						status = "Server error";
						break;
					case SWFUpload.UPLOAD_ERROR.SECURITY_ERROR:
						status = "Security error";
						break;
					case SWFUpload.UPLOAD_ERROR.UPLOAD_LIMIT_EXCEEDED:
						status = "Upload limit exceeded";
						break;
					case SWFUpload.UPLOAD_ERROR.FILE_VALIDATION_FAILED:
						status = "Invalid file";
						break;
					case SWFUpload.UPLOAD_ERROR.FILE_CANCELLED:
						status = "Cancelled";
						break;
					case SWFUpload.UPLOAD_ERROR.UPLOAD_STOPPED:
						status = "Stopped";
						break;
					default:
						status = "Error";
						break;
					}
					uploadStatus({
						file: file,
						status: status,
						error: true
					});
				},
				upload_success_handler: function(file, serverData) {
					uploadStatus({
						file: file,
						status: "Completed",
						success: true
					});
				},
				queue_complete_handler: function() {
					getUploads();
					inProgress = false;
				}
			});
		})();

	};

	Constructor.isImage = function(contentType) {
		if (contentType == "image/jpeg" || contentType == "image/png" || contentType == "image/gif") {
			return true;
		}
		return false;
	};

	Constructor.isPdf = function(contentType) {
		if (contentType == "application/pdf") {
			return true;
		}
		return false;
	};

	Constructor.setPrecision = function(flt, precision) {
		flt *= 1;
		if (flt.toFixed) {
			return flt.toFixed(precision) * 1;
		}
		var pow = Math.pow(10, precision);
		return parseInt(flt * pow, 10) / pow;
	};

	Constructor.niceLength = function(length) {
		length *= 1;
		if (length >= 1048576) {
			length /= 1048576;
			return Constructor.setPrecision(length, length > 10 ? 0 : 1) + " MB";
		} else if (length >= 1024) {
			length /= 1024;
			return Constructor.setPrecision(length, length > 10 ? 0 : 1) + " kB";
		}
		return length + " bytes";
	};

	Constructor.openDialog = function() {
		var dialog = $.FrameDialog.create({
			url: baseUrl + "/uploads/manager",
			width: 600,
			height: 400,
			title: "My Uploads",
			buttons: {}
		});
		dialog.dialog("option", "resizable", false);
		return dialog;
	};

	return Constructor;

})();