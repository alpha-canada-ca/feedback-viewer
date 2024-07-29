package ca.gc.tbs.controller;

import ca.gc.tbs.domain.TopTaskSurvey;
import ca.gc.tbs.domain.User;
import ca.gc.tbs.repository.TopTaskRepository;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class TopTaskController {

    private static final Logger LOG = LoggerFactory.getLogger(TopTaskController.class);
    @Autowired
    private TopTaskRepository topTaskRepository;
    private int totalDistinctTasks = 0;

    private int totalTaskCount = 0;
    @Autowired
    private UserService userService;

    @Autowired
    private ProblemDateService problemDateService;
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

    @RequestMapping(value = "/topTaskData")
    @ResponseBody
    public DataTablesOutput<TopTaskSurvey> list(@Valid DataTablesInput input, HttpServletRequest request) {
        String pageLang = (String) request.getSession().getAttribute("lang");
        String departmentFilterVal = request.getParameter("department");
        String themeFilterVal = request.getParameter("theme");
        String[] taskFilterVals = request.getParameterValues("tasks[]");
        String startDateVal = request.getParameter("startDate");
        String endDateVal = request.getParameter("endDate");
        boolean includeCommentsOnly = request.getParameter("includeCommentsOnly").equals("true");

        Criteria criteria = Criteria.where("processed").is("true");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startDateVal != null && endDateVal != null) {
            LocalDate start = LocalDate.parse(startDateVal, formatter);
            LocalDate end = LocalDate.parse(endDateVal, formatter);
            criteria.and("dateTime").gte(start.format(formatter)).lte(end.format(formatter));
        }

        if (themeFilterVal != null && !themeFilterVal.isEmpty()) {
            criteria.and("theme").regex(themeFilterVal, "i");
        }
        if (departmentFilterVal != null && !departmentFilterVal.isEmpty()) {
            criteria.and("dept").is(departmentFilterVal);
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
                    commentCriteriaWithTasks.add(new Criteria().andOperator(taskCriteria, new Criteria().orOperator(nonEmptyCriteria.toArray(new Criteria[0]))));
                }
                criteria.orOperator(commentCriteriaWithTasks.toArray(new Criteria[0]));
            } else {
                criteria.orOperator(nonEmptyCriteria.toArray(new Criteria[0]));
            }
        } else if (!combinedOrCriteria.isEmpty()) {
            criteria.orOperator(combinedOrCriteria.toArray(new Criteria[0]));
        }

        List<Map> distinctTaskCounts = topTaskRepository.findDistinctTaskCountsWithFilters(criteria);
        totalDistinctTasks = distinctTaskCounts.size();
        DataTablesOutput<TopTaskSurvey> results = topTaskRepository.findAll(input, criteria);

        totalTaskCount = (int) results.getRecordsFiltered();
        return results;
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
        String departmentsStr = "AAFC / AAC,ATSSC / SCDATA,CATSA / ACSTA,CFIA / ACIA,CIRNAC / RCAANC,NSERC / CRSNG,CBSA / ASFC,CCG / GCC,CGC / CCG,"
                + "CIHR / IRSC,CIPO / OPIC,CRA / ARC,CRTC / CRTC,CSA / ASC,CSEC / CSTC,CSPS / EFPC,DFO / MPO,DND / MDN,ECCC / ECCC,"
                + "ESDC / EDSC,FCAC / ACFC,FIN / FIN,GAC / AMC,HC / SC,INFC / INFC,IRCC / IRCC,ISC / SAC,ISED / ISDE,JUS / JUS,"
                + "LAC / BAC,NFB / ONF,NRC / CNRC,NRCan / RNCan,OSB / BSF,PBC / CLCC,PC / PC,PCH / PCH,PCO / BCP,PHAC / ASPC,"
                + "PS / SP,PSC / CFP,SSC / PSC,PSPC / SPAC,RCMP / GRC,StatCan / StatCan,TBS / SCT,TC / TC,VAC / ACC,WAGE / FEGC,WD / DEO";
        String[] departmentData = departmentsStr.split(",");

        return Arrays.stream(departmentData)
                .map(dept -> {
                    String[] parts = dept.split(" / ");
                    String value = dept; // EN / FR format
                    String display = lang != null && lang.equalsIgnoreCase("fr") ? parts[1] + " / " + parts[0] : dept; // FR / EN format for French, EN / FR for English

                    Map<String, String> departmentMap = new HashMap<>();
                    departmentMap.put("value", value);
                    departmentMap.put("display", display);
                    return departmentMap;
                })
                .collect(Collectors.toList());
    }

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JWTUtil jwtUtil;

    @GetMapping("/api/toptasks")
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
        query.fields().exclude("_id").exclude("processed").exclude("personalInfoProcessed").exclude("autoTagProcessed").exclude("_class");
        List<Document> documents = mongoTemplate.find(query, Document.class, "toptasksurvey");
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

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}