package ca.gc.tbs.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "toptasksurvey")
public class TopTaskSurvey {

	@Id
	private String id="";

	
	@Field("date-time")
	private String dateTime;
	
	@Field("survey-referrer")
	private String surveyReferrer;
	
	private String language;
	
	private String device;
	
	private String screener;
	
	
	
	
	@Field("dept-1")
	private String dept1;
	
	@Field("theme-1")
	private String theme1;
	
	@Field("grouping-1")
	private String grouping1;
	
	@Field("task-1")
	private String task1;
	
	@Field("task-1-other")
	private String task1other;
	
	
	
	
	@Field("dept-2")
	private String dept2;
	
	@Field("theme-2")
	private String theme2;
	
	@Field("grouping-2")
	private String grouping2;
	
	@Field("task-2")
	private String task2;
	
	@Field("task-2-other")
	private String task2other;
	
	
	

	@Field("task-satisfaction")
	private String taskSatisfaction;
	
	@Field("task-ease")
	private String taskEase;
	
	@Field("task-completion")
	private String taskCompletion;
	
	@Field("task-improve")
	private String taskImprove;
	
	@Field("task-improve-comment")
	private String taskImproveComment;
	
	@Field("task-why-not")
	private String taskWhyNot;
	
	@Field("task-why-not-comment")
	private String taskWhyNotComment;
	
	private String sampling;
	
	@Field("sampling-invitation")
	private String samplingInvitation;
	
	@Field("sampling-gc")
	private String samplingGC;
	
	@Field("sampling-canada")
	private String samplingCanada;
	
    @Field("sampling-theme")
    private String samplingTheme;
    
	@Field("sampling-institution")
	private String samplingInstitution;
	
	@Field("sampling-grouping")
	private String samplingGrouping;
	
	@Field("sampling-task")
	private String samplingTask;
	
	// pipeline fields
	private String processed;
	private String topTaskAirTableSync;
	private String personalInfoProcessed;
	private String autoTagProcessed;
	
	public TopTaskSurvey(String id, String dateTime, String surveyReferrer, String language, String device,
			String screener, String dept1, String theme1, String grouping1, String task1, String task1other,
			String dept2, String theme2, String grouping2, String task2, String task2other, String taskSatisfaction,
			String taskEase, String taskCompletion, String taskImprove, String taskImproveComment, String taskWhyNot,
			String taskWhyNotComment, String sampling, String samplingInvitation, String samplingGC,
			String samplingCanada, String samplingTheme, String samplingInstitution, String samplingGrouping,
			String samplingTask) {
		super();
		this.id = id;
		this.dateTime = dateTime;
		this.surveyReferrer = surveyReferrer;
		this.language = language;
		this.device = device;
		this.screener = screener;
		this.dept1 = dept1;
		this.theme1 = theme1;
		this.grouping1 = grouping1;
		this.task1 = task1;
		this.task1other = task1other;
		this.dept2 = dept2;
		this.theme2 = theme2;
		this.grouping2 = grouping2;
		this.task2 = task2;
		this.task2other = task2other;
		this.taskSatisfaction = taskSatisfaction;
		this.taskEase = taskEase;
		this.taskCompletion = taskCompletion;
		this.taskImprove = taskImprove;
		this.taskImproveComment = taskImproveComment;
		this.taskWhyNot = taskWhyNot;
		this.taskWhyNotComment = taskWhyNotComment;
		this.sampling = sampling;
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

	public String getDept1() {
		return dept1;
	}

	public void setDept1(String dept1) {
		this.dept1 = dept1;
	}

	public String getTheme1() {
		return theme1;
	}

	public void setTheme1(String theme1) {
		this.theme1 = theme1;
	}

	public String getGrouping1() {
		return grouping1;
	}

	public void setGrouping1(String grouping1) {
		this.grouping1 = grouping1;
	}

	public String getTask1() {
		return task1;
	}

	public void setTask1(String task1) {
		this.task1 = task1;
	}

	public String getTask1other() {
		return task1other;
	}

	public void setTask1other(String task1other) {
		this.task1other = task1other;
	}

	public String getDept2() {
		return dept2;
	}

	public void setDept2(String dept2) {
		this.dept2 = dept2;
	}

	public String getTheme2() {
		return theme2;
	}

	public void setTheme2(String theme2) {
		this.theme2 = theme2;
	}

	public String getGrouping2() {
		return grouping2;
	}

	public void setGrouping2(String grouping2) {
		this.grouping2 = grouping2;
	}

	public String getTask2() {
		return task2;
	}

	public void setTask2(String task2) {
		this.task2 = task2;
	}

	public String getTask2other() {
		return task2other;
	}

	public void setTask2other(String task2other) {
		this.task2other = task2other;
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

	public String getSampling() {
		return sampling;
	}

	public void setSampling(String sampling) {
		this.sampling = sampling;
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