package ca.gc.tbs.controller;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesOutput;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.data.mongodb.core.query.Criteria.where;


import ca.gc.tbs.domain.Problem;

import ca.gc.tbs.repository.ProblemRepository;
import ca.gc.tbs.service.UserService;

@Controller
public class ProblemController {

	public static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("EEE MMM dd yyyy");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final Logger LOG = LoggerFactory.getLogger(ProblemController.class);
	public static final String COLLECTION_PROBLEM = "problem";
	
	String[][] translations = {/* ENGLISH, FRENCH*/{"The answer I need is missing","La réponse dont j’ai besoin n’est pas là"},{"The information isn't clear","L'information n'est pas claire"},{"I can't find the information","Je ne peux pas trouver l'information"},{"The information isn’t clear","L'information n'est pas claire"},{"I’m not in the right place","Je ne suis pas au bon endroit"},{"I'm not in the right place","Je ne suis pas au bon endroit"},{"Something is broken or incorrect","Quelque chose est brisé ou incorrect"}
	,{"Other reason","Autre raison"},{"The information is hard to understand","l'information est difficile à comprendre"},{"Health","Santé"},{"Taxes","Impôt"},{"Travel","Voyage"},{"Public Health Agency of Canada","Agence de santé publique du Canada"},{"Health Canada","Santé Canada"},{"CRA","ARC"},{"ISED","ISDE"},{"Example","Exemple"},{"CEWS","SSUC"},{"CRSB","PCMRE"},{"CRB","PCRE"},{"CRCB","PCREPA"},{"CERS","SUCL"}
	,{"Vaccines","Vaccins"},{"Business","Entreprises"},{"WFHE","DTDE"},{"travel-wizard","assistant-voyage"},{"PTR","DRP"},{"COVID Alert","Alerte COVID"},{"Financial Consumer Agency of Canada", "Agence de la consommation en matière financière du Canada"},{"National Research Council","Conseil national de recherches"},{"Department of Fisheries and Oceans","Pêches et Océans Canada"}
	,{"Money and finances","Argent et finances"},{"Science and innovation","Science et innovation"},{"Environment and natural resources","Environnement et ressources naturelles"}};

	private HashMap<String, String> tagTranslations = new HashMap<String, String>();
	private HashMap<String, String> translationsMap = new HashMap<String, String>(translations.length);
	
	@Autowired
	private ProblemRepository problemRepository;

	@Autowired
	private UserService userService;

	
	public void populateTranslationsMap() {
		for (String[] translation : translations) {
		    translationsMap.put(translation[0], translation[1]);
		}
	}
	// This function grabs all the models and associated URLs from the google spreadsheet.
	public void importTagTranslations() throws Exception {
		final Reader reader = new InputStreamReader(new URL(
				"https://docs.google.com/spreadsheets/d/1xcoSXKwH0-_N_t056pfeEXzAXseZhpFMnvUsvmF0OBw/export?format=csv")
						.openConnection().getInputStream(),
				"UTF-8");
		final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
		try {
			for (final CSVRecord record : parser) {
				try {
					if(!record.get("FRENCH_TAG").equals(""))
						tagTranslations.put(record.get("ENGLISH_TAG"), record.get("FRENCH_TAG"));			
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
		} finally {
			parser.close();
			reader.close();
		}
	}
	
			
	String lang = "";

	@GetMapping(value = "/pageFeedback")
	public ModelAndView pageFeedback(HttpServletRequest request) throws Exception {
		ModelAndView mav = new ModelAndView();
		lang = request.getParameter("lang");
		importTagTranslations();
		populateTranslationsMap();
		//uniqueValues();
		//System.out.println(tagTranslations.size());
		mav.setViewName("pageFeedback_" + lang);
		return mav;
	}
	
    @RequestMapping(value = "/problemData") 
    @ResponseBody
    public DataTablesOutput<Problem> list(@Valid DataTablesInput input, HttpServletRequest request)  {
    	
    	Criteria findProcessed = where("processed").is("true");
    	
		if(lang.equals("en")) {
			
			String dateSearchVal = input.getColumn("problemDate").get().getSearch().getValue();

	    	if(dateSearchVal.contains(":")) {
	    		
	    		String[] ret = dateSearchVal.split(":");
	    
	    		if(ret.length == 2) {
	    		
		    		String dateSearchValA = ret[0];
		    		
		    		String dateSearchValB = ret[1];
		    		
		    		input.getColumn("problemDate").get().getSearch().setValue("");
		
		        	Criteria dateCriteria = where("problemDate").gte(dateSearchValA).lte(dateSearchValB);
		        	
		    		if(dateSearchValA != "" && dateSearchValB != "") {
		    			return problemRepository.findAll(input, dateCriteria);
		    		}
	    		} 
	    	}
	    	return problemRepository.findAll(input, findProcessed);
		}
		if(lang.equals("fr")) {
			String dateSearchVal = input.getColumn("problemDate").get().getSearch().getValue();

	    	if(dateSearchVal.contains(":")) {
	    		
	    		String[] ret = dateSearchVal.split(":");
	    
	    		if(ret.length == 2) {
	    		
		    		String dateSearchValA = ret[0];
		    		
		    		String dateSearchValB = ret[1];
		    		
		    		input.getColumn("problemDate").get().getSearch().setValue("");
		
		        	Criteria dateCriteria = where("problemDate").gte(dateSearchValA).lte(dateSearchValB);
		        	
		    		if(dateSearchValA != "" && dateSearchValB != "") {
		    			DataTablesOutput<Problem> problems = problemRepository.findAll(input, dateCriteria, findProcessed); // this part checks date range and returns without translations
		    			for(int i = 0; i < problems.getData().size(); i++) {
		    	    		problems.getData().get(i).setInstitution(translationsMap.get(problems.getData().get(i).getInstitution()));
		    	    		problems.getData().get(i).setProblem(translationsMap.get(problems.getData().get(i).getProblem()));
		    	    		problems.getData().get(i).setTheme(translationsMap.get(problems.getData().get(i).getTheme()));
		    	    		problems.getData().get(i).setSection(translationsMap.get(problems.getData().get(i).getSection()));
		    	    		
		    	    		List<String> tags = problems.getData().get(i).getTags();
		    	    		for(int j = 0; j < tags.size(); j++) {
		    	    			if(tagTranslations.containsKey(tags.get(j)))
		    	    				tags.set(j, tagTranslations.get(tags.get(j)));
		    	    		}
		    	    	}
		    	    	return problems;
		    		}
	    		} 
	    	}
	    	DataTablesOutput<Problem> problems = problemRepository.findAll(input, findProcessed);
	    	for(int i = 0; i < problems.getData().size(); i++) {
	    		problems.getData().get(i).setInstitution(translationsMap.get(problems.getData().get(i).getInstitution()));
	    		problems.getData().get(i).setProblem(translationsMap.get(problems.getData().get(i).getProblem()));
	    		problems.getData().get(i).setTheme(translationsMap.get(problems.getData().get(i).getTheme()));
	    		problems.getData().get(i).setSection(translationsMap.get(problems.getData().get(i).getSection()));
	    		
	    		List<String> tags = problems.getData().get(i).getTags();
	    		for(int j = 0; j < tags.size(); j++) {
	    			if(tagTranslations.containsKey(tags.get(j)))
	    				tags.set(j, tagTranslations.get(tags.get(j)));
	    		}
	    	}
	    	return problems;
		}
		return null;
	}
	
    /*
    
    @CrossOrigin(origins = "*")
	@PostMapping(value = "/addProblem")
	public View addProblem(HttpServletRequest request) {

		try {
			String problemDetails = request.getParameter("problemDetails");
			Problem problem = new Problem(System.currentTimeMillis() + "", request.getParameter("url"),
					DATE_FORMAT.format(new Date()), request.getParameter("problem"), problemDetails,
					request.getParameter("language"), "", "", "", "Test", request.getParameter("institution"),
					request.getParameter("theme"), request.getParameter("section"));
			problem.setProblemDate(INPUT_FORMAT.format(new Date()));
			problem.setProcessed("true");
			problemRepository.save(problem);
			return new RedirectView("/pageFeedback");
		} catch (Exception e) {
			return new RedirectView("/error");
		}
	}
	
	public void uniqueValues() {
		List<Problem> problems = problemRepository.findAll();
     	for(Problem p: problems) {
    			sections.add(p.getSection());
    			institutions.add(p.getInstitution());
    			themes.add(p.getTheme());
     	}
     	System.out.println(sections.toString());
     	System.out.println(institutions.toString());
     	System.out.println(themes.toString());
	}
    
	@PostMapping(value = "/deleteTag")
	public @ResponseBody String deleteTag(HttpServletRequest request) {
		try {
			String tag = request.getParameter("tag");
			Optional<Problem> opt = problemRepository.findById(request.getParameter("id"));
			Problem problem = opt.get();
			problem.getTags().remove(tag);
			this.problemRepository.save(problem);
			return this.generateTagHtml(problem);
		} catch (Exception e) {
			return "Error:" + e.getMessage();
		}
	}

	@PostMapping(value = "/updateTags")
	public @ResponseBody String updateTags(HttpServletRequest request) {
		try {
			Optional<Problem> opt = problemRepository.findById(request.getParameter("id"));
			String tags[] = request.getParameter("tags").split(",");
			for (int i = 0; i < tags.length; i++) {
				tags[i] = tags[i].trim();
			}
			Problem problem = opt.get();
			problem.setTags(Arrays.asList(tags));
			this.problemRepository.save(problem);
			return this.generateTagHtml(problem);
		} catch (Exception e) {
			return "Error:" + e.getMessage();
		}
	}

	@PostMapping(value = "/updateProblem")
	public @ResponseBody String updateProblem(HttpServletRequest request) {
		try {
			Optional<Problem> opt = problemRepository.findById(request.getParameter("id"));
			Problem problem = opt.get();
			problem.setResolution(request.getParameter("resolution"));
			problem.setResolutionDate(DATE_FORMAT.format(new Date()));
			this.problemRepository.save(problem);
			return problem.getResolutionDate();
		} catch (Exception e) {
			return "Error:" + e.getMessage();
		}
	}

	@GetMapping(value = "/deleteProblem")
	public @ResponseBody String deleteProblem(HttpServletRequest request) {
		try {
			this.problemRepository.deleteById(request.getParameter("id"));
			return "deleted";
		} catch (Exception e) {
			return "Error:" + e.getMessage();
		}
	}


	public String generateTagHtml(Problem problem) {
		StringBuilder builder = new StringBuilder();
		for (String tag : problem.getTags()) {
			builder.append("<button id='tagDelete" + problem.getId() + "' class='tagDeleteBtn btn btn-xs'>" + tag
					+ " </button>");
		}
		return builder.toString();
	}

*/
	
	@GetMapping(value = "/testForm")
	public String testForm() {
		return "testForm";
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
