package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.validation.Valid;



@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model m,Principal principal) {
		String username = principal.getName();
		User user = this.userRepository.getUserByUsername(username);
		m.addAttribute("user", user);
	}
	
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {
		String username = principal.getName();
		User user = this.userRepository.getUserByUsername(username);
		model.addAttribute("title", "User Dashboard");
		model.addAttribute("user", user);
		return "normal/user_dashboard";
	}
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	@PostMapping("/process-contact")
    public String addContactHandler(@Valid @ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file ,BindingResult bindingResult, Principal principal, Model model){
        try {
		String name = principal.getName();
        User user = this.userRepository.getUserByUsername(name);
        
        if(file.isEmpty()) {
        	contact.setImage("contact.png");   
        	}
        else {
        	contact.setImage(file.getOriginalFilename());
        	File saveFile = new ClassPathResource("static/img").getFile();
        	Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
        	Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        	System.out.println("File Successfully Uploaded");
        }
        contact.setUser(user);
        user.getContacts().add(contact);
        this.userRepository.save(user);
        System.out.println(contact);
        
        model.addAttribute("message",new Message("Contact Added Successfully!!","alert-success"));

        }
        catch(Exception e) {
        	System.out.println(e.getMessage());
        	e.printStackTrace();
        	model.addAttribute("message",new Message("Something went wrong","alert-danger"));
        }
        return "normal/add_contact_form";
    }
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model,Principal principal) {
		model.addAttribute("title", "Contacts");
		String username = principal.getName();
		User user = this.userRepository.getUserByUsername(username);
		Pageable pageable = PageRequest.of(page, 10);
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model m) {
		m.addAttribute("title", "Contact Details");
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
			m.addAttribute("contact", contact);
		
		return "normal/contact_details";
	}
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		this.contactRepository.delete(contact);
		String image = contact.getImage();
		if (!image.equals("default.png") && !image.isEmpty()) {
            
            try {
            	File deleteFile = new ClassPathResource("static/img").getFile();
	            String imagePath = deleteFile+File.separator+ image;

                Files.deleteIfExists(Paths.get(imagePath));
            } catch (IOException e) {
                
            	return"redirect:/user/show-contacts/0";
            }
        }
		return "redirect:/user/show-contacts/0";
	}
	
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cId, Model model) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model m, Principal principal) {
		try{
			Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()){
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldcontactDetail.getImage());
				file1.delete();

				File saveFile = new ClassPathResource("static/img").getFile();
        		Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
        		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else{
				contact.setImage(oldcontactDetail.getImage());
			}
			User user = this.userRepository.getUserByUsername(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);

		} catch(Exception e){
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}

	@GetMapping("/profile")
	public String myProfile(Model m) {
		m.addAttribute("title", "Profile Page");
		return "normal/profile";
	}

}
