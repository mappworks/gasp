package jasp.core.security;

import jasp.core.model.User;
import jasp.core.util.Mapper;

class Mappers {

    static Mapper<User> user() {
        return (rs) -> (User) new User()
            .handle(rs.getString("handle"))
            .fullName(rs.getString("full_name"))
            .created(rs.getDate("created"))
            .modified(rs.getDate("modified"))
            .id(rs.getObject("id"));
    }
}
