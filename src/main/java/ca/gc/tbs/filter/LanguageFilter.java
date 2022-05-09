package ca.gc.tbs.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LanguageFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();

        String lang = getSelectedLang(req);

        session.setAttribute("lang", lang.equals("en") ? "en" : "fr");

        // build alt lang
        String altLang;
        String altLangText;
        if (lang.equals("en")) {
            altLang = "fr";
            altLangText = "Français";
        } else {
            altLang = "en";
            altLangText = "English";
        }

        String requestURL = req.getRequestURL().toString();
        String queryString = cleanQueryStringLangParam(req, lang);
        if (!queryString.equals("")){
            queryString += "&";
        }
        requestURL = requestURL +"?"+ queryString + "lang=" + altLang;


        session.setAttribute("langUrl", requestURL);
        session.setAttribute("altLang", altLang);
        session.setAttribute("altLangText", altLangText);

        chain.doFilter(request, response);
    }

    private String cleanQueryStringLangParam(HttpServletRequest req, String lang) {
        String queryString = req.getQueryString();
        if (queryString == null) {
            return "";
        }

        if (!queryString.contains("lang=")) {
            return queryString;
        }
        return queryString.replace("lang=" + lang, "");
    }

    private String getSelectedLang(HttpServletRequest req) {
        String lang = (String) req.getSession().getAttribute("lang");

        // if lang query param set -> set to selected language
        String langParam = req.getParameter("lang");
        if (langParam != null) {
            lang = langParam;
        }
        // find default if needed
        if (lang == null) {
            lang = LocaleContextHolder.getLocale().getLanguage();
        }
        return lang;
    }
}
