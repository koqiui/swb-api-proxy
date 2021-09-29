<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<%@include file="/view/inc/head.jsp"%>
	<title>首页</title>
</head>
<body>
	<div style="width:100%;height:100%;">
		<div style="width:600px;height:200px;position:relative; margin: 200px auto; text-align:center;">
			<div class="panel panel-default">
			  <div class="panel-heading">
			  	<h3 class="panel-title">商维保应用代理</h3>
			  </div>
			  <div class="panel-body" style="padding-left:120px;">
			    	<div class="input-group">
					    <span class="input-group-addon" style="width:100px;" id="basic-addon1">环境名称：</span>
					    <span  id="appEnvName" class="form-control" aria-describedby="basic-addon1"></span>
					</div>
					
			    	<div class="input-group" style="margin-top:10px;">
					    <span class="input-group-addon" style="width:100px;" id="basic-addon2">基础URL：</span>
					    <span  id="appBaseUrl" class="form-control" aria-describedby="basic-addon2"></span>
					</div>
			    	
			    	<div class="input-group" style="margin-top:10px;display:none;">
					    <span class="input-group-addon" style="width:100px;" id="basic-addon3">AppKey：</span>
					    <span  id="appKey" class="form-control" aria-describedby="basic-addon3"></span>
					</div>
			  </div>
			</div>
			
		</div>
	
	</div>
	
	<%@include file="/view/inc/foot.jsp"%>
	<script type="text/javascript">
		var $id = Jqext.$id;
		var Toast = Jqext.Toast;
		//
		function fetchAppConfInfo(){
			var ajax = Ajax.get('/app/conf/get');
			ajax.done(function(result){
				if(result.success){
					var conf = result.data || null;
					if(conf == null){
						Layer.warning('无有效配置信息');
					}
					else {
						$id('appEnvName').text(conf.appEnvName);
						$id('appBaseUrl').text(conf.appBaseUrl);
						$id('appKey').text(conf.appKey);
					}
				}
				else {
					Toast.show(result.message, 5000, 'error');
				}
			});
			ajax.go();
		}
		
		$(function(){
			//获取配置和环境信息
			fetchAppConfInfo();
		});
	</script>
</body>
</html>