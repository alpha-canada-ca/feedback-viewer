package ca.gc.tbs.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TopTaskSurveyTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        TopTaskSurvey survey = new TopTaskSurvey(
                "id1", "dt", "ts", "pd",
                "ref", "en", "dev", "scr",
                "dept", "theme", "themeOther", "group",
                "task", "taskOther", "sat", "ease", "comp", "improve", "improveComm",
                "whyNot", "whyNotComm", "sampling", "inv", "gc", "can", "thm", "inst", "grp", "tsk"
        );
        assertEquals("id1", survey.getId());
        assertEquals("dt", survey.getDateTime());
        assertEquals("ts", survey.getTimeStamp());
        assertEquals("pd", survey.getProcessedDate());
        assertEquals("ref", survey.getSurveyReferrer());
        assertEquals("en", survey.getLanguage());
        assertEquals("dev", survey.getDevice());
        assertEquals("scr", survey.getScreener());
        assertEquals("dept", survey.getDept());
        assertEquals("theme", survey.getTheme());
        assertEquals("themeOther", survey.getThemeOther());
        assertEquals("group", survey.getGrouping());
        assertEquals("task", survey.getTask());
        assertEquals("taskOther", survey.getTaskOther());
        assertEquals("sat", survey.getTaskSatisfaction());
        assertEquals("ease", survey.getTaskEase());
        assertEquals("comp", survey.getTaskCompletion());
        assertEquals("improve", survey.getTaskImprove());
        assertEquals("improveComm", survey.getTaskImproveComment());
        assertEquals("whyNot", survey.getTaskWhyNot());
        assertEquals("whyNotComm", survey.getTaskWhyNotComment());
        assertEquals("sampling", survey.getTaskSampling());
        assertEquals("inv", survey.getSamplingInvitation());
        assertEquals("gc", survey.getSamplingGC());
        assertEquals("can", survey.getSamplingCanada());
        assertEquals("thm", survey.getSamplingTheme());
        assertEquals("inst", survey.getSamplingInstitution());
        assertEquals("grp", survey.getSamplingGrouping());
        assertEquals("tsk", survey.getSamplingTask());
    }

    @Test
    void testSetters() {
        TopTaskSurvey survey = new TopTaskSurvey();
        survey.setId("id2");
        survey.setDateTime("dateTime");
        survey.setTimeStamp("timeStamp");
        survey.setProcessedDate("processedDate");
        survey.setSurveyReferrer("referrer");
        survey.setLanguage("fr");
        survey.setDevice("device");
        survey.setScreener("screener");
        survey.setDept("dept");
        survey.setTheme("theme");
        survey.setThemeOther("themeOther");
        survey.setGrouping("grouping");
        survey.setTask("task");
        survey.setTaskOther("taskOther");
        survey.setTaskSatisfaction("satisfaction");
        survey.setTaskEase("ease");
        survey.setTaskCompletion("completion");
        survey.setTaskImprove("improve");
        survey.setTaskImproveComment("improveComment");
        survey.setTaskWhyNot("whyNot");
        survey.setTaskWhyNotComment("whyNotComment");
        survey.setTaskSampling("taskSampling");
        survey.setSamplingInvitation("invitation");
        survey.setSamplingGC("gc");
        survey.setSamplingCanada("canada");
        survey.setSamplingTheme("theme");
        survey.setSamplingInstitution("institution");
        survey.setSamplingGrouping("samplingGrouping");
        survey.setSamplingTask("samplingTask");
        survey.setProcessed("Y");
        survey.setTopTaskAirTableSync("sync");
        survey.setPersonalInfoProcessed("Y");
        survey.setAutoTagProcessed("Y");

        assertEquals("id2", survey.getId());
        assertEquals("dateTime", survey.getDateTime());
        assertEquals("timeStamp", survey.getTimeStamp());
        assertEquals("processedDate", survey.getProcessedDate());
        assertEquals("referrer", survey.getSurveyReferrer());
        assertEquals("fr", survey.getLanguage());
        assertEquals("device", survey.getDevice());
        assertEquals("screener", survey.getScreener());
        assertEquals("dept", survey.getDept());
        assertEquals("theme", survey.getTheme());
        assertEquals("themeOther", survey.getThemeOther());
        assertEquals("grouping", survey.getGrouping());
        assertEquals("task", survey.getTask());
        assertEquals("taskOther", survey.getTaskOther());
        assertEquals("satisfaction", survey.getTaskSatisfaction());
        assertEquals("ease", survey.getTaskEase());
        assertEquals("completion", survey.getTaskCompletion());
        assertEquals("improve", survey.getTaskImprove());
        assertEquals("improveComment", survey.getTaskImproveComment());
        assertEquals("whyNot", survey.getTaskWhyNot());
        assertEquals("whyNotComment", survey.getTaskWhyNotComment());
        assertEquals("taskSampling", survey.getTaskSampling());
        assertEquals("invitation", survey.getSamplingInvitation());
        assertEquals("gc", survey.getSamplingGC());
        assertEquals("canada", survey.getSamplingCanada());
        assertEquals("theme", survey.getSamplingTheme());
        assertEquals("institution", survey.getSamplingInstitution());
        assertEquals("samplingGrouping", survey.getSamplingGrouping());
        assertEquals("samplingTask", survey.getSamplingTask());
        assertEquals("Y", survey.getProcessed());
        assertEquals("sync", survey.getTopTaskAirTableSync());
        assertEquals("Y", survey.getPersonalInfoProcessed());
        assertEquals("Y", survey.getAutoTagProcessed());
    }
}