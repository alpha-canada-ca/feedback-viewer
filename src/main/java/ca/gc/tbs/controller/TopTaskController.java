package ca.gc.tbs.controller;

import ca.gc.tbs.domain.TopTaskSurvey;
import ca.gc.tbs.repository.TopTaskRepository;
import ca.gc.tbs.service.ProblemDateService;
import ca.gc.tbs.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.datatables.DataTablesInput;
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
import java.util.stream.Collectors;


@Controller
public class TopTaskController {

    private static final Logger LOG = LoggerFactory.getLogger(TopTaskController.class);
    @Autowired
    private TopTaskRepository topTaskRepository;
    private int totalDistinctTasks = 0;

    private int totalTaskCount = 0;
    @Autowired
    private UserService userService;

    @Autowired
    private ProblemDateService problemDateService;

    @RequestMapping(value = "/topTask/totalDistinctTasks")
    @ResponseBody
    public String totalDistinctTasks() {
        return String.valueOf(totalDistinctTasks);
    }

    @RequestMapping(value = "/topTask/totalTaskCount")
    @ResponseBody
    public String totalTaskCount() {
        return String.valueOf(totalTaskCount);
    }

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

        if (themeFilterVal != null && !themeFilterVal.isEmpty()) {
            criteria.and("theme").regex(themeFilterVal, "i");
        }
        if (departmentFilterVal != null && !departmentFilterVal.isEmpty()) {
            criteria.and("dept").is(departmentFilterVal);
        }

        List<Criteria> combinedOrCriteria = new ArrayList<>();
        if (taskFilterVals != null && taskFilterVals.length > 0) {
            for (String task : taskFilterVals) {
                Criteria taskCriteria = Criteria.where("task").is(task);
                combinedOrCriteria.add(taskCriteria);
            }
        }

        if (includeCommentsOnly) {
            List<Criteria> nonEmptyCriteria = createNonEmptyCriteria();
            if (!combinedOrCriteria.isEmpty()) {
                List<Criteria> commentCriteriaWithTasks = new ArrayList<>();
                for (Criteria taskCriteria : combinedOrCriteria) {
                    commentCriteriaWithTasks.add(new Criteria().andOperator(taskCriteria, new Criteria().orOperator(nonEmptyCriteria.toArray(new Criteria[0]))));
                }
                criteria.orOperator(commentCriteriaWithTasks.toArray(new Criteria[0]));
            } else {
                criteria.orOperator(nonEmptyCriteria.toArray(new Criteria[0]));
            }
        } else if (!combinedOrCriteria.isEmpty()) {
            criteria.orOperator(combinedOrCriteria.toArray(new Criteria[0]));
        }

        List<Map> distinctTaskCounts = topTaskRepository.findDistinctTaskCountsWithFilters(criteria);
        totalDistinctTasks = distinctTaskCounts.size();
        DataTablesOutput<TopTaskSurvey> results = topTaskRepository.findAll(input, criteria);

        totalTaskCount = (int) results.getRecordsFiltered();
        return results;
    }


    private List<Criteria> createNonEmptyCriteria() {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("taskOther").exists(true).ne(""));
        criteriaList.add(Criteria.where("themeOther").exists(true).ne(""));
        criteriaList.add(Criteria.where("taskWhyNotComment").exists(true).ne(""));
        criteriaList.add(Criteria.where("taskImproveComment").exists(true).ne(""));
        return criteriaList;
    }

    @RequestMapping(value = "/topTaskSurvey/departments", produces = "application/json")
    @ResponseBody
    public List<Map<String, String>> departmentData(HttpServletRequest request) {
        String lang = (String) request.getSession().getAttribute("lang");
        String departmentsStr = "AAFC / AAC,ATSSC / SCDATA,CATSA / ACSTA,CFIA / ACIA,CIRNAC / RCAANC,NSERC / CRSNG,CBSA / ASFC,CCG / GCC,CGC / CCG,"
                + "CIHR / IRSC,CIPO / OPIC,CRA / ARC,CRTC / CRTC,CSA / ASC,CSEC / CSTC,CSPS / EFPC,DFO / MPO,DND / MDN,ECCC / ECCC,"
                + "ESDC / EDSC,FCAC / ACFC,FIN / FIN,GAC / AMC,HC / SC,INFC / INFC,IRCC / IRCC,ISC / SAC,ISED / ISDE,JUS / JUS,"
                + "LAC / BAC,NFB / ONF,NRC / CNRC,NRCan / RNCan,OSB / BSF,PBC / CLCC,PC / PC,PCH / PCH,PCO / BCP,PHAC / ASPC,"
                + "PS / SP,PSC / CFP,SSC / PSC,PSPC / SPAC,RCMP / GRC,StatCan / StatCan,TBS / SCT,TC / TC,VAC / ACC,WAGE / FEGC,WD / DEO";
        String[] departmentData = departmentsStr.split(",");

        return Arrays.stream(departmentData)
                .map(dept -> {
                    String[] parts = dept.split(" / ");
                    String value = dept; // EN / FR format
                    String display = lang != null && lang.equalsIgnoreCase("fr") ? parts[1] + " / " + parts[0] : dept; // FR / EN format for French, EN / FR for English

                    Map<String, String> departmentMap = new HashMap<>();
                    departmentMap.put("value", value);
                    departmentMap.put("display", display);
                    return departmentMap;
                })
                .collect(Collectors.toList());
    }


    @GetMapping(value = "/topTaskSurvey")
    public ModelAndView topTaskSurvey(HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView();
        String lang = (String) request.getSession().getAttribute("lang");
        Map<String, String> dateMap = problemDateService.getProblemDates();
        mav.setViewName("topTaskSurvey_" + lang);
        mav.addObject("earliestDate", "2021-01-14");
        mav.addObject("latestDate", dateMap.get("latestDate"));

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