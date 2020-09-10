package ca.gc.tbs.controller;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import ca.gc.tbs.domain.OriginalProblem;
import ca.gc.tbs.repository.OriginalProblemRepository;
import ca.gc.tbs.repository.ProblemRepository;
import ca.gc.tbs.service.ContentService;

@Controller
public class ImportController {

	@Autowired
	ProblemRepository problemRepository;

	@Autowired
	OriginalProblemRepository originalProblemRespository;

	@Autowired
	ContentService contentService;

	SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@GetMapping(value = "/importcsv")
	public View importData() throws Exception {
		final Reader reader = new InputStreamReader(new URL(
				"https://docs.google.com/spreadsheets/d/1tTNrPJqKyNNkJo1UaCoSp1RMpSz3dJsRKmieDglSAOU/export?format=csv")
						.openConnection().getInputStream(),
				"UTF-8");
		final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
		try {
			for (final CSVRecord record : parser) {
				try {
					OriginalProblem problem = new OriginalProblem();
					problem.setId(record.get("Ref Number").replace("/", ""));
					problem.setProblemDate(record.get("Date/time received"));
					problem.setTitle(record.get("Page Title"));
					problem.setUrl(record.get("Page URL"));
					problem.setProblem(record.get("What's wrong"));
					problem.setProblemDetails(record.get("Details"));
					problem.setYesno(record.get("Y/N"));
					String[] topics = record.get("Topic").trim().split(",");
					if (topics.length > 0) {
						problem.setTags(Arrays.asList(topics));
					}
					problem.setResolution("");
					problem.setResolutionDate("");
					problem.setInstitution("Health");
					if (problem.getUrl().contains("/en/")) {
						problem.setLanguage("en");
					} else {
						problem.setLanguage("fr");
					}
					problem.setProcessed("false");
					problem.setAirTableSync("false");
					problem.setAutoTagProcessed("false");
					problem.setPersonalInfoProcessed("false");
					problem.setDataOrigin("Health CSV");
					this.problemRepository.save(problem);
					this.originalProblemRespository.save(problem);

				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}

		} finally {
			parser.close();
			reader.close();
		}
		return new RedirectView("/problemDashboard");
	}
}
