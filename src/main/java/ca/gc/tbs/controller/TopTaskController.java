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
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TopTaskController {

  private static final Logger LOG = LoggerFactory.getLogger(TopTaskController.class);
  @Autowired private TopTaskRepository topTaskRepository;
  private int totalDistinctTasks = 0;

  private int totalTaskCount = 0;
  @Autowired private UserService userService;

  @Autowired private ProblemDateService problemDateService;
  @Autowired private ca.gc.tbs.service.InstitutionService institutionService;



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
  public DataTablesOutput<TopTaskSurvey> list(
      @Valid DataTablesInput input, HttpServletRequest request) {

    // Log request details for debugging
    LOG.info("=== TopTaskData Request Debug ===");
    LOG.info("Request URL: {}", request.getRequestURL());
    LOG.info("Query String: {}", request.getQueryString());
    LOG.info("Query String Length: {}", request.getQueryString() != null ? request.getQueryString().length() : 0);

    // Log all parameters
    request.getParameterMap().forEach((key, values) -> {
      LOG.info("Parameter '{}': {}", key, Arrays.toString(values));
    });

    String pageLang = (String) request.getSession().getAttribute("lang");
    String departmentFilterVal = request.getParameter("department");
    String themeFilterVal = request.getParameter("theme");
    if (themeFilterVal != null) {
        themeFilterVal = themeFilterVal.trim().replaceAll("\\s+", " "); // Trim and normalize spaces
    }
    String[] taskFilterVals = request.getParameterValues("tasks[]");
    String startDateVal = request.getParameter("startDate");
    String endDateVal = request.getParameter("endDate");
    String groupFilterVal = request.getParameter("group");
    String language = request.getParameter("language");
    String includeCommentsOnlyParam = request.getParameter("includeCommentsOnly");
    boolean includeCommentsOnly = includeCommentsOnlyParam != null && includeCommentsOnlyParam.equals("true");
    String taskCompletionFilterVal = request.getParameter("taskCompletion");
    String comments = request.getParameter("comments");

    // Log specific filter values
    LOG.info("Department: {}", departmentFilterVal);
    LOG.info("Theme (cleaned): {}", themeFilterVal);
    LOG.info("Tasks count: {}", taskFilterVals != null ? taskFilterVals.length : 0);
    if (taskFilterVals != null) {
      LOG.info("Tasks: {}", Arrays.toString(taskFilterVals));
    }
    LOG.info("Date range: {} to {}", startDateVal, endDateVal);
    LOG.info("Task Completion: {}", taskCompletionFilterVal);

    Criteria criteria = Criteria.where("processed").is("true");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    if (startDateVal != null && endDateVal != null) {
      LocalDate start = LocalDate.parse(startDateVal, formatter);
      LocalDate end = LocalDate.parse(endDateVal, formatter);
      criteria.and("dateTime").gte(start.format(formatter)).lte(end.format(formatter));
    }
    if (language != null && !language.isEmpty()) {
      criteria.and("language").is(language);
    }
    if (themeFilterVal != null && !themeFilterVal.isEmpty()) {
      criteria.and("theme").regex(Pattern.quote(themeFilterVal), "i");
    }
    if (groupFilterVal != null && !groupFilterVal.isEmpty()) {
      criteria.and("grouping").is(groupFilterVal);
    }
    if (departmentFilterVal != null && !departmentFilterVal.isEmpty()) {
      try {
        Criteria departmentCriteria = applyDepartmentFilter(new Criteria(), departmentFilterVal);
        criteria = new Criteria().andOperator(criteria, departmentCriteria);
      } catch (IllegalArgumentException e) {
        DataTablesOutput<TopTaskSurvey> output = new DataTablesOutput<>();
        output.setDraw(input.getDraw());
        output.setRecordsTotal(0);
        output.setRecordsFiltered(0);
        output.setData(Collections.emptyList());
        return output;
      }
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
          commentCriteriaWithTasks.add(
              new Criteria()
                  .andOperator(
                      taskCriteria,
                      new Criteria().orOperator(nonEmptyCriteria.toArray(new Criteria[0]))));
        }
        criteria.orOperator(commentCriteriaWithTasks.toArray(new Criteria[0]));
      } else {
        criteria.orOperator(nonEmptyCriteria.toArray(new Criteria[0]));
      }
    } else if (!combinedOrCriteria.isEmpty()) {
      criteria.orOperator(combinedOrCriteria.toArray(new Criteria[0]));
    }
    //taskCompletion filter
      if (taskCompletionFilterVal != null && !taskCompletionFilterVal.isEmpty()) {
          List<String> allowed = new ArrayList<>();
          if (taskCompletionFilterVal.equals("Yes")) {
              allowed.add("Yes / Oui");
          } else if (taskCompletionFilterVal.equals("No")) {
              allowed.add("No / Non");
          } else if (taskCompletionFilterVal.equals("I started this survey before I finished my visit")) {
              allowed.add("I started this survey before I finished my visit / J’ai commencé ce sondage avant d’avoir terminé ma visite");
          }
          if (!allowed.isEmpty()) {
              criteria.and("taskCompletion").in(allowed);
          }
      }
      // Comments filtering
      if (comments != null && !comments.trim().isEmpty() && !"null".equalsIgnoreCase(comments. trim())) {
          String escapedComment = escapeSpecialRegexCharacters(comments.trim());
          List<Criteria> commentCriteria = new ArrayList<>();
          commentCriteria.add(Criteria.where("taskImproveComment").regex(escapedComment, "i"));
          commentCriteria.add(Criteria.where("taskWhyNotComment").regex(escapedComment, "i"));
          commentCriteria.add(Criteria.where("themeOther").regex(escapedComment, "i"));
          commentCriteria.add(Criteria.where("taskOther").regex(escapedComment, "i"));

          criteria.andOperator(new Criteria().andOperator(criteria, new Criteria().orOperator(commentCriteria.toArray(new Criteria[0]))));
      }


    List<Map> distinctTaskCounts = topTaskRepository.findDistinctTaskCountsWithFilters(criteria);
    totalDistinctTasks = distinctTaskCounts.size();
    DataTablesOutput<TopTaskSurvey> results = topTaskRepository.findAll(input, criteria);

    setInstitution(results, pageLang);

    totalTaskCount = (int) results.getRecordsFiltered();
    return results;
  }

  private void setInstitution(DataTablesOutput<TopTaskSurvey> surveys, String lang) {
    for (TopTaskSurvey survey : surveys.getData()) {
      String currentDept = survey.getDept();
      if (currentDept == null || currentDept.isEmpty()) {
        String inferredDept = institutionService.getInstitutionFromUrl(survey.getSurveyReferrer());
        if (inferredDept != null) {
          currentDept = inferredDept;
        }
      }
      String translatedDept = institutionService.getTranslatedInstitution(currentDept, lang);
      survey.setDept(translatedDept);
    }
  }

  @GetMapping("/exportTopTaskExcel")
  public void exportTopTaskExcel(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    LOG.info("Starting Excel export...");

    try {
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      String currentDate = LocalDate.now().format(dateFormatter);

      // Build criteria from request parameters
      Criteria criteria = buildExportCriteria(request);
      Query query = buildExportQuery(criteria);

      // Check if we have any data before proceeding
      long count = mongoTemplate.count(query, TopTaskSurvey.class);
      LOG.info("Found {} records to export", count);

      if (count == 0) {
        LOG.warn("No data found for export with the given criteria");
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        response.getWriter().write("No data found for export");
        return;
      }

      // Set response headers after confirming we have data
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      String filename = "top_task_survey_export_" + currentDate + ".xlsx";
      response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");

      SXSSFWorkbook workbook = new SXSSFWorkbook(100);
      Sheet sheet = workbook.createSheet("Top Task Survey Data");

      // Create header row
      String[] columns = {
        "Date Time",
        "Time Stamp (UTC)",
        "Survey Referrer",
        "Language",
        "Device",
        "Screener",
        "Department",
        "Theme",
        "Theme Other",
        "Grouping",
        "Task",
        "Task Other",
        "Task Satisfaction",
        "Task Ease",
        "Task Completion",
        "Task Improve",
        "Task Improve Comment",
        "Task Why Not",
        "Task Why Not Comment",
        "Task Sampling",
        "Sampling Invitation",
        "Sampling GC",
        "Sampling Canada",
        "Sampling Theme",
        "Sampling Institution",
        "Sampling Grouping",
        "Sampling Task"
      };
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < columns.length; i++) {
        headerRow.createCell(i).setCellValue(columns[i]);
      }

      final int[] rowNum = {1};
      try (ServletOutputStream outputStream = response.getOutputStream()) {
        mongoTemplate.stream(query, TopTaskSurvey.class)
            .forEachRemaining(
                survey -> {
                  try {
                    Row row = sheet.createRow(rowNum[0]++);
                    row.createCell(0).setCellValue(survey.getDateTime());
                    row.createCell(1).setCellValue(survey.getTimeStamp());
                    row.createCell(2).setCellValue(survey.getSurveyReferrer());
                    row.createCell(3).setCellValue(survey.getLanguage());
                    row.createCell(4).setCellValue(survey.getDevice());
                    row.createCell(5).setCellValue(survey.getScreener());
                    row.createCell(6).setCellValue(survey.getDept());
                    row.createCell(7).setCellValue(survey.getTheme());
                    row.createCell(8).setCellValue(survey.getThemeOther());
                    row.createCell(9).setCellValue(survey.getGrouping());
                    row.createCell(10).setCellValue(survey.getTask());
                    row.createCell(11).setCellValue(survey.getTaskOther());
                    row.createCell(12).setCellValue(survey.getTaskSatisfaction());
                    row.createCell(13).setCellValue(survey.getTaskEase());
                    row.createCell(14).setCellValue(survey.getTaskCompletion());
                    row.createCell(15).setCellValue(survey.getTaskImprove());
                    row.createCell(16).setCellValue(survey.getTaskImproveComment());
                    row.createCell(17).setCellValue(survey.getTaskWhyNot());
                    row.createCell(18).setCellValue(survey.getTaskWhyNotComment());
                    row.createCell(19).setCellValue(survey.getTaskSampling());
                    row.createCell(20).setCellValue(survey.getSamplingInvitation());
                    row.createCell(21).setCellValue(survey.getSamplingGC());
                    row.createCell(22).setCellValue(survey.getSamplingCanada());
                    row.createCell(23).setCellValue(survey.getSamplingTheme());
                    row.createCell(24).setCellValue(survey.getSamplingInstitution());
                    row.createCell(25).setCellValue(survey.getSamplingGrouping());
                    row.createCell(26).setCellValue(survey.getSamplingTask());

                    if (rowNum[0] % 100 == 0) {
                      ((SXSSFSheet) sheet).flushRows(100);
                      LOG.debug("Flushed {} rows", rowNum[0]);
                    }
                  } catch (Exception e) {
                    LOG.error("Error writing row {}: {}", rowNum[0], e.getMessage());
                  }
                });

        LOG.info("Writing {} rows to Excel file", rowNum[0] - 1);
        workbook.write(outputStream);
        outputStream.flush();
        LOG.info("Excel export completed successfully");
      } catch (Exception e) {
        LOG.error("Error writing to output stream", e);
        throw e;
      } finally {
        workbook.dispose();
        workbook.close();
      }
    } catch (Exception e) {
      LOG.error("Error exporting Excel", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write("Error exporting data: " + e.getMessage());
    }
  }

  @GetMapping("/exportTopTaskCSV")
  public void exportTopTaskCSV(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    LOG.info("Starting CSV export...");

    try {
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      String currentDate = LocalDate.now().format(dateFormatter);

      // Build criteria from request parameters
      Criteria criteria = buildExportCriteria(request);
      Query query = buildExportQuery(criteria);

      // Check if we have any data before proceeding
      long count = mongoTemplate.count(query, TopTaskSurvey.class);
      LOG.info("Found {} records to export", count);

      if (count == 0) {
        LOG.warn("No data found for export with the given criteria");
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        response.getWriter().write("No data found for export");
        return;
      }

      // Set response headers after confirming we have data
      response.setContentType("text/csv");
      String filename = "top_task_survey_export_" + currentDate + ".csv";
      response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");

      try (Writer writer = response.getWriter()) {
        // Write CSV header
        writer.write(
            "Date Time,Time Stamp (UTC),Survey"
                + " Referrer,Language,Device,Screener,Department,Theme,Theme"
                + " Other,Grouping,Task,Task Other,Task Satisfaction,Task Ease,Task Completion,Task"
                + " Improve,Task Improve Comment,Task Why Not,Task Why Not Comment,Task"
                + " Sampling,Sampling Invitation,Sampling GC,Sampling Canada,Sampling"
                + " Theme,Sampling Institution,Sampling Grouping,Sampling Task\n");

        // Stream and write data
        mongoTemplate.stream(query, TopTaskSurvey.class)
            .forEachRemaining(
                survey -> {
                  try {
                    writer.write(
                        String.format(
                            "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            escapeCSV(survey.getDateTime()),
                            escapeCSV(survey.getTimeStamp()),
                            escapeCSV(survey.getSurveyReferrer()),
                            escapeCSV(survey.getLanguage()),
                            escapeCSV(survey.getDevice()),
                            escapeCSV(survey.getScreener()),
                            escapeCSV(survey.getDept()),
                            escapeCSV(survey.getTheme()),
                            escapeCSV(survey.getThemeOther()),
                            escapeCSV(survey.getGrouping()),
                            escapeCSV(survey.getTask()),
                            escapeCSV(survey.getTaskOther()),
                            escapeCSV(survey.getTaskSatisfaction()),
                            escapeCSV(survey.getTaskEase()),
                            escapeCSV(survey.getTaskCompletion()),
                            escapeCSV(survey.getTaskImprove()),
                            escapeCSV(survey.getTaskImproveComment()),
                            escapeCSV(survey.getTaskWhyNot()),
                            escapeCSV(survey.getTaskWhyNotComment()),
                            escapeCSV(survey.getTaskSampling()),
                            escapeCSV(survey.getSamplingInvitation()),
                            escapeCSV(survey.getSamplingGC()),
                            escapeCSV(survey.getSamplingCanada()),
                            escapeCSV(survey.getSamplingTheme()),
                            escapeCSV(survey.getSamplingInstitution()),
                            escapeCSV(survey.getSamplingGrouping()),
                            escapeCSV(survey.getSamplingTask())));
                  } catch (IOException e) {
                    LOG.error("Error writing CSV row: {}", e.getMessage());
                  }
                });

        writer.flush();
        LOG.info("CSV export completed successfully");
      } catch (Exception e) {
        LOG.error("Error writing to CSV output", e);
        throw e;
      }
    } catch (Exception e) {
      LOG.error("Error exporting CSV", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write("Error exporting data: " + e.getMessage());
    }
  }

  // Helper methods for the export endpoints
  private Criteria buildExportCriteria(HttpServletRequest request) {
    String department = request.getParameter("department");
    String theme = request.getParameter("theme");
    String[] tasks = request.getParameterValues("tasks[]");
    String group = request.getParameter("group");
    String language = request.getParameter("language");
    String startDate = request.getParameter("startDate");
    String endDate = request.getParameter("endDate");
    String comments = request.getParameter("comments");
    boolean includeCommentsOnly = Boolean.parseBoolean(request.getParameter("includeCommentsOnly"));

    Criteria criteria = Criteria.where("processed").is("true");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    if (startDate != null && endDate != null) {
      LocalDate start = LocalDate.parse(startDate, formatter);
      LocalDate end = LocalDate.parse(endDate, formatter);
      criteria.and("dateTime").gte(start.format(formatter)).lte(end.format(formatter));
    }
    if (language != null && !language.isEmpty()) {
      criteria.and("language").is(language);
    }
    if (theme != null && !theme.isEmpty()) {
      criteria.and("theme").regex(theme, "i");
    }
    if (group != null && !group.isEmpty()) {
      criteria.and("grouping").is(group);
    }
    if (department != null && !department.isEmpty()) {
      criteria.and("dept").regex("^" + Pattern.quote(department) + "$", "i");
    }

    List<Criteria> combinedOrCriteria = new ArrayList<>();
    if (tasks != null && tasks.length > 0) {
      for (String task : tasks) {
        Criteria taskCriteria = Criteria.where("task").is(task);
        combinedOrCriteria.add(taskCriteria);
      }
    }

    if (includeCommentsOnly) {
      List<Criteria> nonEmptyCriteria = createNonEmptyCriteria();
      if (!combinedOrCriteria.isEmpty()) {
        List<Criteria> commentCriteriaWithTasks = new ArrayList<>();
        for (Criteria taskCriteria : combinedOrCriteria) {
          commentCriteriaWithTasks.add(
              new Criteria()
                  .andOperator(
                      taskCriteria,
                      new Criteria().orOperator(nonEmptyCriteria.toArray(new Criteria[0]))));
        }
        criteria.andOperator(
            new Criteria().orOperator(commentCriteriaWithTasks.toArray(new Criteria[0])));
      } else {
        criteria.andOperator(new Criteria().orOperator(nonEmptyCriteria.toArray(new Criteria[0])));
      }
    } else if (!combinedOrCriteria.isEmpty()) {
      criteria.andOperator(new Criteria().orOperator(combinedOrCriteria.toArray(new Criteria[0])));
    }

      if (comments != null && !comments.isEmpty()) {
          String escapedComment = escapeSpecialRegexCharacters(comments);
          List<Criteria> commentCriteria = new ArrayList<>();
          commentCriteria.add(Criteria.where("taskImproveComment").regex(escapedComment, "i"));
          commentCriteria.add(Criteria.where("taskWhyNotComment").regex(escapedComment, "i"));
          commentCriteria.add(Criteria.where("themeOther").regex(escapedComment, "i"));
          commentCriteria.add(Criteria. where("taskOther").regex(escapedComment, "i"));

          criteria.andOperator(new Criteria().andOperator(criteria, new Criteria().orOperator(commentCriteria.toArray(new Criteria[0]))));
      }

    return criteria;
  }

  private Query buildExportQuery(Criteria criteria) {
    Query query = new Query(criteria);
    query
        .fields()
        .include("dateTime")
        .include("timeStamp")
        .include("surveyReferrer")
        .include("language")
        .include("device")
        .include("screener")
        .include("dept")
        .include("theme")
        .include("themeOther")
        .include("grouping")
        .include("task")
        .include("taskOther")
        .include("taskSatisfaction")
        .include("taskEase")
        .include("taskCompletion")
        .include("taskImprove")
        .include("taskImproveComment")
        .include("taskWhyNot")
        .include("taskWhyNotComment")
        .include("taskSampling")
        .include("samplingInvitation")
        .include("samplingGC")
        .include("samplingCanada")
        .include("samplingTheme")
        .include("samplingInstitution")
        .include("samplingGrouping")
        .include("samplingTask");
    return query;
  }

  private String escapeCSV(String value) {
    if (value == null) {
      return "";
    }
    return "\"" + value.replace("\"", "\"\"") + "\"";
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

    return institutionService.getInstitutionMappings().entrySet().stream()
        .map(
            entry -> {
              String value =
                  entry
                      .getValue()
                      .get(entry.getValue().size() - 1); // Get the last element (with slashes)
              // Keep same format for French
              String display = value; // Keep same format for English

              Map<String, String> departmentMap = new HashMap<>();
              departmentMap.put("value", value);
              departmentMap.put("display", display);
              return departmentMap;
            })
        .sorted(
            (a, b) -> a.get("display").compareToIgnoreCase(b.get("display"))) // Sort alphabetically
        .collect(Collectors.toList());
  }

  @Autowired private MongoTemplate mongoTemplate;
  @Autowired private JWTUtil jwtUtil;

  @GetMapping("/api/toptasks")
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
          criteria.and("dateTime").gte(startDate).lte(endDate);
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

    try {
      if (institution != null && !institution.isEmpty()) {
        criteria = applyDepartmentFilter(criteria, institution);
      }
    } catch (IllegalArgumentException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    if (url != null && !url.isEmpty()) {
      System.out.println("URL: " + url);
      criteria.and("surveyReferrer").regex(url, "i");
    }
    Query query = new Query(criteria);
    query
        .fields()
        .exclude("_id")
        .exclude("processed")
        .exclude("personalInfoProcessed")
        .exclude("autoTagProcessed")
        .exclude("_class");
    List<Document> documents = mongoTemplate.find(query, Document.class, "toptasksurvey");
    return ResponseEntity.ok(documents);
  }

  private Criteria applyDepartmentFilter(Criteria criteria, String department) {
    Set<String> matchingVariations = institutionService.getMatchingVariations(department);

    if (matchingVariations.isEmpty()) {
      throw new IllegalArgumentException("Couldn't find department name: " + department);
    }

    criteria.and("dept").in(matchingVariations);
    return criteria;
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

  private String escapeSpecialRegexCharacters(String input) {
      if (input == null) {
          return null;
      }
      // Escape all regex metacharacters
      return input.replaceAll("([\\\\.|^$|()\\[\\]{}*+?])", "\\\\$1");
  }

  public UserService getUserService() {
    return userService;
  }

  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}
