var ThishoodUserProfile = (function() {

	var name = "ThishoodUserProfile";
	var count = 0;

	var Constructor = function(params) {

		var container = params.container;
		var templateContainers = params.templateContainers;
		var baseUrl = params.baseUrl;

		var id = name + count++;
		var referenceTplId = id + "Reference";
		$(templateContainers.reference).template(referenceTplId);

		this.id = function() {
			return id;
		};

		var onAjaxError = function(xhr) {
			if (xhr.status == 401) {
				document.location = baseUrl + "/logout/index";
			}
		};

		this.createReference = function(form) {
			if (!$(form.message).val().length) {
				jAlert("Please enter your recommendation.")
				return;
			}
			$.ajax({
				url: baseUrl + "/userReference/createReference",
				global: false,
				type: "POST",
				data: $(form).serialize(),
				dataType: "json",
				success: function(reference) {
					if (reference) {
						$(form.score).val(1);
						$(form.message).val("");
						Constructor.hideForm(form.id);
						form = null;
						$.tmpl(referenceTplId, reference).prependTo(container);
						var el = $("#reference" + reference.referenceId);
						el.hide();
						el.addClass("highlighted");
						el = null;
						$("#reference" + reference.referenceId + ":hidden").fadeIn("slow", function() {
							$(this).removeClass("highlighted");
						});
					}
				},
				error: onAjaxError
			});
		};

		this.deleteReference = function(referenceId) {
			jConfirm("The recommendation will be deleted permanently. Are you sure?", "Please confirm", function (answer) {
				if (answer) {
					deleteReference(referenceId);
				}
			});
		};

		var deleteReference = function(referenceId) {
			$.ajax({
				url: baseUrl + "/userReference/deleteReference",
				global: false,
				type: "POST",
				data: "referenceId=" + referenceId,
				dataType: "json",
				success: function(response) {
					if (response && response.ok) {
						var el = $("#reference" + referenceId);
						if (el.length) {
							el.addClass("deleted");
							el.hide("slow", function() {
								el.remove();
								el = null;
							});
						}
					}
				},
				error: onAjaxError
			});
		};

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
		textarea = null;
		var subject = $("#" + formId + "Subject");
		subject.focus();
	};

	Constructor.hideForm = function(formId) {
		$("#" + formId).hide();
		$("#" + formId + "Placeholder").show();
	};

	return Constructor;

})();