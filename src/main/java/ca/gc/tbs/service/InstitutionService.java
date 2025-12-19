package ca.gc.tbs.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class InstitutionService {

    private final Map<String, List<String>> institutionMappings = new HashMap<>();
    private final Map<String, List<String>> sectionMappings = new HashMap<>();
    private final Map<String, String> urlToInstitutionKey = new LinkedHashMap<>();

    public InstitutionService() {
        initializeInstitutionMappings();
        initializeSectionMappings();
        initializeUrlMappings();
    }

    private void initializeSectionMappings() {
        sectionMappings.put("disability", Arrays.asList("disability", "disability benefits"));
        sectionMappings.put("news", Arrays.asList("news"));
    }

    private void initializeUrlMappings() {
        urlToInstitutionKey.put("ircc.canada.ca", "IRCC");
        urlToInstitutionKey.put("immigration-refugees-citizenship", "IRCC");
        urlToInstitutionKey.put("immigration-refugies-citoyennete", "IRCC");
        urlToInstitutionKey.put("revenue-agency", "CRA");
        urlToInstitutionKey.put("agence-revenu", "CRA");
        urlToInstitutionKey.put("treasury-board-secretariat", "TBS");
        urlToInstitutionKey.put("secretariat-conseil-tresor", "TBS");
        urlToInstitutionKey.put("public-health", "PHAC");
        urlToInstitutionKey.put("sante-publique", "PHAC");
        urlToInstitutionKey.put("health-canada", "HC");
        urlToInstitutionKey.put("sante-canada", "HC");
        urlToInstitutionKey.put("employment-social-development", "ESDC");
        urlToInstitutionKey.put("emploi-developpement-social", "ESDC");
        urlToInstitutionKey.put("environment-climate-change", "ECCC");
        urlToInstitutionKey.put("environnement-changement-climatique", "ECCC");
        urlToInstitutionKey.put("department-national-defence", "DND");
        urlToInstitutionKey.put("ministere-defense-nationale", "DND");
        urlToInstitutionKey.put("canadian-heritage", "PCH");
        urlToInstitutionKey.put("patrimoine-canadien", "PCH");
        urlToInstitutionKey.put("department-finance", "FIN");
        urlToInstitutionKey.put("budget.canada.ca", "FIN");
        urlToInstitutionKey.put("1-800-o-canada", "SC");
        urlToInstitutionKey.put("service-canada", "SC");
        urlToInstitutionKey.put("infrastructure-canada", "INFC");
        urlToInstitutionKey.put("global-affairs", "GAC");
        urlToInstitutionKey.put("affaires-mondiales", "GAC");
        urlToInstitutionKey.put("justice.gc.ca", "JUS");
        urlToInstitutionKey.put("veterans.gc.ca", "VAC");
        urlToInstitutionKey.put("veterans-affairs", "VAC");
        urlToInstitutionKey.put("anciens-combattants", "VAC");
        urlToInstitutionKey.put("statcan.gc.ca", "STATCAN");
        urlToInstitutionKey.put("tc.gc.ca", "TC");
        urlToInstitutionKey.put("transport-canada", "TC");
        urlToInstitutionKey.put("rcmp-grc.gc.ca", "RCMP");
    }

    private void initializeInstitutionMappings() {
        institutionMappings.put("AAFC", Arrays.asList("AAFC", "AAC", "AGRICULTURE AND AGRI-FOOD CANADA", "AGRICULTURE ET AGROALIMENTAIRE CANADA", "AAFC / AAC"));
        institutionMappings.put("ACOA", Arrays.asList("ACOA", "APECA", "ATLANTIC CANADA OPPORTUNITIES AGENCY", "AGENCE DE PROMOTION ÉCONOMIQUE DU CANADA ATLANTIQUE", "ACOA / APECA"));
        institutionMappings.put("ATSSC", Arrays.asList("ATSSC", "SCDATA", "ADMINISTRATIVE TRIBUNALS SUPPORT SERVICE OF CANADA", "SERVICE CANADIEN D'APPUI AUX TRIBUNAUX ADMINISTRATIFS", "ATSSC / SCDATA"));
        institutionMappings.put("CANNOR", Arrays.asList("CANNOR", "RNCAN", "CANADIAN NORTHERN ECONOMIC DEVELOPMENT AGENCY", "AGENCE CANADIENNE DE DÉVELOPPEMENT ÉCONOMIQUE DU NORD", "CANNOR / RNCAN"));
        institutionMappings.put("CATSA", Arrays.asList("CATSA", "ACSTA", "CANADIAN AIR TRANSPORT SECURITY AUTHORITY", "ADMINISTRATION CANADIENNE DE LA SÛRETÉ DU TRANSPORT AÉRIEN", "CATSA / ACSTA"));
        institutionMappings.put("CBSA", Arrays.asList("CBSA", "ASFC", "CANADA BORDER SERVICES AGENCY", "AGENCE DES SERVICES FRONTALIERS DU CANADA", "CBSA / ASFC"));
        institutionMappings.put("CCG", Arrays.asList("CCG", "GCC", "CANADIAN COAST GUARD", "GARDE CÔTIÈRE CANADIENNE", "CCG / GCC"));
        institutionMappings.put("CER", Arrays.asList("CER", "REC", "CANADA ENERGY REGULATOR", "RÉGIE DE L'ÉNERGIE DU CANADA", "CER / REC"));
        institutionMappings.put("CFIA", Arrays.asList("CFIA", "ACIA", "CANADIAN FOOD INSPECTION AGENCY", "AGENCE CANADIENNE D'INSPECTION DES ALIMENTS", "CFIA / ACIA"));
        institutionMappings.put("CGC", Arrays.asList("CGC", "CANADIAN GRAIN COMMISSION", "COMMISSION CANADIENNE DES GRAINS", "CGC"));
        institutionMappings.put("CIHR", Arrays.asList("CIHR", "IRSC", "CANADIAN INSTITUTES OF HEALTH RESEARCH", "INSTITUTS DE RECHERCHE EN SANTÉ DU CANADA", "CIHR / IRSC"));
        institutionMappings.put("CIPO", Arrays.asList("CIPO", "OPIC", "CANADIAN INTELLECTUAL PROPERTY OFFICE", "OFFICE DE LA PROPRIÉTÉ INTELLECTUELLE DU CANADA", "CIPO / OPIC"));
        institutionMappings.put("CIRNAC", Arrays.asList("CIRNAC", "RCAANC", "CROWN-INDIGENOUS RELATIONS AND NORTHERN AFFAIRS CANADA", "RELATIONS COURONNE-AUTOCHTONES ET AFFAIRES DU NORD CANADA", "CIRNAC / RCAANC"));
        institutionMappings.put("CRA", Arrays.asList("CRA", "ARC", "CANADA REVENUE AGENCY", "AGENCE DU REVENU DU CANADA", "CRA / ARC"));
        institutionMappings.put("CRTC", Arrays.asList("CRTC", "CANADIAN RADIO-TELEVISION AND TELECOMMUNICATIONS COMMISSION", "CONSEIL DE LA RADIODIFFUSION ET DES TÉLÉCOMMUNICATIONS CANADIENNES", "CRTC / CRTC"));
        institutionMappings.put("CSA", Arrays.asList("CSA", "ASC", "CANADIAN SPACE AGENCY", "AGENCE SPATIALE CANADIENNE", "CSA / ASC"));
        institutionMappings.put("CSC", Arrays.asList("CSC", "SCC", "CORRECTIONAL SERVICE CANADA", "SERVICE CORRECTIONNEL CANADA", "CSC / SCC"));
        institutionMappings.put("CSE", Arrays.asList("CSE", "CST", "COMMUNICATIONS SECURITY ESTABLISHMENT", "CENTRE DE LA SÉCURITÉ DES TÉLÉCOMMUNICATIONS", "CSE / CST"));
        institutionMappings.put("CSEC", Arrays.asList("CSEC", "CSTC", "COMMUNICATIONS SECURITY ESTABLISHMENT CANADA", "CENTRE DE LA SÉCURITÉ DES TÉLÉCOMMUNICATIONS CANADA", "CSEC / CSTC"));
        institutionMappings.put("CSPS", Arrays.asList("CSPS", "EFPC", "CANADA SCHOOL OF PUBLIC SERVICE", "ÉCOLE DE LA FONCTION PUBLIQUE DU CANADA", "CSPS / EFPC"));
        institutionMappings.put("DFO", Arrays.asList("DFO", "MPO", "FISHERIES AND OCEANS CANADA", "PÊCHES ET OCÉANS CANADA", "DFO / MPO", "GOVERNMENT OF CANADA, FISHERIES AND OCEANS CANADA, COMMUNICATIONS BRANCH"));
        institutionMappings.put("DND", Arrays.asList("DND", "MDN", "NATIONAL DEFENCE", "DÉFENSE NATIONALE", "DND / MDN"));
        institutionMappings.put("ECCC", Arrays.asList("ECCC", "ENVIRONMENT AND CLIMATE CHANGE CANADA", "ENVIRONNEMENT ET CHANGEMENT CLIMATIQUE CANADA", "ECCC / ECCC"));
        institutionMappings.put("ESDC", Arrays.asList("ESDC", "EDSC", "EMPLOYMENT AND SOCIAL DEVELOPMENT CANADA", "EMPLOI ET DÉVELOPPEMENT SOCIAL CANADA", "ESDC / EDSC"));
        institutionMappings.put("FCAC", Arrays.asList("FCAC", "ACFC", "FINANCIAL CONSUMER AGENCY OF CANADA", "AGENCE DE LA CONSOMMATION EN MATIÈRE FINANCIÈRE DU CANADA", "FCAC / ACFC"));
        institutionMappings.put("FIN", Arrays.asList("FIN", "FINANCE CANADA", "MINISTÈRE DES FINANCES CANADA", "DEPARTMENT OF FINANCE CANADA", "GOVERNMENT OF CANADA, DEPARTMENT OF FINANCE", "MINISTÈRE DES FINANCES", "FIN / FIN"));
        institutionMappings.put("GAC", Arrays.asList("GAC", "AMC", "GLOBAL AFFAIRS CANADA", "AFFAIRES MONDIALES CANADA", "GAC / AMC"));
        institutionMappings.put("HC", Arrays.asList("HC", "SC", "HEALTH CANADA", "SANTÉ CANADA", "HC / SC"));
        institutionMappings.put("HICC", Arrays.asList("HICC", "LICC", "HOUSING, INFRASTRUCTURE AND COMMUNITIES CANADA", "LOGEMENT, INFRASTRUCTURES ET COLLECTIVITÉS CANADA", "HICC / LICC"));
        institutionMappings.put("INFC", Arrays.asList("INFC", "INFRASTRUCTURE CANADA", "INFC / INFC"));
        institutionMappings.put("IOGC", Arrays.asList("IOGC", "BPGI", "INDIAN OIL AND GAS CANADA", "BUREAU DU PÉTROLE ET DU GAZ DES INDIENS", "IOGC / BPGI"));
        institutionMappings.put("IRCC", Arrays.asList("IRCC", "IMMIGRATION, REFUGEES AND CITIZENSHIP CANADA", "IMMIGRATION, RÉFUGIÉS ET CITOYENNETÉ CANADA", "IRCC / IRCC"));
        institutionMappings.put("ISC", Arrays.asList("ISC", "SAC", "INDIGENOUS SERVICES CANADA", "SERVICES AUX AUTOCHTONES CANADA", "ISC / SAC"));
        institutionMappings.put("ISED", Arrays.asList("ISED", "ISDE", "INNOVATION, SCIENCE AND ECONOMIC DEVELOPMENT CANADA", "INNOVATION, SCIENCES ET DÉVELOPPEMENT ÉCONOMIQUE CANADA", "ISED / ISDE"));
        institutionMappings.put("JUS", Arrays.asList("JUS", "JUSTICE CANADA", "MINISTÈRE DE LA JUSTICE CANADA", "JUS / JUS"));
        institutionMappings.put("LAC", Arrays.asList("LAC", "BAC", "LIBRARY AND ARCHIVES CANADA", "BIBLIOTHÈQUE ET ARCHIVES CANADA", "LAC / BAC"));
        institutionMappings.put("NFB", Arrays.asList("NFB", "ONF", "NATIONAL FILM BOARD", "OFFICE NATIONAL DU FILM", "NFB / ONF"));
        institutionMappings.put("NRC", Arrays.asList("NRC", "CNRC", "NATIONAL RESEARCH COUNCIL", "CONSEIL NATIONAL DE RECHERCHES CANADA", "NRC / CNRC"));
        institutionMappings.put("NRCAN", Arrays.asList("NRCAN", "RNCAN", "NATURAL RESOURCES CANADA", "RESSOURCES NATURELLES CANADA", "NRCAN / RNCAN"));
        institutionMappings.put("NSERC", Arrays.asList("NSERC", "CRSNG", "NATURAL SCIENCES AND ENGINEERING RESEARCH CANADA", "CONSEIL DE RECHERCHES EN SCIENCES NATURELLES ET EN GÉNIE DU CANADA", "NSERC / CRSNG"));
        institutionMappings.put("OMBDNDCAF", Arrays.asList("OMBDNDCAF", "OMBMDNFAC", "DND / CAF OMBUDSMAN", "OMBUDSMAN DU MDN / FAC", "OFFICE OF THE NATIONAL DEFENCE AND CANADIAN ARMED FORCES OMBUDSMAN", "BUREAU DE L'OMBUDSMAN DE LA DÉFENSE NATIONALE ET DES FORCES ARMÉES CANADIENNES", "OMBDNDCAF / OMBMDNFAC"));
        institutionMappings.put("OSB", Arrays.asList("OSB", "BSF", "SUPERINTENDENT OF BANKRUPTCY CANADA", "BUREAU DU SURINTENDANT DES FAILLITES CANADA", "OSB / BSF"));
        institutionMappings.put("PBC", Arrays.asList("PBC", "CLCC", "PAROLE BOARD OF CANADA", "COMMISSION DES LIBÉRATIONS CONDITIONNELLES DU CANADA", "PBC / CLCC"));
        institutionMappings.put("PC", Arrays.asList("PC", "PARCS CANADA", "PARKS CANADA", "PC / PC"));
        institutionMappings.put("PCH", Arrays.asList("PCH", "CANADIAN HERITAGE", "PATRIMOINE CANADIEN", "PCH / PCH"));
        institutionMappings.put("PCO", Arrays.asList("PCO", "BCP", "PRIVY COUNCIL OFFICE", "BUREAU DU CONSEIL PRIVÉ", "PCO / BCP"));
        institutionMappings.put("PHAC", Arrays.asList("PHAC", "ASPC", "PUBLIC HEALTH AGENCY OF CANADA", "AGENCE DE LA SANTÉ PUBLIQUE DU CANADA", "PHAC / ASPC"));
        institutionMappings.put("PS", Arrays.asList("PS", "SP", "PUBLIC SAFETY CANADA", "SÉCURITÉ PUBLIQUE CANADA", "PS / SP"));
        institutionMappings.put("PSC", Arrays.asList("PSC", "CFP", "PUBLIC SERVICE COMMISSION OF CANADA", "COMMISSION DE LA FONCTION PUBLIQUE DU CANADA", "PSC / CFP"));
        institutionMappings.put("PSPC", Arrays.asList("PSPC", "SPAC", "PUBLIC SERVICES AND PROCUREMENT CANADA", "SERVICES PUBLICS ET APPROVISIONNEMENT CANADA", "GOUVERNEMENT DU CANADA, SERVICES PUBLICS ET APPROVISIONNEMENT CANADA", "GOVERNMENT OF CANADA, PUBLIC SERVICES AND PROCUREMENT CANADA", "PSPC / SPAC"));
        institutionMappings.put("RCMP", Arrays.asList("RCMP", "GRC", "ROYAL CANADIAN MOUNTED POLICE", "GENDARMERIE ROYALE DU CANADA", "RCMP / GRC"));
        institutionMappings.put("SC", Arrays.asList("SC", "SERVICE CANADA", "SC / SC"));
        institutionMappings.put("SSC", Arrays.asList("SSC", "PSC", "SHARED SERVICES CANADA", "SERVICES PARTAGÉS CANADA", "SSC / PSC"));
        institutionMappings.put("SSHRC", Arrays.asList("SSHRC", "CRSH", "SOCIAL SCIENCES AND HUMANITIES RESEARCH COUNCIL", "CONSEIL DE RECHERCHES EN SCIENCES HUMAINES", "SSHRC / CRSH"));
        institutionMappings.put("SST", Arrays.asList("SST", "TSS", "SOCIAL SECURITY TRIBUNAL OF CANADA", "TRIBUNAL DE LA SÉCURITÉ SOCIALE DU CANADA", "SST / TSS"));
        institutionMappings.put("STATCAN", Arrays.asList("STATCAN", "STATISTICS CANADA", "STATISTIQUE CANADA", "STATCAN / STATCAN"));
        institutionMappings.put("TBS", Arrays.asList("TBS", "SCT", "TREASURY BOARD OF CANADA SECRETARIAT", "SECRÉTARIAT DU CONSEIL DU TRÉSOR DU CANADA", "TBS / SCT"));
        institutionMappings.put("TC", Arrays.asList("TC", "TRANSPORT CANADA", "TRANSPORTS CANADA", "TC / TC"));
        institutionMappings.put("VAC", Arrays.asList("VAC", "ACC", "VETERANS AFFAIRS CANADA", "ANCIENS COMBATTANTS CANADA", "VAC / ACC"));
        institutionMappings.put("WAGE", Arrays.asList("WAGE", "FEGC", "WOMEN AND GENDER EQUALITY CANADA", "FEMMES ET ÉGALITÉ DES GENRES CANADA", "WAGE / FEGC"));
        institutionMappings.put("WD", Arrays.asList("WD", "DEO", "WESTERN ECONOMIC DIVERSIFICATION CANADA", "DIVERSIFICATION DE L'ÉCONOMIE DE L'OUEST CANADA", "WD / DEO"));
    }

    public Map<String, List<String>> getInstitutionMappings() {
        return institutionMappings;
    }

    public Map<String, List<String>> getSectionMappings() {
        return sectionMappings;
    }

    public Set<String> getMatchingVariations(String department) {
        Set<String> matchingVariations = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : institutionMappings.entrySet()) {
            if (entry.getValue().stream().anyMatch(variation -> variation.equalsIgnoreCase(department))) {
                matchingVariations.addAll(entry.getValue());
            }
        }
        return matchingVariations;
    }

    public String getTranslatedInstitution(String currentInstitution, String lang) {
        for (Map.Entry<String, List<String>> entry : institutionMappings.entrySet()) {
            if (entry.getValue().contains(currentInstitution)) {
                return entry.getValue().get(lang.equalsIgnoreCase("fr") ? 1 : 0);
            }
        }
        return currentInstitution;
    }

    public List<String> getSectionVariations(String section) {
        return sectionMappings.getOrDefault(section.toLowerCase(), Collections.singletonList(section));
    }

    public String getInstitutionFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : urlToInstitutionKey.entrySet()) {
            if (url.toLowerCase().contains(entry.getKey().toLowerCase())) {
                // Return the first variation (usually the acronym) for the matched key
                List<String> variations = institutionMappings.get(entry.getValue());
                if (variations != null && !variations.isEmpty()) {
                    return variations.get(0);
                }
            }
        }
        return null;
    }
}
