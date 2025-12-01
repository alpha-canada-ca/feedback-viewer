package ca.gc.tbs.controller;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.repository.ProblemRepository;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ReportController {

  @Autowired ProblemRepository problemRepository;

  public static String INPUT_FILENAME = "page_success_may_24.csv";

  public ReportController() {}

  @GetMapping("/reports")
  public View generateReports() throws Exception {
    BufferedWriter writer =
        Files.newBufferedWriter(Paths.get("/tmp/" + INPUT_FILENAME));

    try (CSVPrinter csvPrinter =
        new CSVPrinter(
            writer,
            CSVFormat.DEFAULT.withHeader(
                "Ref Number",
                "Date/time received",
                "Page Title",
                "Page URL",
                "Y/N",
                "What's wrong",
                "Details",
                "Topic",
                "Personal info",
                "(Y/N)",
                "Notes",
                "",
                "Test - auto-topic generator"))) {

      List<Problem> problems = this.problemRepository.findAll();
      for (Problem problem : problems) {
        csvPrinter.printRecord(
            problem.getId(),
            problem.getProblemDate(),
            problem.getTitle(),
            problem.getUrl(),
            "N",
            problem.getProblemDetails(),
            String.join(", ", problem.getTags()),
            "N",
            "",
            "");
      }
      csvPrinter.flush();
    }

    return new RedirectView("/reports/view");
  }

  @GetMapping("/reports/view")
  public String viewReports() {
    return "reports";
  }
}
