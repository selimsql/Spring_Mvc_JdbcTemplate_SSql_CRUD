package com.selimsql.lesson.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.selimsql.lesson.dao.UserDao;
import com.selimsql.lesson.domain.User;
import com.selimsql.lesson.dto.UserDTO;

@Service("userService")
public class UserServiceImpl implements UserService {

	//@Autowired
	private UserDao userDao;

	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public List<User> queryUserList(UserDTO userDTO) {
		List<User> list = userDao.queryUserList(userDTO);
		return list;
	}

	@Override
	public User findById(Integer id) {
		User user = userDao.findById(id);
		return user;
	}

	@Override
	public User findByCode(String code) {
		return userDao.findByCode(code);
	}

	@Override
	public User findByEmail(String email) {
		return userDao.findByEmail(email);
	}

	@Override
	public int fetchMaxId() {
		return userDao.fetchMaxId();
	}

	@Override
	public int insertRow(User row) {
		return userDao.insertRow(row);
	}

	@Override
	public int updateRow(User row) {
		return userDao.updateRow(row);
	}

	@Override
	public int deleteRow(User row) {
		return userDao.deleteRow(row);
	}
}
