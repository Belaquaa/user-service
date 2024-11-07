package belaquaa.userserviceuser.service;

import belaquaa.userserviceuser.model.User;

import java.util.List;

public interface UserService {
    User createUser(User user);

    User updateUser(Long id, User user);

    User getUserById(Long id, boolean includeDeleted);

    void restoreUserById(Long id);

    List<User> getAllUsers(boolean includeDeleted);

    void deleteUserById(Long id);
}