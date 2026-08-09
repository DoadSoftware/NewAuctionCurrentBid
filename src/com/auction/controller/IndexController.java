package com.auction.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.auction.model.Auction;
import com.auction.model.Clock;
import com.auction.service.AuctionService;
import com.auction.util.AuctionUtil;
import com.auction.model.Configurations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.auction.util.AuctionFunctions;

@Controller
public class IndexController 
{
	@Autowired
	AuctionService auctionService;

	public static String expiry_date = "2026-12-31";
	public static String current_date = "";
	public static String error_message = "";
	public static Auction session_auction;
	public static Auction session_current_bid;
	public static Clock session_clock;
	public static String session_selected_broadcaster;
	public static boolean is_this_updating = false;
	public static Configurations session_Configurations;
	public static int PortNumber;
	public static ObjectMapper objectMapper = new ObjectMapper();
	public static List<PrintWriter> print_writer;
	
	@RequestMapping(value = {"/","/initialise"}, method={RequestMethod.GET,RequestMethod.POST}) 
	public String initialisePage(ModelMap model,
			@ModelAttribute("session_Configurations") Configurations session_Configurations) throws JAXBException, IOException, ParseException 
	{
		if(current_date == null || current_date.isEmpty()) {
			current_date = AuctionFunctions.getOnlineCurrentDate();
		}
		
		if(new File(AuctionUtil.AUCTION_DIRECTORY + "Auction_Config.XML").exists()) {
			session_Configurations = (Configurations)JAXBContext.newInstance(Configurations.class).createUnmarshaller().unmarshal(
					new File(AuctionUtil.AUCTION_DIRECTORY + "Auction_Config.XML"));
		} else {
			session_Configurations = new Configurations();
			JAXBContext.newInstance(Configurations.class).createMarshaller().marshal(session_Configurations, 
					new File(AuctionUtil.AUCTION_DIRECTORY + "Auction_Config.XML"));
		}
		model.addAttribute("session_Configurations",session_Configurations);
		
		return "initialise";
	}
	@RequestMapping(value = {"/auction"}, method={RequestMethod.GET,RequestMethod.POST}) 
	public String auctionPage(ModelMap model,
			@ModelAttribute("session_Configurations") Configurations session_Configurations,
			@RequestParam(value = "selected_broadcaster", required = false, defaultValue = "") String selected_broadcaster,
			@RequestParam(value = "vizIPAddress", required = false, defaultValue = "") String vizIPAddress,
			@RequestParam(value = "vizPortNumber", required = false, defaultValue = "") String vizPortNumber,
			@RequestParam(value = "vizSecondaryIPAddress", required = false, defaultValue = "") String vizSecondaryIPAddress,
			@RequestParam(value = "vizSecondaryPortNumber", required = false, defaultValue = "") String vizSecondaryPortNumber)
					throws JAXBException, IOException, ParseException 
	{
		if(current_date == null || current_date.isEmpty()) {
			current_date = AuctionFunctions.getOnlineCurrentDate();
		}
		if(current_date == null || current_date.isEmpty()) {
			model.addAttribute("error_message","You must be connected to the internet online");
			return "error";
		} else if(new SimpleDateFormat("yyyy-MM-dd").parse(expiry_date).before(new SimpleDateFormat("yyyy-MM-dd").parse(current_date))) {
			model.addAttribute("error_message","This software has expired");
			return "error";
		}else {
			
			session_Configurations = new Configurations(selected_broadcaster, vizIPAddress, Integer.valueOf(vizPortNumber),
					vizSecondaryIPAddress, Integer.valueOf(vizSecondaryPortNumber));
			JAXBContext.newInstance(Configurations.class).createMarshaller().marshal(session_Configurations, 
					new File(AuctionUtil.AUCTION_DIRECTORY + "Auction_Config.XML"));
			
			PortNumber = Integer.valueOf(vizPortNumber);
			
			print_writer = processPrintWriter(session_Configurations);
			
			session_selected_broadcaster = selected_broadcaster;			
			session_current_bid = new Auction();
			session_auction = new Auction();
			session_clock = new Clock();
			model.addAttribute("session_selected_broadcaster", session_selected_broadcaster);
			return "auction";
		}
	}
	
	@RequestMapping(value = {"/processAuctionProcedures"}, method={RequestMethod.GET,RequestMethod.POST})    
	public @ResponseBody String processAuctionProcedures(
			@RequestParam(value = "whatToProcess", required = false, defaultValue = "") String whatToProcess,
			@RequestParam(value = "valueToProcess", required = false, defaultValue = "") String valueToProcess)
					throws JAXBException, IllegalAccessException, InvocationTargetException, IOException, 
					NumberFormatException, InterruptedException
	{	
		System.out.println(whatToProcess);
		System.out.println(session_selected_broadcaster);
		switch (whatToProcess.toUpperCase()) {
		case AuctionUtil.INCREMENT_BID: case AuctionUtil.DECREMENT_BID: case AuctionUtil.SOLD_POINTS: case AuctionUtil.PLAYER_OVERWRITE:
		case AuctionUtil.INCREMENT_BID_LKH: case AuctionUtil.DECREMENT_BID_LKH: case "LOG_CLOCK_STATUS": case "LOG_TIME": case "BUILD_CONNECTION":
			switch (whatToProcess.toUpperCase()) {
			case "BUILD_CONNECTION":
				session_Configurations = (Configurations)JAXBContext.newInstance(Configurations.class).createUnmarshaller().unmarshal(
						new File(AuctionUtil.AUCTION_DIRECTORY + "Auction_Config.XML"));
				PortNumber = Integer.valueOf(session_Configurations.getPortNumber());
				print_writer = processPrintWriter(session_Configurations);
				break;
			case "LOG_TIME":
				long main_sec=0;
				
				session_current_bid.getClock().setMatchTotalMilliSeconds(Integer.valueOf(valueToProcess));
				new ObjectMapper().writeValue(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON), session_current_bid);
				main_sec = (session_clock.getMatchTotalMilliSeconds()/1000);
				
				if(print_writer.size() == 2) {
					print_writer.get(0).println("LAYER1*EVEREST*TREEVIEW*Main*FUNCTION*TAG_CONTROL SET tTimer " + twoDigitString(main_sec) + ";");
					
					print_writer.get(1).println("-1 RENDERER*FRONT_LAYER*TREE*$gfx_ScoreBug$Main$Timer$Text$txt_Timer*GEOM*TEXT SET " + twoDigitString(main_sec) + "\0");
					
					if(main_sec == 1) {
						print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_End START;");
						TimeUnit.MILLISECONDS.sleep(200);
						print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_Out START;");
						
						print_writer.get(1).println("-1 RENDERER*FRONT_LAYER*STAGE*DIRECTOR*anim_Timer$In_Out CONTINUE\0");
						
						TimeUnit.MILLISECONDS.sleep(1000);
						print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_End SHOW 0.0;");
						TimeUnit.MILLISECONDS.sleep(200);
						print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_In SHOW 0.0;");
						
						session_current_bid.getClock().setMatchTotalMilliSeconds(15000);
						session_current_bid.getClock().setMatchTimeStatus("PAUSE");
						new ObjectMapper().writeValue(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON), session_current_bid);
					}
				}else if(print_writer.size() == 1){
					if(PortNumber == 1980) {
						print_writer.get(0).println("LAYER1*EVEREST*TREEVIEW*Main*FUNCTION*TAG_CONTROL SET tTimer " + twoDigitString(main_sec) + ";");
						if(main_sec == 1) {
							print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_End START;");
							TimeUnit.MILLISECONDS.sleep(200);
							print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_Out START;");
							
							TimeUnit.MILLISECONDS.sleep(1000);
							print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_End SHOW 0.0;");
							TimeUnit.MILLISECONDS.sleep(200);
							print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_In SHOW 0.0;");
							
							session_current_bid.getClock().setMatchTotalMilliSeconds(15000);
							session_current_bid.getClock().setMatchTimeStatus("PAUSE");
							new ObjectMapper().writeValue(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON), session_current_bid);
						}
						
					}else if(PortNumber == 6100) {
						print_writer.get(0).println("-1 RENDERER*FRONT_LAYER*TREE*$gfx_ScoreBug$Main$Timer$Text$txt_Timer*GEOM*TEXT SET " + twoDigitString(main_sec) + "\0");
						if(main_sec == 1) {
							print_writer.get(0).println("-1 RENDERER*FRONT_LAYER*STAGE*DIRECTOR*anim_Timer$In_Out CONTINUE\0");

							session_current_bid.getClock().setMatchTotalMilliSeconds(15000);
							session_current_bid.getClock().setMatchTimeStatus("PAUSE");
							new ObjectMapper().writeValue(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON), session_current_bid);
						}
					}
				}
				break;
			case "LOG_CLOCK_STATUS":
				session_current_bid.getClock().setMatchTimeStatus(valueToProcess);

				if(valueToProcess.equalsIgnoreCase("PAUSE")) {
					if(print_writer.size() == 2) {
						print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_End START;");
						TimeUnit.MILLISECONDS.sleep(200);
						print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_Out START;");
						
						print_writer.get(1).println("-1 RENDERER*FRONT_LAYER*STAGE*DIRECTOR*anim_Timer$In_Out CONTINUE\0");
						
						TimeUnit.MILLISECONDS.sleep(1000);
						print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_End SHOW 0.0;");
						TimeUnit.MILLISECONDS.sleep(200);
						print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_In SHOW 0.0;");
					}else if(print_writer.size() == 1) {
						if(PortNumber == 1980) {
							print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_End START;");
							TimeUnit.MILLISECONDS.sleep(200);
							print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_Out START;");
							
							TimeUnit.MILLISECONDS.sleep(1000);
							print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_End SHOW 0.0;");
							TimeUnit.MILLISECONDS.sleep(200);
							print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_In SHOW 0.0;");
							
						}else if(PortNumber == 6100) {
							print_writer.get(0).println("-1 RENDERER*FRONT_LAYER*STAGE*DIRECTOR*anim_Timer$In_Out CONTINUE\0");
						}
					}
					
					session_current_bid.getClock().setMatchTotalMilliSeconds(15000);
					
				}else if(valueToProcess.equalsIgnoreCase("START")) {
					main_sec = (session_clock.getMatchTotalMilliSeconds()/1000);
					
					if(print_writer.size() == 2) {
						print_writer.get(0).println("LAYER1*EVEREST*TREEVIEW*Main*FUNCTION*TAG_CONTROL SET tTimer " + twoDigitString(main_sec) + ";");
						print_writer.get(1).println("-1 RENDERER*FRONT_LAYER*TREE*$gfx_ScoreBug$Main$Timer$Text$txt_Timer*GEOM*TEXT SET " + twoDigitString(main_sec) + "\0");
						
						print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_In START;");
						print_writer.get(1).println("-1 RENDERER*FRONT_LAYER*STAGE*DIRECTOR*anim_Timer$In_Out START\0");
						
					}else if(print_writer.size() == 1) {
						if(PortNumber == 1980) {
							print_writer.get(0).println("LAYER1*EVEREST*TREEVIEW*Main*FUNCTION*TAG_CONTROL SET tTimer " + twoDigitString(main_sec) + ";");
							print_writer.get(0).println("LAYER1*EVEREST*STAGE*DIRECTOR*Timer_In START;");
							
						}else if(PortNumber == 6100) {
							print_writer.get(0).println("-1 RENDERER*FRONT_LAYER*TREE*$gfx_ScoreBug$Main$Timer$Text$txt_Timer*GEOM*TEXT SET " + twoDigitString(main_sec) + "\0");
							print_writer.get(0).println("-1 RENDERER*FRONT_LAYER*STAGE*DIRECTOR*anim_Timer$In_Out START\0");
						}
					}
					TimeUnit.MILLISECONDS.sleep(800);
				}

				new ObjectMapper().writeValue(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON), session_current_bid);
				break;
			case AuctionUtil.INCREMENT_BID:
				switch (session_selected_broadcaster) {
				case "PSL":
					if(session_current_bid.getCurrentPlayers().getSoldOrUnsold().equalsIgnoreCase(AuctionUtil.BID)) {
						if(session_current_bid.getCurrentPlayers().getSoldForPoints() == 6000000 || session_current_bid.getCurrentPlayers().getSoldForPoints() < 11000000) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 250000);
							
						}else if(session_current_bid.getCurrentPlayers().getSoldForPoints() == 11000000 || session_current_bid.getCurrentPlayers().getSoldForPoints() < 22000000){
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 500000);
							
						}else if(session_current_bid.getCurrentPlayers().getSoldForPoints() == 22000000 || session_current_bid.getCurrentPlayers().getSoldForPoints() < 42000000){
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 1000000);
							
						}
						else if(session_current_bid.getCurrentPlayers().getSoldForPoints() >= 42000000) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 1500000);
						}
					}
					break;
				case "ISPL":
					if(session_current_bid.getCurrentPlayers().getSoldOrUnsold().equalsIgnoreCase(AuctionUtil.BID)) {
						if(session_current_bid.getCurrentPlayers().getSoldForPoints() == 300000 || session_current_bid.getCurrentPlayers().getSoldForPoints() < 500000) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 20000);
							
						}else if(session_current_bid.getCurrentPlayers().getSoldForPoints() == 500000 || session_current_bid.getCurrentPlayers().getSoldForPoints() < 700000){
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 25000);
							
						}else if(session_current_bid.getCurrentPlayers().getSoldForPoints() >= 700000) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 50000);
						}
					}
					break;
				case "MUMBAI_T20":
					if (session_current_bid.getCurrentPlayers().getSoldOrUnsold().equalsIgnoreCase(AuctionUtil.BID)) {
					    if (session_current_bid.getCurrentPlayers().getSoldForPoints() < 500000) {
					        session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 25000);
					        
					    } else if (session_current_bid.getCurrentPlayers().getSoldForPoints() >= 500000) {
					        session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 50000);
					    }
					}

					break;
				case "PWL":
					String category = session_current_bid.getCurrentPlayers().getCategory();
					int points = 0;

					switch (category.toUpperCase()) {
					    case "A+":case "A":
					        points = 100000;
					        break;

					    case "B":case "C":
					        points = 50000;
					        break;
					}

					session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + points);

					break;	
				case "KCL":
					if(session_current_bid.getCurrentPlayers().getSoldOrUnsold().equalsIgnoreCase(AuctionUtil.BID)) {
						if(Integer.valueOf(session_current_bid.getCurrentPlayers().getBasePrice()) == 300) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 20000);
							
						}else if(Integer.valueOf(session_current_bid.getCurrentPlayers().getBasePrice()) == 100) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 10000);
							
						}else if(Integer.valueOf(session_current_bid.getCurrentPlayers().getBasePrice()) == 50) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 5000);
							
						}
					}
					break;
				case "WPL":
					if(Integer.valueOf(session_current_bid.getCurrentPlayers().getSoldForPoints()) >= 100000) {
						session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 10000);
						
					}else {
						session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 5000);
						
					}
					break;
				default:
					session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() + 20000);
					break;
				}
				break;
			case AuctionUtil.DECREMENT_BID:
				switch (session_selected_broadcaster) {
				case "PSL":
					if(session_current_bid.getCurrentPlayers().getSoldOrUnsold().equalsIgnoreCase(AuctionUtil.BID)) {
						if(session_current_bid.getCurrentPlayers().getSoldForPoints() == 6000000 || session_current_bid.getCurrentPlayers().getSoldForPoints() < 11000000) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 250000);
							
						}else if(session_current_bid.getCurrentPlayers().getSoldForPoints() == 11000000 || session_current_bid.getCurrentPlayers().getSoldForPoints() < 22000000){
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 500000);
							
						}else if(session_current_bid.getCurrentPlayers().getSoldForPoints() == 22000000 || session_current_bid.getCurrentPlayers().getSoldForPoints() < 42000000){
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 1000000);
							
						}
						else if(session_current_bid.getCurrentPlayers().getSoldForPoints() >= 42000000) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 1500000);
						}
					}
					break;
				case "ISPL":
					if(session_current_bid.getCurrentPlayers().getSoldOrUnsold().equalsIgnoreCase(AuctionUtil.BID)) {
						if(session_current_bid.getCurrentPlayers().getSoldForPoints() > 700000) {
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 50000);
							
						}else if(session_current_bid.getCurrentPlayers().getSoldForPoints() > 500000){
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 25000);
							
						}else if(session_current_bid.getCurrentPlayers().getSoldForPoints() > 300000) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 20000);
								
						}
					}
					break;
				case "MUMBAI_T20":
					if (session_current_bid.getCurrentPlayers().getSoldOrUnsold().equalsIgnoreCase(AuctionUtil.BID)) {
					    if (session_current_bid.getCurrentPlayers().getSoldForPoints() <= 500000) {
					        session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 25000);
					        
					    } else if (session_current_bid.getCurrentPlayers().getSoldForPoints() > 500000) {
					        session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 50000);
					    }
					}

					break;
				case "PWL":
					String category = session_current_bid.getCurrentPlayers().getCategory();
					int points = 0;

					switch (category.toUpperCase()) {
					    case "A+":case "A":
					        points = 100000;
					        break;

					    case "B":case "C":
					        points = 50000;
					        break;
					}

					session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - points);
					break;			
				case "KCL":
					if(session_current_bid.getCurrentPlayers().getSoldOrUnsold().equalsIgnoreCase(AuctionUtil.BID)) {
						if(Integer.valueOf(session_current_bid.getCurrentPlayers().getBasePrice()) == 300) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 20000);
							
						}else if(Integer.valueOf(session_current_bid.getCurrentPlayers().getBasePrice()) == 100) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 10000);
							
						}else if(Integer.valueOf(session_current_bid.getCurrentPlayers().getBasePrice()) == 50) {
							
							session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 5000);
							
						}
					}
					break;
				case "WPL":
					if(Integer.valueOf(session_current_bid.getCurrentPlayers().getSoldForPoints()) > 100000) {
						session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 10000);
						
					}else {
						session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 5000);
						
					}break;
				default:
					session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints() - 20000);
					break;
				}
				
				break;
			case AuctionUtil.SOLD_POINTS:
				session_current_bid.getCurrentPlayers().setSoldForPoints(Integer.valueOf(valueToProcess));
				break;	
				
			case AuctionUtil.PLAYER_OVERWRITE:
				if(session_current_bid.getCurrentPlayers() != null) {
					if(session_current_bid.getCurrentPlayers().getSoldOrUnsold().equalsIgnoreCase(AuctionUtil.BID)) {
						session_current_bid.getCurrentPlayers().setSoldForPoints(Integer.valueOf(valueToProcess.split(",")[0] + "000"));
					}
				}
				break;
			case AuctionUtil.INCREMENT_BID_LKH:
				session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints()+100000);
				break;
			case AuctionUtil.DECREMENT_BID_LKH: 
				session_current_bid.getCurrentPlayers().setSoldForPoints(session_current_bid.getCurrentPlayers().getSoldForPoints()-100000);

				break;
			}
			new ObjectMapper().writeValue(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON), 
					session_current_bid);
			return objectMapper.writeValueAsString(session_current_bid);

		case AuctionUtil.REFRESH_PLAYER:
			if(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.AUCTION_JSON).exists()) {
				//Logger file
				session_auction = new ObjectMapper().readValue(new File(AuctionUtil.AUCTION_DIRECTORY + 
						AuctionUtil.AUCTION_JSON), Auction.class);
			}
			
			if(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON).exists()) {
				
				session_current_bid.setCurrentPlayers(session_auction.getPlayers().get(session_auction.getPlayers().size() -1));
				
				new ObjectMapper().writeValue(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON), 
						session_current_bid);
			}
			return objectMapper.writeValueAsString(session_current_bid);
			
		case AuctionUtil.LOAD_MATCH: 
			if(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.AUCTION_JSON).exists()) {
				//Logger file
				session_auction = new ObjectMapper().readValue(new File(AuctionUtil.AUCTION_DIRECTORY + 
						AuctionUtil.AUCTION_JSON), Auction.class);
			}
			
			if(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON).exists()) {
				if(session_auction.getPlayers() != null && session_auction.getPlayers().size() > 0) {
					session_current_bid.setCurrentPlayers(session_auction.getPlayers().get(session_auction.getPlayers().size() -1));
				}
				
				session_clock.setMatchHalves("15_Seconds");
				session_clock.setMatchTotalMilliSeconds(15000);
				session_clock.setMatchTimeStatus("PAUSE");
				session_current_bid.setClock(session_clock);
				
				new ObjectMapper().writeValue(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON), 
						session_current_bid);
			}else {
				session_clock.setMatchHalves("15_Seconds");
				session_clock.setMatchTotalMilliSeconds(15000);
				session_clock.setMatchTimeStatus("PAUSE");
				session_current_bid.setClock(session_clock);
				
				new ObjectMapper().writeValue(new File(AuctionUtil.AUCTION_DIRECTORY + AuctionUtil.CURRENT_BID_JSON), 
						session_current_bid);
			}
			
			return objectMapper.writeValueAsString(session_current_bid);
		default:
			return objectMapper.writeValueAsString(session_current_bid);
			
		}
	}
	
	@SuppressWarnings("resource")
	public static List<PrintWriter> processPrintWriter(Configurations config) throws IOException
	{
		List<PrintWriter> print_writer = new ArrayList<PrintWriter>();
		
		if(config.getIpAddress() != null && !config.getIpAddress().isEmpty()) {
			print_writer.add(new PrintWriter(new OutputStreamWriter(new Socket(config.getIpAddress(), 
					config.getPortNumber()).getOutputStream(), StandardCharsets.UTF_8),true));
		}
		
		if(config.getSecondaryipAddress() != null && !config.getSecondaryipAddress().isEmpty()) {
			print_writer.add(new PrintWriter(new OutputStreamWriter(new Socket(config.getSecondaryipAddress(), 
					config.getSecondaryportNumber()).getOutputStream(), StandardCharsets.UTF_8),true));
		}
		
		return print_writer;
	}
	
	public static String twoDigitString(long number) {
	    if (number == 0) {
	        return "00";
	    }
	    if (number / 10 == 0) {
	        return "0" + number;
	    }
	    return String.valueOf(number);
	}
}