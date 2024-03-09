package ca.gc.tbs.controller;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.repository.ProblemRepository;
import ca.gc.tbs.service.ProblemDateService;
import ca.gc.tbs.service.UserService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;


@Controller
public class DashboardController {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemController.class);

    @Autowired
    private ProblemRepository problemRepository;
    private static final boolean ASC = true;
    private static final boolean DESC = false;
    @Autowired
    private ProblemDateService problemDateService;
    @Autowired
    private MongoTemplate mongoTemplate;
    private int totalComments = 0;
    @Autowired
    private UserService userService;

    private static final Map<String, List<String>> institutionMappings = new HashMap<>();

    static {
        institutionMappings.put("AAFC", Arrays.asList("AAFC", "AAC", "AGRICULTURE AND AGRI-FOOD CANADA", "AGRICULTURE ET AGROALIMENTAIRE CANADA", "AAFC/AAC"));
        institutionMappings.put("ATSSC", Arrays.asList("ATSSC", "SCDATA", "ADMINISTRATIVE TRIBUNALS SUPPORT SERVICE OF CANADA", "SERVICE CANADIEN D’APPUI AUX TRIBUNAUX ADMINISTRATIFS", "ATSSC/SCDATA"));
        institutionMappings.put("ACOA", Arrays.asList("ACOA", "APECA", "ATLANTIC CANADA OPPORTUNITIES AGENCY", "AGENCE DE PROMOTION ÉCONOMIQUE DU CANADA ATLANTIQUE", "ACOA/APECA"));
        institutionMappings.put("CATSA", Arrays.asList("CATSA", "ACSTA", "CANADIAN AIR TRANSPORT SECURITY AUTHORITY", "ADMINISTRATION CANADIENNE DE LA SÛRETÉ DU TRANSPORT AÉRIEN", "CATSA/ACSTA"));
        institutionMappings.put("CFIA", Arrays.asList("CFIA", "ACIA", "CANADIAN FOOD INSPECTION AGENCY", "AGENCE CANADIENNE D’INSPECTION DES ALIMENTS", "CFIA/ACIA"));
        institutionMappings.put("CIRNAC", Arrays.asList("CIRNAC", "RCAANC", "CROWN-INDIGENOUS RELATIONS AND NORTHERN AFFAIRS CANADA", "RELATIONS COURONNE-AUTOCHTONES ET AFFAIRES DU NORD CANADA", "CIRNAC/RCAANC"));
        institutionMappings.put("NSERC", Arrays.asList("NSERC", "CRSNG", "NATURAL SCIENCES AND ENGINEERING RESEARCH CANADA", "CONSEIL DE RECHERCHES EN SCIENCES NATURELLES ET EN GÉNIE DU CANADA", "NSERC/CRSNG"));
        institutionMappings.put("CBSA", Arrays.asList("CBSA", "ASFC", "CANADA BORDER SERVICES AGENCY", "AGENCE DES SERVICES FRONTALIERS DU CANADA", "CBSA/ASFC"));
        institutionMappings.put("CCG", Arrays.asList("CCG", "GCC", "CANADIAN COAST GUARD", "GARDE CÔTIÈRE CANADIENNE", "CCG/GCC"));
        institutionMappings.put("CIHR", Arrays.asList("CIHR", "IRSC", "CANADIAN INSTITUTES OF HEALTH RESEARCH", "INSTITUTS DE RECHERCHE EN SANTÉ DU CANADA", "CIHR/IRSC"));
        institutionMappings.put("CIPO", Arrays.asList("CIPO", "OPIC", "CANADIAN INTELLECTUAL PROPERTY OFFICE", "OFFICE DE LA PROPRIÉTÉ INTELLECTUELLE DU CANADA", "CIPO/OPIC"));
        institutionMappings.put("CRA", Arrays.asList("CRA", "ARC", "CANADA REVENUE AGENCY", "AGENCE DU REVENU DU CANADA", "CRA/ARC"));
        institutionMappings.put("CRTC", Arrays.asList("CRTC", "CRTC", "CANADIAN RADIO-TELEVISION AND TELECOMMUNICATIONS COMMISSION", "CONSEIL DE LA RADIODIFFUSION ET DES TÉLÉCOMMUNICATIONS CANADIENNES"));
        institutionMappings.put("CSA", Arrays.asList("CSA", "ASC", "CANADIAN SPACE AGENCY", "AGENCE SPATIALE CANADIENNE", "CSA/ASC"));
        institutionMappings.put("CSEC", Arrays.asList("CSEC", "CSTC", "COMMUNICATIONS SECURITY ESTABLISHMENT CANADA", "CENTRE DE LA SÉCURITÉ DES TÉLÉCOMMUNICATIONS CANADA", "CSEC/CSTC"));
        institutionMappings.put("CSPS", Arrays.asList("CSPS", "EFPC", "CANADA SCHOOL OF PUBLIC SERVICE", "ÉCOLE DE LA FONCTION PUBLIQUE DU CANADA", "CSPS/EFPC"));
        institutionMappings.put("DFO", Arrays.asList("DFO", "MPO", "FISHERIES AND OCEANS CANADA", "PÊCHES ET OCÉANS CANADA", "DFO/MPO"));
        institutionMappings.put("DND", Arrays.asList("DND", "MDN", "NATIONAL DEFENCE", "DÉFENSE NATIONALE", "DND/MDN"));
        institutionMappings.put("ECCC", Arrays.asList("ECCC", "ENVIRONMENT AND CLIMATE CHANGE CANADA", "ENVIRONNEMENT ET CHANGEMENT CLIMATIQUE CANADA", "ECCC"));
        institutionMappings.put("ESDC", Arrays.asList("ESDC", "EDSC", "EMPLOYMENT AND SOCIAL DEVELOPMENT CANADA", "EMPLOI ET DÉVELOPPEMENT SOCIAL CANADA", "ESDC/EDSC", "EMPLOI ET DÉVÉLOPPEMENT SOCIALE CANADA"));
        institutionMappings.put("FCAC", Arrays.asList("FCAC", "ACFC", "FINANCIAL CONSUMER AGENCY OF CANADA", "AGENCE DE LA CONSOMMATION EN MATIÈRE FINANCIÈRE DU CANADA", "FCAC/ACFC"));
        institutionMappings.put("FIN", Arrays.asList("FIN", "FIN", "FINANCE CANADA", "MINISTÈRE DES FINANCES CANADA", "DEPARTMENT OF FINANCE CANADA", "GOVERNMENT OF CANADA, DEPARTMENT OF FINANCE", "MINISTÈRE DES FINANCES", "FIN"));
        institutionMappings.put("GAC", Arrays.asList("GAC", "AMC", "GLOBAL AFFAIRS CANADA", "AFFAIRES MONDIALES CANADA", "GAC/AMC"));
        institutionMappings.put("HC", Arrays.asList("HC", "SC", "HEALTH CANADA", "SANTÉ CANADA", "HC/SC"));
        institutionMappings.put("INFC", Arrays.asList("INFC", "INFC", "INFRASTRUCTURE CANADA", "INFRASTRUCTURE CANADA"));
        institutionMappings.put("IRCC", Arrays.asList("IRCC", "IRCC", "IMMIGRATION, REFUGEES AND CITIZENSHIP CANADA", "IMMIGRATION, RÉFUGIÉS ET CITOYENNETÉ CANADA"));
        institutionMappings.put("ISC", Arrays.asList("ISC", "SAC", "INDIGENOUS SERVICES CANADA", "SERVICES AUX AUTOCHTONES CANADA", "ISC/SAC"));
        institutionMappings.put("ISED", Arrays.asList("ISED", "ISDE", "INNOVATION, SCIENCE AND ECONOMIC DEVELOPMENT CANADA", "INNOVATION, SCIENCES ET DÉVELOPPEMENT ÉCONOMIQUE CANADA", "ISED/ISDE"));
        institutionMappings.put("JUS", Arrays.asList("JUS", "JUSTICE CANADA", "MINISTÈRE DE LA JUSTICE CANADA", "JUS"));
        institutionMappings.put("LAC", Arrays.asList("LAC", "BAC", "LIBRARY AND ARCHIVES CANADA", "BIBLIOTHÈQUE ET ARCHIVES CANADA", "LAC/BAC"));
        institutionMappings.put("NFB", Arrays.asList("NFB", "ONF", "NATIONAL FILM BOARD", "OFFICE NATIONAL DU FILM", "NFB/ONF"));
        institutionMappings.put("NRC", Arrays.asList("NRC", "CNRC", "NATIONAL RESEARCH COUNCIL", "CONSEIL NATIONAL DE RECHERCHES CANADA", "NRC/CNRC"));
        institutionMappings.put("NRCAN", Arrays.asList("NRCAN", "RNCAN", "NATURAL RESOURCES CANADA", "RESSOURCES NATURELLES CANADA", "NRCAN/RNCAN"));
        institutionMappings.put("OSB", Arrays.asList("OSB", "BSF", "SUPERINTENDENT OF BANKRUPTCY CANADA", "BUREAU DU SURINTENDANT DES FAILLITES CANADA", "OSB/BSF"));
        institutionMappings.put("PBC", Arrays.asList("PBC", "CLCC", "PAROLE BOARD OF CANADA", "COMMISSION DES LIBÉRATIONS CONDITIONNELLES DU CANADA", "PBC/CLCC"));
        institutionMappings.put("PC", Arrays.asList("PC", "PC", "PARCS CANADA", "PARKS CANADA"));
        institutionMappings.put("PCH", Arrays.asList("PCH", "PCH", "CANADIAN HERITAGE", "PATRIMOINE CANADIEN"));
        institutionMappings.put("PCO", Arrays.asList("PCO", "BCP", "PRIVY COUNCIL OFFICE", "BUREAU DU CONSEIL PRIVÉ", "PCO/BCP"));
        institutionMappings.put("PHAC", Arrays.asList("PHAC", "ASPC", "PUBLIC HEALTH AGENCY OF CANADA", "AGENCE DE LA SANTÉ PUBLIQUE DU CANADA", "PHAC/ASPC"));
        institutionMappings.put("PS", Arrays.asList("PS", "SP", "PUBLIC SAFETY CANADA", "SÉCURITÉ PUBLIQUE CANADA", "PS/SP"));
        institutionMappings.put("PSC", Arrays.asList("PSC", "CFP", "PUBLIC SERVICE COMMISSION OF CANADA", "COMMISSION DE LA FONCTION PUBLIQUE DU CANADA", "PSC/CFP"));
        institutionMappings.put("SSC", Arrays.asList("SSC", "PSC", "SHARED SERVICES CANADA", "SERVICES PARTAGÉS CANADA", "SSC/PSC"));
        institutionMappings.put("PSPC", Arrays.asList("PSPC", "SPAC", "PUBLIC SERVICES AND PROCUREMENT CANADA", "SERVICES PUBLICS ET APPROVISIONNEMENT CANADA", "GOUVERNEMENT DU CANADA, SERVICES PUBLICS ET APPROVISIONNEMENT CANADA", "GOVERNMENT OF CANADA, PUBLIC SERVICES AND PROCUREMENT CANADA", "PSPC/SPAC"));
        institutionMappings.put("RCMP", Arrays.asList("RCMP", "GRC", "ROYAL CANADIAN MOUNTED POLICE", "GENDARMERIE ROYALE DU CANADA", "RCMP/GRC"));
        institutionMappings.put("STATCAN", Arrays.asList("STATCAN", "STATISTIQUE CANADA"));
        institutionMappings.put("SC", Arrays.asList("SC", "SC", "SERVICE CANADA", "SERVICE CANADA", "SC/SC"));
        institutionMappings.put("TBS", Arrays.asList("TBS", "SCT", "TREASURY BOARD OF CANADA SECRETARIAT", "SECRÉTARIAT DU CONSEIL DU TRÉSOR DU CANADA", "TBS/SCT"));
        institutionMappings.put("TC", Arrays.asList("TC", "TC", "TRANSPORT CANADA", "TRANSPORTS CANADA"));
        institutionMappings.put("VAC", Arrays.asList("VAC", "ACC", "VETERANS AFFAIRS CANADA", "ANCIENS COMBATTANTS CANADA", "VAC/ACC"));
        institutionMappings.put("WAGE", Arrays.asList("WAGE", "FEGC", "WOMEN AND GENDER EQUALITY CANADA", "FEMMES ET ÉGALITÉ DES GENRES CANADA", "WAGE/FEGC"));
        institutionMappings.put("WD", Arrays.asList("WD", "DEO", "WESTERN ECONOMIC DIVERSIFICATION CANADA", "DIVERSIFICATION DE L’ÉCONOMIE DE L’OUEST CANADA", "WD/DEO"));
        institutionMappings.put("OMBDNDCAF", Arrays.asList("OMBDNDCAF", "OMBMDNFAC", "DND/CAF OMBUDSMAN", "OMBUDSMAN DU MDN/FAC", "OFFICE OF THE NATIONAL DEFENCE AND CANADIAN ARMED FORCES OMBUDSMAN", "BUREAU DE L'OMBUDSMAN DE LA DÉFENSE NATIONALE ET DES FORCES ARMÉES CANADIENNES", "OMBDNDCAF/OMBMDNFAC"));
        institutionMappings.put("CSE", Arrays.asList("CSE", "CST", "COMMUNICATIONS SECURITY ESTABLISHMENT", "CENTRE DE LA SÉCURITÉ DES TÉLÉCOMMUNICATIONS", "CSE/CST"));
        institutionMappings.put("IOGC", Arrays.asList("IOGC", "BPGI", "INDIAN OIL AND GAS CANADA", "BUREAU DU PÉTROLE ET DU GAZ DES INDIENS", "IOGC/BPGI"));
        institutionMappings.put("CANNOR", Arrays.asList("CANNOR", "RNCAN", "CANADIAN NORTHERN ECONOMIC DEVELOPMENT AGENCY", "AGENCE CANADIENNE DE DÉVELOPPEMENT ÉCONOMIQUE DU NORD", "CANNOR/RNCAN"));
        institutionMappings.put("SST", Arrays.asList("SST", "TSS", "SOCIAL SECURITY TRIBUNAL OF CANADA", "TRIBUNAL DE LA SÉCURITÉ SOCIALE DU CANADA", "SST/TSS"));
    }

    @RequestMapping(value = "/pageFeedback/totalCommentsCount")
    @ResponseBody
    public String totalCommentsCount() {
        return String.valueOf(totalComments);
    }
    @GetMapping(value = "/dashboard")
    public ModelAndView pageFeedback(HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView();
        String lang = (String) request.getSession().getAttribute("lang");

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

        mav.setViewName("pageFeedbackDashboard_" + lang);
        return mav;
    }
    @GetMapping(value = "/dashboardData")
    @ResponseBody
    public DataTablesOutput<Problem> list(@Valid DataTablesInput input, HttpServletRequest request) {
        String pageLang = (String) request.getSession().getAttribute("lang");
//        String language = request.getParameter("language"); // Existing language parameter handling
//        String department = request.getParameter("department"); // Retrieve the department parameter
//        String comments = request.getParameter("comments"); // Retrieve the comments filter parameter
//        String theme = request.getParameter("theme"); // Retrieve the theme filter parameter
//        String section = request.getParameter("section"); // Retrieve the section filter parameter
//        String url = request.getParameter("url"); // Retrieve the url filter parameter
//        String startDate = request.getParameter("startDate");
//        String endDate = request.getParameter("endDate");
//        String[] titles = request.getParameterValues("titles[]");

        Criteria criteria = Criteria.where("processed").is("true");

        input.setStart(0);
        input.setLength(-1);
        // Execute the query with the built criteria
        DataTablesOutput<Problem> results = problemRepository.findAll(input, criteria);

        // Map to store counts of URLs


        HashMap<String, Integer> urlCountMap = new HashMap<>();
        HashMap<String, List<String>> urlCountMap2 = new HashMap<>();

        for (Problem problem: results.getData()) {
            System.out.println(problem.getUrl());
            int count = urlCountMap.getOrDefault(problem.getUrl(), 0);
            urlCountMap.put(problem.getUrl(), count + 1);
            urlCountMap2.put(
                problem.getUrl(),
                Arrays.asList(
                        problem.getTitle(),
                        problem.getLanguage(),
                        problem.getInstitution(),
                        problem.getTheme(),
                        problem.getSection())
                );
        }
        HashMap<String, Integer> sortedUrlCountMap = sortByValue(urlCountMap, DESC);

        ArrayList<Problem> urlList = new ArrayList<>();
        int index = 0;
        totalComments = 0;
        for (String key : sortedUrlCountMap.keySet()) {
            totalComments += urlCountMap.get(key);
            results.getData().get(index).setUrl(key);
            results.getData().get(index).setUrlEntries(sortedUrlCountMap.get(key));
            results.getData().get(index).setTitle(urlCountMap2.get(key).get(0));
            results.getData().get(index).setLanguage(urlCountMap2.get(key).get(1));
            results.getData().get(index).setInstitution(urlCountMap2.get(key).get(2));
            results.getData().get(index).setTheme(urlCountMap2.get(key).get(3));
            results.getData().get(index).setSection(urlCountMap2.get(key).get(4));
            urlList.add(results.getData().get(index));
            index++;
        }

        results.setRecordsFiltered(sortedUrlCountMap.size());
        results.setData(urlList);

        setInstitution(results, pageLang);

        // Update institution names in the results based on the language
        // Return the updated results
        return results;
    }

    /*
    Build a Spring Boot endpoint that fetches "Problem" entities marked as "processed" from a MongoDB database. For each unique URL, count the occurrences, and collect additional information like title, language, institution, theme, and section. Sort these aggregated results by the count of occurrences in descending order. Finally, update the institution names based on the user's language preference and package the data in a format suitable for a DataTables frontend component, ensuring pagination and sorting are handled correctly.
     */
    private void setInstitution(DataTablesOutput<Problem> problems, String lang) {
        for (Problem problem : problems.getData()) {
            String currentInstitution = problem.getInstitution();
            for (Map.Entry<String, List<String>> entry : institutionMappings.entrySet()) {
                if (entry.getValue().contains(currentInstitution)) {
                    // Assuming the translated institution name is at index 1 for French and index 0 for other languages
                    problem.setInstitution(entry.getValue().get(lang.equalsIgnoreCase("fr") ? 1 : 0));
                    break; // Exit the loop once the institution is found and updated
                }
            }
        }
    }
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

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
