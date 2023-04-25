package ca.gc.tbs.controller;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.repository.ProblemRepository;
import ca.gc.tbs.service.UserService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesOutput;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Controller
public class ProblemController {

    public static final long DAY_IN_MS = 1000 * 60 * 60 * 24;
    private static final Logger LOG = LoggerFactory.getLogger(ProblemController.class);
    private static final boolean ASC = true;
    private static final boolean DESC = false;
    private final HashMap<String, String> tagTranslations = new HashMap<>();
    String[][] translations = {
            /* ENGLISH, FRENCH */{"The answer I need is missing", "La réponse dont j’ai besoin n’est pas là"},
            {"The information isn't clear", "L'information n'est pas claire"},
            {"I can't find the information", "Je ne peux pas trouver l'information"},
            {"The information isn’t clear", "L'information n'est pas claire"},
            {"I’m not in the right place", "Je ne suis pas au bon endroit"},
            {"I'm not in the right place", "Je ne suis pas au bon endroit"},
            {"Something is broken or incorrect", "Quelque chose est brisé ou incorrect"},
            {"Other reason", "Autre raison"},
            {"The information is hard to understand", "l'information est difficile à comprendre"},
            {"Health", "Santé"}, {"Taxes", "Impôt"}, {"Travel", "Voyage"},
            {"Public Health Agency of Canada", "Agence de santé publique du Canada"},
            {"Health Canada", "Santé Canada"}, {"CRA", "ARC"}, {"ISED", "ISDE"}, {"Example", "Exemple"},
            {"CEWS", "SSUC"}, {"CRSB", "PCMRE"}, {"CRB", "PCRE"}, {"CRCB", "PCREPA"}, {"CERS", "SUCL"},
            {"Vaccines", "Vaccins"}, {"Business", "Entreprises"}, {"WFHE", "DTDE"},
            {"travel-wizard", "assistant-voyage"}, {"PTR", "DRP"}, {"COVID Alert", "Alerte COVID"},
            {"Financial Consumer Agency of Canada", "Agence de la consommation en matière financière du Canada"},
            {"National Research Council", "Conseil national de recherches"},
            {"Department of Fisheries and Oceans", "Pêches et Océans Canada"},
            {"Money and finances", "Argent et finances"}, {"Science and innovation", "Science et innovation"},
            {"Environment and natural resources", "Environnement et ressources naturelles"}};
    private final HashMap<String, String> translationsMap = new HashMap<>(translations.length);
    private int totalComments = 0;
    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserService userService;

    private static HashMap<String, Integer> sortByValue(HashMap<String, Integer> unsortMap, final boolean order) {
        List<Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue())
                : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));

    }

    private static void printMap(HashMap<String, Integer> map) {
        map.forEach((key, value) -> System.out.println("Key : " + key + " Value : " + value));
    }

    public void populateTranslationsMap() {
        for (String[] translation : translations) {
            translationsMap.put(translation[0], translation[1]);
        }
    }

    // This function grabs all the models and associated URLs from the google
    // spreadsheet.
    public void importTagTranslations() throws Exception {
        final Reader reader = new InputStreamReader(new URL(
                "https://docs.google.com/spreadsheets/d/1xcoSXKwH0-_N_t056pfeEXzAXseZhpFMnvUsvmF0OBw/export?format=csv")
                .openConnection().getInputStream(),
                StandardCharsets.UTF_8);
        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
        try {
            for (final CSVRecord record : parser) {
                try {
                    if (!record.get("FRENCH_TAG").equals(""))
                        tagTranslations.put(record.get("ENGLISH_TAG"), record.get("FRENCH_TAG"));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        } finally {
            parser.close();
            reader.close();
        }
    }

    @GetMapping(value = "/pageFeedback")
    public ModelAndView pageFeedback(HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView();
        // lang = request.getParameter("lang");
        String lang = (String) request.getSession().getAttribute("lang");
        importTagTranslations();
        populateTranslationsMap();
        // uniqueValues();
        // System.out.println(tagTranslations.size());
        mav.setViewName("pageFeedback_" + lang);
        return mav;
    }

    @GetMapping(value = "/problemData")
    @ResponseBody
    public DataTablesOutput<Problem> list(@Valid DataTablesInput input, HttpServletRequest request) {
        Criteria findProcessed = where("processed").is("true");

        String lang = (String) request.getSession().getAttribute("lang");
        String dateSearchVal = input.getColumn("problemDate").get().getSearch().getValue();
        String deptSearchVal = input.getColumn("institution").get().getSearch().getValue();
        String sectionSearchVal = input.getColumn("section").get().getSearch().getValue();
        String themeSearchVal = input.getColumn("theme").get().getSearch().getValue();
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        Criteria dateCriteria = buildDateCriteria(dateSearchVal, simpleDateFormat);
        if (dateCriteria != null) {
            return findProblemsWithCriteria(input, findProcessed, dateCriteria, lang);
        }

        if (containsTilde(deptSearchVal, sectionSearchVal, themeSearchVal)) {
            return processProblemsWithTilde(input, lang, findProcessed, deptSearchVal, sectionSearchVal, themeSearchVal);
        }

        return findProblemsWithCriteria(input, findProcessed, null, lang);
    }

    private Criteria buildSingleDateCriteria(String dateSearchVal, SimpleDateFormat simpleDateFormat) {
        String date = null;
        if (dateSearchVal.contains("today")) {
            date = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        } else if (dateSearchVal.contains("yesterday")) {
            date = simpleDateFormat.format(new Date(System.currentTimeMillis() - DAY_IN_MS));
        } else if (dateSearchVal.contains("seven")) {
            date = simpleDateFormat.format(new Date(System.currentTimeMillis() - (7 * DAY_IN_MS)));
        } else if (dateSearchVal.contains("fifteen")) {
            date = simpleDateFormat.format(new Date(System.currentTimeMillis() - (15 * DAY_IN_MS)));
        } else if (dateSearchVal.contains("thirty")) {
            date = simpleDateFormat.format(new Date(System.currentTimeMillis() - (30 * DAY_IN_MS)));
        }
        if (date != null) {
            return where("problemDate").gte(date);
        }
        return null;
    }

    private DataTablesOutput<Problem> findProblemsWithCriteria(DataTablesInput input, Criteria findProcessed,
                                                               Criteria dateCriteria, String lang) {
        DataTablesOutput<Problem> problems;

        if (dateCriteria != null) {
            problems = problemRepository.findAll(input, dateCriteria, findProcessed);
        } else {
            problems = problemRepository.findAll(input, findProcessed);
        }

        if (lang.equals("fr")) {
            for (int i = 0; i < problems.getData().size(); i++) {
                problems.getData().get(i)
                        .setInstitution(translationsMap.get(problems.getData().get(i).getInstitution()));
                problems.getData().get(i).setProblem(translationsMap.get(problems.getData().get(i).getProblem()));
                problems.getData().get(i).setTheme(translationsMap.get(problems.getData().get(i).getTheme()));
                problems.getData().get(i).setSection(translationsMap.get(problems.getData().get(i).getSection()));

                List<String> tags = problems.getData().get(i).getTags();
                for (int j = 0; j < tags.size(); j++) {
                    if (tagTranslations.containsKey(tags.get(j)))
                        tags.set(j, tagTranslations.get(tags.get(j)));
                }
            }
        }

        return problems;
    }


    private Criteria buildDateCriteria(String dateSearchVal, SimpleDateFormat simpleDateFormat) {
        if (dateSearchVal.contains(":")) {
            return buildDateRangeCriteria(dateSearchVal);
        } else {
            return buildSingleDateCriteria(dateSearchVal, simpleDateFormat);
        }
    }

    private boolean containsTilde(String... searchValues) {
        return Arrays.stream(searchValues).anyMatch(searchVal -> searchVal.contains("~"));
    }

    private DataTablesOutput<Problem> processProblemsWithTilde(DataTablesInput input, String lang, Criteria findProcessed,
                                                               String deptSearchVal, String sectionSearchVal, String themeSearchVal) {
        String deptValue = deptSearchVal.equals("") ? "" : deptSearchVal.substring(0, deptSearchVal.length() - 2);
        String sectionValue = sectionSearchVal.equals("") ? "" : sectionSearchVal.substring(0, sectionSearchVal.length() - 2);
        String themeValue = themeSearchVal.equals("") ? "" : themeSearchVal.substring(0, themeSearchVal.length() - 2);

        input.getColumn("institution").get().getSearch().setValue(deptValue);
        input.getColumn("section").get().getSearch().setValue(sectionValue);
        input.getColumn("theme").get().getSearch().setValue(themeValue);

        input.setStart(0);
        input.setLength(-1);

        DataTablesOutput<Problem> urls = problemRepository.findAll(input);

        HashMap<String, Integer> urlCountMap = new HashMap<>();
        HashMap<String, List<String>> urlCountMap2 = new HashMap<>();

        for (int i = 0; i < urls.getData().size(); i++) {
            int count = urlCountMap.getOrDefault(urls.getData().get(i).getUrl(), 0);
            urlCountMap.put(urls.getData().get(i).getUrl(), count + 1);
            urlCountMap2.put(urls.getData().get(i).getUrl(), Arrays.asList(urls.getData().get(i).getTitle(),
                    urls.getData().get(i).getLanguage(), urls.getData().get(i).getInstitution(),
                    urls.getData().get(i).getTheme(), urls.getData().get(i).getSection()));
        }

        HashMap<String, Integer> sortedUrlCountMap = sortByValue(urlCountMap, DESC);

        ArrayList<Problem> urlList = new ArrayList<>();
        int index = 0;
        totalComments = 0;
        for (String key : sortedUrlCountMap.keySet()) {
            totalComments += urlCountMap.get(key);
            urls.getData().get(index).setUrl(key);
            urls.getData().get(index).setUrlEntries(sortedUrlCountMap.get(key));
            urls.getData().get(index).setTitle(urlCountMap2.get(key).get(0));
            urls.getData().get(index).setLanguage(urlCountMap2.get(key).get(1));
            urls.getData().get(index).setInstitution(urlCountMap2.get(key).get(2));
            urls.getData().get(index).setTheme(urlCountMap2.get(key).get(3));
            urls.getData().get(index).setSection(urlCountMap2.get(key).get(4));
            urlList.add(urls.getData().get(index));
            index++;
        }

        urls.setRecordsFiltered(sortedUrlCountMap.size());
        urls.setData(urlList);

        if (lang.equals("fr")) {
            for (int i = 0; i < urls.getData().size(); i++) {
                urls.getData().get(i).setInstitution(translationsMap.get(urls.getData().get(i).getInstitution()));
                urls.getData().get(i).setProblem(translationsMap.get(urls.getData().get(i).getProblem()));
                urls.getData().get(i).setTheme(translationsMap.get(urls.getData().get(i).getTheme()));
                urls.getData().get(i).setSection(translationsMap.get(urls.getData().get(i).getSection()));

                List<String> tags = urls.getData().get(i).getTags();
                for (int j = 0; j < tags.size(); j++) {
                    if (tagTranslations.containsKey(tags.get(j)))
                        tags.set(j, tagTranslations.get(tags.get(j)));
                }
            }
        }
        return urls;
    }

    private Criteria buildDateRangeCriteria(String dateSearchVal) {
        String[] ret = dateSearchVal.split(":");

        if (ret.length == 2) {
            String dateSearchValA = ret[0];
            String dateSearchValB = ret[1];

            return where("problemDate").gte(dateSearchValA).lte(dateSearchValB);
        }
        return null;
    }

    @RequestMapping(value = "/pageFeedback/totalCommentsCount")
    @ResponseBody
    public String totalCommentsCount() {
        return String.valueOf(totalComments);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
