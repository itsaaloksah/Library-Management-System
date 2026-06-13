package com.mcsproject.lms.controller;

import com.mcsproject.lms.entity.User;
import com.mcsproject.lms.repository.UserRepository;
import com.mcsproject.lms.repository.BookRepository; // Imported your BookRepository

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

	// Added injection for BookRepository to calculate real-time inventory metrics
	@Autowired
	private BookRepository bookRepo;

	// Login Page
	@GetMapping("/login")
	public String loginPage() {
		return "login-page";
	}

	// Signup Page
	@GetMapping("/signup")
	public String signupPage() {
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
	

	// Logout
	@GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // clears login context tracking session storage properties
        return "redirect:/login";
    }
	
	// Render Profile Layout View via AJAX background call
		@GetMapping("/profile")
		public String userProfilePage(HttpSession session, Model model) {
			String username = (String) session.getAttribute("loggedInUser");
			if (username == null) {
				return "redirect:/login";
			}

			// Pull user data down from the repository to extract properties safely
			Optional<User> currentUser = userRepo.findByUsername(username);
			if (currentUser.isPresent()) {
				model.addAttribute("user", currentUser.get());
			}

			return "profile";
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

			// Match confirmation values
			if (!newPassword.equals(confirmPassword)) {
				return "PASSWORD_MATCH_ERROR";
			}

			User user = userRepo.findByUsername(username).orElse(null);
			if (user == null) return "USER_NOT_FOUND";

			// Direct raw string match check corresponding to your user login process
			if (!user.getPassword().equals(currentPassword)) {
				return "INVALID_CURRENT_PASSWORD";
			}

			// Update fields and commit database transactions
			user.setPassword(newPassword);
			userRepo.save(user);

			return "PASSWORD_UPDATE_SUCCESS";
		}
}