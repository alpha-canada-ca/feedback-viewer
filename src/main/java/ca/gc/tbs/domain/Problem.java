package ca.gc.tbs.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(
    name = "problems",
    indexes = {
      @Index(name = "idx_problem_url", columnList = "url"),
      @Index(name = "idx_problem_language", columnList = "language"),
      @Index(name = "idx_problem_date", columnList = "problemDate"),
      @Index(name = "idx_problem_timestamp", columnList = "timeStamp"),
      @Index(name = "idx_problem_title", columnList = "title"),
      @Index(name = "idx_problem_processed", columnList = "processed"),
      @Index(name = "idx_problem_airtable_sync", columnList = "airTableSync"),
      @Index(name = "idx_problem_personal_info", columnList = "personalInfoProcessed"),
      @Index(name = "idx_problem_auto_tag", columnList = "autoTagProcessed"),
      @Index(name = "idx_problem_processed_date", columnList = "processedDate"),
      @Index(name = "idx_problem_institution", columnList = "institution"),
      @Index(name = "idx_problem_theme", columnList = "theme"),
      @Index(name = "idx_problem_section", columnList = "section"),
      @Index(name = "idx_problem_device_type", columnList = "deviceType"),
      @Index(name = "idx_problem_browser", columnList = "browser")
    })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "problem_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("PROBLEM")
public class Problem {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  @Column(length = 2048)
  private String url;

  private int urlEntries;

  @Column(columnDefinition = "TEXT")
  private String problemDetails;

  private String language;

  private String problemDate;

  private String timeStamp;

  @Column(length = 1024)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String dataOrigin;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "problem_tags", joinColumns = @JoinColumn(name = "problem_id"))
  @Column(name = "tag")
  private List<String> tags = new ArrayList<>();

  // Pipeline fields
  private String processed;
  private String airTableSync;
  private String personalInfoProcessed;
  private String autoTagProcessed;
  private String processedDate;

  @Column(columnDefinition = "TEXT")
  private String institution;
  @Column(columnDefinition = "TEXT")
  private String theme;
  @Column(columnDefinition = "TEXT")
  private String section;
  @Column(columnDefinition = "TEXT")
  private String oppositeLang;
  @Column(columnDefinition = "TEXT")
  private String contact;
  @Column(columnDefinition = "TEXT")
  private String deviceType;
  @Column(columnDefinition = "TEXT")
  private String browser;

  public Problem() {
    this.tags = new ArrayList<>();
  }

  public Problem(
      String id,
      String url,
      int urlEntries,
      String deviceType,
      String browser,
      String problemDate,
      String timeStamp,
      String problemDetails,
      String language,
      String title,
      String institution,
      String theme,
      String section,
      String oppositeLang,
      String contact) {
    this.id = id;
    this.url = url;
    this.urlEntries = urlEntries;
    this.deviceType = deviceType;
    this.browser = browser;
    this.problemDetails = problemDetails;
    this.problemDate = problemDate;
    this.timeStamp = timeStamp;
    this.language = language;
    this.title = title;
    this.institution = institution;
    this.theme = theme;
    this.section = section;
    this.oppositeLang = oppositeLang;
    this.contact = contact;
    this.tags = new ArrayList<>();
  }

  public Problem(Problem existingProblem) {
    this(
        existingProblem.id,
        existingProblem.url,
        existingProblem.urlEntries,
        existingProblem.deviceType,
        existingProblem.browser,
        existingProblem.problemDate,
        existingProblem.timeStamp,
        existingProblem.problemDetails,
        existingProblem.language,
        existingProblem.title,
        existingProblem.institution,
        existingProblem.theme,
        existingProblem.section,
        existingProblem.oppositeLang,
        existingProblem.contact);
    this.tags = new ArrayList<>(existingProblem.tags);
  }

  // Getter and Setter methods

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getUrlEntries() {
    return urlEntries;
  }

  public void setUrlEntries(int urlEntries) {
    this.urlEntries = urlEntries;
  }

  public String getBrowser() {
    return browser;
  }

  public void setBrowser(String browser) {
    this.browser = browser;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getProblemDetails() {
    return problemDetails;
  }

  public void setProblemDetails(String problemDetails) {
    this.problemDetails = problemDetails;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getProblemDate() {
    return problemDate;
  }

  public void setProblemDate(String problemDate) {
    this.problemDate = problemDate;
  }

  public String getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(String timeStamp) {
    this.timeStamp = timeStamp;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getProcessed() {
    return processed;
  }

  public void setProcessed(String processed) {
    this.processed = processed;
  }

  public String getAirTableSync() {
    return airTableSync;
  }

  public void setAirTableSync(String airTableSync) {
    this.airTableSync = airTableSync;
  }

  public String getDataOrigin() {
    return dataOrigin;
  }

  public void setDataOrigin(String dataOrigin) {
    this.dataOrigin = dataOrigin;
  }

  public String getPersonalInfoProcessed() {
    return personalInfoProcessed;
  }

  public void setPersonalInfoProcessed(String personalInfoProcessed) {
    this.personalInfoProcessed = personalInfoProcessed;
  }

  public String getAutoTagProcessed() {
    return autoTagProcessed;
  }

  public void setAutoTagProcessed(String autoTagProcessed) {
    this.autoTagProcessed = autoTagProcessed;
  }

  public String getInstitution() {
    return institution;
  }

  public void setInstitution(String institution) {
    this.institution = institution;
  }

  public String getTheme() {
    return theme;
  }

  public void setTheme(String theme) {
    this.theme = theme;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getOppositeLang() {
    return oppositeLang;
  }

  public void setOppositeLang(String oppositeLang) {
    this.oppositeLang = oppositeLang;
  }

  public String getContact() {
    return contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public String getProcessedDate() {
    return processedDate;
  }

  public void setProcessedDate(String processedDate) {
    this.processedDate = processedDate;
  }
}
