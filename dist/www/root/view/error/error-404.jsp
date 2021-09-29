<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <%@include file="/view/inc/head.jsp"%>
    <!--本项目-->
	<style>
	    .error-container {
	        overflow: hidden;
	        height: 255px;
	        position: absolute;
	        top: 0;
	        left: 0;
	        bottom: 0;
	        right: 0;
	        margin: auto;
	    }
	
	    .error-container .error-sub {
	        float: left;
	        width: 550px;
	        text-align: center;
	        height: 255px;
	    }
	
	    .error-container .error-main {
	        float: left;
	        color: #666;
	        width: calc(100% - 550px);
	    }
	
	    .error-container .error-main .header {
	        padding: 35px 0 15px;
	    }
	
	    .error-container .error-main .content {
	        padding-top: 20px;
	    }
	
	    .error-container .error-main .header .title > em:nth-child(1) {
	        font-size: 26px;
	        color: #2d8cf0;
	        margin-right: 10px;
	    }
	
	    .error-container .error-main .header .title > em:nth-child(2) {
	        font-size: 16px;
	        color: #1b1b1b;
	    }
	</style>
</head>
<body>
<div class="page-wrapper">
    <div class="main-wrapper">
        <div class="main">
            <div class="error-container clearfix swb-row">
                <div class="error-sub">
                    <img src="/resc/image/error-404.jpg"/>
                </div>
                <div class="error-main">
                    <div class="header">
                        <h4 class="title mb10"><em>出错了</em><em>抱歉，您要访问的URL资源不存在</em></h4>
                        <p class="font14">有可能您输入的网址不正确或者我们的网页正在维护
                        	<a id="returnHead" class="swb-btn swb-btn-small ml20 mr20">去首页</a>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<%@include file="/view/inc/foot.jsp"%>
<script type="text/javascript">
	// var errorInfo = ${error_info == null ? "{}" : error_info};
	$(function(){
		$("#returnHead").on("click",function(){
			toHomePage();
		});
	});
</script>
</body>
</html>