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
    
    
    @RequestMapping(value = "/topTaskSurvey/departments")
    @ResponseBody
	public String departmentData(HttpServletRequest request) {
    	return  "AAFC / AAC,ATSSC / SCDATA,CATSA / ACSTA,CFIA / ACIA,CIRNAC / RCAANC,NSERC / CRSNG,CBSA / ASFC,CCG / GCC,CGC / CCG,"
				+ "CIHR / IRSC,CIPO / OPIC,CRA / ARC,CRTC / CRTC,CSA / ASC,CSEC / CSTC,CSPS / EFPC,DFO / MPO,DND / MDN,ECCC / ECCC,"
				+ "ESDC / EDSC,FCAC / ACFC,FIN / FIN,GAC / AMC,HC / SC,INFC / INFC,IRCC / IRCC,ISC / SAC,ISED / ISDE,JUS / JUS,"
				+ "LAC / BAC,NFB / ONF,NRC / CNRC,NRCan / RNCan,OSB / BSF,PBC / CLCC,PC / PC,PCH / PCH,PCO / BCP,PHAC / ASPC,"
				+ "PS / SP,PSC / CFP,SSC / PSC,PSPC / SPAC,RCMP / GRC,StatCan / StatCan,TBS / SCT,TC / TC,VAC / ACC,WAGE / FEGC,WD / DEO";
    }
	@RequestMapping(value = "/topTaskSurvey/taskList")
	@ResponseBody
	public String[] taskList(HttpServletRequest request){
		return new String[]{"COVID-19 cases, statistics", "Vaccines and treatment", "Get your Canadian COVID-19 proof of vaccination", "Symptoms of COVID-19", "Testing for COVID-19", "Prevention, masks", "Canada Recovery Caregiving Benefit (CRCB)", "Canada Recovery Sickness Benefit (CRSB)", "Canada Recovery Benefit (CRB)", "Canada Worker Lockdown Benefit (CWLB)", "Tourism and Hospitality Recovery Program (THRP)", "Hardest-Hit Businesses Recovery Program (HHBRP)", "Canada Emergency Wage Subsidy (CEWS)", "Canada Emergency Rent Subsidy (CERS)", "Canada Recovery Hiring Program (CRHP)", "COVID-19 requirements for travellers to Canada", "Travel inside Canada", "Travel outside Canada", "Other - Reason for my visit is not in this list", "Jobs in the private sector", "Jobs in the federal public service", "Find a student job in the federal public service", "Find an apprenticeship", "Join the army", "Apply to work and travel abroad with International Experience Canada (IEC) as a Canadian", "Apply for a work permit", "Apply for a Social Insurance Number (SIN)", "Get job training", "My Service Canada Account (MSCA) - View Records of Employment (ROE)", "Post a job in the Job Bank", "Hire a foreign worker", "Apply for Guaranteed Income Supplement (GIS)", "Old Age Seccurity (OAS) - Apply for Old Age Security", "Old Age Seccurity (OAS) - View payment amounts", "Canada Pension Plan (CPP) - Apply for Canada Pension Plan", "Calculate your Retirement Payments", "Find a pension payment date", "Employment Insurance (EI) - Apply for Employment Insurance", "Employment Insurance (EI) - Submit an Employment Insurance Report", "My Service Canada Account (MSCA) - Sign in", "My Service Canada Account (MSCA) - Register for an account", "Sign up for direct deposit or update your banking information", "Apply for an apprenticeship grant or loan", "Apply for funding for Canada summer jobs", "Support for Temporary Foreign Workers", "Apply for Canadian citizenship", "Check if you need a visa or electronic travel authorization (eTA) to travel to Canada", "Check processing times", "Check your application status", "Contact Immigration, Refugees and Citizenship Canada", "Find a designated learning institution", "Find a visa application centre", "Find an IRCC application package or form", "Find out if you need to give your fingerprints and photo (biometrics) and where to do that", "Find your National Occupational Classification (NOC)", "Get, renew or replace a permanent resident card", "How to extend your stay in Canada", "Immigrate as a provincial nominee", "Immigrate through Express Entry", "IRCC secure account - register, sign in, help", "Pay your fees online", "Sponsor your family members to immigrate to Canada", "Study in Canada – for non-Canadians", "Use the Authorized Paid Representatives Portal", "Apply for a copyright", "Apply for a patent", "Apply for a trademark", "Apply for the Farm Debt Mediation Service", "Consult a trade commissioner to receive international business information", "Download RETScreen clean energy management software", "Establish or expand your business in Canada as a foreign investor", "File an annual corporate return", "Find a bankruptcy or insolvency record", "Find a corporation", "Find a decision, notice, order, policy on radio, television or telecommunications", "Find a patent", "Find a trademark", "Find Canadian importing companies", "Find government grants, loans and financing", "Trade negotiations and agreements", "Incorporate a business", "Look up industry statistics", "Manage and report goods imported into Canada", "Register a sole-proprietorship or partnership (not incorporated)", "Review customs tariffs for importing goods to Canada", "Set up a not-for-profit corporation", "Apply for a study permit", "ArriveCAN - Submit travel information or daily symptoms", "Boarding flights to and within Canada", "Check provincial and territorial restrictions", "Check the Canada-United States border wait times – from Canada", "Check the United States-Canada border wait times – from the United States", "Check vaccination requirements to enter Canada", "Compassionate travel quarantine exemptions (caring for others, funerals and support)", "Contact a federal office – outside Canada (embassy, consulate, trade office)", "Contact the Canada Border Services Agency (CBSA)", "COVID-19 testing requirements for travellers", "Publications and guides", "Find out about identification requirements for crossing the Canadian border", "Find out if you can enter Canada", "Find out what you can bring on a plane", "Follow mandatory quarantine or isolation", "Identify what you can bring home to Canada", "Latest news – Travel and emergency assistance", "Apply for a new Canadian passport", "Renew a Canadian passport", "Register as a Canadian living or travelling outside Canada", "Visit a national park or historic site", "Travel advice and advisories", "Travel health notices in effect for a country", "Apply for a visa when travelling outside Canada", "Apply for a visitor visa to Canada", "Apply for a benefit for Indigenous people", "My Service Canada Account (MSCA) - Check your Employment Insurance (EI) claim status and correspondence", "My Service Canada Account (MSCA) - View payment information", "My Service Canada Account (MSCA) - Check your CPP/OAS application status", "My Service Canada Account (MSCA) - Check your estimated monthly Canada Pension Plan (CPP) benefits", "My Service Canada Account (MSCA) - Get your T4E, T4A or NR4 tax slips", "My Service Canada Account (MSCA) - View/Change your personal information", "Employment Insurance (EI) - Check eligibility", "Canada Pension Plan (CPP) - Check status of application", "Canada Pension Plan (CPP) - Estimate monthly benefits", "Canada Pension Plan (CPP) - Get statement of benefits", "Canada Pension Plan (CPP) - Check payment information", "Canada Pension Plan Disability (CPPD) - Apply for Canada Pension Plan Disability benefits", "Canada Pension Plan (CPP) - Apply for a Survivors Pension", "Apply for a student grant or loan", "Get student loan repayment assistance", "Delay repayment of a student loan (still studying)", "Learn about benefits for people with disabilities", "Apply for the Canada Child Benefit (CCB)", "Look up expected child support payments", "Sign into or create a Veterans Affairs Canada (VAC) account", "Consult Canada's Food Guide", "Contact Health Canada or the Public Health Agency of Canada", "Find information on family violence and how to get help", "Find information on producing and selling cannabis", "Find out if a medical device is licenced for sale in Canada", "Find out the hazardous properties of a human pathogen and how to work with it in a laboratory setting", "Find out the safe cooking temperature for food", "Find out the symptoms or risks for a disease or condition", "Find out whether a drug is approved for use and sale in Canada", "Find out whether a natural health product is licensed for use in Canada", "Find out who is eligible for and how to access medical assistance in dying", "Get consumer or health product recalls and safety alerts", "Get food recalls and safety alerts", "Get guidelines for immunization and vaccination practices", "Get information on vaping or the risk of vaping", "Learn about Canada's health care system", "Learn about cannabis, its health effects, its medical use, its legalization in Canada", "Learn about food labels", "Learn about hazardous materials in the workplace", "Learn about mental health support and how to get help", "Learn about outbreaks of food and water-borne illnesses and diseases in Canada", "Learn about the problematic use of drugs (such as opioids) and how to get help", "Look up the key social determinants of health in Canada", "Report a side effect to a drug, medical device, vaccine, or natural health product", "Apply for the disability tax credit", "Calculate payroll deductions", "Change my address with the Canada Revenue Agency", "Check online mail", "Check the balance owing on a corporate income tax account", "File a GST/HST return", "File an employer information return, such as a T4 Summary", "Find a Canada Revenue Agency telephone number", "Find my Registered Retirement Savings Plan (RRSP) contribution limit", "Find my Tax-free savings Account (TFSA) contribution limit", "Find out how much my benefit payment will be", "Find out when I can expect my tax refund", "Find out which tax deductions, credits, or expenses I can claim", "Find tax software", "Find the income tax rates", "Find the next benefit payment date", "Get a copy of a notice of assessment or reassessment", "Get a GST/HST access code", "Get an income tax form and guide", "Get the TD1 Personal Tax Credits Return form", "Look up a charitable organization", "Make a payment to the Canada Revenue Agency", "Open a GST/HST account", "Access My Account", "Open a My Account", "Access My Business Account", "Open a My Business Account", "Report suspected fraud", "See my T4 information", "Calculate the magnetic declination for a specified location and date", "Check past weather conditions", "Consult Canada's plan to fight climate change", "Consult the reporting and inventory of greenhouse gas emissions", "Current and past earthquakes in Canada", "Current and past wildfires in Canada", "Find a fuel efficient vehicle", "Find out about forest conservation", "Find out about the major pollutants and toxic substances affecting the environment", "Find out how Canada is managing pollution", "Find out if you need a fishing licence", "Find out what Canada is doing about climate change", "Find out which energy efficiency incentives and grants are available", "Get an emergency kit (to be prepared during a natural disaster, power outage, bomb threat, etc)", "Get and understand Air Quality Health Index ratings for my location", "Get current and forecasted ice conditions", "Get current and forecasted weather for your location", "Get marine conditions (tides and ocean water levels)", "Get satellite and air imagery", "Get space weather forecasts", "How to prepare for seasonal severe weather conditions", "Learn about climate change", "Learn about managing and reducing waste", "Look up a species at risk in Canada", "Make a home energy efficient", "Renewable energy in Canada", "Understand how pricing pollution works in Canada", "Use a geodetic reference tool", "Water level and flood monitoring in Canada (inland)", "Access cyber security bulletins", "Apply for a job in the military", "Civilian jobs with National Defence", "Find information on a specific unit, squadron or ship", "Find out the various ranks in the military", "Find out where the military is currently deployed on operations around the world", "Join the Cadets", "Learn about Canada's defence policy", "Learn about cyber security threats", "Learn about Passenger Protect program", "Learn about the Canadian Army Reserve", "Learn about the current list of terrorist entities", "Learn about the required documents needed for air travel", "Pay rates for military members", "Read the Defence administrative orders and directives (DAOD)", "Read the Queen's regulations and orders (QR&O)", "Search for jobs in the military", "Apply for a tax credit — film or video (CAVCO)", "Apply to a grant or funding program — arts", "Apply to a grant or funding program — cultural preservation", "Apply to a grant or funding program — multiculturalism", "Apply to a grant or funding program — music", "Apply to a grant or funding program — sports", "Participate in Winterlude events and activities", "Participate in Canada Day celebrations", "Participate in National Indigenous Peoples Day", "Participate in Remembrance Day", "Find information on human rights in Canada", "Learn about Aboriginal history in Canada", "Learn about the First World War", "Look up soldiers who lost their lives in the military or RCMP", "Learn about the Governor General or Lieutenant Governors and the appointment process", "Download the lyrics and recordings of Canada’s national anthem", "Learn about the official symbols of Canada", "Discover the origins of the names of Canada's provincial and territorial capitals", "Research your genealogy and family history", "Search births, marriages and deaths recorded in Canada", "View current census data", "Find student job and internship programs", "Find a decision, notice, order or policy about radio, television or telecommunications", "Apply for a criminal record suspension", "Apply for a cannabis record suspension", "Get a criminal record check", "Apply for or renew your firearms licence", "Check the status of your firearms licence application", "Change your address for your firearms licence", "Contact a chief firearms officer", "Learn about firearms safety, storage and when you need a licence", "Join the RCMP", "Find your local RCMP detachment (police service)", "Check the National Sex Offender Registry", "Check the Royal Canadian Mounted Police (RCMP) wanted list", "Consult a law, treaty or regulation", "Learn about how the Government of Canada responds to emergency events", "Learn about national search and rescue", "Look up a Canadian disaster", "Prepare an emergency plan", "Apply for the Memorial Grant Program for First Responders", "Apply for your transportation security clearance", "Assessing medical fitness of aviation personnel", "Building, maintaining and registering an aircraft", "Find out how to import or bring a vehicle into Canada", "Flying an aircraft", "Find where you can fly your drone", "Learn about advanced vehicle technologies", "Learn about child car seat safety", "Find if there is a defect or recall for your vehicle, tires or child car seat", "Learn about grade crossing safety", "Learn about safety awareness when transporting dangerous goods", "Learn about shipping or mailing lithium batteries", "Learn about the containers required to transport dangerous goods by rail", "Learn how to build an emergency response assistance plan in case of an accident while shipping dangerous goods", "Learn how to get your transportation security clearance", "Learn how to register your vessel", "Learn tips on safe boating", "Licensing and training of aviation personnel", "Operating a commercial air service", "Operating an airport or aerodrome", "Read the Canadian Aviation Regulations (CARs)", "Read the regulations for transporting dangerous goods", "Read the rules for operating a federal railway", "Find statistics for vehicle collisions", "Training and certification of individuals", "Authenticate documents so that they can be recognized outside Canada", "Canada's feminist international assistance policy", "Canada’s actions in a specific country or region", "Canada’s foreign policy and international relations", "Canada’s response to conflicts, crisis and disasters", "Contact Global Affairs Canada – offices in Canada", "Country insights (cultural information)", "Democracy and good governance", "Environment and climate change", "Find international study or research opportunities in Canada for non-Canadians", "Gender equality and the empowerment of women and girls", "Human rights and dignity", "International assistance budget (humanitarian aid and development projects)", "International assistance projects funded by Canada", "International treaties signed by Canada", "Jobs with Global Affairs Canada (embassies/consulate, trade offices and partnership organizations)", "Latest news – Foreign affairs (international relations and global issues)", "Latest news – International development (humanitarian assistance and development projects)", "Peace and security", "Right to education (international development)", "Sanctions imposed by Canada against specific countries, organisations and individuals", "Scholarships to study in Canada – for non-Canadians", "Scholarships to study outside Canada – for Canadians", "Seek funding opportunities for international aid projects", "Sustainable agriculture, green technologies and renewable energy", "The right to health and nutrition", "United nations (UN)", "Volunteer for work in a humanitarian aid project", "Buying a home", "Calculate if you qualify for a mortgage", "Calculate your credit card payments", "Calculate your mortgage payments and/or prepayments", "Cashing a Government of Canada cheque", "Check Canada's tax treaties", "Compare Canadian bank accounts", "Consult the Federal Budget", "Create a plan to get out of debt and/or savings goals", "Determine current market prices for lumber, crude oil and other fuels", "Find available disability benefits", "Find information on loans and lines of credit", "Find out about student lines of credit", "Getting a home equity line of credit", "How much you need for a down payment", "Improving your credit score", "Make a budget", "Make a will and plan your estate", "Making a complaint to your financial institution", "Opening a bank account", "Ordering your credit report and score", "Understand severance pay", "Apply for a grant for technological innovation - Industrial Research Assistance Program (IRAP)", "Find a scientific, technical or medical journal", "View the northern lights camera", "Solar eclipses times and appreciation guide", "Build your own projector to watch solar eclipses safely", "Read biographies of Canadian astronauts", "Official time across Canada", "Check sunrise and sunset times", "Get the latest National building code for Canada", "Buy building codes or Certified Reference Materials", "Consult the Canadian National Master Construction Specifications", "View statistical data", "Apply for an International Standard Book Number (ISBN) / catalogue number", "Consult the annual report on advertising activities", "Consult the Clerk’s Annual Report to the Prime Minister", "Consult the Speech from the Throne", "Examine the activities of the Independent Advisory Board for Senate Appointments", "Find a government department or agency", "Read a report or a publication", "Find a government policy, directive, standard or guideline", "Find public opinion research requirements", "Find travel and hospitality expenses of government officials", "Learn about Canada’s democratic institutions", "Learn about Governor in Council appointments process", "Learn about health and safety in federal properties", "Federal government property management, maintenance, and construction", "Learn about the Clerk of the Privy Council", "Learn about the relationship between provinces and territories", "Learn how the government provides financial support to provinces and territories", "Make an access to information or personal information request", "Review of the state of bilingualism in the federal Public Service", "Review the progress of Government of Canada commitments", "Search government grants and contributions", "Search the Orders in Council database", "See how the Government of Canada supports the LGBTQ2+ community", "View public notices in the Canada Gazette", "View Parliament Hill camera", "Calculate my expected pension as a public servant", "Contact the Government of Canada Pension Centre", "Find a benefits form", "Find available benefits for survivors of deceased public servants", "Find guidance on leave requests due to COVID-19", "Find guidance on working remotely during COVID-19", "Find out what is considered harassment in the public service", "Find out when I could retire as a public servant", "Find public service employees rates of pay", "Find public service executives rates of pay", "Find public service staffing tools", "Find public service students rates of pay", "Help with pay issues (pay action requests, pay enquiry)", "Get updates on public service collective bargaining", "Get updates on the status of the Phoenix pay system", "Learn about employee rights and responsibilities related to COVID-19", "Learn about qualifications, competencies and classifications for public service jobs", "Learn about temporary changes to benefits due to COVID-19", "Learn about the public service pension plan", "Learn about your public service pay", "Make a public service dental insurance claim", "Make a public service health care insurance claim", "Read a public service collective agreement", "Read about the performance management program for employees", "See what's covered by dental insurance in the public service", "See what's covered by health insurance in the public service", "Access your pay (MyGCPay, CWA, Phoenix)"};
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