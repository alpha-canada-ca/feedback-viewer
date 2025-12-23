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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Predicate;
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

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JWTUtil jwtUtil;

    private static final Map<String, List<String>> institutionMappings = new HashMap<>();
    private static final Map<String, List<String>> sectionMappings = new HashMap<>();

    static {
        // Initialize section mappings
        sectionMappings.put("disability", Arrays.asList("disability", "disability benefits"));

        // Initialize institution mappings
        institutionMappings.put(
                "AAFC",
                Arrays.asList(
                        "AAFC",
                        "AAC",
                        "AGRICULTURE AND AGRI-FOOD CANADA",
                        "AGRICULTURE ET AGROALIMENTAIRE CANADA",
                        "AAFC / AAC"));
        institutionMappings.put(
                "ACOA",
                Arrays.asList(
                        "ACOA",
                        "APECA",
                        "ATLANTIC CANADA OPPORTUNITIES AGENCY",
                        "AGENCE DE PROMOTION ÉCONOMIQUE DU CANADA ATLANTIQUE",
                        "ACOA / APECA"));
        institutionMappings.put(
                "CBSA",
                Arrays.asList(
                        "CBSA",
                        "ASFC",
                        "CANADA BORDER SERVICES AGENCY",
                        "AGENCE DES SERVICES FRONTALIERS DU CANADA",
                        "CBSA / ASFC"));
        institutionMappings.put(
                "CRA",
                Arrays.asList(
                        "CRA", "ARC", "CANADA REVENUE AGENCY", "AGENCE DU REVENU DU CANADA", "CRA / ARC"));
        institutionMappings.put(
                "DFO",
                Arrays.asList(
                        "DFO", "MPO", "FISHERIES AND OCEANS CANADA", "PÊCHES ET OCÉANS CANADA", "DFO / MPO", "GOVERNMENT OF CANADA, FISHERIES AND OCEANS CANADA, COMMUNICATIONS BRANCH"));
        institutionMappings.put(
                "ECCC",
                Arrays.asList(
                        "ECCC",
                        "ECCC",
                        "ENVIRONMENT AND CLIMATE CHANGE CANADA",
                        "ENVIRONNEMENT ET CHANGEMENT CLIMATIQUE CANADA",
                        "ECCC / ECCC"));
        institutionMappings.put(
                "ESDC",
                Arrays.asList(
                        "ESDC",
                        "EDSC",
                        "EMPLOYMENT AND SOCIAL DEVELOPMENT CANADA",
                        "EMPLOI ET DÉVELOPPEMENT SOCIAL CANADA",
                        "ESDC / EDSC"));
        institutionMappings.put(
                "GAC",
                Arrays.asList(
                        "GAC", "AMC", "GLOBAL AFFAIRS CANADA", "AFFAIRES MONDIALES CANADA", "GAC / AMC"));
        institutionMappings.put(
                "HC", Arrays.asList("HC", "SC", "HEALTH CANADA", "SANTÉ CANADA", "HC / SC"));
        institutionMappings.put(
                "IRCC",
                Arrays.asList(
                        "IRCC",
                        "IRCC",
                        "IMMIGRATION, REFUGEES AND CITIZENSHIP CANADA",
                        "IMMIGRATION, RÉFUGIÉS ET CITOYENNETÉ CANADA",
                        "IRCC / IRCC"));
        institutionMappings.put(
                "ISC",
                Arrays.asList(
                        "ISC",
                        "SAC",
                        "INDIGENOUS SERVICES CANADA",
                        "SERVICES AUX AUTOCHTONES CANADA",
                        "ISC / SAC"));
        institutionMappings.put(
                "ISED",
                Arrays.asList(
                        "ISED",
                        "ISDE",
                        "INNOVATION, SCIENCE AND ECONOMIC DEVELOPMENT CANADA",
                        "INNOVATION, SCIENCES ET DÉVELOPPEMENT ÉCONOMIQUE CANADA",
                        "ISED / ISDE"));
        institutionMappings.put(
                "NRCAN",
                Arrays.asList(
                        "NRCAN",
                        "RNCAN",
                        "NATURAL RESOURCES CANADA",
                        "RESSOURCES NATURELLES CANADA",
                        "NRCAN / RNCAN"));
        institutionMappings.put(
                "PHAC",
                Arrays.asList(
                        "PHAC",
                        "ASPC",
                        "PUBLIC HEALTH AGENCY OF CANADA",
                        "AGENCE DE LA SANTÉ PUBLIQUE DU CANADA",
                        "PHAC / ASPC"));
        institutionMappings.put(
                "PSPC",
                Arrays.asList(
                        "PSPC",
                        "SPAC",
                        "PUBLIC SERVICES AND PROCUREMENT CANADA",
                        "SERVICES PUBLICS ET APPROVISIONNEMENT CANADA",
                        "GOUVERNEMENT DU CANADA, SERVICES PUBLICS ET APPROVISIONNEMENT CANADA",
                        "GOVERNMENT OF CANADA, PUBLIC SERVICES AND PROCUREMENT CANADA",
                        "PSPC / SPAC"));
        institutionMappings.put(
                "RCMP",
                Arrays.asList(
                        "RCMP",
                        "GRC",
                        "ROYAL CANADIAN MOUNTED POLICE",
                        "GENDARMERIE ROYALE DU CANADA",
                        "RCMP / GRC"));
        institutionMappings.put(
                "SC", Arrays.asList("SC", "SC", "SERVICE CANADA", "SERVICE CANADA", "SC / SC"));
        institutionMappings.put(
                "STATCAN",
                Arrays.asList(
                        "STATCAN", "STATCAN", "STATISTICS CANADA", "STATISTIQUE CANADA", "STATCAN / STATCAN"));
        institutionMappings.put(
                "TBS",
                Arrays.asList(
                        "TBS",
                        "SCT",
                        "TREASURY BOARD OF CANADA SECRETARIAT",
                        "SECRÉTARIAT DU CONSEIL DU TRÉSOR DU CANADA",
                        "TBS / SCT"));
        institutionMappings.put(
                "TC", Arrays.asList("TC", "TC", "TRANSPORT CANADA", "TRANSPORTS CANADA", "TC / TC"));
        institutionMappings.put(
                "VAC",
                Arrays.asList(
                        "VAC", "ACC", "VETERANS AFFAIRS CANADA", "ANCIENS COMBATTANTS CANADA", "VAC / ACC"));
    }

    @GetMapping("/pageTitles")
    @ResponseBody
    public List<String> getPageTitles(
            @RequestParam(name = "search", required = false) String search) {
        if (search != null && !search.isEmpty()) {
            return problemRepository.findPageTitlesBySearch(search);
        } else {
            return problemRepository.findDistinctPageNames();
        }
    }

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

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Ensure only one type of date filter is used
        if ((startDate != null || endDate != null)
                && (processedStartDate != null || processedEndDate != null)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(
                    "error", "You can only filter by normal date range or processed date range, not both.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Build JPA Specification
        Specification<Problem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("processed"), "true"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

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
                    final String finalStartDate = startDate;
                    final String finalEndDate = endDate;
                    spec = spec.and((root, query, cb) ->
                            cb.and(
                                    cb.greaterThanOrEqualTo(root.get("problemDate"), finalStartDate),
                                    cb.lessThanOrEqualTo(root.get("problemDate"), finalEndDate)
                            ));
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
                    final String finalProcessedStart = processedStartDate;
                    final String finalProcessedEnd = processedEndDate;
                    spec = spec.and((root, query, cb) ->
                            cb.and(
                                    cb.greaterThanOrEqualTo(root.get("processedDate"), finalProcessedStart),
                                    cb.lessThanOrEqualTo(root.get("processedDate"), finalProcessedEnd)
                            ));
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
                Set<String> matchingVariations = getMatchingInstitutionVariations(institution);
                if (matchingVariations.isEmpty()) {
                    throw new IllegalArgumentException("Couldn't find department name: " + institution);
                }
                spec = spec.and((root, query, cb) -> root.get("institution").in(matchingVariations));
            }
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // URL filtering
        if (url != null && !url.isEmpty()) {
            final String urlPattern = "%" + url.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("url")), urlPattern));
        }

        List<Problem> problems = problemRepository.findAll(spec);
        return ResponseEntity.ok(problems);
    }

    private Set<String> getMatchingInstitutionVariations(String department) {
        Set<String> matchingVariations = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : institutionMappings.entrySet()) {
            if (entry.getValue().stream().anyMatch(variation -> variation.equalsIgnoreCase(department))) {
                matchingVariations.addAll(entry.getValue());
            }
        }
        return matchingVariations;
    }

    @GetMapping("/exportExcel")
    public void exportExcel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"feedback_export.xlsx\"");

        Specification<Problem> spec = buildExportSpecification(request);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ServletOutputStream outputStream = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("Feedback Data");

            String[] columns = {
                    "Problem Date", "Time Stamp (UTC)", "Problem Details", "Language", "Title",
                    "URL", "Institution", "Section", "Theme", "Device Type", "Browser"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            List<Problem> problems = problemRepository.findAll(spec);
            int rowNum = 1;
            for (Problem problem : problems) {
                Row row = sheet.createRow(rowNum++);
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

                if (rowNum % 100 == 0) {
                    try {
                        ((SXSSFSheet) sheet).flushRows(100);
                    } catch (IOException e) {
                        LOG.error("Error flushing rows", e);
                    }
                }
            }

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

        Specification<Problem> spec = buildExportSpecification(request);

        Writer writer = response.getWriter();
        try {
            writer.write("Problem Date,Time Stamp (UTC),Problem Details,Language,Title,URL,Institution,Section,Theme,Device Type,Browser\n");

            List<Problem> problems = problemRepository.findAll(spec);
            for (Problem problem : problems) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
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
            }
        } finally {
            writer.close();
        }
    }

    private Specification<Problem> buildExportSpecification(HttpServletRequest request) {
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

        Specification<Problem> spec = (root, query, cb) -> cb.equal(root.get("processed"), "true");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            spec = spec.and((root, query, cb) ->
                    cb.and(
                            cb.greaterThanOrEqualTo(root.get("problemDate"), start.format(formatter)),
                            cb.lessThanOrEqualTo(root.get("problemDate"), end.format(formatter))
                    ));
        }

        if (theme != null && !theme.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("theme"), theme));
        }

        if (section != null && !section.isEmpty()) {
            List<String> sectionList = sectionMappings.getOrDefault(section.toLowerCase(), Collections.singletonList(section));
            spec = spec.and((root, query, cb) -> root.get("section").in(sectionList));
        }

        if (language != null && !language.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("language"), language));
        }

        if (titles != null && titles.length > 0) {
            spec = spec.and((root, query, cb) -> root.get("title").in(Arrays.asList(titles)));
        }

        if (url != null && !url.isEmpty()) {
            final String urlPattern = "%" + url.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("url")), urlPattern));
        }

        if (department != null && !department.isEmpty()) {
            Set<String> matchingVariations = getMatchingInstitutionVariations(department);
            if (!matchingVariations.isEmpty()) {
                spec = spec.and((root, query, cb) -> root.get("institution").in(matchingVariations));
            }
        }

        if (comments != null && !comments.isEmpty()) {
            final String commentPattern = "%" + comments.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("problemDetails")), commentPattern));
        }

        if (error_keyword) {
            Set<String> keywordsToCheck = new HashSet<>();
            keywordsToCheck.addAll(errorKeywordService.getEnglishKeywords());
            keywordsToCheck.addAll(errorKeywordService.getFrenchKeywords());
            keywordsToCheck.addAll(errorKeywordService.getBilingualKeywords());

            if (!keywordsToCheck.isEmpty()) {
                LOG.debug("Checking {} error keywords for export", keywordsToCheck.size());
                spec = spec.and((root, query, cb) -> {
                    List<Predicate> keywordPredicates = new ArrayList<>();
                    for (String keyword : keywordsToCheck) {
                        keywordPredicates.add(cb.like(cb.lower(root.get("problemDetails")), "%" + keyword.toLowerCase() + "%"));
                    }
                    return cb.or(keywordPredicates.toArray(new Predicate[0]));
                });
            }
        }

        return spec;
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

        Map<String, String> dateMap = problemDateService.getProblemDates();

        if (dateMap != null) {
            mav.addObject("earliestDate", dateMap.get("earliestDate"));
            mav.addObject("latestDate", dateMap.get("latestDate"));
        } else {
            mav.addObject("earliestDate", "N/A");
            mav.addObject("latestDate", "N/A");
        }
        mav.addObject("lang", lang);

        mav.setViewName("pageFeedback_" + lang);
        return mav;
    }

    @GetMapping(value = "/feedbackData")
    @ResponseBody
    public DataTablesOutput<Problem> list(@Valid DataTablesInput input, HttpServletRequest request) {
        String pageLang = (String) request.getSession().getAttribute("lang");
        String language = request.getParameter("language");
        String department = request.getParameter("department");
        String comments = request.getParameter("comments");
        String theme = request.getParameter("theme");
        String section = request.getParameter("section");
        String url = request.getParameter("url");
        Boolean error_keyword = "true".equals(request.getParameter("error_keyword"));
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String[] titles = request.getParameterValues("titles[]");

        Specification<Problem> spec = (root, query, cb) -> cb.equal(root.get("processed"), "true");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            spec = spec.and((root, query, cb) ->
                    cb.and(
                            cb.greaterThanOrEqualTo(root.get("problemDate"), start.format(formatter)),
                            cb.lessThanOrEqualTo(root.get("problemDate"), end.format(formatter))
                    ));
        }

        if (theme != null && !theme.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("theme"), theme));
        }

        if (section != null && !section.isEmpty()) {
            List<String> sectionList = sectionMappings.getOrDefault(section.toLowerCase(), Collections.singletonList(section));
            spec = spec.and((root, query, cb) -> root.get("section").in(sectionList));
        }

        if (language != null && !language.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("language"), language));
        }

        if (titles != null && titles.length > 0) {
            spec = spec.and((root, query, cb) -> root.get("title").in(Arrays.asList(titles)));
        }

        if (url != null && !url.isEmpty()) {
            final String urlPattern = "%" + url.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("url")), urlPattern));
        }

        if (department != null && !department.isEmpty()) {
            Set<String> matchingVariations = getMatchingInstitutionVariations(department);
            if (!matchingVariations.isEmpty()) {
                spec = spec.and((root, query, cb) -> root.get("institution").in(matchingVariations));
            }
        }

        if (comments != null && !comments.isEmpty()) {
            final String commentPattern = "%" + comments.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("problemDetails")), commentPattern));
        }

        DataTablesOutput<Problem> results;

        if (error_keyword) {
            Set<String> keywordsToCheck = new HashSet<>();
            keywordsToCheck.addAll(errorKeywordService.getEnglishKeywords());
            keywordsToCheck.addAll(errorKeywordService.getFrenchKeywords());
            keywordsToCheck.addAll(errorKeywordService.getBilingualKeywords());

            if (!keywordsToCheck.isEmpty()) {
                LOG.debug("Checking {} error keywords", keywordsToCheck.size());
                Specification<Problem> keywordSpec = (root, query, cb) -> {
                    List<Predicate> keywordPredicates = new ArrayList<>();
                    for (String keyword : keywordsToCheck) {
                        keywordPredicates.add(cb.like(cb.lower(root.get("problemDetails")), "%" + keyword.toLowerCase() + "%"));
                    }
                    return cb.or(keywordPredicates.toArray(new Predicate[0]));
                };
                spec = spec.and(keywordSpec);
            }
        }

        results = problemRepository.findAll(input, spec);

        setInstitution(results, pageLang);
        return results;
    }

    private void setInstitution(DataTablesOutput<Problem> problems, String lang) {
        for (Problem problem : problems.getData()) {
            String currentInstitution = problem.getInstitution();
            for (Map.Entry<String, List<String>> entry : institutionMappings.entrySet()) {
                if (entry.getValue().contains(currentInstitution)) {
                    problem.setInstitution(entry.getValue().get(lang.equalsIgnoreCase("fr") ? 1 : 0));
                    break;
                }
            }
        }
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
