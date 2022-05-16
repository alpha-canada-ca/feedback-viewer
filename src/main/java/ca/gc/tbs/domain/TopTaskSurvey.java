package ca.gc.tbs.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "toptasksurvey")
public class TopTaskSurvey {

	@Id
	private String id="";

	
	private String dateTime;
	private String timeStamp;
	private String surveyReferrer;
	private String language;
	private String device;
	private String screener;
	private String dept;
	private String theme;
	private String themeOther;
	private String grouping;
	private String task;
	private String taskOther;
	private String taskSatisfaction;
	private String taskEase;
	private String taskCompletion;
	private String taskImprove;
	private String taskImproveComment;
	private String taskWhyNot;
	private String taskWhyNotComment;
	private String taskSampling;
	private String samplingInvitation;
	private String samplingGC;
	private String samplingCanada;
    private String samplingTheme;
	private String samplingInstitution;
	private String samplingGrouping;
	private String samplingTask;
	
	// pipeline fields
	private String processed;
	private String topTaskAirTableSync;
	private String personalInfoProcessed;
	private String autoTagProcessed;
	
	public TopTaskSurvey() {}
	
	public TopTaskSurvey(String id, String dateTime, String timeStamp, String surveyReferrer, String language, String device,
			String screener, String dept, String theme, String themeOther, String grouping, String task, String taskOther,
			String taskSatisfaction,
			String taskEase, String taskCompletion, String taskImprove, String taskImproveComment, String taskWhyNot,
			String taskWhyNotComment, String taskSampling, String samplingInvitation, String samplingGC,
			String samplingCanada, String samplingTheme, String samplingInstitution, String samplingGrouping,
			String samplingTask) {
		super();
		this.id = id;
		this.dateTime = dateTime;
		this.timeStamp = timeStamp;
		this.surveyReferrer = surveyReferrer;
		this.language = language;
		this.device = device;
		this.screener = screener;
		this.dept = dept;
		this.theme = theme;
		this.themeOther = themeOther;
		this.grouping = grouping;
		this.task = task;
		this.taskOther = taskOther;
		this.taskSatisfaction = taskSatisfaction;
		this.taskEase = taskEase;
		this.taskCompletion = taskCompletion;
		this.taskImprove = taskImprove;
		this.taskImproveComment = taskImproveComment;
		this.taskWhyNot = taskWhyNot;
		this.taskWhyNotComment = taskWhyNotComment;
		this.taskSampling = taskSampling;
		this.samplingInvitation = samplingInvitation;
		this.samplingGC = samplingGC;
		this.samplingCanada = samplingCanada;
		this.samplingTheme = samplingTheme;
		this.samplingInstitution = samplingInstitution;
		this.samplingGrouping = samplingGrouping;
		this.samplingTask = samplingTask;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getSurveyReferrer() {
		return surveyReferrer;
	}

	public void setSurveyReferrer(String surveyReferrer) {
		this.surveyReferrer = surveyReferrer;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getScreener() {
		return screener;
	}

	public void setScreener(String screener) {
		this.screener = screener;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String getThemeOther() {
		return themeOther;
	}

	public void setThemeOther(String themeOther) {
		this.themeOther = themeOther;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getGrouping() {
		return grouping;
	}

	public void setGrouping(String grouping) {
		this.grouping = grouping;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getTaskOther() {
		return taskOther;
	}

	public void setTaskOther(String taskOther) {
		this.taskOther = taskOther;
	}

	public String getTaskSatisfaction() {
		return taskSatisfaction;
	}

	public void setTaskSatisfaction(String taskSatisfaction) {
		this.taskSatisfaction = taskSatisfaction;
	}

	public String getTaskEase() {
		return taskEase;
	}

	public void setTaskEase(String taskEase) {
		this.taskEase = taskEase;
	}

	public String getTaskCompletion() {
		return taskCompletion;
	}

	public void setTaskCompletion(String taskCompletion) {
		this.taskCompletion = taskCompletion;
	}

	public String getTaskImprove() {
		return taskImprove;
	}

	public void setTaskImprove(String taskImprove) {
		this.taskImprove = taskImprove;
	}

	public String getTaskImproveComment() {
		return taskImproveComment;
	}

	public void setTaskImproveComment(String taskImproveComment) {
		this.taskImproveComment = taskImproveComment;
	}

	public String getTaskWhyNot() {
		return taskWhyNot;
	}

	public void setTaskWhyNot(String taskWhyNot) {
		this.taskWhyNot = taskWhyNot;
	}

	public String getTaskWhyNotComment() {
		return taskWhyNotComment;
	}

	public void setTaskWhyNotComment(String taskWhyNotComment) {
		this.taskWhyNotComment = taskWhyNotComment;
	}

	public String getTaskSampling() {
		return taskSampling;
	}

	public void setTaskSampling(String sampling) {
		this.taskSampling = sampling;
	}

	public String getSamplingInvitation() {
		return samplingInvitation;
	}

	public void setSamplingInvitation(String samplingInvitation) {
		this.samplingInvitation = samplingInvitation;
	}

	public String getSamplingGC() {
		return samplingGC;
	}

	public void setSamplingGC(String samplingGC) {
		this.samplingGC = samplingGC;
	}

	public String getSamplingCanada() {
		return samplingCanada;
	}

	public void setSamplingCanada(String samplingCanada) {
		this.samplingCanada = samplingCanada;
	}

	public String getSamplingTheme() {
		return samplingTheme;
	}

	public void setSamplingTheme(String samplingTheme) {
		this.samplingTheme = samplingTheme;
	}

	public String getSamplingInstitution() {
		return samplingInstitution;
	}

	public void setSamplingInstitution(String samplingInstitution) {
		this.samplingInstitution = samplingInstitution;
	}

	public String getSamplingGrouping() {
		return samplingGrouping;
	}

	public void setSamplingGrouping(String samplingGrouping) {
		this.samplingGrouping = samplingGrouping;
	}

	public String getSamplingTask() {
		return samplingTask;
	}

	public void setSamplingTask(String samplingTask) {
		this.samplingTask = samplingTask;
	}

	public String getProcessed() {
		return processed;
	}

	public void setProcessed(String processed) {
		this.processed = processed;
	}

	public String getTopTaskAirTableSync() {
		return topTaskAirTableSync;
	}

	public void setTopTaskAirTableSync(String topTaskAirTableSync) {
		this.topTaskAirTableSync = topTaskAirTableSync;
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
	

}