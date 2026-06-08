package com.mcsproject.lms.controller;

import com.mcsproject.lms.entity.User;
import com.mcsproject.lms.repository.UserRepository;

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

	// Dashboard
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model) {

		String username = (String) session.getAttribute("loggedInUser");

		if (username == null) {
			return "redirect:/login"; // protect page
		}

		model.addAttribute("username", username);

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
        session.invalidate(); // ✅ clears login
        return "redirect:/login";
    }
}