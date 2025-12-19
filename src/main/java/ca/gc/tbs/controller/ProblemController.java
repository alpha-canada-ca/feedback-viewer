package ca.gc.tbs.controller;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.domain.User;
import ca.gc.tbs.repository.ProblemRepository;
import ca.gc.tbs.security.JWTUtil;
import ca.gc.tbs.service.ErrorKeywordService;
import ca.gc.tbs.service.ProblemDateService;
import ca.gc.tbs.service.UserService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Controller
public class ProblemController {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemController.class);

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ProblemDateService problemDateService;

    @Autowired
    private ErrorKeywordService errorKeywordService;

    @Autowired
    private UserService userService;

    @Autowired
    private ca.gc.tbs.service.InstitutionService institutionService;



    @GetMapping("/pageTitles")
    @ResponseBody
    public List<String> getPageTitles(
            @RequestParam(name = "search", required = false) String search) {
        if (search != null && !search.isEmpty()) {
            // Use the new repository method to filter page titles based on the search term
            return problemRepository.findPageTitlesBySearch(search);
        } else {
            // Return all page titles if no search term is provided
            return problemRepository.findDistinctPageNames();
        }
    }

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JWTUtil jwtUtil;

    @GetMapping("/api/problems")
    public ResponseEntity<?> getProblemsJson(
            @RequestParam Map<String, String> requestParams,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String processedStartDate,
            @RequestParam(required = false) String processedEndDate,
            @RequestParam(required = false) String institution,
            @RequestParam(required = false) String url,
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        String token = null;
        String userName = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            userName = jwtUtil.extractUsername(token);
        }

        if (userName != null) {
            User user = userService.findUserByEmail(userName);
            if (!userService.isAdmin(user) && !userService.isAPI(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only API users & Admins can access this endpoint.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization header is missing or invalid.");
        }

        Set<String> validParams =
                new HashSet<>(
                        Arrays.asList(
                                "startDate",
                                "endDate",
                                "processedStartDate",
                                "processedEndDate",
                                "institution",
                                "url",
                                "authorizationHeader"));

        for (String param : requestParams.keySet()) {
            if (!validParams.contains(param)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid parameter: " + param);
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        Criteria criteria = new Criteria("processed").is("true");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Ensure only one type of date filter is used
        if ((startDate != null || endDate != null)
                && (processedStartDate != null || processedEndDate != null)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(
                    "error", "You can only filter by normal date range or processed date range, not both.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Validate and apply normal date range filter
        if (startDate != null || endDate != null) {
            if (startDate == null || endDate == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Both startDate and endDate are required.");
                return ResponseEntity.badRequest().body(errorResponse);
            } else {
                try {
                    LocalDate start = LocalDate.parse(startDate, dateFormat);
                    LocalDate end = LocalDate.parse(endDate, dateFormat);
                    if (end.isBefore(start)) {
                        Map<String, String> errorResponse = new HashMap<>();
                        errorResponse.put("error", "endDate must be greater than or equal to startDate.");
                        return ResponseEntity.badRequest().body(errorResponse);
                    }
                    criteria.and("problemDate").gte(startDate).lte(endDate);
                } catch (DateTimeParseException e) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid date format. Please use yyyy-MM-dd.");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
        }

        // Validate and apply processed date range filter
        if (processedStartDate != null || processedEndDate != null) {
            if (processedStartDate == null || processedEndDate == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Both processedStartDate and processedEndDate are required.");
                return ResponseEntity.badRequest().body(errorResponse);
            } else {
                try {
                    LocalDate processedStart = LocalDate.parse(processedStartDate, dateFormat);
                    LocalDate processedEnd = LocalDate.parse(processedEndDate, dateFormat);
                    if (processedEnd.isBefore(processedStart)) {
                        Map<String, String> errorResponse = new HashMap<>();
                        errorResponse.put(
                                "error", "processedEndDate must be greater than or equal to processedStartDate.");
                        return ResponseEntity.badRequest().body(errorResponse);
                    }
                    criteria.and("processedDate").gte(processedStartDate).lte(processedEndDate);
                } catch (DateTimeParseException e) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid date format. Please use yyyy-MM-dd.");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
        }

        // Department filtering
        try {
            if (institution != null && !institution.isEmpty()) {
                criteria = applyDepartmentFilter(criteria, institution);
            }
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // URL filtering
        if (url != null && !url.isEmpty()) {
            criteria.and("url").regex(url, "i");
        }

        Query query = new Query(criteria);
        query
                .fields()
                .exclude("_id")
                .exclude("section")
                .exclude("oppositeLang")
                .exclude("processed")
                .exclude("contact")
                .exclude("urlEntries")
                .exclude("resolutionDate")
                .exclude("resolution")
                .exclude("topic")
                .exclude("title")
                .exclude("problem")
                .exclude("dataOrigin")
                .exclude("airTableSync")
                .exclude("tags")
                .exclude("personalInfoProcessed")
                .exclude("autoTagProcessed")
                .exclude("_class");

        List<Document> documents = mongoTemplate.find(query, Document.class, "problem");
        return ResponseEntity.ok(documents);
    }

    private Criteria applyDepartmentFilter(Criteria criteria, String department) {
        Set<String> matchingVariations = institutionService.getMatchingVariations(department);

        if (matchingVariations.isEmpty()) {
            throw new IllegalArgumentException("Couldn't find department name: " + department);
        }

        criteria.and("institution").in(matchingVariations);
        return criteria;
    }

    @GetMapping("/exportExcel")
    public void exportExcel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"feedback_export.xlsx\"");

        String[] titles = request.getParameterValues("titles[]");
        String language = request.getParameter("language");
        String department = request.getParameter("department");
        String comments = request.getParameter("comments");
        String theme = request.getParameter("theme");
        String section = request.getParameter("section");
        String url = request.getParameter("url");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        Boolean error_keyword = "true".equals(request.getParameter("error_keyword"));

        Criteria criteria = Criteria.where("processed").is("true");

        // Apply filters (similar to the existing method)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startDate != null && endDate != null) {
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
        if (titles != null && titles.length > 0) {
            List<Criteria> titleCriterias = new ArrayList<>();
            for (String title : titles) {
                titleCriterias.add(Criteria.where("title").is(title));
            }
            criteria = new Criteria().andOperator(
                    criteria,
                    new Criteria().orOperator(titleCriterias.toArray(new Criteria[0]))
            );
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
        if (comments != null && !comments.isEmpty()) {
            String safeComments = escapeSpecialRegexCharacters(comments);
            criteria.and("problemDetails").regex(safeComments, "i");
        }

        // Apply error keyword filter
        if (error_keyword) {
            // Build regex pattern from all keywords
            String combinedKeywordRegex = errorKeywordService.getCombinedKeywordRegex();

            if (!combinedKeywordRegex.isEmpty()) {
                LOG.debug("Checking error keywords for Excel export");
                criteria.and("problemDetails").regex(combinedKeywordRegex, "i");
            }
        }

        Query query = new Query(criteria);
        query
                .fields()
                .include("problemDate")
                .include("timeStamp")
                .include("problemDetails")
                .include("language")
                .include("title")
                .include("url")
                .include("institution")
                .include("section")
                .include("theme")
                .include("deviceType")
                .include("browser");

        // Use SXSSFWorkbook for better performance with large data
        try (SXSSFWorkbook workbook =
                     new SXSSFWorkbook(100); // The argument (100) flushes rows after 100 are written
             ServletOutputStream outputStream = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("Feedback Data");

            // Create header row
            String[] columns = {
                    "Problem Date",
                    "Time Stamp (UTC)",
                    "Problem Details",
                    "Language",
                    "Title",
                    "URL",
                    "Institution",
                    "Section",
                    "Theme",
                    "Device Type",
                    "Browser"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            // Stream and write data in batches
            final int[] rowNum = {1};
            mongoTemplate.stream(query, Problem.class)
                    .forEachRemaining(
                            problem -> {
                                Row row = sheet.createRow(rowNum[0]++);
                                row.createCell(0).setCellValue(problem.getProblemDate());
                                row.createCell(1).setCellValue(problem.getTimeStamp());
                                row.createCell(2).setCellValue(problem.getProblemDetails());
                                row.createCell(3).setCellValue(problem.getLanguage());
                                row.createCell(4).setCellValue(problem.getTitle());
                                row.createCell(5).setCellValue(problem.getUrl());
                                row.createCell(6).setCellValue(problem.getInstitution());
                                row.createCell(7).setCellValue(problem.getSection());
                                row.createCell(8).setCellValue(problem.getTheme());
                                row.createCell(9).setCellValue(problem.getDeviceType());
                                row.createCell(10).setCellValue(problem.getBrowser());

                                if (rowNum[0] % 100 == 0) {
                                    try {
                                        ((SXSSFSheet) sheet).flushRows(100); // Flush rows every 100 rows
                                    } catch (IOException e) {
                                        LOG.error("Error flushing rows", e);
                                    }
                                }
                            });

            // Write the workbook to the output stream
            workbook.write(outputStream);
        } catch (Exception e) {
            LOG.error("Error exporting Excel", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error exporting data: " + e.getMessage());
        }
    }

    @GetMapping("/exportCSV")
    public void exportCSV(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"feedback_export.csv\"");

        String[] titles = request.getParameterValues("titles[]");
        String language = request.getParameter("language");
        String department = request.getParameter("department");
        String comments = request.getParameter("comments");
        String theme = request.getParameter("theme");
        String section = request.getParameter("section");
        String url = request.getParameter("url");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        Boolean error_keyword = "true".equals(request.getParameter("error_keyword"));

        Criteria criteria = Criteria.where("processed").is("true");

        // Apply filters (similar to the list method)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startDate != null && endDate != null) {
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
            criteria
                    .and("language")
                    .regex(Pattern.compile(Pattern.quote(language), Pattern.CASE_INSENSITIVE));
        }
        if (titles != null && titles.length > 0) {
            List<Criteria> titleCriterias = new ArrayList<>();
            for (String title : titles) {
                titleCriterias.add(Criteria.where("title").is(title));
            }
            criteria = new Criteria().andOperator(
                    criteria,
                    new Criteria().orOperator(titleCriterias.toArray(new Criteria[0]))
            );
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
        if (comments != null && !comments.isEmpty()) {
            String safeComments = escapeSpecialRegexCharacters(comments);
            criteria.and("problemDetails").regex(safeComments, "i");
        }

        // Apply error keyword filter
        if (error_keyword) {
            // Build regex pattern from all keywords
            String combinedKeywordRegex = errorKeywordService.getCombinedKeywordRegex();

            if (!combinedKeywordRegex.isEmpty()) {
                LOG.debug("Checking error keywords for CSV export");
                criteria.and("problemDetails").regex(combinedKeywordRegex, "i");
            }
        }

        Query query = new Query(criteria);
        query
                .fields()
                .include("problemDate")
                .include("timeStamp")
                .include("problemDetails")
                .include("language")
                .include("title")
                .include("url")
                .include("institution")
                .include("section")
                .include("theme")
                .include("deviceType")
                .include("browser");

        // Stream results directly to the response
        Writer writer = response.getWriter();
        try {
            // Write CSV header
            writer.write(
                    "Problem Date,Time Stamp (UTC),Problem"
                            + " Details,Language,Title,URL,Institution,Section,Theme,Device Type,Browser\n");

            // Stream and write data
            mongoTemplate.stream(query, Problem.class)
                    .forEachRemaining(
                            new java.util.function.Consumer<Problem>() {
                                @Override
                                public void accept(Problem problem) {
                                    try {
                                        writer.write(
                                                String.format(
                                                        "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                                                        escapeCSV(problem.getProblemDate()),
                                                        escapeCSV(problem.getTimeStamp()),
                                                        escapeCSV(problem.getProblemDetails()),
                                                        escapeCSV(problem.getLanguage()),
                                                        escapeCSV(problem.getTitle()),
                                                        escapeCSV(problem.getUrl()),
                                                        escapeCSV(problem.getInstitution()),
                                                        escapeCSV(problem.getSection()),
                                                        escapeCSV(problem.getTheme()),
                                                        escapeCSV(problem.getDeviceType()),
                                                        escapeCSV(problem.getBrowser())));
                                    } catch (IOException e) {
                                        LOG.error("Error writing CSV data", e);
                                    }
                                }
                            });
        } finally {
            writer.close();
        }
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    @GetMapping(value = "/pageFeedback")
    public ModelAndView pageFeedback(HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView();
        String lang = (String) request.getSession().getAttribute("lang");

        // Fetch the aggregation results
        Map<String, String> dateMap = problemDateService.getProblemDates();

        if (dateMap != null) {
            mav.addObject("earliestDate", dateMap.get("earliestDate"));
            mav.addObject("latestDate", dateMap.get("latestDate"));
        } else {
            // Handle the case where no dates are returned
            mav.addObject("earliestDate", "N/A");
            mav.addObject("latestDate", "N/A");
        }
        mav.addObject("lang", lang);

        mav.setViewName("pageFeedback_" + lang);
        return mav;
    }

    private boolean containsErrorKeywords(Problem problem) {
        if (problem == null || problem.getProblemDetails() == null) {
            return false;
        }
        return errorKeywordService.containsErrorKeywords(
                problem.getProblemDetails(), problem.getLanguage());
    }

    @GetMapping(value = "/feedbackData")
    @ResponseBody
    public DataTablesOutput<Problem> list(@Valid DataTablesInput input, HttpServletRequest request) {
        String pageLang = (String) request.getSession().getAttribute("lang");
        String language = request.getParameter("language"); // Existing language parameter handling
        String department = request.getParameter("department"); // Retrieve the department parameter
        String comments = request.getParameter("comments"); // Retrieve the comments filter parameter
        String theme = request.getParameter("theme"); // Retrieve the theme filter parameter
        String section = request.getParameter("section"); // Retrieve the section filter parameter
        String url = request.getParameter("url"); // Retrieve the url filter parameter
        Boolean error_keyword = "true".equals(request.getParameter("error_keyword"));
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String[] titles = request.getParameterValues("titles[]");

        Criteria criteria = Criteria.where("processed").is("true");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startDate != null && endDate != null) {
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
        // Language filtering (existing logic)
        if (language != null && !language.isEmpty()) {
            criteria.and("language").is(language);
        }

        if (titles != null && titles.length > 0) {
            // Create a list to hold the title criteria
            List<Criteria> titleCriterias = new ArrayList<>();
            // Iterate over the titles and add each one as a criterion
            for (String title : titles) {
                titleCriterias.add(Criteria.where("title").is(title));
            }
            // Combine all title criteria using AND operation
            criteria = new Criteria().andOperator(
                    criteria,
                    new Criteria().orOperator(titleCriterias.toArray(new Criteria[0]))
            );
            System.out.println("Titles received: " + Arrays.toString(titles));
        }
        // URL filtering
        if (url != null && !url.isEmpty()) {
            criteria.and("url").regex(url, "i"); // 'i' for case-insensitive matching
        }
        // Department filtering based on institutionMappings
        if (department != null && !department.isEmpty()) {
            Set<String> matchingVariations = institutionService.getMatchingVariations(department);
            if (!matchingVariations.isEmpty()) {
                criteria.and("institution").in(matchingVariations);
            } else {
                DataTablesOutput<Problem> output = new DataTablesOutput<>();
                output.setDraw(input.getDraw());
                output.setRecordsTotal(0);
                output.setRecordsFiltered(0);
                output.setData(Collections.emptyList());
                return output;
            }
        }
        // Comments filtering
        if (comments != null && !comments.isEmpty()) {
            String safeComments = escapeSpecialRegexCharacters(comments);
            criteria.and("problemDetails").regex(safeComments, "i"); // 'i' for case-insensitive matching
        }
        DataTablesOutput<Problem> results;

        if (error_keyword) {
            // Build regex pattern from all keywords
            String combinedKeywordRegex = errorKeywordService.getCombinedKeywordRegex();

            if (!combinedKeywordRegex.isEmpty()) {
                LOG.debug("Checking error keywords");
                criteria.and("problemDetails").regex(combinedKeywordRegex, "i");
                results = problemRepository.findAll(input, criteria);
            } else {
                results = problemRepository.findAll(input, criteria);
            }
        } else {
            results = problemRepository.findAll(input, criteria);
        }
        // Update institution names in the results based on the language
        setInstitution(results, pageLang);
        // Return the updated results
        return results;
    }

    /**
     * Escapes special regex characters in the input string.
     *
     * @param input The string to escape.
     * @return A string with special regex characters escaped.
     */
    private String escapeSpecialRegexCharacters(String input) {
        // Escape all regex metacharacters
        return input.replaceAll("([\\\\.^$|()\\[\\]{}*+?])", "\\\\$1");
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
