	<!-- Global Javascript -->
	<script type="text/javascript" src="/resc/lib/common/utils.js"></script>
	<script type="text/javascript" src="/resc/lib/common/laytpl.js"></script>
	<script type="text/javascript" src="/resc/lib/jquery/jquery.js"></script>
	<script type="text/javascript" src="/resc/lib/jquery/jquery.ext.js"></script>
	<script type="text/javascript" src="/resc/lib/jquery/ajax.js"></script>
	<script type="text/javascript" src="/resc/lib/jquery/jquery-ui.js"></script>
    <script type="text/javascript" src="/resc/lib/bootstrap.min.js"></script>
	<script type="text/javascript" src="/resc/lib/layer/layer.js"></script>
	<script type="text/javascript" src="/resc/lib/layer/extend/layer.ext.js"></script>
	<script type="text/javascript" src="/resc/lib/layer/layer.ext.js"></script>
	<script type="text/javascript">
		var __appBaseUrl = "";
		//快捷方式获取应用url
		function getAppUrl(url){
			url = url || "";
			return url.indexOf("http") == 0 ?  url : __appBaseUrl + url;
		}
		//
		var __restBaseUrl = ""; ///api
		//快捷方式获取rest api
		function getRestUrl(url) {
			url = url || "";
			return url.indexOf("http") == 0 ? url : __restBaseUrl + url;
		}
		//转到系统首页
		function toHomePage(){
			window.location = getAppUrl();
		}
	</script>
	<script type="text/javascript" src="/resc/app.conf.js"></script>