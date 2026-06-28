package com.mcsproject.lms.controller;

import com.mcsproject.lms.entity.User;
import com.mcsproject.lms.repository.UserRepository;
import com.mcsproject.lms.repository.BookRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private BookRepository bookRepo;

	// Login Page (Redirects to dashboard if already logged in)
	@GetMapping("/login")
	public String loginPage(HttpSession session) {
		if (session.getAttribute("loggedInUser") != null) {
			return "redirect:/dashboard"; 
		}
		return "login-page";
	}

	// Signup Page (Redirects to dashboard if already logged in)
	@GetMapping("/signup")
	public String signupPage(HttpSession session) {
		if (session.getAttribute("loggedInUser") != null) {
			return "redirect:/dashboard";
		}
		return "signup-page";
	}
	
	// Dashboard Landing Page Route
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model, @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {

		String username = (String) session.getAttribute("loggedInUser");

		if (username == null) {
			return "redirect:/login"; 
		}

		// Calculate exact live totals across tables
		long totalBooksCount = bookRepo.count();
		long totalCategoriesCount = bookRepo.countDistinctCategories();
		long totalUsersCount = userRepo.count(); 

		// Push values to dashboard-page.html template
		model.addAttribute("username", username);
		model.addAttribute("totalBooks", totalBooksCount);
		model.addAttribute("totalCategories", totalCategoriesCount);
		model.addAttribute("totalMembers", totalUsersCount); 

		// F5 REFRESH ROUTER: Check if user was routed here from a refresh on a subpage
		String initialView = (String) session.getAttribute("loadInitialView");
		if (initialView != null) {
			model.addAttribute("initialViewRoute", initialView);
			session.removeAttribute("loadInitialView"); // Wipe it from the session immediately
		} else {
			model.addAttribute("initialViewRoute", "/dashboard"); // Default homepage landing view
		}

		// IF it's an AJAX background call, return ONLY the inner stats grid fragment!
		if ("XMLHttpRequest".equals(requestedWith)) {
			return "dashboard-page :: #dashboard-view-fragment";
		}

		return "dashboard-page";
	}

	// Process Signup
	@PostMapping("/signup")
	public String processSignup(@RequestParam String username, @RequestParam String email,
			@RequestParam String password, RedirectAttributes redirectAttributes) {

		if (userRepo.existsByEmail(email)) {
			redirectAttributes.addFlashAttribute("error", "Email already exists!");
			return "redirect:/signup";
		}

		if (userRepo.existsByUsername(username)) {
			redirectAttributes.addFlashAttribute("error", "Username already taken!");
			return "redirect:/signup";
		}

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(password);

		userRepo.save(user);

		redirectAttributes.addFlashAttribute("success", "Account created successfully 🎉");

		return "redirect:/login";
	}

	// Process Login
	@PostMapping("/login")
	public String processLogin(@RequestParam String username,
	                           @RequestParam String password,
	                           Model model,
	                           HttpSession session) {

	    Optional<User> optionalUser = userRepo.findByUsername(username);

	    if (optionalUser.isEmpty()) {
	        model.addAttribute("error", "User not found!");
	        return "login-page";
	    }

	    User user = optionalUser.get();

	    if (!user.getPassword().equals(password)) {
	        model.addAttribute("error", "Invalid password!");
	        return "login-page";
	    }

	    session.setAttribute("loggedInUser", user.getUsername());

	    return "redirect:/dashboard";
	}
	
	// Secure POST-driven logout processing execution
	@PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/login";
    }

	// ADDED: Safety fallback mapping to intercept raw browser address bar GET requests to /logout
	@GetMapping("/logout")
	public String handleGetLogoutFallback(HttpSession session) {
		// If the user is already authenticated inside the application frame, keep them there!
		if (session.getAttribute("loggedInUser") != null) {
			return "redirect:/dashboard";
		}
		// Otherwise, send them back cleanly to the login screen
		return "redirect:/login";
	}
	
	// Render Profile Layout View via AJAX background call
	@GetMapping("/profile")
	public String userProfilePage(HttpServletRequest request, HttpSession session, Model model) {
		String username = (String) session.getAttribute("loggedInUser");
		if (username == null) {
			return "redirect:/login";
		}

		Optional<User> currentUser = userRepo.findByUsername(username);
		if (currentUser.isPresent()) {
			model.addAttribute("user", currentUser.get());
		}

		// F5 Fix for Profile subpage route
		String requestedWith = request.getHeader("X-Requested-With");
		if ("XMLHttpRequest".equals(requestedWith)) {
			return "profile";
		}

		session.setAttribute("loadInitialView", "/profile");
		return "redirect:/dashboard";
	}

	// Process Change Password Form Submission Asynchronously
	@PostMapping("/profile/change-password")
	@ResponseBody
	public String changeUserPassword(@RequestParam String currentPassword,
			                         @RequestParam String newPassword,
			                         @RequestParam String confirmPassword,
			                         HttpSession session) {
		
		String username = (String) session.getAttribute("loggedInUser");
		if (username == null) return "UNAUTHORIZED";

		if (!newPassword.equals(confirmPassword)) {
			return "PASSWORD_MATCH_ERROR";
		}

		User user = userRepo.findByUsername(username).orElse(null);
		if (user == null) return "USER_NOT_FOUND";

		if (!user.getPassword().equals(currentPassword)) {
			return "INVALID_CURRENT_PASSWORD";
		}

		user.setPassword(newPassword);
		userRepo.save(user);

		return "PASSWORD_UPDATE_SUCCESS";
	}
}