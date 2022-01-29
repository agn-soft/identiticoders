package com.agung.icoders.controller;

import com.agung.icoders.model.User;
import com.agung.icoders.repository.UserRepository;
import com.agung.icoders.service.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder ;

    @RequestMapping("/")
    public String viewHomePage(Model model) {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = myUserDetails.getUser();

        if (user.getRole().equals("ROLE_ADMIN")){
            List<User> listUsers = userRepository.findAll();
            model.addAttribute("listUsers", listUsers);
        }else {
            Long id = user.getId();
            Optional<User> listUsers = userRepository.findById(id);
            model.addAttribute("listUsers", listUsers.get());
        }
        return "index";
    }

    @RequestMapping("/new")
    public String showNewUserForm(Model model) {
        User user = new User();
        model.addAttribute("user", user);

        return "new_user";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveProduct(@ModelAttribute("user") User userRequest) {

        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userAuth = myUserDetails.getUser();
        if (userRequest.getId()==null){
            userRepository.save(userRequest);
        }else{
            Optional<User> userOptional = userRepository.findById(userRequest.getId());
            User user = null ;
            if(userOptional.isPresent()) {
                user = userOptional.get();

                if (userAuth.getRole().equals("ROLE_ADMIN")) {
                    if (!userRequest.getPassword().equals("")) {
                        String password = passwordEncoder.encode(userRequest.getPassword());
                        user.setPassword(password);
                    }
                    user.setUsername(userRequest.getUsername());
                    user.setRole(userRequest.getRole());
                    user.setEnabled(userRequest.isEnabled());
                }
                user.setMobileNo(userRequest.getMobileNo());
                try {
                    userRepository.save(user);
                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        }
        return "redirect:/";
    }

    @RequestMapping("/edit/{id}")
    public ModelAndView showEditUserForm(@PathVariable(name = "id") Long id) {
        ModelAndView mav = new ModelAndView("edit_user");

        Optional<User> userOptional = userRepository.findById(id);
        User user = null ;
        if(userOptional.isPresent()){
            user = userOptional.get();
            user.setPassword("");
        }
        mav.addObject("user", user);

        return mav;
    }

    @RequestMapping("/delete/{id}")
    public String deleteUser(@PathVariable(name = "id") Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        User user = null ;
        if(userOptional.isPresent()) {
            user = userOptional.get();

            userRepository.delete(user);
        }

        return "redirect:/";
    }

}
