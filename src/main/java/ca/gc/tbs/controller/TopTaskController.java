package ca.gc.tbs.controller;

import java.text.SimpleDateFormat;
import static org.springframework.util.StringUtils.hasText;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesOutput;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import static org.springframework.data.mongodb.core.query.Criteria.where;


import ca.gc.tbs.domain.TopTaskSurvey;
import ca.gc.tbs.domain.User;

import ca.gc.tbs.repository.TopTaskRepository;
import ca.gc.tbs.service.UserService;

@Controller
public class TopTaskController {

	private static final Logger LOG = LoggerFactory.getLogger(TopTaskController.class);

	@Autowired
	private TopTaskRepository topTaskRepository;

	@Autowired
	private UserService userService;

    @RequestMapping(value = "/topTaskData") 
    @ResponseBody
    public DataTablesOutput<TopTaskSurvey> list(@Valid DataTablesInput input)  {
    	
    	String dateSearchVal = input.getColumn("dateTime").get().getSearch().getValue();
    	
    	String dataSetVal = input.getColumn("taskOther").get().getSearch().getValue();

    	Criteria findProcessed = where("processed").is("true");
    	
    	if(dataSetVal.contains("nonEmpty") && dateSearchVal.contains(":")) {
    		String[] ret = dateSearchVal.split(":");
    	    
    		if(ret.length == 2) {
        		input.getColumn("taskOther").get().getSearch().setValue("");

	    		String dateSearchValA = ret[0];
	    		
	    		String dateSearchValB = ret[1];
	    		
	    		input.getColumn("dateTime").get().getSearch().setValue("");
	
	        	Criteria dateCriteria = where("dateTime").gte(dateSearchValA).lte(dateSearchValB).and("processed").is("true");

	    		if(dateSearchValA != "" && dateSearchValB != "") {
	    			return topTaskRepository.findAll(input, dateCriteria, new Criteria().orOperator(
	        				Criteria.where("taskOther").exists(true).ne(""),
	        				Criteria.where("taskWhyNotComment").exists(true).ne(""),
	        				Criteria.where("taskImproveComment").exists(true).ne("")));
	    		}
    		}
    		
    	}
    	
    	if(dateSearchVal.contains(":")) {
    		String[] ret = dateSearchVal.split(":");
    		if(ret.length == 2) {
    		
	    		String dateSearchValA = ret[0];
	    		
	    		String dateSearchValB = ret[1];
	    		
	    		input.getColumn("dateTime").get().getSearch().setValue("");
	
	        	Criteria dateCriteria = where("dateTime").gte(dateSearchValA).lte(dateSearchValB);
	        	
	    		if(dateSearchValA != "" && dateSearchValB != "") {
	    			return topTaskRepository.findAll(input, findProcessed, dateCriteria);
	    		}
    		}
    	}
    	if(dataSetVal.contains("nonEmpty")) {
    		
    		input.getColumn("taskOther").get().getSearch().setValue("");
    		
    		return topTaskRepository.findAll(input, findProcessed, new Criteria().orOperator(
    				Criteria.where("taskOther").exists(true).ne(""),
    				Criteria.where("taskWhyNotComment").exists(true).ne(""),
    				Criteria.where("taskImproveComment").exists(true).ne("")));
    	}
    	return topTaskRepository.findAll(input, findProcessed);
	}
    
    
    @RequestMapping(value = "/topTaskSurvey/tasks")
    @ResponseBody
	public String paginationProblemData(HttpServletRequest request) {
    	String lang = request.getParameter("language");
    	return "AAFC,ACOA,AECL,APA,ATSSC,BDC,BWB,CA,CAFC,CanNor,CANSOFCOM,CART,CAS,CATSA,CB,CBC,CBSA,CCC,CCG,CCI,"
    			+ "CCOHS,CDC,CDEV,CDIC,CED,CEIC,CER,CFIA,CGC,CHIN,CHRC,CIB,CICS,CIEC,CIHR,CIPO,CIRB,CIRNAC,CITT,CJC,CLC,"
    			+ "CMAC,CMH,CMHC,CMHR,CMIP,CMN,CNSC,COBU,CPC,CPMA,CPPIB,CRA,CRC,CRCC,CRRF,CRTC,CSA,CSC,CSEC,CSIS,CSPS,CTA,"
    			+ "DC,DCC,DFO,DND,DRDC,ECCC,EDC,Elections,EPRC,ERC,ESDC,FBCL,FC,FCA,FCAC,FCC,FedDev,FFMC,FIN,FINTRAC,FJA,FPCC,"
    			+ "GAC,GLPA,HC,HRTC,HSMBC,IAAC,IDRC,IGA,INAC,INFC,Investments,IRB,IRCC,IRPDA,ISC,ISED,ITO,JCCBI,JUS,LAC,LPA,"
    			+ "MarineAtlantic,MC,MGERC,Mint,MPCC,NAC,NBC,NCC,NFB,NGC,NPA,NRC,NRCan,NSERC,NSICOP,NSIRA,OAG,OCI,OCL,OCMJ,OCOL,"
    			+ "OFOVC,OHSTC,OIC,Ontario,OPC,OPO,OSB,OSFI,OSGG,OTO,PBC,PC,PCH,PCO,PHAC,PMPRB,POLAR,PPA,PPSC,PS,PSC,PSDPTC,PSIC,"
    			+ "PSLREB,PSP,PSPC,Rail,RCAF,RCMP,RCN,RMCC,SCC,SCC-CCN,SCT,ServCan,SSC,SSHRC,SST,StatCan,TATC,TBS,TC,TCC,TCS,TSB,VAC,"
    			+ "VIA,VMC,VRAB,WAGE,WD,WDBA,YOUTH";
    }

	@GetMapping(value = "/topTaskSurvey")
	public ModelAndView topTaskSurvey(HttpServletRequest request) throws Exception {
		ModelAndView mav = new ModelAndView();
		String lang = request.getParameter("lang");
		mav.setViewName("topTaskSurvey_"+lang);
		return mav;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}