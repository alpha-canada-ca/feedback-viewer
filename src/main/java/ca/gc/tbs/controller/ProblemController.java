package ca.gc.tbs.controller;

import java.text.SimpleDateFormat;
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
import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesInput.Column;
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


import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.domain.User;

import ca.gc.tbs.repository.ProblemRepository;
import ca.gc.tbs.service.UserService;

@Controller
public class ProblemController {

	public static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("EEE MMM dd yyyy");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final Logger LOG = LoggerFactory.getLogger(ProblemController.class);
	public static final String COLLECTION_PROBLEM = "problem";
	public String[] OUTPUT_HEADERS_EN = { "Institution", "Section", "Theme", "Language", "URL",
			"Problem", "Problem Details", "Date Entered", "Tags", "Resolution", "Resolution Date",
			"Action" };

	
	
	
	@Autowired
	private ProblemRepository problemRepository;



	@Autowired
	private UserService userService;

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

		

    @RequestMapping(value = "/problemData") 
    @ResponseBody
    public DataTablesOutput<Problem> list(@Valid DataTablesInput input)  {

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
    	
    	Criteria findProcessed = where("processed").is("true") ;
    	return problemRepository.findAll(input, findProcessed);
	}
	
    
    
    
    @RequestMapping(value = "/excelProblemData") 
    @ResponseBody
    public DataTablesOutput<Problem> excel(@Valid DataTablesInput input)  {
    	input.setStart(0);
    	input.setLength(-1);
    	System.out.println(problemRepository.findAll(input));
    	return problemRepository.findAll(input);
	}
    
    
    /*
    @PostMapping(value = "/csv") 
    @ResponseBody
    public DataTablesOutput<Problem> csv(@Valid DataTablesInput input)  {
    	Criteria findProcessed = where("processed").is("false");
    	return problemRepository.findAll(input, findProcessed);
	}
    */
    
    


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
	public void parseDates() {
		List<Problem> problems = null;
		problems = this.problemRepository.findByProcessed("true");
		for (Problem problem : problems) {
			try {
				if(problem.getProblemDate().contains("GMT")) {
					System.out.println("before parse Date for: " + problem.getProblemDate());
					problem.setProblemDate(DATE_FORMAT.format(INPUT_FORMAT.parse(problem.getProblemDate())));
					this.problemRepository.save(problem);
					System.out.println("parsed Date for: " + problem.getProblemDate());
				}
			}
			catch (Exception e) {
				LOG.error("Could not parse date because:" + problem.getId() + " " + e.getMessage());
			}
		}
	}

	public String getProblemData() {

		String returnData = "";

		StringBuilder finalBuilder = new StringBuilder();
		User user = this.userService.getCurrentUser();
		List<Problem> problems = null;
		if (this.userService.isAdmin(user)) {
			problems = this.problemRepository.findByProcessed("true");
		} else {
			problems = this.problemRepository.findByProcessedAndInstitution("true", user.getInstitution());
		}
		for (Problem problem : problems) {
			try {
				StringBuilder builder = new StringBuilder();
				builder.append("<tr>");
				builder.append("<td>" + problem.getInstitution() + "</td>");
				builder.append("<td>" + problem.getSection() + "</td>");
				builder.append("<td>" + problem.getTheme() + "</td>");
				builder.append("<td>" + problem.getLanguage() + "</td>");
				builder.append("<td>" + problem.getUrl() + "</td>");
				builder.append("<td>" + problem.getProblem() + "</td>");
				builder.append("<td>" + problem.getProblemDetails() + "</td>");
				builder.append("<td>" + problem.getProblemDate() + "</td>");
				builder.append("<td class='tagCol'>");
				builder.append(this.generateTagHtml(problem));
				builder.append("</td>");
				try {
					builder.append("<td>" + problem.getResolution() + "</td>");
					builder.append("<td>" + problem.getResolutionDate() + "</td>");
				} catch (Exception e) {
					builder.append("<td></td>");
					builder.append("<td></td>");
				}
				builder.append("<td><div class='btn-group'><button id='tag" + problem.getId()
						+ "' class='btn btn-xs tagBtn'>Tag</button><button id='resolve" + problem.getId()
						+ "' class='btn btn-xs resolveBtn'>Resolve</button><button id='delete" + problem.getId()
						+ "' class='btn btn-xs deleteBtn'><span class='fas fa-trash-alt'></span><span class='wb-inv'>Delete</span></button></div></td>");
				builder.append("</tr>");
				finalBuilder.append(builder);
			} catch (Exception e) {
				LOG.error("Could not display row because:" + problem.getId() + " " + e.getMessage());
			}
		}
		returnData = finalBuilder.toString();

		return returnData;
	}

	

	@GetMapping(value = "/pageFeedback")
	public ModelAndView pageFeedback() throws Exception {
		ModelAndView mav = new ModelAndView();
		//mav.addObject("data", this.getProblemData());
		mav.setViewName("pageFeedback");
		return mav;
	}



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
