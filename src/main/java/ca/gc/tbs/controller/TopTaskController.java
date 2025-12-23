package ca.gc.tbs.controller;

import ca.gc.tbs.domain.TopTaskSurvey;
import ca.gc.tbs.domain.User;
import ca.gc.tbs.repository.TopTaskRepository;
import ca.gc.tbs.security.JWTUtil;
import ca.gc.tbs.service.ProblemDateService;
import ca.gc.tbs.service.UserService;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TopTaskController {

  private static final Logger LOG = LoggerFactory.getLogger(TopTaskController.class);
  @Autowired private TopTaskRepository topTaskRepository;
  private int totalDistinctTasks = 0;
  private int totalTaskCount = 0;
  @Autowired private UserService userService;
  @Autowired private ProblemDateService problemDateService;
  @Autowired private JWTUtil jwtUtil;

  private static final Map<String, List<String>> institutionMappings = new HashMap<>();

  static {
    institutionMappings.put("AAFC", Arrays.asList("AAFC", "AAC", "AGRICULTURE AND AGRI-FOOD CANADA", "AGRICULTURE ET AGROALIMENTAIRE CANADA", "AAFC / AAC"));
    institutionMappings.put("CRA", Arrays.asList("CRA", "ARC", "CANADA REVENUE AGENCY", "AGENCE DU REVENU DU CANADA", "CRA / ARC"));
    institutionMappings.put("ESDC", Arrays.asList("ESDC", "EDSC", "EMPLOYMENT AND SOCIAL DEVELOPMENT CANADA", "EMPLOI ET DEVELOPPEMENT SOCIAL CANADA", "ESDC / EDSC"));
    institutionMappings.put("HC", Arrays.asList("HC", "SC", "HEALTH CANADA", "SANTE CANADA", "HC / SC"));
    institutionMappings.put("IRCC", Arrays.asList("IRCC", "IRCC", "IMMIGRATION, REFUGEES AND CITIZENSHIP CANADA", "IMMIGRATION, REFUGIES ET CITOYENNETE CANADA", "IRCC / IRCC"));
  }

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

  @RequestMapping(value = "/topTaskData", method = {RequestMethod.GET, RequestMethod.POST})
  @ResponseBody
  public DataTablesOutput<TopTaskSurvey> list(@Valid DataTablesInput input, HttpServletRequest request) {
    String departmentFilterVal = request.getParameter("department");
    String themeFilterVal = request.getParameter("theme");
    String[] taskFilterVals = request.getParameterValues("tasks[]");
    String startDateVal = request.getParameter("startDate");
    String endDateVal = request.getParameter("endDate");
    String groupFilterVal = request.getParameter("group");
    String language = request.getParameter("language");
    String includeCommentsOnlyParam = request.getParameter("includeCommentsOnly");
    boolean includeCommentsOnly = includeCommentsOnlyParam != null && includeCommentsOnlyParam.equals("true");
    String taskCompletionFilterVal = request.getParameter("taskCompletion");
    String comments = request.getParameter("comments");

    Specification<TopTaskSurvey> spec = (root, query, cb) -> cb.equal(root.get("processed"), "true");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    if (startDateVal != null && endDateVal != null) {
      LocalDate start = LocalDate.parse(startDateVal, formatter);
      LocalDate end = LocalDate.parse(endDateVal, formatter);
      spec = spec.and((root, query, cb) -> cb.and(
          cb.greaterThanOrEqualTo(root.get("dateTime"), start.format(formatter)),
          cb.lessThanOrEqualTo(root.get("dateTime"), end.format(formatter))));
    }

    if (language != null && !language.isEmpty()) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("language"), language));
    }

    if (themeFilterVal != null && !themeFilterVal.isEmpty()) {
      final String themeLower = themeFilterVal.toLowerCase();
      spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("theme")), "%" + themeLower + "%"));
    }

    if (groupFilterVal != null && !groupFilterVal.isEmpty()) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("grouping"), groupFilterVal));
    }

    if (departmentFilterVal != null && !departmentFilterVal.isEmpty()) {
      final String deptLower = departmentFilterVal.toLowerCase();
      spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("dept")), "%" + deptLower + "%"));
    }

    if (taskFilterVals != null && taskFilterVals.length > 0) {
      spec = spec.and((root, query, cb) -> root.get("task").in(Arrays.asList(taskFilterVals)));
    }

    if (includeCommentsOnly) {
      spec = spec.and((root, query, cb) -> cb.or(
          cb.and(cb.isNotNull(root.get("taskOther")), cb.notEqual(root.get("taskOther"), "")),
          cb.and(cb.isNotNull(root.get("themeOther")), cb.notEqual(root.get("themeOther"), "")),
          cb.and(cb.isNotNull(root.get("taskWhyNotComment")), cb.notEqual(root.get("taskWhyNotComment"), "")),
          cb.and(cb.isNotNull(root.get("taskImproveComment")), cb.notEqual(root.get("taskImproveComment"), ""))));
    }

    if (taskCompletionFilterVal != null && !taskCompletionFilterVal.isEmpty()) {
      List<String> allowed = new ArrayList<>();
      if (taskCompletionFilterVal.equals("Yes")) {
        allowed.add("Yes / Oui");
      } else if (taskCompletionFilterVal.equals("No")) {
        allowed.add("No / Non");
      }
      if (!allowed.isEmpty()) {
        spec = spec.and((root, query, cb) -> root.get("taskCompletion").in(allowed));
      }
    }

    if (comments != null && !comments.trim().isEmpty()) {
      final String commentLower = comments.trim().toLowerCase();
      spec = spec.and((root, query, cb) -> cb.or(
          cb.like(cb.lower(root.get("taskImproveComment")), "%" + commentLower + "%"),
          cb.like(cb.lower(root.get("taskWhyNotComment")), "%" + commentLower + "%"),
          cb.like(cb.lower(root.get("themeOther")), "%" + commentLower + "%"),
          cb.like(cb.lower(root.get("taskOther")), "%" + commentLower + "%")));
    }

    List<Map<String, Object>> distinctTaskCounts = topTaskRepository.findDistinctTaskCountsWithFilters(spec);
    totalDistinctTasks = distinctTaskCounts.size();
    DataTablesOutput<TopTaskSurvey> results = topTaskRepository.findAll(input, spec);
    totalTaskCount = (int) results.getRecordsFiltered();
    return results;
  }

  @GetMapping("/exportTopTaskExcel")
  public void exportTopTaskExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String currentDate = LocalDate.now().format(dateFormatter);
    Specification<TopTaskSurvey> spec = buildExportSpecification(request);

    List<TopTaskSurvey> surveys = topTaskRepository.findAll(spec);
    if (surveys.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      response.getWriter().write("No data found for export");
      return;
    }

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=\"top_task_survey_export_" + currentDate + ".xlsx\"");

    try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); ServletOutputStream outputStream = response.getOutputStream()) {
      Sheet sheet = workbook.createSheet("Top Task Survey Data");
      String[] columns = {"Date Time", "Language", "Device", "Department", "Theme", "Task", "Task Satisfaction", "Task Ease", "Task Completion"};
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < columns.length; i++) {
        headerRow.createCell(i).setCellValue(columns[i]);
      }

      int rowNum = 1;
      for (TopTaskSurvey survey : surveys) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(survey.getDateTime());
        row.createCell(1).setCellValue(survey.getLanguage());
        row.createCell(2).setCellValue(survey.getDevice());
        row.createCell(3).setCellValue(survey.getDept());
        row.createCell(4).setCellValue(survey.getTheme());
        row.createCell(5).setCellValue(survey.getTask());
        row.createCell(6).setCellValue(survey.getTaskSatisfaction());
        row.createCell(7).setCellValue(survey.getTaskEase());
        row.createCell(8).setCellValue(survey.getTaskCompletion());
        if (rowNum % 100 == 0) ((SXSSFSheet) sheet).flushRows(100);
      }
      workbook.write(outputStream);
    }
  }

  @GetMapping("/exportTopTaskCSV")
  public void exportTopTaskCSV(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String currentDate = LocalDate.now().format(dateFormatter);
    Specification<TopTaskSurvey> spec = buildExportSpecification(request);

    List<TopTaskSurvey> surveys = topTaskRepository.findAll(spec);
    if (surveys.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      response.getWriter().write("No data found for export");
      return;
    }

    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=\"top_task_survey_export_" + currentDate + ".csv\"");

    try (Writer writer = response.getWriter()) {
      writer.write("Date Time,Language,Device,Department,Theme,Task,Task Satisfaction,Task Ease,Task Completion\n");
      for (TopTaskSurvey survey : surveys) {
        writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
            escapeCSV(survey.getDateTime()), escapeCSV(survey.getLanguage()), escapeCSV(survey.getDevice()),
            escapeCSV(survey.getDept()), escapeCSV(survey.getTheme()), escapeCSV(survey.getTask()),
            escapeCSV(survey.getTaskSatisfaction()), escapeCSV(survey.getTaskEase()), escapeCSV(survey.getTaskCompletion())));
      }
    }
  }

  private Specification<TopTaskSurvey> buildExportSpecification(HttpServletRequest request) {
    String department = request.getParameter("department");
    String theme = request.getParameter("theme");
    String[] tasks = request.getParameterValues("tasks[]");
    String language = request.getParameter("language");
    String startDate = request.getParameter("startDate");
    String endDate = request.getParameter("endDate");

    Specification<TopTaskSurvey> spec = (root, query, cb) -> cb.equal(root.get("processed"), "true");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    if (startDate != null && endDate != null) {
      spec = spec.and((root, query, cb) -> cb.and(
          cb.greaterThanOrEqualTo(root.get("dateTime"), startDate),
          cb.lessThanOrEqualTo(root.get("dateTime"), endDate)));
    }
    if (language != null && !language.isEmpty()) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("language"), language));
    }
    if (theme != null && !theme.isEmpty()) {
      final String themeLower = theme.toLowerCase();
      spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("theme")), "%" + themeLower + "%"));
    }
    if (department != null && !department.isEmpty()) {
      final String deptLower = department.toLowerCase();
      spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("dept")), "%" + deptLower + "%"));
    }
    if (tasks != null && tasks.length > 0) {
      spec = spec.and((root, query, cb) -> root.get("task").in(Arrays.asList(tasks)));
    }
    return spec;
  }

  private String escapeCSV(String value) {
    if (value == null) return "";
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }

  @RequestMapping(value = "/topTaskSurvey/departments", produces = "application/json")
  @ResponseBody
  public List<Map<String, String>> departmentData(HttpServletRequest request) {
    return institutionMappings.entrySet().stream()
        .map(entry -> {
          String value = entry.getValue().get(entry.getValue().size() - 1);
          Map<String, String> departmentMap = new HashMap<>();
          departmentMap.put("value", value);
          departmentMap.put("display", value);
          return departmentMap;
        })
        .sorted((a, b) -> a.get("display").compareToIgnoreCase(b.get("display")))
        .collect(Collectors.toList());
  }

  @GetMapping("/api/toptasks")
  public ResponseEntity<?> getTopTasksJson(
      @RequestParam Map<String, String> requestParams,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(required = false) String institution,
      @RequestHeader(name = "Authorization") String authorizationHeader) {

    String userName = null;
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      userName = jwtUtil.extractUsername(authorizationHeader.substring(7));
    }

    if (userName != null) {
      User user = userService.findUserByEmail(userName);
      if (!userService.isAdmin(user) && !userService.isAPI(user)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
      }
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header missing.");
    }

    Specification<TopTaskSurvey> spec = (root, query, cb) -> cb.equal(root.get("processed"), "true");
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    if (startDate != null && endDate != null) {
      try {
        LocalDate start = LocalDate.parse(startDate, dateFormat);
        LocalDate end = LocalDate.parse(endDate, dateFormat);
        if (end.isBefore(start)) {
          return ResponseEntity.badRequest().body(Map.of("error", "endDate must be >= startDate"));
        }
        spec = spec.and((root, query, cb) -> cb.and(
            cb.greaterThanOrEqualTo(root.get("dateTime"), startDate),
            cb.lessThanOrEqualTo(root.get("dateTime"), endDate)));
      } catch (DateTimeParseException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format"));
      }
    }

    if (institution != null && !institution.isEmpty()) {
      final String instLower = institution.toLowerCase();
      spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("dept")), "%" + instLower + "%"));
    }

    List<TopTaskSurvey> results = topTaskRepository.findAll(spec);
    return ResponseEntity.ok(results);
  }

  @GetMapping(value = "/topTaskSurvey")
  public ModelAndView topTaskSurvey(HttpServletRequest request) {
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
      return topTaskRepository.findTaskTitlesBySearch(search);
    } else {
      return topTaskRepository.findDistinctTaskNames();
    }
  }
}
