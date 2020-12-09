package ca.gc.tbs.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "toptasksurvey")
public class TopTaskSurvey {
	
	@Id
	private String id="";
	// pipeline fields
	private String processed;
	private String topTaskAirTableSync;
	private String personalInfoProcessed;
	private String autoTagProcessed;
	
	
	//@Field("date-time")
	private String dateTime;
	
	//@Field("survey-referrer")
	private String surveyReferrer;
	
	//@Field("language")
	private String language;
	
	//@Field("device")
	private String device;
	
	//@Field("dept")
	private String dept;
	
	//@Field("theme")
	private String theme;
	
	//@Field("grouping")
	private String grouping;
	
	//@Field("task")
	private String task;
	

	//@Field("task-satisfaction")
	private String taskSatisfaction;
	
	//@Field("task-ease")
	private String taskEase;
	
	//@Field("task-complete")
	private String taskComplete;
	
	//@Field("task-complete-no-why")
	private String taskCompleteNoWhy;
	
	//@Field("task-improve")
	private String taskImprove;
	
	//@Field("sampling-invitation")
	private String samplingInvitation;
	
	//@Field("sampling-gc")
	private String samplingGC;
	
	//@Field("sampling-canada")
	private String samplingCanada;
	
   // @Field("sampling-theme")
    private String samplingTheme;
    
	//@Field("sampling-institution")
	private String samplingInstitution;
	
	//@Field("sampling-grouping")
	private String samplingGrouping;
	
	//@Field("sampling-task")
	private String samplingTask;
	
	public TopTaskSurvey( String dateTime, String surveyReferrer, String language, String device, String dept, 
	 String theme, String grouping, String task, String taskSatisfaction, String taskEase,String taskComplete,
	 String taskCompleteNoWhy, String taskImprove, String samplingInvitation,String samplingGC, String samplingCanada,
	 String samplingTheme, String samplingInstitution, String samplingGrouping, String samplingTask) {
		 this.dateTime = dateTime;
		 this.surveyReferrer = surveyReferrer;
		 this.language = language;
		 this.device = device;
		 this.dept = dept;
		 this.theme = theme;
		 this.grouping = grouping;
		 this.task = task;
		 this.taskSatisfaction = taskSatisfaction;
		 this.taskEase = taskEase;
		 this.taskComplete = taskComplete;
		 this.taskCompleteNoWhy = taskCompleteNoWhy;
		 this.taskImprove = taskImprove;
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
	public String getProcessed() {
		return processed;
	}
	public void setProcessed(String processed) {
		this.processed = processed;
	}
	public String getAirTableSync() {
		return topTaskAirTableSync;
	}
	public void setAirTableSync(String airTableSync) {
		this.topTaskAirTableSync = airTableSync;
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
	public String getDept() {
		return dept;
	}
	public void setDept(String dept) {
		this.dept = dept;
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

	public String getTaskEase() {
		return taskEase;
	}
	public void setTaskEase(String taskEase) {
		this.taskEase = taskEase;
	}
	public String getTaskComplete() {
		return taskComplete;
	}
	public void setTaskComplete(String taskComplete) {
		this.taskComplete = taskComplete;
	}
	public String getTaskCompleteNoWhy() {
		return taskCompleteNoWhy;
	}
	public void setTaskCompleteNoWhy(String taskCompleteNoWhy) {
		this.taskCompleteNoWhy = taskCompleteNoWhy;
	}
	public String getTaskSatisfaction() {
		return taskSatisfaction;
	}

	public void setTaskSatisfaction(String taskSatisfaction) {
		this.taskSatisfaction = taskSatisfaction;
	}

	public String getTaskImprove() {
		return taskImprove;
	}

	public void setTaskImprove(String taskImprove) {
		this.taskImprove = taskImprove;
	}
	public String getSamplingImprove() {
		return taskImprove;
	}
	public void setSamplingImprove(String taskImprove) {
		this.taskImprove = taskImprove;
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

}
