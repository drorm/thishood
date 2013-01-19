var ThishoodUservoice = (function() {

	var Constructor = function(params) {

		var url = params.baseUrl;
		url += "/uservoice/getSsoToken?r=";

		var that = this;

		this.ssoToken = "";

		this.toString = function() {
			return this.ssoToken;
		};

		function updateSsoToken() {
			$.ajax({
				url: url + Math.random(),
				success: function(token) {
					that.ssoToken = token;
				},
				error: function(xhr) {
					if (xhr.status == 401) {
						document.location = baseUrl + "/logout/index";
					}
				},
				dataType: "json"
			});
			setTimeout(updateSsoToken, 3600000);
		}

		// Init
		(function() {
			$(document).ready(function() {
				updateSsoToken();
			});
		})();

	};

	return Constructor;

})();