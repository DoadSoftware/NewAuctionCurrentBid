<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>

  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Error</title>
	
	<script src="<c:url value='/webjars/jquery/3.7.1/jquery.min.js'/>"></script>
	<script src="<c:url value='/webjars/bootstrap/5.3.8/js/bootstrap.bundle.min.js'/>"></script>
	<link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.8/css/bootstrap.min.css'/>"/>
	
</head>
<body>
	<div class="content py-5" style="background-color: #EAE8FF; color: #2E008B">
	  <div class="container">
		<div class="row">
		 <div class="col-md-8 offset-md-2">
	       <span class="anchor"></span>
	         <div class="card card-outline-secondary">
	           <div class="card-header">
	             <h2>${error_message}</h2>
	           </div>
	         </div>
	     </div>
	   </div>
	 </div>
	</div>
</body>
</html>