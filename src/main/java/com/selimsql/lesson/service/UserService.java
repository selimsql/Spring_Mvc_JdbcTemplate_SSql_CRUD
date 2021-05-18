package com.selimsql.lesson.service;

import java.util.List;

import com.selimsql.lesson.domain.User;
import com.selimsql.lesson.dto.UserDTO;

public interface UserService {

	List<User> queryUserList(UserDTO userDTO);

	User findById(Integer id);

	User findByCode(String code);

	User findByEmail(String email);

	int fetchMaxId();

	int insertRow(User row);

	int updateRow(User row);

	int deleteRow(User row);
}