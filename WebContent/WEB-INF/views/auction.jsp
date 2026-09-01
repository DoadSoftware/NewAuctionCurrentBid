<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>

  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Auction</title>
  
	<script src="<c:url value='/webjars/jquery/3.7.1/jquery.min.js'/>"></script>
	<script src="<c:url value='/webjars/bootstrap/5.3.8/js/bootstrap.bundle.min.js'/>"></script>
	<script src="<c:url value='/resources/javascript/index.js'/>"></script>
	
	<link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.8/css/bootstrap.min.css'/>"/>
  
  <script type="text/javascript">
	 $(document).on("keydown", function(e){
		  
		  if($('#waiting_modal').hasClass('show')) {
			  e.cancelBubble = true;
			  e.stopImmediatePropagation();
	    	  e.preventDefault();
			  return false;
		  }
		  
	      var evtobj = window.event? event : e;
	      
	      switch(e.target.tagName.toLowerCase())
	      {
	      case "input": case "textarea":
	    	 break;
	      default:
	    	  e.preventDefault();
		      var whichKey = '';
			  var validKeyFound = false;
		    
		      if(evtobj.ctrlKey) {
		    	  whichKey = 'Control';
		      }
		      if(evtobj.altKey) {
		    	  if(whichKey) {
		        	  whichKey = whichKey + '_Alt';
		    	  } else {
		        	  whichKey = 'Alt';
		    	  }
		      }
		      if(evtobj.shiftKey) {
		    	  if(whichKey) {
		        	  whichKey = whichKey + '_Shift';
		    	  } else {
		        	  whichKey = 'Shift';
		    	  }
		      }
		      
			  if(evtobj.keyCode) {
		    	  if(whichKey) {
		    		  if(!whichKey.includes(evtobj.key)) {
		            	  whichKey = whichKey + '_' + evtobj.key;
		    		  }
		    	  } else {
		        	  whichKey = evtobj.key;
		    	  }
			  }
			  validKeyFound = false;
			  if (whichKey.includes('_')) {
				  whichKey.split("_").forEach(function (this_key) {
					  switch (this_key) {
					  case 'Control': case 'Shift': case 'Alt':
						break;
					  default:
						validKeyFound = true;
						break;
					  }
				  });
			   } else {
				  if(whichKey != 'Control' && whichKey != 'Alt' && whichKey != 'Shift') {
					  validKeyFound = true;
				  }
			   }
				  
			   if(validKeyFound == true) {
				   console.log('whichKey = ' + whichKey);
				   processUserSelectionData('LOGGER_FORM_KEYPRESS',whichKey);
			   }
		      }
		  });
  </script>
  <style type="text/css">
  /* Custom 3D Button Style */
	.btn-sm {
	  width: 50%;
	  background-color: #2E008B;
	  color: #fff;
	  padding: 10px;
	  font-size: 15px;
	  border-radius: 10px;
	  border: none;
	  font-weight: 500;
	  text-align: center;
	  box-shadow: 0 5px 10px rgba(46, 0, 139, 0.4);
	  transition: all 0.3s ease;
	  cursor: pointer;
	}
	
	.btn-sm:hover {
	  background-color: #4711b4;
	  color: white;
	  transform: translateY(-2px);
	  box-shadow: 0 8px 15px rgba(46, 0, 139, 0.5);
	}
	 body {
	    margin: 0;
	    padding: 0;
	    background-color: #eeeaff;
	    font-family: Arial, sans-serif;
	    background-image: url('<c:url value="/resources/Images/img.png"/>');
	    background-size: cover; 
	    background-position: center center;
	    background-repeat: no-repeat; 
	    animation: zoomIn 1s ease-in-out;
	}
	@keyframes zoomIn {
    0% {
        opacity: 0;
        transform: scale(0.8); /* Start smaller */
    }
    100% {
        opacity: 1;
        transform: scale(1); /* End at normal size */
      }
	}
	.wordart-3d {
	  color: #fff;
	  text-transform: uppercase;
	  text-shadow:
	    1px 1px 0 #000,
	    1px 1px 0 #333,
	    1px 1px 0 #666,
	    1px 1px 0 #999,
	    1px 1px 0 #ccc;
	  padding: 10px 20px;
	  border-radius: 8px;
	  display: inline-block;
	  transition: transform 0.2s ease;
	}
	
	.wordart-3d:hover {
	  transform: scale(1.1);
	  cursor: pointer;
	}
	a,h4,table{
		font-size: larger;
		font-weight: bolder;
	}
  </style>
</head>
<body onload="afterPageLoad('AUCTION');">
<form:form name="auction_form" autocomplete="off" action="auction" method="POST" >
<div class="content py-5">
  <div class="container">
	<div class="row">
	 <div class="col-md-12 offset-md-2">
       <span class="anchor"></span>
         <div class="card card-outline-secondary">
           <div class="card-header">
           </div>
          <div class="card-body">
			  <div class="panel-group" id="match_configuration">
			    <div class="panel panel-default">
			      <div class="panel-heading">
			        <h4 class="panel-title">
			          <a class="wordart-3d" data-bs-toggle="collapse"  href="#load_setup_match">Configuration</a>
			        </h4>
			      </div>
			      <div id="load_setup_match" class="panel-collapse collapse">
					<div class="panel-body">
 					  <div id="start_pause_match_time_div" style="margin-bottom:5px;">
						<div class="row">
						 <div class="col-sm-3 col-md-3">
						    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
						  		name="player_overwrite" id="player_overwrite" onclick="processUserSelection(this);">
						  		<i class="fas fa-tools"></i>OVERWRITE</button>
						 </div>
						 <div class="col-sm-3 col-md-3">
						    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
						  		name="refresh_player" id="refresh_player" onclick="processUserSelection(this);">
						  		<i class="fas fa-tools"></i>Refresh</button>
						 </div>
						</div>
					  </div> 
				    </div>
			      </div>
			    </div>
			  </div> 
		    <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			  <div id="select_event_div" style="display:none;"></div>
			  <div id="auction_div" style="display:none;"></div>
           </div>
          </div>
         </div>
       </div>
    </div>
  </div>
 </div>
<input type="hidden" name="selected_broadcaster" id="selected_broadcaster" value="${session_selected_broadcaster}"/>
</form:form>
</body>
</html>