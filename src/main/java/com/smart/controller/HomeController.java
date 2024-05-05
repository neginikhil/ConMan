package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home");
		return "index";
	}
	
	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register");
		model.addAttribute(new User());
		return "signup";
	}
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1, Model model, HttpSession session) {
		
			if(result1.hasErrors()) {
				System.out.println(result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
		
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			User result = this.userRepository.save(user);
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully added", "alert-success"));
			System.out.println(result);
			return "signup";
	}
	
	@GetMapping("/login")
	public String CustomLogin() {
		return "login";
	}
	
}
