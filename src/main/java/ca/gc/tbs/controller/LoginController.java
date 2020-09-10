package ca.gc.tbs.controller;

import ca.gc.tbs.domain.User;
import ca.gc.tbs.service.EmailService;
import ca.gc.tbs.service.UserService;

import java.text.SimpleDateFormat;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class LoginController {
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);

	@Autowired
	private UserService userService;
	
	@Autowired
	private EmailService emailService;

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView login() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("login");
		return modelAndView;
	}

	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public ModelAndView signup() {
		ModelAndView modelAndView = new ModelAndView();
		User user = new User();
		modelAndView.addObject("user", user);
		modelAndView.addObject("institutions",this.userService.findInstitutions());
		modelAndView.setViewName("signup");
		return modelAndView;
	}

	@GetMapping("/checkExists")
	public @ResponseBody String checkExists(@RequestParam String email) {
		// verify that one has not already been created.
		User userExists = userService.findUserByEmail(email);
		if (userExists != null) {
			return "true";
		} else {
			return "false";
		}
	}

	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public RedirectView createNewUser(@Valid User user, RedirectAttributes atts) {
		userService.saveUser(user);
		emailService.sendUserActivationRequestEmail(user.getEmail());
		atts.addFlashAttribute("successMessage",
				"User has been registered successfully. You will be notified when the account has been activated.");
		return new RedirectView("success");
	}

	@RequestMapping(value = { "/success" }, method = RequestMethod.GET)
	public String success() {
		return "success";
	}

	@RequestMapping(value = { "/" }, method = RequestMethod.GET)
	public View home() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("home");
		RedirectView view = new RedirectView("signin");
		return view;
	}

}
