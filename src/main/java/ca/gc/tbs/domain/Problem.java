package ca.gc.tbs.domain;

import java.util.ArrayList;

import java.util.List;

import org.springframework.data.annotation.Id;

//@Entity
public class Problem {
	@Id
	private String id = "";
	private String url = "";
	private int urlEntries = 0;
	private String problem = "";
	private String problemDetails = "";
	private String language = "";
	private String resolutionDate = "";
	private String resolution = "";
	private String topic = "";
	private String problemDate = "";
	private String timeStamp = "";
	private String title = "";
	private String dataOrigin = "";
	private List<String> tags = new ArrayList<>();
	public String institution = "";
	public String theme = "";
	public String section = "";

	// pipeline fields
	private String processed;
	private String airTableSync;
	private String personalInfoProcessed;
	private String autoTagProcessed;

	public Problem() {
	}

	public Problem(String id, String url, int urlEntries, String problemDate, String timeStamp, String problem,
			String problemDetails, String language,
			String resolutionDate, String resolution, String topic, String title, String institution,
			String theme, String section) {
		super();
		this.id = id;
		this.url = url;
		this.urlEntries = urlEntries;
		this.problem = problem;
		this.problemDetails = problemDetails;
		this.problemDate = problemDate;
		this.timeStamp = timeStamp;
		this.resolutionDate = resolutionDate;
		this.resolution = resolution;
		this.topic = topic;
		this.language = language;
		this.title = title;
		this.institution = institution;
		this.theme = theme;
		this.section = section;
	}

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

	public String getProblem() {
		return problem;
	}

	public void setProblem(String problem) {
		this.problem = problem;
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

	public String getResolutionDate() {
		return resolutionDate;
	}

	public void setResolutionDate(String resolutionDate) {
		this.resolutionDate = resolutionDate;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
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
}
