package ca.gc.tbs.controller;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.domain.User;
import ca.gc.tbs.repository.ProblemRepository;
import ca.gc.tbs.security.JWTUtil;
import ca.gc.tbs.service.ProblemDateService;
import ca.gc.tbs.service.UserService;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
    private UserService userService;

    private static final Map<String, List<String>> institutionMappings = new HashMap<>();

    static {
        institutionMappings.put("AAFC", Arrays.asList("AAFC", "AAC", "AGRICULTURE AND AGRI-FOOD CANADA", "AGRICULTURE ET AGROALIMENTAIRE CANADA", "AAFC / AAC"));
        institutionMappings.put("ACOA", Arrays.asList("ACOA", "APECA", "ATLANTIC CANADA OPPORTUNITIES AGENCY", "AGENCE DE PROMOTION ÉCONOMIQUE DU CANADA ATLANTIQUE", "ACOA / APECA"));
        institutionMappings.put("ATSSC", Arrays.asList("ATSSC", "SCDATA", "ADMINISTRATIVE TRIBUNALS SUPPORT SERVICE OF CANADA", "SERVICE CANADIEN D’APPUI AUX TRIBUNAUX ADMINISTRATIFS", "ATSSC / SCDATA"));
        institutionMappings.put("CANNOR", Arrays.asList("CANNOR", "RNCAN", "CANADIAN NORTHERN ECONOMIC DEVELOPMENT AGENCY", "AGENCE CANADIENNE DE DÉVELOPPEMENT ÉCONOMIQUE DU NORD", "CANNOR / RNCAN"));
        institutionMappings.put("CATSA", Arrays.asList("CATSA", "ACSTA", "CANADIAN AIR TRANSPORT SECURITY AUTHORITY", "ADMINISTRATION CANADIENNE DE LA SÛRETÉ DU TRANSPORT AÉRIEN", "CATSA / ACSTA"));
        institutionMappings.put("CBSA", Arrays.asList("CBSA", "ASFC", "CANADA BORDER SERVICES AGENCY", "AGENCE DES SERVICES FRONTALIERS DU CANADA", "CBSA / ASFC"));
        institutionMappings.put("CCG", Arrays.asList("CCG", "GCC", "CANADIAN COAST GUARD", "GARDE CÔTIÈRE CANADIENNE", "CCG / GCC"));
        institutionMappings.put("CER", Arrays.asList("CER", "REC", "CANADA ENERGY REGULATOR", "RÉGIE DE L'ÉNERGIE DU CANADA", "CER / REC"));
        institutionMappings.put("CFIA", Arrays.asList("CFIA", "ACIA", "CANADIAN FOOD INSPECTION AGENCY", "AGENCE CANADIENNE D’INSPECTION DES ALIMENTS", "CFIA / ACIA"));
        institutionMappings.put("CGC", Arrays.asList("CGC", "CCG", "CANADIAN GRAIN COMMISSION", "COMMISSION CANADIENNE DES GRAINS", "CGC / CCG"));
        institutionMappings.put("CIHR", Arrays.asList("CIHR", "IRSC", "CANADIAN INSTITUTES OF HEALTH RESEARCH", "INSTITUTS DE RECHERCHE EN SANTÉ DU CANADA", "CIHR / IRSC"));
        institutionMappings.put("CIPO", Arrays.asList("CIPO", "OPIC", "CANADIAN INTELLECTUAL PROPERTY OFFICE", "OFFICE DE LA PROPRIÉTÉ INTELLECTUELLE DU CANADA", "CIPO / OPIC"));
        institutionMappings.put("CIRNAC", Arrays.asList("CIRNAC", "RCAANC", "CROWN-INDIGENOUS RELATIONS AND NORTHERN AFFAIRS CANADA", "RELATIONS COURONNE-AUTOCHTONES ET AFFAIRES DU NORD CANADA", "CIRNAC / RCAANC"));
        institutionMappings.put("CRA", Arrays.asList("CRA", "ARC", "CANADA REVENUE AGENCY", "AGENCE DU REVENU DU CANADA", "CRA / ARC"));
        institutionMappings.put("CRTC", Arrays.asList("CRTC", "CRTC", "CANADIAN RADIO-TELEVISION AND TELECOMMUNICATIONS COMMISSION", "CONSEIL DE LA RADIODIFFUSION ET DES TÉLÉCOMMUNICATIONS CANADIENNES", "CRTC / CRTC"));
        institutionMappings.put("CSA", Arrays.asList("CSA", "ASC", "CANADIAN SPACE AGENCY", "AGENCE SPATIALE CANADIENNE", "CSA / ASC"));
        institutionMappings.put("CSC", Arrays.asList("CSC", "SCC", "CORRECTIONAL SERVICE CANADA", "SERVICE CORRECTIONNEL CANADA", "CSC / SCC"));
        institutionMappings.put("CSE", Arrays.asList("CSE", "CST", "COMMUNICATIONS SECURITY ESTABLISHMENT", "CENTRE DE LA SÉCURITÉ DES TÉLÉCOMMUNICATIONS", "CSE / CST"));
        institutionMappings.put("CSEC", Arrays.asList("CSEC", "CSTC", "COMMUNICATIONS SECURITY ESTABLISHMENT CANADA", "CENTRE DE LA SÉCURITÉ DES TÉLÉCOMMUNICATIONS CANADA", "CSEC / CSTC"));
        institutionMappings.put("CSPS", Arrays.asList("CSPS", "EFPC", "CANADA SCHOOL OF PUBLIC SERVICE", "ÉCOLE DE LA FONCTION PUBLIQUE DU CANADA", "CSPS / EFPC"));
        institutionMappings.put("DFO", Arrays.asList("DFO", "MPO", "FISHERIES AND OCEANS CANADA", "PÊCHES ET OCÉANS CANADA", "DFO / MPO"));
        institutionMappings.put("DND", Arrays.asList("DND", "MDN", "NATIONAL DEFENCE", "DÉFENSE NATIONALE", "DND / MDN"));
        institutionMappings.put("ECCC", Arrays.asList("ECCC", "ECCC", "ENVIRONMENT AND CLIMATE CHANGE CANADA", "ENVIRONNEMENT ET CHANGEMENT CLIMATIQUE CANADA", "ECCC / ECCC"));
        institutionMappings.put("ESDC", Arrays.asList("ESDC", "EDSC", "EMPLOYMENT AND SOCIAL DEVELOPMENT CANADA", "EMPLOI ET DÉVELOPPEMENT SOCIAL CANADA", "ESDC / EDSC"));
        institutionMappings.put("FCAC", Arrays.asList("FCAC", "ACFC", "FINANCIAL CONSUMER AGENCY OF CANADA", "AGENCE DE LA CONSOMMATION EN MATIÈRE FINANCIÈRE DU CANADA", "FCAC / ACFC"));
        institutionMappings.put("FIN", Arrays.asList("FIN", "FIN", "FINANCE CANADA", "MINISTÈRE DES FINANCES CANADA", "DEPARTMENT OF FINANCE CANADA", "GOVERNMENT OF CANADA, DEPARTMENT OF FINANCE", "MINISTÈRE DES FINANCES", "FIN / FIN"));
        institutionMappings.put("GAC", Arrays.asList("GAC", "AMC", "GLOBAL AFFAIRS CANADA", "AFFAIRES MONDIALES CANADA", "GAC / AMC"));
        institutionMappings.put("HC", Arrays.asList("HC", "SC", "HEALTH CANADA", "SANTÉ CANADA", "HC / SC"));
        institutionMappings.put("INFC", Arrays.asList("INFC", "INFC", "INFRASTRUCTURE CANADA", "INFRASTRUCTURE CANADA", "INFC / INFC"));
        institutionMappings.put("IOGC", Arrays.asList("IOGC", "BPGI", "INDIAN OIL AND GAS CANADA", "BUREAU DU PÉTROLE ET DU GAZ DES INDIENS", "IOGC / BPGI"));
        institutionMappings.put("IRCC", Arrays.asList("IRCC", "IRCC", "IMMIGRATION, REFUGEES AND CITIZENSHIP CANADA", "IMMIGRATION, RÉFUGIÉS ET CITOYENNETÉ CANADA", "IRCC / IRCC"));
        institutionMappings.put("ISC", Arrays.asList("ISC", "SAC", "INDIGENOUS SERVICES CANADA", "SERVICES AUX AUTOCHTONES CANADA", "ISC / SAC"));
        institutionMappings.put("ISED", Arrays.asList("ISED", "ISDE", "INNOVATION, SCIENCE AND ECONOMIC DEVELOPMENT CANADA", "INNOVATION, SCIENCES ET DÉVELOPPEMENT ÉCONOMIQUE CANADA", "ISED / ISDE"));
        institutionMappings.put("JUS", Arrays.asList("JUS", "JUS", "JUSTICE CANADA", "MINISTÈRE DE LA JUSTICE CANADA", "JUS / JUS"));
        institutionMappings.put("LAC", Arrays.asList("LAC", "BAC", "LIBRARY AND ARCHIVES CANADA", "BIBLIOTHÈQUE ET ARCHIVES CANADA", "LAC / BAC"));
        institutionMappings.put("NFB", Arrays.asList("NFB", "ONF", "NATIONAL FILM BOARD", "OFFICE NATIONAL DU FILM", "NFB / ONF"));
        institutionMappings.put("NRC", Arrays.asList("NRC", "CNRC", "NATIONAL RESEARCH COUNCIL", "CONSEIL NATIONAL DE RECHERCHES CANADA", "NRC / CNRC"));
        institutionMappings.put("NRCAN", Arrays.asList("NRCAN", "RNCAN", "NATURAL RESOURCES CANADA", "RESSOURCES NATURELLES CANADA", "NRCAN / RNCAN"));
        institutionMappings.put("NSERC", Arrays.asList("NSERC", "CRSNG", "NATURAL SCIENCES AND ENGINEERING RESEARCH CANADA", "CONSEIL DE RECHERCHES EN SCIENCES NATURELLES ET EN GÉNIE DU CANADA", "NSERC / CRSNG"));
        institutionMappings.put("OMBDNDCAF", Arrays.asList("OMBDNDCAF", "OMBMDNFAC", "DND / CAF OMBUDSMAN", "OMBUDSMAN DU MDN / FAC", "OFFICE OF THE NATIONAL DEFENCE AND CANADIAN ARMED FORCES OMBUDSMAN", "BUREAU DE L'OMBUDSMAN DE LA DÉFENSE NATIONALE ET DES FORCES ARMÉES CANADIENNES", "OMBDNDCAF / OMBMDNFAC"));
        institutionMappings.put("OSB", Arrays.asList("OSB", "BSF", "SUPERINTENDENT OF BANKRUPTCY CANADA", "BUREAU DU SURINTENDANT DES FAILLITES CANADA", "OSB / BSF"));
        institutionMappings.put("PBC", Arrays.asList("PBC", "CLCC", "PAROLE BOARD OF CANADA", "COMMISSION DES LIBÉRATIONS CONDITIONNELLES DU CANADA", "PBC / CLCC"));
        institutionMappings.put("PC", Arrays.asList("PC", "PC", "PARCS CANADA", "PARKS CANADA", "PC / PC"));
        institutionMappings.put("PCH", Arrays.asList("PCH", "PCH", "CANADIAN HERITAGE", "PATRIMOINE CANADIEN", "PCH / PCH"));
        institutionMappings.put("PCO", Arrays.asList("PCO", "BCP", "PRIVY COUNCIL OFFICE", "BUREAU DU CONSEIL PRIVÉ", "PCO / BCP"));
        institutionMappings.put("PHAC", Arrays.asList("PHAC", "ASPC", "PUBLIC HEALTH AGENCY OF CANADA", "AGENCE DE LA SANTÉ PUBLIQUE DU CANADA", "PHAC / ASPC"));
        institutionMappings.put("PS", Arrays.asList("PS", "SP", "PUBLIC SAFETY CANADA", "SÉCURITÉ PUBLIQUE CANADA", "PS / SP"));
        institutionMappings.put("PSC", Arrays.asList("PSC", "CFP", "PUBLIC SERVICE COMMISSION OF CANADA", "COMMISSION DE LA FONCTION PUBLIQUE DU CANADA", "PSC / CFP"));
        institutionMappings.put("PSPC", Arrays.asList("PSPC", "SPAC", "PUBLIC SERVICES AND PROCUREMENT CANADA", "SERVICES PUBLICS ET APPROVISIONNEMENT CANADA", "GOUVERNEMENT DU CANADA, SERVICES PUBLICS ET APPROVISIONNEMENT CANADA", "GOVERNMENT OF CANADA, PUBLIC SERVICES AND PROCUREMENT CANADA", "PSPC / SPAC"));
        institutionMappings.put("RCMP", Arrays.asList("RCMP", "GRC", "ROYAL CANADIAN MOUNTED POLICE", "GENDARMERIE ROYALE DU CANADA", "RCMP / GRC"));
        institutionMappings.put("SC", Arrays.asList("SC", "SC", "SERVICE CANADA", "SERVICE CANADA", "SC / SC"));
        institutionMappings.put("SSC", Arrays.asList("SSC", "PSC", "SHARED SERVICES CANADA", "SERVICES PARTAGÉS CANADA", "SSC / PSC"));
        institutionMappings.put("SSHRC", Arrays.asList("SSHRC", "CRSH", "SOCIAL SCIENCES AND HUMANITIES RESEARCH COUNCIL", "CONSEIL DE RECHERCHES EN SCIENCES HUMAINES", "SSHRC / CRSH"));
        institutionMappings.put("SST", Arrays.asList("SST", "TSS", "SOCIAL SECURITY TRIBUNAL OF CANADA", "TRIBUNAL DE LA SÉCURITÉ SOCIALE DU CANADA", "SST / TSS"));
        institutionMappings.put("STATCAN", Arrays.asList("STATCAN", "STATCAN", "STATISTICS CANADA", "STATISTIQUE CANADA", "STATCAN / STATCAN"));
        institutionMappings.put("TBS", Arrays.asList("TBS", "SCT", "TREASURY BOARD OF CANADA SECRETARIAT", "SECRÉTARIAT DU CONSEIL DU TRÉSOR DU CANADA", "TBS / SCT"));
        institutionMappings.put("TC", Arrays.asList("TC", "TC", "TRANSPORT CANADA", "TRANSPORTS CANADA", "TC / TC"));
        institutionMappings.put("VAC", Arrays.asList("VAC", "ACC", "VETERANS AFFAIRS CANADA", "ANCIENS COMBATTANTS CANADA", "VAC / ACC"));
        institutionMappings.put("WAGE", Arrays.asList("WAGE", "FEGC", "WOMEN AND GENDER EQUALITY CANADA", "FEMMES ET ÉGALITÉ DES GENRES CANADA", "WAGE / FEGC"));
        institutionMappings.put("WD", Arrays.asList("WD", "DEO", "WESTERN ECONOMIC DIVERSIFICATION CANADA", "DIVERSIFICATION DE L’ÉCONOMIE DE L’OUEST CANADA", "WD / DEO"));
    }


    @GetMapping("/pageTitles")
    @ResponseBody
    public List<String> getPageTitles(@RequestParam(name = "search", required = false) String search) {
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
            @RequestHeader(name = "Authorization") String authorizationHeader
    ) {
        String token = null;
        String userName = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            userName = jwtUtil.extractUsername(token);
        }

        if (userName != null) {
            User user = userService.findUserByEmail(userName);
            if (!userService.isAdmin(user) && !userService.isAPI(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Only API users & Admins can access this endpoint.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing or invalid.");
        }

        Set<String> validParams = new HashSet<>(Arrays.asList(
                "startDate", "endDate", "processedStartDate", "processedEndDate", "institution", "url", "authorizationHeader"
        ));

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
        if ((startDate != null || endDate != null) && (processedStartDate != null || processedEndDate != null)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "You can only filter by normal date range or processed date range, not both.");
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
                        errorResponse.put("error", "processedEndDate must be greater than or equal to processedStartDate.");
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
        query.fields().exclude("_id")
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
        Set<String> matchingVariations = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : institutionMappings.entrySet()) {
            if (entry.getValue().stream().anyMatch(variation -> variation.equalsIgnoreCase(department))) {
                matchingVariations.addAll(entry.getValue());
            }
        }

        if (matchingVariations.isEmpty()) {
            throw new IllegalArgumentException("Couldn't find department name: " + department);
        }

        criteria.and("institution").in(matchingVariations);
        return criteria;
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
            criteria.and("section").is(section);
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
            criteria.orOperator(titleCriterias.toArray(new Criteria[0]));
            System.out.println("Titles received: " + Arrays.toString(titles));
        }
        // URL filtering
        if (url != null && !url.isEmpty()) {
            criteria.and("url").regex(url, "i"); // 'i' for case-insensitive matching
        }
        // Department filtering based on institutionMappings
        if (department != null && !department.isEmpty()) {
            Set<String> matchingVariations = new HashSet<>();
            // Filter variations based on department:
            for (Map.Entry<String, List<String>> entry : institutionMappings.entrySet()) {
                if (entry.getValue().stream().anyMatch(variation -> variation.equalsIgnoreCase(department))) {
                    matchingVariations.addAll(entry.getValue());
                }
            }
            if (!matchingVariations.isEmpty()) {
                criteria.and("institution").in(matchingVariations);
            }
        }
        // Comments filtering
        if (comments != null && !comments.isEmpty()) {
            String safeComments = escapeSpecialRegexCharacters(comments);
            criteria.and("problemDetails").regex(safeComments, "i"); // 'i' for case-insensitive matching
        }
        // Execute the query with the built criteria
        DataTablesOutput<Problem> results = problemRepository.findAll(input, criteria);
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
            for (Map.Entry<String, List<String>> entry : institutionMappings.entrySet()) {
                if (entry.getValue().contains(currentInstitution)) {
                    // Assuming the translated institution name is at index 1 for French and index 0 for other languages
                    problem.setInstitution(entry.getValue().get(lang.equalsIgnoreCase("fr") ? 1 : 0));
                    break; // Exit the loop once the institution is found and updated
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
