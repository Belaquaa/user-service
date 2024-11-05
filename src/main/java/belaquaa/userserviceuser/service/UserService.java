package belaquaa.userserviceuser.service;

import belaquaa.userserviceuser.model.User;

import java.util.List;

public interface UserService {
    User getUserById(Long id);

    void deleteUserById(Long id);

    List<User> getAllUsers();
}