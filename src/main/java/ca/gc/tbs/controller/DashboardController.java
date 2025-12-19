package ca.gc.tbs.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesOutput;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.repository.ProblemRepository;
import ca.gc.tbs.service.ErrorKeywordService;
import ca.gc.tbs.service.ProblemCacheService;
import ca.gc.tbs.service.ProblemDateService;
import ca.gc.tbs.service.UserService;

@Controller
public class DashboardController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private ProblemRepository problemRepository;
    private static final boolean ASC = true;
    private static final boolean DESC = false;
    @Autowired
    private ProblemDateService problemDateService;
    @Autowired
    private ProblemCacheService problemCacheService;
    private int totalComments = 0;
    private int totalPages = 0;

    private List<Problem> problems;

    @Autowired
    private UserService userService;
    @Autowired
    private ErrorKeywordService errorKeywordService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ca.gc.tbs.service.InstitutionService institutionService;




    @RequestMapping(value = "/pageFeedback/totalCommentsCount")
    @ResponseBody
    public String totalCommentsCount() {
        return String.valueOf(totalComments);
    }

    @RequestMapping(value = "/pageFeedback/totalPagesCount")
    @ResponseBody
    public String totalPagesCount() {
        return String.valueOf(totalPages);
    }

    @GetMapping(value = "/dashboard")
    public ModelAndView pageFeedback(HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView();
        String lang = (String) request.getSession().getAttribute("lang");
        mav.addObject("lang", lang);
        Map<String, String> dateMap = problemDateService.getProblemDates();
        if (dateMap != null) {
            mav.addObject("earliestDate", dateMap.get("earliestDate"));
            LocalDate latestDate =
                    LocalDate.parse(dateMap.get("latestDate"), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate previousDate = latestDate.minusDays(1);
            String modifiedLatestDate = previousDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            mav.addObject("latestDate", modifiedLatestDate);
        } else {
            mav.addObject("earliestDate", "N/A");
            mav.addObject("latestDate", "N/A");
        }

        mav.setViewName("pageFeedbackDashboard_" + lang);
        return mav;
    }

    @GetMapping(value = "/chartData")
    @ResponseBody
    public List<Map<String, Object>> commentsByDate(HttpServletRequest request) {
        // Extract request parameters
        boolean filterByErrorKeywords = "true".equals(request.getParameter("error_keyword"));
        String commentFilter = request.getParameter("comments");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String theme = request.getParameter("theme");
        String section = request.getParameter("section");
        String language = request.getParameter("language");
        String url = request.getParameter("url");
        String department = request.getParameter("department");

        // Build base filter criteria (date range, theme, section, language, url, department)
        Criteria baseCriteria = buildFilterCriteria(startDate, endDate, theme, section, language, url, department);
        
        // Add text search criteria if needed (error keywords or comment search)
        Criteria finalCriteria = buildTextSearchCriteria(baseCriteria, filterByErrorKeywords, commentFilter);
        
        // Determine data source: use database for text search, cache for simple filters
        boolean requiresDatabaseQuery = filterByErrorKeywords || isValidCommentFilter(commentFilter);
        
        if (requiresDatabaseQuery) {
            return aggregateCommentsFromDatabase(finalCriteria);
        }
        
        return aggregateCommentsFromCache();
    }

    /**
     * Checks if the comment filter is valid and should be applied.
     */
    private boolean isValidCommentFilter(String commentFilter) {
        return commentFilter != null && !commentFilter.trim().isEmpty() && !"null".equalsIgnoreCase(commentFilter.trim());
    }

    /**
     * Builds criteria for text search (error keywords and/or comment filter).
     * Returns combined criteria using AND operator if text search is needed.
     */
    private Criteria buildTextSearchCriteria(Criteria baseCriteria, boolean filterByErrorKeywords, String commentFilter) {
        List<Criteria> textSearchCriteria = new ArrayList<>();
        
        // Add error keyword criteria if requested
        if (filterByErrorKeywords) {
            String combinedKeywordRegex = errorKeywordService.getCombinedKeywordRegex();
            if (!combinedKeywordRegex.isEmpty()) {
                textSearchCriteria.add(Criteria.where("problemDetails").regex(combinedKeywordRegex, "i"));
            }
        }
        
        // Add comment filter criteria if provided
        if (isValidCommentFilter(commentFilter)) {
            String escapedComment = escapeSpecialRegexCharacters(commentFilter.trim());
            textSearchCriteria.add(Criteria.where("problemDetails").regex(escapedComment, "i"));
        }
        
        // Combine base criteria with text search criteria
        if (textSearchCriteria.isEmpty()) {
            return baseCriteria;
        }
        
        List<Criteria> allCriteria = new ArrayList<>();
        allCriteria.add(baseCriteria);
        allCriteria.addAll(textSearchCriteria);
        return new Criteria().andOperator(allCriteria.toArray(new Criteria[0]));
    }

    /**
     * Aggregates comments by date using MongoDB aggregation pipeline.
     * Used when text search (error keywords or comment filter) is required.
     */
    private List<Map<String, Object>> aggregateCommentsFromDatabase(Criteria criteria) {
        // Build aggregation pipeline: match -> group by date -> sort by date
        GroupOperation groupByDate = Aggregation.group("problemDate").count().as("comments");
        SortOperation sortByDate = Aggregation.sort(Sort.Direction.ASC, "_id");
        
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                groupByDate,
                sortByDate
        );
        
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "problem", Document.class);
        
        // Convert results to list of maps
        List<Map<String, Object>> dailyCommentsList = new ArrayList<>();
        for (Document doc : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", doc.getString("_id"));
            entry.put("comments", doc.getInteger("comments", 0));
            dailyCommentsList.add(entry);
        }
        
        return dailyCommentsList;
    }

    /**
     * Aggregates comments by date from cached problem list.
     * Used for simple filters without text search for better performance.
     */
    private List<Map<String, Object>> aggregateCommentsFromCache() {
        if (problems == null) {
            return new ArrayList<>();
        }
        
        // Group and sum URL entries by date using streams
        Map<String, Integer> commentCountsByDate = problems.stream()
                .filter(problem -> problem != null && problem.getProblemDate() != null)
                .collect(Collectors.groupingBy(
                        Problem::getProblemDate,
                        Collectors.summingInt(Problem::getUrlEntries)
                ));
        
        // Convert to list of maps and sort by date
        return commentCountsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", entry.getKey());
                    map.put("comments", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }



    @Scheduled(cron = "0 1 0 * * *")
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        LOGGER.info("DashboardController: Starting initial data fetch and cache population.");
        
        // Preload dashboard totals for fast initial page load
        try {
            LOGGER.info("DashboardController: Calculating initial totalComments and totalPages...");
            List<Problem> processedProblems = problemCacheService.getProcessedProblems();
            problemDateService.getProblemDates();
            
            // Group by URL and problemDate to get merged problems
            List<Problem> mergedProblems = new ArrayList<>(
                processedProblems.stream()
                    .collect(
                        Collectors.groupingBy(
                            p -> new AbstractMap.SimpleEntry<>(p.getUrl(), p.getProblemDate()),
                            Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Problem problem = new Problem();
                                    problem.setUrl(list.get(0).getUrl());
                                    problem.setProblemDate(list.get(0).getProblemDate());
                                    problem.setUrlEntries(list.size());
                                    problem.setInstitution(list.get(0).getInstitution());
                                    problem.setTitle(list.get(0).getTitle());
                                    problem.setLanguage(list.get(0).getLanguage());
                                    problem.setSection(list.get(0).getSection());
                                    problem.setTheme(list.get(0).getTheme());
                                    return problem;
                                })))
                    .values());
            
            // Filter out future dates
            LocalDate currentDate = LocalDate.now();
            mergedProblems = mergedProblems.stream()
                .filter(p -> {
                    LocalDate problemDate = LocalDate.parse(p.getProblemDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                    return !problemDate.isAfter(currentDate);
                })
                .collect(Collectors.toList());
            
            // Merge problems with same URL (across different dates)
            mergedProblems = mergeProblems(mergedProblems);
            
            // Calculate totals
            totalComments = mergedProblems.stream().mapToInt(Problem::getUrlEntries).sum();
            totalPages = mergedProblems.size();
            
            LOGGER.info("DashboardController: Preloaded totals - {} comments across {} pages", 
                totalComments, totalPages);
        } catch (Exception e) {
            LOGGER.error("DashboardController: Error calculating initial totals", e);
        }
        
        LOGGER.info("DashboardController: Initial data fetch and cache population complete.");
    }

    @GetMapping(value = "/dashboardData")
    @ResponseBody
    public DataTablesOutput<Problem> getDashboardData(
            @Valid DataTablesInput input, HttpServletRequest request) {
        long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        String pageLang = (String) request.getSession().getAttribute("lang");
        String department = request.getParameter("department");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String language = request.getParameter("language");
        String url = request.getParameter("url");
        String comments = request.getParameter("comments");
        String section = request.getParameter("section");
        String theme = request.getParameter("theme");
        Boolean error_keyword = "true".equals(request.getParameter("error_keyword"));

        //error keyword filtering
        if (error_keyword) {

            Criteria criteria = buildFilterCriteria(startDate, endDate, theme, section, language, url, department);

            List<Criteria> regexCriteria = new ArrayList<>();//added for combined regex

            // Build regex pattern from all keywords
            String combinedKeywordRegex = errorKeywordService.getCombinedKeywordRegex();


            if (!combinedKeywordRegex.isEmpty()) {
                regexCriteria.add(Criteria.where("problemDetails").regex(combinedKeywordRegex, "i"));
            }
            // Comment filter regex
            if (comments != null && !comments.trim().isEmpty() && !"null".equalsIgnoreCase(comments.trim())) {
                String safeComments = escapeSpecialRegexCharacters(comments.trim());
                regexCriteria.add(Criteria.where("problemDetails").regex(safeComments, "i"));
            }

            // Combine base criteria with all regex criteria with .andOperator()
            Criteria finalCriteria;
            if (!regexCriteria.isEmpty()) {
                List<Criteria> ands = new ArrayList<>();
                ands.add(criteria);
                ands.addAll(regexCriteria);
                finalCriteria = new Criteria().andOperator(ands.toArray(new Criteria[0]));
            } else {
                finalCriteria = criteria;
            }

            // Aggregation for error keywords
            MatchOperation match = Aggregation.match(finalCriteria);
            GroupOperation groupByUrl = Aggregation.group("url")
                    .first("url").as("url")
                    .first("problemDate").as("problemDate")
                    .first("institution").as("institution")
                    .first("title").as("title")
                    .first("language").as("language")
                    .first("section").as("section")
                    .first("theme").as("theme")
                    .count().as("urlEntries");
            SortOperation sortByEntriesDesc = Aggregation.sort(Sort.Direction.DESC, "urlEntries");

            Aggregation agg = Aggregation.newAggregation(
                    match,
                    groupByUrl,
                    sortByEntriesDesc,
                    Aggregation.skip((long) input.getStart()),
                    Aggregation.limit(input.getLength())
            );

            AggregationResults<Problem> results = mongoTemplate.aggregate(agg, "problem", Problem.class);
            List<Problem> groupedProblems = results.getMappedResults();

            // Total comments and pages
            totalPages = mongoTemplate.aggregate(
                    Aggregation.newAggregation(match, groupByUrl), "problem", Problem.class
            ).getMappedResults().size();
            totalComments =(int) mongoTemplate.count(Query.query(finalCriteria), "problem");


            // Create a DataTablesOutput instance
            DataTablesOutput<Problem> output = new DataTablesOutput<>();
            output.setData(groupedProblems);
            output.setDraw(input.getDraw());
            output.setRecordsTotal(groupedProblems.size());
            output.setRecordsFiltered(groupedProblems.size());

            output.setRecordsTotal(totalComments);
            output.setRecordsFiltered(totalComments);

            // Adjust institution names based on language (same as normal dashboard)
            setInstitution(output, pageLang);

            return output;

        //comments filtering
        } else if (comments != null && !comments.trim().isEmpty() && !"null".equalsIgnoreCase(comments.trim())) {

            Criteria criteria = buildFilterCriteria(startDate, endDate, theme, section, language, url, department);

            // Only use the comment as a regex filter
            String escapedComment = escapeSpecialRegexCharacters(comments.trim());
            LOGGER.info("Applying comment-only regex: '{}'", escapedComment);
            criteria.and("problemDetails").regex(escapedComment, "i");

            // Aggregation
            MatchOperation match = Aggregation.match(criteria);
            GroupOperation groupByUrl = Aggregation.group("url")
                    .first("url").as("url")
                    .first("problemDate").as("problemDate")
                    .first("institution").as("institution")
                    .first("title").as("title")
                    .first("language").as("language")
                    .first("section").as("section")
                    .first("theme").as("theme")
                    .count().as("urlEntries");
            SortOperation sortByEntriesDesc = Aggregation.sort(Sort.Direction.DESC, "urlEntries");

            // Get all groups for totals
            List<Problem> allGroupedProblems = mongoTemplate.aggregate(
                    Aggregation.newAggregation(match, groupByUrl),
                    "problem",
                    Problem.class
            ).getMappedResults();

            // Calculate totals
            totalPages = allGroupedProblems.size();
            totalComments = allGroupedProblems.stream().mapToInt(Problem::getUrlEntries).sum();

            // Paginate for current page
            Aggregation agg = Aggregation.newAggregation(
                    match,
                    groupByUrl,
                    sortByEntriesDesc,
                    Aggregation.skip((long) input.getStart()),
                    Aggregation.limit(input.getLength())
            );

            AggregationResults<Problem> results = mongoTemplate.aggregate(agg, "problem", Problem.class);
            List<Problem> groupedProblems = results.getMappedResults();

            // Set up DataTablesOutput
            DataTablesOutput<Problem> output = new DataTablesOutput<>();
            output.setData(groupedProblems);
            output.setDraw(input.getDraw());
            output.setRecordsTotal(totalComments);
            output.setRecordsFiltered(totalComments);


            setInstitution(output, pageLang);

            return output;
        }

            LOGGER.debug("Retrieving dashboard data");
            List<Problem> processedProblems = problemCacheService.getProcessedProblems();
            LOGGER.debug("Retrieved {} problems for dashboard data", processedProblems.size());

            problems =
                    new ArrayList<>(
                            processedProblems.stream()
                                    .collect(
                                            Collectors.groupingBy(
                                                    p -> new AbstractMap.SimpleEntry<>(p.getUrl(), p.getProblemDate()),
                                                    Collectors.collectingAndThen(
                                                            Collectors.toList(),
                                                            list -> {
                                                                Problem problem = new Problem();
                                                                problem.setUrl(list.get(0).getUrl());
                                                                problem.setProblemDate(list.get(0).getProblemDate());
                                                                problem.setUrlEntries(list.size());
                                                                problem.setInstitution(list.get(0).getInstitution());
                                                                problem.setTitle(list.get(0).getTitle());
                                                                problem.setLanguage(list.get(0).getLanguage());
                                                                problem.setSection(list.get(0).getSection());
                                                                problem.setTheme(list.get(0).getTheme());
                                                                return problem;
                                                            })))
                                    .values());

            LocalDate currentDate = LocalDate.now();
            problems =
                    problems.stream()
                            .filter(
                                    p -> {
                                        LocalDate problemDate =
                                                LocalDate.parse(p.getProblemDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                                        return !problemDate.isAfter(currentDate); //was isBefore - changed to !isAfter for more entries including current date
                                    })
                            .collect(Collectors.toList());
            // Apply filters
            problems =
                    applyFilters(problems, department, startDate, endDate, language, url, section, theme);

        // Sort problems by URL entries in descending order
            problems.sort(Comparator.comparingInt(Problem::getUrlEntries).reversed());

            // Merge problems with the same URL
            List<Problem> mergedProblems = mergeProblems(problems);

            mergedProblems.sort(Comparator.comparingInt(Problem::getUrlEntries).reversed());
            // Calculate total comments and pages
            totalComments = mergedProblems.stream().mapToInt(Problem::getUrlEntries).sum();
            totalPages = mergedProblems.size();

        // Apply pagination
            List<Problem> paginatedProblems =
                    applyPagination(mergedProblems, input.getStart(), input.getLength());

            // Create a DataTablesOutput instance and set the paginated data
            DataTablesOutput<Problem> output = new DataTablesOutput<>();
            output.setData(paginatedProblems);
            output.setDraw(input.getDraw());
            output.setRecordsTotal(mergedProblems.size());
            output.setRecordsFiltered(mergedProblems.size());

            output.setRecordsTotal(totalComments);
            output.setRecordsFiltered(totalComments);

            // Adjust institution names based on language
            setInstitution(output, pageLang);
            long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long actualMemUsed = afterUsedMem - beforeUsedMem;
            LOGGER.info("Memory used by yourMethod(): {} bytes", actualMemUsed);

        return output;
    }
    //Helper method for criteria building with filters
    private Criteria buildFilterCriteria(String startDate, String endDate, String theme,
                                         String section, String language, String url,
                                         String department) {
        Criteria criteria = Criteria.where("processed").is("true");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            criteria.and("problemDate").gte(start.format(formatter)).lte(end.format(formatter));
        }
        if (theme != null && !theme.isEmpty()) {
            criteria.and("theme").is(theme);
        }
        if (section != null && !section.isEmpty()) {
            criteria.and("section").in(institutionService.getSectionVariations(section));
        }
        if (language != null && !language.isEmpty()) {
            criteria.and("language").is(language);
        }
        if (url != null && !url.isEmpty()) {
            criteria.and("url").regex(url, "i");
        }
        if (department != null && !department.isEmpty()) {
            Set<String> matchingVariations = institutionService.getMatchingVariations(department);
            if (!matchingVariations.isEmpty()) {
                criteria.and("institution").in(matchingVariations);
            }
        }
        return criteria;
    }

    private String escapeSpecialRegexCharacters(String input) {
        // Escape all regex metacharacters
        return input.replaceAll("([\\\\.^$|()\\[\\]{}*+?])", "\\\\$1");
    }


    private Problem createProblemFromResult(Map result) {
        Problem problem = new Problem();
        problem.setUrl((String) result.get("url"));
        problem.setProblemDate((String) result.get("day"));
        problem.setUrlEntries((Integer) result.get("count"));
        problem.setTitle((String) result.get("title"));
        problem.setLanguage((String) result.get("language"));
        problem.setInstitution((String) result.get("institution"));
        problem.setTheme((String) result.get("theme"));
        problem.setSection((String) result.get("section"));
        return problem;
    }

    private List<Problem> applyFilters(
            List<Problem> problems,
            String department,
            String startDate,
            String endDate,
            String language,
            String url,
            String section,
            String theme) {
        problems = applyDepartmentFilter(problems, department);
        problems = applyDateRangeFilter(problems, startDate, endDate);
        problems = applyLanguageFilter(problems, language);
        problems = applyUrlFilter(problems, url);
        problems = applySectionFilter(problems, section);
        problems = applyThemeFilter(problems, theme);

        return problems;
    }

    // Extract filter methods here...

    private List<Problem> mergeProblems(List<Problem> problems) {
        Map<String, Problem> urlToProblemMap = new LinkedHashMap<>();

        for (Problem problem : problems) {
            urlToProblemMap.merge(
                    problem.getUrl(),
                    problem,
                    (existingProblem, newProblem) -> {
                        Problem updatedProblem = new Problem(existingProblem);
                        updatedProblem.setUrlEntries(
                                existingProblem.getUrlEntries() + newProblem.getUrlEntries());
                        return updatedProblem;
                    });
        }

        return new ArrayList<>(urlToProblemMap.values());
    }

    private List<Problem> applyLanguageFilter(List<Problem> problems, String language) {
        if (language != null && !language.isEmpty()) {
            return problems.stream()
                    .filter(problem -> problem.getLanguage().equals(language))
                    .collect(Collectors.toList());
        }
        return problems;
    }

    // DEPT
    private List<Problem> applyDateRangeFilter(
            List<Problem> problems, String startDate, String endDate) {
        if (startDate != null && endDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            return problems.stream()
                    .filter(
                            problem -> {
                                LocalDate problemDate = LocalDate.parse(problem.getProblemDate(), formatter);
                                return !problemDate.isBefore(start) && !problemDate.isAfter(end);
                            })
                    .collect(Collectors.toList());
        }
        return problems;
    }



    private List<Problem> applyDepartmentFilter(
            List<Problem> problems, String department) {
        if (department != null && !department.isEmpty()) {
            Set<String> matchingVariations = institutionService.getMatchingVariations(department);
            if (!matchingVariations.isEmpty()) {
                return problems.stream()
                        .filter(problem -> matchingVariations.contains(problem.getInstitution()))
                        .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }
        return problems;
    }

    private List<Problem> applySectionFilter(List<Problem> problems, String section) {
        if (section != null && !section.isEmpty()) {
            return problems.stream()
                    .filter(problem -> institutionService.getSectionVariations(section).contains(problem.getSection()))
                    .collect(Collectors.toList());
        }
        return problems;
    }

    // theme
    private List<Problem> applyThemeFilter(List<Problem> problems, String theme) {
        if (theme != null && !theme.isEmpty()) {
            return problems.stream()
                    .filter(problem -> problem.getTheme().equals(theme))
                    .collect(Collectors.toList());
        }
        return problems;
    }

    private List<Problem> applyUrlFilter(List<Problem> problems, String url) {
        if (url != null && !url.isEmpty()) {
            String filterUrl = url.toLowerCase();
            return problems.stream()
                    .filter(problem -> problem.getUrl().toLowerCase().contains(filterUrl))
                    .collect(Collectors.toList());
        }
        return problems;
    }

    private List<Problem> applyPagination(List<Problem> mergedProblems, int start, int length) {
        return mergedProblems.stream().skip(start).limit(length).collect(Collectors.toList());
    }

    private void setInstitution(DataTablesOutput<Problem> problems, String lang) {
        for (Problem problem : problems.getData()) {
            String currentInstitution = problem.getInstitution();
            if (currentInstitution == null || currentInstitution.isEmpty()) {
                String inferredInstitution = institutionService.getInstitutionFromUrl(problem.getUrl());
                if (inferredInstitution != null) {
                    currentInstitution = inferredInstitution;
                }
            }
            String translatedInstitution = institutionService.getTranslatedInstitution(currentInstitution, lang);
            problem.setInstitution(translatedInstitution);
        }
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
