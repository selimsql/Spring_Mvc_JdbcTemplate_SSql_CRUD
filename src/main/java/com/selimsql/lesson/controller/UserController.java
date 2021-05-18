package com.selimsql.lesson.controller;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.selimsql.lesson.configuration.SpringAppContext;
import com.selimsql.lesson.constant.Constants;
import com.selimsql.lesson.constant.StatusEnum;
import com.selimsql.lesson.domain.User;
import com.selimsql.lesson.dto.UserDTO;
import com.selimsql.lesson.exceptions.CustomRuntimeException;
import com.selimsql.lesson.util.Util;

@Controller
@Scope(value = "session")
public class UserController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String VIEW_userList      = "userList";
	private static final String VIEW_userRow       = "userRow";
	private static final String VIEW_userRowSuccess= "userRowSuccess";

	private UserDTO userDTO;
	private List<User> listUser;


	private UserController() {
		super();
		resetUserDTO();
	}

	private UserDTO resetUserDTO() {
		this.userDTO = new UserDTO(); //Empty query params at first!
		return userDTO;
	}

	private List<User> resetListUser() {
		this.listUser = null; //Empty list at first!
		return listUser;
	}


	@RequestMapping(value = { "/", "/home", "/userList" }, method = RequestMethod.GET)
	public String userListFirst(ModelMap model) {
		resetListUser();
		return userListShow(model);
	}


	@RequestMapping(value = "/userQuery", method = RequestMethod.POST)
	public String userQuery(@Valid UserDTO userDTO, ModelMap model) {
		this.userDTO = userDTO;
		this.listUser = SpringAppContext.getUserServiceBean().queryUserList(userDTO);
		return userListShow(model);
	}

	@RequestMapping(value = "/userListShow", method = RequestMethod.GET)
	//In: this.userDTO
	//In: this.listUser
	public String userListShow(ModelMap model) {
		model.addAttribute("userDTO", userDTO);
		model.addAttribute("listUser", listUser);
		return VIEW_userList;
	}

	@RequestMapping(value = "/userListRefresh", method = RequestMethod.GET)
	public String userListRefresh(ModelMap model) {
		this.listUser = SpringAppContext.getUserServiceBean().queryUserList(userDTO);
		return userListShow(model);
	}


	/**
	 * This method will provide the page to add a new user.
	 */
	@RequestMapping(value = "/newUser", method = RequestMethod.GET)
	public String newUser(ModelMap model) {
		User user = new User();
		int maxId = SpringAppContext.getUserServiceBean().fetchMaxId();
		user.setId(Integer.valueOf(maxId + 1));
		user.setStatus(StatusEnum.ACTIVE.getValue());

		model.addAttribute("user", user);
		model.addAttribute("operation", Constants.AUTHORITYVALUE_Add);

		return VIEW_userRow;
	}


	@RequestMapping(value = "/newUser", method = RequestMethod.POST)
	public String newUserSave(@Valid User user, BindingResult result, ModelMap model) {
		Integer userId = (user==null ? null : user.getId());

		final boolean addUser = true;
		validateNewData(user, addUser, result);

		if (result.hasErrors()) {
			model.addAttribute("operation", Constants.AUTHORITYVALUE_Add);
			return VIEW_userRow;
		}

		SpringAppContext.getUserServiceBean().insertRow(user);

		model.addAttribute("success", "UserId:" + userId + ", " + user.getName() + " " + user.getSurname() + " row has been added.");

		return VIEW_userRowSuccess;
	}


	private void validateNewData(User user, boolean add, Errors errors) {
		Integer userId = (user==null ? null : user.getId());
		if (Util.getInt(userId) <= 0) {
			errors.rejectValue("id", "required.Id");
			//return;
		}
		else
		//if valid
		{
			User userBefore = SpringAppContext.getUserServiceBean().findById(userId);
			if (add) {
				if (userBefore!=null)
					errors.rejectValue("id", "Row.idX.already.exist.Please.enter.different.id", new Object[]{userId}, "Error");
			}
			else
			//if (update)
			{
				if (userBefore==null)
					errors.rejectValue("id", "message.generic", new Object[]{"UserId:" + userId + " row is absent!"}, "Error");
			}
		}

		//------------------------------
		String code = (user==null ? null : user.getCode());
		if (Util.isEmpty(code)) {
			errors.rejectValue("code", "required.Code");
		}
		else {
			User userBefore = SpringAppContext.getUserServiceBean().findByCode(code);
			if (userBefore!=null) {
				if (add) {
					errors.rejectValue("code", "Row.codeX.already.exist.Please.enter.different.code", new Object[]{code}, "Error");
				}
				else
				//if (update)
				if (Util.isNotEqual(userId, userBefore.getId()))
				{
					errors.rejectValue("code", "message.generic", new Object[]{"UserCode:" + code + " has already been defined!"}, "Error");
				}
			}

			String codeSmoothed = Util.codeFromStr(code);
			if (Util.isNotEqual(code, codeSmoothed)) {
				errors.rejectValue("code", "message.generic", new Object[]{"Code is invalid! Suggestion code:" + codeSmoothed}, "Error");
			}
		}


		//------------------------------
		String email = (user==null ? null : user.getEmail());
		if (Util.isEmpty(email)) {
			errors.rejectValue("email", "required.Email");
		}
		else {
			User userBefore = SpringAppContext.getUserServiceBean().findByEmail(email);
			if (userBefore!=null) {
				if (add) {
					errors.rejectValue("email", "Row.emailX.already.exist.Please.enter.different.email", new Object[]{email}, "Error");
				}
				else
				//if (update)
				if (Util.isNotEqual(userId, userBefore.getId()))
				{
					errors.rejectValue("email", "message.generic", new Object[]{"UserEmail:" + email + " has already been defined!"}, "Error");
				}
			}

			if (Util.isEMailValid(email)==false) {
				errors.rejectValue("email", "invalid.Email");
			}
		}


		ValidationUtils.rejectIfEmpty(errors, "name", "required.Name");
		ValidationUtils.rejectIfEmpty(errors, "surname", "required.Surname");
		ValidationUtils.rejectIfEmpty(errors, "password", "required.Password");
		ValidationUtils.rejectIfEmpty(errors, "status", "required.Status");
	}//validate_NewData


	@RequestMapping(value = "/showUser-{id}", method = RequestMethod.GET)
	public String showUser(@PathVariable String id, ModelMap model) {
		Integer userId = Integer.valueOf(Util.getInt(id));
		User user = SpringAppContext.getUserServiceBean().findById(userId);
		if (user==null)
			throw new CustomRuntimeException("PU", "UserId:" + userId + " row is absent!");

		model.addAttribute("user", user);
		model.addAttribute("operation", Constants.AUTHORITYVALUE_Show);

		return VIEW_userRow;
	}


	/**
	 * This method will provide page to update an existing user.
	 */
	@RequestMapping(value = "/updateUser-{id}", method = RequestMethod.GET)
	public String updateUser(@PathVariable String id, ModelMap model) {
		Integer userId = Integer.valueOf(Util.getInt(id));
		User user = SpringAppContext.getUserServiceBean().findById(userId);
		if (user==null)
			throw new CustomRuntimeException("PU", "UserId:" + userId + " row is absent!");

		model.addAttribute("user", user);
		model.addAttribute("operation", Constants.AUTHORITYVALUE_Update);

		return VIEW_userRow;
	}


	/**
	 * This method will be called on form submission, handling POST request for
	 * updating user in database. It also validates the user input
	 */
	@RequestMapping(value = "/updateUser-{id}", method = RequestMethod.POST)
	public String updateUserSave(@Valid User user, BindingResult result, ModelMap model, @PathVariable String id) {

		final boolean addUser = false; //update!
		validateNewData(user, addUser, result);

		if (result.hasErrors()) {
			model.addAttribute("operation", Constants.AUTHORITYVALUE_Update);
			return VIEW_userRow;
		}

		SpringAppContext.getUserServiceBean().updateRow(user);

		model.addAttribute("success", "UserId:" + id + ", " + user.getName() + " " + user.getSurname() + " row has been updated.");

		return VIEW_userRowSuccess;
	}


	@RequestMapping(value = "/deleteUser-{id}", method = RequestMethod.GET)
	public String deleteUser(@PathVariable String id, ModelMap model) {
		Integer userId = Integer.valueOf(Util.getInt(id));
		User user = SpringAppContext.getUserServiceBean().findById(userId);
		if (user==null)
			throw new CustomRuntimeException("PU", "UserId:" + userId + " row is absent!");

		model.addAttribute("user", user);
		model.addAttribute("operation", Constants.AUTHORITYVALUE_Delete);

		return VIEW_userRow;
	}

	@RequestMapping(value = "/deleteUser-{id}", method = RequestMethod.POST)
	public String deleteUserSave(@Valid User user, BindingResult result, ModelMap model, @PathVariable String id)
	{
		SpringAppContext.getUserServiceBean().deleteRow(user);

		model.addAttribute("success", "UserId:" + id + ", " + user.getName() + " " + user.getSurname() + " row has been deleted.");

		return VIEW_userRowSuccess;
		//return "redirect:/" + VIEW_userList;
	}
}