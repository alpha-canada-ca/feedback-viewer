package ca.gc.tbs.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesOutput;
import org.springframework.data.mongodb.datatables.DataTablesInput.Column;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.data.mongodb.core.query.Criteria.where;


import ca.gc.tbs.domain.TopTaskSurvey;

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
    	
    	List<Column> columns = input.getColumns();
		
		for(Column col: columns) {
			System.out.println(col);
		}
		System.out.println("---------------------");

    	Criteria findProcessed = where("processed").is("true");
    	
    	if(dataSetVal.contains("nonEmpty") && dateSearchVal.contains(":")) {
    		String[] ret = dateSearchVal.split(":");
    	    
    		if(ret.length == 2) {
        		input.getColumn("taskOther").get().getSearch().setValue("");

	    		String dateSearchValA = ret[0];
	    		
	    		String dateSearchValB = ret[1];
	    		
	    		input.getColumn("dateTime").get().getSearch().setValue("");
	
	        	Criteria dateCriteria = where("dateTime").gte(dateSearchValA).lte(dateSearchValB).and("processed").is("true");

	    		if(!dateSearchValA.equals("") && !dateSearchValB.equals("")) {
	    			return topTaskRepository.findAll(input, new Criteria().orOperator(
	        				Criteria.where("taskOther").exists(true).ne(""),
	        				Criteria.where("themeOther").exists(true).ne(""),
	        				Criteria.where("taskWhyNotComment").exists(true).ne(""),
	        				Criteria.where("taskImproveComment").exists(true).ne("")
	        				), dateCriteria);
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
	        	
	    		if(!dateSearchValA.equals("") && !dateSearchValB.equals("")) {
	    			return topTaskRepository.findAll(input, findProcessed, dateCriteria);
	    		}
    		}
    	}
    	if(dataSetVal.contains("nonEmpty")) {
    		
    		input.getColumn("taskOther").get().getSearch().setValue("");
    		
    		return topTaskRepository.findAll(input, findProcessed, new Criteria().orOperator(
    				Criteria.where("taskOther").exists(true).ne(""),
    				Criteria.where("themeOther").exists(true).ne(""),
    				Criteria.where("taskWhyNotComment").exists(true).ne(""),
    				Criteria.where("taskImproveComment").exists(true).ne("")));
    	}
    	return topTaskRepository.findAll(input, findProcessed);
	}
    
    
    @RequestMapping(value = "/topTaskSurvey/tasks")
    @ResponseBody
	public String paginationProblemData(HttpServletRequest request) {
//    	return "AAFC,ACOA,AECL,APA,ATSSC,BDC,BWB,CA,CAFC,CanNor,CANSOFCOM,CART,CAS,CATSA,CB,CBC,CBSA,CCC,CCG,CCI,"
//    			+ "CCOHS,CDC,CDEV,CDIC,CED,CEIC,CER,CFIA,CGC,CHIN,CHRC,CIB,CICS,CIEC,CIHR,CIPO,CIRB,CIRNAC,CITT,CJC,CLC,"
//    			+ "CMAC,CMH,CMHC,CMHR,CMIP,CMN,CNSC,COBU,CPC,CPMA,CPPIB,CRA,CRC,CRCC,CRRF,CRTC,CSA,CSC,CSEC,CSIS,CSPS,CTA,"
//    			+ "DC,DCC,DFO,DND,DRDC,ECCC,EDC,Elections,EPRC,ERC,ESDC,FBCL,FC,FCA,FCAC,FCC,FedDev,FFMC,FIN,FINTRAC,FJA,FPCC,"
//    			+ "GAC,GLPA,HC,HRTC,HSMBC,IAAC,IDRC,IGA,INAC,INFC,Investments,IRB,IRCC / IRCC,IRPDA,ISC,ISED,ITO,JCCBI,JUS,LAC,LPA,"
//    			+ "MarineAtlantic,MC,MGERC,Mint,MPCC,NAC,NBC,NCC,NFB,NGC,NPA,NRC,NRCan,NSERC,NSICOP,NSIRA,OAG,OCI,OCL,OCMJ,OCOL,"
//    			+ "OFOVC,OHSTC,OIC,Ontario,OPC,OPO,OSB,OSFI,OSGG,OTO,PBC,PC,PCH,PCO,PHAC,PMPRB,POLAR,PPA,PPSC,PS,PSC,PSDPTC,PSIC,"
//    			+ "PSLREB,PSP,PSPC,Rail,RCAF,RCMP,RCN,RMCC,SCC,SCC-CCN,SCT,ServCan,SSC,SSHRC,SST,StatCan,TATC,TBS,TC,TCC,TCS,TSB,VAC,"
//    			+ "VIA,VMC,VRAB,WAGE,WD,WDBA,YOUTH";
    	return  "AAFC / AAC,ATSSC / SCDATA,CATSA / ACSTA,CFIA / ACIA,CIRNAC / RCAANC,NSERC / CRSNG,CBSA / ASFC,CCG / GCC,CGC / CCG,"
				+ "CIHR / IRSC,CIPO / OPIC,CRA / ARC,CRTC / CRTC,CSA / ASC,CSEC / CSTC,CSPS / EFPC,DFO / MPO,DND / MDN,ECCC / ECCC,"
				+ "ESDC / EDSC,FCAC / ACFC,FIN / FIN,GAC / AMC,HC / SC,INFC / INFC,IRCC / IRCC,ISC / SAC,ISED / ISDE,JUS / JUS,"
				+ "LAC / BAC,NFB / ONF,NRC / CNRC,NRCan / RNCan,OSB / BSF,PBC / CLCC,PC / PC,PCH / PCH,PCO / BCP,PHAC / ASPC,"
				+ "PS / SP,PSC / CFP,SSC / PSC,PSPC / SPAC,RCMP / GRC,StatCan / StatCan,TBS / SCT,TC / TC,VAC / ACC,WAGE / FEGC,WD / DEO";
    }
    			

	@GetMapping(value = "/topTaskSurvey")
	public ModelAndView topTaskSurvey(HttpServletRequest request) throws Exception {
		ModelAndView mav = new ModelAndView();
		String lang = (String) request.getSession().getAttribute("lang");
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