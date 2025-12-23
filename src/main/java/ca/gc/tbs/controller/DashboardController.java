package ca.gc.tbs.controller;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.repository.ProblemRepository;
import ca.gc.tbs.service.ErrorKeywordService;
import ca.gc.tbs.service.ProblemDateService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DashboardController {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ErrorKeywordService errorKeywordService;

    @Autowired
    private ProblemDateService problemDateService;

    private static final Map<String, List<String>> institutionMappings = new HashMap<>();

    static {
        institutionMappings.put("AAFC", Arrays.asList("AAFC", "AAC", "AGRICULTURE AND AGRI-FOOD CANADA"));
        institutionMappings.put("CRA", Arrays.asList("CRA", "ARC", "CANADA REVENUE AGENCY"));
        institutionMappings.put("ESDC", Arrays.asList("ESDC", "EDSC", "EMPLOYMENT AND SOCIAL DEVELOPMENT CANADA"));
        institutionMappings.put("HC", Arrays.asList("HC", "SC", "HEALTH CANADA"));
        institutionMappings.put("IRCC", Arrays.asList("IRCC", "IMMIGRATION, REFUGEES AND CITIZENSHIP CANADA"));
    }

    @GetMapping(value = "/pageFeedbackDashboard")
    public ModelAndView pageFeedbackDashboard(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        String lang = (String) request.getSession().getAttribute("lang");
        Map<String, String> dateMap = problemDateService.getProblemDates();
        mav.addObject("earliestDate", dateMap.get("earliestDate"));
        mav.addObject("latestDate", dateMap.get("latestDate"));
        mav.addObject("lang", lang);
        mav.setViewName("pageFeedbackDashboard_" + lang);
        return mav;
    }

    @GetMapping(value = "/dashboard/dailyCommentCount")
    @ResponseBody
    public List<Map<String, Object>> getDailyCommentCount(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String comments,
            @RequestParam(required = false) Boolean error_keyword) {

        Specification<Problem> spec = buildFilterSpecification(startDate, endDate, theme, section, language, url, department, comments, error_keyword);
        List<Problem> problems = problemRepository.findAll(spec);

        Map<String, Long> dailyCounts = problems.stream()
                .filter(p -> p.getProblemDate() != null && p.getProblemDate().length() >= 10)
                .collect(Collectors.groupingBy(p -> p.getProblemDate().substring(0, 10), Collectors.counting()));

        return dailyCounts.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("date", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .sorted((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/dashboard/errorKeywordCount")
    @ResponseBody
    public Map<String, Object> getErrorKeywordCount(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String comments) {

        Specification<Problem> spec = buildFilterSpecification(startDate, endDate, theme, section, language, url, department, comments, true);
        long errorKeywordCount = problemRepository.count(spec);

        Specification<Problem> totalSpec = buildFilterSpecification(startDate, endDate, theme, section, language, url, department, comments, false);
        long totalComments = problemRepository.count(totalSpec);

        Map<String, Object> result = new HashMap<>();
        result.put("errorKeywordCount", errorKeywordCount);
        result.put("totalComments", totalComments);
        return result;
    }

    @GetMapping(value = "/dashboard/urlCommentCount")
    @ResponseBody
    public List<Map<String, Object>> getUrlCommentCount(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String comments,
            @RequestParam(required = false) Boolean error_keyword) {

        Specification<Problem> spec = buildFilterSpecification(startDate, endDate, theme, section, language, url, department, comments, error_keyword);
        List<Problem> problems = problemRepository.findAll(spec);

        Map<String, Long> urlCounts = problems.stream()
                .filter(p -> p.getUrl() != null)
                .collect(Collectors.groupingBy(Problem::getUrl, Collectors.counting()));

        return urlCounts.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("url", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                .limit(20)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/dashboardData", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public DataTablesOutput<Problem> dashboardData(@Valid DataTablesInput input, HttpServletRequest request) {
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String theme = request.getParameter("theme");
        String section = request.getParameter("section");
        String language = request.getParameter("language");
        String url = request.getParameter("url");
        String department = request.getParameter("department");
        String comments = request.getParameter("comments");
        Boolean error_keyword = "true".equals(request.getParameter("error_keyword"));

        Specification<Problem> spec = buildFilterSpecification(startDate, endDate, theme, section, language, url, department, comments, error_keyword);
        return problemRepository.findAll(input, spec);
    }

    private Specification<Problem> buildFilterSpecification(String startDate, String endDate, String theme,
            String section, String language, String url, String department, String comments, Boolean errorKeyword) {

        Specification<Problem> spec = (root, query, cb) -> cb.equal(root.get("processed"), "true");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.and(
                    cb.greaterThanOrEqualTo(root.get("problemDate"), startDate),
                    cb.lessThanOrEqualTo(root.get("problemDate"), endDate)));
        }

        if (theme != null && !theme.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("theme"), theme));
        }

        if (section != null && !section.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("section"), section));
        }

        if (language != null && !language.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("language"), language));
        }

        if (url != null && !url.isEmpty()) {
            final String urlLower = url.toLowerCase();
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("url")), "%" + urlLower + "%"));
        }

        if (department != null && !department.isEmpty()) {
            Set<String> matchingVariations = new HashSet<>();
            for (Map.Entry<String, List<String>> entry : institutionMappings.entrySet()) {
                if (entry.getValue().stream().anyMatch(v -> v.equalsIgnoreCase(department))) {
                    matchingVariations.addAll(entry.getValue());
                }
            }
            if (!matchingVariations.isEmpty()) {
                spec = spec.and((root, query, cb) -> root.get("institution").in(matchingVariations));
            }
        }

        if (comments != null && !comments.isEmpty()) {
            final String commentLower = comments.toLowerCase();
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("problemDetails")), "%" + commentLower + "%"));
        }

        if (errorKeyword != null && errorKeyword) {
            Set<String> keywords = new HashSet<>();
            keywords.addAll(errorKeywordService.getEnglishKeywords());
            keywords.addAll(errorKeywordService.getFrenchKeywords());
            keywords.addAll(errorKeywordService.getBilingualKeywords());

            if (!keywords.isEmpty()) {
                spec = spec.and((root, query, cb) -> {
                    List<Predicate> keywordPredicates = new ArrayList<>();
                    for (String keyword : keywords) {
                        keywordPredicates.add(cb.like(cb.lower(root.get("problemDetails")), "%" + keyword.toLowerCase() + "%"));
                    }
                    return cb.or(keywordPredicates.toArray(new Predicate[0]));
                });
            }
        }

        return spec;
    }
}
