var session_auction, soldForPoints, timer = 15;
function millisToMinutesAndSeconds(millis) {
  var s = (millis / 1000).toFixed(0);
  return (s < 10 ? '0' + s :s);
}
function minutesAndSecondsToMillis(time) {
  var parts = time.split(':');
  var minutes = parseInt(parts[0]);
  var seconds = parseInt(parts[1]);
  return (minutes * 60 + seconds) * 1000;
}

function processMatchTime() {
	if(session_auction.clock) {
		if(session_auction.clock.matchTimeStatus.toLowerCase() == 'start') {
			if(session_auction.clock.matchTotalMilliSeconds > 0){
				session_auction.clock.matchTotalMilliSeconds = session_auction.clock.matchTotalMilliSeconds - 1000;
				processAuctionProcedures('LOG_TIME',session_auction.clock.matchTotalMilliSeconds);
			}
		}
		
		if(document.getElementById('match_time_hdr')) {
			document.getElementById('match_time_hdr').innerHTML = 'TIMER : ' + 
				millisToMinutesAndSeconds(session_auction.clock.matchTotalMilliSeconds);
		}
		if(document.getElementById('match_status_hdr')) {
			document.getElementById('match_status_hdr').innerHTML = session_auction.clock.matchTimeStatus;
		}
	}
}
function processWaitingButtonSpinner(whatToProcess) 
{
	switch (whatToProcess) {
	case 'START_WAIT_TIMER': 
		$('.spinner-border').show();
		$(':button').prop('disabled', true);
		break;	case 'END_WAIT_TIMER': 
		$('.spinner-border').hide();
		$(':button').prop('disabled', false);
		break;
	}
}
function afterPageLoad(whichPageHasLoaded)
{
	switch (whichPageHasLoaded) {
	case 'AUCTION':
		processAuctionProcedures('LOAD_MATCH',null);
		setInterval(processMatchTime, 1000);
		break;
	}
}
function processUserSelectionData(whatToProcess,dataToProcess){
	//alert(whatToProcess);
	switch (whatToProcess) {
	case 'LOGGER_FORM_KEYPRESS':
		switch (dataToProcess) {
		case 's': // 83
			timer = 15
			processAuctionProcedures('LOG_CLOCK_STATUS','START');
			break;
		case 'a': // 65
			processAuctionProcedures('LOG_CLOCK_STATUS','PAUSE');
			break;
		case '1':
			processAuctionProcedures('BUILD_CONNECTION');
			break;
		}
		
		break;
	}
}
function initialiseForm(whatToProcess, dataToProcess)
{
	switch (whatToProcess) {
	case 'TIME':
	
		break;
	case 'MATCH':
	
		break;
	}
}
function uploadFormDataToSessionObjects(whatToProcess)
{
	var formData = new FormData();
	var url_path;

	$('input, select, textarea').each(
		function(index){  
			if($(this).is("select")) {
				formData.append($(this).attr('id'),$('#' + $(this).attr('id') + ' option:selected').val());  
			} else {
				formData.append($(this).attr('id'),$(this).val());  
			}	
		}
	);
	
	url_path = 'upload_match_setup_data';
	
	$.ajax({    
		headers: {'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')},
        url : url_path,     
        data : formData,
        cache: false,
        contentType: false,
        processData: false,
        type: 'POST',     
        success : function(data) {

        },    
        error : function(e) {    
       	 	console.log('Error occured in uploadFormDataToSessionObjects with error description = ' + e);     
        }    
    });		
	
}
function processUserSelection(whichInput)
{	
	switch ($(whichInput).attr('name')) {
	case 'load_scene_btn':
	  	document.initialise_form.submit();
		break;
	case 'selected_broadcaster':
		switch ($('#selected_broadcaster :selected').val()) {
		case 'HANDBALL':
			//$('#vizPortNumber').attr('value','1980');
			//$('label[for=vizScene], input#vizScene').hide();
			//$('label[for=which_scene], select#which_scene').hide();
			//$('label[for=which_layer], select#which_layer').hide();
			break;
		}
		break;
	case 'cancel_btn': 
		document.getElementById('select_event_div').style.display = 'none';
		processWaitingButtonSpinner('END_WAIT_TIMER');
		break;
	case 'player_overwrite_btn':
		processWaitingButtonSpinner('START_WAIT_TIMER');
		processAuctionProcedures('PLAYER_OVERWRITE',null);
		break;
	case 'refresh_player':
		processWaitingButtonSpinner('START_WAIT_TIMER');
		processAuctionProcedures('REFRESH_PLAYER',null);
		break;
	case 'player_overwrite':
		addItemsToList('LOAD_PLAYER_OVERWRITE',session_auction);
		document.getElementById('select_event_div').style.display = '';
		break;
	default:
		switch ($(whichInput).attr('id')) {
		case 'increment_btn':
			processAuctionProcedures('INCREMENT_BID',null);
			break;
		case 'decrement_btn':
			processAuctionProcedures('DECREMENT_BID',null);
			break;
		case 'increment_btn_lkh':
			processAuctionProcedures('INCREMENT_BID_LKH',null);
			break;
		case 'decrement_btn_lkh':
			processAuctionProcedures('DECREMENT_BID_LKH',null);
			break;
		}
		break;
	}
	
}
function processAuctionProcedures(whatToProcess, whichInput)
{
	var value_to_process; 
	
	switch(whatToProcess) {
	case 'PLAYER_OVERWRITE':
		value_to_process = $('#player_base_rupess').val();
		break;
	case 'SOLD_POINTS':
		value_to_process = soldForPoints;
		console.log("value_to_process:",value_to_process);
		break;
	case 'LOG_CLOCK_STATUS': case 'LOG_TIME':
		value_to_process = whichInput;
		break;
	}

	$.ajax({    
        type : 'Get',     
        url : 'processAuctionProcedures.html',     
        data : 'whatToProcess=' + whatToProcess + '&valueToProcess=' + value_to_process, 
        dataType : 'json',
        success : function(data) {
			session_auction = data;
        	switch(whatToProcess) {
			case 'BUILD_CONNECTION':
				alert('Connection is build');
				break;
			case 'LOAD_MATCH':
				addItemsToList('LOAD_MATCH',data);
				document.getElementById('auction_div').style.display = '';
				document.getElementById('select_event_div').style.display = 'none';
				break;
			case 'INCREMENT_BID': case 'DECREMENT_BID': case 'REFRESH_PLAYER':
			case 'INCREMENT_BID_LKH': case 'DECREMENT_BID_LKH':
				addItemsToList('LOAD_MATCH',data);
				break;
			case 'UNDO_PLAYERS':
				alert('Removed Successfully');
				addItemsToList('LOAD_MATCH',data);
				break;
			case 'PLAYER_OVERWRITE':
				addItemsToList('LOAD_PLAYER_OVERWRITE',data);
				addItemsToList('LOAD_MATCH',data);
				break;
        	}
    		processWaitingButtonSpinner('END_WAIT_TIMER');
	    },    
	    error : function(e) {    
	  	 	console.log('Error occured in ' + whatToProcess + ' with error description = ' + e);     
	    }    
	});
}
function addItemsToList(whatToProcess, dataToProcess)
{
	var div,row,header_text,option,table,tbody;
	
	switch (whatToProcess) {
	case 'LOAD_MATCH':
		$('#auction_div').empty();
		if(dataToProcess){
			table = document.createElement('table');
			table.setAttribute('class', 'table table-bordered');
			tbody = document.createElement('tbody');
			let size = ($('#selected_broadcaster').val().toUpperCase() === 'ISPL') ? 2 : 1;
			for(var i = 0; i < 2; i++){
				row = tbody.insertRow(tbody.rows.length);
				switch(i){
				case 0:
					for (var j = 0; j <= size; j++){
						text = document.createElement('label');
						div = document.createElement('div');					
						switch (j) {
						case 0:
						 text.innerHTML = ` <span style="font-size: 1.8em; font-weight: 900; color: #ff5722; vertical-align: middle; margin-right: 8px;">
									    ${dataToProcess.currentPlayers.playerNumber}
									  </span><br>
									  <span style="font-size: 1.2em; font-weight: 900; color: #333;">
									    ${dataToProcess.currentPlayers.full_name}
									  </span>
									`;
						 	break;
						case 1:case 2:				
						for(var k=0; k<=2; k++){
							switch (k) {
							case 0: case 2:
			        			option = document.createElement('input');
			    				option.type = "button";
			    				if (k == 0) {
			        				option.id = 'increment' + (j === 1 ? '_btn' : '_btn_lkh');
			        				option.value="+";
			        				option.setAttribute('onclick','processUserSelection(this);');
			        				div.appendChild(option);
			        			}else {
									option.id = 'decrement' + (j === 1 ? '_btn' : '_btn_lkh');
			        				option.value="-";
			        				option.setAttribute('onclick','processUserSelection(this);');
			        				div.appendChild(option);
									break;
								}
			    				option.style = 'text-align:center;';
								break;
							case 1: 
			        			option = document.createElement('input');
			    				option.type = "text";
			    				option.style = 'width:70%;text-align:center;';
								
								if (j === 1) {
									option.value = dataToProcess.currentPlayers.soldForPoints;
	                                option.addEventListener('change', function(event) {
							        soldForPoints= event.target.value;						       
							       	processAuctionProcedures('SOLD_POINTS',soldForPoints);
	
							   	  });
	                            } else {
	                                option.value = 'GOOGLY POWER'; 
	                                option.setAttribute('readonly', true); 
	                            }
			    				div.appendChild(option);
			    				break;
							}
						}
						break;
						}
					    row.insertCell(j).appendChild(text).appendChild(div);
					    if (j === 0) {
				            row.cells[j].style.width = '200px'; 
				            row.cells[j].style.fontSize = '18px';
				        }
				        if (j === 1 || j === 2) {
							div.style.marginTop = '40px'; 
						}
					}
					break;
				case 1:
					for (var j = 0; j <= size; j++){
						text = document.createElement('label');
						div = document.createElement('div');					
						switch (j) {
						case 0:
							text.innerHTML = 'START : S <br> PAUSE : A <br> Build Connection : 1';
						 	break;
						case 1:
							text.id = 'match_time_hdr';
							text.innerHTML = 'TIMER: 15';
							break;
						case 2:
							text.id = 'match_status_hdr';
							text.innerHTML = '';
							break;
						}
					    row.insertCell(j).appendChild(text);
					}
					break;
				}
				
			}

			table.appendChild(tbody);
			document.getElementById('auction_div').appendChild(table);
			applyTableStyles(table);
		}
		break;
	case 'LOAD_PLAYER_OVERWRITE':
		$('#select_event_div').empty();

		table1 = document.createElement('table');
		table1.setAttribute('class', 'table table-bordered');
				
		tbody = document.createElement('tbody');
		row = tbody.insertRow(tbody.rows.length);
		
		header_text = document.createElement('label');
		header_text.innerHTML = session_auction.currentPlayers.full_name;
		row.insertCell(0).appendChild(header_text);
		
		option = document.createElement('input');
		option.type = "text";
		header_text = document.createElement('label');
		header_text.innerHTML = 'Bid Rupess ';
		option.id = 'player_base_rupess';
		//option.value = session_auction.players[session_auction.players.length-1].soldForPoints;
		option.value = '0';
		
		header_text.htmlFor = option.id;
		row.insertCell(1).appendChild(header_text).appendChild(option);
		
		option = document.createElement('input');
	    option.type = 'button';
		option.name = 'player_overwrite_btn';
		option.value = 'OverWrite';
		option.style.backgroundColor = 'skyblue';
		option.style.width = '150px';
        option.style.height = '30px';
        option.style.fontSize = '20px';
        option.style.border = 'none';
        option.style.cursor = 'pointer';
        option.style.margin = '0 5px';
        option.style.borderRadius = '4px';
	    option.id = option.name;
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div = document.createElement('div');
	    div.append(option);
	    row.insertCell(2).appendChild(div);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_btn';
		option.id = option.name;
		option.style.backgroundColor = 'red';
		option.style.color = 'white';
		option.style.width = '150px';
        option.style.height = '30px';
        option.style.fontSize = '20px';
        option.style.border = 'none';
        option.style.cursor = 'pointer';
        option.style.margin = '0 5px';
        option.style.borderRadius = '4px';
		option.value = 'Cancel';
		option.setAttribute('onclick','processUserSelection(this)');

	    div = document.createElement('div');
	    div.append(option);
	    
	    row.insertCell(3).appendChild(div);
	    row.style.backgroundColor = 'white';
		table1.appendChild(tbody);
		document.getElementById('select_event_div').appendChild(table1);
		
		break;		
	}
}
function removeSelectDuplicates(select_id)
{
	var this_list = {};
	$("select[id='" + select_id + "'] > option").each(function () {
	    if(this_list[this.text]) {
	        $(this).remove();
	    } else {
	        this_list[this.text] = this.value;
	    }
	});
}
function checkEmpty(inputBox,textToShow) {

	var name = $(inputBox).attr('id');
	
	document.getElementById(name + '-validation').innerHTML = '';
	document.getElementById(name + '-validation').style.display = 'none';
	$(inputBox).css('border','');
	if(document.getElementById(name).value.trim() == '') {
		$(inputBox).css('border','#E11E26 2px solid');
		document.getElementById(name + '-validation').innerHTML = textToShow + ' required';
		document.getElementById(name + '-validation').style.display = '';
		document.getElementById(name).focus({preventScroll:false});
		return false;
	}
	return true;	
}	
function applyTableStyles(table) {
    // Apply table styles
    table.style.width = '100%';
    table.style.borderCollapse = 'collapse';
    table.style.fontFamily = 'Arial, sans-serif';

    // Apply styles to all table cells (th and td)
    const cells = table.querySelectorAll('th, td');
    cells.forEach(cell => {
        cell.style.padding = '10px';
        cell.style.textAlign = 'center';
        cell.style.border = '1px solid #ddd';
    });

    // Apply header styles
    const headers = table.querySelectorAll('th');
    headers.forEach(header => {
        header.style.backgroundColor = '#f2f2f2';
        header.style.color = '#333';
    });

    // Apply data cell styles
    const dataCells = table.querySelectorAll('td');
    dataCells.forEach(cell => {
        cell.style.backgroundColor = '#f9f9f9';
    });

    // Style the buttons
    const buttons = table.querySelectorAll('input[type="button"]');
    buttons.forEach(button => {
        button.style.width = '30px';
        button.style.height = '30px';
        button.style.fontSize = '20px';
        button.style.backgroundColor = '#4CAF50';
        button.style.color = 'white';
        button.style.border = 'none';
        button.style.cursor = 'pointer';
        button.style.margin = '0 5px';
        button.style.borderRadius = '4px';

         // Apply specific colors for "+" and "-" buttons
        if (button.value === "+") {
            button.style.backgroundColor = '#4CAF50'; // Green for +
            button.onmouseover = function() {
                button.style.backgroundColor = '#45a049'; // Darker green on hover
            };
            button.onmouseout = function() {
                button.style.backgroundColor = '#4CAF50'; // Reset to original green
            };
        } else if (button.value === "-") {
            button.style.backgroundColor = '#f44336'; // Red for -
            button.onmouseover = function() {
                button.style.backgroundColor = '#e53935'; // Darker red on hover
            };
            button.onmouseout = function() {
                button.style.backgroundColor = '#f44336'; // Reset to original red
            };
        }
    });

    // Style the text input fields
    const textInputs = table.querySelectorAll('input[type="text"]');
    textInputs.forEach(input => {
        input.style.width = '70%';
        input.style.height = '30px';
        input.style.textAlign = 'center';
        input.style.fontSize = '16px';
        input.style.borderRadius = '4px';
        input.style.border = '1px solid #ddd';
        input.style.backgroundColor = '#f9f9f9';

        // Focus styling
        input.addEventListener('focus', () => {
            input.style.border = '1px solid #4CAF50'; // Green border on focus
        });
        input.addEventListener('blur', () => {
            input.style.border = '1px solid #ddd'; // Reset border
        });
    });

    // Optional: Apply hover effect on rows
    const rows = table.querySelectorAll('tr');
    rows.forEach(row => {
        row.onmouseover = function() {
            row.style.backgroundColor = '#f1f1f1'; // Change background color on hover
        };
        row.onmouseout = function() {
            row.style.backgroundColor = ''; // Reset background
        };
    });
}

