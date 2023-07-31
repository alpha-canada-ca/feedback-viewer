package ca.gc.tbs.controller;

import ca.gc.tbs.domain.TopTaskSurvey;
import ca.gc.tbs.repository.TopTaskRepository;
import ca.gc.tbs.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesInput.Column;
import org.springframework.data.mongodb.datatables.DataTablesOutput;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Controller
public class TopTaskController {

    private static final Logger LOG = LoggerFactory.getLogger(TopTaskController.class);

    @Autowired
    private TopTaskRepository topTaskRepository;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/topTaskData")
    @ResponseBody
    public DataTablesOutput<TopTaskSurvey> list(@Valid DataTablesInput input) {

        String dateSearchVal = getInputSearchValue(input, "dateTime");
        String dataSetVal = getInputSearchValue(input, "taskOther");
        String taskValue = getInputSearchValue(input, "task");

        // escape all the brackets so that the input can return a result.
        taskValue = escapeBrackets(taskValue);
        setInputSearchValue(input, "task", taskValue);

        printColumns(input);

        Criteria findProcessed = where("processed").is("true");

        if (dataSetVal.contains("nonEmpty") && dateSearchVal.contains(":")) {
            String[] dateRange = dateSearchVal.split(":");

            if (dateRange.length == 2) {
                setInputSearchValue(input, "taskOther", "");
                setInputSearchValue(input, "dateTime", "");

                Criteria dateCriteria = createDateCriteria(dateRange[0], dateRange[1]);

                if (!dateRange[0].equals("") && !dateRange[1].equals("")) {
                    return topTaskRepository.findAll(input, createNonEmptyCriteria(), dateCriteria);
                }
            }
        }

        if (dateSearchVal.contains(":")) {
            String[] dateRange = dateSearchVal.split(":");
            if (dateRange.length == 2) {
                setInputSearchValue(input, "dateTime", "");

                Criteria dateCriteria = createDateCriteria(dateRange[0], dateRange[1]);

                if (!dateRange[0].equals("") && !dateRange[1].equals("")) {
                    return topTaskRepository.findAll(input, findProcessed, dateCriteria);
                }
            }
        }

        if (dataSetVal.contains("nonEmpty")) {
            setInputSearchValue(input, "taskOther", "");
            return topTaskRepository.findAll(input, findProcessed, createNonEmptyCriteria());
        }

        return topTaskRepository.findAll(input, findProcessed);
    }

    private String getInputSearchValue(DataTablesInput input, String columnName) {
        return input.getColumn(columnName).get().getSearch().getValue();
    }

    private void setInputSearchValue(DataTablesInput input, String columnName, String value) {
        input.getColumn(columnName).get().getSearch().setValue(value);
    }

    private String escapeBrackets(String value) {
        return value.replace("(", "\\(").replace(")", "\\)");
    }

    private void printColumns(DataTablesInput input) {
        for (Column col : input.getColumns()) {
            System.out.println(col);
        }
        System.out.println("---------------------");
    }

    private Criteria createDateCriteria(String startDate, String endDate) {
        return where("dateTime").gte(startDate).lte(endDate);
    }

    private Criteria createNonEmptyCriteria() {
        return new Criteria().orOperator(
                Criteria.where("taskOther").exists(true).ne(""),
                Criteria.where("themeOther").exists(true).ne(""),
                Criteria.where("taskWhyNotComment").exists(true).ne(""),
                Criteria.where("taskImproveComment").exists(true).ne(""));
    }

    @RequestMapping(value = "/topTaskSurvey/departments")
    @ResponseBody
    public String departmentData(HttpServletRequest request) {
        return "AAFC / AAC,ATSSC / SCDATA,CATSA / ACSTA,CFIA / ACIA,CIRNAC / RCAANC,NSERC / CRSNG,CBSA / ASFC,CCG / GCC,CGC / CCG,"
                + "CIHR / IRSC,CIPO / OPIC,CRA / ARC,CRTC / CRTC,CSA / ASC,CSEC / CSTC,CSPS / EFPC,DFO / MPO,DND / MDN,ECCC / ECCC,"
                + "ESDC / EDSC,FCAC / ACFC,FIN / FIN,GAC / AMC,HC / SC,INFC / INFC,IRCC / IRCC,ISC / SAC,ISED / ISDE,JUS / JUS,"
                + "LAC / BAC,NFB / ONF,NRC / CNRC,NRCan / RNCan,OSB / BSF,PBC / CLCC,PC / PC,PCH / PCH,PCO / BCP,PHAC / ASPC,"
                + "PS / SP,PSC / CFP,SSC / PSC,PSPC / SPAC,RCMP / GRC,StatCan / StatCan,TBS / SCT,TC / TC,VAC / ACC,WAGE / FEGC,WD / DEO";
    }

    @RequestMapping(value = "/topTaskSurvey/taskList/en")
    @ResponseBody
    public String[] taskListEN(HttpServletRequest request) {
        return new String[]{"COVID-19 cases, statistics", "Vaccines and treatment",
                "Get your Canadian COVID-19 proof of vaccination", "Symptoms of COVID-19", "Testing for COVID-19",
                "Prevention, masks", "Canada Recovery Caregiving Benefit (CRCB)",
                "Canada Recovery Sickness Benefit (CRSB)", "Canada Recovery Benefit (CRB)",
                "Canada Worker Lockdown Benefit (CWLB)", "Tourism and Hospitality Recovery Program (THRP)",
                "Hardest-Hit Businesses Recovery Program (HHBRP)", "Canada Emergency Wage Subsidy (CEWS)",
                "Canada Emergency Rent Subsidy (CERS)", "Canada Recovery Hiring Program (CRHP)",
                "COVID-19 requirements for travellers to Canada", "Travel inside Canada", "Travel outside Canada",
                "Other - Reason for my visit is not in this list", "Jobs in the private sector",
                "Jobs in the federal public service", "Find a student job in the federal public service",
                "Find an apprenticeship", "Join the army",
                "Apply to work and travel abroad with International Experience Canada (IEC) as a Canadian",
                "Apply for a work permit", "Apply for a Social Insurance Number (SIN)", "Get job training",
                "My Service Canada Account (MSCA) - View Records of Employment (ROE)", "Post a job in the Job Bank",
                "Hire a foreign worker", "Apply for Guaranteed Income Supplement (GIS)",
                "Old Age Seccurity (OAS) - Apply for Old Age Security",
                "Old Age Seccurity (OAS) - View payment amounts",
                "Canada Pension Plan (CPP) - Apply for Canada Pension Plan", "Calculate your Retirement Payments",
                "Find a pension payment date", "Employment Insurance (EI) - Apply for Employment Insurance",
                "Employment Insurance (EI) - Submit an Employment Insurance Report",
                "My Service Canada Account (MSCA) - Sign in",
                "My Service Canada Account (MSCA) - Register for an account",
                "Sign up for direct deposit or update your banking information",
                "Apply for an apprenticeship grant or loan", "Apply for funding for Canada summer jobs",
                "Support for Temporary Foreign Workers", "Apply for Canadian citizenship",
                "Check if you need a visa or electronic travel authorization (eTA) to travel to Canada",
                "Check processing times", "Check your application status",
                "Contact Immigration, Refugees and Citizenship Canada", "Find a designated learning institution",
                "Find a visa application centre", "Find an IRCC application package or form",
                "Find out if you need to give your fingerprints and photo (biometrics) and where to do that",
                "Find your National Occupational Classification (NOC)",
                "Get, renew or replace a permanent resident card", "How to extend your stay in Canada",
                "Immigrate as a provincial nominee", "Immigrate through Express Entry",
                "IRCC secure account - register, sign in, help", "Pay your fees online",
                "Sponsor your family members to immigrate to Canada", "Study in Canada – for non-Canadians",
                "Use the Authorized Paid Representatives Portal", "Apply for a copyright", "Apply for a patent",
                "Apply for a trademark", "Apply for the Farm Debt Mediation Service",
                "Consult a trade commissioner to receive international business information",
                "Download RETScreen clean energy management software",
                "Establish or expand your business in Canada as a foreign investor", "File an annual corporate return",
                "Find a bankruptcy or insolvency record", "Find a corporation",
                "Find a decision, notice, order, policy on radio, television or telecommunications", "Find a patent",
                "Find a trademark", "Find Canadian importing companies", "Find government grants, loans and financing",
                "Trade negotiations and agreements", "Incorporate a business", "Look up industry statistics",
                "Manage and report goods imported into Canada",
                "Register a sole-proprietorship or partnership (not incorporated)",
                "Review customs tariffs for importing goods to Canada", "Set up a not-for-profit corporation",
                "Apply for a study permit", "ArriveCAN - Submit travel information or daily symptoms",
                "Boarding flights to and within Canada", "Check provincial and territorial restrictions",
                "Check the Canada-United States border wait times – from Canada",
                "Check the United States-Canada border wait times – from the United States",
                "Check vaccination requirements to enter Canada",
                "Compassionate travel quarantine exemptions (caring for others, funerals and support)",
                "Contact a federal office – outside Canada (embassy, consulate, trade office)",
                "Contact the Canada Border Services Agency (CBSA)", "COVID-19 testing requirements for travellers",
                "Publications and guides",
                "Find out about identification requirements for crossing the Canadian border",
                "Find out if you can enter Canada", "Find out what you can bring on a plane",
                "Follow mandatory quarantine or isolation", "Identify what you can bring home to Canada",
                "Latest news – Travel and emergency assistance", "Apply for a new Canadian passport",
                "Renew a Canadian passport", "Register as a Canadian living or travelling outside Canada",
                "Visit a national park or historic site", "Travel advice and advisories",
                "Travel health notices in effect for a country", "Apply for a visa when travelling outside Canada",
                "Apply for a visitor visa to Canada", "Apply for a benefit for Indigenous people",
                "My Service Canada Account (MSCA) - Check your Employment Insurance (EI) claim status and correspondence",
                "My Service Canada Account (MSCA) - View payment information",
                "My Service Canada Account (MSCA) - Check your CPP/OAS application status",
                "My Service Canada Account (MSCA) - Check your estimated monthly Canada Pension Plan (CPP) benefits",
                "My Service Canada Account (MSCA) - Get your T4E, T4A or NR4 tax slips",
                "My Service Canada Account (MSCA) - View/Change your personal information",
                "Employment Insurance (EI) - Check eligibility",
                "Canada Pension Plan (CPP) - Check status of application",
                "Canada Pension Plan (CPP) - Estimate monthly benefits",
                "Canada Pension Plan (CPP) - Get statement of benefits",
                "Canada Pension Plan (CPP) - Check payment information",
                "Canada Pension Plan Disability (CPPD) - Apply for Canada Pension Plan Disability benefits",
                "Canada Pension Plan (CPP) - Apply for a Survivors Pension", "Apply for a student grant or loan",
                "Get student loan repayment assistance", "Delay repayment of a student loan (still studying)",
                "Learn about benefits for people with disabilities", "Apply for the Canada Child Benefit (CCB)",
                "Look up expected child support payments",
                "Sign into or create a Veterans Affairs Canada (VAC) account", "Consult Canada's Food Guide",
                "Contact Health Canada or the Public Health Agency of Canada",
                "Find information on family violence and how to get help",
                "Find information on producing and selling cannabis",
                "Find out if a medical device is licenced for sale in Canada",
                "Find out the hazardous properties of a human pathogen and how to work with it in a laboratory setting",
                "Find out the safe cooking temperature for food",
                "Find out the symptoms or risks for a disease or condition",
                "Find out whether a drug is approved for use and sale in Canada",
                "Find out whether a natural health product is licensed for use in Canada",
                "Find out who is eligible for and how to access medical assistance in dying",
                "Get consumer or health product recalls and safety alerts", "Get food recalls and safety alerts",
                "Get guidelines for immunization and vaccination practices",
                "Get information on vaping or the risk of vaping", "Learn about Canada's health care system",
                "Learn about cannabis, its health effects, its medical use, its legalization in Canada",
                "Learn about food labels", "Learn about hazardous materials in the workplace",
                "Learn about mental health support and how to get help",
                "Learn about outbreaks of food and water-borne illnesses and diseases in Canada",
                "Learn about the problematic use of drugs (such as opioids) and how to get help",
                "Look up the key social determinants of health in Canada",
                "Report a side effect to a drug, medical device, vaccine, or natural health product",
                "Apply for the disability tax credit", "Calculate payroll deductions",
                "Change my address with the Canada Revenue Agency", "Check online mail",
                "Check the balance owing on a corporate income tax account", "File a GST/HST return",
                "File an employer information return, such as a T4 Summary",
                "Find a Canada Revenue Agency telephone number",
                "Find my Registered Retirement Savings Plan (RRSP) contribution limit",
                "Find my Tax-free savings Account (TFSA) contribution limit",
                "Find out how much my benefit payment will be", "Find out when I can expect my tax refund",
                "Find out which tax deductions, credits, or expenses I can claim", "Find tax software",
                "Find the income tax rates", "Find the next benefit payment date",
                "Get a copy of a notice of assessment or reassessment", "Get a GST/HST access code",
                "Get an income tax form and guide", "Get the TD1 Personal Tax Credits Return form",
                "Look up a charitable organization", "Make a payment to the Canada Revenue Agency",
                "Open a GST/HST account", "Access My Account", "Open a My Account", "Access My Business Account",
                "Open a My Business Account", "Report suspected fraud", "See my T4 information",
                "Calculate the magnetic declination for a specified location and date", "Check past weather conditions",
                "Consult Canada's plan to fight climate change",
                "Consult the reporting and inventory of greenhouse gas emissions",
                "Current and past earthquakes in Canada", "Current and past wildfires in Canada",
                "Find a fuel efficient vehicle", "Find out about forest conservation",
                "Find out about the major pollutants and toxic substances affecting the environment",
                "Find out how Canada is managing pollution", "Find out if you need a fishing licence",
                "Find out what Canada is doing about climate change",
                "Find out which energy efficiency incentives and grants are available",
                "Get an emergency kit (to be prepared during a natural disaster, power outage, bomb threat, etc)",
                "Get and understand Air Quality Health Index ratings for my location",
                "Get current and forecasted ice conditions", "Get current and forecasted weather for your location",
                "Get marine conditions (tides and ocean water levels)", "Get satellite and air imagery",
                "Get space weather forecasts", "How to prepare for seasonal severe weather conditions",
                "Learn about climate change", "Learn about managing and reducing waste",
                "Look up a species at risk in Canada", "Make a home energy efficient", "Renewable energy in Canada",
                "Understand how pricing pollution works in Canada", "Use a geodetic reference tool",
                "Water level and flood monitoring in Canada (inland)", "Access cyber security bulletins",
                "Apply for a job in the military", "Civilian jobs with National Defence",
                "Find information on a specific unit, squadron or ship", "Find out the various ranks in the military",
                "Find out where the military is currently deployed on operations around the world", "Join the Cadets",
                "Learn about Canada's defence policy", "Learn about cyber security threats",
                "Learn about Passenger Protect program", "Learn about the Canadian Army Reserve",
                "Learn about the current list of terrorist entities",
                "Learn about the required documents needed for air travel", "Pay rates for military members",
                "Read the Defence administrative orders and directives (DAOD)",
                "Read the Queen's regulations and orders (QR&O)", "Search for jobs in the military",
                "Apply for a tax credit — film or video (CAVCO)", "Apply to a grant or funding program — arts",
                "Apply to a grant or funding program — cultural preservation",
                "Apply to a grant or funding program — multiculturalism", "Apply to a grant or funding program — music",
                "Apply to a grant or funding program — sports", "Participate in Winterlude events and activities",
                "Participate in Canada Day celebrations", "Participate in National Indigenous Peoples Day",
                "Participate in Remembrance Day", "Find information on human rights in Canada",
                "Learn about Aboriginal history in Canada", "Learn about the First World War",
                "Look up soldiers who lost their lives in the military or RCMP",
                "Learn about the Governor General or Lieutenant Governors and the appointment process",
                "Download the lyrics and recordings of Canada’s national anthem",
                "Learn about the official symbols of Canada",
                "Discover the origins of the names of Canada's provincial and territorial capitals",
                "Research your genealogy and family history", "Search births, marriages and deaths recorded in Canada",
                "View current census data", "Find student job and internship programs",
                "Find a decision, notice, order or policy about radio, television or telecommunications",
                "Apply for a criminal record suspension", "Apply for a cannabis record suspension",
                "Get a criminal record check", "Apply for or renew your firearms licence",
                "Check the status of your firearms licence application",
                "Change your address for your firearms licence", "Contact a chief firearms officer",
                "Learn about firearms safety, storage and when you need a licence", "Join the RCMP",
                "Find your local RCMP detachment (police service)", "Check the National Sex Offender Registry",
                "Check the Royal Canadian Mounted Police (RCMP) wanted list", "Consult a law, treaty or regulation",
                "Learn about how the Government of Canada responds to emergency events",
                "Learn about national search and rescue", "Look up a Canadian disaster", "Prepare an emergency plan",
                "Apply for the Memorial Grant Program for First Responders",
                "Apply for your transportation security clearance", "Assessing medical fitness of aviation personnel",
                "Building, maintaining and registering an aircraft",
                "Find out how to import or bring a vehicle into Canada", "Flying an aircraft",
                "Find where you can fly your drone", "Learn about advanced vehicle technologies",
                "Learn about child car seat safety",
                "Find if there is a defect or recall for your vehicle, tires or child car seat",
                "Learn about grade crossing safety", "Learn about safety awareness when transporting dangerous goods",
                "Learn about shipping or mailing lithium batteries",
                "Learn about the containers required to transport dangerous goods by rail",
                "Learn how to build an emergency response assistance plan in case of an accident while shipping dangerous goods",
                "Learn how to get your transportation security clearance", "Learn how to register your vessel",
                "Learn tips on safe boating", "Licensing and training of aviation personnel",
                "Operating a commercial air service", "Operating an airport or aerodrome",
                "Read the Canadian Aviation Regulations (CARs)",
                "Read the regulations for transporting dangerous goods",
                "Read the rules for operating a federal railway", "Find statistics for vehicle collisions",
                "Training and certification of individuals",
                "Authenticate documents so that they can be recognized outside Canada",
                "Canada's feminist international assistance policy", "Canada’s actions in a specific country or region",
                "Canada’s foreign policy and international relations",
                "Canada’s response to conflicts, crisis and disasters",
                "Contact Global Affairs Canada – offices in Canada", "Country insights (cultural information)",
                "Democracy and good governance", "Environment and climate change",
                "Find international study or research opportunities in Canada for non-Canadians",
                "Gender equality and the empowerment of women and girls", "Human rights and dignity",
                "International assistance budget (humanitarian aid and development projects)",
                "International assistance projects funded by Canada", "International treaties signed by Canada",
                "Jobs with Global Affairs Canada (embassies/consulate, trade offices and partnership organizations)",
                "Latest news – Foreign affairs (international relations and global issues)",
                "Latest news – International development (humanitarian assistance and development projects)",
                "Peace and security", "Right to education (international development)",
                "Sanctions imposed by Canada against specific countries, organisations and individuals",
                "Scholarships to study in Canada – for non-Canadians",
                "Scholarships to study outside Canada – for Canadians",
                "Seek funding opportunities for international aid projects",
                "Sustainable agriculture, green technologies and renewable energy", "The right to health and nutrition",
                "United nations (UN)", "Volunteer for work in a humanitarian aid project", "Buying a home",
                "Calculate if you qualify for a mortgage", "Calculate your credit card payments",
                "Calculate your mortgage payments and/or prepayments", "Cashing a Government of Canada cheque",
                "Check Canada's tax treaties", "Compare Canadian bank accounts", "Consult the Federal Budget",
                "Create a plan to get out of debt and/or savings goals",
                "Determine current market prices for lumber, crude oil and other fuels",
                "Find available disability benefits", "Find information on loans and lines of credit",
                "Find out about student lines of credit", "Getting a home equity line of credit",
                "How much you need for a down payment", "Improving your credit score", "Make a budget",
                "Make a will and plan your estate", "Making a complaint to your financial institution",
                "Opening a bank account", "Ordering your credit report and score", "Understand severance pay",
                "Apply for a grant for technological innovation - Industrial Research Assistance Program (IRAP)",
                "Find a scientific, technical or medical journal", "View the northern lights camera",
                "Solar eclipses times and appreciation guide",
                "Build your own projector to watch solar eclipses safely", "Read biographies of Canadian astronauts",
                "Official time across Canada", "Check sunrise and sunset times",
                "Get the latest National building code for Canada",
                "Buy building codes or Certified Reference Materials",
                "Consult the Canadian National Master Construction Specifications", "View statistical data",
                "Apply for an International Standard Book Number (ISBN) / catalogue number",
                "Consult the annual report on advertising activities",
                "Consult the Clerk’s Annual Report to the Prime Minister", "Consult the Speech from the Throne",
                "Examine the activities of the Independent Advisory Board for Senate Appointments",
                "Find a government department or agency", "Read a report or a publication",
                "Find a government policy, directive, standard or guideline",
                "Find public opinion research requirements",
                "Find travel and hospitality expenses of government officials",
                "Learn about Canada’s democratic institutions", "Learn about Governor in Council appointments process",
                "Learn about health and safety in federal properties",
                "Federal government property management, maintenance, and construction",
                "Learn about the Clerk of the Privy Council",
                "Learn about the relationship between provinces and territories",
                "Learn how the government provides financial support to provinces and territories",
                "Make an access to information or personal information request",
                "Review of the state of bilingualism in the federal Public Service",
                "Review the progress of Government of Canada commitments", "Search government grants and contributions",
                "Search the Orders in Council database",
                "See how the Government of Canada supports the LGBTQ2+ community",
                "View public notices in the Canada Gazette", "View Parliament Hill camera",
                "Calculate my expected pension as a public servant", "Contact the Government of Canada Pension Centre",
                "Find a benefits form", "Find available benefits for survivors of deceased public servants",
                "Find guidance on leave requests due to COVID-19", "Find guidance on working remotely during COVID-19",
                "Find out what is considered harassment in the public service",
                "Find out when I could retire as a public servant", "Find public service employees rates of pay",
                "Find public service executives rates of pay", "Find public service staffing tools",
                "Find public service students rates of pay", "Help with pay issues (pay action requests, pay enquiry)",
                "Get updates on public service collective bargaining",
                "Get updates on the status of the Phoenix pay system",
                "Learn about employee rights and responsibilities related to COVID-19",
                "Learn about qualifications, competencies and classifications for public service jobs",
                "Learn about temporary changes to benefits due to COVID-19",
                "Learn about the public service pension plan", "Learn about your public service pay",
                "Make a public service dental insurance claim", "Make a public service health care insurance claim",
                "Read a public service collective agreement",
                "Read about the performance management program for employees",
                "See what's covered by dental insurance in the public service",
                "See what's covered by health insurance in the public service",
                "Access your pay (MyGCPay, CWA, Phoenix)"};
    }

    @RequestMapping(value = "/topTaskSurvey/taskList/fr")
    @ResponseBody
    public String[] taskListFR(HttpServletRequest request) {
        return new String[]{"Autre - La raison de ma visite n'est pas sur cette liste",
                "S’enregistrer pour le dépôt direct ou mettre à jour ses renseignements bancaires",
                "Présenter une demande de permis de travail", "Visiter un parc national ou un lieu historique",
                "Assurance-emploi – Présenter une demande de prestations de l'assurance-emploi",
                "Assurance-emploi – Soumettre une déclaration d'assurance-emploi",
                "Communiquer avec un bureau fédéral – à l'extérieur du Canada (ambassade, consulat ou bureau de commerce)",
                "Communiquez avec Immigration, Réfugiés et Citoyenneté Canada",
                "Conseils de santé aux voyageurs en vigueur pour un pays", "Consulter le budget fédéral",
                "Déterminer ce que vous pouvez emporter en avion", "Mon dossier Service Canada – Créer un compte",
                "Mon dossier Service Canada – Ouvrir une session",
                "Mon dossier Service Canada – Voir les relevés d’emploi (RE)", "Négociations et accords de commerce",
                "Obtenir votre preuve de vaccination canadienne contre la COVID-19",
                "Présenter une demande de Supplément de revenu garanti (SRG)",
                "Régime de pensions du Canada (RPC) – Présenter une demande de prestations",
                "Se procurer une trousse d'urgence (à préparer en cas de catastrophe naturelle, de panne d'électricité, d'alerte à la bombe, etc.)",
                "Se renseigner sur le plafond de cotisation annuel à un compte d’épargne libre d’impôt (CÉLI)",
                "Se renseigner sur son plafond de cotisation à un régime enregistré d’épargne-retraite (REER)",
                "Sécurité de la Vieillesse – Présenter une demande de prestations",
                "Sécurité de la Vieillesse – Voir le montant des paiements", "Signaler une fraude présumée",
                "Travailler et voyager à l'étranger avec Expérience internationale Canada (EIC) en tant que Canadien",
                "Trouver la date de paiement d’une pension", "Trouver un dossier de faillite et d’insolvabilité",
                "Accéder à l’information sur ma paye (MaPayeGC, applications Web de la rémunération, Phénix)",
                "Accéder à Mon dossier", "Accéder à Mon dossier d'entreprise", "Accédez aux bulletins de cybersécurité",
                "Acheter des exemplaires des codes du bâtiment ou du matériel de référence certifié",
                "Acheter une maison", "Agriculture durable, technologies vertes et énergie renouvelable",
                "Aide aux travailleurs étrangers temporaires", "Améliorer votre cote de crédit",
                "Aperçu des pays (informations culturelles)", "Apprendre comment le Canada gère la pollution",
                "Apprendre comment le gouvernement fournit un soutien financier aux provinces et aux territoires",
                "Apprenez des conseils sur la sécurité nautique",
                "Apprenez-en davantage sur les prestations pour les personnes handicapées",
                "Apprenez-en plus sur les menaces à la cybersécurité",
                "ArriveCAN - Soumettre des informations sur le voyage ou les symptômes quotidiens",
                "Assurance-emploi – Vérifier votre admissibilité",
                "Authentifier des documents afin qu'ils puissent être reconnus à l'extérieur du Canada",
                "Bourses pour étudier à l’extérieur du Canada – pour les Canadiens",
                "Bourses pour étudier au Canada – pour les non-Canadiens",
                "Budget de l'aide internationale (aide humanitaire et projets de développement)",
                "Calculer des retenues sur la paie",
                "Calculer la déclinaison magnétique pour un lieu et une date donnés",
                "Calculer le montant de pension de retraite que je peux m’attendre à recevoir en tant que fonctionnaire publique",
                "Calculer le montant de prêt hypothécaire auquel vous êtes admissible",
                "Calculer vos paiements par carte de crédit", "Calculer vos versements de retraite",
                "Calculer vos versements hypothécaires ou remboursements anticipés",
                "Changer d’adresse pour son permis d’armes à feu", "Changer mon adresse à l’Agence du revenu du Canada",
                "Combien faut-il pour une mise de fonds", "Commander votre dossier de crédit",
                "Comment se préparer aux conditions météorologiques saisonnières difficiles",
                "Communiquer avec Affaires mondiales Canada – bureaux situés au Canada",
                "Communiquer avec l'Agence des services frontaliers du Canada (ASFC)",
                "Communiquer avec le Centre des pensions du gouvernement du Canada",
                "Communiquer avec le détachement de la GRC (ou le service de police) local",
                "Communiquer avec Santé Canada ou l'Agence de la santé publique du Canada",
                "Communiquer avec un contrôleur des armes à feu", "Comparer les comptes bancaires canadiens",
                "Comprendre comment fonctionne la tarification de la pollution au Canada",
                "Comprendre l’indemnité de départ", "Compte sécurisé d'IRCC - enregistrer, connecter, aide",
                "Connaître la date de mon prochain versement de prestation",
                "Connaître le montant de mon versement de prestation",
                "Connaître les crédits d’impôt, les déductions ou les dépenses que je peux demander",
                "Connaître les principaux polluants et substances toxiques affectant l'environnement",
                "Connaître les propriétés dangereuses d'un agent pathogène humain et ses procédures de manipulation en laboratoire",
                "Connaître les symptômes ou les risques d’une maladie ou d’une affection",
                "Connaître les taux de l’impôt sur le revenu", "Conseils aux voyageurs et avertissements",
                "Constituer une entreprise en société", "Constituer une organisation à but non lucratif",
                "Construire, entretenir et immatriculer un aéronef",
                "Consulter le devis directeur national de la construction au Canada", "Consulter le discours du Trône",
                "Consulter le guide alimentaire canadien",
                "Consulter le plan du Canada pour lutter contre les changements climatiques",
                "Consulter le rapport annuel du greffier au premier ministre",
                "Consulter le rapport annuel sur les activités de publicité",
                "Consulter les avis publics dans la Gazette du Canada", "Consulter les conventions fiscales du Canada",
                "Consulter les données actuelles du recensement",
                "Consulter les paiements de pension alimentaire pour enfants prévus",
                "Consulter les rapports et inventaires des émissions de gaz à effet de serre",
                "Consulter les services couverts par le Régime de soins de santé de la fonction publique",
                "Consulter les services couverts par le Régime de soins dentaires de la fonction publique",
                "Consulter les statistiques de l’industrie", "Consulter mes feuillets de renseignements T4",
                "Consulter mon courrier en ligne",
                "Consulter un délégué commercial pour obtenir de l’information sur le commerce international",
                "Consulter une loi, un traité ou un règlement", "Consultez le Règlement de l'aviation canadien (RAC)",
                "Consultez le Règlement sur le transport des marchandises dangereuses",
                "Consultez les règles relatives à l'exploitation d'un chemin de fer fédéral",
                "Dates des éclipses solaires et conseils pour l'observation",
                "Découvrez comment importer ou apporter un véhicule au Canada",
                "Découvrez les différents grades dans les Forces",
                "Découvrez où les Forces sont actuellement en déploiement dans le cadre d’opérations dans le monde entier",
                "Découvrez si vous avez besoin d’un visa ou d’une autorisation de voyage électronique (AVE) pour entrer au Canada",
                "Découvrez si vous devez fournir vos empreintes digitales et une photo (données biométriques) et où vous pouvez le faire",
                "Découvrir ce que le Canada fait pour lutter contre les changements climatiques",
                "Découvrir comment le gouvernement du Canada soutient la communauté LGBTQ2+",
                "Découvrir l’origine du nom des capitales provinciales et territoriales du Canada",
                "Découvrir les institutions démocratiques du Canada",
                "Découvrir les qualifications, les compétences et les classifications des emplois dans la fonction publique",
                "Découvrir quels sont les principaux déterminants sociaux de la santé au Canada",
                "Délivrance des licences et formation du personnel de l'aviation",
                "Demander l’Allocation canadienne pour enfants (ACE)",
                "Demander le crédit d’impôt pour personnes handicapées",
                "Demander ou renouveler un permis d’armes à feu", "Demander un nouveau passeport",
                "Démocratie et bonne gouvernance", "Dépistage de la COVID-19", "Déposer une marque de commerce",
                "Dépôt d'une plainte auprès de votre institution financière",
                "Dernières nouvelles – Affaires étrangères (relations internationales et enjeux mondiaux)",
                "Dernières nouvelles – Développement international (aide humanitaire et projets de développement)",
                "Dernières nouvelles – Voyage et aide d'urgence",
                "Déterminer à quel moment je pourrai prendre ma retraite en tant que fonctionnaire publique",
                "Déterminer ce que vous pouvez apporter au Canada",
                "Déterminer les prix actuels du marché pour le bois d’œuvre, le pétrole brut et les autres combustibles",
                "Déterminer si l’utilisation d’un médicament est autorisée au Canada",
                "Déterminer si l’utilisation d’un produit de santé naturel est autorisée au Canada",
                "Devenir bénévole pour un projet d’aide humanitaire", "Droits de la personne et dignité humaine",
                "Effectuer une recherche sur un désastre au Canada",
                "Efforts du Canada dans un pays ou une région spécifique",
                "Égalité entre les genres et l’autonomisation des femmes et des filles",
                "Élaborer un plan pour se désendetter ou pour ses objectifs d’épargne",
                "Embaucher un travailleur étranger",
                "Emploi à Affaires mondiales Canada (ambassades/consulats, bureaux de délégués commerciaux et organismes partenaires)",
                "Emploi à la fonction publique fédérale", "Emploi dans le secteur privé",
                "En savoir plus sur la liste actuelle des organisations terroristes",
                "En savoir plus sur la santé et la sécurité dans les immeubles fédéraux",
                "En savoir plus sur le greffier du Conseil privé",
                "En savoir plus sur le processus de nomination par le gouverneur en conseil",
                "En savoir plus sur le Programme de gestion du rendement pour les employés",
                "Encaisser un chèque du gouvernement du Canada",
                "Enregistrer une entreprise à propriétaire unique ou une co-entreprise (non incorporée)",
                "Entrée de voyageurs pour des raisons humanitaires et levée limitée de la mise en quarantaine (soins des autres, funérailles et soutien)",
                "Environnement et changements climatiques",
                "Établir ou développer votre entreprise au Canada en tant qu’investisseur étranger",
                "Établir un budget", "Étudier au Canada – pour les non-Canadiens",
                "Évaluation de l'état de santé du personnel de l'aviation",
                "Examen de l’état du bilinguisme dans la fonction publique fédérale",
                "Examiner les activités du Comité consultatif indépendant sur les nominations au Sénat",
                "Examiner les progrès réalisés en lien avec les engagements du gouvernement du Canada",
                "Exigences concernant les tests de COVID-19 pour les voyageurs",
                "Exigences COVID-19 pour les voyageurs", "Exploitation d'aéroports et d'aérodromes",
                "Exploitation d'un service aérien commercial",
                "Fabriquer son propre projecteur pour observer les éclipses solaires sans danger",
                "Faire un paiement à l’Agence du revenu du canada", "Faire un testament et planifier sa succession",
                "Faire une demande à un programme de subvention ou de financement — arts",
                "Faire une demande à un programme de subvention ou de financement — multiculturalisme",
                "Faire une demande à un programme de subvention ou de financement — musique",
                "Faire une demande à un programme de subvention ou de financement — préservation de la culture",
                "Faire une demande à un programme de subvention ou de financement — sport",
                "Faire une demande d’accès à l’information ou à vos renseignements personnels",
                "Faire une demande de bourse ou de prêt étudiant",
                "Faire une demande de crédit d’impôt — film ou vidéo (BCPAC)",
                "Faire une demande de Numéro international normalisé du livre (ISBN) / numéro de catalogue",
                "Faire une recherche dans la base de données des décrets du Conseil",
                "Faire une recherche dans les subventions et contributions du gouvernement",
                "Faire une recherche sur les militaires ou les membres de la GRC qui sont morts en service",
                "Faire une recherche sur vos généalogie et histoire familiale", "Formation et certification",
                "Gérer et déclarer les marchandises importées au Canada",
                "Gestion, entretien et construction de biens du gouvernement fédéral",
                "Heure officielle à travers le Canada", "Immigrez dans le cadre d’Entrée express",
                "Immigrez en tant que candidat d’une province", "Incendies de forêt actuels et passés au Canada",
                "Joignez-vous aux cadets", "L'énergie renouvelable au Canada",
                "Le droit à l'éducation (développement international)", "Le droit à la santé et à la nutrition",
                "Les emplois civils à la Défense nationale", "Lire les biographies des astronautes canadiens",
                "Lire un rapport ou une publication", "Lire une convention collective de la fonction publique",
                "Lisez les directives et ordonnances administratives de la Défense (DOAD)",
                "Lisez les Ordonnances et règlements royaux (ORFC)",
                "Mon dossier Service Canada – Obtenir vos feuillets d’impôt T4E, T4A ou NR4",
                "Mon dossier Service Canada – Visualiser l’estimation de vos prestations mensuelles du Régime de pensions du Canada (RPC)",
                "Mon dossier Service Canada – Visualiser l’état de votre demande d'assurance-emploi et votre correspondance",
                "Mon dossier Service Canada – Visualiser l’état de votre demande de Régime de pensions du Canada ou de Sécurité de la Vieillesse",
                "Mon dossier Service Canada – Voir les renseignements sur vos paiements",
                "Mon dossier Service Canada – Voir/modifier vos renseignements personnels",
                "Nombre de cas de COVID-19, statistiques",
                "Obtenez, renouvelez ou remplacez une carte de résident permanent",
                "Obtenir de l’aide à l’égard de problèmes de paye (demandes d'intervention de paye, demandes de renseignements au sujet de la paye)",
                "Obtenir de l’aide au remboursement des prêts d’études",
                "Obtenir de l’aide au remboursement des prêts étudiants",
                "Obtenir de l’information sur le vapotage ou les risques du vapotage",
                "Obtenir des images satellites et aériennes",
                "Obtenir des lignes directrices sur les pratiques d’immunisation et de vaccination",
                "Obtenir des mises à jour sur l’état du système de paye Phénix",
                "Obtenir des mises à jour sur les négociations collectives de la fonction publique",
                "Obtenir et comprendre les cotes de la cote air santé pour ma région",
                "Obtenir l'heure du lever et du coucher du soleil",
                "Obtenir la plus récente version du Code national du bâtiment",
                "Obtenir le formulaire TD1, Déclaration des crédits d'impôt personnels",
                "Obtenir les conditions actuelles et prévisions pour votre emplacement",
                "Obtenir les conditions de la glace actuelles et prévues",
                "Obtenir les conditions marines (marées et niveaux d'eau de l'océan)",
                "Obtenir les prévisions météorologiques spatiales",
                "Obtenir les rappels et les avis de sécurité sur les aliments",
                "Obtenir les rappels et les avis de sécurité sur les biens de consommation et les produits de santé",
                "Obtenir un code d’accès pour la TPS/TVH", "Obtenir un formulaire ou un guide d’impôt",
                "Obtenir une copie de mon avis de cotisation ou de nouvelle cotisation",
                "Obtenir une marge de crédit hypothécaire",
                "Obtenir une subvention à l'innovation technologique - Programme d'aide à la recherche industrielle",
                "Obtenir une vérification de casier judiciaire", "Organisation des Nations Unies (ONU)",
                "Ouvrir un compte de banque", "Ouvrir un compte de TPS/TVH",
                "Ouvrir un compte pour le service Mon dossier",
                "Ouvrir un compte pour le service Mon dossier d’entreprise",
                "Ouvrir une session dans un compte d’Anciens Combattants Canada (ACC) ou créer un tel compte",
                "Paix et sécurité", "Parrainez les membres de votre famille aux fins d’immigration au Canada",
                "Participer à des événements et activités du Bal de Neige",
                "Participer à la Journée nationale des peuples autochtones", "Participer au jour du Souvenir",
                "Participer aux célébrations de la fête du Canada", "Payez vos frais en ligne", "Piloter un aéronef",
                "Politique d'aide internationale féministe du Canada",
                "Politique étrangère et relations internationales du Canada", "Postulez pour un emploi dans les Forces",
                "Préparer un plan d’urgence",
                "Présenter dune demande pour le service de médiation en matière d’endettement agricole",
                "Présenter une demande d'habilitation de sécurité en matière de transport",
                "Présenter une demande d’Allocation canadienne pour enfants (ACE)", "Présenter une demande de brevet",
                "Présenter une demande de droit d’auteur",
                "Présenter une demande de financement avec Emplois d'été Canada",
                "Présenter une demande de numéro d’assurance sociale (NAS)", "Présenter une demande de permis d'études",
                "Présenter une demande de prestations d’invalidité du Régime de pensions du Canada",
                "Présenter une demande de prestations pour Autochtones", "Présenter une demande de prêts et bourses",
                "Présenter une demande de prêts et bourses aux apprentis",
                "Présenter une demande de suspension de casier judiciaire",
                "Présenter une demande de suspension du casier liée au cannabis",
                "Présenter une demande de visa de visiteur au Canada",
                "Présenter une demande de visa pour les voyages à l'extérieur du Canada",
                "Présenter une demande pour le Programme de subvention commémorative pour les premiers intervenants",
                "Présentez une demande de citoyenneté canadienne",
                "Prestation canadienne de la relance économique (PCRE)",
                "Prestation canadienne de la relance économique pour proches aidants (PCREPA)",
                "Prestation canadienne de maladie pour la relance économique (PCMRE)",
                "Prestation canadienne pour les travailleurs en cas de confinement (PCTCC)", "Prévention, masques",
                "Produire une déclaration annuelle de revenus des sociétés", "Produire une déclaration de TPS/TVH",
                "Programme d'embauche pour la relance économique du Canada",
                "Programme de relance pour le tourisme et l'accueil (PRTA)",
                "Programme de relance pour les entreprises les plus durement touchées (PREPDT)",
                "Projets d'aide internationale financés par le Canada", "Prolonger votre séjour au Canada",
                "Publications et guides", "Publier une offre d’emploi sur Guichet-Emplois",
                "Rechercher des opportunités de financement pour un projet d'aide internationale",
                "Rechercher les naissances, mariages et décès enregistrés au Canada",
                "Rechercher un organisme de bienfaisance", "Rechercher une espèce en péril au Canada",
                "Régime de pensions du Canada (RPC) – Estimer le montant des prestations mensuelles",
                "Régime de pensions du Canada (RPC) – Obtenir un relevé des prestations",
                "Régime de pensions du Canada (RPC) – Présenter une demande de pension de survivant",
                "Régime de pensions du Canada (RPC) – Visualiser l’état de votre demande",
                "Régime de pensions du Canada (RPC) – Voir les renseignements sur vos paiements",
                "Remplir une déclaration de renseignements d’employeur, comme un T4 Sommaire",
                "Rendre une maison éconergétique", "Renouveler un passeport canadien",
                "Renseignements sur les exigences en matière d’identification pour traverser la frontière canadienne",
                "Renseignements sur une unité, un escadron ou un navire particulier",
                "Renseignements sur votre paye dans la fonction publique",
                "Renseignez-vous sur l’expédition ou l’envoi de piles au lithium",
                "Renseignez-vous sur la façon d'élaborer un plan d'intervention d'urgence en cas d'un accident lors de transporter des marchandises dangereuses",
                "Renseignez-vous sur la façon d'immatriculer votre bâtiment",
                "Renseignez-vous sur la façon de présenter une demande d'habilitation de sécurité en matière de transport",
                "Renseignez-vous sur la politique de défense du Canada",
                "Renseignez-vous sur la Réserve de l’Armée canadienne",
                "Renseignez-vous sur la sécurité des passages à niveau",
                "Renseignez-vous sur la sécurité des sièges d'auto pour enfants",
                "Renseignez-vous sur la sensibilisation à la sécurité en matière de transport de marchandises dangereuses",
                "Renseignez-vous sur le programme de protection des passagers",
                "Renseignez-vous sur les contenants pour le transport de marchandises dangereuses par chemin de fer",
                "Renseignez-vous sur les documents requis pour les voyages aériens",
                "Renseignez-vous sur les technologies automobiles de pointe",
                "Réponse du Canada lors de conflits, de crises et de catastrophes",
                "Retard dans le remboursement d’un prêt étudiant (toujours aux études)",
                "Retarder le remboursement des prêts aux étudiants (encore aux études)",
                "S'inscrire en tant que Canadien vivant ou voyageant à l'extérieur du Canada", "S’enrôler dans l’armée",
                "S’informer sur la température de cuisson sécuritaire des aliments",
                "Sanctions imposées par le Canada à l’encontre de pays, d’organisations et d’individus particuliers",
                "Savoir quand je peux m’attendre à recevoir mon remboursement d’impôt",
                "Savoir si la vente d’un appareil médical est autorisée au Canada", "Se joindre à la GRC",
                "Se renseigner pour savoir si vous avez besoin d'un permis de pêche",
                "Se renseigner sur ce qui constitue du harcèlement dans la fonction publique",
                "Se renseigner sur l’histoire autochtone au Canada",
                "Se renseigner sur l’usage problématique des drogues (comme les opioïdes) et les façons d’obtenir de l’aide",
                "Se renseigner sur la conservation des forêts",
                "Se renseigner sur la façon dont le gouvernement du Canada intervient en cas d'urgence",
                "Se renseigner sur la gestion et la réduction des déchets",
                "Se renseigner sur la Première Guerre mondiale",
                "Se renseigner sur la recherche et le sauvetage à l’échelle nationale",
                "Se renseigner sur la sécurité des armes à feu, l’entreposage et le moment où vous avez besoin d’un permis",
                "Se renseigner sur le gouverneur général ou les lieutenants-gouverneurs et le processus de nomination",
                "Se renseigner sur le Régime de retraite de la fonction publique",
                "Se renseigner sur le soutien en santé mentale et les façons d’obtenir de l’aide",
                "Se renseigner sur le système de soins de santé canadien",
                "Se renseigner sur les changements climatiques",
                "Se renseigner sur les droits et les responsabilités des employés liés au COVID-19",
                "Se renseigner sur les éclosions de maladies d’origine alimentaire et hydrique au Canada",
                "Se renseigner sur les incitatifs et les subventions disponibles en matière d'efficacité énergétique",
                "Se renseigner sur les marges de crédit pour étudiants",
                "Se renseigner sur les matières dangereuses utilisées au travail",
                "Se renseigner sur les modifications temporaires au Régime de soins de santé de la fonction publique (RSSFP)",
                "Se renseigner sur les personnes admissibles à l’aide médicale à mourir et sur la façon d’y avoir accès",
                "Se renseigner sur les relations entre les provinces et les territoires",
                "Se renseigner sur les symboles officiels du Canada", "Séismes actuels et passés au Canada",
                "Signaler un effet secondaire d’un médicament, d’un instrument médical, d’un vaccin ou d’un produit de santé naturel",
                "Soumettre une demande de règlement du Régime de soins de santé de la fonction publique",
                "Soumettre une demande de règlement du Régime de soins dentaires de la fonction publique",
                "Subvention d'urgence du Canada pour le loyer (SUCL)",
                "Subvention salariale d'urgence du Canada (SSUC)", "Suivre les exigences de quarantaine ou d'isolation",
                "Suivre une formation professionnelle",
                "Surveillance des niveaux d'eau et des inondations au Canada (intérieur)", "Symptômes de la COVID-19",
                "Taux de rémunération des militaires",
                "Télécharger le logiciel de gestion d’énergies propres RETScreen",
                "Télécharger les paroles et les enregistrements de l’hymne national du Canada",
                "Temps d’attente à la frontière canado-américaine – en provenance des États-Unis",
                "Temps d’attente à la frontière canado-américaine – en provenance du Canada",
                "Traités internationaux signés par le Canada",
                "Trouver de l'information sur les droits de la personne au Canada",
                "Trouver de l’information sur la violence familiale et les façons d’obtenir de l’aide",
                "Trouver de l’information sur les prêts et les marges de crédit",
                "Trouver des conseils sur le travail à distance pendant COVID-19",
                "Trouver des conseils sur les demandes de congé en raison du COVID-19",
                "Trouver des informations au sujet des étiquettes de produits alimentaires",
                "Trouver des politiques, directives, normes et lignes directrices du gouvernement",
                "Trouver des possibilités d’études ou de recherches internationales au Canada pour les non-Canadiens",
                "Trouver des programmes d'emplois et de stages pour étudiants",
                "Trouver des renseignements sur la production et la vente de cannabis",
                "Trouver des renseignements sur le cannabis, ses effets sur la santé, son usage à des fins médicales et sa légalisation au Canada",
                "Trouver des sociétés d’importation canadiennes",
                "Trouver des subventions, des prêts et du financement du gouvernement",
                "Trouver les exigences en matière de recherche sur l’opinion publique",
                "Trouver les frais de déplacement et d’accueil des fonctionnaires",
                "Trouver les outils de dotation dans la fonction publique",
                "Trouver les prestations d’invalidité disponibles",
                "Trouver les prestations offertes aux survivants de fonctionnaires décédés",
                "Trouver les taux de rémunération des employés de la fonction publique",
                "Trouver les taux de rémunération des étudiants de la fonction publique",
                "Trouver les taux de rémunération du personnel de direction de la fonction publique",
                "Trouver un brevet", "Trouver un emploi d’étudiant à la fonction publique fédérale",
                "Trouver un formulaire pour les avantages sociaux",
                "Trouver un logiciel de préparation de déclarations de revenus", "Trouver un ministère ou organisme",
                "Trouver un numéro de téléphone de l’Agence du revenu du Canada", "Trouver un stage d’apprentissage",
                "Trouver un véhicule éconergétique",
                "Trouver une décision, un avis, une ordonnance ou une politique concernant la radio, la télévision ou les télécommunications",
                "Trouver une décision, un avis, une ordonnance, une politique sur la radio, la télévision ou les télécommunications",
                "Trouver une marque de commerce", "Trouver une revue scientifique, technique ou médicale",
                "Trouver une société", "Trouvez des statistiques sur les collisions de véhicules",
                "Trouvez un centre de réception des demandes de visa", "Trouvez un emploi dans les Forces",
                "Trouvez un endroit où faire voler votre drone", "Trouvez un établissement d’enseignement désigné",
                "Trouvez une trousse de demande ou un formulaire d’IRCC",
                "Trouvez votre code de la Classification nationale des professions (CNP)",
                "Utiliser un outil de référence géodésique",
                "Utilisez le Portail des représentants autorisés rémunérés", "Vaccins et traitements",
                "Vérifier l’état de sa demande de permis d’armes à feu",
                "Vérifier la liste des personnes recherchées par la Gendarmerie royale du Canada (GRC)",
                "Vérifier le Registre national des délinquants sexuels",
                "Vérifier le solde dû pour un compte d’impôt sur le revenu des sociétés",
                "Vérifier les conditions météorologiques passées",
                "Vérifier les restrictions provinciales et territoriales",
                "Vérifier les tarifs douaniers pour l'importation de marchandises au Canada",
                "Vérifier si vous pouvez entrer au Canada", "Vérifiez l’état de sa demande",
                "Vérifiez les délais de traitement",
                "Vérifiez les exigences en matière de vaccination pour entrer au Canada",
                "Vérifiez si votre véhicule, vos pneus ou votre siège d’auto pour enfant sont touchés par un défaut ou font l’objet d’un rappel",
                "Voir l’image transmise par la caméra de la Colline du Parlement",
                "Voir la caméra des aurores boréales", "Voir les données statistiques",
                "Vols d'embarquement à destination et à l'intérieur du Canada", "Voyager au Canada",
                "Voyages hors du Canada"};
    }

    @GetMapping(value = "/topTaskSurvey")
    public ModelAndView topTaskSurvey(HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView();
        String lang = (String) request.getSession().getAttribute("lang");
        mav.setViewName("topTaskSurvey_" + lang);
        return mav;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}