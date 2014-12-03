package jasp.core.security;

import jasp.core.model.Group;
import jasp.core.model.Query;
import jasp.core.model.Role;
import jasp.core.model.User;

import java.util.List;

/**
 * Data access for the security subsystem.
 */
public interface SecurityService {

    /**
     * Initializes the dao.
     * <p>
     * This method is called on startup during boot.
     * </p>
     */
    void init() throws Exception;

//    /**
//     * Queries the dao for users.
//     *
//     * @param q Query object constraining the query.
//     */
//    Iterable<User> users(Query q);
//
//    /**
//     * Adds a new user.
//     *
//     * @param user The user to add.
//     */
//    void insert(User user);
//
//    /**
//     * Updates an existing user.
//     *
//     * @param user The user to update.
//     */
//    void update(User user);
//
//    /**
//     * Deletes an existing user.
//     *
//     * @param user The user to delete.
//     */
//    void delete(User user);
//
//    /**
//     * Returns the list of roles for the specified user.
//     */
//    List<Role> roles(User user);
//
//    /**
//     * Queries the dao for groups.
//     *
//     * @param q Query object constraining the query.
//     */
//    Iterable<Group> groups(Query q);
//
//    /**
//     * Adds a new user.
//     *
//     * @param group The user to add.
//     */
//    void insert(Group group);
//
//    /**
//     * Updates an existing group.
//     *
//     * @param group The group to update.
//     */
//    void update(Group group);
//
//    /**
//     * Deletes an existing group.
//     *
//     * @param group The group to delete.
//     */
//    void delete(Group group);
//
//    /**
//     * Returns the list of groups for the specified user.
//     */
//    List<Group> groups(User user);
//
//    /**
//     * Queries the dao for roles.
//     *
//     * @param q Query object constraining the query.
//     */
//    Iterable<Role> roles(Query q);
//
//    /**
//     * Adds a new user.
//     *
//     * @param role The user to add.
//     */
//    void insert(Role role);
//
//    /**
//     * Updates an existing role.
//     *
//     * @param role The role to update.
//     */
//    void update(Role role);
//
//    /**
//     * Deletes an existing role.
//     *
//     * @param role The role to delete.
//     */
//    void delete(Role role);


}
