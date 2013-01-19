var ThishoodTooltip = (function() {

	var name = "ThishoodTooltip";
	var count = 0;
	var hideMs = 1000;

	return function(templateContainer) {

		var id = name + count++;
		$(templateContainer).template(id);
		$('<div id="' + id + '" style="position:absolute;display:none"></div>').appendTo("body");
		var container = "#" + id;
		var currentElId;
		var that;

		this.id = function() {
			return id;
		};

		this.show = function(elId, params) {
			var el = $("#" + elId);
			var cr = $(container);
			cr.html("");
			cr.show();
			var offset = el.offset();
			cr.offset({left: offset.left + el.width(), top: offset.top});
			$.tmpl(id, params).appendTo(container);
			// schedule hide
			suspendHide();
			currentElId = elId;
			that = this;
			el.hover(suspendHide, scheduleHide);
			cr.hover(suspendHide, scheduleHide);
			// cleanup
			el = cr = null;
		};

		this.showUrl = function(elId, paramsUrl) {
			currentElId = elId;
			that = this;
			$.ajax({
				url: paramsUrl,
				success: onParamsReceived,
				error: cleanup,
				dataType: "json"
			});
		};

		var onParamsReceived = function(params) {
			that.show(currentElId, params);
		};

		var cleanup = function() {
			that = null;
		};

		this.hide = function() {
			var cr = $(container);
			cr.hide();
			$("#" + currentElId).unbind("mouseenter", suspendHide).unbind("mouseleave", scheduleHide);
			cr.unbind("mouseenter mouseleave");
			suspendHide();
			cr = null;
			that = null;
		};

		var hideTimeout = null;

		var scheduleHide = function() {
			if (!hideTimeout) {
				hideTimeout = setTimeout(hide, hideMs);
			}
		};

		var suspendHide = function() {
			if (hideTimeout) {
				clearTimeout(hideTimeout);
				hideTimeout = null;
			}
		};

		var hide = function() {
			that.hide();
		};

	};

})();