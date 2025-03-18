package ca.gc.tbs.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request) {
    String lang = (String) request.getSession().getAttribute("lang");
    return "error_" + lang;
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }
}
