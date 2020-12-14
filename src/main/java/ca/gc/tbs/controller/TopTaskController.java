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

	public static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("EEE MMM dd yyyy");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final Logger LOG = LoggerFactory.getLogger(TopTaskController.class);
	public static final String COLLECTION_PROBLEM = "problem";
	public String[] OUTPUT_HEADERS_EN = { "Institution", "Section", "Theme", "Language", "URL",
			"Problem", "Problem Details", "Date Entered", "Tags", "Resolution", "Resolution Date",
			"Action" };

	@Autowired
	private TopTaskRepository topTaskRepository;

	@Autowired
	private UserService userService;

    @RequestMapping(value = "/topTaskData") 
    @ResponseBody
    public DataTablesOutput<TopTaskSurvey> list(@Valid DataTablesInput input)  {
    	Criteria findProcessed = where("processed").is("false");
    	return topTaskRepository.findAll(input, findProcessed);
	}
   
	@GetMapping(value = "/topTaskDashboard")
	public ModelAndView topTaskDashboard() throws Exception {
		ModelAndView mav = new ModelAndView();
		//mav.addObject("data", this.getProblemData());
		mav.setViewName("topTaskDashboard");
		return mav;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}