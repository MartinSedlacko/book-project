/*
 * The book project lets a user keep track of different books they would like to read, are currently
 * reading, have read or did not finish.
 * Copyright (C) 2021  Karan Kumar
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.karankumar.bookproject.backend.controller;

import com.karankumar.bookproject.backend.dto.UserToDeleteDto;
import com.karankumar.bookproject.backend.dto.UserToRegisterDto;
import com.karankumar.bookproject.backend.model.account.User;
import com.karankumar.bookproject.backend.service.UserAlreadyRegisteredException;
import com.karankumar.bookproject.backend.service.UserService;
import com.karankumar.bookproject.constant.EmailConstant;
import com.karankumar.bookproject.service.EmailServiceImpl;
import com.karankumar.bookproject.template.EmailTemplate;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(Mappings.USER)
@Log
public class UserController {
    public static final String INCORRECT_PASSWORD_ERROR_MESSAGE =
            "The current password entered is incorrect";

    private final UserService userService;
    private final EmailServiceImpl emailService;
    private final PasswordEncoder passwordEncoder;

    private static final String USER_NOT_FOUND_ERROR_MESSAGE = "Could not find the user with ID %d";

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder, EmailServiceImpl emailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findUserById(id)
                          .orElseThrow(() ->
                                  new ResponseStatusException(HttpStatus.NOT_FOUND,
                                  String.format(USER_NOT_FOUND_ERROR_MESSAGE, id))
                          );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody UserToRegisterDto user) {
        try {
            userService.register(user);
            emailService.sendMessageUsingThymeleafTemplate(
                    user.getUsername(),
                    EmailConstant.ACCOUNT_CREATED_SUBJECT,
                    EmailTemplate.getAccountCreatedEmailTemplate(emailService.getUsernameFromEmail(user.getUsername()))
            );
        } catch (UserAlreadyRegisteredException | MessagingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email taken");
        }
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUser(@RequestBody UserToDeleteDto user) throws MessagingException {
        String password = user.getPassword();
        if (passwordEncoder.matches(password, userService.getCurrentUser().getPassword())) {
            User userEntity = userService.getCurrentUser();
            if (userEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
            userService.deleteUserById(userEntity.getId());
            emailService.sendMessageUsingThymeleafTemplate(
                    userEntity.getEmail(),
                    EmailConstant.ACCOUNT_DELETED_SUBJECT,
                    EmailTemplate.getAccountDeletedEmailTemplate(emailService.getUsernameFromEmail(userEntity.getEmail()))
                    );
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password.");
        }
   }
  
    @PostMapping("/update-password")
    @ResponseStatus(HttpStatus.OK)
    public boolean updatePassword(@RequestParam("currentPassword") String currentPassword,
                               @RequestParam("newPassword") String newPassword) throws MessagingException {
        User user = userService.getCurrentUser();

        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            userService.changeUserPassword(user, newPassword);
            emailService.sendMessageUsingThymeleafTemplate(
                    user.getEmail(),
                    EmailConstant.ACCOUNT_PASSWORD_CHANGED_SUBJECT,
                    EmailTemplate.getChangePasswordEmailTemplate(emailService.getUsernameFromEmail(user.getEmail()))
                    );
            return true;
        } else {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    INCORRECT_PASSWORD_ERROR_MESSAGE
            );
        }
    }
}
