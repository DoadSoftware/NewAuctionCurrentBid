<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Initialise Screen</title>

	<script src="<c:url value='/webjars/jquery/3.7.1/jquery.min.js'/>"></script>
	<script src="<c:url value='/webjars/bootstrap/5.3.8/js/bootstrap.bundle.min.js'/>"></script>
	<script src="<c:url value='/resources/javascript/index.js'/>"></script>
	
	<link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.8/css/bootstrap.min.css'/>"/>
	<link rel="stylesheet" href="<c:url value='/webjars/font-awesome/6.7.2/css/all.min.css'/>"/>
  
<style>
	 body {
	  font-family: 'Poppins', sans-serif;
	  margin: 0;
	  padding: 0;
	  min-height: 100vh;
	  display: flex;
	  justify-content: center;
	  align-items: center;
	  position: relative;
	  z-index: 1;
	  animation: fadeIn 0.8s ease-in-out;
	}
	
	body::before {
	  content: "";
	  position: fixed;
	  top: 0;
	  left: 0;
	  width: 100%;
	  height: 100%;
	  background: url('<c:url value="/resources/Images/img.png"/>') no-repeat center center fixed;
	  background-size: cover;
	  z-index: -1;
	  opacity: 1;
	}

  .container {
    width: 100vh;
    min-height: 50vh;
    padding: 40px 30px;
    background-color: #f5f4ff;
    border-radius: 20px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
    animation: slideIn 0.5s ease-in-out;
  }

  @keyframes fadeIn {
    0% { opacity: 0; transform: translateY(-20px); }
    100% { opacity: 1; transform: translateY(0); }
  }

  @keyframes slideIn {
    from { transform: scale(0.95); opacity: 0; }
    to { transform: scale(1); opacity: 1; }
  }

  .card-header {
    text-align: center;
    margin-bottom: 25px;
  }

  .card-header h3 {
    margin: 0;
    color: #2E008B;
    font-weight: 600;
    font-size: 48px;
  }

  label {
    font-weight: 800;
    color: #333;
    font-size: 18px;
    display: block;
    margin-bottom: 8px;
  }

  select {
    width: 100%;
    font-weight: 800;
    padding: 18px;
    font-size: 15px;
    border-radius: 10px;
    border: 1px solid #ccc;
    background-color: #f9f9f9;
    color: #333;
    margin-bottom: 25px;
    outline: none;
  }

  .btn-sm {
	  width: 60%;
	  background-color: #2E008B;
	  color: #fff;
	  padding: 14px;
	  font-size: 16px;
	  font-weight: 600;
	  text-align: center;
	  border: none;
	  border-radius: 10px;
	  cursor: pointer;
	  box-shadow: 0 6px #1c005f;
	  transition: all 0.2s ease;
	  display: block;
	  margin: 0 auto; /* centers the button */
	}
	.btn-sm:hover {
	  background-color: #3a00b5; /* slightly lighter on hover */
	  color: #fff; /* keep the text white */
	}
	.btn-sm:active {
	  box-shadow: 0 2px #1c005f;
	  transform: translateY(4px);
	}


  @media (max-width: 600px) {
    .container {
      width: 90%;
      padding: 25px;
    }
  }
</style>  
</head>
<body>
  <form:form name="initialise_form" autocomplete="off" action="auction" method="POST">
    <div class="container">
      <div class="card-header">
      <h3 style="text-decoration: #FFD700 dashed underline; text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);">CURRENT BID </h3>
      </div>

      <div class="card-body">
        <div class="form-group">
          <label for="selected_broadcaster">Select Broadcaster</label>
          <select id="selected_broadcaster" name="selected_broadcaster" onchange="processUserSelection(this)">
           <option value="WPL">WPL</option>
           <option value="PSL">PSL</option>
           <option value="PWL">PWL</option>
            <option value="KCL">KCL</option>
            <option value="ISPL">ISPL</option>
            <option value="DOAD">DOAD</option>
            <option value="MUMBAI_T20">MUMBAI T20</option>
          </select>
        </div>
        
        <div id="vizPortNumber_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
		    <label for="vizPortNumber" class="col-sm-4 col-form-label text-left">Viz Port Number 
		    	<i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
		    <div class="col-sm-6 col-md-6">
	             <input type="text" id="vizPortNumber" name="vizPortNumber" value="${session_Configurations.portNumber}"
	             	class="form-control form-control-sm floatlabel"></input>
		    </div>
		  </div>
		  <div id="vizIPAddress_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
		    <label for="vizIPAddress" class="col-sm-4 col-form-label text-left">Viz IP Address 
		    	<i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
		    <div class="col-sm-6 col-md-6">
	             <input type="text" id="vizIPAddress" name="vizIPAddress" value="${session_Configurations.ipAddress}"
	             		class="form-control form-control-sm floatlabel" value="localhost"></input>
		    </div>
		  </div>
		  <div id="vizSecondaryPortNumber_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
		    <label for="vizSecondaryPortNumber" class="col-sm-4 col-form-label text-left">Viz Secondary Port Number 
		    	<i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
		    <div class="col-sm-6 col-md-6">
	             <input type="text" id="vizSecondaryPortNumber" name="vizSecondaryPortNumber" value="${session_Configurations.secondaryportNumber}"
	             	class="form-control form-control-sm floatlabel"></input>
		    </div>
		  </div>
		  <div id="vizSecondaryIPAddress_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
		    <label for="vizSecondaryIPAddress" class="col-sm-4 col-form-label text-left">Viz Secondary IP Address 
		    	<i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
		    <div class="col-sm-6 col-md-6">
	             <input type="text" id="vizSecondaryIPAddress" name="vizSecondaryIPAddress" value="${session_Configurations.secondaryipAddress}"
	             		class="form-control form-control-sm floatlabel" value="localhost"></input>
		    </div>
		  </div>

        <button class="btn btn-sm" type="button" name="load_scene_btn" id="load_scene_btn" onclick="processUserSelection(this)" style="animation: pulse 1s infinite; transition: transform 0.3s ease;">
      		<i class="fas fa-film"></i> Load Scene
    	</button>
      </div>
    </div>
  </form:form>
</body>

</html>