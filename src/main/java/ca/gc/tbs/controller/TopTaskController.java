package ca.gc.tbs.controller;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.domain.TopTaskSurvey;
import ca.gc.tbs.repository.TopTaskRepository;
import ca.gc.tbs.service.ProblemDateService;
import ca.gc.tbs.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesInput.Column;
import org.springframework.data.mongodb.datatables.DataTablesOutput;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;


@Controller
public class TopTaskController {

    private static final Logger LOG = LoggerFactory.getLogger(TopTaskController.class);
    @Autowired
    private TopTaskRepository topTaskRepository;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/topTaskData")
    @ResponseBody
    public DataTablesOutput<TopTaskSurvey> list(@Valid DataTablesInput input, HttpServletRequest request) {
        String pageLang = (String) request.getSession().getAttribute("lang");
        String departmentFilterVal = request.getParameter("department");
        String themeFilterVal = request.getParameter("theme");
        String[] taskFilterVals = request.getParameterValues("tasks[]");
        String startDateVal = request.getParameter("startDate");
        String endDateVal = request.getParameter("endDate");
        boolean includeCommentsOnly = request.getParameter("includeCommentsOnly").equals("true");


        Criteria criteria = Criteria.where("processed").is("true");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startDateVal != null && endDateVal != null) {
            LocalDate start = LocalDate.parse(startDateVal, formatter);
            LocalDate end = LocalDate.parse(endDateVal, formatter);
            criteria.and("dateTime").gte(start.format(formatter)).lte(end.format(formatter));
        }

        if(includeCommentsOnly) {
            List<Criteria> nonEmptyCriteria = createNonEmptyCriteria();
            criteria.orOperator(nonEmptyCriteria.toArray(new Criteria[0]));
        }
        if (themeFilterVal != null && !themeFilterVal.isEmpty()) {
            criteria.and("theme").is(themeFilterVal);
        }
        if (taskFilterVals != null && taskFilterVals.length > 0) {
            // Create a list to hold the title criteria
            List<Criteria> taskCriterias = new ArrayList<>();
            // Iterate over the titles and add each one as a criterion
            for (String task : taskFilterVals) {
                taskCriterias.add(Criteria.where("task").is(task));
            }
            // Combine all title criteria using AND operation
            criteria.orOperator(taskCriterias.toArray(new Criteria[0]));
            System.out.println("Tasks received: " + Arrays.toString(taskFilterVals));
        }
        if (departmentFilterVal != null && !departmentFilterVal.isEmpty()) {
            criteria.and("dept").is(departmentFilterVal);
        }

        return topTaskRepository.findAll(input, criteria);
    }


    private List<Criteria> createNonEmptyCriteria() {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("taskOther").exists(true).ne(""));
        criteriaList.add(Criteria.where("themeOther").exists(true).ne(""));
        criteriaList.add(Criteria.where("taskWhyNotComment").exists(true).ne(""));
        criteriaList.add(Criteria.where("taskImproveComment").exists(true).ne(""));
        return criteriaList;
    }

    @RequestMapping(value = "/topTaskSurvey/departments")
    @ResponseBody
    public String departmentData(HttpServletRequest request) {
        return "AAFC / AAC,ATSSC / SCDATA,CATSA / ACSTA,CFIA / ACIA,CIRNAC / RCAANC,NSERC / CRSNG,CBSA / ASFC,CCG / GCC,CGC / CCG,"
                + "CIHR / IRSC,CIPO / OPIC,CRA / ARC,CRTC / CRTC,CSA / ASC,CSEC / CSTC,CSPS / EFPC,DFO / MPO,DND / MDN,ECCC / ECCC,"
                + "ESDC / EDSC,FCAC / ACFC,FIN / FIN,GAC / AMC,HC / SC,INFC / INFC,IRCC / IRCC,ISC / SAC,ISED / ISDE,JUS / JUS,"
                + "LAC / BAC,NFB / ONF,NRC / CNRC,NRCan / RNCan,OSB / BSF,PBC / CLCC,PC / PC,PCH / PCH,PCO / BCP,PHAC / ASPC,"
                + "PS / SP,PSC / CFP,SSC / PSC,PSPC / SPAC,RCMP / GRC,StatCan / StatCan,TBS / SCT,TC / TC,VAC / ACC,WAGE / FEGC,WD / DEO";
    }

    @GetMapping(value = "/topTaskSurvey")
    public ModelAndView topTaskSurvey(HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView();
        String lang = (String) request.getSession().getAttribute("lang");
        mav.setViewName("topTaskSurvey_" + lang);

        mav.addObject("earliestDate", "2020-09-01");
        mav.addObject("latestDate", "2024-03-18");

        return mav;
    }


    @GetMapping("/taskNames")
    @ResponseBody
    public List<String> getTaskNames(@RequestParam(name = "search", required = false) String search) {
        if (search != null && !search.isEmpty()) {
            // Use the new repository method to filter page titles based on the search term
            return topTaskRepository.findTaskTitlesBySearch(search);
        } else {
            // Return all page titles if no search term is provided
            return topTaskRepository.findDistinctTaskNames();
        }
    }
    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}