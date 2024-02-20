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
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
            /* ENGLISH, FRENCH */
            {"The answer I need is missing", "La réponse dont j’ai besoin n’est pas là"},
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

    @GetMapping("/institutionMappings")
    @ResponseBody
    public Map<String, List<String>> getInstitutionMappings() {
        return institutionMappings;
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
        String lang = (String) request.getSession().getAttribute("lang");

        // Fetch the aggregation results
        AggregationResults<Map> results = problemRepository.findEarliestAndLatestProblemDate();
        Map<String, String> dateMap = results.getUniqueMappedResult();

        if (dateMap != null) {
            mav.addObject("earliestDate", dateMap.get("earliestDate"));
            mav.addObject("latestDate", dateMap.get("latestDate"));
        } else {
            // Handle the case where no dates are returned
            mav.addObject("earliestDate", "N/A");
            mav.addObject("latestDate", "N/A");
        }

        mav.setViewName("pageFeedback_" + lang);
        return mav;
    }

    @GetMapping(value = "/feedbackData")
    @ResponseBody
    public DataTablesOutput<Problem> list(@Valid DataTablesInput input, HttpServletRequest request) {
        String language = request.getParameter("language"); // Existing language parameter handling
        String departmentKey = request.getParameter("department"); // Retrieve the department parameter
        String comments = request.getParameter("comments"); // Retrieve the comments filter parameter
        String url = request.getParameter("url"); // Retrieve the url filter parameter
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");


        Criteria criteria = Criteria.where("processed").is("true");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            criteria.and("problemDate").gte(start.format(formatter)).lte(end.format(formatter));
        }

        // Language filtering (existing logic)
        if (language != null && !language.isEmpty()) {
            criteria.and("language").is(language);
        }

        // URL filtering
        if (url != null && !url.isEmpty()) {
            criteria.and("url").regex(url, "i"); // 'i' for case-insensitive matching
        }

        // Department filtering based on institutionMappings
        if (departmentKey != null && !departmentKey.isEmpty()) {
            List<String> departmentVariations = institutionMappings.get(departmentKey); // Get variations for the department key
            if (departmentVariations != null && !departmentVariations.isEmpty()) {
                criteria.and("institution").in(departmentVariations); // Use variations in the query
            }
        }

        // Comments filtering
        if (comments != null && !comments.isEmpty()) {
            // Assuming 'problemDetails' field contains the comments
            criteria.and("problemDetails").regex(comments, "i"); // 'i' for case-insensitive matching
        }

        // Execute the query with the built criteria
        DataTablesOutput<Problem> results = problemRepository.findAll(input, criteria);

        // Update institution names in the results based on the language
        setInstitution(results, language);

        // Return the updated results
        return results;
    }


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


    private Criteria buildDateCriteria(String dateSearchVal, SimpleDateFormat simpleDateFormat) {
        if (dateSearchVal.contains(":")) {
            return buildDateRangeCriteria(dateSearchVal);
        } else {
            return buildSingleDateCriteria(dateSearchVal, simpleDateFormat);
        }
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

    private Criteria buildInstitutionCriteria(String instSearchVal) {
        if (instSearchVal.contains("|")) {

            String[] ret = instSearchVal.split("\\|");


            //build a criteria for each value
            Criteria[] criteria = new Criteria[ret.length];
            for (int i = 0; i < ret.length; i++) {
                criteria[i] = where("institution").is(ret[i]);
            }

            //return the criteria
            return new Criteria().orOperator(criteria);
        }
        return null;

    }

    private Criteria buildSingleDateCriteria(String dateSearchVal, SimpleDateFormat simpleDateFormat) {
        String startDate = null;
        String endDate = null;

        if (dateSearchVal.contains("today")) {
            startDate = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        } else if (dateSearchVal.contains("yesterday")) {
            startDate = simpleDateFormat.format(new Date(System.currentTimeMillis() - DAY_IN_MS));
            endDate = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        } else if (dateSearchVal.contains("seven")) {
            startDate = simpleDateFormat.format(new Date(System.currentTimeMillis() - (7 * DAY_IN_MS)));
        } else if (dateSearchVal.contains("fifteen")) {
            startDate = simpleDateFormat.format(new Date(System.currentTimeMillis() - (15 * DAY_IN_MS)));
        } else if (dateSearchVal.contains("thirty")) {
            startDate = simpleDateFormat.format(new Date(System.currentTimeMillis() - (30 * DAY_IN_MS)));
        }

        if (startDate != null) {
            if (endDate != null) {
                return where("problemDate").gte(startDate).lt(endDate);
            } else {
                return where("problemDate").gte(startDate);
            }
        }
        return null;
    }


    private boolean containsTilde(String... searchValues) {
        return Arrays.stream(searchValues).anyMatch(searchVal -> searchVal.contains("~"));
    }

    private void updateInputSearchValuesWithTilde(DataTablesInput input, String searchVal, String columnName) {
        if (!searchVal.isEmpty()) {
            String updatedValue = searchVal.substring(0, searchVal.length() - 2);
            input.getColumn(columnName).get().getSearch().setValue(updatedValue);
        }
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
